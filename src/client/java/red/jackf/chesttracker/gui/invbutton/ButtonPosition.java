package red.jackf.chesttracker.gui.invbutton;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.util.Nudge;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Optional;
import java.util.Set;

public record ButtonPosition(HorizontalAlignment xAlign, int xOffset, VerticalAlignment yAlign, int yOffset) {
    public static final Codec<ButtonPosition> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    JFLCodecs.forEnum(HorizontalAlignment.class).fieldOf("x_align").forGetter(ButtonPosition::xAlign),
                    Codec.INT.fieldOf("x_offset").forGetter(ButtonPosition::xOffset),
                    JFLCodecs.forEnum(VerticalAlignment.class).fieldOf("y_align").forGetter(ButtonPosition::yAlign),
                    Codec.INT.fieldOf("y_offset").forGetter(ButtonPosition::yOffset)
            ).apply(instance, ButtonPosition::new));

    public int getX(AbstractContainerScreen<?> screen) {
        int left = ((CTScreenDuck) screen).chesttracker$getLeft();
        int recipeWidth = getRecipeComponentWidth(screen);

        return Mth.clamp(switch (xAlign) {
            case screen_left -> xOffset;
            case screen_right -> screen.width - xOffset;
            case left_with_recipe -> left - recipeWidth + xOffset;
            case left -> left + xOffset;
            case right -> left + ((CTScreenDuck) screen).chesttracker$getWidth() - xOffset;
        }, Nudge.SCREEN_MARGIN, screen.width - Nudge.SCREEN_MARGIN - InventoryButton.SIZE);
    }

    public int getY(AbstractContainerScreen<?> screen) {
        int top = ((CTScreenDuck) screen).chesttracker$getTop();

        return Mth.clamp(switch (yAlign) {
            case screen_top -> yOffset;
            case screen_bottom -> screen.height - yOffset;
            case top -> top + yOffset;
            case bottom -> top + ((CTScreenDuck) screen).chesttracker$getHeight() - yOffset;
        }, Nudge.SCREEN_MARGIN, screen.height - Nudge.SCREEN_MARGIN - InventoryButton.SIZE);
    }

    public void apply(AbstractContainerScreen<?> parent, InventoryButton inventoryButton) {
        int x = getX(parent);
        int y = getY(parent);
        inventoryButton.setPosition(x, y);
    }

    public static @Nullable RecipeBookComponent getVisibleRecipe(AbstractContainerScreen<?> screen) {
        if (screen instanceof RecipeUpdateListener recipeHolder && recipeHolder.getRecipeBookComponent().isVisible()) {
            return recipeHolder.getRecipeBookComponent();
        } else {
            return null;
        }
    }

    public static int getRecipeComponentWidth(AbstractContainerScreen<?> screen) {
        return getVisibleRecipe(screen) == null ? 0 : RecipeBookComponent.IMAGE_WIDTH + 32;
    }

    static Optional<ButtonPosition> calculate(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        final int width = ((CTScreenDuck) screen).chesttracker$getWidth();
        final int recipeWidth = getRecipeComponentWidth(screen);
        final int height = ((CTScreenDuck) screen).chesttracker$getHeight();

        final int left = ((CTScreenDuck) screen).chesttracker$getLeft();
        final int top = ((CTScreenDuck) screen).chesttracker$getTop();

        // so we're dragging the center of the button
        mouseX -= InventoryButton.SIZE / 2;
        mouseY -= InventoryButton.SIZE / 2;

        // keep within screen bounds
        mouseX = Mth.clamp(mouseX, Nudge.SCREEN_MARGIN, screen.width - Nudge.SCREEN_MARGIN - InventoryButton.SIZE);
        mouseY = Mth.clamp(mouseY, Nudge.SCREEN_MARGIN, screen.height - Nudge.SCREEN_MARGIN - InventoryButton.SIZE);

        if (!Screen.hasShiftDown()) {
            // don't allow in recipe book
            Set<ScreenRectangle> collisions = Nudge.getCollidersFor(screen);

            // apply
            var nudged = Nudge.adjust(new ScreenRectangle(mouseX, mouseY, InventoryButton.SIZE, InventoryButton.SIZE), collisions, screen.getRectangle());

            if (nudged.isEmpty()) return Optional.empty();

            mouseX = nudged.get().left();
            mouseY = nudged.get().top();
        }

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
            if (mouseX <= left - 2 && mouseY > top && mouseY <= top + height) {
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

        return Optional.of(new ButtonPosition(xAlign, xOffset, yAlign, yOffset));
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
