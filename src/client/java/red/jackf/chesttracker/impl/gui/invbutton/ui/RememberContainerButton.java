package red.jackf.chesttracker.impl.gui.invbutton.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import red.jackf.chesttracker.impl.util.GuiUtil;
import red.jackf.chesttracker.impl.util.Misc;

import java.util.function.Supplier;

public class RememberContainerButton extends SecondaryButton {
    private State state = State.REMEMBER;

    public RememberContainerButton() {
        super(State.REMEMBER.sprites, State.REMEMBER.tooltip.get(), () -> {});
        this.onClick = this::cycleState;

        this.setState(State.REMEMBER);
    }

    @Override
    protected WidgetSprites getSprites() {
        return this.state.sprites;
    }

    private void cycleState() {
        setState(Misc.next(this.state));
    }

    private void setState(State state) {
        this.state = state;
        Component message = state.tooltip.get();
        this.setMessage(message);
        this.setTooltip(Tooltip.create(message));
    }

    public enum State {
        REMEMBER(GuiUtil.twoSprite("inventory_button/remember_container/always"),
                () -> Component.translatable("chesttracker.inventoryButton.rememberContainer.remember").withStyle(ChatFormatting.GREEN)),
        BLOCK(GuiUtil.twoSprite("inventory_button/remember_container/never"),
                () -> Component.translatable("chesttracker.inventoryButton.rememberContainer.block").withStyle(ChatFormatting.RED));

        private final WidgetSprites sprites;
        private final Supplier<Component> tooltip;

        State(WidgetSprites sprites, Supplier<Component> tooltip) {
            this.sprites = sprites;
            this.tooltip = tooltip;
        }
    }
}
