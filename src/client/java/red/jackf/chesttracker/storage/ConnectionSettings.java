package red.jackf.chesttracker.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.ModCodecs;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A map of {@link LoadContext} ids to memory bank ids to various connection-specific settings, such as whether to
 * auto-load memory banks or the specific override.
 */
public record ConnectionSettings(boolean autoLoadMemories, Optional<String> memoryBankIdOverride) {
    private static final Path PATH = Constants.STORAGE_DIR.resolve("default_ids.dat");

    private static Map<String, ConnectionSettings> settings = new HashMap<>();

    public static void load() {
        StorageUtil.loadFromNbt(FILE_CODEC, PATH).ifPresent(connectionSettingsMap -> settings = connectionSettingsMap);
    }

    public static void save() {
        StorageUtil.saveToNbt(settings, FILE_CODEC, PATH);
    }

    public static ConnectionSettings getOrCreate(String connectionId) {
        if (!settings.containsKey(connectionId)) {
            settings.put(connectionId, new ConnectionSettings(true, Optional.empty()));
            save();
        }
        return settings.get(connectionId);
    }

    @Nullable
    public static ConnectionSettings get(String connectionId) {
        return settings.get(connectionId);
    }

    public static void put(String id, ConnectionSettings connectionSettings) {
        settings.put(id, connectionSettings);
        save();
    }

    private static final Codec<ConnectionSettings> CONNECTION_SETTINGS_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("auto_load_memories").forGetter(ConnectionSettings::autoLoadMemories),
            Codec.STRING.optionalFieldOf("memory_bank_id_override").forGetter(ConnectionSettings::memoryBankIdOverride)
    ).apply(i, ConnectionSettings::new));
    private static final Codec<Map<String, ConnectionSettings>> FILE_CODEC = ModCodecs.makeMutableMap(Codec.unboundedMap(Codec.STRING, CONNECTION_SETTINGS_CODEC));
}
