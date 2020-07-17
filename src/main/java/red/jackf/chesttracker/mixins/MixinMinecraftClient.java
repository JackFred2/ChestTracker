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
import red.jackf.chesttracker.tracker.Tracker;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow
    @Nullable
    public Screen currentScreen;

    /**
     * @reason Catch when screens with inventories are closed.
     * @author JackFred
     */

    @Inject(method = "openScreen(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V"))
    public <T extends ScreenHandler> void countItemsBeforeClose(@Nullable Screen screen, CallbackInfo ci) {
        if (currentScreen instanceof HandledScreen) {
            //noinspection unchecked
            Tracker.getInstance().handleScreen((HandledScreen<T>) currentScreen);
            //RenderManager.getInstance().addRenderList(Collections.singletonList(MinecraftClient.getInstance().player.getBlockPos()), MinecraftClient.getInstance().world.getTime());
        }
    }
}
