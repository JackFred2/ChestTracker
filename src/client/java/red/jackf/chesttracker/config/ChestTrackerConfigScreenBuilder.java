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
import net.minecraft.world.item.Items;
import org.apache.commons.io.FileUtils;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.config.custom.HoldToConfirmButtonOption;
import red.jackf.chesttracker.config.custom.MemoryKeyIconController;
import red.jackf.chesttracker.gui.MemoryBankManagerScreen;
import red.jackf.chesttracker.gui.MemoryKeyIcon;
import red.jackf.chesttracker.memory.LightweightStack;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.Storage;
import red.jackf.chesttracker.storage.StorageUtil;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.StringUtil;
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
                .category(makeMemoryCategory(instance, parent))
                .category(makeWhereIsItLink())
                .save(instance::save)
                .build()
                .generateScreen(parent);
    }

    ///////////
    // UTILS //
    ///////////
    private static ResourceLocation getDescriptionImage(String basePath, boolean value) {
        return ChestTracker.guiTex("config/%s_%s".formatted(basePath, value ? "enabled" : "disabled"));
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
                .group(makeMemoryKeyIconGroup(instance))
                .group(makeManagementGuiGroup(instance))
                .build();
    }

    private static ConfigCategory makeMemoryCategory(GsonConfigInstance<ChestTrackerConfig> instance, Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(translatable("chesttracker.config.memory"))
                .group(makeGlobalMemoryGroup(instance, parent));
        if (MemoryBank.INSTANCE != null) builder.group(makeLocalMemoryGroup(MemoryBank.INSTANCE, parent));
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
                                .image(ChestTracker.guiTex("config/grid_width"), 135, 102)
                                .build())
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(Constants.MIN_GRID_WIDTH, Constants.MAX_GRID_HEIGHT)
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
                                .image(ChestTracker.guiTex("config/grid_height"), 135, 102)
                                .build())
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(Constants.MIN_GRID_HEIGHT, Constants.MAX_GRID_HEIGHT)
                                .step(1)
                                .valueFormatter(i -> translatable("chesttracker.config.mainGui.gridSizeSlider", i)))
                        .binding(
                                instance.getDefaults().gui.gridHeight,
                                () -> instance.getConfig().gui.gridHeight,
                                i -> instance.getConfig().gui.gridHeight = i).
                        build())
                .build();
    }

    private static OptionGroup makeMemoryKeyIconGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        //don't close the level
        //noinspection resource
        return ListOption.<MemoryKeyIcon>createBuilder()
                .name(translatable("chesttracker.config.mainGui.memoryKeyIcons"))
                .description(OptionDescription.createBuilder()
                        .image(ChestTracker.guiTex("config/memory_key_icon_list"), 624, 285)
                        .text(translatable("chesttracker.config.mainGui.memoryKeyIcons.description"))
                        .build())
                .controller(MemoryKeyIconController.Builder::new)
                .binding(
                        instance.getDefaults().gui.memoryKeyIcons,
                        () -> instance.getConfig().gui.memoryKeyIcons,
                        l -> instance.getConfig().gui.memoryKeyIcons = l
                )
                .initial(new MemoryKeyIcon(Minecraft.getInstance().player != null ?
                        Minecraft.getInstance().player.level().dimension().location() :
                        new ResourceLocation("custom_dimension"), new LightweightStack(Items.CRAFTING_TABLE)))
                .build();
    }

    private static OptionGroup makeManagementGuiGroup(GsonConfigInstance<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.managementGui"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.managementGui.hideMemoryBankIds"))
                        .description(b -> OptionDescription.createBuilder()
                                .image(getDescriptionImage("hide_memory_bank_ids", b), 143, 81)
                                .text(translatable("chesttracker.config.managementGui.hideMemoryBankIds.description1"))
                                .text(CommonComponents.EMPTY)
                                .text(translatable("chesttracker.config.managementGui.hideMemoryBankIds.description2"))
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

    ////////////
    // MEMORY //
    ////////////
    private static OptionGroup makeGlobalMemoryGroup(GsonConfigInstance<ChestTrackerConfig> instance, Screen parent) {
        var rootBuilder = OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.memory.global"))
                .option(ButtonOption.createBuilder()
                        .name(translatable("chesttracker.config.memory.global.openFolder"))
                        .action((screen, button) -> Util.getPlatform().openUri(Constants.STORAGE_DIR.toUri()))
                        .text(literal(getDirectorySizeString()))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.memory.global.autoLoadMemories"))
                        .description(OptionDescription.createBuilder()
                                .text(translatable("chesttracker.config.memory.global.autoLoadMemories.description"))
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().memory.autoLoadMemories,
                                () -> instance.getConfig().memory.autoLoadMemories,
                                b -> instance.getConfig().memory.autoLoadMemories = b)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.memory.global.readableJsonMemories"))
                        .description(b -> OptionDescription.createBuilder()
                                .text(translatable("chesttracker.config.memory.global.readableJsonMemories.description"))
                                .image(getDescriptionImage("readable_json_memories", b), 468, 244)
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.getDefaults().memory.readableJsonMemories,
                                () -> instance.getConfig().memory.readableJsonMemories,
                                b -> {
                                    instance.getConfig().memory.readableJsonMemories = b;
                                    MemoryBank.save();
                                    refreshConfigScreen(parent);
                                })
                        .build())
                .option(Option.<Storage.Backend>createBuilder()
                        .name(translatable("chesttracker.config.memory.global.storageBackend"))
                        .description(b -> {
                            var builder = OptionDescription.createBuilder()
                                    .text(translatable("chesttracker.config.memory.global.storageBackend.description"))
                                    .text(CommonComponents.NEW_LINE)
                                    .text(literal(b.name() + ": ").withStyle(ChatFormatting.GOLD)
                                            .append(translatable("chesttracker.config.memory.global.storageBackend.description." + b.name().toLowerCase(Locale.ROOT))
                                                    .withStyle(ChatFormatting.WHITE)));
                            if (b == Storage.Backend.MEMORY)
                                builder.text(CommonComponents.NEW_LINE)
                                        .text(translatable("chesttracker.config.memory.global.storageBackend.description.memoryLossOnReboot").withStyle(ChatFormatting.RED));
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
                                    refreshConfigScreen(parent);
                                })
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(translatable("chesttracker.gui.memoryManager.title"))
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
            rootBuilder.option(LabelOption.create(translatable("chesttracker.config.memory.global.noMemoryBankLoaded")));

        return rootBuilder.build();
    }

    private static OptionGroup makeLocalMemoryGroup(MemoryBank memory, Screen parent) {
        var builder = OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.memory.local.title", memory.getDisplayName()))
                .option(new HoldToConfirmButtonOption(translatable("chesttracker.config.memory.local.delete"),
                        OptionDescription.createBuilder()
                                .text(translatable("selectServer.deleteButton"))
                                .text(CommonComponents.NEW_LINE)
                                .text(translatable("chesttracker.config.memory.irreversable").withStyle(ChatFormatting.RED))
                                .build(),
                        (screen, button) -> {
                            MemoryBank.unload();
                            StorageUtil.getStorage().delete(memory.getId());
                            refreshConfigScreen(parent);
                        },
                        null,
                        true,
                        60));
        memory.getKeys()
                .forEach(resloc -> builder.option(new HoldToConfirmButtonOption(translatable("chesttracker.config.memory.local.deleteKey", resloc),
                        OptionDescription.createBuilder()
                                .text(translatable("chesttracker.config.memory.local.deleteKey.description", resloc))
                                .text(CommonComponents.NEW_LINE)
                                .text(translatable("chesttracker.config.memory.irreversable").withStyle(ChatFormatting.RED))
                                .build(),
                        (screen, button) -> {
                            memory.removeKey(resloc);
                            StorageUtil.getStorage().save(memory);
                            refreshConfigScreen(parent);
                        },
                        null,
                        true,
                        40)));
        StorageUtil.getStorage().appendOptionsToSettings(memory, builder);
        return builder.build();
    }

    /////////////////
    // WHERE IS IT //
    /////////////////
    private static ConfigCategory makeWhereIsItLink() {
        return PlaceholderCategory.createBuilder()
                .name(translatable("whereisit.config.title"))
                .tooltip(translatable("chesttracker.config.whereisit.tooltip"))
                .screen((mc, parent) -> WhereIsItConfigScreenBuilder.build(parent))
                .build();
    }
}
