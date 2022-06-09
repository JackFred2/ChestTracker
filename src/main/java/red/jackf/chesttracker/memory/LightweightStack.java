package red.jackf.chesttracker.memory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public record LightweightStack(Item item, @Nullable NbtCompound tag) {
    public LightweightStack(Item item, @Nullable NbtCompound tag) {
        this.item = item;
        this.tag = tag;
    }
}
