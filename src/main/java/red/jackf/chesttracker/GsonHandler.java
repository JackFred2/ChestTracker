package red.jackf.chesttracker;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import red.jackf.chesttracker.tracker.Location;

import java.lang.reflect.Type;
import java.util.List;

public final class GsonHandler {
    private GsonHandler() {
    }

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeAdapter(new TypeToken<BlockPos>() {
            }.getType(), new BlockPosSerializer())
            .registerTypeAdapter(new TypeToken<Location>() {
            }.getType(), new LocationSerializer())
            .registerTypeAdapter(new TypeToken<ItemStack>() {
            }.getType(), new ItemStackSerializer())
            .create();

    public static Gson get() {
        return GSON;
    }

    private static class LocationSerializer implements JsonDeserializer<Location>, JsonSerializer<Location> {

        @Override
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            BlockPos pos = GSON.fromJson(object.getAsJsonObject("position"), BlockPos.class);
            Text name = (object.has("name") ? GSON.fromJson(object.getAsJsonObject("name"), Text.class) : null);
            List<ItemStack> items = GSON.fromJson(object.getAsJsonArray("items"), new TypeToken<List<ItemStack>>() {
            }.getType());
            Vec3d nameOffset = (object.has("nameOffset") ? GSON.fromJson(object.getAsJsonObject("nameOffset"), Vec3d.class) : null);
            return new Location(pos, name, nameOffset, items);
        }

        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("position", GSON.toJsonTree(src.getPosition()));
            if (src.getName() != null) object.add("name", GSON.toJsonTree(src.getName()));
            object.add("items", GSON.toJsonTree(src.getItems()));
            if (src.getNameOffset() != null) object.add("nameOffset", GSON.toJsonTree(src.getNameOffset()));
            return object;
        }
    }

    private static class ItemStackSerializer implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

        @Override
        public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            try {
                return ItemStack.fromTag(StringNbtReader.parse(object.get("stack").getAsString()));
            } catch (Exception e) {
                throw new JsonParseException("Could not read item tag", e);
            }
        }

        @Override
        public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("stack", src.toTag(new CompoundTag()).toString());
            return object;
        }
    }

    private static class BlockPosSerializer implements JsonDeserializer<BlockPos>, JsonSerializer<BlockPos> {

        @Override
        public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            return new BlockPos(object.get("x").getAsInt(), object.get("y").getAsInt(), object.get("z").getAsInt());
        }

        @Override
        public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("x", src.getX());
            object.addProperty("y", src.getY());
            object.addProperty("z", src.getZ());
            return object;
        }
    }
}
