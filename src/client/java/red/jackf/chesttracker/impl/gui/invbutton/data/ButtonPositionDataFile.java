package red.jackf.chesttracker.impl.gui.invbutton.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import red.jackf.chesttracker.impl.gui.invbutton.position.ButtonPosition;

import java.util.List;

public record ButtonPositionDataFile(List<String> classNames, ButtonPosition position) {
    public static final Codec<ButtonPositionDataFile> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.listOf().fieldOf("class_names").forGetter(ButtonPositionDataFile::classNames),
                    ButtonPosition.CODEC.fieldOf("position").forGetter(ButtonPositionDataFile::position)
            ).apply(instance, ButtonPositionDataFile::new));
}
