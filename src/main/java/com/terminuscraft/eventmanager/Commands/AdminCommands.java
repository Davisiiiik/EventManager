package com.terminuscraft.eventmanager.Commands;

import org.bukkit.command.CommandSender;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.terminuscraft.eventmanager.EventManager;

import io.papermc.paper.command.brigadier.CommandSourceStack;

public class AdminCommands {

    public int endEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage("Successfully ended the " + EventManager.getCurrentEvent() + " Event!");
        EventManager.setCurrentEvent("");

        /* Unload world */

        return Command.SINGLE_SUCCESS;
    }

    public int startEvent(CommandContext<CommandSourceStack> ctx) {
        String event = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage("Successfully started the " + event + " Event");
        EventManager.setCurrentEvent(event);

        /* Load world */
        /* Refresh CMI holograms */

        return Command.SINGLE_SUCCESS;
    }
}
