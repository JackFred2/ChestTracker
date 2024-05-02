package red.jackf.chesttracker.impl.gui.widget;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public abstract class EnumSlider<E extends Enum<E>> extends SteppedSlider<E> {
    public EnumSlider(int x, int y, int width, int height, Class<E> enumClass, E initialValue, Function<E, Component> messager) {
        super(x, y, width, height, List.of(enumClass.getEnumConstants()), initialValue, messager);
    }
}
