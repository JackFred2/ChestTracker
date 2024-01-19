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

import java.util.function.Supplier;

public class InventoryButton extends AbstractWidget {
    private static final WidgetSprites TEXTURE = GuiUtil.twoSprite("inventory_button/button");
    private static final int SIZE = 9;
    private final AbstractContainerScreen<?> parent;
    private final Supplier<Integer> xSupplier;

    protected InventoryButton(AbstractContainerScreen<?> parent, Supplier<Integer> xSupplier, int y) {
        super(xSupplier.get(), y, SIZE, SIZE, Component.translatable("chesttracker.title"));
        this.parent = parent;
        this.xSupplier = xSupplier;

        this.setTooltip(Tooltip.create(Component.translatable("chesttracker.title")));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.setX(this.xSupplier.get());
        ResourceLocation resourceLocation = TEXTURE.get(this.isActive(), this.isHoveredOrFocused());
        graphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ChestTracker.openInGame(Minecraft.getInstance(), this.parent);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
