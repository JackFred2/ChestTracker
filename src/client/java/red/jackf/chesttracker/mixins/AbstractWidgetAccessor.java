package red.jackf.chesttracker.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractWidget.class)
public interface AbstractWidgetAccessor {

    @Accessor
    void setHeight(int height);

    @Invoker("renderWidget")
    void renderWidget(GuiGraphics graphics, int x, int y, float partial);

    @Invoker("updateWidgetNarration")
    void updateWidgetNarration(NarrationElementOutput output);
}
