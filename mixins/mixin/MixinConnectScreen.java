package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsImpl;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

// 1.20.5 and higher
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
// 1.20.4 and lower
// import net.minecraft.client.gui.screen.ConnectScreen;

@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen extends Screen {

    private MixinConnectScreen(Text title) {
        super(title);
    }

    // Overwrite for 1.20.5 and higher
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        LoadingBackgroundsImpl.getInstance().draw(context, this);
    }

    // Overwrite for 1.20.4 and lower
    public void renderBackgroundTexture(DrawContext context) {
        LoadingBackgroundsImpl.getInstance().draw(context, this);
    }

}
