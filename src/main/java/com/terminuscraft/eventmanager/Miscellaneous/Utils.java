package com.terminuscraft.eventmanager.miscellaneous;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.hooks.CmiAdapter;

public class Utils {

    private static Utils instance;

    public static void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            Log.logger.info("CMI detected, hooking to its API ...");
            instance = CmiAdapter.getInstance();
        } else {
            instance = new Utils();
        }
    }

    public static Utils getInstance() {
        return instance;
    }

    public void sendToSpawn(Player player) {
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    public void refreshHolograms() {
        Log.logger.warning("Cannot reload CMI holograms, because CMI hook is not loaded!");
    }
}
