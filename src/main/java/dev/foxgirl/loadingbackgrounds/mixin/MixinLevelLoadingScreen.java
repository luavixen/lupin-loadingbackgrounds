package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import net.minecraft.client.gui.DrawContext;
// import net.minecraft.client.gui.WorldGenerationProgressTracker;
// import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.server.WorldGenerationProgressTracker;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelLoadingScreen.class)
public abstract class MixinLevelLoadingScreen extends Screen {

    private MixinLevelLoadingScreen(Text title) {
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

    @Shadow @Final
    private WorldGenerationProgressTracker progressProvider;

    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("STORE"), ordinal = 2)
    private int loadingbackgrounds$render$0(int x) {
        var position = LoadingBackgrounds.getInstance().getPosition();
        if (position != LoadingBackgrounds.Position.CENTER) {
            int width = this.width;
            int size = progressProvider.getSize();

            switch (position.ordinal()) {
                case 1:
                case 3:
                    return size + (size / 4);
                case 2:
                case 4:
                    return width - size - (size / 4);
            }
        }

        return x;
    }
    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("STORE"), ordinal = 3)
    private int loadingbackgrounds$render$1(int y) {
        var position = LoadingBackgrounds.getInstance().getPosition();
        if (position != LoadingBackgrounds.Position.CENTER) {
            int height = this.height;
            int size = progressProvider.getSize();

            switch (position.ordinal()) {
                case 1:
                case 2:
                    return size + (size / 4);
                case 3:
                case 4:
                    return height - 30 - size - (size / 4);
            }
        }

        return y;
    }

}
