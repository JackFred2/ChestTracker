package red.jackf.chesttracker.config.custom;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Variant of {@link ButtonOption} that forces the user to hold down the button to activate
 */
public class HoldToConfirmButtonOption implements Option<BiConsumer<YACLScreen, HoldToConfirmButtonOption>> {
    private final Component name;
    private final OptionDescription description;
    private final BiConsumer<YACLScreen, HoldToConfirmButtonOption> action;
    private boolean available;
    private final int holdTimeTicks;
    private final Controller<BiConsumer<YACLScreen, HoldToConfirmButtonOption>> controller;
    private final Binding<BiConsumer<YACLScreen, HoldToConfirmButtonOption>> binding;

    public HoldToConfirmButtonOption(
            @NotNull Component name,
            @Nullable OptionDescription description,
            @NotNull BiConsumer<YACLScreen, HoldToConfirmButtonOption> action,
            @Nullable Component text,
            boolean available,
            int holdTimeTicks
    ) {

        this.name = name;
        this.description = description == null ? OptionDescription.EMPTY : description;
        this.action = action;
        this.available = available;
        this.holdTimeTicks = holdTimeTicks;
        this.controller = text != null ? new HoldToConfirmActionController(this, text) : new HoldToConfirmActionController(this);
        this.binding = new EmptyBinderImpl();
    }

    @Override
    public @NotNull Component name() {
        return name;
    }

    @Override
    public @NotNull OptionDescription description() {
        return description;
    }

    public BiConsumer<YACLScreen, HoldToConfirmButtonOption> action() {
        return action;
    }

    @Override
    public @NotNull Component tooltip() {
        return description().text();
    }

    @Override
    public @NotNull Controller<BiConsumer<YACLScreen, HoldToConfirmButtonOption>> controller() {
        return controller;
    }

    @Override
    public @NotNull Binding<BiConsumer<YACLScreen, HoldToConfirmButtonOption>> binding() {
        return binding;
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public @NotNull ImmutableSet<OptionFlag> flags() {
        return ImmutableSet.of();
    }

    @Override
    public boolean changed() {
        return false;
    }

    @Override
    public @NotNull BiConsumer<YACLScreen, HoldToConfirmButtonOption> pendingValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void requestSet(BiConsumer<YACLScreen, HoldToConfirmButtonOption> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean applyValue() {
        return false;
    }

    @Override
    public void forgetPendingValue() {
    }

    @Override
    public void requestSetDefault() {
    }

    @Override
    public boolean isPendingValueDefault() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(BiConsumer<Option<BiConsumer<YACLScreen, HoldToConfirmButtonOption>>, BiConsumer<YACLScreen, HoldToConfirmButtonOption>> changedListener) {
    }

    public long holdTime() {
        return holdTimeTicks;
    }

    private static class EmptyBinderImpl implements Binding<BiConsumer<YACLScreen, HoldToConfirmButtonOption>> {
        @Override
        public void setValue(BiConsumer<YACLScreen, HoldToConfirmButtonOption> value) {

        }

        @Override
        public BiConsumer<YACLScreen, HoldToConfirmButtonOption> getValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BiConsumer<YACLScreen, HoldToConfirmButtonOption> defaultValue() {
            throw new UnsupportedOperationException();
        }
    }
}
