package red.jackf.chesttracker.gui.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.profiling.ProfilerFiller;
import red.jackf.chesttracker.ChestTracker;

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
    private static final ResourceLocation TEXTURE = ChestTracker.guiTex("text_colours");
    private ImagePixelReader() {}

    private static final List<Consumer<Integer>> results = new ArrayList<>();
    private static final List<Function<NativeImage, Integer>> hooks = new ArrayList<>();

    @SuppressWarnings("SameParameterValue")
    private static void addPixelColourListener(int x, int y, int defaultColour, Consumer<Integer> result) {
        hooks.add(image -> {
            if (image.getWidth() > x && image.getHeight() > y) {
                return abgrToArgb(image.getPixelRGBA(x, y));
            } else {
                return defaultColour;
            }
        });

        results.add(result);
    }

    private static int abgrToArgb(int abgr) {
        var r = ABGR32.red(abgr);
        var g = ABGR32.green(abgr);
        var b = ABGR32.blue(abgr);
        var a = ABGR32.alpha(abgr);
        return ARGB32.color(a, r, g, b);
    }

    public static class TitleListener implements SimpleResourceReloadListener<List<Integer>> {
        @Override
        public ResourceLocation getFabricId() {
            return ChestTracker.id("pixel_colour_listener");
        }

        @Override
        public CompletableFuture<List<Integer>> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
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
        public CompletableFuture<Void> apply(List<Integer> data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
            for (int i = 0; i < data.size(); i++) {
                results.get(i).accept(data.get(i));
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    public static void setup() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ImagePixelReader.TitleListener());

        addPixelColourListener(2, 5, 0x404040, TextColours::setLabelColour);
        addPixelColourListener(2, 14, 0xFFFFFF, TextColours::setSearchTextColour);
        addPixelColourListener(2, 22, 0x669BBC, TextColours::setSearchKeyColour);
        addPixelColourListener(2, 32, 0xEECC77, TextColours::setSearchTermColour);
        addPixelColourListener(2, 40, 0xFF0000, TextColours::setSearchErrorColour);
    }
}
