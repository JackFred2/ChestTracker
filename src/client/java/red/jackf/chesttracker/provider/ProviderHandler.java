package red.jackf.chesttracker.provider;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.api.provider.Provider;
import red.jackf.chesttracker.compat.servers.hypixel.HypixelProvider;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class ProviderHandler {
    public static final Provider DEFAULT = new DefaultProvider();
    public static @Nullable Provider INSTANCE = null;

    private static final List<Provider> PROVIDERS = new ArrayList<>();

    public static @Nullable ResourceLocation getCurrentKey() {
        return INSTANCE != null ? INSTANCE.getPlayersCurrentKey().orElse(null) : null;
    }

    public static void register(Provider provider) {
        PROVIDERS.add(provider);
    }

    public static void load(Coordinate coordinate) {
        for (Provider provider : PROVIDERS) {
            if (provider.applies(coordinate)) {
                INSTANCE = provider;
                return;
            }
        }
        INSTANCE = DEFAULT;
    }

    public static void unload() {
        INSTANCE = null;
    }

    public static void setup() {
        // TODO move into self plugin
        Provider.register(HypixelProvider.INSTANCE);

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (INSTANCE == null) return;
            if (!overlay) INSTANCE.onChatMessage(message);
        });


    }
}
