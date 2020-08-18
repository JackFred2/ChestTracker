package red.jackf.chesttracker.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.mixins.AccessorRenderPhase;

import java.util.*;
import java.util.stream.Collectors;

public abstract class RenderUtils {
    private static final Map<VoxelShape, List<Box>> CACHED_SHAPES = new HashMap<>();
    private static final List<PositionData> RENDER_POSITIONS = Collections.synchronizedList(new ArrayList<>());

    private static final RenderPhase.Transparency RENDER_TRANSPARENCY = new RenderPhase.Transparency("chesttracker_translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);
    public static final RenderLayer OUTLINE_LAYER = RenderLayer.of("chesttracker_blockoutline",
        VertexFormats.POSITION_COLOR,
        1, 256,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(getDynamicLineWidth())
            .depthTest(new RenderPhase.DepthTest("pass", 519))
            .transparency(RENDER_TRANSPARENCY)
            .build(false)
    );

    public static void addRenderPositions(Collection<BlockPos> positions, long startTime) {
        synchronized (RENDER_POSITIONS) {
            RENDER_POSITIONS.addAll(positions.stream()
                .map(blockPos -> new PositionData(blockPos, startTime))
                .collect(Collectors.toList()));
        }
    }

    public static List<PositionData> getRenderPositions() {
        synchronized (RENDER_POSITIONS) {
            return ImmutableList.copyOf(RENDER_POSITIONS);
        }
    }

    public static void removeRenderPositions(Collection<PositionData> positions) {
        synchronized (RENDER_POSITIONS) {
            RENDER_POSITIONS.removeAll(positions);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static RenderPhase.LineWidth getDynamicLineWidth() {
        RenderPhase.LineWidth layer = new RenderPhase.LineWidth(OptionalDouble.empty());
        ((AccessorRenderPhase) layer).setName("line_width_dynamic");
        ((AccessorRenderPhase) layer).setBeginAction(() -> RenderSystem.lineWidth(ChestTracker.CONFIG.visualOptions.borderWidth));
        ((AccessorRenderPhase) layer).setEndAction(() -> RenderSystem.lineWidth(1.0f));
        return layer;
    }

    // Optimized version of a voxelshape renderer, most noticeable at large counts.

    public static void optimizedDrawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double x, double y, double z, float r, float g, float b, float a) {
        if (!CACHED_SHAPES.containsKey(voxelShape)) {
            List<Box> boxes = new ArrayList<>();
            voxelShape.forEachEdge(((minX, minY, minZ, maxX, maxY, maxZ) -> boxes.add(new Box(minX, minY, minZ, maxX, maxY, maxZ))));
            CACHED_SHAPES.put(voxelShape, boxes);
        }

        Matrix4f matrix = matrixStack.peek().getModel();
        List<Box> shape = CACHED_SHAPES.get(voxelShape);
        for (Box box : shape) {
            vertexConsumer.vertex(matrix, (float) (box.minX + x), (float) (box.minY + y), (float) (box.minZ + z)).color(r, g, b, a).next();
            vertexConsumer.vertex(matrix, (float) (box.maxX + x), (float) (box.maxY + y), (float) (box.maxZ + z)).color(r, g, b, a).next();
        }

    }


    public static void drawOutlines(MatrixStack matrices, VertexConsumerProvider.Immediate provider, Camera camera, long worldTime, float tickDelta) {
        Vec3d cameraPos = camera.getPos();
        RenderSystem.disableDepthTest();
        matrices.push();
        List<PositionData> toRemove = new ArrayList<>();
        List<PositionData> renderPositions = getRenderPositions();

        float r = ((ChestTracker.CONFIG.visualOptions.borderColour >> 16) & 0xff) / 255f;
        float g = ((ChestTracker.CONFIG.visualOptions.borderColour >> 8) & 0xff) / 255f;
        float b = ((ChestTracker.CONFIG.visualOptions.borderColour) & 0xff) / 255f;
        for (PositionData data : renderPositions) {
            //tickDelta = tickDelta - (tickDelta % (1f/6f));

            float timeDiff = worldTime + tickDelta - data.getStartTime();
            if (timeDiff >= ChestTracker.CONFIG.visualOptions.fadeOutTime) {
                toRemove.add(data);
            } else {
                Vec3d finalPos = cameraPos.subtract(data.getPos().getX(), data.getPos().getY(), data.getPos().getZ()).negate();
                if (finalPos.lengthSquared() > 4096) {
                    finalPos = finalPos.normalize().multiply(64);
                }

                VoxelShape shape = VoxelShapes.fullCube();

                // https://www.desmos.com/calculator/bs2whnaxqp
                // scaleFactor, transparencyFactor and offset in order

                float scaleFactor = (2 * timeDiff - ChestTracker.CONFIG.visualOptions.fadeOutTime)/ChestTracker.CONFIG.visualOptions.fadeOutTime;
                scaleFactor *= scaleFactor;
                scaleFactor *= scaleFactor;
                float transparencyFactor = 1 - scaleFactor;
                scaleFactor *= scaleFactor;
                scaleFactor *= scaleFactor;
                float offset = 0.5f * scaleFactor;
                scaleFactor = 1 - scaleFactor;

                matrices.push();
                matrices.scale(scaleFactor, scaleFactor, scaleFactor);

                optimizedDrawShapeOutline(matrices,
                    provider.getBuffer(OUTLINE_LAYER),
                    VoxelShapes.fullCube(),
                    (offset + finalPos.x) * (1/scaleFactor),
                    (offset + finalPos.y) * (1/scaleFactor),
                    (offset + finalPos.z) * (1/scaleFactor),
                    r,
                    g,
                    b,
                    transparencyFactor);

                matrices.pop();
            }
        }

        provider.draw(RenderUtils.OUTLINE_LAYER);
        matrices.pop();
        RenderSystem.enableDepthTest();

        if (toRemove.size() > 0)
            removeRenderPositions(toRemove);
    }

    public static void drawTextInWorld(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, Camera camera, Vec3d pos, Text text, boolean force) {
        Vec3d renderPos = camera.getPos().negate().add(pos);
        if (force) {
            double d = MathHelper.sqrt(renderPos.x * renderPos.x + renderPos.y * renderPos.y + renderPos.z * renderPos.z);
            if (d > 4)
                renderPos = renderPos.multiply(4 / d);
        }
        matrixStack.push();
        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);
        matrixStack.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
        matrixStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrixStack.peek().getModel();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int j = (int) (g * 255.0F) << 24;
        TextRenderer textRenderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getTextRenderer();
        float h = (float) (-textRenderer.getWidth(text) / 2);
        textRenderer.draw(text, h, 0, 0xffffffff, false, matrix4f, vertexConsumers, true, j, 0xf000f0);
        textRenderer.draw(text, h, 0, -1, false, matrix4f, vertexConsumers, false, 0, 0xf000f0);
        matrixStack.pop();
    }

    public static class PositionData {
        private final BlockPos pos;
        private final long startTime;

        public PositionData(BlockPos pos, long startTime) {
            this.pos = pos;
            this.startTime = startTime;
        }

        public BlockPos getPos() {
            return pos;
        }

        public long getStartTime() {
            return startTime;
        }
    }
}
