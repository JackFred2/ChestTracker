package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.memory.MemoryUtils;

@Environment(EnvType.CLIENT)
@Mixin(RealmsMainScreen.class)
public class MixinRealmsMainScreen {

    @Inject(method = "play", at = @At("HEAD"))
    private void chestTracker$saveLastConnectedServer(RealmsServer realmsServer, Screen screen, CallbackInfo ci) {
        MemoryUtils.setLastRealmsServer(realmsServer);
    }
}
