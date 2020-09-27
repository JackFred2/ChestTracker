package red.jackf.chesttracker.gui.widgets;

import io.github.cottonmc.cotton.gui.client.LibGuiClient;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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
    }

    public WHeldButton(Text text, int timeNeededToActivate) {
        this(text, text, timeNeededToActivate);
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
        ScreenDrawing.drawBeveledPanel(x, y, this.width, this.height, 0xFFFFFFFF, hovered ? 0xFF8892C9 : 0xFFC6C6C6, hovered ? 0xFF00073E : 0xFF000000);
        int xProgress = MathHelper.floor((((float) this.timeHeldDown) / (this.timeNeededToActivate - 1)) * (this.width - 2));
        if (xProgress > 0) ScreenDrawing.coloredRect(x + 1, y + 1, xProgress, this.height - 2, 0xFFFF4F4F);
        ScreenDrawing.drawString(matrices, timeHeldDown > 0 ? altText.asOrderedText() : text.asOrderedText(), x + 6, y + 6, color);
    }

    @Override
    public void onClick(int x, int y, int button) {
        if (isEnabled() && isWithinBounds(x, y)) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (lastMouseX >= 0 && lastMouseY >= 0 && lastMouseX < getWidth() && lastMouseY < getHeight() && GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == 1) {
            timeHeldDown++;
            if (timeHeldDown == timeNeededToActivate) {
                if (getOnClick() != null) getOnClick().run();
                timeHeldDown = 0;
            }
        } else if (timeHeldDown > 0) {
            timeHeldDown--;
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
