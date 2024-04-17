package red.jackf.chesttracker.impl.memory.key;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.Optional;

public class OverrideInfo {
    public static final Codec<OverrideInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            JFLCodecs.forEnum(ManualMode.class).optionalFieldOf("manualMode")
                    .forGetter(info -> info.manualMode == ManualMode.DEFAULT ? Optional.empty() : Optional.of(info.manualMode))
    ).apply(instance, manualMode -> new OverrideInfo(manualMode.orElse(ManualMode.DEFAULT))));

    private ManualMode manualMode;

    // blank copy
    public OverrideInfo() {
        this.manualMode = ManualMode.DEFAULT;
    }

    public OverrideInfo(ManualMode manualMode) {
        this.manualMode = manualMode;
    }

    public boolean shouldKeep() {
        return this.manualMode != ManualMode.DEFAULT;
    }

    public ManualMode getManualMode() {
        return manualMode;
    }

    public void setManualMode(ManualMode mode) {
        this.manualMode = mode;
    }
}
