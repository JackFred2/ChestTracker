package red.jackf.chesttracker.impl.providers;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.api.providers.MemoryKeyIcon;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;

import java.util.List;

public class DefaultIconsImpl {
    private static final List<MemoryKeyIcon> ICONS = Lists.newArrayList(
            new MemoryKeyIcon(MemoryBankImpl.ENDER_CHEST_KEY, Items.ENDER_CHEST.getDefaultInstance()),

            new MemoryKeyIcon(Level.OVERWORLD.location(), Items.GRASS_BLOCK.getDefaultInstance()),
            new MemoryKeyIcon(Level.NETHER.location(), Items.NETHERRACK.getDefaultInstance()),
            new MemoryKeyIcon(Level.END.location(), Items.END_STONE.getDefaultInstance())
    );

    public static List<MemoryKeyIcon> getDefaultIcons() {
        return ICONS.stream()
                .map(MemoryKeyIcon::copy)
                .toList();
    }


    public static void registerIcon(MemoryKeyIcon icon) {
        ICONS.add(icon);
    }

    public static void registerIconAbove(ResourceLocation target, MemoryKeyIcon icon) {
        int targetIndex = 0;
        while (targetIndex < ICONS.size() && !ICONS.get(targetIndex).id().equals(target)) {
            targetIndex++;
        }
        if (targetIndex == ICONS.size()) targetIndex = 0;
        ICONS.add(targetIndex, icon);
    }

    public static void registerIconBelow(ResourceLocation target, MemoryKeyIcon icon) {
        int targetIndex = 0;
        while (targetIndex < ICONS.size() && !ICONS.get(targetIndex).id().equals(target)) {
            targetIndex++;
        }
        ICONS.add(targetIndex + 1, icon);
    }
}
