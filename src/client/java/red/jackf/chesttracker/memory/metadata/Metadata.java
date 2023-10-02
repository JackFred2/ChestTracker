package red.jackf.chesttracker.memory.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;

public class Metadata {
    public static final Codec<Metadata> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("name").forGetter(meta -> Optional.ofNullable(meta.name)),
                    ExtraCodecs.INSTANT_ISO8601.optionalFieldOf("lastModified").forGetter(meta -> Optional.of(meta.lastModified)),
                    Codec.LONG.fieldOf("loadedTime").forGetter(meta -> meta.loadedTime),
                    FilteringSettings.CODEC.optionalFieldOf("filtering")
                            .forGetter(meta -> Optional.of(meta.filteringSettings)),
                    IntegritySettings.CODEC.optionalFieldOf("integrity")
                            .forGetter(meta -> Optional.of(meta.integritySettings)),
                    SearchSettings.CODEC.optionalFieldOf("search")
                            .forGetter(meta -> Optional.of(meta.searchSettings)),
                    VisualSettings.CODEC.optionalFieldOf("visual")
                            .forGetter(meta -> Optional.of(meta.visualSettings))
            ).apply(instance, (name, lastModified, loadedTime, filtering, integrity, search, visual) -> new Metadata(
                    name.orElse(null),
                    lastModified.orElse(Instant.now()),
                    loadedTime,
                    filtering.orElseGet(FilteringSettings::new),
                    integrity.orElseGet(IntegritySettings::new),
                    search.orElseGet(SearchSettings::new),
                    visual.orElseGet(VisualSettings::new)
            ))
    );

    @Nullable
    private String name;
    private Instant lastModified;
    private long loadedTime;
    private final FilteringSettings filteringSettings;
    private final IntegritySettings integritySettings;
    private final SearchSettings searchSettings;
    private final VisualSettings visualSettings;

    public Metadata(
            @Nullable String name,
            Instant lastModified,
            long loadedTime,
            FilteringSettings filteringSettings,
            IntegritySettings integritySettings,
            SearchSettings searchSettings,
            VisualSettings visualSettings) {
        this.name = name;
        this.lastModified = lastModified;
        this.loadedTime = loadedTime;
        this.filteringSettings = filteringSettings;
        this.integritySettings = integritySettings;
        this.searchSettings = searchSettings;
        this.visualSettings = visualSettings;
    }

    public static Metadata blank() {
        return new Metadata(null, Instant.now(), 0L, new FilteringSettings(), new IntegritySettings(), new SearchSettings(), new VisualSettings());
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

    public VisualSettings getVisualSettings() {
        return visualSettings;
    }

    public Metadata deepCopy() {
        return new Metadata(name, lastModified, loadedTime, filteringSettings.copy(), integritySettings.copy(), searchSettings.copy(), visualSettings.copy());
    }

    public void incrementLoadedTime() {
        this.loadedTime++;
    }
}
