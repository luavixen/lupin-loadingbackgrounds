package dev.foxgirl.loadingbackgrounds;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("loadingbackgrounds")
public final class LoadingBackgroundsMod {

    public LoadingBackgroundsMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        LoadingBackgrounds.createInstance().init(FMLPaths.CONFIGDIR.get(), true);
    }

}
