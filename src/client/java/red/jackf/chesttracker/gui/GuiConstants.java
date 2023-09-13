package red.jackf.chesttracker.gui;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.memory.MemoryBank;

import java.util.List;

public interface GuiConstants {
    // GUI Positioning
    int UTIL_GUI_WIDTH = 320;
    int UTIL_GUI_HEIGHT = 240;
    int MARGIN = 8;
    int SMALL_MARGIN = 5;
    int GRID_SLOT_SIZE = 18;
    int MIN_GRID_COLUMNS = 9;
    int MAX_GRID_WIDTH = 18;
    int MIN_GRID_ROWS = 6;
    int MAX_GRID_HEIGHT = 12;

    // Buttons
    long ARE_YOU_SURE_BUTTON_HOLD_TIME = 20L;
    long ARE_YOU_REALLY_SURE_BUTTON_HOLD_TIME = 30L;

    // Icon Buttons
    Item DEFAULT_ICON_ITEM = Items.CRAFTING_TABLE;
    List<MemoryKeyIcon> DEFAULT_ICONS = List.of(
            new MemoryKeyIcon(MemoryBank.ENDER_CHEST_KEY, new LightweightStack(Items.ENDER_CHEST)),
            new MemoryKeyIcon(Level.OVERWORLD.location(), new LightweightStack(Items.GRASS_BLOCK)),
            new MemoryKeyIcon(Level.NETHER.location(), new LightweightStack(Items.NETHERRACK)),
            new MemoryKeyIcon(Level.END.location(), new LightweightStack(Items.END_STONE))
    );
}
