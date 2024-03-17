package red.jackf.chesttracker.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import red.jackf.chesttracker.compat.Compatibility;
import red.jackf.chesttracker.gui.GuiConstants;
import red.jackf.chesttracker.gui.invbutton.ButtonPositionMap;
import red.jackf.chesttracker.gui.invbutton.PositionExporter;
import red.jackf.chesttracker.gui.screen.MemoryBankManagerScreen;
import red.jackf.chesttracker.memory.MemoryBank;
import red.jackf.chesttracker.storage.backend.Backend;
import red.jackf.chesttracker.util.Constants;
import red.jackf.chesttracker.util.GuiUtil;
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
                .category(makeMemoryAndStorageCategory(instance, parent))
                .category(makeCompatibilityCategory(instance))
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

    private static Component requiresRestart() {
        return translatable("chesttracker.config.requiresRestart").withStyle(ChatFormatting.RED);
    }

    ////////////////
    // CATEGORIES //
    ////////////////
    private static ConfigCategory makeMainCategory(ConfigClassHandler<ChestTrackerConfig> instance) {
        return ConfigCategory.createBuilder()
                .name(translatable("chesttracker.title"))
                .group(makeGuiGroup(instance))
                .group(makeInventoryButtonGroup(instance))
                .group(makeRenderingGroup(instance))
                .group(makeDevGuiGroup(instance))
                .build();
    }

    private static ConfigCategory makeMemoryAndStorageCategory(ConfigClassHandler<ChestTrackerConfig> instance, Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(translatable("chesttracker.config.memoryAndStorage"))
                .group(makeMemoryGroup(instance, parent))
                .group(makeStorageGroup(instance, parent));
        return builder.build();
    }

    /////////
    // GUI //
    /////////

    private static OptionGroup makeGuiGroup(ConfigClassHandler<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.gui"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.gui.autofocusSearchBar"))
                        .description(OptionDescription.of(translatable("chesttracker.config.gui.autofocusSearchBar.description")))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.defaults().gui.autofocusSearchBar,
                                () -> instance.instance().gui.autofocusSearchBar,
                                b -> instance.instance().gui.autofocusSearchBar = b)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.gui.showAutocomplete"))
                        .description(b -> {
                            var desc = OptionDescription.createBuilder()
                                             .image(getDescriptionImage("show_autocomplete", b), 85, 59)
                                             .text(translatable("chesttracker.config.searchables.required"));

                            if (!Compatibility.SEARCHABLES) desc.text(translatable("chesttracker.config.searchables.notInstalled").withStyle(ChatFormatting.RED));

                            return desc.build();
                        })
                        .available(Compatibility.SEARCHABLES)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.defaults().gui.showAutocomplete,
                                () -> instance.instance().gui.showAutocomplete,
                                b -> instance.instance().gui.showAutocomplete = b)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.gui.autocompleteShowsRegularNames"))
                        .description(b -> {
                            var desc = OptionDescription.createBuilder()
                                             .image(getDescriptionImage("show_unnamed_in_autocomplete", b), 118, 85)
                                             .text(translatable("chesttracker.config.searchables.required"));

                            if (!Compatibility.SEARCHABLES) desc.text(translatable("chesttracker.config.searchables.notInstalled").withStyle(ChatFormatting.RED));

                            return desc.build();
                        })
                        .available(Compatibility.SEARCHABLES)
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.defaults().gui.autocompleteShowsRegularNames,
                                () -> instance.instance().gui.autocompleteShowsRegularNames,
                                b -> instance.instance().gui.autocompleteShowsRegularNames = b)
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
                                instance.defaults().gui.showResizeWidget,
                                () -> instance.instance().gui.showResizeWidget,
                                b -> instance.instance().gui.showResizeWidget = b)
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(translatable("chesttracker.config.gui.gridWidth"))
                        .description(OptionDescription.createBuilder()
                                .image(getDescriptionImage("grid_width"), 135, 102)
                                .build())
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(GuiConstants.MIN_GRID_COLUMNS, GuiConstants.MAX_GRID_HEIGHT)
                                .step(1)
                                .formatValue(i -> translatable("chesttracker.config.gui.gridSizeSlider", i)))
                        .binding(
                                instance.defaults().gui.gridWidth,
                                () -> instance.instance().gui.gridWidth,
                                i -> instance.instance().gui.gridWidth = i).
                        build())
                .option(Option.<Integer>createBuilder()
                        .name(translatable("chesttracker.config.gui.gridHeight"))
                        .description(OptionDescription.createBuilder()
                                .image(getDescriptionImage("grid_height"), 135, 102)
                                .build())
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(GuiConstants.MIN_GRID_ROWS, GuiConstants.MAX_GRID_HEIGHT)
                                .step(1)
                                .formatValue(i -> translatable("chesttracker.config.gui.gridSizeSlider", i)))
                        .binding(
                                instance.defaults().gui.gridHeight,
                                () -> instance.instance().gui.gridHeight,
                                i -> instance.instance().gui.gridHeight = i).
                        build())
                .option(Option.<Boolean>createBuilder()
                                .name(translatable("chesttracker.config.gui.hideMemoryBankIds"))
                                .description(b -> OptionDescription.createBuilder()
                                        .image(getDescriptionImage("hide_memory_bank_ids", b), 143, 81)
                                        .text(translatable("chesttracker.config.gui.hideMemoryBankIds.description"))
                                        .build())
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .yesNoFormatter()
                                        .coloured(true))
                                .binding(
                                        instance.defaults().gui.hideMemoryIds,
                                        () -> instance.instance().gui.hideMemoryIds,
                                        b -> instance.instance().gui.hideMemoryIds = b)
                                .build())
                .option(Option.<Integer>createBuilder()
                                .name(translatable("chesttracker.config.gui.itemListTextScale"))
                                .description(OptionDescription.of(translatable("chesttracker.config.gui.itemListTextScale.description")))
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(-6, 0)
                                        .step(1))
                                .binding(instance.defaults().gui.itemListTextScale,
                                         () -> instance.instance().gui.itemListTextScale,
                                         i -> instance.instance().gui.itemListTextScale = i)
                                .build())
                .build();
    }

    private static OptionGroup makeInventoryButtonGroup(ConfigClassHandler<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.inventoryButton"))
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.inventoryButton.enabled"))
                        .description(b -> OptionDescription.createBuilder()
                                .image(getDescriptionImage("inventory_button", b), 128, 128)
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.defaults().gui.inventoryButton.enabled,
                                () -> instance.instance().gui.inventoryButton.enabled,
                                b -> instance.instance().gui.inventoryButton.enabled = b
                        ).build())
                /*
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.inventoryButton.showExtra"))
                        .description(b -> OptionDescription.createBuilder()
                                .text(translatable("chesttracker.config.inventoryButton.showExtra.description"))
                                .image(getDescriptionImage("inventory_button_show_extra", b), 256, 192)
                                .build())
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.defaults().gui.inventoryButton.showExtra,
                                () -> instance.instance().gui.inventoryButton.showExtra,
                                b -> instance.instance().gui.inventoryButton.showExtra = b
                        ).build())*/
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.inventoryButton.export"))
                        .description(OptionDescription.of(translatable("chesttracker.config.inventoryButton.export.description",
                                literal(PositionExporter.getExportPath().toString()).withStyle(ChatFormatting.GOLD))))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(instance.defaults().gui.inventoryButton.showExport,
                                () -> instance.instance().gui.inventoryButton.showExport,
                                b -> instance.instance().gui.inventoryButton.showExport = b)
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(translatable("chesttracker.config.inventoryButton.manageCustom"))
                        .text(translatable("chesttracker.gui.open"))
                        .action(ChestTrackerConfigScreenBuilder::managePreferences)
                        .build())
                .build();
    }

    private static OptionGroup makeRenderingGroup(ConfigClassHandler<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.rendering"))
                .option(Option.<Integer>createBuilder()
                        .name(translatable("chesttracker.config.rendering.nameRenderRange"))
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(4, 24)
                                .step(1)
                                .formatValue(i -> translatable("chesttracker.generic.blocks", i)))
                        .binding(
                                instance.defaults().rendering.nameRange,
                                () -> instance.instance().rendering.nameRange,
                                i -> instance.instance().rendering.nameRange = i
                        ).build())
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.rendering.displayContainerNames"))
                        .description(OptionDescription.of(translatable("chesttracker.config.rendering.displayContainerNames.description")))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter())
                        .binding(
                                instance.defaults().rendering.displayContainerNames,
                                () -> instance.instance().rendering.displayContainerNames,
                                b -> instance.instance().rendering.displayContainerNames = b
                        ).build())
                .option(ButtonOption.createBuilder()
                        .name(translatable("chesttracker.config.whereisit"))
                        .description(OptionDescription.of(translatable("chesttracker.config.whereisit.description")))
                        .text(translatable("chesttracker.gui.open"))
                        .action((yaclScreen, button) -> Minecraft.getInstance().setScreen(WhereIsItConfigScreenBuilder.build(yaclScreen))).build())
                .build();
    }

    private static OptionGroup makeDevGuiGroup(ConfigClassHandler<ChestTrackerConfig> instance) {
        return OptionGroup.createBuilder()
                .name(translatable("chesttracker.config.dev"))
                .collapsed(true)
                .option(Option.<Boolean>createBuilder()
                        .name(translatable("chesttracker.config.dev.showDevHud"))
                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                .yesNoFormatter()
                                .coloured(true))
                        .binding(
                                instance.defaults().debug.showDevHud,
                                () -> instance.instance().debug.showDevHud,
                                b -> instance.instance().debug.showDevHud = b
                        ).build())
                .build();
    }

    ////////////
    // STORAGE //
    ////////////
    private static OptionGroup makeMemoryGroup(@SuppressWarnings("unused") ConfigClassHandler<ChestTrackerConfig> instance, Screen parent) {
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

    private static OptionGroup makeStorageGroup(ConfigClassHandler<ChestTrackerConfig> instance, Screen parent) {
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
                                instance.defaults().storage.storageBackend,
                                () -> instance.instance().storage.storageBackend,
                                e -> {
                                    instance.instance().storage.storageBackend = e;
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
                                instance.defaults().storage.readableJsonMemories,
                                () -> instance.instance().storage.readableJsonMemories,
                                b -> {
                                    instance.instance().storage.readableJsonMemories = b;
                                    MemoryBank.save();
                                    refreshConfigScreen(parent);
                                })
                        .build());

        return rootBuilder.build();
    }

    ///////////////////
    // COMPATIBILITY //
    ///////////////////
    private static ConfigCategory makeCompatibilityCategory(ConfigClassHandler<ChestTrackerConfig> instance) {
        return ConfigCategory.createBuilder()
                .name(translatable("whereisit.config.compatibility"))
                .option(Option.<Boolean>createBuilder()
                                .name(translatable("chesttracker.config.compatibility.shulkerboxtooltip"))
                                .description(OptionDescription.of(
                                        translatable("chesttracker.config.compatibility.shulkerboxtooltip.description"),
                                        CommonComponents.EMPTY,
                                        requiresRestart()
                                ))
                                .flag(OptionFlag.GAME_RESTART)
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .onOffFormatter()
                                        .coloured(true))
                                .binding(
                                        instance.defaults().compatibility.shulkerBoxTooltipIntegration,
                                        () -> instance.instance().compatibility.shulkerBoxTooltipIntegration,
                                        b -> instance.instance().compatibility.shulkerBoxTooltipIntegration = b
                                )
                                .build())
                .option(Option.<Boolean>createBuilder()
                                .name(translatable("chesttracker.config.compatibility.wthit"))
                                .description(OptionDescription.of(
                                        translatable("chesttracker.config.compatibility.wthit.description")
                                ))
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .onOffFormatter()
                                        .coloured(true))
                                .binding(
                                        instance.defaults().compatibility.wthitIntegration,
                                        () -> instance.instance().compatibility.wthitIntegration,
                                        b -> instance.instance().compatibility.wthitIntegration = b
                                )
                                .build())
                .build();
    }

    //////////////////////////////////
    // USER BUTTON POSITION MANAGER //
    //////////////////////////////////
    private static String getDeobfName(String className) {
        // yes these are yarn names not mojmap but im avoiding licensing issues
        return switch (className) {
            case "net.minecraft.class_471" -> "net.minecraft.client.gui.screen.ingame.AnvilScreen";
            case "net.minecraft.class_466" -> "net.minecraft.client.gui.screen.ingame.BeaconScreen";
            case "net.minecraft.class_3871" -> "net.minecraft.client.gui.screen.ingame.BlastFurnaceScreen";
            case "net.minecraft.class_472" -> "net.minecraft.client.gui.screen.ingame.BrewingStandScreen";
            case "net.minecraft.class_3934" -> "net.minecraft.client.gui.screen.ingame.CartographyTableScreen";
            case "net.minecraft.class_476" -> "net.minecraft.client.gui.screen.ingame.GenericContainerScreen";
            case "net.minecraft.class_479" -> "net.minecraft.client.gui.screen.ingame.CraftingScreen";
            case "net.minecraft.class_481" -> "net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen";
            case "net.minecraft.class_480" -> "net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen";
            case "net.minecraft.class_486" -> "net.minecraft.client.gui.screen.ingame.EnchantmentScreen";
            case "net.minecraft.class_3873" -> "net.minecraft.client.gui.screen.ingame.FurnaceScreen";
            case "net.minecraft.class_3802" -> "net.minecraft.client.gui.screen.ingame.GrindstoneScreen";
            case "net.minecraft.class_488" -> "net.minecraft.client.gui.screen.ingame.HopperScreen";
            case "net.minecraft.class_491" -> "net.minecraft.client.gui.screen.ingame.HorseScreen";
            case "net.minecraft.class_490" -> "net.minecraft.client.gui.screen.ingame.InventoryScreen";
            case "net.minecraft.class_494" -> "net.minecraft.client.gui.screen.ingame.LoomScreen";
            case "net.minecraft.class_492" -> "net.minecraft.client.gui.screen.ingame.MerchantScreen";
            case "net.minecraft.class_495" -> "net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen";
            case "net.minecraft.class_4895" -> "net.minecraft.client.gui.screen.ingame.SmithingScreen";
            case "net.minecraft.class_3874" -> "net.minecraft.client.gui.screen.ingame.SmokerScreen";
            case "net.minecraft.class_3979" -> "net.minecraft.client.gui.screen.ingame.StonecutterScreen";
            default -> className;
        };
    }

    private static void managePreferences(YACLScreen parent, ButtonOption button) {
        var category = ConfigCategory.createBuilder()
                .name(translatable("chesttracker.config.inventoryButton.manageCustom"));

        var options = ButtonPositionMap.getUserPositions();

        for (String className : options.keySet().stream().sorted().toList()) {
            String rawName = getDeobfName(className);

            String[] split = rawName.split("\\.");
            String shortened = split[split.length - 1];

            category.option(ButtonOption.createBuilder()
                    .name(literal(shortened))
                    .description(OptionDescription.of(literal(rawName)))
                    .text(translatable("mco.configure.world.buttons.delete"))
                    .action((ignored, button2) -> {
                        button2.setAvailable(false);
                        ButtonPositionMap.removeUserPosition(className);
                    }).build());
        }

        Minecraft.getInstance().setScreen(YetAnotherConfigLib.createBuilder()
                .title(translatable("chesttracker.config.inventoryButton.manageCustom"))
                .category(category.build())
                .build()
                .generateScreen(parent));
    }
}
