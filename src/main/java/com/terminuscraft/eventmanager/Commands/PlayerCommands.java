package com.terminuscraft.eventmanager.commands;

import java.util.List;
import java.util.Map;

import com.Zrips.CMI.utils.SpawnUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.communication.PaginationUtil;
import com.terminuscraft.eventmanager.gamehandler.Game;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;

public class PlayerCommands {

    public final GameHandler gameHandler;

    public PlayerCommands(GameHandler handler) {
        this.gameHandler = handler;
    }

    public int join(CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player)) {
            executor.sendMessage(Lang.get("error.players_only"));
            return 0;
        }

        Game event = gameHandler.getCurrentEvent();
        if (event == null) {
            executor.sendMessage(Lang.get("cmd.current.no_event"));
            return Command.SINGLE_SUCCESS;
        }

        String eventName = event.getName();
        World eventWorld = event.getWorld();
        if (eventWorld == null) {
            executor.sendMessage(Lang.get("error.event_load_try", Map.of("event", eventName)));
            eventWorld = event.getWorld();

            if (eventWorld == null) {
                executor.sendMessage(
                    Lang.get("error.event_load_abort", Map.of("event", eventName))
                );
                return 0;
            }
        }

        executor.teleport(eventWorld.getSpawnLocation());
        executor.sendMessage(Lang.get("cmd.tp.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int leave(CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player)) {
            executor.sendMessage(Lang.get("error.players_only"));
            return 0;
        }

        Log.logger.severe("Executor's world: " + executor.getWorld().toString());   /* DEBUG? */
        Bukkit.dispatchCommand(executor, "spawn");

        return Command.SINGLE_SUCCESS;
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        Log.logger.severe(SpawnUtil.getSpawnPoint((Player) sender).toString()); /* DEBUG */

        for (String line : Lang.getList("cmd.help.messages")) {
            sender.sendMessage(line);
        }

        return Command.SINGLE_SUCCESS;
    }

    public int getEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (gameHandler.getCurrentEvent() == null) {
            sender.sendMessage(Lang.get("cmd.current.no_event"));
            return 0;
        } else {
            Game event = gameHandler.getCurrentEvent();
            sender.sendMessage(
                Lang.get("cmd.current.success", Map.of("event", event.getName()))
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    public int listEvents(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        int page = 1;

        try {
            page = IntegerArgumentType.getInteger(ctx, "page");
        } catch (IllegalArgumentException ignored) {
            // default to page 1
        }

        List<String> worldsList = this.gameHandler.getEventList();
        PaginationUtil.sendPaginatedList(sender, worldsList, page, "evm list");

        return Command.SINGLE_SUCCESS;
    }
}
