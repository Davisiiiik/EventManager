package com.terminuscraft.eventmanager.gamehandler;

import org.bukkit.World;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.terminuscraft.eventmanager.hooks.AspAdapter;

public class Game {
    private final String eventName;

    /* CANNOT DO IT LIKE THAT!!! NEED LIVE INSTANCE, have aspLoader as static? */
    private SlimeWorldInstance worldInstance = null;
    public SlimePropertyMap properties;     /* Doesnt need, just load on startup */

    final static AspAdapter aspAdapter = new AspAdapter();


    public Game(String name, SlimePropertyMap properties) {
        this(name, properties, null);
    }

    public Game(String name, SlimePropertyMap properties, SlimeWorldInstance worldInstance) {
        this.eventName = name;
        this.properties = properties;
        this.worldInstance = worldInstance;
    }

    public String getName() {
        return this.eventName;
    }

    public void setWorldInstance(SlimeWorldInstance worldInstance) {
        this.worldInstance = worldInstance;
    }

    public SlimeWorldInstance getWorldInstance() {
        return this.worldInstance;
    }

    public World getWorld() {
        return this.worldInstance.getBukkitWorld();
    }
}
