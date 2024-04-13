package red.jackf.chesttracker.mixins;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.chesttracker.impl.gui.invbutton.CTScreenDuck;
import red.jackf.chesttracker.impl.gui.invbutton.ui.InventoryButton;

// adds mouse dragged and release callbacks for the inv button
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements CTScreenDuck {
    @Unique
    @Nullable
    private InventoryButton ctButton = null;

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void tryDragInvButton(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (this.ctButton != null && this.ctButton.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void tryMouseReleaseInvButton(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.ctButton != null && this.ctButton.mouseReleased(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    @Accessor("leftPos")
    public abstract int chesttracker$getLeft();

    @Override
    @Accessor("topPos")
    public abstract int chesttracker$getTop();

    @Override
    @Accessor("imageWidth")
    public abstract int chesttracker$getWidth();

    @Override
    @Accessor("imageHeight")
    public abstract int chesttracker$getHeight();

    @Override
    public void chesttracker$setButton(InventoryButton button) {
        this.ctButton = button;
    }
}
