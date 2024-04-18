package red.jackf.chesttracker.mixins;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import red.jackf.chesttracker.impl.gui.util.CTTitleOverrideDuck;

/**
 * Used to allow for custom names supplied by the local user
 */
@Mixin(Screen.class)
public class ScreenMixin implements CTTitleOverrideDuck {

    @Final
    @Shadow
    @Mutable
    protected Component title;

    @Override
    public void chesttracker$setTitle(Component title) {
        this.title = title;
    }
}
