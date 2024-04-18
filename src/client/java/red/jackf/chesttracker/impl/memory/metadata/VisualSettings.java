package red.jackf.chesttracker.impl.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.providers.MemoryKeyIcon;
import red.jackf.chesttracker.api.providers.ProviderUtils;
import red.jackf.chesttracker.api.providers.ServerProvider;
import red.jackf.chesttracker.impl.gui.GuiConstants;
import red.jackf.chesttracker.impl.util.Misc;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VisualSettings {
    public static Codec<VisualSettings> CODEC = RecordCodecBuilder.create(instance -> {
        final var def = new VisualSettings();
        return instance.group(
                JFLCodecs.mutableList(MemoryKeyIcon.CODEC.listOf()).optionalFieldOf("icons")
                    .forGetter(meta -> Optional.of(meta.icons)),
                Codec.BOOL.optionalFieldOf("useDefaultIconOrder")
                    .forGetter(visualSettings -> Optional.of(visualSettings.useDefaultIconOrder))
        ).apply(instance, (icons, useDefaultIconOrder) -> new VisualSettings(
                icons.orElse(def.icons),
                useDefaultIconOrder.orElse(def.useDefaultIconOrder)
        ));
    });

    private List<MemoryKeyIcon> icons = new ArrayList<>();

    public boolean useDefaultIconOrder = true;


    public List<ResourceLocation> getKeyOrder() {
        return icons.stream().map(MemoryKeyIcon::id).toList();
    }

    public void moveIcon(int from, int to) {
        icons.add(to, icons.remove(from));
        this.useDefaultIconOrder = false;
    }

    public ItemStack getOrCreateIcon(ResourceLocation key) {
        for (MemoryKeyIcon icon : icons)
            if (icon.id().equals(key)) return icon.icon();

        MemoryKeyIcon newIcon = ProviderUtils.getCurrentProvider().flatMap(provider ->
            provider.getMemoryKeyIcons().stream()
                    .filter(icon -> icon.id().equals(key))
                    .findFirst()
        ).orElseGet(() -> new MemoryKeyIcon(key, GuiConstants.UNKNOWN_ICON));

        icons.add(newIcon);
        reorderIfNecessary();
        return newIcon.icon();
    }

    public void setIcon(ResourceLocation key, ItemStack icon) {
        var existingIndex = IntStream.range(0, icons.size())
                .filter(index -> icons.get(index).id().equals(key))
                .findFirst();
        var keyIcon = new MemoryKeyIcon(key, icon);
        if (existingIndex.isPresent()) {
            icons.set(existingIndex.getAsInt(), keyIcon);
        } else {
            icons.add(keyIcon);
        }
        reorderIfNecessary();
    }

    public void removeIcon(ResourceLocation key) {
        var iter = icons.iterator();
        while (iter.hasNext()) {
            if (iter.next().id().equals(key)) {
                iter.remove();
                return;
            }
        }
    }

    public void reorderIfNecessary() {
        if (!this.useDefaultIconOrder) return;

        ServerProvider provider = ProviderUtils.getCurrentProvider().orElse(null);
        if (provider == null) return;;

        var iconKeys = provider.getMemoryKeyIcons().stream().map(MemoryKeyIcon::id).toList();

        this.icons = this.icons.stream()
                .sorted(Comparator.comparing(MemoryKeyIcon::id, Misc.bringToFront(iconKeys)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    VisualSettings() {}

    public VisualSettings(List<MemoryKeyIcon> icons, boolean useDefaultIconOrder) {
        this.icons = icons;
        this.useDefaultIconOrder = useDefaultIconOrder;
    }

    public VisualSettings copy() {
        return new VisualSettings(new ArrayList<>(this.icons), this.useDefaultIconOrder);
    }
}
