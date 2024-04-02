package red.jackf.chesttracker.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Optional;
import java.util.function.Predicate;

import static net.minecraft.network.chat.Component.translatable;

public class FilteringSettings {
    protected static final Codec<FilteringSettings> CODEC = RecordCodecBuilder.create(instance -> {
        final var def = new FilteringSettings();
        return instance.group(
                Codec.BOOL.optionalFieldOf("manualMode")
                        .forGetter(settings -> Optional.of(settings.manualMode)),
                Codec.BOOL.optionalFieldOf("onlyRememberNamed")
                        .forGetter(settings -> Optional.of(settings.onlyRememberNamed)),
                JFLCodecs.forEnum(RememberedContainers.class).optionalFieldOf("rememberedContainers")
                        .forGetter(settings -> Optional.of(settings.rememberedContainers)),
                Codec.BOOL.optionalFieldOf("rememberEnderChests")
                        .forGetter(settings -> Optional.of(settings.rememberEnderChests)),
                JFLCodecs.forEnum(AutoAddPlacedBlocks.class).optionalFieldOf("autoAddPlacedBlocks")
                        .forGetter(settings -> Optional.of(settings.autoAddPlacedBlocks))
        ).apply(instance, (manualMode, onlyRememberNamed, rememberedContainers, rememberEnderChests, autoAddPlacedBlocks) -> new FilteringSettings(
                manualMode.orElse(def.manualMode),
                onlyRememberNamed.orElse(def.onlyRememberNamed),
                rememberedContainers.orElse(def.rememberedContainers),
                rememberEnderChests.orElse(def.rememberEnderChests),
                autoAddPlacedBlocks.orElse(def.autoAddPlacedBlocks)
        ));
    });

    public boolean manualMode = false;
    public boolean onlyRememberNamed = false;
    public RememberedContainers rememberedContainers = RememberedContainers.ALL;
    public boolean rememberEnderChests = true;
    public AutoAddPlacedBlocks autoAddPlacedBlocks = AutoAddPlacedBlocks.SHULKER_BOXES_ONLY;

    protected FilteringSettings() {}

    public FilteringSettings(boolean manualMode,
                             boolean onlyRememberNamed,
                             RememberedContainers rememberedContainers,
                             boolean rememberEnderChests,
                             AutoAddPlacedBlocks autoAddPlacedBlocks) {
        this.manualMode = manualMode;
        this.onlyRememberNamed = onlyRememberNamed;
        this.rememberedContainers = rememberedContainers;
        this.rememberEnderChests = rememberEnderChests;
        this.autoAddPlacedBlocks = autoAddPlacedBlocks;
    }

    public FilteringSettings copy() {
        return new FilteringSettings(manualMode, onlyRememberNamed, rememberedContainers, rememberEnderChests, autoAddPlacedBlocks);
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

    public enum AutoAddPlacedBlocks {
        YES(state -> true,
            translatable("chesttracker.gui.editMemoryBank.filtering.autoAddPlacedBlocks.yes")),
        SHULKER_BOXES_ONLY(state -> state.is(BlockTags.SHULKER_BOXES),
                           translatable("chesttracker.gui.editMemoryBank.filtering.autoAddPlacedBlocks.shulkerBoxesOnly")),
        NO(state -> false,
           translatable("chesttracker.gui.editMemoryBank.filtering.autoAddPlacedBlocks.no"));

        public final Predicate<BlockState> blockPredicate;
        public final Component label;

        AutoAddPlacedBlocks(Predicate<BlockState> blockPredicate, Component label) {
            this.blockPredicate = blockPredicate;
            this.label = label;
        }
    }
}
