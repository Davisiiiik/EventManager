package com.terminuscraft.eventmanager;

import java.util.List;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.Command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class CommandManager {

    private static AspAdapter aspHandler;

    public static void initialize(AspAdapter handler) {
        aspHandler = handler;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> commandTree;

        commandTree = Commands.literal("evm")
            .then(Commands.literal("help"))
            .then(Commands.literal("list").executes(CommandManager::listEvents))
            .then(Commands.literal("tp")
                .then(Commands.argument("event", StringArgumentType.word())
                    .executes(CommandManager::teleport))
            )
            .then(Commands.literal("admin")
                .then(Commands.literal("get").executes(CommandManager::getEvent))
                .then(Commands.literal("end").executes(CommandManager::endEvent))
                .then(Commands.literal("purge"))

                .then(Commands.literal("start")
                    .then(Commands.argument("event", StringArgumentType.word())
                        .executes(CommandManager::startEvent))
                )

                .then(Commands.literal("add")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )

                .then(Commands.literal("remove")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )

                .then(Commands.literal("load")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )
                
                .then(Commands.literal("unload")
                    .then(Commands.argument("event", StringArgumentType.word()))
                )
            );

        return commandTree;
    }

    private static int listEvents(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        // Call ASPHandler method
        try {
            List<String> worldsList = aspHandler.listWorlds();
            sender.sendMessage("Current list of events:\n" + worldsList);
        } catch (IOException e) {
            sender.sendMessage("Error: Couldnt retrieve list of events, try contacting Administrator!");
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int teleport(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();
        
        // Check whether the executor is a player, as you can only set a player's flight speed
        if (!(executor instanceof Player)) {
            // If a non-player tried to set their own flight speed
            sender.sendPlainMessage("Only players can teleport!");
            return Command.SINGLE_SUCCESS;
        }

        //player.teleport(new Location("test", 0, 0, 0));

        sender.sendMessage("NOT IMPLEMENTED");

        return Command.SINGLE_SUCCESS;
    }

    private static int getEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (EventManager.getCurrentEvent().isEmpty()) {
            sender.sendMessage("There is no Event currently started!");
        } else {
            sender.sendMessage("Current event is " + EventManager.getCurrentEvent());
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int endEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage("Successfully ended the " + EventManager.getCurrentEvent() + " Event!");
        EventManager.setCurrentEvent("");

        /* Unload world */

        return Command.SINGLE_SUCCESS;
    }

    private static int startEvent(CommandContext<CommandSourceStack> ctx) {
        String event = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage("Successfully started the " + event + " Event");
        EventManager.setCurrentEvent(event);

        /* Load world */
        /* Refresh CMI holograms */

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
