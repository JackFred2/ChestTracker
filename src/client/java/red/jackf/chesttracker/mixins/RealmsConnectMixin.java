package red.jackf.chesttracker.mixins;

import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.realms.RealmsConnect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.storage.StorageUtil;

/**
 * get the ID for the last connected realm
 */
@Mixin(RealmsConnect.class)
public class RealmsConnectMixin {

    @Inject(method = "connect", at = @At("HEAD"))
    public void getLastRealmsID(final RealmsServer server, ServerAddress address, CallbackInfo ci) {
        StorageUtil.setLastRealmID(server.id);
    }
}
