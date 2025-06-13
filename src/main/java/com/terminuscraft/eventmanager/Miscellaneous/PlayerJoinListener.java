package com.terminuscraft.eventmanager.miscellaneous;

import java.io.IOException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.terminuscraft.eventmanager.hooks.AspAdapter;

public class PlayerJoinListener implements Listener {
    
    private final AspAdapter aspHandler;

    public PlayerJoinListener(AspAdapter handler) {
        this.aspHandler = handler;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        /* If event is set and player doesnt have bypass permission, tp him to the set event */
        System.out.println("Player " + event.getPlayer().getName() + " just joined");

        try {
            System.out.println("Current ASWM worlds: " + aspHandler.listWorlds());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
