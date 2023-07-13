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
                .registerTypeHierarchyAdapter(LevelIcon.class, new LevelIconAdapter())
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

    public static class LevelIconAdapter implements JsonSerializer<LevelIcon>, JsonDeserializer<LevelIcon> {
        private static final String ID = "level_id";
        private static final String ICON = "icon";

        @Override
        public LevelIcon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var object = json.getAsJsonObject();
            ResourceLocation levelId = context.deserialize(object.get(ID), ResourceLocation.class);
            LightweightStack icon = context.deserialize(object.get(ICON), LightweightStack.class);
            return new LevelIcon(levelId, icon);
        }

        @Override
        public JsonElement serialize(LevelIcon src, Type typeOfSrc, JsonSerializationContext context) {
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
            var object = json.getAsJsonObject();
            var id = context.<ResourceLocation>deserialize(object.get(ID), ResourceLocation.class);
            CompoundTag tag = null;
            if (object.has(TAG)) {
                tag = context.deserialize(object.get(TAG), CompoundTag.class);
            }
            return new LightweightStack(BuiltInRegistries.ITEM.get(id), tag);
        }

        @Override
        public JsonElement serialize(LightweightStack src, Type typeOfSrc, JsonSerializationContext context) {
            var object = new JsonObject();
            object.add(ID, context.serialize(BuiltInRegistries.ITEM.getKey(src.item())));
            if (src.tag() != null) {
                object.add(TAG, context.serialize(src.tag()));
            }
            return object;
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
