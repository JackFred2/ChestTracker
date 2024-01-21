package red.jackf.chesttracker.gui.invbutton.ui;

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
        this.setMessage(state.tooltip);
        this.setTooltip(Tooltip.create(state.tooltip));
    }

    public enum State {
        YES(GuiUtil.twoSprite("inventory_button/should_remember/yes"), Component.literal("yes")),
        DEFAULT(GuiUtil.twoSprite("inventory_button/should_remember/default"), Component.literal("default")),
        NO(GuiUtil.twoSprite("inventory_button/should_remember/no"), Component.literal("no"));

        private final WidgetSprites sprites;
        private final Component tooltip;

        State(WidgetSprites sprites, Component tooltip) {
            this.sprites = sprites;
            this.tooltip = tooltip;
        }
    }
}
