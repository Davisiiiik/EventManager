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

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        /* /evm ... */
        return Commands.literal("event")
                .requires(sender -> sender.getSender().hasPermission("event.player.join"))
                .executes(player::join)

            /* /evm help */
            .then(Commands.literal("help")
                .requires(sender -> sender.getSender().hasPermission("event.player.help"))
                .executes(player::help))
            
            /* /evm current */
            .then(Commands.literal("current")
                .requires(sender -> sender.getSender().hasPermission("event.player.current"))
                .executes(player::getEvent))
            
            /* /evm join */
            .then(Commands.literal("join")
                .requires(sender -> sender.getSender().hasPermission("event.player.join"))
                .executes(player::join))
            
            /* /evm leave */
            .then(Commands.literal("leave")
                .requires(sender -> sender.getSender().hasPermission("event.player.leave"))
                .executes(player::leave))
            
            /* /evm list [page] */
            .then(Commands.literal("list")
                .requires(sender -> sender.getSender().hasPermission("event.player.list"))
                .executes(player::listEvents)
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(player::listEvents)
                )
            )

            /* /evm admin ... */
            .then(Commands.literal("admin")
            /* TODO: Solve, /event admin is visible to regular players */

                /* /evm admin tp <event> */
                .then(Commands.literal("tp")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.tp"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::teleport))
                )
            
                /* /evm admin start <event> */
                .then(Commands.literal("start")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.start"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::startEvent))
                )   /* TODO: Add alias set, or use set to change world properties? */
            
                /* /evm admin end; /evm admin stop */
                .then(Commands.literal("end")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.stop"))
                    .executes(admin::endEvent))
                .then(Commands.literal("stop")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.stop"))
                    .executes(admin::endEvent))

                /* /evm admin add <event> */
                .then(Commands.literal("add")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.add"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(orphanWorldsSuggestion())
                        .executes(admin::addEvent))
                )

                /* /evm admin create <event> */
                .then(Commands.literal("create")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.create"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .executes(admin::createEvent))
                )

                /* /evm admin setspawn */
                .then(Commands.literal("setspawn")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.setspawn"))
                    .executes(admin::setSpawn))

                /* /evm admin remove <event> */
                .then(Commands.literal("remove")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.remove"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::removeEvent))
                )

                /* /evm admin delete <event> */
                .then(Commands.literal("delete")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.delete"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::deleteEvent))
                )

                /* /evm admin unload <event> */
                .then(Commands.literal("unload")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.unload"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::unloadEvent))
                )

                /* /evm admin saveEventConfigs */
                .then(Commands.literal("saveEventConfigs")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.saveconfigs"))
                    .executes(admin::saveEvents))

                /* /evm admin reload */
                .then(Commands.literal("reload")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.stop"))
                    .executes(admin::reload))
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
