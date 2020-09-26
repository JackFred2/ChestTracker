package red.jackf.chesttracker.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryDatabase;
import red.jackf.chesttracker.mixins.AccessorRenderPhase;

import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
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

    public static void addRenderPositions(@NotNull Collection<Memory> memories, long startTime) {
        synchronized (RENDER_POSITIONS) {
            RENDER_POSITIONS.addAll(memories.stream()
                .map(memory -> new PositionData(memory, startTime))
                .collect(Collectors.toList()));
        }
    }

    public static List<PositionData> getRenderPositions() {
        synchronized (RENDER_POSITIONS) {
            return ImmutableList.copyOf(RENDER_POSITIONS);
        }
    }

    public static void removeRenderPositions(@NotNull Collection<PositionData> positions) {
        synchronized (RENDER_POSITIONS) {
            RENDER_POSITIONS.removeAll(positions);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static RenderPhase.@NotNull LineWidth getDynamicLineWidth() {
        RenderPhase.LineWidth layer = new RenderPhase.LineWidth(OptionalDouble.empty());
        ((AccessorRenderPhase) layer).setName("line_width_dynamic");
        ((AccessorRenderPhase) layer).setBeginAction(() -> RenderSystem.lineWidth(ChestTracker.CONFIG.visualOptions.borderWidth));
        ((AccessorRenderPhase) layer).setEndAction(() -> RenderSystem.lineWidth(1.0f));
        return layer;
    }

    // Optimized version of a voxelshape renderer, most noticeable at large counts.

    public static void optimizedDrawShapeOutline(@NotNull MatrixStack matrixStack, @NotNull VertexConsumer vertexConsumer, @NotNull VoxelShape voxelShape, double x, double y, double z, float r, float g, float b, float a) {
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


    public static void drawOutlines(@NotNull MatrixStack matrices, VertexConsumerProvider.@NotNull Immediate provider, @NotNull Camera camera, long worldTime, float tickDelta) {
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
                Memory memory = data.getMemory();
                if (memory.getPosition() == null) continue;
                Vec3d finalPos = cameraPos.subtract(memory.getPosition().getX(), memory.getPosition().getY(), memory.getPosition().getZ()).negate();
                if (finalPos.lengthSquared() > 4096) {
                    finalPos = finalPos.normalize().multiply(64);
                }

                Vec3d offset = memory.getNameOffset();
                double xSize;
                double ySize;
                double zSize;
                double xPos;
                double yPos;
                double zPos;

                if (offset == null) {
                    xPos = 0;
                    yPos = 0;
                    zPos = 0;
                    xSize = 1;
                    ySize = 1;
                    zSize = 1;
                } else {
                    xPos = Math.min(0, offset.getX() * 2);
                    yPos = Math.min(0, offset.getY() * 2);
                    zPos = Math.min(0, offset.getZ() * 2);
                    xSize = 1 + Math.abs(offset.getX() * 2);
                    ySize = 1 + Math.abs(offset.getY() * 2);
                    zSize = 1 + Math.abs(offset.getZ() * 2);
                }

                // https://www.desmos.com/calculator/bs2whnaxqp
                // scaleFactor, transparencyFactor and offset in order

                float scaleFactor = (2 * timeDiff - ChestTracker.CONFIG.visualOptions.fadeOutTime) / ChestTracker.CONFIG.visualOptions.fadeOutTime;
                scaleFactor *= scaleFactor;
                scaleFactor *= scaleFactor;
                float transparencyFactor = 1 - scaleFactor;
                scaleFactor *= scaleFactor;
                scaleFactor *= scaleFactor;
                float tweeningOffset = 0.5f * scaleFactor;
                scaleFactor = 1 - scaleFactor;

                matrices.push();
                matrices.scale(scaleFactor, scaleFactor, scaleFactor);

                optimizedDrawShapeOutline(matrices,
                    provider.getBuffer(OUTLINE_LAYER),
                    VoxelShapes.cuboid(0, 0, 0, xSize, ySize, zSize),
                    ((xSize * tweeningOffset) + finalPos.x + xPos) * (1 / scaleFactor),
                    ((ySize * tweeningOffset) + finalPos.y + yPos) * (1 / scaleFactor),
                    ((zSize * tweeningOffset) + finalPos.z + zPos) * (1 / scaleFactor),
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

    private static void drawTextAt(@NotNull MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, @NotNull Camera camera, double x, double y, double z, @NotNull Text text, boolean force) {
        Vec3d renderPos = camera.getPos().negate().add(x, y, z);
        double d = renderPos.x * renderPos.x + renderPos.y * renderPos.y + renderPos.z * renderPos.z;
        if (force) {
            if (d > 16) renderPos = renderPos.multiply(4 / Math.sqrt(d));
        } else if (d > ChestTracker.CONFIG.visualOptions.nameRenderRange * ChestTracker.CONFIG.visualOptions.nameRenderRange) {
            return;
        }
        matrixStack.push();
        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);
        matrixStack.multiply(camera.getRotation());
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

    public static void drawLabels(MatrixStack matrices, VertexConsumerProvider.Immediate entityVertexConsumers, Camera camera) {
        MemoryDatabase database = MemoryDatabase.getCurrent();
        if (database == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null) {
            // Named
            Collection<Memory> toRender = database.getNamedMemories(mc.world.getRegistryKey().getValue());
            for (Memory memory : toRender) {
                BlockPos blockPos = memory.getPosition();
                if (blockPos != null && memory.getTitle() != null) {
                    Vec3d pos = Vec3d.ofCenter(blockPos);
                    if (memory.getNameOffset() != null) pos = pos.add(memory.getNameOffset());
                    drawTextAt(matrices, entityVertexConsumers, camera, pos.getX(), pos.getY() + 1, pos.getZ(), memory.getTitle(), false);
                }
            }

            // Named Highlighted
            for (PositionData data : getRenderPositions()) {
                BlockPos blockPos = data.memory.getPosition();
                if (blockPos != null && data.memory.getTitle() != null) {
                    Vec3d pos = Vec3d.ofCenter(blockPos);
                    if (data.memory.getNameOffset() != null) pos = pos.add(data.memory.getNameOffset());
                    drawTextAt(matrices, entityVertexConsumers, camera, pos.getX(), pos.getY() + 1, pos.getZ(), data.memory.getTitle(), true);
                }
            }

            // Some reason the last textRender.draw call doesn't account for FOV
            // TODO: Find the reason and remove this hack
            matrices.push();
            matrices.scale(0, 0, 0);
            drawTextAt(matrices, entityVertexConsumers, camera, 0, 0, 0,  new LiteralText(""), true);
            matrices.pop();
        }
    }

    public static class PositionData {
        private final Memory memory;
        private final long startTime;

        public PositionData(Memory memory, long startTime) {
            this.memory = memory;
            this.startTime = startTime;
        }

        public Memory getMemory() {
            return memory;
        }

        public long getStartTime() {
            return startTime;
        }
    }
}
