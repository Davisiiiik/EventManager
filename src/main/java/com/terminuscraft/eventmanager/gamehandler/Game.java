package com.terminuscraft.eventmanager.gamehandler;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.terminuscraft.eventmanager.hooks.AspAdapter;
import com.terminuscraft.eventmanager.miscellaneous.Constants;

public class Game {
    public static final AspAdapter aspAdapter = new AspAdapter();

    private final String eventName;
    private final SlimeWorld gameWorld;


    public Game(String name, SlimePropertyMap properties) {
        this.eventName = name;
        this.gameWorld = aspAdapter.readOrCreateWorld(name, properties);

        if (properties.getValue(GameProperties.LOAD_ON_STARTUP)) {
            loadWorld();
        }
    }

    public String getName() {
        return this.eventName;
    }

    public SlimePropertyMap getProperties() {
        return gameWorld.getPropertyMap();
    }

    public SlimeWorldInstance getWorldInstance() {
        return aspAdapter.loadWorld(this.gameWorld);
    }

    public World getWorld() {
        SlimeWorldInstance worldInstance = this.getWorldInstance();
        if (worldInstance != null) {
            return worldInstance.getBukkitWorld();
        } else {
            return null;
        }
    }

    public boolean hasValidWorld() {
        return (gameWorld != null);
    }

    public boolean hasLoadedWorld() {
        return aspAdapter.worldIsLoaded(gameWorld);
    }

    public int loadWorld() {
        if (getWorldInstance() == null) {
            return Constants.FAIL;
        }

        return Constants.SUCCESS;
    }

    public int saveAndUnloadWorld() {
        if (hasLoadedWorld()) {
            World eventWorld = getWorld();
            if (eventWorld == null) {
                return Constants.FAIL;
            }

            //aspAdapter.saveWorld(eventWorld);
            eventWorld.save();
            if (Bukkit.unloadWorld(eventWorld, false)) {
                return Constants.SUCCESS;
            }
        }

        return Constants.FAIL;
    }

    public void deleteWorld() {
        /* TODO */
    }
}
