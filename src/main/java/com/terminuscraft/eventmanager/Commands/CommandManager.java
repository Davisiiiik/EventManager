package com.terminuscraft.eventmanager.commands;

import java.util.List;
import java.io.IOException;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.terminuscraft.eventmanager.hooks.AspAdapter;

import org.bukkit.Bukkit;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class CommandManager {

    private final AspAdapter aspHandler;

    private final PlayerCommands player;
    private final AdminCommands admin;

    public CommandManager(AspAdapter aspHandler) {
        this.aspHandler = aspHandler;

        this.player = new PlayerCommands(aspHandler);
        this.admin = new AdminCommands();
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        /* /evm ... */
        return Commands.literal("evm")
            /* /evm help */
            .then(Commands.literal("help").executes(player::help))
            
            /* /evm current */
            .then(Commands.literal("current").executes(player::getEvent))
            
            /* /evm list */
            .then(Commands.literal("list").executes(player::listEvents))
            
            /* /evm tp <event> */
            .then(Commands.literal("tp")
                .then(Commands.argument("event", StringArgumentType.word())
                    .suggests(eventListSuggestion())
                    .executes(player::teleport)
                )
            )
            
            /* /evm admin ... */
            .then(Commands.literal("admin")
            
                /* /evm admin purge */
                .then(Commands.literal("purge"))
            
                /* /evm admin start <event> */
                .then(Commands.literal("start")
                    .then(Commands.argument("event", StringArgumentType.word())
                        .executes(admin::startEvent))
                )
            
                /* /evm admin end <event> */
                .then(Commands.literal("end").executes(admin::endEvent))

                /* /evm admin add <event> */
                .then(Commands.literal("add")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )

                /* /evm admin remove <event> */
                .then(Commands.literal("remove")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )

                /* /evm admin delete <event> */
                .then(Commands.literal("delete")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )

                /* /evm admin load <event> */
                .then(Commands.literal("load")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )

                /* /evm admin unload <event> */
                .then(Commands.literal("unload")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )
            );
    }

    public SuggestionProvider<CommandSourceStack> eventListSuggestion() {
        return (context, builder) -> {
            try {
                List<String> worldNames = aspHandler.listWorlds();
                for (String worldName : worldNames) {
                    builder.suggest(worldName);
                }
            } catch (IOException e) {
                // Optionally log to console
                Bukkit.getLogger().warning(
                    "Failed to get world list for tab completion: " + e.getMessage()
                );
            }
            return builder.buildFuture(); // Return whatever suggestions were added (even if empty)
        };
    }
}
