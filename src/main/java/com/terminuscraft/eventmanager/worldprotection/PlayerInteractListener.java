package com.terminuscraft.eventmanager.worldprotection;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;


public class PlayerInteractListener implements Listener {
    
    public final GameHandler gameHandler;

    public PlayerInteractListener(GameHandler handler) {
        this.gameHandler = handler;
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(!hasBuildPermission(event.getPlayer(), event));
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(!hasBuildPermission(event.getPlayer(), event));
    }

    private boolean hasBuildPermission(Player player, BlockEvent event) {
        String worldName = event.getBlock().getWorld().getName();
                        
        /* If the world is not event related, return */
        if (!gameHandler.eventExists(worldName)) return true;

        boolean hasPerm = (player.hasPermission("event.admin.build.*")
                        || player.hasPermission("event.admin.build." + worldName.toLowerCase()));

        if (!hasPerm) {
            player.sendMessage(Lang.pget("error.no_permission"));
        }
        
        return hasPerm;
    }

    /*private boolean hasBuildPermission(Player player, String worldName) {
        boolean result = (player.hasPermission("event.admin.build.*")
                      || player.hasPermission("event.admin.build." + worldName.toLowerCase()));

        if (!result) {
            player.sendMessage(Lang.pget("error.no_permission"));
        }

        return result;
    }*/

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        String playerName = event.getPlayer().getName();
        String action = event.getAction().toString();
        String blockType;
        String worldName = event.getPlayer().getWorld().getName();

        if (event.getClickedBlock() != null) {
            blockType = event.getClickedBlock().getType().toString();
        } else {
            blockType = "AIR";
        }

        if(event.getHand() == EquipmentSlot.OFF_HAND) {
            Log.logger.severe("OFFHAND, BABY!");
            return;
        }

        Log.logger.severe(playerName + " has " + action + " with " + blockType + " in world " + worldName + "!");
    }
}
