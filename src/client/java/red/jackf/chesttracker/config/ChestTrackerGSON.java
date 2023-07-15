package red.jackf.chesttracker.config;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.function.Function;

public class ChestTrackerGSON {
    static Gson get() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .setExclusionStrategies(new ConfigExclusionStrategy())
                .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
                .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
                .registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
                .registerTypeHierarchyAdapter(MemoryIcon.class, adapterFor(MemoryIcon.CODEC))
                .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
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

    private interface JsonSerializerDeserializer<T> extends JsonSerializer<T>, JsonDeserializer<T> {}

    @SuppressWarnings("SameParameterValue")
    private static <T> JsonSerializerDeserializer<T> adapterFor(Codec<T> codec) {
        return new JsonSerializerDeserializer<>() {
            @Override
            public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return codec.decode(JsonOps.INSTANCE, json)
                        .get()
                        .map(Pair::getFirst, part -> {
                                throw new JsonParseException("Couldn't deserialize %s: %s".formatted(typeOfT.getTypeName(), part));
                            });
            }

            @Override
            public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
                return codec.encode(src, JsonOps.INSTANCE, JsonOps.INSTANCE.empty())
                        .get()
                        .map(Function.identity(), part -> {
                            throw new JsonParseException("Couldn't serialize %s: %s".formatted(typeOfSrc.getTypeName(), part));
                        });
            }
        };
    }
}
