package red.jackf.chesttracker.impl.gui.util;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface CTTitleOverrideDuck {
    void chesttracker$setTitleOverride(@NotNull Component title);

    void chesttracker$clearTitleOverride();

    Component chesttracker$getOriginalTitle();
}
