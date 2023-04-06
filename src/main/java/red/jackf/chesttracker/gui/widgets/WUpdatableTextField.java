package red.jackf.chesttracker.gui.widgets;

import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class WUpdatableTextField extends WTextField {
    private Consumer<String> onTextChanged;

    public WUpdatableTextField(Text text) {
        super(text);
    }

    @Override
    public InputResult onCharTyped(char ch) {
        var result = super.onCharTyped(ch);
        if (result == InputResult.PROCESSED && onTextChanged != null)onTextChanged.accept(getText());
        return result;
    }

    @Override
    public InputResult onKeyPressed(int ch, int key, int modifiers) {
        var result = super.onKeyPressed(ch, key, modifiers);
        if (result == InputResult.PROCESSED && onTextChanged != null) onTextChanged.accept(getText());
        return result;
    }

    public void setOnTextChanged(Consumer<String> onCharTyped) {
        this.onTextChanged = onCharTyped;
    }
}
