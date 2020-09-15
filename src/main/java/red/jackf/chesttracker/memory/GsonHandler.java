package red.jackf.chesttracker.memory;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Type;

@Environment(EnvType.CLIENT)
public class GsonHandler {
    private static final Gson GSON = new GsonBuilder()
        //.setPrettyPrinting()
        .registerTypeAdapter(BlockPos.class, new BlockPosSerializer())
        .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
        .registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
        .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
        .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
        .enableComplexMapKeySerialization()
        .create();

    private GsonHandler() {
    }

    public static Gson get() {
        return GSON;
    }

    private static class BlockPosSerializer implements JsonSerializer<BlockPos>, JsonDeserializer<BlockPos> {
        @Override
        public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray array = json.getAsJsonArray();
            return new BlockPos(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
        }

        @Override
        public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            array.add(src.getX());
            array.add(src.getY());
            array.add(src.getZ());
            return array;
        }
    }

    private static class IdentifierSerializer implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {
        @Override
        public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Identifier(json.getAsJsonPrimitive().getAsString());
        }

        @Override
        public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

        @Override
        public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return ItemStack.fromTag(StringNbtReader.parse(json.getAsString()));
            } catch (Exception ex) {
                throw new JsonParseException("Could not read item", ex);
            }
        }

        @Override
        public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toTag(new CompoundTag()).toString());
        }
    }
}
