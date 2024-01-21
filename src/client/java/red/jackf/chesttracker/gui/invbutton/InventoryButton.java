package red.jackf.chesttracker.gui.invbutton;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.util.Nudge;
import red.jackf.chesttracker.util.GuiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class InventoryButton extends AbstractWidget {
    private static final WidgetSprites TEXTURE = GuiUtil.twoSprite("inventory_button/button");
    static final int Z_OFFSET = 400;
    private static final int MS_BEFORE_DRAG_START = 200;
    private static final int EXPANDED_HOVER_INFLATE = 5;
    private static final int EXTRA_BUTTON_SPACING = 3;
    static final int SIZE = 9;
    static final int IMAGE_SIZE = 11;
    private final AbstractContainerScreen<?> parent;
    private ButtonPosition lastPosition;
    private ButtonPosition position;

    private boolean canDrag = false;
    private long mouseDownStart = -1;
    private boolean isDragging = false;
    private final List<SecondaryButton> secondaryButtons;
    private ScreenRectangle expandedHoverArea = ScreenRectangle.empty();

    protected InventoryButton(AbstractContainerScreen<?> parent, ButtonPosition position) {
        super(position.getX(parent), position.getY(parent), SIZE, SIZE, Component.translatable("chesttracker.title"));
        this.parent = parent;
        this.position = position;
        this.lastPosition = position;

        this.setTooltip(Tooltip.create(Component.translatable("chesttracker.title")));

        this.secondaryButtons = List.of(
                new SecondaryButton(GuiUtil.twoSprite("inventory_button/forget"), Component.translatable("chesttracker.inventory_button.forget"), () -> {}),
                new SecondaryButton(GuiUtil.twoSprite("inventory_button/rename"), Component.translatable("chesttracker.inventory_button.rename"), () -> {})
        );

        for (int i = 0; i < this.secondaryButtons.size(); i++) {
            this.secondaryButtons.get(i).setButtonIndex(i + 1);
        }

        this.applyPosition(true);
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
        graphics.blitSprite(texture, this.getX() - 1, this.getY() - 1, Z_OFFSET, IMAGE_SIZE, IMAGE_SIZE);

        for (AbstractWidget secondary : this.secondaryButtons) {
            secondary.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private boolean isExpandedHover(int mouseX, int mouseY) {
        return this.expandedHoverArea.overlaps(new ScreenRectangle(mouseX, mouseY,  1, 1));
    }

    private void applyPosition(boolean force) {
        if (!force && this.position.equals(this.lastPosition)) return;
        this.lastPosition = position;
        this.setPosition(this.position.getX(parent), this.position.getY(parent));

        var colliders = Nudge.getCollidersFor(parent);

        // get best direction
        // todo: try all and:
        //   - try to go along a non-free dir with a priority of some sort
        //   - default to right
        //   - if none free, move entire block somewhere free using Nudge#adjust
        //
        ScreenDirection freeDir = ScreenDirection.RIGHT;
        for (var dir : List.of(ScreenDirection.RIGHT, ScreenDirection.LEFT, ScreenDirection.DOWN, ScreenDirection.UP)) {
            var rect = this.rectangleFor(dir);
            if (Nudge.isFree(rect, colliders, parent.getRectangle())) {
                freeDir = dir;
                break;
            }
        }

        for (int i = 1; i <= this.secondaryButtons.size(); i++) {
            ScreenRectangle pos = Nudge.step(this.getRectangle(), freeDir, (SIZE + EXTRA_BUTTON_SPACING) * i);
            this.secondaryButtons.get(i - 1).setPosition(pos.left(), pos.top());
        }
    }

    private ScreenRectangle rectangleFor(ScreenDirection dir) {
        var boxes = new ArrayList<ScreenRectangle>();
        boxes.add(this.getRectangle());
        for (int i = 1; i <= this.secondaryButtons.size(); i++) {
            boxes.add(Nudge.step(this.getRectangle(), dir, (SIZE + EXTRA_BUTTON_SPACING) * i));
        }

        return Nudge.encompassing(boxes);
    }

    private void showExtraButtons(boolean shouldShow) {
        for (SecondaryButton secondary : this.secondaryButtons) {
            secondary.setVisible(shouldShow, this.getX(), this.getY());
        }

        if (shouldShow) {
            var encompassing = Nudge.encompassing(Stream.concat(
                    Stream.of(this.getRectangle()),
                    this.secondaryButtons.stream().map(AbstractWidget::getRectangle))
                    .toList());
            this.expandedHoverArea = Nudge.inflate(encompassing, EXPANDED_HOVER_INFLATE);
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
            var newPos = ButtonPosition.calculate(parent, (int) mouseX, (int) mouseY);
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
