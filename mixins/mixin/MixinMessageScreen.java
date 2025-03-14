package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsImpl;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MessageScreen.class)
public abstract class MixinMessageScreen extends Screen {

    private MixinMessageScreen(Text title) {
        super(title);
    }

    // Overwrite for 1.20.5 and higher
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (
            LoadingBackgroundsImpl.isLoadingMessage(getTitle()) &&
            LoadingBackgroundsImpl.getInstance().draw(context, this, true)
        ) return;
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    // Overwrite for 1.20.4 and lower
    public void renderBackgroundTexture(DrawContext context) {
        if (LoadingBackgroundsImpl.isLoadingMessage(getTitle())) {
            LoadingBackgroundsImpl.getInstance().draw(context, this, true);
        } else {
            LoadingBackgroundsImpl.getInstance().drawDefaultBackground(context, this);
        }
    }

}
