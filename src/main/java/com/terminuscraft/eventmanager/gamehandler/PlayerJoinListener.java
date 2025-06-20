package com.terminuscraft.eventmanager.gamehandler;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;

public class PlayerJoinListener implements Listener {
    
    public final GameHandler gameHandler;

    public PlayerJoinListener(GameHandler handler) {
        this.gameHandler = handler;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Game currentEvent = gameHandler.getCurrentEvent();
        if (currentEvent != null) {
            Player player = event.getPlayer();

            /* TODO: Check if player has permission to bypass auto-tp */
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
            /* TODO: Teleport to spawn (PlayerCommands.leave?) */
        }

    }
}
