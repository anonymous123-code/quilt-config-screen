package io.github.anonymous123_code.quilt_config_screen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.Configs;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class QuiltConfigScreen implements ModMenuApi {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod name as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("Quilt Config Screen");

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, QuiltConfigConfigScreen.Factory> screens = new HashMap<>();
        for (Config config : Configs.getAll()) {
            Optional<ModContainer> modContainer = tryGetModContainer(config);
            if (modContainer.isPresent()) {
                QuiltConfigConfigScreen.Factory factory = screens.computeIfAbsent(modContainer.get().metadata().id(), s -> new QuiltConfigConfigScreen.Factory(modContainer.get().metadata().name()));
                LOGGER.warn("Adding config with id: '" + config.id() + "' and family: '" + config.family() + "' to mod '" + modContainer.get().metadata().id() + "'");
                factory.addConfig(config);
            } else {
                LOGGER.error("Config with id: '" + config.id() + "' and family: '" + config.family() + "' has no corresponding mod id");
            }
        }
        return (Map<String, ConfigScreenFactory<?>>) (Object) screens;
    }

    public Optional<ModContainer> tryGetModContainer(Config config) {
        String family = config.family();
        if (!family.isEmpty()) {
            for (String modIdCandidate : family.split("/")) {
                Optional<ModContainer> modContainer = QuiltLoader.getModContainer(modIdCandidate);
                if (modContainer.isPresent()) {
                    return modContainer;
                }
                // This will work with qsl_registry
                modContainer = QuiltLoader.getModContainer(modIdCandidate + "_" + config.id());
                if (modContainer.isPresent()) {
                    return modContainer;
                }
            }
        }
        return QuiltLoader.getModContainer(config.id());
    }
}
