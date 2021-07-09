package red.jackf.chesttracker.mixins;

import io.github.cottonmc.cotton.gui.widget.WCardPanel;
import io.github.cottonmc.cotton.gui.widget.WTabPanel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(WTabPanel.class)
public interface AccessorWTabPanel {
    @Accessor
    WCardPanel getMainPanel();
}
