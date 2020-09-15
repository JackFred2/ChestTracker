package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.memory.MemoryDatabase;

@Environment(EnvType.CLIENT)
@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "disconnect", at = @At("HEAD"))
    public void chestTracker$onDisconnectHandler(Text ignored, CallbackInfo ci) {
        MemoryDatabase database = MemoryDatabase.getCurrent();
        if (database != null) {
            MemoryDatabase.clearCurrent();
        }
    }
}
