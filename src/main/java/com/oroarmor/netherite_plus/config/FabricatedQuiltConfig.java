package com.oroarmor.netherite_plus.config;

import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.ConfigEnvironment;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.impl.ConfigImpl;
import org.quiltmc.config.implementor_api.ConfigFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("deprecation")
public final class FabricatedQuiltConfig {

    private static ConfigEnvironment ENV = null;

    private static ConfigEnvironment getConfigEnvironment() {
        if (ENV == null) {
            var serializer = new NightConfigSerializer<>("toml", new TomlParser(), new TomlWriter());

            var env = new ConfigEnvironment(FabricLoaderImpl.INSTANCE.getConfigDir(), serializer.getFileExtension(), serializer);

            env.registerSerializer(serializer);

            ENV = env;
        }

        return ENV;
    }

    public static <C extends ReflectiveConfig> C create(String family, String id, Path path, Config.Creator before, Class<C> configCreatorClass, Config.Creator after) {
        return ConfigFactory.create(getConfigEnvironment(), family, id, path, before, configCreatorClass, after);
    }

    public static <C extends ReflectiveConfig> C create(String family, String id, Class<C> configCreatorClass) {
        return create(family, id, Paths.get(""), builder -> {}, configCreatorClass, builder -> {});
    }
}