package red.jackf.chesttracker.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.MemoryKeyIcon;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.util.ModCodecs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Metadata {
    public static final Codec<Metadata> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("name").forGetter(meta -> Optional.ofNullable(meta.name)),
                    ModCodecs.INSTANT.optionalFieldOf("lastModified").forGetter(meta -> Optional.of(meta.lastModified)),
                    Codec.LONG.fieldOf("loadedTime").forGetter(meta -> meta.loadedTime),
                    ModCodecs.makeMutableList(MemoryKeyIcon.CODEC.listOf()).optionalFieldOf("icons")
                            .forGetter(meta -> Optional.of(meta.icons)),
                    FilteringSettings.CODEC.optionalFieldOf("filtering")
                            .forGetter(meta -> Optional.of(meta.filteringSettings)),
                    IntegritySettings.CODEC.optionalFieldOf("integrity")
                            .forGetter(meta -> Optional.of(meta.integritySettings)),
                    SearchSettings.CODEC.optionalFieldOf("search")
                            .forGetter(meta -> Optional.of(meta.searchSettings))
            ).apply(instance, (name, lastModified, loadedTime, icons, filtering, integrity, search) -> new Metadata(
                    name.orElse(null),
                    lastModified.orElse(Instant.now()),
                    loadedTime,
                    icons.orElseGet(ArrayList::new),
                    filtering.orElseGet(FilteringSettings::new),
                    integrity.orElseGet(IntegritySettings::new),
                    search.orElseGet(SearchSettings::new)
            ))
    );

    @Nullable
    private String name;
    private Instant lastModified;
    private long loadedTime;
    private final List<MemoryKeyIcon> icons;
    private final FilteringSettings filteringSettings;
    private final IntegritySettings integritySettings;
    private final SearchSettings searchSettings;

    public Metadata(
            @Nullable String name,
            Instant lastModified,
            long loadedTime,
            List<MemoryKeyIcon> icons,
            FilteringSettings filteringSettings,
            IntegritySettings integritySettings,
            SearchSettings searchSettings) {
        this.name = name;
        this.lastModified = lastModified;
        this.icons = icons;
        this.loadedTime = loadedTime;
        this.filteringSettings = filteringSettings;
        this.integritySettings = integritySettings;
        this.searchSettings = searchSettings;
    }

    public static Metadata blank() {
        return new Metadata(null, Instant.now(), 0L, new ArrayList<>(), new FilteringSettings(), new IntegritySettings(), new SearchSettings());
    }

    public static Metadata blankWithName(String name) {
        var blank = blank();
        blank.setName(name);
        return blank;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void updateModified() {
        this.lastModified = Instant.now();
    }

    public List<ResourceLocation> getKeyOrder() {
        return icons.stream().map(MemoryKeyIcon::id).toList();
    }

    public void moveIcon(int from, int to) {
        icons.add(to, icons.remove(from));
    }

    public LightweightStack getOrCreateIcon(ResourceLocation key) {
        for (MemoryKeyIcon icon : icons) {
            if (icon.id().equals(key)) return icon.icon();
        }
        // doesn't exist, populate
        var newIcon = new MemoryKeyIcon(key, GuiConstants.DEFAULT_ICONS.getOrDefault(key, GuiConstants.DEFAULT_ICON));
        icons.add(newIcon);
        return newIcon.icon();
    }

    public void setIcon(ResourceLocation key, LightweightStack icon) {
        var existingIndex = IntStream.range(0, icons.size())
                .filter(index -> icons.get(index).id().equals(key))
                .findFirst();
        var keyIcon = new MemoryKeyIcon(key, icon);
        if (existingIndex.isPresent()) {
            icons.set(existingIndex.getAsInt(), keyIcon);
        } else {
            icons.add(keyIcon);
        }
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

    public long getLoadedTime() {
        return loadedTime;
    }

    public FilteringSettings getFilteringSettings() {
        return filteringSettings;
    }

    public IntegritySettings getIntegritySettings() {
        return integritySettings;
    }

    public SearchSettings getSearchSettings() {
        return searchSettings;
    }

    public Metadata deepCopy() {
        return new Metadata(name, lastModified, loadedTime, new ArrayList<>(icons), filteringSettings.copy(), integritySettings.copy(), searchSettings.copy());
    }

    public void incrementLoadedTime() {
        this.loadedTime++;
    }
}
