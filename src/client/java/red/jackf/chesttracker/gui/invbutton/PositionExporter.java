package red.jackf.chesttracker.gui.invbutton;

import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.gui.invbutton.data.ButtonPositionDataFile;
import red.jackf.chesttracker.gui.invbutton.position.ButtonPosition;
import red.jackf.chesttracker.util.Constants;
import red.jackf.jackfredlib.client.api.toasts.ImageSpec;
import red.jackf.jackfredlib.client.api.toasts.ToastBuilder;
import red.jackf.jackfredlib.client.api.toasts.ToastFormat;
import red.jackf.jackfredlib.client.api.toasts.Toasts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PositionExporter {
    public static Path getExportPath() {
        return Constants.STORAGE_DIR.resolve("export");
    }

    public static void export(AbstractContainerScreen<?> screen, ButtonPosition toExport) {
        String className = ButtonPositionMap.getClassString(screen);

        ButtonPositionDataFile file = new ButtonPositionDataFile(List.of(className), toExport);
        var json = ButtonPositionDataFile.CODEC.encodeStart(JsonOps.INSTANCE, file)
                .resultOrPartial(Util.prefix("Could not export button position", PositionExporter::onFail));

        String[] split = className.split("\\.");
        Path path = getExportPath().resolve(split[split.length - 1] + ".json");

        if (json.isPresent()) {
            try {
                Files.createDirectories(path.getParent());
                FileUtils.write(path.toFile(), json.get().toString(), StandardCharsets.UTF_8);
                Toasts.INSTANCE.send(ToastBuilder.builder(ToastFormat.DARK, Component.translatable("chesttracker.title"))
                        .withImage(ImageSpec.modIcon(ChestTracker.ID))
                        .addMessage(Component.translatable("chesttracker.inventoryButton.export.toast",
                                Component.literal(path.toString()).withStyle(ChatFormatting.GOLD)))
                        .progressShowsVisibleTime()
                        .build());
            } catch (NullPointerException | IOException ex) {
                onFail(ex);
            }
        }
    }

    private static void sendErrorToast() {
        Toasts.INSTANCE.send(ToastBuilder.builder(ToastFormat.DARK, Component.translatable("chesttracker.title"))
                .withImage(ImageSpec.modIcon("chesttracker"))
                .addMessage(Component.translatable("chesttracker.inventoryButton.export.errorToast").withStyle(ChatFormatting.RED))
                .progressShowsVisibleTime()
                .build());
    }

    private static void onFail(Exception ex) {
        sendErrorToast();
        ChestTracker.LOGGER.error("Error exporting button position", ex);
    }

    private static void onFail(String message) {
        sendErrorToast();
        ChestTracker.LOGGER.error(message);
    }
}
