package com.terminuscraft.eventmanager.gamehandler;

import org.bukkit.World;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.terminuscraft.eventmanager.hooks.AspAdapter;

public class Game {
    public final static AspAdapter aspAdapter = new AspAdapter();

    private final String eventName;
    private final SlimeWorld gameWorld;


    public Game(String name, SlimePropertyMap properties) {
        this.eventName = name;
        this.gameWorld = aspAdapter.readOrCreateWorld(name, properties);
        
        boolean loadOnStartup = properties.getValue(GameProperties.LOAD_ON_STARTUP);
        /* TODO: Implement load on startup */
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
}
