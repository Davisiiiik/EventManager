package com.terminuscraft.eventmanager.communication;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.terminuscraft.eventmanager.EventManager;

public class Log {
    public static final Logger logger = JavaPlugin.getPlugin(EventManager.class).getLogger();
}
