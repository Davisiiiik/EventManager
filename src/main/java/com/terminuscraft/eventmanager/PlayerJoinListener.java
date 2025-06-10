package com.terminuscraft.eventmanager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;

public class PlayerJoinListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        /* If event is set and player doesnt have bypass permission, tp him to the set event */
        System.out.println("Player " + event.getPlayer().getName() + " just joined");

        AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
        System.out.println("Current ASWM worlds: " + asp.getLoadedWorlds());
    }
}
