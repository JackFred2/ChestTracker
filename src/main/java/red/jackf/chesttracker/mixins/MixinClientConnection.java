package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.tracker.LocationStorage;

@Environment(EnvType.CLIENT)
@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "disconnect(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    public void onDisconnectEvent(Text ignored, CallbackInfo ci) {
        LocationStorage storage = LocationStorage.get();
        if (storage != null) storage.closeDown();
    }
}
