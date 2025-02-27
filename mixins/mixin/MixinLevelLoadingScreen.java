package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsImpl;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// 1.20.5 and higher
import net.minecraft.server.WorldGenerationProgressTracker;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
// 1.20.4 and lower
// import net.minecraft.client.gui.WorldGenerationProgressTracker;
// import net.minecraft.client.gui.screen.LevelLoadingScreen;

@Mixin(LevelLoadingScreen.class)
public abstract class MixinLevelLoadingScreen extends Screen {

    private MixinLevelLoadingScreen(Text title) {
        super(title);
    }

    // Overwrite for 1.20.5 and higher
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (LoadingBackgroundsImpl.getInstance().draw(context, this, false)) return;
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    // Overwrite for 1.20.4 and lower
    public void renderBackgroundTexture(DrawContext context) {
        LoadingBackgroundsImpl.getInstance().draw(context, this, true);
    }

    @Shadow @Final
    private WorldGenerationProgressTracker progressProvider;

    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("STORE"), ordinal = 2)
    private int loadingbackgrounds$render$0(int x) {
        var position = LoadingBackgroundsImpl.getInstance().getPosition();
        if (position != LoadingBackgroundsImpl.Position.CENTER) {
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
        var position = LoadingBackgroundsImpl.getInstance().getPosition();
        if (position != LoadingBackgroundsImpl.Position.CENTER) {
            int height = this.height;
            int size = progressProvider.getSize();

            switch (position.ordinal()) {
                case 1:
                case 2:
                    return size + (size / 4) + 15;
                case 3:
                case 4:
                    return height - size - (size / 4);
            }
        }

        return y;
    }

}
