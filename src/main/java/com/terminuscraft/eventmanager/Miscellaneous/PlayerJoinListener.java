package com.terminuscraft.eventmanager.miscellaneous;

import java.io.IOException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.terminuscraft.eventmanager.eventhandler.EvmHandler;

public class PlayerJoinListener implements Listener {
    
    private final EvmHandler evmHandler;

    public PlayerJoinListener(EvmHandler handler) {
        this.evmHandler = handler;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        /* If event is set and player doesnt have bypass permission, tp him to the set event */
        System.out.println("Player " + event.getPlayer().getName() + " just joined");

        try {
            System.out.println("Current ASWM worlds: " + evmHandler.listWorlds());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
