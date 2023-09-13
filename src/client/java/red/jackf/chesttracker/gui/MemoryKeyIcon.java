package red.jackf.chesttracker.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.List;

public record MemoryKeyIcon(ResourceLocation id, LightweightStack icon) {
    public static final Codec<MemoryKeyIcon> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(MemoryKeyIcon::id),
                    LightweightStack.CODEC.fieldOf("icon").forGetter(MemoryKeyIcon::icon)
            ).apply(instance, MemoryKeyIcon::new));

    public static final List<MemoryKeyIcon> DEFAULT_ORDER = List.of(
            new MemoryKeyIcon(MemoryBank.ENDER_CHEST_KEY, new LightweightStack(Items.ENDER_CHEST)),
            new MemoryKeyIcon(Level.OVERWORLD.location(), new LightweightStack(Items.GRASS_BLOCK)),
            new MemoryKeyIcon(Level.NETHER.location(), new LightweightStack(Items.NETHERRACK)),
            new MemoryKeyIcon(Level.END.location(), new LightweightStack(Items.END_STONE))
    );
}
