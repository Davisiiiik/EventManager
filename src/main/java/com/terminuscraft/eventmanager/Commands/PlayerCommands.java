package com.terminuscraft.eventmanager.commands;

import java.io.IOException;
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

import com.terminuscraft.eventmanager.eventhandler.Event;
import com.terminuscraft.eventmanager.eventhandler.EvmHandler;
import com.terminuscraft.eventmanager.miscellaneous.Constants;
import com.terminuscraft.eventmanager.miscellaneous.Lang;
import com.terminuscraft.eventmanager.miscellaneous.PaginationUtil;

public class PlayerCommands {

    public final EvmHandler evmHandler;

    public PlayerCommands(EvmHandler handler) {
        this.evmHandler = handler;
    }

    public int teleport(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage(Lang.get("error.players_only"));
            return Constants.FAIL;
        }

        Event event = EvmHandler.getCurrentEvent();
        if (event == null) {
            player.sendMessage(Lang.get("cmd.current.no_event"));
            return Command.SINGLE_SUCCESS;
        }

        String eventName = event.getName();

        SlimeWorldInstance eventWorldInstance = evmHandler.getWorldInstance(eventName);
        if (eventWorldInstance == null) {
            player.sendMessage(Lang.get("error.event_load_try", Map.of("event", eventName)));
            evmHandler.loadWorld(eventName);
            eventWorldInstance = evmHandler.getWorldInstance(eventName);

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

    public int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        for (String line : Lang.getList("cmd.help.messages")) {
            sender.sendMessage(line);
        }

        return Command.SINGLE_SUCCESS;
    }

    public int getEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (EvmHandler.getCurrentEvent() == null) {
            sender.sendMessage(Lang.get("cmd.current.no_event"));
        } else {
            Event event = EvmHandler.getCurrentEvent();
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
