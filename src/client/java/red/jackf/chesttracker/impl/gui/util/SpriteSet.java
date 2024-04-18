package red.jackf.chesttracker.impl.gui.util;

import net.minecraft.resources.ResourceLocation;

public record SpriteSet(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation focused, ResourceLocation disabledFocused) {
    public SpriteSet(ResourceLocation enabled, ResourceLocation disabled) {
        this(enabled, enabled, disabled, disabled);
    }

    public ResourceLocation get(boolean enabled, boolean focused) {
        if (enabled) {
            return focused ? this.focused : this.enabled;
        } else {
            return focused ? this.disabledFocused : this.disabled;
        }
    }

    public ResourceLocation enabled() {
        return this.enabled;
    }

    public ResourceLocation disabled() {
        return this.disabled;
    }

    public ResourceLocation focused() {
        return this.focused;
    }
}
