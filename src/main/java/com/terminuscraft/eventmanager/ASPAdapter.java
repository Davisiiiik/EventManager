package com.terminuscraft.eventmanager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang3.ObjectUtils.Null;
import java.util.logging.Logger;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.file.FileLoader;


public class AspAdapter {

    private final AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
    private final SlimeLoader slimeLoader;
    private SlimePropertyMap properties = new SlimePropertyMap();

    private final Logger logger;

    public AspAdapter(EventManager plugin) {
        this.logger = plugin.getLogger();

        this.slimeLoader = new FileLoader(new File("slime_worlds"));

        properties.setValue(SlimeProperties.DIFFICULTY, "peaceful");
        properties.setValue(SlimeProperties.ALLOW_MONSTERS, false);
        properties.setValue(SlimeProperties.ALLOW_ANIMALS, false);
        properties.setValue(SlimeProperties.PVP, false);
        properties.setValue(SlimeProperties.SAVE_POI, true);
        properties.setValue(SlimeProperties.SAVE_BLOCK_TICKS, true);
        properties.setValue(SlimeProperties.SAVE_FLUID_TICKS, true);
    }

    public SlimeWorldInstance loadWorld(String worldName) {
        SlimeWorld slimeWorld;
        SlimeWorldInstance slimeWorldInstance = null;

        try {
            if (this.slimeLoader.worldExists(worldName)) {
                slimeWorld = asp.readWorld(this.slimeLoader, worldName, false, new SlimePropertyMap());
            } else {
                slimeWorld = asp.createEmptyWorld(worldName, false, properties, this.slimeLoader);
            }

            slimeWorldInstance = asp.loadWorld(slimeWorld, true);
            
        } catch (IOException | CorruptedWorldException | NewerFormatException | UnknownWorldException exception) {
            logger.log(Level.SEVERE, "An exception occurred while trying to create or load world: " + worldName, exception);
        }

        return slimeWorldInstance;
    }

    public void saveWorld(SlimeWorld world) throws IOException {
        asp.saveWorld(world);
    }

    public List<String> listWorlds() throws IOException {
        return this.slimeLoader.listWorlds();
    }
}
