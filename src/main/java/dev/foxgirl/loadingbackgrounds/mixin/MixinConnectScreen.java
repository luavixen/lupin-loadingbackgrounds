package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import net.minecraft.client.gui.DrawContext;
// import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen extends Screen {

    private MixinConnectScreen(Text title) {
        super(title);
    }

    // Overwrite for 1.20.5 and higher
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        LoadingBackgrounds.getInstance().draw(context, this);
    }

    // Overwrite for 1.20.4 and lower
    public void renderBackgroundTexture(DrawContext context) {
        LoadingBackgrounds.getInstance().draw(context, this);
    }

}
