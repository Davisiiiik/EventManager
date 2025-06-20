package com.terminuscraft.eventmanager.commands;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;

import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.gamehandler.Game;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;
import com.terminuscraft.eventmanager.hooks.CmiAdapter;
import com.terminuscraft.eventmanager.miscellaneous.Constants;

public class AdminCommands {

    public final GameHandler gameHandler;

    public AdminCommands(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public int addEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        if (!gameHandler.worldExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.add.no_world", Map.of("event", eventName)));
            return 0;
        }

        if (gameHandler.eventExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.create.dupe_event", Map.of("event", eventName)));
            return 0;

        }

        gameHandler.addEvent(eventName);
        sender.sendMessage(Lang.get("cmd.add.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int createEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        if (gameHandler.eventExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.create.dupe_event", Map.of("event", eventName)));
            return 0;
        }

        if (gameHandler.worldExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.create.dupe_world", Map.of("event", eventName)));
            return 0;
        }

        if (gameHandler.createEvent(eventName) != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("cmd.create.fail", Map.of("event", eventName)));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.create.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int setSpawn(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        /* TODO: !!! */

        return Command.SINGLE_SUCCESS;
    }

    public int startEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();
        Game currEvent = gameHandler.getCurrentEvent();

        if ((currEvent != null) && currEvent.getName().equalsIgnoreCase(eventName)) {
            sender.sendMessage(Lang.get("cmd.start.dupe", Map.of("event", currEvent.getName())));
            return 0;
        }

        Game newEvent = gameHandler.getEvent(eventName);
        if (gameHandler.setCurrentEvent(newEvent) != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("error.event_invalid", Map.of("event", eventName)));
            return 0;
        }

        if (newEvent.loadWorld() != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("error.start.fail", Map.of("event", eventName)));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.start.success", Map.of("event", eventName)));

        /* TODO: Refresh CMI holograms */

        return Command.SINGLE_SUCCESS;
    }

    public int endEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        Game event = gameHandler.getCurrentEvent();
        if (event == null) {
            sender.sendMessage(Lang.get("cmd.current.no_event"));
            return 0;
        }

        gameHandler.resetCurrentEvent();
        sender.sendMessage(Lang.get("cmd.end", Map.of("event", event.getName())));
        
        /* TODO: Unload world ? */

        return Command.SINGLE_SUCCESS;
    }

    public int teleport(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage(Lang.get("error.players_only"));
            return 0;
        }

        Game event = gameHandler.getEvent(ctx.getArgument("event", String.class));
        if (event == null) {
            player.sendMessage(Lang.get("cmd.tp.not_found", Map.of("event", eventName)));
            return 0;
        }

        World eventWorld = event.getWorld();
        if (eventWorld == null) {
            player.sendMessage(Lang.get("error.event_load_try", Map.of("event", eventName)));
            eventWorld = event.getWorld();

            if (eventWorld == null) {
                player.sendMessage(Lang.get("error.event_load_abort", Map.of("event", eventName)));
                return 0;
            }
        }

        player.teleport(eventWorld.getSpawnLocation());
        player.sendMessage(Lang.get("cmd.tp.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int unloadEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        Game event = gameHandler.getEvent(eventName);
        if (event == null) {
            sender.sendMessage(Lang.get("error.event_invalid"));
            return 0;
        }

        for (Player player : event.getWorld().getPlayers()) {
            //player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            CmiAdapter.sendToSpawn(player);
            player.sendMessage(Lang.get("system.unload_tp", Map.of("event", eventName)));
        }

        if (event.saveAndUnloadWorld() != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("cmd.unload.fail", Map.of("event", eventName)));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.unload.success", Map.of("event", eventName)));

        /*for (Player player : event.getWorld().getPlayers()) {
            Bukkit.dispatchCommand(player, "spawn");
            player.sendMessage(Lang.get("system.unload_tp", Map.of("event", eventName)));
        }

        // Delay unload logic by 2 ticks (approx. 100ms)
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(EventManager.class), () -> {
            if (event.saveAndUnloadWorld() != Constants.SUCCESS) {
                sender.sendMessage(Lang.get("cmd.unload.fail", Map.of("event", eventName)));
            } else {
                sender.sendMessage(Lang.get("cmd.unload.success", Map.of("event", eventName)));
            }
        }, 2L);*/

        return Command.SINGLE_SUCCESS;
    }

    public int saveEvents(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        gameHandler.saveEventConfigs();

        sender.sendMessage(Component.text(Lang.get("cmd.save_events")));

        return Command.SINGLE_SUCCESS;
    }

    public int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        JavaPlugin.getPlugin(EventManager.class).pluginReload();

        sender.sendMessage(Component.text(Lang.get("cmd.reload")));

        return Command.SINGLE_SUCCESS;
    }
}
