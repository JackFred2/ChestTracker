package red.jackf.chesttracker.gui.util;

import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

/**
 * <p>Nudges a screen rectangle into a free position closest to cursor. Pretty inefficiently, it checks each pixel position
 * up to a radius of 100 but it has no FPS drops on my machine and premature optimization is the root of all evil. If
 * you've got a faster algorithm to suggest please do.</p>
 */
public class Nudge {
    /**
     * Max radius to check before failing
     */
    private static final int LIMIT = 100;

    public static Optional<ScreenRectangle> adjust(ScreenRectangle start, Set<ScreenRectangle> colliders) {
        if (isFree(start, colliders)) return Optional.of(start);

        /*for (int i = 0; i < LIMIT; i++) {
            for (ScreenDirection direction : ScreenDirection.values()) {
                var pos = step(start, direction, i);
                if (isFree(pos, colliders)) return Optional.of(pos);
            }
        }*/

        /*
        for (Iterator<ScreenPosition> it = spiral(start.position(), 20); it.hasNext(); ) {
            var rect = new ScreenRectangle(it.next(), start.width(), start.height());
            if (isFree(rect, colliders)) return Optional.of(rect);
        }*/

        for (int i = 0; i < LIMIT; i++) {
            for (var pos : getAtRadius(start.position(), i)) {
                var rect = new ScreenRectangle(pos, start.width(), start.height());
                if (isFree(rect, colliders)) return Optional.of(rect);
            }
        }

        return Optional.empty();
    }

    private static boolean isFree(ScreenRectangle start, Set<ScreenRectangle> colliders) {
        for (ScreenRectangle collider : colliders) {
            if (start.overlaps(collider)) return false;
        }
        return true;
    }

    /*
    private static ScreenRectangle step(ScreenRectangle start, ScreenDirection dir, int amount) {
        var pos = switch (dir) {
            case UP -> new ScreenPosition(start.position().x(), start.position().y() - amount);
            case DOWN -> new ScreenPosition(start.position().x(), start.position().y() + amount);
            case LEFT -> new ScreenPosition(start.position().x() - amount, start.position().y());
            case RIGHT -> new ScreenPosition(start.position().x() + amount, start.position().y());
        };
        return new ScreenRectangle(pos, start.width(), start.height());
    }*/

    private static ScreenPosition stepOrth(ScreenPosition start, ScreenDirection dir, int amount, int orthAmount) {
        return switch (dir) {
            case UP -> new ScreenPosition(start.x() + orthAmount, start.y() - amount);
            case DOWN -> new ScreenPosition(start.x() - orthAmount, start.y() + amount);
            case LEFT -> new ScreenPosition(start.x() - amount, start.y() - orthAmount);
            case RIGHT -> new ScreenPosition(start.x() + amount, start.y() + orthAmount);
        };
    }

    private static Iterable<ScreenPosition> getAtRadius(ScreenPosition origin, int radius) {
        var list = new ArrayList<ScreenPosition>(8 * radius);

        for (int i = 0; i < radius; i++) {
            for (ScreenDirection dir : ScreenDirection.values()) {
                list.add(stepOrth(origin, dir, radius, i));
                list.add(stepOrth(origin, dir, radius,  - 1 - i));
            }
        }

        return list;
    }

    /*
    private static ScreenDirection right(ScreenDirection in) {
        return switch (in) {
            case UP -> ScreenDirection.RIGHT;
            case RIGHT -> ScreenDirection.DOWN;
            case DOWN -> ScreenDirection.LEFT;
            case LEFT -> ScreenDirection.UP;
        };
    }

    private static Iterator<ScreenPosition> spiral(ScreenPosition origin, int radius) {
        return new Iterator<>() {
            private final int max = (1 + 2 * radius) * (1 + 2 * radius);
            private int index = 0;
            private int currentStep = 0;
            private int currentStepLength = 1;
            private ScreenPosition pos = origin;
            private ScreenDirection dir = ScreenDirection.UP;

            @Override
            public boolean hasNext() {
                return index < max;
            }

            @Override
            public ScreenPosition next() {
                if (index >= max) throw new NoSuchElementException();
                var result = pos;

                index++;
                currentStep++;
                pos = pos.step(dir);
                if (currentStep >= currentStepLength) {
                    dir = right(dir);
                    currentStep = 0;
                    if (dir.getAxis() == ScreenAxis.VERTICAL) currentStepLength += 1;
                }

                return result;
            }
        };
    }*/
}
