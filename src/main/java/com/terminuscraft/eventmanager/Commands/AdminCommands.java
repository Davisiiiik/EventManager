package com.terminuscraft.eventmanager.commands;

import java.io.IOException;
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
import com.terminuscraft.eventmanager.gamehandler.Game;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;
import com.terminuscraft.eventmanager.miscellaneous.Constants;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;

public class AdminCommands {

    public final GameHandler evmHandler;

    public AdminCommands(GameHandler evmHandler) {
        this.evmHandler = evmHandler;
    }

    public int addEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        boolean worldExists;

        try {
            worldExists = evmHandler.worldExists(eventName);
        } catch (IOException e) {
            worldExists = false;
        }

        if (!worldExists) {
            sender.sendMessage(Lang.get("cmd.add.no_world", Map.of("event", eventName)));
        } else if (evmHandler.getEventList().contains(eventName)) {
            sender.sendMessage(Lang.get("cmd.create.dupe", Map.of("event", eventName)));
        } else {
            evmHandler.addEvent(eventName);
            sender.sendMessage(Lang.get("cmd.add.success", Map.of("event", eventName)));
        }

        return Command.SINGLE_SUCCESS;
    }

    public int createEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        if (evmHandler.eventExists(eventName)) {
            sender.sendMessage(
                Lang.get("cmd.create.dupe", Map.of("event", eventName))
            );
        } else {
            if (evmHandler.createEvent(eventName) == Constants.SUCCESS) {
                sender.sendMessage(
                    Lang.get("cmd.create.success", Map.of("event", eventName))
                );
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    public int startEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        if (evmHandler.eventExists(eventName)) {
            GameHandler.setCurrentEvent(evmHandler.getEvent(eventName));
            sender.sendMessage(Lang.get("cmd.start", Map.of("event", eventName)));
        } else {
            sender.sendMessage(Lang.get("error.event_invalid", Map.of("event", eventName)));
        }

        /* TODO: Load world */
        /* TODO: Refresh CMI holograms */

        return Command.SINGLE_SUCCESS;
    }

    public int endEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        Game event = GameHandler.getCurrentEvent();
        if (event == null) {
            sender.sendMessage(Lang.get("cmd.current.no_event"));
        } else {
            sender.sendMessage(Lang.get("cmd.end", Map.of("event", event.getName())));
            GameHandler.setCurrentEvent(null);
            
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

        Game event = evmHandler.getEvent(ctx.getArgument("event", String.class));
        if (event == null) {
            player.sendMessage(Lang.get("cmd.tp.not_found", Map.of("event", eventName)));
            return Constants.FAIL;
        }

        SlimeWorldInstance eventWorldInstance = evmHandler.getWorldInstance(event);
        if (eventWorldInstance == null) {
            player.sendMessage(Lang.get("error.event_load_try", Map.of("event", eventName)));
            eventWorldInstance = evmHandler.loadWorldInstance(event);

            if (eventWorldInstance == null) {
                player.sendMessage(Lang.get("error.event_load_abort", Map.of("event", eventName)));
                return Constants.FAIL;
            }
        }

        World eventWorld = eventWorldInstance.getBukkitWorld();

        player.teleport(eventWorld.getSpawnLocation());
        player.sendMessage(Lang.get("cmd.tp.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        JavaPlugin.getPlugin(EventManager.class).pluginReload();
        this.evmHandler.reload();

        sender.sendMessage(Component.text(Lang.get("cmd.reload")));

        return Command.SINGLE_SUCCESS;
    }
}
