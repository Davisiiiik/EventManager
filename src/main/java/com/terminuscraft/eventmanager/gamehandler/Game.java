package com.terminuscraft.eventmanager.gamehandler;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.terminuscraft.eventmanager.hooks.AspAdapter;
import com.terminuscraft.eventmanager.miscellaneous.Constants;

public class Game {
    public static final AspAdapter aspAdapter = new AspAdapter();

    private final String eventName;
    private final SlimePropertyMap propertyMap;
    
    /* TODO: Redesign spawn with my own Location */

    public Game(String name, SlimePropertyMap propertyMap) {
        this.eventName = name;
        this.propertyMap = propertyMap.clone();

        if (propertyMap.getValue(GameProperties.LOAD_ON_STARTUP)) {
            loadWorld();
        }
    }

    public String getName() {
        return eventName;
    }

    public SlimePropertyMap getPropertyMap() {
        return propertyMap.clone();
    }

    public void addProperties(SlimePropertyMap newMap) {
        /* This doesn't affect live world, its must be reloaded to take an effect */
        propertyMap.merge(newMap);
    }

    public SlimeWorld getSlimeWorld() {
        return aspAdapter.readOrCreateWorld(eventName, propertyMap);
    }

    public SlimeWorldInstance getWorldInstance() {
        SlimeWorldInstance worldInstance = aspAdapter.getLoadedWorld(eventName);
        if (worldInstance == null) {
            worldInstance = aspAdapter.loadWorld(getSlimeWorld());
        }
        
        return worldInstance;
    }

    public World getWorld() {
        SlimeWorldInstance worldInstance = this.getWorldInstance();
        if (worldInstance != null) {
            return worldInstance.getBukkitWorld();
        } else {
            return null;
        }
    }

    public boolean hasExistingWorld() {
        return aspAdapter.worldExists(eventName);
    }

    public boolean hasLoadedWorld() {
        return aspAdapter.worldIsLoaded(getSlimeWorld());
    }

    public int loadWorld() {
        if (getWorldInstance() == null) {
            return Constants.FAIL;
        }

        return Constants.SUCCESS;
    }

    public int unloadWorld(boolean saveWorld) {
        if (hasLoadedWorld()) {
            World eventWorld = getWorld();
            if (eventWorld == null) {
                return Constants.FAIL;
            }

            if (saveWorld) {
                //aspAdapter.saveWorld(eventWorld);
                eventWorld.save();
            }

            if (Bukkit.unloadWorld(eventWorld, false)) {
                return Constants.SUCCESS;
            }
        }

        return Constants.FAIL;
    }

    public void deleteWorld() {
        try {
            aspAdapter.deleteWorld(eventName);
        } catch (IOException | UnknownWorldException e) {
            /* Either the world is already deleted or this shouldnt happen */
        }
    }
    
    public Game copy() {
        return new Game(this.eventName, this.propertyMap.clone());
    }
}
