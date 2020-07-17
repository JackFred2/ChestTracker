package red.jackf.chesttracker.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.shape.VoxelShape;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.mixins.AccessorRenderPhase;

import java.util.*;
import java.util.stream.Collectors;

public class RenderManager {
    private static final RenderManager INSTANCE = new RenderManager();

    private final List<PositionData> positionsToRender = new ArrayList<>();
    public static final Map<VoxelShape, List<Box>> CACHED_SHAPES = new HashMap<>();

    public static RenderManager getInstance() {
        return INSTANCE;
    }

    private RenderManager() {
    }

    public void addRenderList(List<BlockPos> newList, long time) {
        positionsToRender.addAll(newList.stream()
                .map(blockPos -> new PositionData(time, blockPos))
                .collect(Collectors.toList()));
    }

    public List<PositionData> getPositionsToRender() {
        return positionsToRender;
    }

    public void optimizedDrawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double x, double y, double z, float r, float g, float b, float a) {
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

    @SuppressWarnings("ConstantConditions")
    public static RenderPhase.LineWidth getDynamicLineWidth() {
        RenderPhase.LineWidth layer = new RenderPhase.LineWidth(OptionalDouble.empty());
        ((AccessorRenderPhase) layer).setName("line_width_dynamic");
        ((AccessorRenderPhase) layer).setBeginAction(() -> RenderSystem.lineWidth(ChestTracker.CONFIG.generalOptions.borderWidth));
        ((AccessorRenderPhase) layer).setEndAction(() -> RenderSystem.lineWidth(1.0f));
        return layer;
    }

    public static class PositionData {
        private final long startTime;
        private final BlockPos pos;

        public PositionData(long startTime, BlockPos pos) {
            this.startTime = startTime;
            this.pos = pos;
        }

        public BlockPos getPos() {
            return pos;
        }

        public long getStartTime() {
            return startTime;
        }
    }

}
