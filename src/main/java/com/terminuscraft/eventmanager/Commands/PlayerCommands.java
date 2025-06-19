package com.terminuscraft.eventmanager.commands;

import java.util.List;
import java.util.Map;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
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
import com.terminuscraft.eventmanager.miscellaneous.Constants;

public class PlayerCommands {

    public final GameHandler gameHandler;

    public PlayerCommands(GameHandler handler) {
        this.gameHandler = handler;
    }

    public int join(CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player)) {
            executor.sendMessage(Lang.get("error.players_only"));
            return Constants.FAIL;
        }

        Game event = gameHandler.getCurrentEvent();
        if (event == null) {
            executor.sendMessage(Lang.get("cmd.current.no_event"));
            return Command.SINGLE_SUCCESS;
        }

        if (event.getWorldInstance() == null) {
            executor.sendMessage(Lang.get("error.event_load_try", Map.of("event", event.getName())));

            if (gameHandler.loadEventWorld(event) == Constants.FAIL) {
                executor.sendMessage(Lang.get("error.event_load_abort", Map.of("event", event.getName())));
                return Constants.FAIL;
            }
        }

        World eventWorld = event.getWorld();

        executor.teleport(eventWorld.getSpawnLocation());
        executor.sendMessage(Lang.get("cmd.tp.success", Map.of("event", event.getName())));

        return Command.SINGLE_SUCCESS;
    }

    public int leave(CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player)) {
            executor.sendMessage(Lang.get("error.players_only"));
            return Constants.FAIL;
        }
        Log.logger.severe("Executor's world: " + executor.getWorld().toString());
        Bukkit.dispatchCommand(executor, "spawn");

        return Command.SINGLE_SUCCESS;
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        for (String line : Lang.getList("cmd.help.messages")) {
            sender.sendMessage(line);
        }

        return Command.SINGLE_SUCCESS;
    }

    public int getEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (gameHandler.getCurrentEvent() == null) {
            sender.sendMessage(Lang.get("cmd.current.no_event"));
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
