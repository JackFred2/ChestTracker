package red.jackf.chesttracker.config;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.memory.LightweightStack;

import java.awt.*;
import java.lang.reflect.Type;

public class ChestTrackerGSON {
    static Gson get() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .setExclusionStrategies(new ConfigExclusionStrategy())
                .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
                .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
                .registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
                .registerTypeHierarchyAdapter(LightweightStack.class, new LightweightStackAdapter())
                .registerTypeHierarchyAdapter(MemoryIcon.class, new MemoryIconAdapter())
                .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
                .registerTypeHierarchyAdapter(CompoundTag.class, new CompoundTagAdapter())
                .create();
    }

    private static class ConfigExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(ConfigEntry.class) == null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    }

    public static class MemoryIconAdapter implements JsonSerializer<MemoryIcon>, JsonDeserializer<MemoryIcon> {
        private static final String ID = "id";
        private static final String ICON = "icon";

        @Override
        public MemoryIcon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var object = json.getAsJsonObject();
            ResourceLocation levelId = context.deserialize(object.get(ID), ResourceLocation.class);
            LightweightStack icon = context.deserialize(object.get(ICON), LightweightStack.class);
            return new MemoryIcon(levelId, icon);
        }

        @Override
        public JsonElement serialize(MemoryIcon src, Type typeOfSrc, JsonSerializationContext context) {
            var object = new JsonObject();
            object.add(ID, context.serialize(src.id()));
            object.add(ICON, context.serialize(src.icon()));
            return object;
        }
    }

    public static class LightweightStackAdapter implements JsonSerializer<LightweightStack>, JsonDeserializer<LightweightStack> {
        private static final String ID = "id";
        private static final String TAG = "tag";

        @Override
        public LightweightStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ResourceLocation id;
            @Nullable CompoundTag tag;
            if (json.isJsonObject()) {
                var object = json.getAsJsonObject();
                id = context.deserialize(object.get(ID), ResourceLocation.class);
                tag = context.deserialize(object.get(TAG), CompoundTag.class);
            } else {
                id = context.deserialize(json, ResourceLocation.class);
                tag = null;
            }
            return new LightweightStack(BuiltInRegistries.ITEM.get(id), tag);
        }

        @Override
        public JsonElement serialize(LightweightStack src, Type typeOfSrc, JsonSerializationContext context) {
            var idTag = context.serialize(BuiltInRegistries.ITEM.getKey(src.item()));
            if (src.tag() != null) {
                var object = new JsonObject();
                object.add(ID, idTag);
                object.add(TAG, context.serialize(src.tag()));
                return object;
            } else {
                return idTag;
            }
        }
    }

    public static class CompoundTagAdapter implements JsonSerializer<CompoundTag>, JsonDeserializer<CompoundTag> {

        @Override
        public CompoundTag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var tag = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
            try {
                return (CompoundTag) tag;
            } catch (ClassCastException ex) {
                throw new JsonParseException(ex);
            }
        }

        @Override
        public JsonElement serialize(CompoundTag src, Type typeOfSrc, JsonSerializationContext context) {
            return NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, src);
        }
    }
}
