package com.oroarmor.netherite_plus.config;

import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.ConfigEnvironment;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.impl.ConfigImpl;

import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("deprecation")
public final class FabricatedQuiltConfig {

    private static ConfigEnvironment ENV = null;

    private static ConfigEnvironment getConfigEnvironment() {
        if (ENV == null) {
            var serializer = new NightConfigSerializer<>("toml", new TomlParser(), new TomlWriter());
            com.electronwill.nightconfig.

            var env = new ConfigEnvironment(FabricLoaderImpl.INSTANCE.getConfigDir(), serializer.getFileExtension(), serializer);

            env.registerSerializer(serializer);

            ENV = env;
        }

        return ENV;
    }

    public static Config create(String family, String id, Path path, Config.Creator... creators) {
        return ConfigImpl.create(getConfigEnvironment(), family, id, path, creators);
    }

    /**
     * Creates and registers a config file
     *
     * @param family the mod owning the resulting config file
     * @param id the configs id
     * @param creators any number of {@link Config.Creator}s that can be used to configure the resulting config
     */
    public static Config create(String family, String id, Config.Creator... creators) {
        return create(family, id, Paths.get(""), creators);
    }

}