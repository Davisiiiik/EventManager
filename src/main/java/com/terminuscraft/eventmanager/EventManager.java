package com.terminuscraft.eventmanager;

import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import com.terminuscraft.eventmanager.commands.CommandManager;
import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;
import com.terminuscraft.eventmanager.gamehandler.PlayerJoinListener;
import com.terminuscraft.eventmanager.miscellaneous.Utils;
import com.terminuscraft.eventmanager.worldprotection.PlayerInteractListener;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class EventManager extends JavaPlugin {
    private GameHandler gameHandler;
    private PlayerInteractListener worldProtect;

    @Override
    public void onEnable() {
        /* Make sure the config file exists */
        saveDefaultConfig();

        /* Now load the language specified in config by user or default lang.yml file */
        Lang.init(this);

        /* Prepare event handling */
        this.gameHandler = new GameHandler(this);
        Utils.init();

        /* Initialize the CommandManager */
        CommandManager commandManager = new CommandManager(this.gameHandler);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandManager.createCommand().build());
        });

        /* Register EventListeners */
        getServer().getPluginManager().registerEvents(
            new PlayerJoinListener(this.gameHandler), this
        );

        this.worldProtect = new PlayerInteractListener(this, this.gameHandler);
        getServer().getPluginManager().registerEvents(worldProtect, this);

        getLogger().info(Lang.get("console.start"));
    }

    @Override
    public void onDisable() {
        getLogger().info(Lang.get("console.end"));
    }

    public void pluginReload() {
        this.reloadConfig();
        Lang.reloadLanguage();
        this.gameHandler.reloadEvents();
        this.worldProtect.reloadInteractables();
    }
}
