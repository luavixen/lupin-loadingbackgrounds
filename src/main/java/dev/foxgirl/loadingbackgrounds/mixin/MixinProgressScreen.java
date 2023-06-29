package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProgressScreen.class)
public abstract class MixinProgressScreen extends Screen {

    private MixinProgressScreen(Text title) {
        super(title);
    }

    @Override
    public void renderBackgroundTexture(DrawContext context) {
        super.renderBackgroundTexture(context);
        LoadingBackgrounds.getInstance().draw(context, this);
    }

}
