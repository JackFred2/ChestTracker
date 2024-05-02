package red.jackf.chesttracker.impl.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.util.Constants;
import red.jackf.chesttracker.impl.util.FileUtil;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A map of {@link Coordinate} ids to memory bank ids to various connection-specific settings; currently this allows a
 * user to override which file gets loaded.
 */
public record ConnectionSettings(Optional<String> memoryBankIdOverride) {
    public ConnectionSettings setOverride(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> memoryBankIdOverride) {
        return new ConnectionSettings(memoryBankIdOverride);
    }

    ///////////////
    // INTERNALS //
    ///////////////
    private static final Path PATH = Constants.STORAGE_DIR.resolve("connection_settings.dat");

    private static Map<String, ConnectionSettings> settings = new HashMap<>();

    public static void load() {
        FileUtil.loadFromNbt(FILE_CODEC, PATH)
                .ifPresent(connectionSettingsMap -> settings = connectionSettingsMap);
    }

    public static void save() {
        FileUtil.saveToNbt(settings, FILE_CODEC, PATH);
    }

    public static ConnectionSettings getOrCreate(String connectionId) {
        if (!settings.containsKey(connectionId)) {
            settings.put(connectionId, new ConnectionSettings(Optional.empty()));
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
            Codec.STRING.optionalFieldOf("memory_bank_id_override").forGetter(ConnectionSettings::memoryBankIdOverride)
    ).apply(i, ConnectionSettings::new));
    private static final Codec<Map<String, ConnectionSettings>> FILE_CODEC = JFLCodecs.mutableMap(Codec.unboundedMap(Codec.STRING, CONNECTION_SETTINGS_CODEC));
}
