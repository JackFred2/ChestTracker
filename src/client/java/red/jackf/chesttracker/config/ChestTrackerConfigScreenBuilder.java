package red.jackf.chesttracker.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.screen.MemoryBankManagerScreen;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.backend.Backend;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;
import red.jackf.chesttracker.util.GuiUtil;
import red.jackf.whereisit.client.WhereIsItConfigScreenBuilder;

import java.nio.file.Files;
import java.util.Locale;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class ChestTrackerConfigScreenBuilder {

    //////////
    // INIT //
    //////////
    public static Screen build(Screen parent) {
        var instance = ChestTrackerConfig.INSTANCE;

        return YetAnotherConfigLib.createBuilder()
                .title(translatable("chesttracker.title"))
                .category(makeMainCategory(instance))
                .category(makeMemoryAndStorageCategory(instance, parent))
                .save(instance::save)
                .build()
                .generateScreen(parent);
    }

    ///////////
    // UTILS //
    ///////////
    private static ResourceLocation getDescriptionImage(String basePath, boolean value) {
        return GuiUtil.sprite("textures/gui/config/%s_%s.png".formatted(basePath, value ? "enabled" : "disabled"));
    }

    private static ResourceLocation getDescriptionImage(String basePath) {
        return GuiUtil.sprite("textures/gui/config/%s.png".formatted(basePath));
    }

    private static void refreshConfigScreen(Screen parent) {
        if (Minecraft.getInstance().screen instanceof YACLScreen yacl1) {
            var currentIndex = yacl1.tabNavigationBar.currentTabIndex();
            Minecraft.getInstance().setScreen(build(parent));
            if (Minecraft.getInstance().screen instanceof YACLScreen yacl2)
                yacl2.tabNavigationBar.selectTab(currentIndex, false);
        }
    }

    private static String getDirectorySizeString() {
        long size = 0;
        if (Files.isDirectory(Constants.STORAGE_DIR))
            size = FileUtils.sizeOfDirectory(Constants.STORAGE_DIR.toFile());
        return StringUtil.magnitudeSpace(size, 2) + "B";
    }

    ////////////////
    // CATEGORIES //
    ////////////////
    private static ConfigCategory makeMainCategory(GsonConfigInstance<ChestTrackerConfig> instance) {
        return ConfigCategory.createBuilder()
                .name(translatable("chesttracker.title"))
                .group(makeMainGuiGroup(instance))
                .group(makeManagementGuisGroup(instance))
                .group(makeRenderingGroup(instance))
                .group(makeDevGuiGroup(instance))
                .build();
    }

    private static ConfigCategory makeMemoryAndStorageCategory(GsonConfigInstance<ChestTrackerConfig> instance, Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(translatable("chesttracker.config.memoryAndStorage"))
                .group(makeMemoryGroup(instance, parent))
                .group(makeStorageGroup(instance, parent));
        return builder.build();
    }

    /////////
    // GUI //
    /////////

    private static OptionGroup makeMainGuiGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.mainGui"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.mainGui.autofocusSearchBar"))
                        .description(OptionDescription.of(translatable("chesttracker.config.mainGui.autofocusSearchBar.description")))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().gui.autofocusSearchBar,
                                () -> instance.getConfig().gui.autofocusSearchBar,
                                b -> instance.getConfig().gui.autofocusSearchBar = b)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.mainGui.showAutocomplete"))
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
                        .name(translatable("chesttracker.config.mainGui.autocompleteShowsRegularNames"))
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
                        .name(translatable("chesttracker.config.mainGui.showResizeWidget"))
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
                        .name(translatable("chesttracker.config.mainGui.gridWidth"))
                        .description(OptionDescription.createBuilder()
                                .image(getDescriptionImage("grid_width"), 135, 102)
                                .build())
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(GuiConstants.MIN_GRID_COLUMNS, GuiConstants.MAX_GRID_HEIGHT)
                                .step(1)
                                .valueFormatter(i -> translatable("chesttracker.config.mainGui.gridSizeSlider", i)))
                        .binding(
                                instance.getDefaults().gui.gridWidth,
                                () -> instance.getConfig().gui.gridWidth,
                                i -> instance.getConfig().gui.gridWidth = i).
                        build())
                .option(Option.<Integer>createBuilder()
                        .name(translatable("chesttracker.config.mainGui.gridHeight"))
                        .description(OptionDescription.createBuilder()
                                .image(getDescriptionImage("grid_height"), 135, 102)
                                .build())
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(GuiConstants.MIN_GRID_ROWS, GuiConstants.MAX_GRID_HEIGHT)
                                .step(1)
                                .valueFormatter(i -> translatable("chesttracker.config.mainGui.gridSizeSlider", i)))
                        .binding(
                                instance.getDefaults().gui.gridHeight,
                                () -> instance.getConfig().gui.gridHeight,
                                i -> instance.getConfig().gui.gridHeight = i).
                        build())
                .build();
    }

    private static OptionGroup makeManagementGuisGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.managementGui"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.managementGui.hideMemoryBankIds"))
                        .description(b -> OptionDescription.createBuilder()
                                .image(getDescriptionImage("hide_memory_bank_ids", b), 143, 81)
                                .text(translatable("chesttracker.config.managementGui.hideMemoryBankIds.description"))
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().gui.hideMemoryIds,
                                () -> instance.getConfig().gui.hideMemoryIds,
                                b -> instance.getConfig().gui.hideMemoryIds = b)
                        .build())
                .build();
    }

    private static OptionGroup makeRenderingGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.rendering"))
                .option(Option.<Integer>createBuilder()
                        .name(translatable("chesttracker.config.rendering.nameRenderRange"))
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(4, 24)
                                .step(1)
                                .valueFormatter(i -> translatable("chesttracker.generic.blocks", i)))
                        .binding(
                                instance.getDefaults().rendering.nameRange,
                                () -> instance.getConfig().rendering.nameRange,
                                i -> instance.getConfig().rendering.nameRange = i
                        ).build())
                .option(ButtonOption.createBuilder()
                        .name(translatable("chesttracker.config.whereisit"))
                        .description(OptionDescription.of(translatable("chesttracker.config.whereisit.description")))
                        .text(translatable("chesttracker.gui.open"))
                        .action((yaclScreen, button) -> Minecraft.getInstance().setScreen(WhereIsItConfigScreenBuilder.build(yaclScreen))).build())
                .build();
    }

    private static OptionGroup makeDevGuiGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.devGui"))
                .collapsed(true)
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.devGui.showDevHud"))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().gui.showDevHud,
                                () -> instance.getConfig().gui.showDevHud,
                                b -> instance.getConfig().gui.showDevHud = b
                        ).build())
                .build();
    }

    ////////////
    // STORAGE //
    ////////////
    private static OptionGroup makeMemoryGroup(GsonConfigInstance<ChestTrackerConfig> instance, Screen parent) {
        var builder = OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.memory"))
                .option(ButtonOption.createBuilder()
                        .name(translatable("chesttracker.gui.memoryManager"))
                        .text(translatable("chesttracker.config.open"))
                        .action(((screen, option) -> {
                            Runnable lambda = () -> {
                                Minecraft.getInstance().setScreen(screen);
                                refreshConfigScreen(parent);
                            };
                            Minecraft.getInstance().setScreen(new MemoryBankManagerScreen(lambda, lambda));
                        }))
                        .build());

        if (MemoryBank.INSTANCE == null)
            builder.option(LabelOption.create(translatable("chesttracker.config.memory.noMemoryBankLoaded")));

        return builder.build();
    }

    private static OptionGroup makeStorageGroup(GsonConfigInstance<ChestTrackerConfig> instance, Screen parent) {
        var rootBuilder = OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.storage"))
                .option(ButtonOption.createBuilder()
                        .name(translatable("chesttracker.config.storage.openFolder"))
                        .action((screen, button) -> Util.getPlatform().openUri(Constants.STORAGE_DIR.toUri()))
                        .text(literal(getDirectorySizeString()))
                        .build())
                .option(Option.<Backend.Type>createBuilder()
                        .name(translatable("chesttracker.config.storage.storageBackend"))
                        .description(b -> {
                            var builder = OptionDescription.createBuilder()
                                    .text(translatable("chesttracker.config.storage.storageBackend.description"))
                                    .text(CommonComponents.NEW_LINE)
                                    .text(literal(b.name() + ": ").withStyle(ChatFormatting.GOLD)
                                            .append(translatable("chesttracker.config.storage.storageBackend.description." + b.name()
                                                    .toLowerCase(Locale.ROOT))
                                                    .withStyle(ChatFormatting.WHITE)));
                            if (b == Backend.Type.MEMORY)
                                builder.text(CommonComponents.NEW_LINE)
                                        .text(translatable("chesttracker.config.storage.storageBackend.description.memoryLossOnReboot").withStyle(ChatFormatting.RED));
                            return builder.build();
                        })
                        .controller(opt -> EnumControllerBuilder.create(opt)
                                .enumClass(Backend.Type.class))
                        .binding(
                                instance.getDefaults().storage.storageBackend,
                                () -> instance.getConfig().storage.storageBackend,
                                e -> {
                                    instance.getConfig().storage.storageBackend = e;
                                    e.load();
                                    refreshConfigScreen(parent);
                                })
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.storage.json.readableJsonMemories"))
                        .description(b -> OptionDescription.createBuilder()
                                .text(translatable("chesttracker.config.storage.json.readableJsonMemories.description"))
                                .image(getDescriptionImage("readable_json_memories", b), 468, 244)
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().storage.readableJsonMemories,
                                () -> instance.getConfig().storage.readableJsonMemories,
                                b -> {
                                    instance.getConfig().storage.readableJsonMemories = b;
                                    MemoryBank.save();
                                    refreshConfigScreen(parent);
                                })
                        .build());

        return rootBuilder.build();
    }
}
