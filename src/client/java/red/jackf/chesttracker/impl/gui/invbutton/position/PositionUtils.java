package red.jackf.chesttracker.impl.gui.invbutton.position;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.gui.invbutton.CTButtonScreenDuck;
import red.jackf.chesttracker.impl.gui.invbutton.ui.InventoryButton;

import java.util.Optional;
import java.util.Set;

/**
 * Calculation Methods for buttons
 */
public interface PositionUtils {
    /**
     * Gets the recipe book component current visible on screen, if any.
     */
    static @Nullable RecipeBookComponent getVisibleRecipe(AbstractContainerScreen<?> screen) {
        if (screen instanceof RecipeUpdateListener recipeHolder && recipeHolder.getRecipeBookComponent().isVisible()) {
            return recipeHolder.getRecipeBookComponent();
        } else {
            return null;
        }
    }

    static boolean isRecipeBookVisible(AbstractContainerScreen<?> screen) {
        return getVisibleRecipe(screen) != null;
    }

    /**
     * Gets the width of the recipe component currently visible, or 0 if none. Padded a bit to account for recipe tabs.
     */
    static int getRecipeComponentWidth(AbstractContainerScreen<?> screen) {
        return getVisibleRecipe(screen) == null ? 0 : RecipeBookComponent.IMAGE_WIDTH + 33;
    }

    /**
     * Calculate a free button position from a give cursor X and Y. Snaps to within the screen, and if Shift isn't held
     * snaps around the GUI borders and various elements. Returns an empty optional if none could be found.
     */
    static Optional<ButtonPosition> calculate(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        final int width = ((CTButtonScreenDuck) screen).chesttracker$getWidth();
        final int recipeWidth = getRecipeComponentWidth(screen);
        final int height = ((CTButtonScreenDuck) screen).chesttracker$getHeight();

        final int left = ((CTButtonScreenDuck) screen).chesttracker$getLeft();
        final int top = ((CTButtonScreenDuck) screen).chesttracker$getTop();

        // so we're dragging the center of the button
        mouseX -= InventoryButton.SIZE / 2;
        mouseY -= InventoryButton.SIZE / 2;

        // keep within screen bounds
        mouseX = Mth.clamp(mouseX, RectangleUtils.SCREEN_MARGIN, screen.width - RectangleUtils.SCREEN_MARGIN - InventoryButton.SIZE);
        mouseY = Mth.clamp(mouseY, RectangleUtils.SCREEN_MARGIN, screen.height - RectangleUtils.SCREEN_MARGIN - InventoryButton.SIZE);

        if (!Screen.hasShiftDown()) {
            // don't allow in recipe book
            Set<ScreenRectangle> collisions = RectangleUtils.getCollidersFor(screen);

            // apply
            var nudged = RectangleUtils.adjust(new ScreenRectangle(mouseX, mouseY, InventoryButton.SIZE, InventoryButton.SIZE), collisions, screen.getRectangle());

            if (nudged.isEmpty()) return Optional.empty();

            mouseX = nudged.get().left();
            mouseY = nudged.get().top();
        }

        ButtonPosition.VerticalAlignment yAlign;
        int yOffset;

        // y calc
        int topScreenTopGuiMidpoint = top / 2;
        int guiTopGuiBottomMidpoint = top + height / 2;
        int guiBottomScreenBottom = (screen.height + top + height) / 2;

        if (mouseY <= topScreenTopGuiMidpoint) {
            yAlign = ButtonPosition.VerticalAlignment.screen_top;
            yOffset = mouseY;
        } else if (mouseY <= guiTopGuiBottomMidpoint) {
            yAlign = ButtonPosition.VerticalAlignment.top;
            yOffset = mouseY  - top;
        } else if (mouseY <= guiBottomScreenBottom) {
            yAlign = ButtonPosition.VerticalAlignment.bottom;
            yOffset = top + height - mouseY;
        } else {
            yAlign = ButtonPosition.VerticalAlignment.screen_bottom;
            yOffset = screen.height - mouseY;
        }

        ButtonPosition.HorizontalAlignment xAlign;
        int xOffset;

        int leftScreenLeftRecipeMidpoint = (left - recipeWidth) / 2;
        int leftGuiRightGuiMidpoint = left + width / 2;
        int rightGuiRightScreenMidpoint = (screen.width + left + width) / 2;

        if (mouseX <= leftScreenLeftRecipeMidpoint) {
            xAlign = ButtonPosition.HorizontalAlignment.screen_left;
            xOffset = mouseX;
        } else if (mouseX <= leftGuiRightGuiMidpoint) {
            if (mouseX <= left - 2 && mouseY > top && mouseY <= top + height) {
                xAlign = ButtonPosition.HorizontalAlignment.left_with_recipe;
                xOffset = mouseX - left + recipeWidth;
            } else {
                xAlign = ButtonPosition.HorizontalAlignment.left;
                xOffset = mouseX - left;
            }
        } else if (mouseX <= rightGuiRightScreenMidpoint) {
            xAlign = ButtonPosition.HorizontalAlignment.right;
            xOffset = left + width - mouseX;
        } else {
            xAlign = ButtonPosition.HorizontalAlignment.screen_right;
            xOffset = screen.width - mouseX;
        }

        return Optional.of(new ButtonPosition(xAlign, xOffset, yAlign, yOffset));
    }
}
