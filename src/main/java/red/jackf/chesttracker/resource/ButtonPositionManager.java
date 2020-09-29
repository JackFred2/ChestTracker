package red.jackf.chesttracker.resource;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.GsonHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static red.jackf.chesttracker.ChestTracker.id;

@Environment(EnvType.CLIENT)
public class ButtonPositionManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static ImmutableMap<String, ButtonPosition> overrides = ImmutableMap.of();

    public ButtonPositionManager() {
        super(GsonHandler.get(), "button_positions");
    }

    public static ImmutableMap<String, ButtonPosition> getOverrides() {
        return overrides;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        Map<String, ButtonPosition> overrides = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> entry : loader.entrySet()) {
            try {
                ButtonPosition position = GsonHandler.get().fromJson(entry.getValue(), ButtonPosition.class);
                position.classNames.forEach(s -> overrides.put(s, position));
            } catch (JsonSyntaxException e) {
                ChestTracker.LOGGER.error("Parsing error loading button position override {}", entry.getKey(), e);
            }
        }

        ButtonPositionManager.overrides = ImmutableMap.copyOf(overrides);
    }

    @Override
    public Identifier getFabricId() {
        return id("button_positions");
    }

    public enum HorizontalAlignment {
        LEFT,
        RIGHT
    }

    public enum VerticalAlignment {
        TOP,
        BOTTOM
    }

    public static class ButtonPosition {
        public List<String> classNames = new ArrayList<>();
        public HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
        public int horizontalOffset = 0;
        public VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
        public int verticalOffset = 0;

        @Override
        public String toString() {
            return "ButtonPosition{" +
                "classNames=" + classNames +
                ", horizontalAlignment=" + horizontalAlignment +
                ", horizontalOffset=" + horizontalOffset +
                ", verticalAlignment=" + verticalAlignment +
                ", verticalOffset=" + verticalOffset +
                '}';
        }
    }
}
