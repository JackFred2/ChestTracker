package red.jackf.chesttracker.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnumSlider<E extends Enum<E>> extends AbstractSliderButton {
    private final Function<E, Component> labelGetter;
    private final List<E> options;
    private final Consumer<E> onChange;

    public EnumSlider(int x, int y, int width, int height, Class<E> enumClass, E initialValue, Function<E, Component> labelGetter, Consumer<E> onChange) {
        super(x, y, width, height, CommonComponents.EMPTY, 0.0);
        this.onChange = onChange;
        assert enumClass.getEnumConstants().length > 0;
        this.options = List.of(enumClass.getEnumConstants());
        this.labelGetter = labelGetter;

        this.value = (double) options.indexOf(initialValue) / (options.size() - 1);

        updateMessage();
    }

    private E getValue() {
        return options.get(Mth.floor(Mth.clampedLerp(0, options.size() - 1, this.value)));
    }

    @Override
    protected void updateMessage() {
        this.setMessage(labelGetter.apply(getValue()));
    }

    @Override
    protected void applyValue() {
        onChange.accept(getValue());
    }
}
