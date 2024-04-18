package red.jackf.chesttracker.impl.gui.invbutton.ui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.gui.ScreenBlacklist;
import red.jackf.chesttracker.api.providers.MemoryLocation;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.chesttracker.impl.gui.invbutton.ButtonPositionMap;
import red.jackf.chesttracker.impl.gui.invbutton.PositionExporter;
import red.jackf.chesttracker.impl.gui.invbutton.position.ButtonPosition;
import red.jackf.chesttracker.impl.gui.invbutton.position.PositionUtils;
import red.jackf.chesttracker.impl.gui.invbutton.position.RectangleUtils;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.chesttracker.impl.util.GuiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Main Chest Tracker button.
 */
public class InventoryButton extends AbstractWidget {
    private static final SpriteSet TEXTURE = GuiUtil.twoSprite("inventory_button/button");
    static final int Z_OFFSET = 400;
    private static final int MS_BEFORE_DRAG_START = 200;
    private static final int EXPANDED_HOVER_INFLATE = 5;
    private static final int EXTRA_BUTTON_SPACING = 3;
    public static final int SIZE = 9;
    protected static final int IMAGE_SIZE = 11;

    // used for the rename button to keep the target
    private static @Nullable Pair<AbstractContainerScreen<?>, MemoryLocation> locationToRestore = null;

    private final AbstractContainerScreen<?> parent;
    private ButtonPosition lastPosition;
    private ButtonPosition position;
    private boolean lastRecipeBookVisible;

    private boolean canDrag = false;
    private long mouseDownStart = -1;
    private boolean isDragging = false;
    private final List<SecondaryButton> secondaryButtons = new ArrayList<>();
    private ScreenRectangle expandedHoverArea = ScreenRectangle.empty();

    public InventoryButton(AbstractContainerScreen<?> parent, ButtonPosition position, Optional<MemoryLocation> target) {
        super(position.getX(parent), position.getY(parent), SIZE, SIZE, Component.translatable("chesttracker.title"));
        this.parent = parent;
        this.position = position;
        this.lastPosition = position;
        this.lastRecipeBookVisible = PositionUtils.isRecipeBookVisible(parent);

        this.setTooltip(Tooltip.create(Component.translatable("chesttracker.title")));

        if (locationToRestore != null) {
            if (locationToRestore.getFirst() == parent) { // just to make sure
                target = Optional.of(locationToRestore.getSecond());
            }
            locationToRestore = null;
        }

        // TODO only add ones relevant to the current screen - memory existing, etc.
        if (!ScreenBlacklist.isBlacklisted(parent.getClass())) {
            if (MemoryBankAccessImpl.INSTANCE.getLoadedInternal().isPresent()) {
                MemoryBankImpl bank = MemoryBankAccessImpl.INSTANCE.getLoadedInternal().get();
                if (ChestTrackerConfig.INSTANCE.instance().gui.inventoryButton.showExtra && target.isPresent()) {
                    MemoryLocation location = target.get();

                    this.secondaryButtons.add(new RememberContainerButton(bank, location));
                    this.secondaryButtons.add(new RenameButton(parent, bank, location));
                }
            }
        }

        if (ChestTrackerConfig.INSTANCE.instance().gui.inventoryButton.showExport) {
            this.secondaryButtons.add(new SecondaryButton(GuiUtil.twoSprite("inventory_button/export"), Component.translatable("chesttracker.inventoryButton.export"), () ->
                PositionExporter.export(this.parent, this.position)));
        }

        for (int i = 0; i < this.secondaryButtons.size(); i++) {
            this.secondaryButtons.get(i).setButtonIndex(i + 1);
        }

        this.applyPosition(true);
    }

    protected static void setRestoreLocation(AbstractContainerScreen<?> screen, MemoryLocation location) {
        locationToRestore = Pair.of(screen, location);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.isDragging) {
            this.applyPosition(false);
            this.showExtraButtons(this.isHovered() || this.isExpandedHover(mouseX, mouseY));
        } else {
            this.showExtraButtons(false);
        }

        // NOTE: texture is 11x11 while button is 9x9

        ResourceLocation texture = TEXTURE.get(this.isActive(), this.isHoveredOrFocused());
        GuiUtil.blit(graphics, texture, this.getX() - 1, this.getY() - 1, Z_OFFSET, IMAGE_SIZE, IMAGE_SIZE);

        for (AbstractWidget secondary : this.secondaryButtons) {
            secondary.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private boolean isExpandedHover(int mouseX, int mouseY) {
        return this.expandedHoverArea.overlaps(new ScreenRectangle(mouseX, mouseY,  1, 1));
    }

    private void applyPosition(boolean force) {
        boolean isRecipeBookVisible = PositionUtils.isRecipeBookVisible(parent);
        if (!force && this.position.equals(this.lastPosition) && isRecipeBookVisible == lastRecipeBookVisible) return;
        this.lastPosition = position;
        this.lastRecipeBookVisible = isRecipeBookVisible;
        this.setPosition(this.position.getX(parent), this.position.getY(parent));

        var colliders = RectangleUtils.getCollidersFor(parent);

        // get best direction
        // todo: try all and:
        //   - prefer towards center, horizontal first then vertical
        //   - otherwise find free space anywhere and move all
        ScreenDirection freeDir = ScreenDirection.RIGHT;
        for (var dir : List.of(ScreenDirection.RIGHT, ScreenDirection.LEFT, ScreenDirection.DOWN, ScreenDirection.UP)) {
            var rect = this.rectangleFor(dir);
            if (RectangleUtils.isFree(rect, colliders, parent.getRectangle())) {
                freeDir = dir;
                break;
            }
        }

        for (int i = 1; i <= this.secondaryButtons.size(); i++) {
            ScreenRectangle pos = RectangleUtils.step(this.getRectangle(), freeDir, (SIZE + EXTRA_BUTTON_SPACING) * i);
            this.secondaryButtons.get(i - 1).setPosition(pos.left(), pos.top());
        }
    }

    private ScreenRectangle rectangleFor(ScreenDirection dir) {
        var boxes = new ArrayList<ScreenRectangle>();
        boxes.add(this.getRectangle());
        for (int i = 1; i <= this.secondaryButtons.size(); i++) {
            boxes.add(RectangleUtils.step(this.getRectangle(), dir, (SIZE + EXTRA_BUTTON_SPACING) * i));
        }

        return RectangleUtils.encompassing(boxes);
    }

    private void showExtraButtons(boolean shouldShow) {
        for (SecondaryButton secondary : this.secondaryButtons) {
            secondary.setVisible(shouldShow, this.getX(), this.getY());
        }

        if (shouldShow) {
            var encompassing = RectangleUtils.encompassing(Stream.concat(
                    Stream.of(this.getRectangle()),
                    this.secondaryButtons.stream().map(AbstractWidget::getRectangle))
                    .toList());
            this.expandedHoverArea = RectangleUtils.inflate(encompassing, EXPANDED_HOVER_INFLATE);
        } else {
            this.expandedHoverArea = ScreenRectangle.empty();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.canDrag = true;
            this.mouseDownStart = Util.getMillis();
        }
        for (AbstractWidget secondary : this.secondaryButtons) {
            if (secondary.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.canDrag && Util.getMillis() - mouseDownStart >= MS_BEFORE_DRAG_START) {
            this.isDragging = true;
            var newPos = PositionUtils.calculate(parent, (int) mouseX, (int) mouseY);
            if (newPos.isPresent()) {
                this.position = newPos.get();
                this.applyPosition(false);
                //this.setTooltip(Tooltip.create(Component.literal(this.position.toString())));
                this.setTooltip(null);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.canDrag = false;
        this.mouseDownStart = -1;

        if (this.isDragging) {
            this.isDragging = false;
            ButtonPositionMap.saveUserPosition(this.parent, this.position);
            this.setTooltip(Tooltip.create(Component.translatable("chesttracker.title")));
            return true;
        } else if (this.isMouseOver(mouseX, mouseY)) {
            ChestTracker.openInGame(Minecraft.getInstance(), this.parent);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
