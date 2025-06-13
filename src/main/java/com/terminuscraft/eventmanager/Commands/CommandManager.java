package com.terminuscraft.eventmanager.Commands;

import java.util.List;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.terminuscraft.eventmanager.AspAdapter;
import com.terminuscraft.eventmanager.Miscellaneous.Lang;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class CommandManager {

    private final AspAdapter aspHandler;
    private final AdminCommands admin;

    public CommandManager(AspAdapter handler) {
        this.aspHandler = handler;
        this.admin = new AdminCommands();
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        /* /evm ... */
        return Commands.literal("evm")
            /* /evm help */
            .then(Commands.literal("help").executes(this::help))
            
            /* /evm current */
            .then(Commands.literal("current") .executes(admin::getEvent) )
            
            /* /evm list */
            .then(Commands.literal("list") .executes(this::listEvents) )
            
            /* /evm tp <event> */
            .then(Commands.literal("tp")
                .then(Commands.argument("event", StringArgumentType.word())
                    .suggests(tpSuggestion())
                    .executes(this::teleport))
            )
            
            /* /evm admin ... */
            .then(Commands.literal("admin")
            
                /* /evm admin purge */
                .then(Commands.literal("purge"))
            
                /* /evm admin get <event> */
                .then(Commands.literal("get").executes(admin::getEvent))
            
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

    private int help(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        for (String line : Lang.getList("command.help.messages")) {
            sender.sendMessage(line);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int listEvents(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        // Call ASPHandler method
        try {
            List<String> worldsList = this.aspHandler.listWorlds();
            sender.sendMessage("Current list of events:\n" + worldsList);
        } catch (IOException e) {
            sender.sendMessage("Error: Couldnt retrieve list of events, try contacting Administrator!");
        }

        return Command.SINGLE_SUCCESS;
    }

    private SuggestionProvider<CommandSourceStack> tpSuggestion() {
        return (context, builder) -> {
            try {
                List<String> worldNames = aspHandler.listWorlds();
                for (String worldName : worldNames) {
                    builder.suggest(worldName);
                }
            } catch (IOException e) {
                // Optionally log to console
                Bukkit.getLogger().warning("Failed to get world list for tab completion: " + e.getMessage());
            }
            return builder.buildFuture(); // Return whatever suggestions were added (even if empty)
        };
    }

    private int teleport(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return 0;
        }

        String event = ctx.getArgument("event", String.class);

        SlimeWorldInstance eventWorldInstance = aspHandler.getWorldInstance(event);
        if (eventWorldInstance == null) {
            player.sendMessage("§cWorld '" + event + "' is not loaded. Trying to load it ...");
            aspHandler.loadWorld(event);
            eventWorldInstance = aspHandler.getWorldInstance(event);

            if (eventWorldInstance == null) {
                player.sendMessage(
                    "§cAttempt to load the world '" + event + "' unsuccessful. Aborting ..."
                );
                return 0;
            }
        }

        World eventWorld = eventWorldInstance.getBukkitWorld();

        player.teleport(eventWorld.getSpawnLocation());
        player.sendMessage("§aTeleported to world: §f" + event);

        return Command.SINGLE_SUCCESS;
    }

















//        if (args.length >= 1) {
//            switch (args[0].toLowerCase()) {
//                case "set":
//                    sender.sendMessage("Successfully set the Event to " + args[1]);
//                    EventManager.setCurrentEvent(args[1]);
//
//                    /* Load world */
//                    /* Refresh CMI holograms */
//                    break;
//
//                case "reset":
//                    sender.sendMessage("Successfully reset the Event!");
//                    EventManager.setCurrentEvent("");
//
//                    /* Unload world */
//                    break;
//
//                case "get":
//                    if (EventManager.getCurrentEvent().isEmpty()) {
//                        sender.sendMessage("There is no Event currently set!");
//                    } else {
//                        sender.sendMessage("Current event is " + EventManager.getCurrentEvent());
//                    }
//                    break;
//            
//                default:
//                    sender.sendMessage("Unknown arguments: " + Arrays.toString(args));
//                    break;
//            }
//        } else {
//            return false;
//        }
//
//        return true;
//    }
//
//    @Override
//    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
//                                                @NotNull Command command,
//                                                @NotNull String label,
//                                                @NotNull String @NotNull [] args) {
//
//        if (args.length == 1) {
//            return Arrays.asList("test");
//        }
//        
//        return new ArrayList<>();    //null = all player names
//    }
}
