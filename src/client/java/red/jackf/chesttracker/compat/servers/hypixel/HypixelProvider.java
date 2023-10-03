package red.jackf.chesttracker.compat.servers.hypixel;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.gui.MemoryKeyIcon;
import red.jackf.chesttracker.api.provider.InteractionTracker;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.chesttracker.api.provider.ProviderUtils;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.jackfredlib.client.api.gps.PlayerListUtils;
import red.jackf.jackfredlib.client.api.gps.ScoreboardUtils;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.List;
import java.util.Optional;

/**
 * Provider for Hypixel, mainly skyblock. Currently supports:
 * <li>Loot crates</li>
 * <li>SkyBlock Private Island</li>
 * <li>SkyBlock Ender Chest</li>
 */
public class HypixelProvider implements Provider {
    private static ResourceLocation id(String path) {
        return new ResourceLocation("hypixel", path);
    }

    public static final ResourceLocation LOOT_BOXES = id("loot_boxes");
    public static final ResourceLocation SKYBLOCK_ENDER_CHEST = id("skyblock_ender_chest");
    public static final ResourceLocation SKYBLOCK_PRIVATE_ISLAND = id("skyblock_private");

    private static final List<MemoryKeyIcon> DEFAULT_ICONS = List.of(
            new MemoryKeyIcon(LOOT_BOXES, new ItemStack(Items.NETHER_STAR)),
            new MemoryKeyIcon(SKYBLOCK_ENDER_CHEST, new ItemStack(Items.ENDER_CHEST)),
            new MemoryKeyIcon(SKYBLOCK_PRIVATE_ISLAND, new ItemStack(Items.OAK_SAPLING))
    );

    protected static boolean isOnSkyblock() {
        return ScoreboardUtils.getPrefixed("SKYBLOCK").isPresent();
    }

    @Override
    public ResourceLocation name() {
        return ChestTracker.id("hypixel");
    }

    @Override
    public boolean applies(Coordinate coordinate) {
        return coordinate instanceof Coordinate.Multiplayer multiplayer
                && multiplayer.address().contains("hypixel.net");
    }

    @Override
    public Optional<Pair<ResourceLocation, BlockPos>> getKeyOverride(ClientBlockSource source) {
        return Optional.empty();
    }

    @Override
    public List<MemoryKeyIcon> getDefaultIcons() {
        return DEFAULT_ICONS;
    }

    @Override
    public Optional<MemoryBuilder.Entry> createMemory(AbstractContainerScreen<?> screen) {
        if (isOnSkyblock()) {
            // private island
            var area = PlayerListUtils.getPrefixed("Area: ");
            if (area.isPresent() && area.get().equals("Private Island")) {
                if (InteractionTracker.INSTANCE.getLastBlockSource().isPresent()) {
                    var lastBlock = InteractionTracker.INSTANCE.getLastBlockSource().get();
                    if (lastBlock.blockState().is(Blocks.CHEST) || lastBlock.blockState().is(Blocks.TRAPPED_CHEST)) {
                        List<BlockPos> connected = ConnectedBlocksGrabber.getConnected(lastBlock.level(), lastBlock.blockState(), lastBlock.pos());
                        var truePos = connected.get(0);
                        var items = ProviderUtils.getNonPlayerStacksAsStream(screen)
                                .filter(stack -> !stack.getHoverName().getString().isBlank())
                                .toList();
                        return Optional.of(MemoryBuilder.create(items)
                               .otherPositions(connected.stream().filter(pos -> !pos.equals(truePos)).toList())
                               .toEntry(SKYBLOCK_PRIVATE_ISLAND, truePos));
                    }
                }
            }

            // ender chest
            if (EnderChestReader.isEnderChest(screen)) {
                var page = EnderChestReader.getPage(screen);
                if (page.isPresent()) {
                    var items = EnderChestReader.getItems(screen);
                    return Optional.of(MemoryBuilder.create(items)
                               .toEntry(SKYBLOCK_ENDER_CHEST, new BlockPos(page.get(), 0, 0)));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<ResourceLocation> getPlayersCurrentKey() {
        if (isOnSkyblock()) {
            var area = PlayerListUtils.getPrefixed("Area: ");
            if (area.isPresent() && area.get().equals("Private Island")) {
                return Optional.of(SKYBLOCK_PRIVATE_ISLAND);
            }
        }
        return Optional.empty();
    }
}
