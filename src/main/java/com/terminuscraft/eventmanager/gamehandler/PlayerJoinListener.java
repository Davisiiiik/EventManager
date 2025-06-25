package com.terminuscraft.eventmanager.gamehandler;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.miscellaneous.Utils;

public class PlayerJoinListener implements Listener {
    
    public final GameHandler gameHandler;

    public PlayerJoinListener(GameHandler handler) {
        this.gameHandler = handler;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Game currentEvent = gameHandler.getCurrentEvent();
        Player player = event.getPlayer();

        if (player.hasPermission("event.admin.bypass")) {
            return;
        }

        if (currentEvent != null) {
            World eventWorld = currentEvent.getWorld();
            if (eventWorld == null) {
                eventWorld = currentEvent.getWorld();

                if (eventWorld == null) {
                    Log.logger.severe(
                        "An exception occurred while trying to teleport player on join to the \""
                        + currentEvent.getName() + "\" event. Its world couldn't be loaded!");
                    return;
                }
            }

            player.teleport(currentEvent.getWorld().getSpawnLocation());
            player.sendMessage(
                Lang.get("system.auto_tp", Map.of("event", currentEvent.getName()))
            );
        } else {
            Utils.getInstance().sendToSpawn(player);
        }

    }
}
