package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.memory.MemoryUtils;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow
    @Nullable
    public Screen currentScreen;

    @Inject(method = "openScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V"))
    public void chestTracker$countItemsBeforeClose(@Nullable Screen newScreen, CallbackInfo ci) {
        if (this.currentScreen instanceof HandledScreen) {
            MemoryUtils.handleItemsFromScreen((HandledScreen<? extends ScreenHandler>) this.currentScreen);
        }
    }
}
