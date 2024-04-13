package red.jackf.chesttracker.impl.gui.invbutton.position;

import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import red.jackf.chesttracker.impl.gui.invbutton.CTScreenDuck;

import java.util.*;

/**
 * <p>Utility methods for working with ScreenRectangles</p>
 */
public interface RectangleUtils {
    /**
     * Max radius to check before failing
     */
    int LIMIT = 100;
    /**
     * How far the menu borders extend in a cross shape, easier snapping to corners
     */
    int GUI_BORDER_EXTENSION = 8;
    /**
     * Interior gui min distance for snapping
     */
    int GUI_PADDING = 4;
    /**
     * Exterior gui min distance for snapping
     */
    int GUI_MARGIN = 2;
    /**
     * Border around slots for snapping
     */
    int SLOT_MARGIN = 2;
    /**
     * Buffer around screen edges for snapping
     */
    int SCREEN_MARGIN = 2;

    /**
     * Returns the smallest ScreenRectangle that covers all give rectangles.
     */
    static ScreenRectangle encompassing(List<ScreenRectangle> rectangles) {
        if (rectangles.isEmpty()) return ScreenRectangle.empty();
        if (rectangles.size() == 1) return rectangles.get(0);
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (ScreenRectangle rect : rectangles) {
            minX = Math.min(minX, rect.left());
            maxX = Math.max(maxX, rect.right());
            minY = Math.min(minY, rect.top());
            maxY = Math.max(maxY, rect.bottom());
        }

        return new ScreenRectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Inflates a screen rectangle in each direction by a given amount
     */
    static ScreenRectangle inflate(ScreenRectangle rectangle, int amount) {
        return new ScreenRectangle(
                rectangle.left() - amount,
                rectangle.top() - amount,
                rectangle.width() + 2 * amount,
                rectangle.height() + 2 * amount
        );
    }

    /**
     * Returns a list of screen rectangles that count as occupied positions for snapping purposes.
     */
    static Set<ScreenRectangle> getCollidersFor(AbstractContainerScreen<?> screen) {
        final int width = ((CTScreenDuck) screen).chesttracker$getWidth();
        final int recipeWidth = PositionUtils.getRecipeComponentWidth(screen);
        final int height = ((CTScreenDuck) screen).chesttracker$getHeight();

        final int left = ((CTScreenDuck) screen).chesttracker$getLeft();
        final int top = ((CTScreenDuck) screen).chesttracker$getTop();

        final int twiceMargin = 2 * GUI_MARGIN;
        final int twiceBorderExt = 2 * GUI_BORDER_EXTENSION;

        var colliders = new HashSet<ScreenRectangle>();

        // add nudging around borders around borders
        // these extend a bit in sort of a hashtag shape, for snapping outside the gui
        colliders.add(new ScreenRectangle(
                left - GUI_MARGIN - GUI_BORDER_EXTENSION,
                top - GUI_MARGIN,
                width + twiceMargin + twiceBorderExt,
                GUI_PADDING + GUI_MARGIN
        )); // top
        colliders.add(new ScreenRectangle(
                left - GUI_MARGIN - GUI_BORDER_EXTENSION,
                top + height - GUI_PADDING,
                width + twiceMargin + twiceBorderExt,
                GUI_PADDING + GUI_MARGIN
        )); // bottom

        colliders.add(new ScreenRectangle(
                left - GUI_MARGIN,
                top - GUI_MARGIN - GUI_BORDER_EXTENSION,
                GUI_PADDING + GUI_MARGIN,
                height + twiceMargin + twiceBorderExt
        )); // left
        colliders.add(new ScreenRectangle(
                left + width - GUI_PADDING,
                top - GUI_MARGIN - GUI_BORDER_EXTENSION,
                GUI_PADDING + GUI_MARGIN,
                height + twiceMargin + twiceBorderExt
        )); // right

        // recipe book IF OPEN
        final RecipeBookComponent recipe = PositionUtils.getVisibleRecipe(screen);
        if (recipe != null) {
            colliders.add(new ScreenRectangle(
                    left - recipeWidth - 2,
                    top - 2,
                    recipeWidth + 4,
                    height + 4
            ));
        }

        // add slots
        for (var slot : screen.getMenu().slots) {
            colliders.add(inflate(new ScreenRectangle(
                    left + slot.x,
                    top + slot.y,
                    16,
                    16
            ), SLOT_MARGIN));
        }

        return colliders;
    }

    /**
     * Tries to move a ScreenRectangle to be wholly within another ScreenRectangle. Returns the original if the moved instance
     * is bigger on either axis than the bounds.
     */
    static ScreenRectangle tryPlaceWithin(ScreenRectangle toMove, ScreenRectangle border) {
        // move horizontally
        if (toMove.width() <= border.width()) {
            if (toMove.right() > border.right())
                toMove = new ScreenRectangle(border.right() - toMove.width(), toMove.top(), toMove.width(), toMove.height());
            else if (toMove.left() < border.left())
                toMove = new ScreenRectangle(border.left(), toMove.top(), toMove.width(), toMove.height());
        }

        // move vertically
        if (toMove.height() <= border.height()) {
            if (toMove.bottom() > border.bottom())
                toMove = new ScreenRectangle(toMove.left(), border.bottom() - toMove.height(), toMove.width(), toMove.height());
            else if (toMove.top() < border.top())
                toMove = new ScreenRectangle(toMove.left(), border.top(), toMove.width(), toMove.height());
        }

        return toMove;
    }

    /**
     * Tries to find a free position around a given rectangle that doesn't collide with any other rectangles passed in.
     * @param start Rectangle to move and try to fit
     * @param colliders List of collision boxes {@code start} won't be in
     * @param border Screen border space
     * @return Optional with a free position if successful, empty otherwise
     */
    static Optional<ScreenRectangle> adjust(ScreenRectangle start, Set<ScreenRectangle> colliders, ScreenRectangle border) {
        // adjust start to be within bounds
        start = tryPlaceWithin(start, border);

        if (isFree(start, colliders, border)) return Optional.of(start);

        for (int i = 0; i < LIMIT; i++) {
            for (var pos : getPositionsAtRadius(start.position(), i)) {
                var rect = new ScreenRectangle(pos, start.width(), start.height());
                if (isFree(rect, colliders, border)) return Optional.of(rect);
            }
        }

        return Optional.empty();
    }

    /**
     * Test whether a give rectangle does not collide with any given colliders, and is within screen space.
     */
    static boolean isFree(ScreenRectangle start, Set<ScreenRectangle> colliders, ScreenRectangle border) {
        if (start.left() < border.left() ||
                start.right() > border.right() ||
                start.top() < border.top() ||
                start.bottom() > border.bottom()) return false;

        for (ScreenRectangle collider : colliders) {
            if (start.overlaps(collider)) return false;
        }
        return true;
    }

    /**
     * Moves a ScreenRectangle along a direction by a given amount.
     */
    static ScreenRectangle step(ScreenRectangle start, ScreenDirection dir, int amount) {
        var pos = switch (dir) {
            case UP -> new ScreenPosition(start.position().x(), start.position().y() - amount);
            case DOWN -> new ScreenPosition(start.position().x(), start.position().y() + amount);
            case LEFT -> new ScreenPosition(start.position().x() - amount, start.position().y());
            case RIGHT -> new ScreenPosition(start.position().x() + amount, start.position().y());
        };
        return new ScreenRectangle(pos, start.width(), start.height());
    }

    /**
     * Moves a ScreenRectangle along a direction, and the right of that direction, by specified amounts. Faster than
     * chaining {@link #step(ScreenRectangle, ScreenDirection, int)}.
     */
    private static ScreenPosition stepOrthogonal(ScreenPosition start, ScreenDirection dir, int amount, int orthAmount) {
        return switch (dir) {
            case UP -> new ScreenPosition(start.x() + orthAmount, start.y() - amount);
            case DOWN -> new ScreenPosition(start.x() - orthAmount, start.y() + amount);
            case LEFT -> new ScreenPosition(start.x() - amount, start.y() - orthAmount);
            case RIGHT -> new ScreenPosition(start.x() + amount, start.y() + orthAmount);
        };
    }

    /**
     * Returns all positions a given radius away from the origin, measured as Chebyshev distance. The given iterable is
     * in order of closest to an axis first.
     */
    private static Iterable<ScreenPosition> getPositionsAtRadius(ScreenPosition origin, int radius) {
        var list = new ArrayList<ScreenPosition>(8 * radius);

        for (int i = 0; i < radius; i++) {
            for (ScreenDirection dir : ScreenDirection.values()) {
                list.add(stepOrthogonal(origin, dir, radius, i));
                list.add(stepOrthogonal(origin, dir, radius,  - 1 - i));
            }
        }

        return list;
    }
}
