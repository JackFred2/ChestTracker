package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Function;

public abstract class SteppedSlider<T> extends AbstractSliderButton {
    private final List<T> options;
    private final Function<T, Component> messager;

    public SteppedSlider(int x, int y, int width, int height, List<T> options, T initial, Function<T, Component> messager) {
        super(x, y, width, height, CommonComponents.EMPTY, 0.0);
        if (options.isEmpty()) throw new IllegalArgumentException("Can't have no options");
        this.messager = messager;
        this.options = List.copyOf(options);
        int index = options.indexOf(initial);
        this.value = index == -1 ? 0 : (double) index / (options.size() - 1);

        updateMessage();
    }

    public T getSelected() {
        return options.get(Mth.floor(Mth.clampedLerp(0, options.size() - 1, this.value)));
    }

    @Override
    protected void updateMessage() {
        this.setMessage(messager.apply(getSelected()));
    }
}
