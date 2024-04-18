package red.jackf.chesttracker.impl.compat.mods;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.item.Items;
import red.jackf.chesttracker.api.memory.CommonKeys;
import red.jackf.chesttracker.api.providers.MemoryBuilder;
import red.jackf.chesttracker.api.providers.MemoryKeyIcon;
import red.jackf.chesttracker.api.providers.defaults.DefaultIcons;
import red.jackf.chesttracker.api.providers.defaults.DefaultProviderScreenClose;
import red.jackf.jackfredlib.api.base.ResultHolder;

public class ShareEnderChestIntegration {
    public static void setup() {
        DefaultIcons.registerIconBelow(CommonKeys.ENDER_CHEST_KEY, new MemoryKeyIcon(CommonKeys.SHARE_ENDER_CHEST, Items.ENDER_EYE.getDefaultInstance()));

        DefaultProviderScreenClose.EVENT.register((provider, context) -> {
            if (context.getScreen().getTitle().getContents() instanceof PlainTextContents.LiteralContents literal
                && literal.text().equals("Shared Ender Chest")) {
                var items = context.getItems();

                return ResultHolder.value(MemoryBuilder.create(items).toResult(CommonKeys.SHARE_ENDER_CHEST, BlockPos.ZERO));
            } else {
                return ResultHolder.pass();
            }
        });
    }
}
