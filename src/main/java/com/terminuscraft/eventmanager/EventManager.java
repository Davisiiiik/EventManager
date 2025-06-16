package com.terminuscraft.eventmanager;

import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import com.terminuscraft.eventmanager.commands.CommandManager;
import com.terminuscraft.eventmanager.eventhandler.EvmHandler;
import com.terminuscraft.eventmanager.miscellaneous.Lang;
import com.terminuscraft.eventmanager.miscellaneous.PlayerJoinListener;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class EventManager extends JavaPlugin {
    private EvmHandler evmHandler;

    @Override
    public void onEnable() {
        /* Make sure the config file exists */
        saveDefaultConfig();

        /* Now load the language specified in config by user or default lang.yml file */
        Lang.init(this);

        /* Prepare event handling */
        this.evmHandler = new EvmHandler(this);

        /* Initialize the CommandManager */
        CommandManager commandManager = new CommandManager(this.evmHandler);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandManager.createCommand().build());
        });

        getLogger().info(Lang.get("console.start"));
        getServer().getPluginManager().registerEvents(
            new PlayerJoinListener(this.evmHandler), this
        );
    }

    @Override
    public void onDisable() {
        getLogger().info(Lang.get("console.end"));
    }

    public void pluginReload() {
        this.reloadConfig();
        Lang.reload();
        evmHandler.reload();
    }
}
