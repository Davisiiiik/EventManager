package com.terminuscraft.eventmanager.commands;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import com.terminuscraft.eventmanager.eventhandler.Event;
import com.terminuscraft.eventmanager.eventhandler.EvmHandler;
import com.terminuscraft.eventmanager.hooks.AspAdapter;
import com.terminuscraft.eventmanager.miscellaneous.Lang;

public class PlayerCommands {

    private final AspAdapter aspHandler;

    public PlayerCommands(AspAdapter handler) {
        this.aspHandler = handler;
    }

    public int teleport(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return 0;
        }

        /* TODO: EventWorldHandler should check if the event with given name exists and abort if
                 not, for now just create a new instance of event instead of loading from config */
        String event = EvmHandler.getCurrentEvent().getName();

        SlimeWorldInstance eventWorldInstance = aspHandler.getWorldInstance(event);
        if (eventWorldInstance == null) {
            player.sendMessage("§cWorld '" + event + "' is not loaded. Trying to load it ...");
            aspHandler.loadWorld(event);
            eventWorldInstance = aspHandler.getWorldInstance(event);

            if (eventWorldInstance == null) {
                player.sendMessage(
                    "§cAttempt to load the world '" + event + "' unsuccessful. Aborting ..."
                );
                return 0;
            }
        }

        World eventWorld = eventWorldInstance.getBukkitWorld();

        player.teleport(eventWorld.getSpawnLocation());
        player.sendMessage("§aTeleported to world: §f" + event);

        return Command.SINGLE_SUCCESS;
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        for (String line : Lang.getList("command.help.messages")) {
            sender.sendMessage(line);
        }

        return Command.SINGLE_SUCCESS;
    }

    public int getEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (EvmHandler.getCurrentEvent() == null) {
            sender.sendMessage(Lang.get("command.current.no_event"));
        } else {
            Event event = EvmHandler.getCurrentEvent();
            sender.sendMessage(
                Lang.get("command.current.success", Map.of("event", event.getName()))
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    public int listEvents(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        // Call ASPHandler method
        try {
            List<String> worldsList = this.aspHandler.listWorlds();
            sender.sendMessage("Current list of events:\n" + worldsList);
        } catch (IOException e) {
            sender.sendMessage(
                "Error: Couldnt retrieve list of events, try contacting Administrator!"
                );
        }

        return Command.SINGLE_SUCCESS;
    }
}
