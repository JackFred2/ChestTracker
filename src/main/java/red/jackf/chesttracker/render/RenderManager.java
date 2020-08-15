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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.mixins.AccessorRenderPhase;

import java.util.*;

public class RenderManager {
    public static final Map<VoxelShape, List<Box>> CACHED_SHAPES = new HashMap<>();

    @SuppressWarnings("ConstantConditions")
    public static RenderPhase.LineWidth getDynamicLineWidth() {
        RenderPhase.LineWidth layer = new RenderPhase.LineWidth(OptionalDouble.empty());
        ((AccessorRenderPhase) layer).setName("line_width_dynamic");
        ((AccessorRenderPhase) layer).setBeginAction(() -> RenderSystem.lineWidth(ChestTracker.CONFIG.visualOptions.borderWidth));
        ((AccessorRenderPhase) layer).setEndAction(() -> RenderSystem.lineWidth(1.0f));
        return layer;
    }

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

    public static void drawTextInWorld(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, Camera camera, int light, Vec3d pos, Text text, boolean force) {
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
        textRenderer.draw(text, h, 0, 0xffffffff, false, matrix4f, vertexConsumers, true, j, light);
        textRenderer.draw(text, h, 0, -1, false, matrix4f, vertexConsumers, false, 0, light);
        matrixStack.pop();
    }
}
