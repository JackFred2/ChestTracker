package red.jackf.chesttracker.impl.gui.invbutton.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.providers.MemoryLocation;
import red.jackf.chesttracker.impl.gui.widget.SimpleItemWidget;
import red.jackf.chesttracker.impl.gui.widget.TextWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RenameInputScreen extends Screen {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int BUTTON_GAP = 5;
    private static final int PADDING = 15;
    private static final int GAP = 15;

    private final MemoryLocation location;
    private final @Nullable String currentName;
    private final ItemStack preview;
    private final @Nullable String inWorldName;
    private final Consumer<Optional<String>> callback;
    private boolean returned = false;
    private @Nullable EditBox nameInput = null;

    protected RenameInputScreen(MemoryLocation location, @Nullable String currentName, ItemStack preview, @Nullable String inWorldName, Consumer<Optional<String>> callback) {
        super(Component.translatable("chesttracker.inventoryButton.rename"));
        this.location = location;
        this.currentName = currentName;
        this.preview = preview;
        this.inWorldName = inWorldName;
        this.callback = callback;
    }

    @Override
    protected void init() {
        final int left = (this.width - WIDTH) / 2 + PADDING;
        final int top = (this.height - HEIGHT) / 2 + PADDING;
        final int width = WIDTH - (2 * PADDING);
        final int height = HEIGHT - (2 * PADDING);

        this.addRenderableOnly(new TextWidget(left,
                top,
                width,
                Component.translatable("chesttracker.inventoryButton.rename"),
                0xFF_FFFFFF,
                TextWidget.Alignment.CENTER));

        final int editBoxTop = top + 2 * GAP;

        this.nameInput = this.addRenderableWidget(new EditBox(this.font,
                left,
                editBoxTop,
                width,
                20,
                this.nameInput,
                Component.empty()));

        this.nameInput.setCanLoseFocus(false);
        this.setFocused(this.nameInput);

        this.nameInput.setResponder(str -> {
            if (str.isBlank() && this.inWorldName != null) {
                this.nameInput.setSuggestion(this.inWorldName);
            } else {
                this.nameInput.setSuggestion("");
            }
        });
        this.nameInput.setValue(this.currentName != null ? this.currentName : "");

        final int blockPreviewSize = (int) (width * 0.4f);
        final int infoWidth = width - blockPreviewSize;
        final int infoTop = top + 4 * GAP;
        final int infoLeft = left + blockPreviewSize;

        this.addRenderableOnly(new SimpleItemWidget(this.preview, left, infoTop, blockPreviewSize));

        this.addRenderableOnly(new TextWidget(infoLeft,
                infoTop,
                infoWidth,
                Component.literal(location.position().toShortString() + "@" + location.memoryKey().toString()),
                0xFF_D0D0D0,
                TextWidget.Alignment.LEFT));

        this.addRenderableOnly(new MultiLineTextWidget(infoLeft,
                infoTop + GAP,
                Component.translatable("chesttracker.inventoryButton.rename.clientOnlyWarning"),
                this.font)).setMaxWidth(width - blockPreviewSize);

        final List<Button> bottomButtons = new ArrayList<>(3);

        bottomButtons.add(Button.builder(CommonComponents.GUI_DONE, b -> this.complete(Optional.of(this.nameInput.getValue().strip()))).build());

        if (this.currentName != null)
            bottomButtons.add(Button.builder(Component.translatable("chesttracker.inventoryButton.rename.remove"), b -> this.complete(Optional.of(""))).build());

        bottomButtons.add(Button.builder(CommonComponents.GUI_CANCEL, b -> this.complete(Optional.empty())).build());

        final int buttonTop = infoTop + blockPreviewSize;
        final int buttonWidth = (width - (bottomButtons.size() - 1) * BUTTON_GAP) / bottomButtons.size();

        for (int i = 0; i < bottomButtons.size(); i++) {
            Button button = bottomButtons.get(i);
            button.setPosition(left + i * (buttonWidth + BUTTON_GAP), buttonTop);
            button.setWidth(buttonWidth);
            this.addRenderableWidget(button);
        }
    }

    private void complete(Optional<String> result) {
        if (this.returned) return;
        this.returned = true;
        this.callback.accept(result);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.complete(Optional.empty());
    }
}
