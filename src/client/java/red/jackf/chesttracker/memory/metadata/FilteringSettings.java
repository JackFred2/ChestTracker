package red.jackf.chesttracker.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.chesttracker.util.ModCodecs;

import java.util.Optional;
import java.util.function.Predicate;

import static net.minecraft.network.chat.Component.translatable;

public class FilteringSettings {
    protected static final Codec<FilteringSettings> CODEC = RecordCodecBuilder.create(instance -> {
        final var def = new FilteringSettings();
        return instance.group(
                Codec.BOOL.optionalFieldOf("onlyRememberNamed")
                        .forGetter(settings -> Optional.of(settings.onlyRememberNamed)),
                ModCodecs.ofEnum(RememberedContainers.class).optionalFieldOf("rememberedContainers")
                        .forGetter(settings -> Optional.of(settings.rememberedContainers)),
                Codec.BOOL.optionalFieldOf("rememberEnderChests")
                        .forGetter(settings -> Optional.of(settings.rememberEnderChests))
        ).apply(instance, (onlyRememberNamed, rememberedContainers, rememberEnderChests) -> new FilteringSettings(
                onlyRememberNamed.orElse(def.onlyRememberNamed),
                rememberedContainers.orElse(def.rememberedContainers),
                rememberEnderChests.orElse(def.rememberEnderChests)
        ));
    });

    public boolean onlyRememberNamed = false;
    public RememberedContainers rememberedContainers = RememberedContainers.ALL;
    public boolean rememberEnderChests = true;

    protected FilteringSettings() {}

    public FilteringSettings(boolean onlyRememberNamed, RememberedContainers rememberedContainers, boolean rememberEnderChests) {
        this.onlyRememberNamed = onlyRememberNamed;
        this.rememberedContainers = rememberedContainers;
        this.rememberEnderChests = rememberEnderChests;
    }

    public FilteringSettings copy() {
        return new FilteringSettings(onlyRememberNamed, rememberedContainers, rememberEnderChests);
    }

    public enum RememberedContainers {
        ALL(state -> true,
            translatable("chesttracker.gui.editMemoryBank.filtering.rememberedContainers.all"),
            translatable("chesttracker.gui.editMemoryBank.filtering.rememberedContainers.all.tooltip")),
        COMMON(RememberedContainers::isCommonBlock,
               translatable("chesttracker.gui.editMemoryBank.filtering.rememberedContainers.common"),
               translatable("chesttracker.gui.editMemoryBank.filtering.rememberedContainers.common.tooltip"));

        public final Predicate<BlockState> predicate;
        public final Component label;
        public final Component tooltip;

        RememberedContainers(Predicate<BlockState> predicate, Component label, Component tooltip) {
            this.predicate = predicate;
            this.label = label;
            this.tooltip = tooltip;
        }

        private static boolean isCommonBlock(BlockState state) {
            return state.getBlock() instanceof AbstractChestBlock
                    || state.is(BlockTags.SHULKER_BOXES)
                    || state.getBlock() instanceof BarrelBlock;
        }
    }
}
