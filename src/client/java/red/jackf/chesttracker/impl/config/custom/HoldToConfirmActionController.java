package red.jackf.chesttracker.impl.config.custom;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ActionController;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class HoldToConfirmActionController implements Controller<BiConsumer<YACLScreen, HoldToConfirmButtonOption>> {
    private final HoldToConfirmButtonOption option;
    private final Component text;

    public HoldToConfirmActionController(HoldToConfirmButtonOption option) {
        this(option, ActionController.DEFAULT_TEXT);
    }

    public HoldToConfirmActionController(HoldToConfirmButtonOption option, Component text) {
        this.option = option;
        this.text = text;
    }

    @Override
    public HoldToConfirmButtonOption option() {
        return option;
    }

    @Override
    public Component formatValue() {
        return text;
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new Widget(this, screen, widgetDimension, option.holdTime());
    }

    public static class Widget extends ControllerWidget<HoldToConfirmActionController> {
        public static final float REGRESSION_MULTIPLIER = 2;
        public static final int PROGRESS_COLOUR = 0x40_FF0000;
        private final String buttonString;
        private final long holdTime;

        private final Set<Integer> held = new HashSet<>(4);
        private float progress = 0f;

        public Widget(HoldToConfirmActionController control, YACLScreen screen, Dimension<Integer> dim, long holdTime) {
            super(control, screen, dim);
            buttonString = control.formatValue().getString().toLowerCase();
            this.holdTime = holdTime;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.render(graphics, mouseX, mouseY, delta);
            if (progress > 0f)
                graphics.fill(getDimension().x() + 1,
                        getDimension().y() + 1,
                        (int) (getDimension().x() + (progress / holdTime) * (getDimension().width() - 2)),
                        getDimension().yLimit() - 1,
                        PROGRESS_COLOUR);
            if (!held.isEmpty()) {
                progress = Math.min(holdTime, progress + delta);
                if (progress == holdTime) {
                    executeAction();
                    progress = 0f;
                }
            } else {
                progress = Math.max(0, progress - REGRESSION_MULTIPLIER * delta);
            }

            if (!this.isAvailable()) held.clear();
            if (this.held.contains(-1) && !this.isMouseOver(mouseX, mouseY)) held.remove(-1);
        }

        public void executeAction() {
            playDownSound();
            control.option().action().accept(screen, control.option());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && isAvailable()) {
                playDownSound();
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
            if (isAvailable() && !isMouseOver(mouseX, mouseY)) held.remove(-1);
            super.mouseMoved(mouseX, mouseY);
        }

        private static boolean isActivationKeybind(int keyCode) {
            return keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_SPACE || keyCode == InputConstants.KEY_NUMPADENTER;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!focused) {
                return false;
            }

            if (isActivationKeybind(keyCode)) {
                if (held.isEmpty()) playDownSound();
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
        public void unfocus() {
            super.unfocus();
            this.held.clear();
        }

        @Override
        protected int getHoveredControlWidth() {
            return getUnhoveredControlWidth();
        }

        @Override
        public boolean canReset() {
            return false;
        }

        @Override
        public boolean matchesSearch(String query) {
            return super.matchesSearch(query) || buttonString.contains(query);
        }
    }
}
