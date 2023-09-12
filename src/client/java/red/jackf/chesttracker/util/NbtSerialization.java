package red.jackf.chesttracker.util;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import org.apache.logging.log4j.Logger;
import red.jackf.chesttracker.ChestTracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Utilities for [de]serializing objects to NBT files.
 */
public class NbtSerialization {
    public static final Logger LOGGER = ChestTracker.getLogger("NBT");

    /**
     * Save an object to a path with a given codec as an NBT file
     * @param object Object to serialize
     * @param codec Codec to serialize said object with
     * @param path Path to save the object to
     * @return Whether the save was successful
     * @param <T> Type of the serialized object
     */
    public static <T> boolean saveToNbt(T object, Codec<T> codec, Path path) {
        try {
            Files.createDirectories(path.getParent());
            var tag = codec.encodeStart(NbtOps.INSTANCE, object).get();
            var result = tag.left();
            var err = tag.right();
            if (err.isPresent()) {
                throw new IOException("Error encoding to NBT %s".formatted(err.get()));
            } else if (result.isPresent() && result.get() instanceof CompoundTag compound) {
                NbtIo.writeCompressed(compound, path.toFile());
                return true;
            } else { //noinspection OptionalGetWithoutIsPresent
                throw new IOException("Error encoding to NBT: not a compound tag: %s".formatted(result.get()));
            }
        } catch(IOException ex) {
            LOGGER.error("Error saving object", ex);
            return false;
        }
    }

    /**
     * Load an NBT file to an object using a given codec
     * @param codec Codec to deserialize with
     * @param path Path to read from
     * @return An optional containing the deserialized object, or an empty optional if errored
     * @param <T> Type of deserialized object
     */
    public static <T> Optional<T> loadFromNbt(Codec<T> codec, Path path) {
        if (Files.isRegularFile(path)) {
            try {
                var tag = NbtIo.readCompressed(path.toFile());
                var loaded = codec.decode(NbtOps.INSTANCE, tag).get();
                if (loaded.right().isPresent()) {
                    throw new IOException("Invalid NBT: %s".formatted(loaded.right().get()));
                } else {
                    //noinspection OptionalGetWithoutIsPresent
                    return Optional.ofNullable(loaded.left().get().getFirst());
                }
            } catch (IOException ex) {
                LOGGER.error("Error loading object at {}", path, ex);
            }
        }
        return Optional.empty();
    }
}
