package red.jackf.chesttracker.impl.gui.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import red.jackf.chesttracker.impl.ChestTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Reads colours from an image every reload. Used to get text colours .
 */
public class ImagePixelReader {
    private static final ResourceLocation TEXTURE = ChestTracker.id("textures/gui/text_colours.png");

    private ImagePixelReader() {
    }

    private static final List<Consumer<Integer>> results = new ArrayList<>();
    private static final List<Function<NativeImage, Integer>> hooks = new ArrayList<>();

    @SuppressWarnings("SameParameterValue")
    private static void addPixelColourListener(int x, int y, int defaultColour, Consumer<Integer> result) {
        hooks.add(image -> {
            if (image.getWidth() > x && image.getHeight() > y) {
                return image.getPixel(x, y);
            } else {
                return defaultColour;
            }
        });

        results.add(result);
    }

    public static class TitleListener implements SimpleResourceReloadListener<List<Integer>> {
        @Override
        public ResourceLocation getFabricId() {
            return ChestTracker.id("pixel_colour_listener");
        }

        @Override
        public CompletableFuture<List<Integer>> load(ResourceManager manager, Executor executor) {
            var resource = manager.getResource(TEXTURE);
            var list = new ArrayList<Integer>();
            if (resource.isEmpty()) {
                ChestTracker.LOGGER.warn("Texture {} not found", TEXTURE);
            } else {
                try (var image = NativeImage.read(resource.get().open())) {
                    for (var hook : hooks)
                        list.add(hook.apply(image));
                } catch (IOException e) {
                    ChestTracker.LOGGER.warn("Error loading %s: ".formatted(TEXTURE), e);
                }
            }
            return CompletableFuture.completedFuture(list);
        }

        @Override
        public CompletableFuture<Void> apply(List<Integer> data, ResourceManager manager, Executor executor) {
            for (int i = 0; i < data.size(); i++) {
                results.get(i).accept(data.get(i));
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    public static void setup() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new ImagePixelReader.TitleListener());

        addPixelColourListener(2, 5, 0x404040, TextColours::setLabelColour);
        addPixelColourListener(2, 14, 0xFFFFFF, TextColours::setTextColour);
        addPixelColourListener(2, 23, 0x808080, TextColours::setHintColour);
        addPixelColourListener(2, 31, 0x669BBC, TextColours::setSearchKeyColour);
        addPixelColourListener(2, 41, 0xEECC77, TextColours::setSearchTermColour);
        addPixelColourListener(2, 49, 0xFF0000, TextColours::setErrorColour);
    }
}
