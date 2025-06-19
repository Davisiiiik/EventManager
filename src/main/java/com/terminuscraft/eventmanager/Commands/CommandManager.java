package com.terminuscraft.eventmanager.commands;

import java.util.List;
import java.io.IOException;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;

import org.bukkit.Bukkit;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class CommandManager {

    public final GameHandler evmHandler;

    private final PlayerCommands player;
    private final AdminCommands admin;

    public CommandManager(GameHandler evmHandler) {
        this.evmHandler = evmHandler;

        this.player = new PlayerCommands(evmHandler);
        this.admin = new AdminCommands(evmHandler);
    }

    /* TODO: Change from evm to event, add evm as cmi alias */
    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        /* /evm ... */
        return Commands.literal("evm").executes(player::join)
            /* /evm help */
            .then(Commands.literal("help").executes(player::help))
            
            /* /evm current */
            .then(Commands.literal("current").executes(player::getEvent))
            
            /* /evm join */
            .then(Commands.literal("join").executes(player::join))
            
            /* /evm leave */
            .then(Commands.literal("leave").executes(player::leave))
            
            /* /evm list [page] */
            .then(Commands.literal("list")
                .executes(player::listEvents)
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(player::listEvents)
                )
            )
            
            /* /evm admin ... */
            .then(Commands.literal("admin")
            
                /* /evm admin purge */
//                .then(Commands.literal("purge"))
            
                /* /evm admin tp <event> */
                .then(Commands.literal("tp")
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::teleport))
                )
            
                /* /evm admin start <event> */
                .then(Commands.literal("start") /* TODO: Add alias set */
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::startEvent))
                )
            
                /* /evm admin end <event> */
                .then(Commands.literal("end").executes(admin::endEvent)) /* TODO: Add alias stop */

                /* /evm admin add <event> */
                .then(Commands.literal("add")
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(orphanWorldsSuggestion())
                        .executes(admin::addEvent))
                )

                /* /evm admin create <event> */
                .then(Commands.literal("create")
                    .then(Commands.argument("event", StringArgumentType.word())
                        .executes(admin::createEvent))
                )

                /* /evm admin setspawn */
                .then(Commands.literal("setspawn").executes(admin::setSpawn))

                /* /evm admin remove <event> */
                .then(Commands.literal("remove")
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion()))
                )

                /* /evm admin delete <event> */
                .then(Commands.literal("delete")
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion()))
                )

//                /* /evm admin load <event> */
//                .then(Commands.literal("load")
//                    .then(Commands.argument("event", StringArgumentType.word()))
//                )
//
//                /* /evm admin unload <event> */
//                .then(Commands.literal("unload")
//                    .then(Commands.argument("event", StringArgumentType.word()))
//                )

                /* /evm admin reload */
                .then(Commands.literal("reload").executes(admin::reload))
            );
    }

    public SuggestionProvider<CommandSourceStack> eventListSuggestion() {
        return (context, builder) -> {
            List<String> eventNames = evmHandler.getEventList();
            for (String eventName : eventNames) {
                builder.suggest(eventName);
            }
             
            return builder.buildFuture(); // Return whatever suggestions were added (even if empty)
        };
    }

    public SuggestionProvider<CommandSourceStack> orphanWorldsSuggestion() {
        return (context, builder) -> {
            List<String> worldNames;
            try {
                worldNames = evmHandler.getWorldList();
                worldNames.removeAll(evmHandler.getEventList());
                for (String worldName : worldNames) {
                    builder.suggest(worldName);
                }
            } catch (IOException e) {
                // Optionally log to console
                Bukkit.getLogger().warning(
                    "Failed to get world list for tab completion: " + e.getMessage()
                );
            }
             
            return builder.buildFuture();
        };
    }
}
