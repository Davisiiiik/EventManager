package com.terminuscraft.eventmanager.hooks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.CorruptedWorldException;
import com.infernalsuite.asp.api.exceptions.NewerFormatException;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.file.FileLoader;
import com.terminuscraft.eventmanager.communication.Log;


public class AspAdapter {

    private final AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
    private final SlimeLoader slimeLoader;

    public AspAdapter() {
        this.slimeLoader = new FileLoader(new File("slime_worlds"));
    }

    public SlimeWorld readOrCreateWorld(String worldName, SlimePropertyMap properties) {
        SlimeWorld slimeWorld = null;

        try {
            if (this.slimeLoader.worldExists(worldName)) {
                slimeWorld = asp.readWorld(this.slimeLoader, worldName, false, properties);
            } else {
                Log.logger.log(
                    Level.WARNING,
                    "An exception occurred while trying to load the world \"" + worldName
                    + "\", world with this name does NOT exist! Creating one just for you ..."
                );
                slimeWorld = asp.createEmptyWorld(worldName, false, properties, this.slimeLoader);
            }
        } catch (
            IOException | CorruptedWorldException | NewerFormatException | UnknownWorldException e
        ) {
            Log.logger.log(
                Level.SEVERE,
                "An exception occurred while trying to read or create world: " + worldName, e
            );
        }

        return slimeWorld;
    }

    public SlimeWorldInstance loadWorld(SlimeWorld slimeWorld) {
        SlimeWorldInstance slimeWorldInstance;

        if (asp.worldLoaded(slimeWorld)) {
            slimeWorldInstance = asp.getLoadedWorld(slimeWorld.getName());
        } else {
            slimeWorldInstance = asp.loadWorld(slimeWorld, true);
        }

        return slimeWorldInstance;
    }

    public SlimePropertyMap getWorldProperties(String worldName) {
        try {
            SlimeWorld slimeWorld = asp.readWorld(
                this.slimeLoader, worldName, false, new SlimePropertyMap()
            );
            return slimeWorld.getPropertyMap();
        } catch (
            IOException
          | CorruptedWorldException
          | NewerFormatException
          | UnknownWorldException e) {
            Log.logger.log(
                Level.SEVERE,
                "An exception occurred while trying to load the world: " + worldName, e
            );
        }

        return null;
    }

    public void saveWorld(SlimeWorld world) throws IOException {
        asp.saveWorld(world);
    }

    public List<String> listWorlds() throws IOException {
        return this.slimeLoader.listWorlds();
    }

    public boolean worldExists(String worldName) {
        try {
            return this.slimeLoader.worldExists(worldName);
        } catch (Exception e) {
            return false;
        }
    }

    public SlimeWorldInstance getLoadedWorld(String worldName) {
        return asp.getLoadedWorld(worldName);
    }

    public List<SlimeWorldInstance> getLoadedWorldList() {
        return asp.getLoadedWorlds();
    }

    public boolean worldIsLoaded(SlimeWorld world) {
        return asp.worldLoaded(world);
    }

    public void deleteWorld(String worldName) throws UnknownWorldException, IOException {
        this.slimeLoader.deleteWorld(worldName);
    }
}
