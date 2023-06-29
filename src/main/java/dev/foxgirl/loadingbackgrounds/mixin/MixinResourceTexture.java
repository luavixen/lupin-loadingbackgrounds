package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgrounds;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ResourceTexture.class)
public abstract class MixinResourceTexture extends AbstractTexture implements LoadingBackgrounds.TextureInfo {

    @Unique
    private int loadingbackgrounds$dataWidth;
    @Unique
    private int loadingbackgrounds$dataHeight;

    @Override
    public int loadingbackgrounds$getWidth() {
        return loadingbackgrounds$dataWidth;
    }
    @Override
    public int loadingbackgrounds$getHeight() {
        return loadingbackgrounds$dataHeight;
    }

    @ModifyVariable(method = "load(Lnet/minecraft/resource/ResourceManager;)V", at = @At("STORE"), ordinal = 0)
    private ResourceTexture.TextureData loadingimages$onLoadTextureData(ResourceTexture.TextureData textureData) {
        var image = textureData.image;
        if (image != null) {
            loadingbackgrounds$dataWidth = image.getWidth();
            loadingbackgrounds$dataHeight = image.getHeight();
        }
        return textureData;
    }

}
