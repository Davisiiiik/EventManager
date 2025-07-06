package com.terminuscraft.eventmanager.worldprotection;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;


public class PlayerInteractListener implements Listener {

    public final GameHandler gameHandler;
    
    private final File interactablesFile;
    private static List<String> interactables;

    private final Map<UUID, Long> playersInAction = new HashMap<>();

    private enum PermType {
        BREAK,
        PLACE,
        INTERACT
    }

    public PlayerInteractListener(EventManager plugin, GameHandler handler) {
        this.gameHandler = handler;

        this.interactablesFile = new File(plugin.getDataFolder(), "interactables.yml");
        if (!interactablesFile.exists()) {
            // Extract the default from JAR: /"interactables.yml"
            plugin.saveResource("interactables.yml", false);
        }

        loadInteractables();
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        /* If the world is not event related, return */
        String worldName = event.getBlock().getWorld().getName();
        if (!gameHandler.eventExists(worldName)) {
            return;
        }

        Player player = event.getPlayer();
        String blockName = event.getBlock().getType().toString();
        if (!hasPerm(player, PermType.BREAK, blockName)) {
            event.setCancelled(true);
            player.sendMessage(Lang.pget("error.no_permission"));
            Log.logger.info("Break cancelled");     /* DEBUG */
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        /* If the world is not event related, return */
        String worldName = event.getBlock().getWorld().getName();
        if (!gameHandler.eventExists(worldName)) {
            return;
        }

        Player player = event.getPlayer();
        String blockName = event.getBlockPlaced().getType().toString();
        if (!hasPerm(player, PermType.PLACE, blockName)) {
            event.setCancelled(true);
            player.sendMessage(Lang.pget("error.no_permission"));
            Log.logger.info("Place cancelled");     /* DEBUG */
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        /* If the world is not event related, return */
        String worldName = event.getPlayer().getWorld().getName();
        if (!gameHandler.eventExists(worldName)) {
            return;
        }

        /* If the block is not defined as interactable, return */
        Block block = event.getClickedBlock();
        if (!isInteractable(block)) {
            return;
        }

        /* If the action was performed by left click, return. We dont want to block break event */
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }

        /* If the player is already in physical action, dont spam him */
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!hasPerm(player, PermType.INTERACT, block.getType().toString())) {
            event.setCancelled(true);

            if (event.getAction() == Action.PHYSICAL) {
                boolean alreadyInAction = playersInAction.containsKey(uuid);
                /* Put the new timestamp to player uuid nonetheless */
                playersInAction.put(uuid, System.currentTimeMillis());

                /* If the player is already in physical action, dont spam him */
                if (alreadyInAction) {
                    return;
                }
            }

            /* If the event is caused by offhand, return to avoid dual messages */
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return;
            }

            player.sendMessage(Lang.pget("error.no_permission"));
            Log.logger.info("Interact cancelled: " + event.getAction());     /* DEBUG */
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!playersInAction.containsKey(uuid)) {
            return;
        }
        
        /* Check if the player changed block while moving */
        if (!event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            /* Wait at least 100 ms before removing from the has map */
            if (System.currentTimeMillis() - playersInAction.get(uuid) >= 100) {
                playersInAction.remove(uuid);
            }
        }
    }

    private boolean hasPerm(Player player, PermType permType, String blockName) {
        String perm = permType.toString().toLowerCase();
        return (
            player.hasPermission("event.admin." + perm + ".*")
         || player.hasPermission("event.admin." + perm + "." + blockName)
         || player.hasPermission("event.admin.build." + blockName)
        );
    }

    private void loadInteractables() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(interactablesFile);
        interactables = config.getStringList("interactables");
    }

    public void reloadInteractables() {
        loadInteractables();
    }
    
    private boolean isInteractable(Block block) {
        if (interactables == null || block == null) {
            return false;
        }

        return interactables.stream().anyMatch(
            interactable -> interactable.equalsIgnoreCase(block.getType().toString())
        );
    }
}
