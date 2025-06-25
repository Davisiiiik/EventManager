package com.terminuscraft.eventmanager.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.PaginationUtil;
import com.terminuscraft.eventmanager.gamehandler.Game;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;
import com.terminuscraft.eventmanager.miscellaneous.Utils;

public class PlayerCommands {

    public final GameHandler gameHandler;

    public PlayerCommands(GameHandler handler) {
        this.gameHandler = handler;
    }

    public int join(CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player)) {
            executor.sendMessage(Lang.pget("error.players_only"));
            return 0;
        }

        Game event = gameHandler.getCurrentEvent();
        if (event == null) {
            executor.sendMessage(Lang.pget("cmd.current.no_event"));
            return Command.SINGLE_SUCCESS;
        }

        String eventName = event.getName();
        World eventWorld = event.getWorld();
        if (eventWorld == null) {
            executor.sendMessage(Lang.pget("error.event_load_abort", Map.of("event", eventName)));
            return 0;
        }

        executor.teleport(eventWorld.getSpawnLocation());
        executor.sendMessage(Lang.pget("cmd.tp.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int leave(CommandContext<CommandSourceStack> ctx) {
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            executor.sendMessage(Lang.pget("error.players_only"));
            return 0;
        }

        Utils.getInstance().sendToSpawn(player);

        return Command.SINGLE_SUCCESS;
    }

    public int getEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (gameHandler.getCurrentEvent() == null) {
            sender.sendMessage(Lang.pget("cmd.current.no_event"));
            return 0;
        } else {
            Game event = gameHandler.getCurrentEvent();
            sender.sendMessage(
                Lang.pget("cmd.current.success", Map.of("event", event.getName()))
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

        /* ctx.getInput() returns the command string which player typed, i.e. "event list" */
        String[] parts = ctx.getInput().split("\\s+", 3);
        String command = parts[0] + " " + parts[1];

        List<String> worldsList = new ArrayList<String>();
        List<String> loadedWorldList = gameHandler.getLoadedWorldList();
        gameHandler.getEventNameList().forEach((eventName) -> {
            String worldItem;

            if (sender.hasPermission("event.admin.verbose")) {
                String state;

                /* Using gameHandler.worldIsLoaded might be less optimal for this usecase */
                if (loadedWorldList.contains(eventName)) {
                    state = Lang.get("cmd.list.loaded");
                } else {
                    state = Lang.get("cmd.list.unloaded");
                }

                worldItem = Lang.get(
                    "cmd.list.admin_item", Map.of("event", eventName, "state", state)
                );
            } else {
                worldItem = Lang.get("cmd.list.player_item", Map.of("event", eventName));
            }

            worldsList.add(worldItem);
        });

        String headerName = Lang.get("headers.events");
        PaginationUtil.sendPaginatedList(sender, worldsList, page, command, headerName);

        return Command.SINGLE_SUCCESS;
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        int page = 1;

        try {
            page = IntegerArgumentType.getInteger(ctx, "page");
        } catch (IllegalArgumentException ignored) {
            // default to page 1
        }

        /* ctx.getInput() returns the command string which player typed, i.e. "event list" */
        String[] parts = ctx.getInput().split("\\s+", 3);
        String command = parts[0] + " " + parts[1];

        /* TODO:G0: this is highly temporary, before the command handling refactoring */
        List<String> cmdList = new ArrayList<String>();
        CommandManager.getCommandDict().forEach((cmd, langMap) -> {
            if (sender.hasPermission("event." + langMap)) {
                String desc = Lang.get("help." + langMap, Map.of("cmd", "/event" + cmd));
                cmdList.add(desc);
            }
        });

        String headerName = Lang.get("headers.commands");
        PaginationUtil.sendPaginatedList(sender, cmdList, page, command, headerName);

        return Command.SINGLE_SUCCESS;
    }
}
