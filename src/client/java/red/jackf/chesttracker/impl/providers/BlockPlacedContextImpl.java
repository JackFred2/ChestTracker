package red.jackf.chesttracker.impl.providers;

import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.chesttracker.api.providers.BlockPlacedContext;

public record BlockPlacedContextImpl(ClientBlockSource cbs, ItemStack placementStack) implements BlockPlacedContext {
    @Override
    public ClientBlockSource getBlockSource() {
        return this.cbs;
    }

    @Override
    public ItemStack getPlacementStack() {
        return this.placementStack;
    }
}
