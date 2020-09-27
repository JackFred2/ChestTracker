package red.jackf.chesttracker;

import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import red.jackf.chesttracker.ChestTracker;

import java.lang.reflect.Type;

@Environment(EnvType.CLIENT)
public class GsonHandler {
    private static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder()
            .registerTypeAdapter(BlockPos.class, new BlockPosSerializer())
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
            .enableComplexMapKeySerialization();
            if (ChestTracker.CONFIG.databaseOptions.readableFiles) builder.setPrettyPrinting();
            GSON = builder.create();
    }

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
                JsonObject object = json.getAsJsonObject();
                Identifier id = context.deserialize(object.get("id"), Identifier.class);
                int count = object.getAsJsonPrimitive("count").getAsInt();
                ItemStack stack = new ItemStack(Registry.ITEM.get(id), count);
                JsonPrimitive tagJson = object.getAsJsonPrimitive("tag");
                if (tagJson != null) stack.setTag(StringNbtReader.parse(tagJson.getAsString()));
                return stack;
            } catch (Exception ex) {
                throw new JsonParseException("Could not read item", ex);
            }
        }

        @Override
        public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("id", context.serialize(Registry.ITEM.getId(src.getItem())));
            object.addProperty("count", src.getCount());
            CompoundTag tag = src.getTag();
            if (tag != null) object.addProperty("tag", tag.toString());
            return object;
        }
    }
}
