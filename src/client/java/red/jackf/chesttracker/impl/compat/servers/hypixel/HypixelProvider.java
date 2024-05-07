package red.jackf.chesttracker.impl.compat.servers.hypixel;

import com.google.common.collect.Streams;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.memory.Memory;
import red.jackf.chesttracker.api.memory.MemoryBank;
import red.jackf.chesttracker.api.memory.MemoryBankAccess;
import red.jackf.chesttracker.api.providers.*;
import red.jackf.chesttracker.api.providers.context.ScreenCloseContext;
import red.jackf.chesttracker.api.providers.context.ScreenOpenContext;
import red.jackf.chesttracker.api.providers.defaults.DefaultProvider;
import red.jackf.chesttracker.impl.ChestTracker;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provider for Hypixel SMP and Hypixel Skyblock.
 */
public class HypixelProvider extends ServerProvider {
    public static final ResourceLocation SKYBLOCK_PRIVATE_ISLAND = new ResourceLocation("hypixel", "skyblock_private");
    public static final ResourceLocation SKYBLOCK_ENDER_CHEST = new ResourceLocation("hypixel", "skyblock_ender_chest");

    private static final List<MemoryKeyIcon> ICONS = Streams.concat(Stream.of(
            new MemoryKeyIcon(SKYBLOCK_PRIVATE_ISLAND, Items.OAK_SAPLING.getDefaultInstance())
    ), ProviderUtils.getDefaultIcons().stream()).toList();

    @Override
    public ResourceLocation id() {
        return ChestTracker.id("hypixel");
    }

    @Override
    public boolean appliesTo(Coordinate coordinate) {
        return coordinate instanceof Coordinate.Multiplayer multi
                && multi.address().contains("hypixel.net");
    }

    @Override
    public List<MemoryKeyIcon> getMemoryKeyIcons() {
        return ICONS;
    }

    @Override
    public void onConnect(Coordinate coordinate) {
        DefaultProvider.INSTANCE.onConnect(coordinate);
    }

    @Override
    public void onScreenOpen(ScreenOpenContext context) {
        InteractionTracker.INSTANCE.getLastBlockSource().flatMap(this::getMemoryLocation).ifPresent(context::setMemoryLocation);
    }

    @Override
    public void onScreenClose(ScreenCloseContext context) {
        MemoryBank bank = MemoryBankAccess.INSTANCE.getLoaded().orElse(null);
        if (bank == null) return;

        if (Skyblock.isPlayerOn()) {
            if (Skyblock.isOnPrivateIsland()) {
                Optional<ClientBlockSource> cbs = InteractionTracker.INSTANCE.getLastBlockSource();
                if (cbs.isPresent()) {
                    Optional<MemoryLocation> location = this.getMemoryLocation(cbs.get());
                    if (location.isPresent()) {
                        if (cbs.get().blockState().is(Blocks.CHEST) || cbs.get().blockState().is(Blocks.TRAPPED_CHEST)) {
                            List<BlockPos> connectedBlocks = ConnectedBlocksGrabber.getConnected(cbs.get().level(), cbs.get().blockState(), cbs.get().pos());
                            BlockPos rootPos = connectedBlocks.get(0);

                            Memory memory = MemoryBuilder.create(context.getItemsMatching(stack -> !stack.getHoverName().getString().isBlank()))
                                    .inContainer(cbs.get().blockState().getBlock())
                                    .otherPositions(connectedBlocks.stream().skip(1).toList())
                                    .build();

                            bank.addMemory(location.get().memoryKey(), rootPos, memory);
                            return;
                        }
                    }
                }
            }

            if (context.getTitle().getString().startsWith("Ender Chest")) {
                Optional<Integer> page = Skyblock.getEnderChestPage(context.getTitle());
                if (page.isPresent()) {
                    List<ItemStack> items = Skyblock.getEnderChestItems(context);

                    Memory memory = MemoryBuilder.create(items)
                            .inContainer(Blocks.ENDER_CHEST)
                            .build();

                    bank.addMemory(SKYBLOCK_ENDER_CHEST, new BlockPos(page.get(), 0, 0), memory);
                }
            }
        }
    }

    @Override
    public Optional<MemoryLocation> getMemoryLocation(ClientBlockSource cbs) {
        if (Skyblock.isOnPrivateIsland()) {
            return Optional.of(MemoryLocation.inWorld(SKYBLOCK_PRIVATE_ISLAND, cbs.pos()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ResourceLocation> getPlayersCurrentKey(Level level, LocalPlayer player) {
        if (Skyblock.isOnPrivateIsland()) {
            return Optional.of(SKYBLOCK_PRIVATE_ISLAND);
        }

        return Optional.empty();
    }
}
