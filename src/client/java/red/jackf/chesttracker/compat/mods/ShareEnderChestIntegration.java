package red.jackf.chesttracker.compat.mods;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.EventPhases;
import red.jackf.chesttracker.api.provider.MemoryBuilder;
import red.jackf.chesttracker.api.provider.ProviderUtils;
import red.jackf.chesttracker.api.provider.def.DefaultMemoryCreator;
import red.jackf.jackfredlib.api.base.ResultHolder;

import java.util.List;

public class ShareEnderChestIntegration {
    public static final ResourceLocation MEMORY_KEY = new ResourceLocation("shareenderchest", "contents");

    public static void setup() {
        DefaultMemoryCreator.EVENT.register(EventPhases.PRIORITY_PHASE, (provider, screen) -> {
            if (screen.getTitle().getContents() instanceof LiteralContents literal && literal.text().equals("Shared Ender Chest")) {
                List<ItemStack> items = ProviderUtils.getNonPlayerStacksAsList(screen);

                return ResultHolder.value(MemoryBuilder.create(items)
                                                       .toEntry(MEMORY_KEY, BlockPos.ZERO)
                );
            }

            return ResultHolder.pass();
        });
    }
}
