package red.jackf.chesttracker.tracker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Language;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.InteractRememberType;
import red.jackf.chesttracker.gui.FavouriteButton;
import red.jackf.chesttracker.render.RenderManager;

import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class Tracker {
    private static final Tracker TRACKER = new Tracker();
    @Nullable
    private BlockPos lastInteractedPos = null;

    public void setLastPos(BlockPos newPos) {
        if (newPos == null) lastInteractedPos = null;
        else this.lastInteractedPos = newPos.toImmutable();
    }

    public static Tracker getInstance() {
        return TRACKER;
    }

    public <T extends ScreenHandler> void handleScreen(HandledScreen<T> screen) {
        if (this.lastInteractedPos == null) return;
        if (MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().world == null)
            return;

        String className = screen.getClass().getSimpleName();
        if (ChestTracker.CONFIG.miscOptions.debugPrint) {
            ChestTracker.sendDebugMessage(MinecraftClient.getInstance().player, validScreenToTrack(screen) ?
                new TranslatableText("chesttracker.gui_class_name_tracked", className).formatted(Formatting.GREEN) :
                new TranslatableText("chesttracker.gui_class_name_not_tracked", className).formatted(Formatting.RED));
        }

        if (!validScreenToTrack(screen))
            return;

        ScreenHandler handler = screen.getScreenHandler();
        List<ItemStack> items = handler.slots.stream()
            .filter(slot -> !(slot.inventory instanceof PlayerInventory))
            .filter(Slot::hasStack)
            .map(Slot::getStack)
            .collect(Collectors.toList());

        LocationStorage storage = LocationStorage.get();
        if (storage == null) return;

        storage.mergeItems(this.lastInteractedPos, MinecraftClient.getInstance().player.world, items, getTitle(MinecraftClient.getInstance().world, this.lastInteractedPos, screen.getTitle()), FavouriteButton.current.isActive());
        this.lastInteractedPos = null;
    }
    @Nullable
    private Text getTitle(ClientWorld world, BlockPos pos, Text title) {
        if (title instanceof TranslatableText) {
            return null;
        } else { // Special handling for screens that use LiteralTexts for localised names
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof NamedScreenHandlerFactory) {
                return title;
            } else {
                return null;
            }
        }
    }

    public void handleInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        boolean blockHasBE = world.getBlockState(hitResult.getBlockPos()).getBlock().hasBlockEntity();
        if (ChestTracker.CONFIG.miscOptions.blockInteractionType == InteractRememberType.ALL || blockHasBE) {
            Tracker.getInstance().setLastPos(hitResult.getBlockPos());
        }
        if (ChestTracker.CONFIG.miscOptions.debugPrint)
            ChestTracker.sendDebugMessage(player, new TranslatableText("chesttracker.block_clicked_" + (blockHasBE ? "be_provider" : "not_be_provider"),
                Registry.BLOCK.getId(world.getBlockState(hitResult.getBlockPos()).getBlock()))
                .formatted(blockHasBE ? Formatting.GREEN : Formatting.YELLOW));
    }

    public BlockPos getLastInteractedPos() {
        return lastInteractedPos;
    }

    public ActionResult searchForItem(ItemStack toFind) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return ActionResult.PASS;
        LocationStorage storage = LocationStorage.get();
        if (storage == null) return ActionResult.PASS;
        if (ChestTracker.CONFIG.miscOptions.debugPrint)
            ChestTracker.sendDebugMessage(client.player, new TranslatableText("chesttracker.searching_for_item", toFind).formatted(Formatting.GREEN));

        List<Location> results = storage.findItems(client.player.clientWorld.getDimensionRegistryKey().getValue(), toFind);
        if (results.size() > 0) {
            RenderManager.getInstance().addRenderList(results, client.world.getTime());
            client.player.closeHandledScreen();
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public boolean validScreenToTrack(Screen s) {
        return s instanceof HandledScreen
            && !ChestTracker.CONFIG.trackedScreens.blocklist.contains(s.getClass().getSimpleName())
            && lastInteractedPos != null;
    }
}
