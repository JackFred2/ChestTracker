package red.jackf.chesttracker.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class WHeldButton extends WButton {

    private Text text;
    private Text altText;
    private int timeNeededToActivate;
    private int timeHeldDown = 0;

    private int lastMouseX = 0;
    private int lastMouseY = 0;

    public WHeldButton(Text text, Text altText, int timeNeededToActivate) {
        this.text = text;
        this.altText = altText;
        this.timeNeededToActivate = timeNeededToActivate;
        this.setLabel(text);
    }

    public WHeldButton(Text text, int timeNeededToActivate) {
        this(text, text, timeNeededToActivate);
    }

    @Override
    public void paint(DrawContext matrices, int x, int y, int mouseX, int mouseY) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.setLabel(timeHeldDown > 0 ? altText : text);
        super.paint(matrices, x, y, mouseX, mouseY);

        int xProgress = MathHelper.floor((((float) this.timeHeldDown) / (this.timeNeededToActivate - 1)) * (this.width - 2));
        if (xProgress > 0) ScreenDrawing.coloredRect(matrices, x + 1, y + 1, xProgress, this.height - 2, 0x60FF4F4F);
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        if (isEnabled() && isWithinBounds(x, y)) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return InputResult.PROCESSED;
        }
        return InputResult.IGNORED;
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (isEnabled() && lastMouseX >= 0 && lastMouseY >= 0 && lastMouseX < getWidth() && lastMouseY < getHeight() && GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == 1) {
            timeHeldDown++;
            if (timeHeldDown == timeNeededToActivate) {
                if (getOnClick() != null) getOnClick().run();
                timeHeldDown = 0;
            } else if (timeHeldDown % 4 == 0) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F + (0.4f * timeHeldDown / timeNeededToActivate)));
            }
        } else if (timeHeldDown > 0) {
            timeHeldDown -= 2;
            if (timeHeldDown < 0) timeHeldDown = 0;
        }
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public int getTimeNeededToActivate() {
        return timeNeededToActivate;
    }

    public void setTimeNeededToActivate(int holdTime) {
        this.timeNeededToActivate = holdTime;
    }
}
