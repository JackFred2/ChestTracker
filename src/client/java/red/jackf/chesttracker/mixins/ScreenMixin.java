package red.jackf.chesttracker.mixins;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    @Unique
    private Component originalTitle = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void saveOriginalTitle(Component title, CallbackInfo ci) {
        this.originalTitle = title;
    }

    @Override
    public void chesttracker$setTitleOverride(@NotNull Component override) {
        this.title = override;
    }

    @Override
    public void chesttracker$clearTitleOverride() {
        this.title = this.originalTitle;
    }

    @Override
    public Component chesttracker$getOriginalTitle() {
        return originalTitle;
    }
}
