package com.terminuscraft.eventmanager.commands;

import java.util.List;
import java.util.Map;
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

    /* TODO:G0: Rewrite command structure, so a command is its own class with literal, permission.
     * argument, function to be executed, description for help command, children commands (with
     * recursion to generate desired command tree structure) and aliases
     * (to register the same command under the aliase as its literal with the same setting)
     */

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        /* /evm ... */
        return Commands.literal("event")
                .requires(sender -> sender.getSender().hasPermission("event.player.join"))
                .executes(player::join)
            
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
                    .executes(player::listEvents))
            )

            /* /evm help */
            .then(Commands.literal("help")
                .requires(sender -> sender.getSender().hasPermission("event.player.help"))
                .executes(player::help)
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(player::help))
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
            
                /* /evm admin start <event>; /evm admin set <event> */
                .then(Commands.literal("start")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.start"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::startEvent))
                )
                .then(Commands.literal("set")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.start"))
                    .then(Commands.argument("event", StringArgumentType.word())
                        .suggests(eventListSuggestion())
                        .executes(admin::startEvent))
                )
            
                /* /evm admin stop; /evm admin end */
                .then(Commands.literal("stop")
                    .requires(sender -> sender.getSender().hasPermission("event.admin.stop"))
                    .executes(admin::endEvent))
                .then(Commands.literal("end")
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
                .then(Commands.literal("setSpawn")
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
            List<String> eventNames = evmHandler.getEventNameList();
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
                worldNames.removeAll(evmHandler.getEventNameList());
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

    /* TODO:G0: this is highly temporary, before the command handling refactoring */
    public static Map<String, String> getCommandDict() {
        return Map.ofEntries(
            Map.entry("",                           "player.join"),
            Map.entry(" current",                   "player.current"),
            Map.entry(" join",                      "player.join"),
            Map.entry(" leave",                     "player.leave"),
            Map.entry(" list",                      "player.list"),
            Map.entry(" help",                      "player.help"),
            Map.entry(" admin tp",                  "admin.tp"),
            Map.entry(" admin start",               "admin.start"),
            Map.entry(" admin set",                 "admin.start"),
            Map.entry(" admin stop",                "admin.stop"),
            Map.entry(" admin end",                 "admin.stop"),
            Map.entry(" admin add",                 "admin.add"),
            Map.entry(" admin create",              "admin.create"),
            Map.entry(" admin setSpawn",            "admin.setspawn"),
            Map.entry(" admin remove",              "admin.remove"),
            Map.entry(" admin delete",              "admin.delete"),
            Map.entry(" admin unload",              "admin.unload"),
            Map.entry(" admin saveEventConfigs",    "admin.saveconfigs"),
            Map.entry(" admin reload",              "admin.reload")
        );
    }
}
