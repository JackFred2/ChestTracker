package red.jackf.chesttracker.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.apache.commons.io.FileUtils;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.custom.MemoryIconController;
import red.jackf.chesttracker.gui.MemoryIcon;
import red.jackf.chesttracker.memory.ItemMemory;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;
import red.jackf.whereisit.client.WhereIsItConfigScreenBuilder;

import java.nio.file.Files;
import java.util.Locale;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class ChestTrackerConfigScreenBuilder {

    public static Screen build(Screen parent) {
        var instance = ChestTrackerConfig.INSTANCE;

        return YetAnotherConfigLib.createBuilder()
                .title(translatable("chesttracker.title"))
                .category(makeMainCategory(instance))
                .category(makeMemoryCategory(instance))
                .category(makeWhereIsItLink())
                .save(instance::save)
                .build()
                .generateScreen(parent);
    }

    private static ResourceLocation getDescriptionImage(String basePath, boolean value) {
        return ChestTracker.id("textures/gui/config/%s_%s.png".formatted(basePath, value ? "enabled" : "disabled"));
    }

    private static ConfigCategory makeMainCategory(GsonConfigInstance<ChestTrackerConfig> instance) {
        return ConfigCategory.createBuilder()
                .name(translatable("chesttracker.title"))
                .group(makeGuiGroup(instance))
                .group(makeMemoryIconGroup(instance))
                .build();
    }

    private static ConfigCategory makeMemoryCategory(GsonConfigInstance<ChestTrackerConfig> instance) {
        return ConfigCategory.createBuilder()
                .name(translatable("chesttracker.config.memory"))
                .option(ButtonOption.createBuilder()
                        .name(translatable("chesttracker.config.memory.openFolder"))
                        .action((screen, button) -> Util.getPlatform().openUri(Constants.STORAGE_DIR.toUri()))
                        .text(literal(getDirectorySizeString()))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.memory.readableMemories"))
                        .description(b -> OptionDescription.createBuilder()
                                .text(translatable("chesttracker.config.memory.readableMemories.description"))
                                .image(getDescriptionImage("readable_memories", b), 468, 244)
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().memory.readableMemories,
                                () -> instance.getConfig().memory.readableMemories,
                                b -> {
                                    instance.getConfig().memory.readableMemories = b;
                                    ItemMemory.save();
                                })
                        .build())
                .option(Option.<Storage.Backend>createBuilder()
                        .name(translatable("chesttracker.config.memory.storageBackend"))
                        .description(b -> {
                            var builder = OptionDescription.createBuilder()
                                    .text(translatable("chesttracker.config.memory.storageBackend.description"))
                                    .text(CommonComponents.NEW_LINE)
                                    .text(literal(b.name() + ": ").withStyle(ChatFormatting.GOLD)
                                            .append(translatable("chesttracker.config.memory.storageBackend.description." + b.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.WHITE)));
                                    if (b == Storage.Backend.MEMORY)
                                        builder.text(CommonComponents.NEW_LINE)
                                                .text(translatable("chesttracker.config.memory.storageBackend.description.memoryLossOnReboot").withStyle(ChatFormatting.RED));
                                    return builder.build();
                        })
                        .controller(opt -> EnumControllerBuilder.create(opt)
                                .enumClass(Storage.Backend.class))
                        .binding(
                                instance.getDefaults().memory.storageBackend,
                                () -> instance.getConfig().memory.storageBackend,
                                e -> {
                                    instance.getConfig().memory.storageBackend = e;
                                    e.load();
                                })
                        .build())
                .build();
    }

    private static String getDirectorySizeString() {
        long size = 0;
        if (Files.isDirectory(Constants.STORAGE_DIR))
            size = FileUtils.sizeOfDirectory(Constants.STORAGE_DIR.toFile());
        return StringUtil.magnitudeSpace(size, 2) + "B";
    }

    private static OptionGroup makeGuiGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.gui"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.gui.autofocusSearchBar"))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().gui.autofocusSearchBar,
                                () -> instance.getConfig().gui.autofocusSearchBar,
                                b -> instance.getConfig().gui.autofocusSearchBar = b)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.gui.showAutocomplete"))
                        .description(b -> OptionDescription.createBuilder()
                                .image(getDescriptionImage("show_autocomplete", b), 85, 59)
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().gui.showAutocomplete,
                                () -> instance.getConfig().gui.showAutocomplete,
                                b -> instance.getConfig().gui.showAutocomplete = b)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.gui.autocompleteShowsRegularNames"))
                        .description(b -> OptionDescription.createBuilder()
                                .image(getDescriptionImage("show_unnamed_in_autocomplete", b), 118, 85)
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
                                .image(getDescriptionImage("show_resize", b), 52, 52)
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

    private static OptionGroup makeMemoryIconGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        //don't close the level
        //noinspection resource
        return ListOption.<MemoryIcon>createBuilder()
            .name(translatable("chesttracker.config.gui.memoryIcons"))
            .controller(MemoryIconController.Builder::new)
            .binding(
                    instance.getDefaults().gui.memoryIcons,
                    () -> instance.getConfig().gui.memoryIcons,
                    l -> instance.getConfig().gui.memoryIcons = l
            )
            .initial(new MemoryIcon(Minecraft.getInstance().player != null ?
                    Minecraft.getInstance().player.level().dimension().location() :
                    new ResourceLocation("custom_dimension"), new LightweightStack(Items.CRAFTING_TABLE)))
            //.collapsed(true)
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
