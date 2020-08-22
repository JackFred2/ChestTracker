package red.jackf.chesttracker.gui.widgets;

import io.github.cottonmc.cotton.gui.widget.WTextField;
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
    public void onCharTyped(char ch) {
        super.onCharTyped(ch);
        if (onTextChanged != null) onTextChanged.accept(this.text);
    }

    @Override
    public void onKeyPressed(int ch, int key, int modifiers) {
        super.onKeyPressed(ch, key, modifiers);
        if (onTextChanged != null) onTextChanged.accept(this.text);
    }

    public void setOnTextChanged(Consumer<String> onCharTyped) {
        this.onTextChanged = onCharTyped;
    }
}
