package red.jackf.chesttracker.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.chesttracker.api.gui.MemoryKeyIcon;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.provider.ProviderHandler;
import red.jackf.chesttracker.util.ModCodecs;
import red.jackf.chesttracker.util.StreamUtil;

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
                ModCodecs.makeMutableList(MemoryKeyIcon.CODEC.listOf()).optionalFieldOf("icons")
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

        // doesn't exist, populate
        var newIcon = ProviderHandler.INSTANCE != null ? ProviderHandler.INSTANCE.getDefaultIcons().stream()
                        .filter(icon -> icon.id().equals(key))
                        .findFirst()
                        .orElse(null) : null;
        if (newIcon == null) newIcon = new MemoryKeyIcon(key, GuiConstants.UNKNOWN_ICON);
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
        if (!this.useDefaultIconOrder || ProviderHandler.INSTANCE == null) return;

        var iconKeys = ProviderHandler.INSTANCE.getDefaultIcons().stream().map(MemoryKeyIcon::id).toList();

        this.icons = this.icons.stream()
                .sorted(Comparator.comparing(MemoryKeyIcon::id, StreamUtil.bringToFront(iconKeys)))
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
