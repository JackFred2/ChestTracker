package red.jackf.chesttracker.api.providers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public sealed interface MemoryLocation permits MemoryLocation.InWorld, MemoryLocation.Override {
    ResourceLocation memoryKey();

    BlockPos position();

    boolean isOverride();

    static MemoryLocation inWorld(ResourceLocation memoryKey, BlockPos position) {
        return new InWorld(memoryKey, position);
    }

    static MemoryLocation override(ResourceLocation memoryKey, BlockPos position) {
        return new Override(memoryKey, position);
    }

    record InWorld(ResourceLocation memoryKey, BlockPos position) implements MemoryLocation {
        @java.lang.Override
        public boolean isOverride() {
            return false;
        }
    }

    record Override(ResourceLocation memoryKey, BlockPos position) implements MemoryLocation {
        @java.lang.Override
        public boolean isOverride() {
            return true;
        }
    }
}
