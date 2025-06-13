package com.terminuscraft.eventmanager;

import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import com.terminuscraft.eventmanager.Commands.CommandManager;
import com.terminuscraft.eventmanager.Miscellaneous.Lang;
import com.terminuscraft.eventmanager.Miscellaneous.PlayerJoinListener;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class EventManager extends JavaPlugin {
    
    String ver = "0.0.2";

    private static String currentEvent = "";
    private AspAdapter aspHandler;

    @Override
    public void onEnable() {
        /* Make sure the config file exists */
        saveDefaultConfig();

        /* Now load the language specified in config by user or default lang.yml file */
        Lang.init(this);

        /* Hooking part, woohoo! */
        this.aspHandler = new AspAdapter(this);

        getLogger().info(Lang.get("start", Map.of("ver", ver)));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.aspHandler), this);

        /* Initialize the CommandManager */
        CommandManager commandManager = new CommandManager(this.aspHandler);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandManager.createCommand().build());
        });
    }

    @Override
    public void onDisable() {
            getLogger().info(Lang.get("end"));
    }

    public static void setCurrentEvent(String newEvent) {
        currentEvent = newEvent;
    }

    public static String getCurrentEvent() {
        return currentEvent;
    }
}
