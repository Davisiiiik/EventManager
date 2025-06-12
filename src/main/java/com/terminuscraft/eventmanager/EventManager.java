package com.terminuscraft.eventmanager;

import java.io.IOException;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class EventManager extends JavaPlugin {
    
    String ver = "0.0.1";

    private static String currentEvent = "";
    
    private AspAdapter aspHandler;


    @Override
    public void onEnable() {
        this.aspHandler = new AspAdapter(this);

        CommandManager commandManager = new CommandManager(this.aspHandler);

        getLogger().info("TerminusCraft Event Manager v" + ver + " successfully loaded!");
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.aspHandler), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandManager.createCommand().build());
        });

        //saveDefaultConfig();
    }

    @Override
    public void onDisable() {
            getLogger().info("TerminusCraft Event Manager successfully unloaded!");
    }

    public static void setCurrentEvent(String newEvent) {
        currentEvent = newEvent;
    }

    public static String getCurrentEvent() {
        return currentEvent;
    }
}
