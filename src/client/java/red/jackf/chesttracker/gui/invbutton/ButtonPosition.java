package red.jackf.chesttracker.gui.invbutton;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import red.jackf.chesttracker.mixins.AbstractContainerScreenAccessor;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

public record ButtonPosition(HorizontalAlignment xAlign, int xOffset, VerticalAlignment yAlign, int yOffset) {
    public static final Codec<ButtonPosition> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    JFLCodecs.forEnum(HorizontalAlignment.class).fieldOf("x_align").forGetter(ButtonPosition::xAlign),
                    Codec.INT.fieldOf("x_offset").forGetter(ButtonPosition::xOffset),
                    JFLCodecs.forEnum(VerticalAlignment.class).fieldOf("y_align").forGetter(ButtonPosition::yAlign),
                    Codec.INT.fieldOf("y_offset").forGetter(ButtonPosition::yOffset)
            ).apply(instance, ButtonPosition::new));

    public int getX(AbstractContainerScreen<?> screen) {
        int left = ((AbstractContainerScreenAccessor) screen).chesttracker$getLeft();

        if (xAlign == HorizontalAlignment.left) {
            return left + xOffset;
        } else {
            return left + ((AbstractContainerScreenAccessor) screen).chesttracker$getWidth() - xOffset;
        }
    }

    public int getY(AbstractContainerScreen<?> screen) {
        int top = ((AbstractContainerScreenAccessor) screen).chesttracker$getTop();

        if (yAlign == VerticalAlignment.top) {
            return top + yOffset;
        } else {
            return top + ((AbstractContainerScreenAccessor) screen).chesttracker$getHeight() - yOffset;
        }
    }

    public enum HorizontalAlignment {
        left,
        right
    }

    public enum VerticalAlignment {
        top,
        bottom
    }
}
