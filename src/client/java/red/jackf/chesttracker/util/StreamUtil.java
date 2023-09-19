package red.jackf.chesttracker.util;

import java.util.Comparator;
import java.util.List;

public class StreamUtil {
    /**
     * Returns a sorter that brings elements in a given "head" list to the front, in the given order.
     * @param head List to check elements for and bring forwards
     * @return Comparator bringing specified elements to the front
     * @param <T> Type of elements being sorted
     */
    public static <T> Comparator<T> bringToFront(List<T> head) {
        final var immutable = List.copyOf(head);
        return (a, b) -> {
                int aIndex = immutable.indexOf(a);
                int bIndex = immutable.indexOf(b);
                if (aIndex != -1) {
                    if (bIndex != -1) {
                        return aIndex - bIndex;
                    } else {
                        return -1;
                    }
                } else {
                    if (bIndex != -1) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
        };
    }
}
