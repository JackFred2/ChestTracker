package red.jackf.chesttracker.compat.servers.hypixel;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.gui.MemoryKeyIcon;
import red.jackf.chesttracker.api.provider.InteractionTracker;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.chesttracker.api.provider.ProviderUtils;
import red.jackf.chesttracker.api.provider.def.DefaultProviderHelper;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.jackfredlib.client.api.gps.Coordinate;
import red.jackf.jackfredlib.client.api.gps.PlayerListSnapshot;
import red.jackf.jackfredlib.client.api.gps.ScoreboardSnapshot;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provider for Hypixel, mainly skyblock. Currently supports:
 * <li>SkyBlock Private Island</li>
 * <li>SkyBlock Ender Chest</li>
 */
public class HypixelProvider implements Provider {
    public static final HypixelProvider INSTANCE = new HypixelProvider();

    public static final ResourceLocation LOOT_BOXES = id("loot_boxes");
    public static final ResourceLocation SKYBLOCK_ENDER_CHEST = id("skyblock_ender_chest");
    public static final ResourceLocation SKYBLOCK_PRIVATE_ISLAND = id("skyblock_private");
    private static final List<MemoryKeyIcon> DEFAULT_ICONS = Stream.concat(Stream.of(
            new MemoryKeyIcon(LOOT_BOXES, new ItemStack(Items.NETHER_STAR)),
            new MemoryKeyIcon(SKYBLOCK_ENDER_CHEST, new ItemStack(Items.ENDER_CHEST)),
            new MemoryKeyIcon(SKYBLOCK_PRIVATE_ISLAND, new ItemStack(Items.OAK_SAPLING))
    ), DefaultProviderHelper.getDefaultIcons().stream()).toList();
    public boolean isOnSmp = false;

    private static ResourceLocation id(String path) {
        return new ResourceLocation("hypixel", path);
    }

    public static boolean isOnSkyblock() {
        return ScoreboardSnapshot.take().map(snapshot -> snapshot.title().getString().startsWith("SKYBLOCK"))
                .orElse(false);
    }

    private static Optional<String> getSkyblockArea() {
        return PlayerListSnapshot.take().nameWithPrefixStripped("Area: ");
    }

    // SMP prints 'SMP ID: <GUID>' with a hover event of "Click to put SMP ID in chat!" and clicking doing exactly that
    @Override
    public void onChatMessage(Component message) {
        if (message.getString().startsWith("SMP ID: ") &&
                message.getStyle().getClickEvent() != null &&
                message.getStyle().getClickEvent().getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
            this.isOnSmp = true;
        }
    }

    // to detect leaving the SMP
    // from what i can tell most lobbies and games have 'www.hypixel.net' at the bottom of the scoreboard except smp
    @Override
    public void onRespawn(ResourceKey<Level> from, ResourceKey<Level> to) {
        var snapshot = ScoreboardSnapshot.take();
        if (snapshot.isEmpty()) return;
        var last = snapshot.get().entryFromBottom(0);
        if (last.isEmpty()) return;
        if (last.get().getFirst().getString().contains("www.hypixel.net")) this.isOnSmp = false;
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
            var area = getSkyblockArea();
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
        } else if (isOnSmp) {
            return ProviderHandler.DEFAULT.createMemory(screen);
        }

        return Optional.empty();
    }

    @Override
    public Optional<ResourceLocation> getPlayersCurrentKey() {
        if (isOnSkyblock()) {
            var area = getSkyblockArea();
            if (area.isPresent() && area.get().equals("Private Island")) {
                return Optional.of(SKYBLOCK_PRIVATE_ISLAND);
            }
        } else if (isOnSmp) {
            return ProviderHandler.DEFAULT.getPlayersCurrentKey();
        }
        return Optional.empty();
    }
}
