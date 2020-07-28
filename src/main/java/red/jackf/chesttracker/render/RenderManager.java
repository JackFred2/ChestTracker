package red.jackf.chesttracker.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.mixins.AccessorRenderPhase;
import red.jackf.chesttracker.tracker.LinkedBlocksHandler;
import red.jackf.chesttracker.tracker.Location;
import red.jackf.chesttracker.tracker.LocationStorage;

import java.util.*;
import java.util.stream.Collectors;

public class RenderManager {
    public static final Map<VoxelShape, List<Box>> CACHED_SHAPES = new HashMap<>();
    private static final RenderManager INSTANCE = new RenderManager();
    private final List<PositionData> positionsToRender = new ArrayList<>();

    private RenderManager() {
    }

    public static RenderManager getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("ConstantConditions")
    public static RenderPhase.LineWidth getDynamicLineWidth() {
        RenderPhase.LineWidth layer = new RenderPhase.LineWidth(OptionalDouble.empty());
        ((AccessorRenderPhase) layer).setName("line_width_dynamic");
        ((AccessorRenderPhase) layer).setBeginAction(() -> RenderSystem.lineWidth(ChestTracker.CONFIG.visualOptions.borderWidth));
        ((AccessorRenderPhase) layer).setEndAction(() -> RenderSystem.lineWidth(1.0f));
        return layer;
    }

    public void addRenderList(List<Location> newList, long time) {
        positionsToRender.addAll(newList.stream()
            .map(loc -> {
                double x = loc.getPosition().getX();
                double y = loc.getPosition().getY();
                double z = loc.getPosition().getZ();
                return new PositionData(time, loc.getPosition(),
                    MinecraftClient.getInstance().world != null ?
                        getShapeFromList(LinkedBlocksHandler.getLinked(MinecraftClient.getInstance().world, loc.getPosition())).offset(-x, -y, -z).simplify() :
                        VoxelShapes.fullCube()
                );
            })
            .collect(Collectors.toList()));
    }

    private VoxelShape getShapeFromList(List<BlockPos> locations) {
        VoxelShape base = VoxelShapes.empty();
        for (BlockPos pos : locations)
            base = VoxelShapes.union(base, VoxelShapes.fullCube().offset(pos.getX(), pos.getY(), pos.getZ()));

        return base;
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

    public void drawTextInWorld(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, Camera camera, int light, Vec3d pos, Text text, boolean force) {
        Vec3d renderPos = camera.getPos().negate().add(pos);
        if (force) {
            double d = MathHelper.sqrt(renderPos.x * renderPos.x + renderPos.y * renderPos.y + renderPos.z * renderPos.z);
            if (d > 4)
                renderPos = renderPos.multiply(4 / d);
        }
        matrixStack.push();
        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);
        matrixStack.multiply(MinecraftClient.getInstance().getEntityRenderManager().getRotation());
        matrixStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrixStack.peek().getModel();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int j = (int) (g * 255.0F) << 24;
        TextRenderer textRenderer = MinecraftClient.getInstance().getEntityRenderManager().getTextRenderer();
        float h = (float) (-textRenderer.getWidth(text) / 2);
        textRenderer.draw(text, h, 0, 0xffffffff, false, matrix4f, vertexConsumers, true, j, light);
        textRenderer.draw(text, h, 0, -1, false, matrix4f, vertexConsumers, false, 0, light);
        matrixStack.pop();
    }

    public void renderNames(MatrixStack matrices, VertexConsumerProvider.Immediate entityVertexConsumers, Camera camera) {
        LocationStorage storage = LocationStorage.get();
        if (storage == null) return;
        assert MinecraftClient.getInstance().world != null;
        List<BlockPos> renderedPositions = positionsToRender.stream().map(data -> data.pos).collect(Collectors.toList());
        for (Location location : storage.getStorage(MinecraftClient.getInstance().world.getRegistryKey().getValue())) {
            if (location.getName() != null) {
                if (renderedPositions.contains(location.getPosition())) {
                    RenderManager.getInstance().drawTextInWorld(matrices, entityVertexConsumers, camera, 0xf000f0,
                        Vec3d.of(location.getPosition()).add(0.5, 1.5, 0.5).add(location.hasNameOffset() ? location.getNameOffset() : Vec3d.ZERO),
                        location.getName(),
                        true
                    );
                } else if (location.getName() != null && camera.getPos().squaredDistanceTo(Vec3d.of(location.getPosition())) <= (ChestTracker.CONFIG.visualOptions.nameRenderRange * ChestTracker.CONFIG.visualOptions.nameRenderRange))
                    RenderManager.getInstance().drawTextInWorld(matrices, entityVertexConsumers, camera, 0xf000f0,
                        Vec3d.of(location.getPosition()).add(0.5, 1.5, 0.5).add(location.hasNameOffset() ? location.getNameOffset() : Vec3d.ZERO),
                        location.getName(),
                        false
                    );
            }
        }
    }

    public static class PositionData {
        private final long startTime;
        private final BlockPos pos;
        private final VoxelShape shape;

        public PositionData(long startTime, BlockPos pos, VoxelShape shape) {
            this.startTime = startTime;
            this.pos = pos;
            this.shape = shape;
        }

        public BlockPos getPos() {
            return pos;
        }

        public long getStartTime() {
            return startTime;
        }

        public VoxelShape getShape() {
            return shape;
        }
    }

}
