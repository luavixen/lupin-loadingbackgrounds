package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DownloadingTerrainScreen.class)
public abstract class MixinDownloadingTerrainScreen extends Screen {

    private MixinDownloadingTerrainScreen(Text title) {
        super(title);
    }

    @Override
    public void renderBackgroundTexture(DrawContext context) {
        super.renderBackgroundTexture(context);
        LoadingBackgrounds.getInstance().draw(context, this);
    }

}
