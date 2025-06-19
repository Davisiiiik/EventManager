package com.terminuscraft.eventmanager.gamehandler;

import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimeProperty;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.api.world.properties.type.SlimePropertyBoolean;
import com.infernalsuite.asp.api.world.properties.type.SlimePropertyFloat;
import com.infernalsuite.asp.api.world.properties.type.SlimePropertyInt;
import com.infernalsuite.asp.api.world.properties.type.SlimePropertyString;
import com.terminuscraft.eventmanager.communication.Log;

public class GameProperties extends SlimeProperties {

    /**
     * Whether Game World should load on server startup
     */
    public static final SlimePropertyBoolean LOAD_ON_STARTUP = SlimePropertyBoolean.create("loadOnStartup", false);

    private static List<SlimeProperty<?, ?>> getSlimePropertyList () {
        return Arrays.asList(
            SPAWN_X,
            SPAWN_Y,
            SPAWN_Z,
            SPAWN_YAW,
            DIFFICULTY,
            ALLOW_MONSTERS,
            ALLOW_ANIMALS,
            DRAGON_BATTLE,
            PVP,
            ENVIRONMENT,
            WORLD_TYPE,
            DEFAULT_BIOME,
            LOAD_ON_STARTUP,
            SAVE_BLOCK_TICKS,
            SAVE_FLUID_TICKS,
            SAVE_POI
        );
    }

    public static SlimePropertyMap getDefaultMap (FileConfiguration config) {
        SlimePropertyMap properties = new SlimePropertyMap();

        getSlimePropertyList().forEach((property) -> {
            String key = "default-cfg." + property.getKey();
            Log.logger.severe(config.get(key).toString());

            if (config.get(key) != null) {
                if (property instanceof SlimePropertyBoolean) {
                    properties.setValue(
                        (SlimePropertyBoolean)property,
                        config.getBoolean(key)
                    );
                } else if (property instanceof SlimePropertyInt) {
                    properties.setValue(
                        (SlimePropertyInt)property,
                        config.getInt(key)
                    );
                } else if (property instanceof SlimePropertyFloat) {
                    properties.setValue(
                        (SlimePropertyFloat)property,
                        (float)config.getDouble(key)
                    );
                } else {
                    properties.setValue(
                        (SlimePropertyString)property,
                        config.getString(key)
                    );
                }
            } else {
                Log.logger.warning(
                    "Couldn't find property '"+ property.getKey()
                  + "' in config, defaulting to: " + property.getDefaultValue()
                );
            }
        });

        return properties;
    }
}
