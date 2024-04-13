package red.jackf.chesttracker.api.memory.counting;

import net.minecraft.network.chat.Component;

/**
 * Stack merging method for
 */
public enum StackMergeMode {
    ALL(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.all")),
    WITHIN_CONTAINERS(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.withinContainers")),
    NEVER(Component.translatable("chesttracker.gui.editMemoryBank.search.stackMergeMode.never"));

    public final Component label;

    StackMergeMode(Component label) {
        this.label = label;
    }
}
