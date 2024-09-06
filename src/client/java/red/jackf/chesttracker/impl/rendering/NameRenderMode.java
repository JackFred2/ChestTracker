package red.jackf.chesttracker.impl.rendering;

import net.minecraft.network.chat.Component;

public enum NameRenderMode {
    FULL(Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameRenderMode.full"),
            Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameRenderMode.full.tooltip")),
    HOVERED_ONLY(Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameRenderMode.hoveredOnly"),
            Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameRenderMode.hoveredOnly.tooltip")),
    DISABLED(Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameRenderMode.disabled"),
            Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameRenderMode.disabled.tooltip"));

    public final Component label;
    public final Component tooltip;

    NameRenderMode(Component label, Component tooltip) {
        this.label = label;
        this.tooltip = tooltip;
    }
}
