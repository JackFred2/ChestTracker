package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
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

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    private ClientWorld world;

    // Outline Rendering

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
        at = @At(value = "TAIL"))
    public void chestTracker$render(MatrixStack matrices,
                                    float tickDelta,
                                    long limitTime,
                                    boolean renderBlockOutline,
                                    Camera camera,
                                    GameRenderer gameRenderer,
                                    LightmapTextureManager lightmapTextureManager,
                                    Matrix4f matrix4f,
                                    CallbackInfo ci) {
        if (!renderBlockOutline) return;
        this.world.getProfiler().swap("chesttracker_render_overlay");
        RenderUtils.drawOutlines(matrices, this.bufferBuilders.getEntityVertexConsumers(), camera, this.world.getTime(), tickDelta);
        this.world.getProfiler().swap("chesttracker_render_chestlabels");
        RenderUtils.drawLabels(matrices, this.bufferBuilders.getEntityVertexConsumers(), camera);
    }

    // Text Rendering

    /*@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V", ordinal = 1))
    public void chestTracker$renderLabelledChestOverlay(MatrixStack matrices,
                                                        float tickDelta,
                                                        long limitTime,
                                                        boolean renderBlockOutline,
                                                        Camera camera,
                                                        GameRenderer gameRenderer,
                                                        LightmapTextureManager lightmapTextureManager,
                                                        Matrix4f matrix4f,
                                                        CallbackInfo ci) {
        if (!renderBlockOutline) return;
        this.world.getProfiler().swap("chesttracker_render_chestlabels");
        RenderUtils.drawLabels(matrices, this.bufferBuilders.getEntityVertexConsumers(), camera);
    }*/
}
