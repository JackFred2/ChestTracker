package red.jackf.chesttracker.mixins.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.schemas.V1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.chesttracker.impl.datafix.Types;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(V1460.class)
public class V1460Mixin {

    @Inject(method = "registerTypes", at = @At("TAIL"))
    private void addChestTrackerReferences(Schema schema,
                                           Map<String, Supplier<TypeTemplate>> entityChoices,
                                           Map<String, Supplier<TypeTemplate>> blockEntityChoices,
                                           CallbackInfo ci) {
        schema.registerType(false,
                Types.MEMORY_DATA,
                () -> Types.getMemoryDataType(schema)
        );
        schema.registerType(false,
                Types.MEMORY_DATA_2_3_3,
                () -> Types.get2_3_3MemoryDataType(schema)
        );
    }
}
