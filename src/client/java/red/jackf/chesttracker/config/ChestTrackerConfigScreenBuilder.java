package red.jackf.chesttracker.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.minecraft.client.gui.screens.Screen;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.util.Constants;
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
                .group(makeGuiGroup(instance))
                .build();
    }

    private static OptionGroup makeGuiGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.gui"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.gui.autocompleteShowsRegularNames"))
                        .description(b -> OptionDescription.createBuilder()
                                .image(ChestTracker.id("textures/gui/config/show_unnamed_in_autocomplete_%s.png".formatted(b ? "enabled" : "disabled")), 118, 85)
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().gui.autocompleteShowsRegularNames,
                                () -> instance.getConfig().gui.autocompleteShowsRegularNames,
                                b -> instance.getConfig().gui.autocompleteShowsRegularNames = b)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.gui.showResizeWidget"))
                        .description(b -> OptionDescription.createBuilder()
                                .image(ChestTracker.id("textures/gui/config/show_resize_%s.png".formatted(b ? "enabled" : "disabled")), 52, 52)
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().gui.showResizeWidget,
                                () -> instance.getConfig().gui.showResizeWidget,
                                b -> instance.getConfig().gui.showResizeWidget = b)
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(translatable("chesttracker.config.gui.gridWidth"))
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(Constants.MIN_GRID_WIDTH, Constants.MAX_GRID_HEIGHT)
                                .step(1)
                                .valueFormatter(i -> translatable("chesttracker.config.gui.gridSizeSlider", i)))
                        .binding(
                                instance.getDefaults().gui.gridWidth,
                                () -> instance.getConfig().gui.gridWidth,
                                i -> instance.getConfig().gui.gridWidth = i).
                        build())
                .option(Option.<Integer>createBuilder()
                        .name(translatable("chesttracker.config.gui.gridHeight"))
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(Constants.MIN_GRID_HEIGHT, Constants.MAX_GRID_HEIGHT)
                                .step(1)
                                .valueFormatter(i -> translatable("chesttracker.config.gui.gridSizeSlider", i)))
                        .binding(
                                instance.getDefaults().gui.gridHeight,
                                () -> instance.getConfig().gui.gridHeight,
                                i -> instance.getConfig().gui.gridHeight = i).
                        build())
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
