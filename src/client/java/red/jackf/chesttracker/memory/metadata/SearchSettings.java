package red.jackf.chesttracker.memory.metadata;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import red.jackf.chesttracker.util.ModCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SearchSettings {
    public static final List<Integer> SEARCH_RANGES = makeSearchRanges();

    private static final Codec<Either<Integer, String>> RANGE_CODEC = Codec.either(ModCodecs.oneOf(Codec.INT, SEARCH_RANGES), ModCodecs.singular(Codec.STRING, "infinite"));

    protected static final Codec<SearchSettings> CODEC = RecordCodecBuilder.create(instance -> {
        final var def = new SearchSettings();
        return instance.group(
                RANGE_CODEC.optionalFieldOf("searchRange")
                        .forGetter(settings -> Optional.of(settings.searchRange == Integer.MAX_VALUE ? Either.right("infinite") : Either.left(settings.searchRange)))
        ).apply(instance, (searchRange) -> new SearchSettings(
                searchRange.map(either -> collapse(either.mapRight(s -> s.equals("infinite") ? Integer.MAX_VALUE : def.searchRange))).orElse(def.searchRange)
        ));
    });

    private static <T> T collapse(Either<T, T> either) {
        if (either.left().isPresent()) return either.left().get();
        return either.right().orElseThrow();
    }

    private static List<Integer> makeSearchRanges() {
        return Streams.concat(
                range(4, 16, 1),
                range(16, 32, 2),
                range(32, 64, 4),
                range(64, 128, 8),
                range(128, 256, 16),
                range(256, 512, 32),
                range(512, 1024, 64),
                Stream.of(1024, Integer.MAX_VALUE)
        ).toList();
    }

    private static Stream<Integer> range(int from, int to, int step) {
        var list = new ArrayList<Integer>();
        for (int i = from; i < to; i += step) list.add(i);
        return list.stream();
    }

    public int searchRange = 256;

    SearchSettings() {
    }

    public SearchSettings(int searchRange) {
        this.searchRange = searchRange;
    }

    public SearchSettings copy() {
        return new SearchSettings(searchRange);
    }
}
