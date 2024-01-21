package red.jackf.chesttracker.gui.invbutton.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import red.jackf.chesttracker.util.Enums;
import red.jackf.chesttracker.util.GuiUtil;

public class RememberContainerButton extends SecondaryButton {
    private State state = State.DEFAULT;

    public RememberContainerButton() {
        super(State.DEFAULT.sprites, State.DEFAULT.tooltip, () -> {});
        this.onClick = this::cycleState;

        this.setState(State.DEFAULT);
    }

    @Override
    protected WidgetSprites getSprites() {
        return this.state.sprites;
    }

    private void cycleState() {
        setState(Enums.next(this.state));
    }

    private void setState(State state) {
        this.state = state;
        Component message = Component.translatable("chesttracker.inventoryButton.rememberContainer", state.tooltip);
        this.setMessage(message);
        this.setTooltip(Tooltip.create(message));
    }

    public enum State {
        ALWAYS(GuiUtil.twoSprite("inventory_button/remember_container/always"),
                Component.translatable("chesttracker.inventoryButton.rememberContainer.always").withStyle(ChatFormatting.GREEN)),
        DEFAULT(GuiUtil.twoSprite("inventory_button/remember_container/default"),
                Component.translatable("chesttracker.inventoryButton.rememberContainer.default").withStyle(ChatFormatting.GOLD)),
        NEVER(GuiUtil.twoSprite("inventory_button/remember_container/never"),
                Component.translatable("chesttracker.inventoryButton.rememberContainer.never").withStyle(ChatFormatting.RED));

        private final WidgetSprites sprites;
        private final Component tooltip;

        State(WidgetSprites sprites, Component tooltip) {
            this.sprites = sprites;
            this.tooltip = tooltip;
        }
    }
}
