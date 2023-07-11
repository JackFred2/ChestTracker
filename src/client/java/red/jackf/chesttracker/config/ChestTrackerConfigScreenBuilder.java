package red.jackf.chesttracker.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.minecraft.client.gui.screens.Screen;
import red.jackf.whereisit.client.WhereIsItConfigScreenBuilder;

import static net.minecraft.network.chat.Component.translatable;

public class ChestTrackerConfigScreenBuilder {

    public static Screen build(Screen parent) {
        var instance = ChestTrackerConfig.INSTANCE;

        return YetAnotherConfigLib.createBuilder()
                .title(translatable("chesttracker.title"))
                .category(makeMainCategory(instance))
                .category(makeWhereIsItLink())
                .save(instance::save)
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory makeMainCategory(GsonConfigInstance<ChestTrackerConfig> instance) {
        return ConfigCategory.createBuilder()
                .name(translatable("chesttracker.title"))
                .group(makeSearchGroup(instance))
                .build();
    }

    private static OptionGroup makeSearchGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.search"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.search.autocompleteShowsRegularNames"))
                        .description(b -> OptionDescription.createBuilder()
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().autocompleteShowsRegularNames,
                                () -> instance.getConfig().autocompleteShowsRegularNames,
                                b -> instance.getConfig().autocompleteShowsRegularNames = b)
                        .build())
                .build();
    }

    private static ConfigCategory makeWhereIsItLink() {
        return PlaceholderCategory.createBuilder()
                .name(translatable("whereisit.config.title"))
                .tooltip(translatable("chesttracker.config.whereisit.tooltip"))
                .screen((mc, parent) -> WhereIsItConfigScreenBuilder.build(parent))
                .build();
    }
}
