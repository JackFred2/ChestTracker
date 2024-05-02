package red.jackf.chesttracker.impl.memory.key;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Optional;

public class OverrideInfo {
    public static final Codec<OverrideInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            JFLCodecs.forEnum(ManualMode.class).optionalFieldOf("manualMode")
                    .forGetter(info -> info.manualMode == ManualMode.DEFAULT ? Optional.empty() : Optional.of(info.manualMode)),
            Codec.STRING.optionalFieldOf("customName")
                    .forGetter(info -> Optional.ofNullable(info.customName))
    ).apply(instance, (manualMode, customName) -> new OverrideInfo(manualMode.orElse(ManualMode.DEFAULT), customName.orElse(null))));

    private ManualMode manualMode = ManualMode.DEFAULT;
    private @Nullable String customName = null;

    // blank copy
    public OverrideInfo() {}

    public OverrideInfo(ManualMode manualMode, @Nullable String customName) {
        this.manualMode = manualMode;
        this.customName = customName;
    }

    public boolean shouldKeep() {
        return this.manualMode != ManualMode.DEFAULT || this.customName != null;
    }

    public ManualMode getManualMode() {
        return manualMode;
    }

    public void setManualMode(ManualMode mode) {
        this.manualMode = mode;
    }

    public @Nullable String getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable String customName) {
        this.customName = customName;
    }
}
