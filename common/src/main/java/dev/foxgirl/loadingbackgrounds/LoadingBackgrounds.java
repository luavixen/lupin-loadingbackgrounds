package dev.foxgirl.loadingbackgrounds;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface LoadingBackgrounds {

    void init(@NotNull Path configDirectory, boolean shouldLoadResources);

    static @NotNull LoadingBackgrounds createInstance() throws IllegalStateException {
        try {
            return (LoadingBackgrounds) Class
                .forName("dev.foxgirl.loadingbackgrounds.LoadingBackgroundsImpl")
                .getConstructor().newInstance();
        } catch (Throwable cause) {
            throw new IllegalStateException("Failed to create LoadingBackgrounds instance", cause);
        }
    }

}
