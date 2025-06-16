package com.terminuscraft.eventmanager.miscellaneous;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.terminuscraft.eventmanager.EventManager;

public class Log {
    public static Logger logger = JavaPlugin.getPlugin(EventManager.class).getLogger();
}
