package red.jackf.chesttracker.provider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.api.gui.MemoryKeyIcon;
import red.jackf.chesttracker.compat.mods.ShareEnderChestIntegration;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.ArrayList;
import java.util.List;

public class DefaultIcons {
    public static final DefaultIcons INSTANCE = new DefaultIcons();
    private final List<MemoryKeyIcon> icons = new ArrayList<>();

    public void registerIcon(ResourceLocation key, ItemStack iconStack) {
        icons.add(new MemoryKeyIcon(key, iconStack));
    }

    public void registerIconAfter(ResourceLocation key, ItemStack iconStack, ResourceLocation displayAfter) {
        MemoryKeyIcon icon = new MemoryKeyIcon(key, iconStack);
        int targetIndex = 0;
        while (targetIndex < this.icons.size() && !this.icons.get(targetIndex).id().equals(displayAfter)) {
            targetIndex++;
        }
        this.icons.add(targetIndex + 1, icon);
    }

    public void registerIconBefore(ResourceLocation key, ItemStack iconStack, ResourceLocation displayAfter) {
        MemoryKeyIcon icon = new MemoryKeyIcon(key, iconStack);
        int targetIndex = 0;
        while (targetIndex < this.icons.size() && !this.icons.get(targetIndex).id().equals(displayAfter)) {
            targetIndex++;
        }
        if (targetIndex == this.icons.size()) targetIndex = 0;
        this.icons.add(targetIndex, icon);
    }

    public List<MemoryKeyIcon> getIcons() {
        return List.copyOf(this.icons);
    }

    public static void setup() {
        // vanilla
        INSTANCE.registerIcon(MemoryBank.ENDER_CHEST_KEY, new ItemStack(Items.ENDER_CHEST));
        INSTANCE.registerIcon(Level.OVERWORLD.location(), new ItemStack(Items.GRASS_BLOCK));
        INSTANCE.registerIcon(Level.NETHER.location(), new ItemStack(Items.NETHERRACK));
        INSTANCE.registerIcon(Level.END.location(), new ItemStack(Items.END_STONE));

        // compat
        INSTANCE.registerIconAfter(new ResourceLocation("the_bumblezone", "the_bumblezone"), new ItemStack(Items.BEE_NEST), Level.OVERWORLD.location());
        INSTANCE.registerIconAfter(ShareEnderChestIntegration.MEMORY_KEY, new ItemStack(Items.ENDER_EYE), MemoryBank.ENDER_CHEST_KEY);
    }
}
