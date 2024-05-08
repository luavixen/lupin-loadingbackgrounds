package dev.foxgirl.loadingbackgrounds;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLPaths;

@Mod("loadingbackgrounds")
public final class LoadingBackgroundsMod {

    public LoadingBackgroundsMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        LoadingBackgrounds.createInstance().init(FMLPaths.CONFIGDIR.get(), true);
    }

}
