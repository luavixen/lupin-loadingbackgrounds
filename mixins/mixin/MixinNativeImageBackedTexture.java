package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsImpl;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NativeImageBackedTexture.class)
public abstract class MixinNativeImageBackedTexture extends AbstractTexture implements LoadingBackgroundsImpl.TextureInfo {

    @Override
    public int loadingbackgrounds$getWidth() {
        return ((NativeImageBackedTexture) (Object) this).getImage().getWidth();
    }
    @Override
    public int loadingbackgrounds$getHeight() {
        return ((NativeImageBackedTexture) (Object) this).getImage().getHeight();
    }

    @Override
    public void loadingbackgrounds$init() {
    }

}
