package red.jackf.chesttracker.memory.metadata;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.util.ModCodecs;
import red.jackf.jackfredlib.api.base.codecs.JFLCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchSettings {
    public static final List<Integer> SEARCH_RANGES = makeSearchRanges(true);
    public static final List<Integer> SEARCH_RANGES_NO_INFINITE = makeSearchRanges(false);

    private static final Codec<Either<Integer, String>> RANGE_CODEC = Codec.either(JFLCodecs.oneOf(Codec.INT, SEARCH_RANGES), ModCodecs.singular(Codec.STRING, "infinite"));

    protected static final Codec<SearchSettings> CODEC = RecordCodecBuilder.create(instance -> {
        final var def = new SearchSettings();
        return instance.group(
                RANGE_CODEC.optionalFieldOf("itemListRange")
                           .forGetter(settings -> Optional.of(settings.itemListRange == Integer.MAX_VALUE ? Either.right("infinite") : Either.left(settings.itemListRange))),
                RANGE_CODEC.optionalFieldOf("searchRange")
                           .forGetter(settings -> Optional.of(settings.searchRange == Integer.MAX_VALUE ? Either.right("infinite") : Either.left(settings.searchRange))),
                JFLCodecs.forEnum(MemoryBank.StackMergeMode.class).optionalFieldOf("stackMergeMode")
                        .forGetter(settings -> Optional.of(settings.stackMergeMode))
        ).apply(instance, (itemListRange, searchRange, stackMergeMode) -> new SearchSettings(
                itemListRange.map(either -> collapse(either.mapRight(s -> s.equals("infinite") ? Integer.MAX_VALUE : def.itemListRange))).orElse(def.itemListRange),
                searchRange.map(either -> collapse(either.mapRight(s -> s.equals("infinite") ? Integer.MAX_VALUE : def.searchRange))).orElse(def.searchRange),
                stackMergeMode.orElse(def.stackMergeMode)
        ));
    });

    private static <T> T collapse(Either<T, T> either) {
        if (either.left().isPresent()) return either.left().get();
        return either.right().orElseThrow();
    }

    private static List<Integer> makeSearchRanges(boolean withInfinite) {
        var list = Streams.concat(
                range(4, 16, 1),
                range(16, 32, 2),
                range(32, 64, 4),
                range(64, 128, 8),
                range(128, 256, 16),
                range(256, 512, 32),
                range(512, 1024, 64),
                Stream.of(1024)
        ).collect(Collectors.toList());
        if (withInfinite) list.add(Integer.MAX_VALUE);
        return list;
    }

    private static Stream<Integer> range(int from, int to, int step) {
        var list = new ArrayList<Integer>();
        for (int i = from; i < to; i += step) list.add(i);
        return list.stream();
    }

    public int itemListRange = 256;
    public int searchRange = 256;
    public MemoryBank.StackMergeMode stackMergeMode = MemoryBank.StackMergeMode.ALL;

    SearchSettings() {
    }

    public SearchSettings(int itemListRange, int searchRange, MemoryBank.StackMergeMode stackMergeMode) {
        this.itemListRange = itemListRange;
        this.searchRange = searchRange;
        this.stackMergeMode = stackMergeMode;
    }

    public SearchSettings copy() {
        return new SearchSettings(itemListRange, searchRange, stackMergeMode);
    }
}
