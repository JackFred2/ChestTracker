package red.jackf.chesttracker.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.util.Constants;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RenderUtil {
    private RenderUtil() {}

    private static final int DEFAULT_TITLE_COLOUR = 0xFF_404040;
    public static int titleColour = DEFAULT_TITLE_COLOUR;

    private static final int TITLE_COLOUR_PIXEL_X = 0;
    private static final int TITLE_COLOUR_PIXEL_Y = 76;

    public static class TitleListener implements SimpleResourceReloadListener<Integer> {
        @Override
        public ResourceLocation getFabricId() {
            return ChestTracker.id("title_colour_listener");
        }

        @Override
        public CompletableFuture<Integer> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
            var resource = manager.getResource(Constants.TEXTURE);
            int colour = DEFAULT_TITLE_COLOUR;
            if (resource.isEmpty()) {
                ChestTracker.LOGGER.warn("Texture {} not found", Constants.TEXTURE);
            } else {
                try {
                    try (var image = NativeImage.read(resource.get().open())) {
                        if (image.getWidth() >= TITLE_COLOUR_PIXEL_X && image.getHeight() >= TITLE_COLOUR_PIXEL_Y) {
                            colour = image.getPixelRGBA(TITLE_COLOUR_PIXEL_X, TITLE_COLOUR_PIXEL_Y) | 0xFF000000;
                            ChestTracker.LOGGER.debug(String.format("Set title colour to 0x%06X", colour & 0xFFFFFF));
                        } else
                            ChestTracker.LOGGER.warn("Texture {} too small to get title colour", Constants.TEXTURE);
                    }

                } catch (IOException e) {
                    ChestTracker.LOGGER.warn("Error loading %s: ".formatted(Constants.TEXTURE), e);
                }
            }
            return CompletableFuture.completedFuture(colour);
        }

        @Override
        public CompletableFuture<Void> apply(Integer data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
            titleColour = data;
            return CompletableFuture.completedFuture(null);
        }
    }
}
