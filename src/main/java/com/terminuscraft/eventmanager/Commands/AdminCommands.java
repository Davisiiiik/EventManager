package com.terminuscraft.eventmanager.commands;

import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;

import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.gamehandler.Game;
import com.terminuscraft.eventmanager.gamehandler.GameHandler;
import com.terminuscraft.eventmanager.miscellaneous.Constants;
import com.terminuscraft.eventmanager.miscellaneous.Utils;

public class AdminCommands {

    public final GameHandler gameHandler;

    public AdminCommands(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public int addEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        if (!gameHandler.worldExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.add.no_world", Map.of("event", eventName)));
            return 0;
        }

        if (gameHandler.eventExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.create.dupe_event", Map.of("event", eventName)));
            return 0;

        }

        gameHandler.addEvent(eventName);
        sender.sendMessage(Lang.get("cmd.add.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int createEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        if (gameHandler.eventExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.create.dupe_event", Map.of("event", eventName)));
            return 0;
        }

        if (gameHandler.worldExists(eventName)) {
            sender.sendMessage(Lang.get("cmd.create.dupe_world", Map.of("event", eventName)));
            return 0;
        }

        if (gameHandler.createEvent(eventName) != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("cmd.create.fail", Map.of("event", eventName)));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.create.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int setSpawn(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            sender.sendMessage(Lang.get("error.players_only"));
            return 0;
        }

        World world = player.getWorld();

        Game event = gameHandler.getEvent(world.getName());
        if (event == null) {
            player.sendMessage(
                Lang.get("cmd.set_spawn.not_event", Map.of("world", world.getName()))
            );
            return 0;
        }

        gameHandler.setEventSpawn(event, player.getLocation());

        player.sendMessage(Lang.get("cmd.set_spawn.success", Map.of("event", event.getName())));

        return Command.SINGLE_SUCCESS;
    }

    public int startEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        Game currEvent = gameHandler.getCurrentEvent();
        if ((currEvent != null) && currEvent.getName().equalsIgnoreCase(eventName)) {
            sender.sendMessage(Lang.get("cmd.start.dupe", Map.of("event", currEvent.getName())));
            return 0;
        }

        if (gameHandler.setCurrentEvent(eventName) != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("error.event_invalid", Map.of("event", eventName)));
            return 0;
        }

        Game newEvent = gameHandler.getEvent(eventName);
        if (newEvent.loadWorld() != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("error.start.fail", Map.of("event", eventName)));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.start.success", Map.of("event", eventName)));

        Utils.getInstance().refreshHolograms();

        /* TODO: Add notification across bungeecord, add to config file option to enable this */

        return Command.SINGLE_SUCCESS;
    }

    public int endEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        Game event = gameHandler.getCurrentEvent();
        if (event == null) {
            sender.sendMessage(Lang.get("cmd.current.no_event"));
            return 0;
        }

        gameHandler.resetCurrentEvent();
        sender.sendMessage(Lang.get("cmd.end", Map.of("event", event.getName())));

        if (gameHandler.saveAndUnloadEventWorld(event) != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("cmd.unload.fail", Map.of("event", event.getName())));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.unload.success", Map.of("event", event.getName())));

        return Command.SINGLE_SUCCESS;
    }

    public int teleport(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if (!(executor instanceof Player player)) {
            sender.sendMessage(Lang.get("error.players_only"));
            return 0;
        }

        Game event = gameHandler.getEvent(ctx.getArgument("event", String.class));
        if (event == null) {
            player.sendMessage(Lang.get("cmd.tp.not_found", Map.of("event", eventName)));
            return 0;
        }

        Utils.getInstance().refreshHolograms();
        World eventWorld = event.getWorld();
        if (eventWorld == null) {
            player.sendMessage(Lang.get("error.event_load_abort", Map.of("event", eventName)));
            return 0;
        }

        player.teleport(eventWorld.getSpawnLocation());
        player.sendMessage(Lang.get("cmd.tp.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int unloadEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        Game event = gameHandler.getEvent(eventName);
        if (event == null) {
            sender.sendMessage(Lang.get("error.event_invalid", Map.of("event", eventName)));
            return 0;
        }

        if (gameHandler.saveAndUnloadEventWorld(event) != Constants.SUCCESS) {
            sender.sendMessage(Lang.get("cmd.unload.fail", Map.of("event", eventName)));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.unload.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int removeEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        /* TODO: Add confirmation check */

        Game event = gameHandler.getEvent(eventName);
        if (event == gameHandler.getCurrentEvent()) {
            sender.sendMessage(Lang.get("cmd.remove.event_running", Map.of("event", eventName)));
            return 0;
        }

        if ((event == null) || (gameHandler.removeEvent(event) != Constants.SUCCESS)) {
            sender.sendMessage(Lang.get("cmd.remove.fail", Map.of("event", eventName)));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.remove.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int deleteEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();
        

        /* TODO: Add confirmation check */

        Game event = gameHandler.getEvent(eventName);
        if (event == gameHandler.getCurrentEvent()) {
            sender.sendMessage(Lang.get("cmd.delete.event_running", Map.of("event", eventName)));
            return 0;
        }

        if ((event == null) || (gameHandler.deleteEvent(event) != Constants.SUCCESS)) {
            sender.sendMessage(Lang.get("cmd.delete.fail", Map.of("event", eventName)));
            return 0;
        }

        sender.sendMessage(Lang.get("cmd.delete.success", Map.of("event", eventName)));

        return Command.SINGLE_SUCCESS;
    }

    public int saveEvents(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        gameHandler.saveEventConfigs();

        sender.sendMessage(Component.text(Lang.get("cmd.save_events")));

        return Command.SINGLE_SUCCESS;
    }

    public int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        JavaPlugin.getPlugin(EventManager.class).pluginReload();

        sender.sendMessage(Component.text(Lang.get("cmd.reload")));

        return Command.SINGLE_SUCCESS;
    }
}
