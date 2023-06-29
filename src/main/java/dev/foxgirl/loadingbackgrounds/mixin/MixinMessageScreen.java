package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(MessageScreen.class)
public abstract class MixinMessageScreen extends Screen {

    private MixinMessageScreen(Text title) {
        super(title);
    }

    @Override
    public void renderBackgroundTexture(DrawContext context) {
        super.renderBackgroundTexture(context);
        if (Objects.equals(this.getTitle(), Text.translatable("selectWorld.data_read"))) {
            LoadingBackgrounds.getInstance().draw(context, this);
        }
    }

}
