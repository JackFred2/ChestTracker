package red.jackf.chesttracker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public interface ChestTrackerAccessorHandledScreen {

    @Accessor(value = "focusedSlot")
    Slot getFocusedSlot();

    @Accessor(value = "backgroundWidth")
    int getBackgroundWidth();

    @Accessor(value = "backgroundHeight")
    int getBackgroundHeight();
}
