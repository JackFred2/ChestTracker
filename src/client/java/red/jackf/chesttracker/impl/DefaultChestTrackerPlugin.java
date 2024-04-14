package red.jackf.chesttracker.impl;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.block.Blocks;
import red.jackf.chesttracker.api.ChestTrackerPlugin;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.gui.GetCustomName;
import red.jackf.chesttracker.api.gui.ScreenBlacklist;
import red.jackf.chesttracker.api.memory.CommonKeys;
import red.jackf.chesttracker.api.providers.*;
import red.jackf.chesttracker.api.providers.defaults.DefaultProvider;
import red.jackf.chesttracker.api.providers.defaults.DefaultProviderMemoryKeyOverride;
import red.jackf.chesttracker.api.providers.defaults.DefaultProviderScreenClose;
import red.jackf.chesttracker.impl.compat.mods.ShareEnderChestIntegration;
import red.jackf.jackfredlib.api.base.ResultHolder;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.List;

public class DefaultChestTrackerPlugin implements ChestTrackerPlugin {

    @Override
    public void load() {
        GetCustomName.EVENT.register(EventPhases.FALLBACK_PHASE, ((source, screen) -> {
            Component title = screen.getTitle();

            if (containsTranslatable(title))
                return ResultHolder.pass();

            // if it's not translatable, it's very likely a custom name
            return ResultHolder.value(title);
        }));

        ScreenBlacklist.add(
                EffectRenderingInventoryScreen.class,
                BeaconScreen.class
        );

        ProviderUtils.registerProvider(DefaultProvider.INSTANCE);

        DefaultProviderScreenClose.EVENT.register(EventPhases.FALLBACK_PHASE, DefaultChestTrackerPlugin::defaultMemoryCreator);

        DefaultProviderScreenClose.EVENT.register(EventPhases.DEFAULT_PHASE, DefaultChestTrackerPlugin::enderChestMemoryCreator);

        DefaultProviderMemoryKeyOverride.EVENT.register(cbs -> {
            if (cbs.blockState().getBlock() == Blocks.ENDER_CHEST) {
                return ResultHolder.value(Pair.of(CommonKeys.ENDER_CHEST_KEY, BlockPos.ZERO));
            }

            return ResultHolder.pass();
        });

        ShareEnderChestIntegration.setup();
    }

    // Visits without decomposing
    private static boolean containsTranslatable(Component component) {
        if (component.getContents() instanceof TranslatableContents) return true;
        for (Component sibling : component.getSiblings()) {
            if (containsTranslatable(sibling)) return true;
        }
        return false;
    }

    private static ResultHolder<DefaultProviderScreenClose.Result> defaultMemoryCreator(ServerProvider provider, ScreenCloseContext context) {
        var cbs = InteractionTracker.INSTANCE.getLastBlockSource();
        if (cbs.isEmpty()) return ResultHolder.pass();

        var key = ProviderUtils.getPlayersCurrentKey();
        if (key.isEmpty()) return ResultHolder.pass();

        if (!ProviderUtils.defaultShouldRemember(cbs.get())) return ResultHolder.pass();

        List<BlockPos> connectedBlocks = ConnectedBlocksGrabber.getConnected(cbs.get().level(), cbs.get()
                .blockState(), cbs.get().pos());
        BlockPos rootPos = connectedBlocks.get(0);

        return ResultHolder.value(MemoryBuilder.create(context.getItems())
                .withCustomName(GetCustomName.EVENT.invoker().getName(cbs.get(), context.getScreen()).getNullable())
                .inContainer(cbs.get().blockState().getBlock())
                .otherPositions(connectedBlocks.stream()
                        .filter(pos -> !pos.equals(rootPos))
                        .toList())
                .toResult(key.get(), rootPos));
    }

    private static ResultHolder<DefaultProviderScreenClose.Result> enderChestMemoryCreator(ServerProvider provider, ScreenCloseContext context) {
        var cbs = InteractionTracker.INSTANCE.getLastBlockSource();
        if (cbs.isEmpty()) return ResultHolder.pass();

        if (!cbs.get().blockState().is(Blocks.ENDER_CHEST)) return ResultHolder.pass();

        return ResultHolder.value(MemoryBuilder.create(context.getItems())
                .inContainer(Blocks.ENDER_CHEST)
                .toResult(CommonKeys.ENDER_CHEST_KEY, BlockPos.ZERO));
    }
}
