package red.jackf.chesttracker.mixins;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import red.jackf.chesttracker.impl.gui.invbutton.CTButtonScreenDuck;
import red.jackf.chesttracker.impl.gui.invbutton.ui.InventoryButton;
import red.jackf.chesttracker.impl.providers.ScreenOpenContextImpl;

/**
 * Mixin does a few things:
 * <ul>
 *     <li>Adds early mouse dragging and released callbacks used to drag around the CT button</li>
 *     <li>Adds dimension grabbing for positioning the CT button</li>
 * </ul>
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements CTButtonScreenDuck {
    @Unique
    @Nullable
    private InventoryButton ctButton = null;

    @Unique
    @Nullable
    private ScreenOpenContextImpl openContext = null;

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

    @Override
    public void chesttracker$setContext(ScreenOpenContextImpl openContext) {
        this.openContext = openContext;
    }

    @Override
    public @Nullable ScreenOpenContextImpl chesttracker$getContext() {
        return this.openContext;
    }
}
