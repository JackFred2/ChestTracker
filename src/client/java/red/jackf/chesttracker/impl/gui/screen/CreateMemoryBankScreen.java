package red.jackf.chesttracker.impl.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.impl.gui.GuiConstants;
import red.jackf.chesttracker.impl.gui.util.TextColours;
import red.jackf.chesttracker.impl.gui.widget.CustomEditBox;
import red.jackf.chesttracker.impl.gui.widget.TextWidget;
import red.jackf.chesttracker.impl.memory.MemoryBankAccessImpl;
import red.jackf.chesttracker.impl.storage.Storage;
import red.jackf.chesttracker.impl.util.GuiUtil;
import red.jackf.chesttracker.impl.util.Strings;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class CreateMemoryBankScreen extends BaseUtilScreen {
    private static final int CLOSE_BUTTON_SIZE = 12;
    private static final int CONTENT_TOP = 30;
    private final Screen parent;
    private final Runnable afterBankLoaded;
    private String name = "";
    private String id;

    private CustomEditBox idEditBox = null;
    private CustomEditBox nameEditBox = null;
    private Button loadButton;

    protected CreateMemoryBankScreen(@Nullable Screen parent, Runnable afterBankLoaded) {
        super(Component.translatable("chesttracker.gui.createMemoryBank"));
        this.parent = parent;
        this.afterBankLoaded = afterBankLoaded;
        this.id = getNextIdDefault();
    }

    private String getNextIdDefault() {
        var keys = Storage.getAllIds();
        int index = 1;
        String id;
        do {
            id = makeUserId("custom" + index++);
        } while (keys.contains(id));
        return id;
    }

    private String makeUserId(String id) {
        return "user/" + Strings.sanitizeForPath(id);
    }

    @Override
    protected void init() {
        super.init();

        var font = Minecraft.getInstance().font;
        final int workingWidth = this.menuWidth - 2 * GuiConstants.MARGIN;

        // close button
        this.addRenderableWidget(GuiUtil.close(
                left + menuWidth - (GuiConstants.SMALL_MARGIN + CLOSE_BUTTON_SIZE),
                top + GuiConstants.SMALL_MARGIN,
                b -> this.onClose()));

        int y = this.top + CONTENT_TOP;

        // ID
        var idLabel = translatable("chesttracker.gui.id");
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                y,
                idLabel,
                TextColours.getLabelColour()));

        this.idEditBox = this.addRenderableWidget(new CustomEditBox(
                font,
                this.left + GuiConstants.MARGIN + font.width(idLabel) + GuiConstants.SMALL_MARGIN,
                y - 2,
                workingWidth - GuiConstants.SMALL_MARGIN - font.width(idLabel),
                font.lineHeight + 3,
                idEditBox,
                CommonComponents.EMPTY) {

            // replace with valid windows path
            @Override
            public void setFocused(boolean focused) {
                super.setFocused(focused);
                if (!focused) {
                    var sanitized = sanitize(this.getValue());
                    if (!sanitized.equals(this.getValue())) {
                        this.setValue(sanitized);
                    }
                }
            }
        });
        this.idEditBox.setValue(this.id);
        this.idEditBox.setResponder(s -> {
            this.id = s;
            refreshValidity();
            refreshName();
        });

        y += 15;

        // Name
        var nameLabel = translatable("mco.backup.entry.name");
        this.addRenderableOnly(new TextWidget(this.left + GuiConstants.MARGIN,
                y,
                nameLabel,
                TextColours.getLabelColour()));

        this.nameEditBox = this.addRenderableWidget(new CustomEditBox(
                font,
                this.left + GuiConstants.MARGIN + font.width(nameLabel) + GuiConstants.SMALL_MARGIN,
                y - 2,
                workingWidth - GuiConstants.SMALL_MARGIN - font.width(nameLabel),
                font.lineHeight + 3,
                nameEditBox,
                CommonComponents.EMPTY));
        this.nameEditBox.setValue(this.name);
        this.nameEditBox.setResponder(s -> {
            this.name = s;
            refreshValidity();
            refreshName();
        });

        y += 15;

        // Load World Default
        this.addRenderableWidget(Button.builder(translatable("chesttracker.gui.createMemoryBank.fillDefault"), this::loadDefault)
                .bounds(this.left + GuiConstants.MARGIN,
                        y,
                        workingWidth,
                        20)
                .build());

        // Load
        this.loadButton = this.addRenderableWidget(Button.builder(CommonComponents.EMPTY, this::createAndLoad)
                .bounds(this.left + GuiConstants.MARGIN,
                        this.top + menuHeight - 20 - GuiConstants.MARGIN,
                        workingWidth,
                        20)
                .build());

        refreshValidity();
        refreshName();
    }

    private String sanitize(String in) {
        in = in.trim();
        try {
            var path = Path.of(in);
            var builder = new ArrayList<String>();
            for (var segment : path) {
                builder.add(Strings.sanitizeForPath(segment.toString()));
            }
            return String.join("/", builder);
        } catch (InvalidPathException ex) {
            return Strings.sanitizeForPath(in);
        }
    }

    private void loadDefault(Button button) {
        var coord = Coordinate.getCurrent();
        if (coord.isEmpty()) return;
        this.idEditBox.setValue(coord.get().id());
        this.nameEditBox.setValue(coord.get().userFriendlyName());
    }

    protected static String getNameFromId(String id) {
        return translatable("options.generic_value", translatable("generator.custom"), literal(id)).getString();
    }

    private void refreshName() {
        if (this.name.isEmpty()) {
            this.nameEditBox.setHint(literal(getNameFromId(this.id)));
            this.nameEditBox.setTextColor(TextColours.getHintColour());
        } else {
            this.nameEditBox.setHint(CommonComponents.EMPTY);
            this.nameEditBox.setTextColor(TextColours.getTextColour());
        }
    }

    private void refreshValidity() {
        if (Storage.exists(this.id)) {
            this.idEditBox.setTextColor(TextColours.getErrorColour());
            this.loadButton.setMessage(translatable("chesttracker.gui.createMemoryBank.alreadyExists"));
            this.loadButton.active = false;
        } else if (this.id.isBlank()) {
            this.idEditBox.setTextColor(TextColours.getErrorColour());
            this.loadButton.setMessage(translatable("chesttracker.gui.createMemoryBank.emptyId"));
            this.loadButton.active = false;
        } else {
            this.idEditBox.setTextColor(TextColours.getTextColour());
            this.loadButton.setMessage(translatable("chesttracker.gui.createMemoryBank.createAndLoad"));
            this.loadButton.active = true;
        }
    }

    private void createAndLoad(Button button) {
        this.id = sanitize(this.id);
        MemoryBankAccessImpl.INSTANCE.loadOrCreate(this.id, this.name.isEmpty() ? getNameFromId(this.id) : this.name);
        this.afterBankLoaded.run();
    }

    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
