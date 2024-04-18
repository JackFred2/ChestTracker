package red.jackf.chesttracker.impl.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import red.jackf.chesttracker.impl.config.ChestTrackerConfig;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class CompatibilitySettings {
    protected static final Codec<CompatibilitySettings> CODEC = RecordCodecBuilder.create(instance -> {
        final var def = new CompatibilitySettings();
        return instance.group(
                JFLCodecs.forEnum(NameFilterMode.class).optionalFieldOf("nameFilterMode")
                        .forGetter(settings -> Optional.of(settings.nameFilterMode)),
                Codec.BOOL.optionalFieldOf("displayContainerNames")
                        .forGetter(settings -> Optional.of(settings.displayContainerNames))
        ).apply(instance, (nameFilterMode, displayContainerNames) -> new CompatibilitySettings(
                nameFilterMode.orElse(def.nameFilterMode),
                displayContainerNames.orElse(def.displayContainerNames)
        ));
    });

    public NameFilterMode nameFilterMode = NameFilterMode.NO_FILTER;
    public boolean displayContainerNames = ChestTrackerConfig.INSTANCE.instance().rendering.displayContainerNames;

    protected CompatibilitySettings() {}

    protected CompatibilitySettings(NameFilterMode nameFilterMode, boolean displayContainerNames) {
        this.nameFilterMode = nameFilterMode;
        this.displayContainerNames = displayContainerNames;
    }

    public CompatibilitySettings copy() {
        return new CompatibilitySettings(
                this.nameFilterMode,
                this.displayContainerNames
        );
    }

    private static Component filterRegex(Component component, Pattern regex) {
        MutableComponent base = Component.empty();
        component.visit((style, str) -> {
            String filtered = regex.matcher(str).replaceAll("");
            if (!filtered.isBlank()) {
                base.append(Component.literal(filtered).withStyle(style));
            }
            return Optional.<String>empty();
        }, Style.EMPTY);
        return base;
    }

    public enum NameFilterMode {
        NO_FILTER(c -> c,
                Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameFilterMode.none"),
                Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameFilterMode.none.tooltip")),
        ASCII(c -> filterRegex(c, Pattern.compile("[^\\u0000-\\u007F]")),
                Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameFilterMode.ascii"),
                Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameFilterMode.ascii.tooltip")),
        BASIC_UNICODE(c -> filterRegex(c, Pattern.compile("[^\\u0000-\\u33FF]")),
                Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameFilterMode.basicUnicode"),
                Component.translatable("chesttracker.gui.editMemoryBank.compatibility.nameFilterMode.basicUnicode.tooltip"));

        public final UnaryOperator<Component> filter;
        public final Component label;
        public final Component tooltip;

        NameFilterMode(UnaryOperator<Component> filter, Component label, Component tooltip) {
            this.filter = filter;
            this.label = label;
            this.tooltip = tooltip;
        }
    }
}
