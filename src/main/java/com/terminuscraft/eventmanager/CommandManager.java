package com.terminuscraft.eventmanager;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
final class CommandManager implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");

            return true;
        }

        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "set":
                    sender.sendMessage("Successfully set the Event to " + args[1]);
                    EventManager.setCurrentEvent(args[1]);

                    /* Load world */
                    /* Refresh CMI holograms */
                    break;

                case "reset":
                    sender.sendMessage("Successfully reset the Event!");
                    EventManager.setCurrentEvent("");

                    /* Unload world */
                    break;

                case "get":
                    if (EventManager.getCurrentEvent().isEmpty()) {
                        sender.sendMessage("There is no Event currently set!");
                    } else {
                        sender.sendMessage("Current event is " + EventManager.getCurrentEvent());
                    }
                    break;
            
                default:
                    sender.sendMessage("Unknown arguments: " + Arrays.toString(args));
                    break;
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NotNull [] args) {

        if (args.length == 1) {
            return Arrays.asList("test");
        }
        
        return new ArrayList<>();    //null = all player names
    }
}
