package dev.foxgirl.loadingbackgrounds;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class LoadingBackgroundsMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LoadingBackgrounds.createInstance().init(FabricLoader.getInstance().getConfigDir());
    }

}
