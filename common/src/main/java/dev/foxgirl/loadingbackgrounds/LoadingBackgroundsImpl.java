package dev.foxgirl.loadingbackgrounds;

import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class LoadingBackgroundsImpl extends Screen implements LoadingBackgrounds {

    private static final Logger LOGGER = LogManager.getLogger("loadingbackgrounds");

    private static LoadingBackgroundsImpl INSTANCE;

    public static LoadingBackgroundsImpl getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("""
                Tried to access LoadingBackgroundsImpl instance before it was initialized

                This usually happens when one of the possible loading screens
                attempts to render before the mod initialization step finishes.

                This is probably a conflict with another mod!
            """);
        }
        return INSTANCE;
    }

    private Config config = Config.DEFAULT;

    public LoadingBackgroundsImpl() {
        super(Text.empty());
        INSTANCE = this;
    }

    @Override
    public void init(@NotNull Path configDirectory) {
        LOGGER.info("Setting up Loading Backgrounds...");
        config = Config.read(configDirectory);
    }

    private static final Set<String> loadingMessageTranslationKeys = new HashSet<>(Arrays.asList(new String[] {
        "menu.generatingLevel",
        "menu.generatingTerrain",
        "menu.loadingForcedChunks",
        "menu.loadingLevel",
        "menu.preparingSpawn",
        "menu.savingChunks",
        "menu.savingLevel",
        "menu.working",
        "multiplayer.downloadingStats",
        "multiplayer.downloadingTerrain",
        "selectWorld.data_read",
        "selectWorld.loading_list",
        "selectWorld.resource_load",
        "resourcepack.downloading",
        "resourcepack.progress",
        "download.pack.title",
    }));

    public static boolean isLoadingMessage(@Nullable Text message) {
        if (message != null) {
            var content = message.getContent();
            if (content instanceof TranslatableTextContent) {
                return loadingMessageTranslationKeys.contains(((TranslatableTextContent) content).getKey());
            }
        }
        return false;
    }

    private Iterator<Identifier> textures;

    private Identifier texturePrevious;
    private Identifier textureCurrent;

    private double stateSecondsStarted = seconds();
    private boolean stateIsFading = false;


    public @NotNull Position getPosition() {
        return config.position();
    }

    private void initFromScreen(Screen screen) {
        client = getClient();
        width = screen.width;
        height = screen.height;
    }

    public boolean draw(DrawContext context, Screen screen, boolean shouldDrawDefaultBackground) {
        double secondsNow = seconds();
        double secondsDiff = secondsNow - stateSecondsStarted;

        if (secondsDiff > Math.max(config.secondsStay(), config.secondsFade()) + 5.0D || textures == null) {
            secondsDiff = 0.0D;

            stateSecondsStarted = secondsNow;
            stateIsFading = false;

            textures = getBackgroundTextures();

            if (textures == null) {
                if (shouldDrawDefaultBackground) {
                    drawDefaultBackground(context, screen);
                }
                return false;
            }

            texturePrevious = textures.next();
            textureCurrent = textures.next();
        }

        boolean success;

        if (stateIsFading) {
            success = drawCustomBackground(context, screen, texturePrevious, config.brightness(), 1.0F);
            drawCustomBackground(context, screen, textureCurrent, config.brightness(), (float) Math.min(secondsDiff / config.secondsFade(), 1.0D));
            if (secondsDiff > config.secondsFade()) {
                stateSecondsStarted = secondsNow;
                stateIsFading = false;
            }
        } else {
            success = drawCustomBackground(context, screen, textureCurrent, config.brightness(), 1.0F);
            if (secondsDiff > config.secondsStay()) {
                stateSecondsStarted = secondsNow;
                stateIsFading = true;
                texturePrevious = textureCurrent;
                textureCurrent = textures.next();
            }
        }

        if (!success && shouldDrawDefaultBackground) {
            drawDefaultBackground(context, screen);
        }

        return success;
    }

    public boolean drawCustomBackground(DrawContext context, Screen screen, Identifier texture, float brightness, float opacity) {
        initFromScreen(screen);

        if (texture == null || texture.equals(MissingSprite.getMissingSpriteId())) {
            return false;
        }

        var textureInfo = (TextureInfo) getTextureManager().getTexture(texture);

        if (textureInfo == null || textureInfo == MissingSprite.getMissingSpriteTexture()) {
            return false;
        }

        textureInfo.loadingbackgrounds$init();

        float textureWidth = textureInfo.loadingbackgrounds$getWidth();
        float textureHeight = textureInfo.loadingbackgrounds$getHeight();

        if (textureWidth <= 0 || textureHeight <= 0) {
            return false;
        }

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

        var oldShader = RenderSystem.getShader();
        var oldShaderColor = RenderSystem.getShaderColor();
        var oldShaderTexture = RenderSystem.getShaderTexture(0);

        RenderSystem.setShaderColor(brightness, brightness, brightness, opacity);

        context.drawTexture(texture, 0, 0, 0, offsetX, offsetY, (int) screenWidth, (int) screenHeight, (int) (textureWidth * scaleX), (int) (textureHeight * scaleY));
        // void drawTexture(Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight)

        RenderSystem.setShader(() -> oldShader);
        RenderSystem.setShaderColor(oldShaderColor[0], oldShaderColor[1], oldShaderColor[2], oldShaderColor[3]);
        RenderSystem.setShaderTexture(0, oldShaderTexture);

        RenderSystem.disableBlend();

        return true;
    }

    public void drawDefaultBackground(DrawContext context, Screen screen) {
        initFromScreen(screen);
        drawDefaultBackgroundActual(context, screen);
    }

    /* Implementation for ~~1.20.5~~ 1.21.0 and higher */
    private void drawDefaultBackgroundActual(DrawContext context, Screen screen) {
        float delta = client.getRenderTickCounter().getLastDuration();
        if (client.world == null) {
            renderPanoramaBackground(context, delta);
        }
        applyBlur(delta);
        renderDarkening(context);
    }

    /* Implementation for 1.20.4 and lower
    private void drawDefaultBackgroundActual(DrawContext context, Screen screen) {
        context.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        context.drawTexture(OPTIONS_BACKGROUND_TEXTURE, 0, 0, 0, 0.0F, 0.0F, width, height, 32, 32);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    } */

    public enum Position {
        CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public record Config(
        double secondsStay,
        double secondsFade,
        float brightness,
        @NotNull Position position,
        boolean shouldLoadResources
    ) {
        private static final Gson GSON =
            new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .setPrettyPrinting()
                .setLenient()
                .create();

        private static final String DEFAULT_JSON =
            """
            // Loading Backgrounds configuration JSON file
            {
              // Amount of time that each background is displayed for
              "secondsStay": 5.0,
              // Amount of time it takes to fade between backgrounds
              "secondsFade": 0.5,
              // Background brightness, between 0.0 and 1.0
              "brightness": 1.0,
              // Level loading indicator position
              // One of "CENTER", "BOTTOM_LEFT", "BOTTOM_RIGHT", "TOP_LEFT", or "TOP_RIGHT"
              "position": "BOTTOM_RIGHT",
              // Should we try to forcefully load any resource packs that could contain background images?
              "shouldLoadResources": false
            }
            """;
        private static final Config DEFAULT = GSON.fromJson(DEFAULT_JSON, Config.class);

        public static @NotNull Config read(@NotNull Path pathConfigDirectory) {
            Path pathFile = pathConfigDirectory.resolve("loadingbackgrounds-config.json");
            Path pathTemp = pathConfigDirectory.resolve("loadingbackgrounds-config.json.tmp");

            try (var reader = Files.newBufferedReader(pathFile)) {
                return GSON.fromJson(reader, Config.class);
            } catch (NoSuchFileException cause) {
                LOGGER.warn("Failed to read config, file not found");
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
    private static ResourcePackManager getResourcePackManager() {
        return getClient().getResourcePackManager();
    }
    private static TextureManager getTextureManager() {
        return getClient().getTextureManager();
    }

    private static final Pattern PROFILE_NAME_PATTERN =
        Pattern.compile("load(ing)?[\\W_-]{0,3}(background|image|pic)", Pattern.CASE_INSENSITIVE);

    private static String getProfileID(ResourcePackProfile profile) {
        // 1.20.5 and higher
        return profile.getId();
        // 1.20.4 and lower
        // return profile.getName();
    }

    private static boolean matchesProfileNamePattern(String name) {
        return PROFILE_NAME_PATTERN.matcher(name).find();
    }
    private static boolean matchesProfileNamePattern(ResourcePackProfile profile) {
        return matchesProfileNamePattern(getProfileID(profile)) || matchesProfileNamePattern(profile.getDisplayName().getString());
    }

    private void reloadResourcePacks() {
        if (!config.shouldLoadResources()) return;

        var resourceManager = (ReloadableResourceManagerImpl) getResourceManager();
        var resourcePackManager = getResourcePackManager();

        var profiles = resourcePackManager.getProfiles();
        var profilesEnabled = resourcePackManager.getEnabledProfiles();

        boolean reload = false;

        for (var profile : profiles) {
            if (!profilesEnabled.contains(profile) && matchesProfileNamePattern(profile)) {
                LOGGER.info("Enabling resource pack " + getProfileID(profile));
                resourcePackManager.enable(getProfileID(profile));
                reload = true;
            }
        }

        if (reload) {
            resourceManager.reload(
                getClient(), getClient(),
                CompletableFuture.completedFuture(Unit.INSTANCE),
                resourcePackManager.createResourcePacks()
            );
        }
    }

    private Map<Identifier, Resource> getBackgroundTextureResources() {
        return getResourceManager().findResources("textures/gui/backgrounds", (filename) -> filename.getPath().endsWith(".png"));
    }

    private Iterator<Identifier> getBackgroundTextures() {
        var resources = getBackgroundTextureResources();
        if (resources.isEmpty()) {
            reloadResourcePacks();
            resources = getBackgroundTextureResources();
            if (resources.isEmpty()) {
                return null;
            }
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<Identifier> textures = (List) Arrays.asList(resources.keySet().toArray());
        Collections.shuffle(textures); return Iterators.cycle(textures);
    }

}
