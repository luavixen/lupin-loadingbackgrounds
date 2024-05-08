package dev.foxgirl.loadingbackgrounds.mixin;

import dev.foxgirl.loadingbackgrounds.LoadingBackgroundsImpl;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ResourceTexture.class)
public abstract class MixinResourceTexture extends AbstractTexture implements LoadingBackgroundsImpl.TextureInfo {

    @Unique
    private int loadingbackgrounds$dataWidth = -1;
    @Unique
    private int loadingbackgrounds$dataHeight = -1;

    @Override
    public int loadingbackgrounds$getWidth() {
        return loadingbackgrounds$dataWidth;
    }
    @Override
    public int loadingbackgrounds$getHeight() {
        return loadingbackgrounds$dataHeight;
    }

    @Override
    public void loadingbackgrounds$init() {
        if (loadingbackgrounds$dataWidth > 0 && loadingbackgrounds$dataHeight > 0) return;
        this.bindTexture();
        var buffer = new int[1];
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH, buffer);
        loadingbackgrounds$dataWidth = buffer[0];
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, buffer);
        loadingbackgrounds$dataHeight = buffer[0];
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
