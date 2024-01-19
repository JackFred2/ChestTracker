package red.jackf.chesttracker.gui.invbutton;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.util.Mth;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.NavigableMap;

public record ButtonPosition(HorizontalAlignment xAlign, int xOffset, VerticalAlignment yAlign, int yOffset) {
    public static final Codec<ButtonPosition> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    JFLCodecs.forEnum(HorizontalAlignment.class).fieldOf("x_align").forGetter(ButtonPosition::xAlign),
                    Codec.INT.fieldOf("x_offset").forGetter(ButtonPosition::xOffset),
                    JFLCodecs.forEnum(VerticalAlignment.class).fieldOf("y_align").forGetter(ButtonPosition::yAlign),
                    Codec.INT.fieldOf("y_offset").forGetter(ButtonPosition::yOffset)
            ).apply(instance, ButtonPosition::new));
    private static final int SCREEN_MARGIN = 2;

    public int getX(AbstractContainerScreen<?> screen) {
        int left = ((CTScreenDuck) screen).chesttracker$getLeft();
        int recipeWidth = getRecipeComponentWidth(screen);

        return switch (xAlign) {
            case screen_left -> xOffset;
            case screen_right -> screen.width - xOffset;
            case left_with_recipe -> left - recipeWidth + xOffset;
            case left -> left + xOffset;
            case right -> left + ((CTScreenDuck) screen).chesttracker$getWidth() - xOffset;
        };
    }

    public int getY(AbstractContainerScreen<?> screen) {
        int top = ((CTScreenDuck) screen).chesttracker$getTop();

        return switch (yAlign) {
            case screen_top -> yOffset;
            case screen_bottom -> screen.height - yOffset;
            case top -> top + yOffset;
            case bottom -> top + ((CTScreenDuck) screen).chesttracker$getHeight() - yOffset;
        };
    }

    public void apply(AbstractContainerScreen<?> parent, InventoryButton inventoryButton) {
        int x = Mth.clamp(getX(parent), SCREEN_MARGIN, parent.width - SCREEN_MARGIN - InventoryButton.SIZE);
        int y = Mth.clamp(getY(parent), SCREEN_MARGIN, parent.height - SCREEN_MARGIN - InventoryButton.SIZE);
        inventoryButton.setPosition(x, y);
    }

    static int getRecipeComponentWidth(AbstractContainerScreen<?> screen) {
        if (screen instanceof RecipeUpdateListener recipeHolder && recipeHolder.getRecipeBookComponent().isVisible()) {
            return RecipeBookComponent.IMAGE_WIDTH + 34;
        } else {
            return 0;
        }
    }

    static ButtonPosition calculate(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        final int width = ((CTScreenDuck) screen).chesttracker$getWidth();
        final int recipeWidth = getRecipeComponentWidth(screen);
        final int height = ((CTScreenDuck) screen).chesttracker$getHeight();

        final int left = ((CTScreenDuck) screen).chesttracker$getLeft();
        final int top = ((CTScreenDuck) screen).chesttracker$getTop();

        mouseX = Mth.clamp(mouseX, SCREEN_MARGIN, screen.width - SCREEN_MARGIN - InventoryButton.SIZE);
        mouseY = Mth.clamp(mouseY, SCREEN_MARGIN, screen.height - SCREEN_MARGIN - InventoryButton.SIZE);

        VerticalAlignment yAlign;
        int yOffset;

        // y calc
        int topScreenTopGuiMidpoint = top / 2;
        int guiTopGuiBottomMidpoint = top + height / 2;
        int guiBottomScreenBottom = (screen.height + top + height) / 2;

        if (mouseY <= topScreenTopGuiMidpoint) {
            yAlign = VerticalAlignment.screen_top;
            yOffset = mouseY;
        } else if (mouseY <= guiTopGuiBottomMidpoint) {
            yAlign = VerticalAlignment.top;
            yOffset = mouseY  - top;
        } else if (mouseY <= guiBottomScreenBottom) {
            yAlign = VerticalAlignment.bottom;
            yOffset = top + height - mouseY;
        } else {
            yAlign = VerticalAlignment.screen_bottom;
            yOffset = screen.height - mouseY;
        }

        HorizontalAlignment xAlign;
        int xOffset;

        int leftScreenLeftRecipeMidpoint = (left - recipeWidth) / 2;
        int leftGuiRightGuiMidpoint = left + width / 2;
        int rightGuiRightScreenMidpoint = (screen.width + left + width) / 2;

        if (mouseX <= leftScreenLeftRecipeMidpoint) {
            xAlign = HorizontalAlignment.screen_left;
            xOffset = mouseX;
        } else if (mouseX <= leftGuiRightGuiMidpoint) {
            if (mouseX <= left && mouseY > top && mouseY <= top + height) {
                xAlign = HorizontalAlignment.left_with_recipe;
                xOffset = mouseX - left + recipeWidth;
            } else {
                xAlign = HorizontalAlignment.left;
                xOffset = mouseX - left;
            }
        } else if (mouseX <= rightGuiRightScreenMidpoint) {
            xAlign = HorizontalAlignment.right;
            xOffset = left + width - mouseX;
        } else {
            xAlign = HorizontalAlignment.screen_right;
            xOffset = screen.width - mouseX;
        }

        return new ButtonPosition(xAlign, xOffset, yAlign, yOffset);
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

    private static <T> T getClosest(NavigableMap<Integer, T> options, int value) {
        var lower = options.floorEntry(value);
        var higher = options.higherEntry(value);
        if (lower == null) return higher.getValue();
        if (higher == null) return lower.getValue();
        int lowDistance = Math.abs(value - lower.getKey());
        int highDistance = Math.abs(value - higher.getKey());
        return lowDistance <= highDistance ? lower.getValue() : higher.getValue();
    }
}
