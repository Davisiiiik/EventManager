package com.terminuscraft.eventmanager.commands;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;

import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.gamehandler.Game;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;
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
        } else if (gameHandler.eventExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.create.dupe_event", Map.of("event", eventName)));
        } else {
            gameHandler.addEvent(eventName);
            sender.sendMessage(Lang.get("cmd.add.success", Map.of("event", eventName)));
        }

        return Command.SINGLE_SUCCESS;
    }

    public int createEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        if (gameHandler.eventExists(eventName)) {
            sender.sendMessage(
                Lang.get("cmd.create.dupe_event", Map.of("event", eventName))
            );
        } else if (gameHandler.worldExists(eventName)) {
            sender.sendMessage(
                Lang.get("cmd.create.dupe_world", Map.of("event", eventName))
            );
        } else {
            if (gameHandler.createEvent(eventName) == Constants.SUCCESS) {
                sender.sendMessage(
                    Lang.get("cmd.create.success", Map.of("event", eventName))
                );
            } else {
                sender.sendMessage(
                    Lang.get("cmd.create.fail", Map.of("event", eventName))
                );
            }
        }

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

        } else if (gameHandler.setCurrentEvent(eventName) == Constants.SUCCESS) {
            sender.sendMessage(Lang.get("cmd.start.success", Map.of("event", eventName)));
            
        } else {
            sender.sendMessage(Lang.get("error.event_invalid", Map.of("event", eventName)));
        }

        Log.logger.severe("===== DEBUG START =====");
        Log.logger.warning(gameHandler.getCurrentEvent().getProperties().toString());
        Log.logger.severe("====== DEBUG END ======");


        /* TODO: Load world */
        /* TODO: Refresh CMI holograms */

        return Command.SINGLE_SUCCESS;
    }

    public int endEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        Game event = gameHandler.getCurrentEvent();
        if (event == null) {
            sender.sendMessage(Lang.get("cmd.current.no_event"));
        } else {
            sender.sendMessage(Lang.get("cmd.end", Map.of("event", event.getName())));
            gameHandler.resetCurrentEvent();
            
            /* TODO: Unload world */
        }

        return Command.SINGLE_SUCCESS;
    }

    public int teleport(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage(Lang.get("error.players_only"));
            return Constants.FAIL;
        }

        Game event = gameHandler.getEvent(ctx.getArgument("event", String.class));
        if (event == null) {
            player.sendMessage(Lang.get("cmd.tp.not_found", Map.of("event", eventName)));
            return Constants.FAIL;
        }

        World eventWorld = event.getWorld();
        if (eventWorld == null) {
            player.sendMessage(Lang.get("error.event_load_try", Map.of("event", eventName)));
            eventWorld = event.getWorld();

            if (eventWorld == null) {
                player.sendMessage(Lang.get("error.event_load_abort", Map.of("event", eventName)));
                return Constants.FAIL;
            }
        }

        player.teleport(eventWorld.getSpawnLocation());
        player.sendMessage(Lang.get("cmd.tp.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        JavaPlugin.getPlugin(EventManager.class).pluginReload();
        this.gameHandler.reload();

        sender.sendMessage(Component.text(Lang.get("cmd.reload")));

        return Command.SINGLE_SUCCESS;
    }
}
