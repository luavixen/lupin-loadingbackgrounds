package dev.foxgirl.loadingbackgrounds;

import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mod("loadingbackgrounds")
public final class LoadingBackgrounds {

    private static final Logger LOGGER = LogManager.getLogger("loadingbackgrounds");

    private static LoadingBackgrounds INSTANCE;

    public static LoadingBackgrounds getInstance() {
        return INSTANCE;
    }

    public LoadingBackgrounds() {
        INSTANCE = this;

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    private Config config = Config.DEFAULT;

    public Position getPosition() {
        return config.position();
    }

    private Iterator<Identifier> textures;

    private Identifier texturePrevious;
    private Identifier textureCurrent;

    private double stateSecondsStarted = seconds();
    private boolean stateIsFading = false;

    public void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Setting up loadingbackgrounds...");
        config = Config.read();
    }

    public void draw(DrawContext context, Screen screen) {
        double secondsNow = seconds();
        double secondsDiff = secondsNow - stateSecondsStarted;

        if (secondsDiff > Math.max(config.secondsStay(), config.secondsFade()) + 5.0D || textures == null) {
            secondsDiff = 0.0D;

            stateSecondsStarted = secondsNow;
            stateIsFading = false;

            textures = getBackgroundTextures();
            if (textures == null) return;

            texturePrevious = textures.next();
            textureCurrent = textures.next();
        }

        if (stateIsFading) {
            drawBackgroundTexture(context, screen, texturePrevious, config.brightness(), 1.0F);
            drawBackgroundTexture(context, screen, textureCurrent, config.brightness(), (float) Math.min(secondsDiff / config.secondsFade(), 1.0D));
            if (secondsDiff > config.secondsFade()) {
                stateSecondsStarted = secondsNow;
                stateIsFading = false;
            }
        } else {
            drawBackgroundTexture(context, screen, textureCurrent, config.brightness(), 1.0F);
            if (secondsDiff > config.secondsStay()) {
                stateSecondsStarted = secondsNow;
                stateIsFading = true;
                texturePrevious = textureCurrent;
                textureCurrent = textures.next();
            }
        }
    }

    public enum Position {
        CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public record Config(double secondsStay, double secondsFade, float brightness, Position position) {
        private static final Config DEFAULT = new Config(3.0D, 0.75D, 0.66F, Position.BOTTOM_RIGHT);
        private static final String DEFAULT_JSON =
            """
            {
              // Amount of time that each background is displayed for
              "secondsStay": 5.0,
              // Amount of time it takes to fade between backgrounds
              "secondsFade": 0.5,
              // Background brightness, between 0.0 and 1.0
              "brightness": 1.0,
              // Level loading indicator position
              // One of "CENTER", "BOTTOM_LEFT", "BOTTOM_RIGHT", "TOP_LEFT", or "TOP_RIGHT"
              "position": "BOTTOM_RIGHT"
            }
            """;

        private static final Gson GSON =
            new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .setPrettyPrinting()
                .setLenient()
                .create();

        public static @NotNull Config read() {
            Path pathDirectory = FMLPaths.CONFIGDIR.get();
            Path pathFile = pathDirectory.resolve("loadingbackgrounds-config.json");
            Path pathTemp = pathDirectory.resolve("loadingbackgrounds-config.json.tmp");

            try {
                return GSON.fromJson(Files.newBufferedReader(pathFile), Config.class);
            } catch (NoSuchFileException cause) {
                LOGGER.error("Failed to read config, file not found");
            } catch (IOException cause) {
                LOGGER.error("Failed to read config, IO error", cause);
            } catch (JsonParseException cause) {
                LOGGER.error("Failed to read config, JSON error", cause);
            } catch (Exception cause) {
                LOGGER.error("Failed to read config", cause);
            }

            try {
                Files.writeString(pathTemp, DEFAULT_JSON);
                Files.move(pathTemp, pathFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException cause) {
                LOGGER.error("Failed to write new config, IO error", cause);
            } catch (Exception cause) {
                LOGGER.error("Failed to write new config", cause);
            }

            return DEFAULT;
        }
    }

    private static final long secondsStart = System.nanoTime();

    private static double seconds() {
        return (double) (System.nanoTime() - secondsStart) * 1.0e-9D;
    }

    public interface TextureInfo {
        void loadingbackgrounds$init();
        int loadingbackgrounds$getWidth();
        int loadingbackgrounds$getHeight();
    }

    private static MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }
    private static ResourceManager getResourceManager() {
        return getClient().getResourceManager();
    }
    private static TextureManager getTextureManager() {
        return getClient().getTextureManager();
    }

    private static Iterator<Identifier> getBackgroundTextures() {
        var resources = getResourceManager().findResources("textures/gui/backgrounds", (filename) -> filename.getPath().endsWith(".png"));
        if (resources.isEmpty()) {
            return null;
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<Identifier> textures = (List) Arrays.asList(resources.keySet().toArray());
        Collections.shuffle(textures); return Iterators.cycle(textures);
    }

    private static void drawBackgroundTexture(DrawContext context, Screen screen, Identifier texture, float brightness, float opacity) {
        var textureInfo = (TextureInfo) getTextureManager().getTexture(texture);

        textureInfo.loadingbackgrounds$init();

        float textureWidth = textureInfo.loadingbackgrounds$getWidth();
        float textureHeight = textureInfo.loadingbackgrounds$getHeight();
        float screenWidth = screen.width;
        float screenHeight = screen.height;

        float offsetX = 0.0F;
        float offsetY = 0.0F;
        float scaleX = 1.0F;
        float scaleY = 1.0F;

        // Calculate scale factors
        scaleX = screenWidth / textureWidth;
        scaleY = screenHeight / textureHeight;

        // Check if the texture aspect ratio matches the screen aspect ratio
        if (scaleX < scaleY) {
            // The texture is wider than the screen, so we need to adjust the scale and offset
            scaleX = scaleY;
            offsetX = 0.0F - ((screenWidth - (textureWidth * scaleX)) * 0.5F);
        } else {
            // The texture is taller than the screen or has the same aspect ratio, so we adjust the scale and offset accordingly
            scaleY = scaleX;
            offsetY = 0.0F - ((screenHeight - (textureHeight * scaleY)) * 0.5F);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();

        context.setShaderColor(brightness, brightness, brightness, opacity);
        context.drawTexture(texture, 0, 0, 0, offsetX, offsetY, (int) screenWidth, (int) screenHeight, (int) (textureWidth * scaleX), (int) (textureHeight * scaleY));
        // void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight)
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
