package red.jackf.chesttracker.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.render.RenderManager;

import java.util.Iterator;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    private static final RenderPhase.Transparency TRACKER_Transparency = new RenderPhase.Transparency("chesttracker_translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);

    private static final RenderLayer TRACKER_RENDER_OUTLINE_LAYER = RenderLayer.of("chesttracker_blockoutline",
        VertexFormats.POSITION_COLOR,
        1, 256,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(RenderManager.getDynamicLineWidth())
            .depthTest(new RenderPhase.DepthTest("pass", 519))
            .transparency(TRACKER_Transparency)
            .build(false)
    );

    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    private ClientWorld world;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
        at = @At(value = "TAIL"))
    public void renderFoundItemOverlay(MatrixStack matrices,
                                       float tickDelta,
                                       long limitTime,
                                       boolean renderBlockOutline,
                                       Camera camera,
                                       GameRenderer gameRenderer,
                                       LightmapTextureManager lightmapTextureManager,
                                       Matrix4f matrix4f,
                                       CallbackInfo ci) {
        this.world.getProfiler().swap("chesttracker_render_overlay");
        Vec3d cameraPos = camera.getPos();
        VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
        RenderSystem.disableDepthTest();
        matrices.push();

        Iterator<RenderManager.PositionData> iterator = RenderManager.getInstance().getPositionsToRender().iterator();

        float r = ((ChestTracker.CONFIG.visualOptions.borderColour >> 16) & 0xff) / 255f;
        float g = ((ChestTracker.CONFIG.visualOptions.borderColour >> 8) & 0xff) / 255f;
        float b = ((ChestTracker.CONFIG.visualOptions.borderColour) & 0xff) / 255f;

        while (iterator.hasNext()) {
            RenderManager.PositionData data = iterator.next();
            long timeDiff = this.world.getTime() - data.getStartTime();

            RenderManager.getInstance().optimizedDrawShapeOutline(matrices,
                immediate.getBuffer(TRACKER_RENDER_OUTLINE_LAYER),
                data.getShape(),
                data.getPos().getX() - cameraPos.getX(),
                data.getPos().getY() - cameraPos.getY(),
                data.getPos().getZ() - cameraPos.getZ(),
                r,
                g,
                b,
                ((ChestTracker.CONFIG.visualOptions.fadeOutTime - timeDiff) / (float) ChestTracker.CONFIG.visualOptions.fadeOutTime));

            if (timeDiff >= ChestTracker.CONFIG.visualOptions.fadeOutTime)
                iterator.remove();
        }

        immediate.draw(TRACKER_RENDER_OUTLINE_LAYER);
        matrices.pop();
        RenderSystem.enableDepthTest();
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V", ordinal = 1))
    public void renderLabelledChestOverlay(MatrixStack matrices,
                                           float tickDelta,
                                           long limitTime,
                                           boolean renderBlockOutline,
                                           Camera camera,
                                           GameRenderer gameRenderer,
                                           LightmapTextureManager lightmapTextureManager,
                                           Matrix4f matrix4f,
                                           CallbackInfo ci) {
        this.world.getProfiler().swap("chesttracker_chestlabels");
        RenderManager.getInstance().renderNames(matrices, this.bufferBuilders.getEntityVertexConsumers(), camera);
    }
}
