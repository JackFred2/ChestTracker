package red.jackf.chesttracker.impl.gui.invbutton.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import red.jackf.chesttracker.api.providers.MemoryLocation;
import red.jackf.chesttracker.impl.memory.MemoryBankImpl;
import red.jackf.chesttracker.impl.memory.MemoryKeyImpl;
import red.jackf.chesttracker.impl.memory.key.ManualMode;
import red.jackf.chesttracker.impl.memory.key.OverrideInfo;
import red.jackf.chesttracker.impl.util.GuiUtil;
import red.jackf.chesttracker.impl.util.Misc;

import java.util.Optional;
import java.util.function.Supplier;

public class RememberContainerButton extends SecondaryButton {
    private final MemoryBankImpl bank;
    private final MemoryLocation memoryLocation;
    private State state = State.REMEMBER;

    public RememberContainerButton(MemoryBankImpl bank, MemoryLocation memoryLocation) {
        super(State.REMEMBER.sprites, State.REMEMBER.tooltip.get(), () -> {});
        this.bank = bank;
        this.memoryLocation = memoryLocation;
        this.onClick = this::cycleState; // some init error if we do it in constructor

        State initial = bank.getMetadata().getFilteringSettings().manualMode ? State.BLOCK : State.REMEMBER;
        Optional<MemoryKeyImpl> key = bank.getKeyInternal(this.memoryLocation.memoryKey());
        if (key.isPresent()) {
            OverrideInfo info = key.get().overrides().get(this.memoryLocation.position());
            if (info != null && info.getManualMode() != ManualMode.DEFAULT) {
                initial = info.getManualMode() == ManualMode.REMEMBER  ? State.REMEMBER : State.BLOCK;
            } else if (key.get().get(memoryLocation.position()).isPresent()) {
                initial = State.REMEMBER;
            }
        }

        this.setState(initial);
    }

    @Override
    protected WidgetSprites getSprites() {
        return this.state.sprites;
    }

    private void cycleState() {
        setState(Misc.next(this.state));

        if (this.state == State.BLOCK) {
            this.bank.removeMemory(this.memoryLocation.memoryKey(), this.memoryLocation.position());
        }

        final boolean isManualMode = this.bank.getMetadata().getFilteringSettings().manualMode;
        ManualMode mode = ManualMode.DEFAULT;
        if (this.state == State.REMEMBER && isManualMode) {
            mode = ManualMode.REMEMBER;
        } else if (this.state == State.BLOCK && !isManualMode) {
            mode = ManualMode.BLOCK;
        }

        this.bank.setManualModeOverride(this.memoryLocation.memoryKey(), this.memoryLocation.position(), mode);
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
