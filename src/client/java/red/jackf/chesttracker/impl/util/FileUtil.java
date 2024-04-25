package red.jackf.chesttracker.impl.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.*;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Utilities for working with files.
 */
public class FileUtil {
    public static final Logger LOGGER = ChestTracker.getLogger("FileUtil");
    private static final Gson GSON_COMPACT = new GsonBuilder().create();
    private static final Gson GSON = GSON_COMPACT.newBuilder().setPrettyPrinting().create();

    /**
     * Save an object to a path with a given codec as an NBT file
     *
     * @param object Object to serialize
     * @param codec  Codec to serialize said object with
     * @param path   Path to save the object to
     * @param <T>    Type of the serialized object
     * @return Whether the save was successful
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static <T> boolean saveToNbt(T object, Codec<T> codec, Path path) {
        try {
            Files.createDirectories(path.getParent());
            DataResult<Tag> tag = codec.encodeStart(NbtOps.INSTANCE, object);

            if (tag.isError()) {
                throw new IOException("Error encoding to NBT %s".formatted(tag.error().get()));
            } else if (tag.isSuccess() && tag.result().get() instanceof CompoundTag compound) {
                NbtIo.writeCompressed(compound, path);
                return true;
            } else {
                throw new IOException("Error encoding to NBT: not a compound tag: %s".formatted(tag.result().get()));
            }
        } catch (IOException ex) {
            LOGGER.error("Error saving object", ex);
            return false;
        }
    }

    /**
     * Load an NBT file to an object using a given codec
     *
     * @param codec Codec to deserialize with
     * @param path  Path to read from
     * @param <T>   Type of deserialized object
     * @return An optional containing the deserialized object, or an empty optional if errored
     */
    public static <T> Optional<T> loadFromNbt(Codec<T> codec, Path path) {
        if (Files.isRegularFile(path)) {
            try {
                var tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
                var loaded = codec.decode(NbtOps.INSTANCE, tag);
                if (loaded.isError()) {
                    //noinspection OptionalGetWithoutIsPresent
                    throw new IOException("Invalid NBT: %s".formatted(loaded.error().get().message()));
                } else {
                    return loaded.result().map(Pair::getFirst);
                }
            } catch (IOException ex) {
                LOGGER.error("Error loading object at {}", path, ex);
                FileUtil.tryMove(path, path.resolveSibling(path.getFileName() + ".corrupt"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return Optional.empty();
    }

    public static void tryMove(Path from, Path to, CopyOption... options) {
        try {
            Files.move(from, to, options);
        } catch (IOException e) {
            LOGGER.error("Error moving %s to %s".formatted(from, to), e);
        }
    }

    public static Gson gson() {
        return ChestTrackerConfig.INSTANCE.instance().storage.readableJsonMemories ? GSON : GSON_COMPACT;
    }
}
