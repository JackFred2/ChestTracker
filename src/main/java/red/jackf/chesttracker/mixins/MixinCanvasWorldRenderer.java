package red.jackf.chesttracker.mixins;

import grondag.canvas.mixinterface.WorldRendererExt;
import grondag.canvas.render.CanvasWorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.render.RenderUtils;

@Mixin(CanvasWorldRenderer.class)
@SuppressWarnings("UnresolvedMixinReference")
public abstract class MixinCanvasWorldRenderer {
    @Shadow
    private ClientWorld world;

    @Final
    @Shadow
    private WorldRendererExt wr;

    //@Inject(method = "renderWorld(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V", at = @At("TAIL"))
    @Inject(method = "Lgrondag/canvas/render/CanvasWorldRenderer;renderWorld(Lnet/minecraft/class_4587;FJZLnet/minecraft/class_4184;Lnet/minecraft/class_757;Lnet/minecraft/class_765;Lnet/minecraft/class_1159;)V", at = @At("TAIL"), remap = false)
    public void whereisit$renderOutlines(MatrixStack matrices, float tickDelta, long limitTime, boolean blockOutlines, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (!blockOutlines) return;
        this.world.getProfiler().swap("chesttracker_render_overlay");
        RenderUtils.drawOutlines(matrices, wr.canvas_bufferBuilders().getEntityVertexConsumers(), camera, this.world.getTime(), tickDelta);
        this.world.getProfiler().swap("chesttracker_render_chestlabels");
        RenderUtils.drawLabels(matrices, wr.canvas_bufferBuilders().getEntityVertexConsumers(), camera);
    }
}
