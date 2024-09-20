package red.jackf.chesttracker.impl.gui.invbutton.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonParseException;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.gui.invbutton.ButtonPositionMap;
import red.jackf.chesttracker.impl.gui.invbutton.position.ButtonPosition;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class InventoryButtonPositionLoader implements SimpleResourceReloadListener<Map<String, ButtonPosition>> {
    private static final Logger LOGGER = ChestTracker.getLogger("Button Position Loader");

    private static final FileToIdConverter LISTER = FileToIdConverter.json("chesttracker_button_positions");

    @Override
    public CompletableFuture<Map<String, ButtonPosition>> load(ResourceManager manager,
                                                          ProfilerFiller profiler,
                                                          Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, ButtonPosition> positions = new HashMap<>();

            for (var entry : LISTER.listMatchingResources(manager).entrySet()) {
                ResourceLocation file = entry.getKey();
                ResourceLocation id = LISTER.fileToId(file);

                Resource resource = entry.getValue();
                try (Reader reader = resource.openAsReader()) {
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    ButtonPositionDataFile result = ButtonPositionDataFile.CODEC
                            .parse(JsonOps.INSTANCE, jsonElement)
                            .getOrThrow(JsonParseException::new);

                    for (String className : result.classNames()) {
                        positions.put(className, result.position());
                    }
                } catch (Exception ex) {
                    LOGGER.error("Couldn't read button positions {} from {} in data pack {}", id, file, resource.sourcePackId(), ex);
                }
            }

            return positions;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Map<String, ButtonPosition> data,
                                         ResourceManager manager,
                                         ProfilerFiller profiler,
                                         Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            ButtonPositionMap.loadDatapackPositions(data);
            return null;
        }, executor);
    }

    @Override
    public ResourceLocation getFabricId() {
        return ChestTracker.id("button_position_loader");
    }
}
