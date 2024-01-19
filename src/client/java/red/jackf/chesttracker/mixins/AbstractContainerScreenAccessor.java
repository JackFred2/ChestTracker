package red.jackf.chesttracker.mixins;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int chesttracker$getLeft();

    @Accessor("topPos")
    int chesttracker$getTop();

    @Accessor("imageWidth")
    int chesttracker$getWidth();

    @Accessor("imageHeight")
    int chesttracker$getHeight();
}
