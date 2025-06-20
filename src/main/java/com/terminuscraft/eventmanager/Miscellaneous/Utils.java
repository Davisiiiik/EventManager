package com.terminuscraft.eventmanager.miscellaneous;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.hooks.CmiAdapter;

public class Utils {

    public static Utils instance;

    public static void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            Log.logger.info("CMI detected, hooking to its API ...");
            instance = CmiAdapter.getInstance();
        } else {
            instance = new Utils();
        }
    }

    public void sendToSpawn(Player player) {
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
    }
}
