package red.jackf.chesttracker.impl.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import red.jackf.chesttracker.impl.config.custom.HoldToConfirmActionController;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class HoldToConfirmButton extends AbstractButton {
    private final Consumer<HoldToConfirmButton> callback;
    private final long holdToActivateTime;
    private static final int PROGRESS_TICKS = 5;

    private final Set<Integer> held = new HashSet<>(4);
    private float progress = 0f;
    private int progressTicks = 0;

    public HoldToConfirmButton(int x, int y, int width, int height, Component component, long holdToActivateTime, Consumer<HoldToConfirmButton> callback) {
        super(x, y, width, height, component);
        this.holdToActivateTime = holdToActivateTime;
        this.callback = callback;
    }

    @Override
    public void onPress() {
        playDownSound(getPitch());
        callback.accept(this);
    }

    private float getPitch() {
        return 1f + 0.2f * (progress / holdToActivateTime);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (progress > 0f)
            graphics.fill(getX() + 1,
                    getY() + 1,
                    (int) (getX() + (progress / holdToActivateTime) * (width - 2)),
                    getY() + getHeight() - 1,
                    HoldToConfirmActionController.Widget.PROGRESS_COLOUR);
        if (!held.isEmpty()) {
            progress = Math.min(holdToActivateTime, progress + partialTick);
            if (progress == holdToActivateTime) {
                this.onPress();
                progress = 0f;
                progressTicks = 0;
            }
            var newProgressTicks = (int) (progress * PROGRESS_TICKS / holdToActivateTime);
            if (newProgressTicks > progressTicks) playDownSound(getPitch());
            progressTicks = newProgressTicks;
        } else {
            progress = Math.max(0, progress - HoldToConfirmActionController.Widget.REGRESSION_MULTIPLIER * partialTick);
        }

        if (!active) held.clear();
        if (this.held.contains(-1) && !this.isMouseOver(mouseX, mouseY)) held.remove(-1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && active) {
            playDownSound(1f);
            held.add(-1);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        held.remove(-1);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (active && !isMouseOver(mouseX, mouseY)) held.remove(-1);
        super.mouseMoved(mouseX, mouseY);
    }

    private static boolean isActivationKeybind(int keyCode) {
        return keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_SPACE || keyCode == InputConstants.KEY_NUMPADENTER;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) {
            return false;
        }

        if (isActivationKeybind(keyCode)) {
            if (held.isEmpty()) playDownSound(1f);
            held.add(keyCode);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (isActivationKeybind(keyCode)) {
            held.remove(keyCode);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }


    public void playDownSound(float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
