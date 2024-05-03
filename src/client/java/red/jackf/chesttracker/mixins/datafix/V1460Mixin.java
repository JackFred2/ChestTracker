package red.jackf.chesttracker.mixins.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.References;
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
                () -> DSL.fields(
                        "memories",
                        DSL.compoundList(
                                DSL.constType(DSL.string()),
                                DSL.fields(
                                        "items",
                                        DSL.list(References.ITEM_STACK.in(schema))
                                )
                        ),
                        "overrides",
                        DSL.remainder()
                )
        );
    }
}
