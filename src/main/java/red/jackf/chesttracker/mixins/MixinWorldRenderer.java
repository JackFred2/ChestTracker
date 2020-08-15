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
import red.jackf.chesttracker.render.RenderUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            .lineWidth(RenderUtils.getDynamicLineWidth())
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

        List<RenderUtils.PositionData> toRemove = new ArrayList<>();
        List<RenderUtils.PositionData> renderPositions = RenderUtils.getRenderPositions();

        float r = ((ChestTracker.CONFIG.visualOptions.borderColour >> 16) & 0xff) / 255f;
        float g = ((ChestTracker.CONFIG.visualOptions.borderColour >> 8) & 0xff) / 255f;
        float b = ((ChestTracker.CONFIG.visualOptions.borderColour) & 0xff) / 255f;

        for (RenderUtils.PositionData data : renderPositions) {
            long timeDiff = this.world.getTime() - data.getStartTime();

            Vec3d finalPos = cameraPos.subtract(data.getPos().getX(), data.getPos().getY(), data.getPos().getZ()).negate();
            if (finalPos.lengthSquared() > 4096) {
                finalPos = finalPos.normalize().multiply(64);
            }

            //if (x * x + y * y + z * z < ChestTracker.CONFIG.visualOptions.borderRenderRange * ChestTracker.CONFIG.visualOptions.borderRenderRange)
            RenderUtils.optimizedDrawShapeOutline(matrices,
                immediate.getBuffer(TRACKER_RENDER_OUTLINE_LAYER),
                VoxelShapes.fullCube(),
                finalPos.x,
                finalPos.y,
                finalPos.z,
                r,
                g,
                b,
                ((ChestTracker.CONFIG.visualOptions.fadeOutTime - timeDiff) / (float) ChestTracker.CONFIG.visualOptions.fadeOutTime));

            if (timeDiff >= ChestTracker.CONFIG.visualOptions.fadeOutTime)
                toRemove.add(data);
        }

        immediate.draw(TRACKER_RENDER_OUTLINE_LAYER);
        matrices.pop();
        RenderSystem.enableDepthTest();

        if (toRemove.size() > 0)
            RenderUtils.removeRenderPositions(toRemove);
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
    }
}
