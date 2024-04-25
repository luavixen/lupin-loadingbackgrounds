package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
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
        if (LoadingBackgrounds.getInstance().isLoadingMessage(this.getTitle())) {
            LoadingBackgrounds.getInstance().draw(context, this);
        } else {
            LoadingBackgrounds.getInstance().drawDefaultBackground(context, this);
        }
    }

    // Overwrite for 1.20.4 and lower
    public void renderBackgroundTexture(DrawContext context) {
        if (LoadingBackgrounds.getInstance().isLoadingMessage(this.getTitle())) {
            LoadingBackgrounds.getInstance().draw(context, this);
        } else {
            LoadingBackgrounds.getInstance().drawDefaultBackground(context, this);
        }
    }

}
