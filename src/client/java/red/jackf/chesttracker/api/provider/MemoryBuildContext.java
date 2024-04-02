package red.jackf.chesttracker.api.provider;

import red.jackf.chesttracker.memory.metadata.Metadata;

public record MemoryBuildContext(Metadata metadata, long levelGameTime) {
}
