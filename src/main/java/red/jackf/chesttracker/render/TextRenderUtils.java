package red.jackf.chesttracker.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.memory.Memory;
import red.jackf.chesttracker.memory.MemoryDatabase;

import java.util.Collection;

@Environment(EnvType.CLIENT)
public abstract class TextRenderUtils {
    public static void draw(WorldRenderContext context, EntityRenderDispatcher dispatcher, Vec3d pos, Text title) {
        Vec3d finalPos = pos.subtract(context.camera().getPos()).add(0, 1, 0);

        if (finalPos.lengthSquared() <= ChestTracker.CONFIG.visualOptions.nameRenderRange * ChestTracker.CONFIG.visualOptions.nameRenderRange) {
            MatrixStack matrices = context.matrixStack();
            matrices.push();
            matrices.translate(finalPos.x, finalPos.y, finalPos.z);
            matrices.multiply(dispatcher.getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matrices.peek().getModel();
            int backgroundColour = (int) (MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            float xOffset = (float) (-textRenderer.getWidth(title) / 2);
            textRenderer.draw(title, xOffset, 0, 553648127, false, matrix4f, context.consumers(), true, backgroundColour, 15728880);
            textRenderer.draw(title, xOffset, 0, -1, false, matrix4f, context.consumers(), false, 0, 15728880);
            matrices.pop();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public static void drawLabels(WorldRenderContext context) {

        MemoryDatabase database = MemoryDatabase.getCurrent();
        if (database == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null) {
            EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
            // Named nearby
            Collection<Memory> toRender = database.getNamedMemories(mc.world.getRegistryKey().getValue());
            for (Memory memory : toRender) {
                BlockPos blockPos = memory.getPosition();
                if (blockPos != null && memory.getTitle() != null) {
                    Vec3d pos = Vec3d.ofCenter(blockPos);
                    if (memory.getNameOffset() != null) pos = pos.add(memory.getNameOffset());
                    draw(context, dispatcher, pos, memory.getTitle());
                }
            }
        }

    }
}
