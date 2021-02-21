package red.jackf.chesttracker.render;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryDatabase;

import java.util.*;

@Environment(EnvType.CLIENT)
public abstract class RenderUtils {
    private static final List<PositionData> RENDER_POSITIONS = Collections.synchronizedList(new ArrayList<>());

    public static List<PositionData> getRenderPositions() {
        synchronized (RENDER_POSITIONS) {
            return ImmutableList.copyOf(RENDER_POSITIONS);
        }
    }

    public static void draw(WorldRenderContext context) {


        // context.world().getProfiler().swap("chesttracker_render_chestlabels");
        // RenderUtils.drawLabels(context.matrixStack(), this.bufferBuilders.getEntityVertexConsumers(), camera);
    }

    private static void drawTextAt(@NotNull MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, @NotNull Camera camera, double x, double y, double z, @NotNull Text text, boolean force, int textSizeModifier) {
        Vec3d renderPos = camera.getPos().negate().add(x, y, z);
        double d = renderPos.x * renderPos.x + renderPos.y * renderPos.y + renderPos.z * renderPos.z;
        if (force) {
            if (d > 16) renderPos = renderPos.multiply(4 / Math.sqrt(d));
        } else if (d > ChestTracker.CONFIG.visualOptions.nameRenderRange * ChestTracker.CONFIG.visualOptions.nameRenderRange) {
            return;
        }
        float textMod = textSizeModifier / 100f;
        matrixStack.push();
        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);
        matrixStack.multiply(camera.getRotation());
        matrixStack.scale(-0.025F, -0.025F, 0.025F);
        matrixStack.scale(textMod, textMod, textMod);
        Matrix4f matrix4f = matrixStack.peek().getModel();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int j = (int) (g * 255.0F) << 24;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
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
            Set<Memory> rendered = new HashSet<>();

            // Named Highlighted
            for (PositionData data : getRenderPositions()) {
                BlockPos blockPos = data.memory.getPosition();
                if (blockPos != null && data.memory.getTitle() != null) {
                    Vec3d pos = Vec3d.ofCenter(blockPos);
                    if (data.memory.getNameOffset() != null) pos = pos.add(data.memory.getNameOffset());
                    drawTextAt(matrices, entityVertexConsumers, camera, pos.getX(), pos.getY() + 1, pos.getZ(), data.memory.getTitle(), true, ChestTracker.CONFIG.visualOptions.textSizeModifier);
                }
                rendered.add(data.memory);
            }

            // Named nearby
            Collection<Memory> toRender = database.getNamedMemories(mc.world.getRegistryKey().getValue());
            for (Memory memory : toRender) {
                BlockPos blockPos = memory.getPosition();
                if (!rendered.contains(memory) && blockPos != null && memory.getTitle() != null) {
                    Vec3d pos = Vec3d.ofCenter(blockPos);
                    if (memory.getNameOffset() != null) pos = pos.add(memory.getNameOffset());
                    drawTextAt(matrices, entityVertexConsumers, camera, pos.getX(), pos.getY() + 1, pos.getZ(), memory.getTitle(), false, ChestTracker.CONFIG.visualOptions.textSizeModifier);
                }
            }

            // Some reason the last textRender.draw call doesn't account for FOV
            // TODO: Find the reason and remove this hack
            matrices.push();
            matrices.scale(0, 0, 0);
            drawTextAt(matrices, entityVertexConsumers, camera, 0, 0, 0, new LiteralText(""), true, ChestTracker.CONFIG.visualOptions.textSizeModifier);
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
