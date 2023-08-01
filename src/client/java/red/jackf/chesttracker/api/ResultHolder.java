package red.jackf.chesttracker.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a result of an action that either holds an object ({@link State#VALUE}), an explicit lack of an object
 * ({@link State#EMPTY}), or to pass for further processing ({@link State#PASS}).
 * processing.
 *
 * @param <T> Type of result to hold, in the case of {@link State#VALUE}.
 */
public final class ResultHolder<T> {
    private final State state;
    private final T value;
    private ResultHolder(State state, T value) {
        this.state = state;
        this.value = value;
    }

    /**
     * Create a <i>terminating</i> result with a given value.
     * @param value Value to be held by this result
     * @return Built VALUE result holding the given value
     * @param <T> Type of value held by this result
     * @throws IllegalArgumentException If value == null
     */
    public static <T> ResultHolder<T> value(T value) {
        if (value == null) throw new IllegalArgumentException("Tried to create a VALUE result with a null value");
        return new ResultHolder<>(State.VALUE, value);
    }

    /**
     * Create a <i>terminating</i> result that explicitly holds no value.
     * @return Built EMPTY result holding the given value
     */
    public static <T> ResultHolder<T> empty() {
        return new ResultHolder<>(State.EMPTY, null);
    }

    /**
     * Create a <i>non-terminating</i> result that indicates further processing should occur.
     * @return Built PASS result holding the given value
     */
    public static <T> ResultHolder<T> pass() {
        return new ResultHolder<>(State.PASS, null);
    }

    /**
     * @return Whether this result has a non-null value
     */
    public boolean hasValue() {
        return state == State.VALUE;
    }

    /**
     * @return Whether, as part of an event chain, it should terminate at this result.
     */
    public boolean shouldTerminate() {
        return state != State.PASS;
    }

    /**
     * Get the value of this result, or throw if not a VALUE result.
     * @return Value contained within this result
     * @throws IllegalArgumentException If this result is not a VALUE result
     */
    @NotNull
    public T get() {
        if (state == State.VALUE) return value;
        throw new IllegalArgumentException("Tried to get value from a non-VALUE result");
    }

    /**
     * Get the value of this result if it contains one, or null if EMPTY.
     * @return Value contained within this result iF VALUE, or null if EMPTY
     * @throws IllegalArgumentException If this result is a PASS result.
     */
    @Nullable
    public T getNullable() {
        return switch (state) {
            case VALUE -> value;
            case EMPTY -> null;
            case PASS -> throw new IllegalArgumentException("Tried to get an output from a PASS result");
        };
    }


    private enum State {
        VALUE,
        EMPTY,
        PASS
    }
}
