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
import com.terminuscraft.eventmanager.eventhandler.Event;
import com.terminuscraft.eventmanager.eventhandler.EvmHandler;
import com.terminuscraft.eventmanager.hooks.AspAdapter;
import com.terminuscraft.eventmanager.miscellaneous.Lang;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;

public class AdminCommands {

    private final AspAdapter aspHandler;

    public AdminCommands(AspAdapter handler) {
        this.aspHandler = handler;
    }

    public int endEvent(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        Event event = EvmHandler.getCurrentEvent();

        sender.sendMessage(Lang.get("command.admin.start", Map.of("event", event.getName())));
        EvmHandler.setCurrentEvent(null);

        /* Unload world */

        return Command.SINGLE_SUCCESS;
    }

    public int startEvent(CommandContext<CommandSourceStack> ctx) {
        String eventName = ctx.getArgument("event", String.class);
        CommandSender sender = ctx.getSource().getSender();

        /* TODO: EventWorldHandler should check if the event with given name exists and abort if
                 not, for now just create a new instance of event instead of loading from config */
        Event event = new Event(eventName);

        sender.sendMessage(Lang.get("command.admin.end", Map.of("event", event.getName())));
        EvmHandler.setCurrentEvent(event);

        /* Load world */
        /* Refresh CMI holograms */

        return Command.SINGLE_SUCCESS;
    }

    public int teleport(CommandContext<CommandSourceStack> ctx) {
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

    public int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        JavaPlugin.getPlugin(EventManager.class).pluginReload();

        sender.sendMessage(Component.text(Lang.get("command.admin.reload")));

        return Command.SINGLE_SUCCESS;
    }
}
