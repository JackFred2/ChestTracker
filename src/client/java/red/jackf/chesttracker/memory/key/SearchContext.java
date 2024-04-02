package red.jackf.chesttracker.memory.key;

import net.minecraft.world.phys.Vec3;
import red.jackf.chesttracker.memory.metadata.Metadata;
import red.jackf.whereisit.api.SearchRequest;

public record SearchContext(SearchRequest request,
                            Vec3 rootPosition,
                            Metadata metadata) {
}
