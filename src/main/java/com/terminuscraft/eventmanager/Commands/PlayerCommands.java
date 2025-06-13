package com.terminuscraft.eventmanager.commands;

import java.io.IOException;
import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.hooks.AspAdapter;
import com.terminuscraft.eventmanager.miscellaneous.Lang;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;

public class PlayerCommands {

    private final AspAdapter aspHandler;

    public PlayerCommands(AspAdapter handler) {
        this.aspHandler = handler;
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

        if (EventManager.getCurrentEvent().isEmpty()) {
            sender.sendMessage("Právě neprobíhá žádný event!");
        } else {
            sender.sendMessage("Current event is " + EventManager.getCurrentEvent());
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

    public int teleport(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return 0;
        }

        String event = ctx.getArgument("event", String.class);

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
}
