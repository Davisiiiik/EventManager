package com.terminuscraft.eventmanager.commands;

import java.util.List;
import java.util.Map;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
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
import com.terminuscraft.eventmanager.miscellaneous.Constants;

public class PlayerCommands {

    public final GameHandler evmHandler;

    public PlayerCommands(GameHandler handler) {
        this.evmHandler = handler;
    }

    public int teleport(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage(Lang.get("error.players_only"));
            return Constants.FAIL;
        }

        Game event = GameHandler.getCurrentEvent();
        if (event == null) {
            player.sendMessage(Lang.get("cmd.current.no_event"));
            return Command.SINGLE_SUCCESS;
        }

        SlimeWorldInstance eventWorldInstance = evmHandler.getWorldInstance(event);
        if (eventWorldInstance == null) {
            player.sendMessage(Lang.get("error.event_load_try", Map.of("event", event.getName())));
            eventWorldInstance = evmHandler.loadWorldInstance(event);

            if (eventWorldInstance == null) {
                player.sendMessage(
                    Lang.get("error.event_load_abort", Map.of("event", event.getName()))
                );
                return Constants.FAIL;
            }
        }

        World eventWorld = eventWorldInstance.getBukkitWorld();

        player.teleport(eventWorld.getSpawnLocation());
        player.sendMessage(Lang.get("cmd.tp.success", Map.of("event", event.getName())));

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

        if (GameHandler.getCurrentEvent() == null) {
            sender.sendMessage(Lang.get("cmd.current.no_event"));
        } else {
            Game event = GameHandler.getCurrentEvent();
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

        List<String> worldsList = this.evmHandler.getEventList();
        PaginationUtil.sendPaginatedList(sender, worldsList, page, "evm list");

        return Command.SINGLE_SUCCESS;
    }
}
