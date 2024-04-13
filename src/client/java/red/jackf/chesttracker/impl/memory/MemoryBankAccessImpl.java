package red.jackf.chesttracker.impl.memory;

import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.memory.MemoryBank;
import red.jackf.chesttracker.api.memory.MemoryBankAccess;
import red.jackf.chesttracker.impl.memory.metadata.Metadata;
import red.jackf.chesttracker.impl.storage.ConnectionSettings;
import red.jackf.chesttracker.impl.storage.Storage;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.HashMap;
import java.util.Optional;

public class MemoryBankAccessImpl implements MemoryBankAccess {
    public static final MemoryBankAccessImpl ACCESS = new MemoryBankAccessImpl();
    @Nullable
    private static MemoryBankImpl INSTANCE = null;

    private MemoryBankAccessImpl() {}

    // API

    @Override
    public boolean loadOrCreate(String memoryBankId, String creationName) {
        ACCESS.unload();
        INSTANCE = Storage.load(memoryBankId).orElseGet(() -> {
            var bank = new MemoryBankImpl(Metadata.blankWithName(creationName), new HashMap<>());
            bank.setId(memoryBankId);
            return bank;
        });
        ACCESS.save();

        return true;
    }

    public boolean unload() {
        if (INSTANCE == null) return false;
        save();
        INSTANCE = null;
        return true;
    }

    @Override
    public Optional<MemoryBank> getLoaded() {
        return Optional.ofNullable(INSTANCE);
    }

    public Optional<MemoryBankImpl> getLoadedInternal() {
        return Optional.ofNullable(INSTANCE);
    }

    public void save() {
        if (INSTANCE == null) return;
        Storage.save(INSTANCE);
    }

    // Internal

    // Load from a coordinate's ID, checking the override file if necessary.
    public void loadFromCoordinate(Coordinate coordinate) {
        // not in-game; don't load
        var settings = ConnectionSettings.getOrCreate(coordinate.id());
        var id = settings.memoryBankIdOverride().orElse(coordinate.id());
        loadOrCreate(id, coordinate.userFriendlyName());
    }
}
