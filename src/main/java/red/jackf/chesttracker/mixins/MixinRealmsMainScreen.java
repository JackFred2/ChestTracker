package red.jackf.chesttracker.mixins;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.tracker.LocationStorage;

@Mixin(RealmsMainScreen.class)
public class MixinRealmsMainScreen {

    @Inject(method = "play(Lcom/mojang/realmsclient/dto/RealmsServer;Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void saveLastConnectedServer(RealmsServer realmsServer, Screen screen, CallbackInfo ci) {
        LocationStorage.setLastServer(realmsServer);
    }
}
