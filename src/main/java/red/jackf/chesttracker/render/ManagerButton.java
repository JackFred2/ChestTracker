package red.jackf.chesttracker.render;

import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ManagerButton extends TexturedButtonWidget {
    private static final Identifier TEXTURE = new Identifier("textures/gui/recipe_button.png");
    private static final ManagerButton currentButton = new ManagerButton();

    public ManagerButton() {
        super(0, 0, 20, 18, 0, 0, 0, TEXTURE, (buttonWidget) -> {
        });
    }

    public static void setup() {
        ClothClientHooks.SCREEN_INIT_POST.register((client, screen, screenHooks) -> screenHooks.cloth$addButtonWidget(new ManagerButton()));
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) return;
        this.x = (client.currentScreen.width + client.getWindow().getScaledWidth()) / 2;
        this.x = (client.currentScreen.height + client.getWindow().getScaledHeight()) / 2;
        super.renderButton(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onPress() {
        System.out.println("go");
    }
}
