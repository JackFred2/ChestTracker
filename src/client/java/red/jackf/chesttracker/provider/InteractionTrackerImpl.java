package red.jackf.chesttracker.provider;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.provider.InteractionTracker;
import red.jackf.chesttracker.util.CachedClientBlockSource;

import java.util.Optional;

public class InteractionTrackerImpl implements InteractionTracker {
    public static final InteractionTrackerImpl INSTANCE = new InteractionTrackerImpl();

    private @Nullable ClientBlockSource lastSource = null;

    public static void setup() {
        // Event Setup
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (hand == InteractionHand.MAIN_HAND && level instanceof ClientLevel clientLevel) {
                INSTANCE.setLastBlockSource(new CachedClientBlockSource(clientLevel, hitResult.getBlockPos()));
            }
            return InteractionResult.PASS;
        });
    }

    @Override
    public Optional<ClientLevel> getPlayerLevel() {
        if (Minecraft.getInstance().level == null) return Optional.empty();
        return Optional.of(Minecraft.getInstance().level);
    }

    @Override
    public Optional<ClientBlockSource> getLastBlockSource() {
        return Optional.ofNullable(lastSource);
    }

    public void clear() {
        this.lastSource = null;
    }

    public void setLastBlockSource(ClientBlockSource source) {
        this.lastSource = source;
    }
}
