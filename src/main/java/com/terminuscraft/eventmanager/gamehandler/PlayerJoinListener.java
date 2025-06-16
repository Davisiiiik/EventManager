package com.terminuscraft.eventmanager.gamehandler;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;

public class PlayerJoinListener implements Listener {
    
    private final GameHandler evmHandler;

    public PlayerJoinListener(GameHandler handler) {
        this.evmHandler = handler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        /* If event is set and player doesnt have bypass permission, tp him to the set event */
        System.out.println("Player " + event.getPlayer().getName() + " just joined");

        Game currentEvent = GameHandler.getCurrentEvent();
        if (currentEvent != null) {
            Player player = event.getPlayer();
            SlimeWorldInstance eventWorldInstance = evmHandler.getWorldInstance(currentEvent);

            if (eventWorldInstance == null) {
                eventWorldInstance = evmHandler.loadWorldInstance(currentEvent);

                if (eventWorldInstance == null) {
                    Log.logger.severe(
                        "An exception occurred while trying to teleport player on join to the \""
                        + currentEvent.getName() + "\" event. Its world couldn't be loaded!");
                    return;
                }
            }

            player.teleport(eventWorldInstance.getBukkitWorld().getSpawnLocation());
            player.sendMessage(
                Lang.get("system.auto_tp", Map.of("event", currentEvent.getName()))
            );
        }

    }
}
