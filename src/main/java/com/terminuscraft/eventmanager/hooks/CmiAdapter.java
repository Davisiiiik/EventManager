package com.terminuscraft.eventmanager.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.terminuscraft.eventmanager.communication.Log;

import net.Zrips.CMILib.Container.CMILocation;
import com.Zrips.CMI.utils.SpawnUtil;

public class CmiAdapter {

    private static boolean cmiLoaded = false;

    public static void init() {
        cmiLoaded = Bukkit.getPluginManager().isPluginEnabled("CMI");
        if (cmiLoaded) {
            Log.logger.info("CMI detected, using CMI API for teleportation.");
        }
    }

    /*public CmiAdapter() {
        cmiLoaded = Bukkit.getPluginManager().isPluginEnabled("CMI");
        if (cmiLoaded) {
            Log.logger.info("CMI detected, using CMI API for teleportation.");
        }
    }*/

    public static void sendToSpawn(Player player) {
        if (cmiLoaded) {
            CMILocation spawn = SpawnUtil.getSpawnPoint(player);
            player.teleport(spawn);
        } else {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }
}
