package red.jackf.chesttracker.config.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.gui.MemoryKeyIcon;
import red.jackf.chesttracker.gui.widget.ItemButton;
import red.jackf.chesttracker.memory.LightweightStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.network.chat.Component.translatable;

public record MemoryKeyIconController(Option<MemoryKeyIcon> option) implements Controller<MemoryKeyIcon> {

    @Override
    public Component formatValue() {
        return CommonComponents.EMPTY;
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new Widget(screen, this.option, widgetDimension);
    }

    public static class Builder implements ControllerBuilder<MemoryKeyIcon> {
        private final Option<MemoryKeyIcon> option;

        public Builder(Option<MemoryKeyIcon> option) {
            this.option = option;
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public Controller<MemoryKeyIcon> build() {
            return new MemoryKeyIconController(option);
        }
    }

    public static class Widget extends AbstractWidget implements ContainerEventHandler {
        private static final List<Item> PRIORITY = List.of(
                Items.GRASS_BLOCK, Items.NETHERRACK, Items.END_STONE, Items.ENDER_CHEST, Items.CHEST, Items.OAK_SAPLING,
                Items.GLOWSTONE, Items.NETHER_STAR, Items.CRAFTING_TABLE, Items.EMERALD
        );
        private static final Map<Item, ItemStack> ITEMS = Stream.concat(
                        PRIORITY.stream(),
                        BuiltInRegistries.ITEM.stream().filter(item -> item != Items.AIR && !PRIORITY.contains(item))
                ).map(item -> Pair.of(item, new ItemStack(item)))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (a, b) -> a, LinkedHashMap::new));
        private static final int PADDING = 2; // px
        private final EditBox editBox;
        private final ItemButton setItemButton;
        private GuiEventListener focused;
        private boolean dragging;

        public Widget(YACLScreen screen, Option<MemoryKeyIcon> option, Dimension<Integer> dim) {
            super(dim);

            this.editBox = new EditBox(Minecraft.getInstance().font,
                    dim.x() + PADDING,
                    dim.y() + PADDING,
                    dim.width() - 20 - 2 * PADDING,
                    dim.height() - 2 * PADDING, CommonComponents.EMPTY);
            this.editBox.setValue(option.pendingValue().id().toString());
            this.editBox.setResponder(s -> {
                var parsed = ResourceLocation.tryParse(s);
                if (parsed != null) option.requestSet(new MemoryKeyIcon(parsed, option.binding().getValue().icon()));

                if (s.isEmpty()) {
                    this.editBox.setSuggestion(translatable("chesttracker.config.memoryKeyIcons.dimension").getString());
                } else {
                    this.editBox.setSuggestion("");
                }
            });
            this.editBox.setTooltip(Tooltip.create(translatable("chesttracker.config.memoryKeyIcons.dimension")));

            this.setItemButton = new ItemButton(option.pendingValue().icon()
                    .toStack(), dim.xLimit() - 20, dim.y(), translatable("chesttracker.config.memoryKeyIcons.icon"),
                    b -> Minecraft.getInstance().setScreen(new SelectorScreen<>(Component.translatable("chesttracker.gui.selectIcon"), screen, ITEMS, item -> {
                        if (item != null)
                            option.requestSet(new MemoryKeyIcon(option.binding().getValue()
                                    .id(), new LightweightStack(item)));
                    })), ItemButton.Background.VANILLA, 0);
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.editBox.setY(getDimension().y() + PADDING);
            this.setItemButton.setY(getDimension().y());

            this.editBox.render(graphics, mouseX, mouseY, partialTick);
            this.setItemButton.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.editBox.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(this.editBox);
                return true;
            }
            return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void setDimension(Dimension<Integer> dim) {
            super.setDimension(dim);
            this.editBox.setX(dim.x() + PADDING);
            this.editBox.setY(dim.y() + 2 * PADDING);
            this.editBox.setWidth(dim.width() - 20 - 2 * PADDING);
            this.setItemButton.setX(dim.xLimit() - 20);
            this.setItemButton.setY(dim.y());
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(editBox, setItemButton);
        }

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean isDragging) {
            this.dragging = isDragging;
        }

        @Override
        public void unfocus() {
            this.setFocused(null);
        }

        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return this.focused;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            if (focused == this.focused) return;
            if (this.focused != null)
                this.focused.setFocused(false);
            this.focused = focused;
            if (this.focused != null)
                this.focused.setFocused(true);
        }
    }
}
