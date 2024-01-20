package red.jackf.chesttracker.gui.invbutton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.util.GuiUtil;

public class InventoryButton extends AbstractWidget {
    private static final WidgetSprites TEXTURE = GuiUtil.twoSprite("inventory_button/button");
    private static final int Z_OFFSET = 400;
    static final int SIZE = 9;
    private final AbstractContainerScreen<?> parent;
    private ButtonPosition position;

    private boolean canDrag = false;
    private boolean isDragging = false;

    protected InventoryButton(AbstractContainerScreen<?> parent, ButtonPosition position) {
        super(position.getX(parent), position.getY(parent), SIZE, SIZE, Component.translatable("chesttracker.title"));
        this.parent = parent;
        this.position = position;

        this.setTooltip(Tooltip.create(Component.translatable("chesttracker.title")));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.isDragging) {
            this.position.apply(this.parent, this);
        }
        ResourceLocation resourceLocation = TEXTURE.get(this.isActive(), this.isHoveredOrFocused());
        graphics.blitSprite(resourceLocation, this.getX(), this.getY(), Z_OFFSET, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.canDrag = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.canDrag) {
            this.isDragging = true;
            var newPos = ButtonPosition.calculate(parent, (int) mouseX, (int) mouseY);
            if (newPos.isPresent()) {
                this.position = newPos.get();
                this.position.apply(this.parent, this);
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
        if (this.isDragging) {
            this.isDragging = false;
            ButtonPositionMap.setUser(this.parent, this.position);
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
