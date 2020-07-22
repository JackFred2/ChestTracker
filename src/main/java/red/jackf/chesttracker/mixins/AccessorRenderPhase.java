package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(RenderPhase.class)
public interface AccessorRenderPhase {

    @Accessor(value = "name")
    void setName(String name);

    @Accessor(value = "beginAction")
    void setBeginAction(Runnable beginAction);

    @Accessor(value = "endAction")
    void setEndAction(Runnable endAction);
}
