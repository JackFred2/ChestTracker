package red.jackf.chesttracker.impl.gui.invbutton.position;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import red.jackf.chesttracker.impl.gui.invbutton.CTButtonScreenDuck;
import red.jackf.chesttracker.impl.gui.invbutton.ui.InventoryButton;
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
        int left = ((CTButtonScreenDuck) screen).chesttracker$getLeft();
        int recipeWidth = PositionUtils.getRecipeComponentWidth(screen);

        return Mth.clamp(switch (xAlign) {
            case screen_left -> xOffset;
            case screen_right -> screen.width - xOffset;
            case left_with_recipe -> left - recipeWidth + xOffset;
            case left -> left + xOffset;
            case right -> left + ((CTButtonScreenDuck) screen).chesttracker$getWidth() - xOffset;
        }, RectangleUtils.SCREEN_MARGIN, screen.width - RectangleUtils.SCREEN_MARGIN - InventoryButton.SIZE);
    }

    public int getY(AbstractContainerScreen<?> screen) {
        int top = ((CTButtonScreenDuck) screen).chesttracker$getTop();

        return Mth.clamp(switch (yAlign) {
            case screen_top -> yOffset;
            case screen_bottom -> screen.height - yOffset;
            case top -> top + yOffset;
            case bottom -> top + ((CTButtonScreenDuck) screen).chesttracker$getHeight() - yOffset;
        }, RectangleUtils.SCREEN_MARGIN, screen.height - RectangleUtils.SCREEN_MARGIN - InventoryButton.SIZE);
    }

    public void apply(AbstractContainerScreen<?> parent, InventoryButton inventoryButton) {
        int x = getX(parent);
        int y = getY(parent);
        inventoryButton.setPosition(x, y);
    }

    public enum HorizontalAlignment {
        screen_left,
        screen_right,
        left_with_recipe,
        left,
        right
    }

    public enum VerticalAlignment {
        screen_top,
        screen_bottom,
        top,
        bottom
    }
}
