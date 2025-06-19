package com.terminuscraft.eventmanager.gamehandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.hooks.AspAdapter;
import com.terminuscraft.eventmanager.miscellaneous.Constants;

public class GameHandler {

    private final File eventFile;
    private final AspAdapter aspAdapter = new AspAdapter();
    private final SlimePropertyMap defaultProperties;

    private final List<Game> events = new ArrayList<>();
    private Game currentEvent;

    public GameHandler(EventManager plugin) {
        super();

        this.eventFile = new File(plugin.getDataFolder(), "events.yml");
        if (!eventFile.exists()) {
            // Extract the default from JAR: /"events.yml"
            plugin.saveResource("events.yml", false);
        }

        /* Read Default Properties from config */
        defaultProperties = GameProperties.getDefaultMap(plugin.getConfig());
        plugin.getLogger().warning("============= DEBUG =============");
        plugin.getLogger().warning(defaultProperties.toString());
        plugin.getLogger().warning("============= DEBUG =============");

        loadEvents();
    }

    public int setCurrentEvent(String newEventName) {
        return setCurrentEvent(getEvent(newEventName));
    }

    public int setCurrentEvent(Game newEvent) {
        if (this.eventExists(newEvent.getName().toLowerCase())) {
            currentEvent = newEvent;
            return Constants.SUCCESS;
        }
        return Constants.FAIL;
    }

    public void resetCurrentEvent() {
        currentEvent = null;
    }

    public Game getCurrentEvent() {
        return currentEvent;
    }

    public int loadEventWorld(Game event) {
        SlimeWorldInstance worldInstance = aspAdapter.loadWorldInstance(event.getName());
        if (worldInstance != null) {
            event.setWorldInstance(worldInstance);
            return Constants.SUCCESS;
        }

        return Constants.FAIL;
    }

    private void loadEvents() {
        this.events.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);
        ConfigurationSection section = config.getConfigurationSection("events");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            /*ConfigurationSection evSec = section.getConfigurationSection(key);
            if (evSec == null) {
                continue;
            }*/ // TEMPORARY

            try {
                Game event = new Game(key, defaultProperties);

                this.events.add(event);
            } catch (Exception e) {
                Log.logger.warning("Failed to load event '" + key + "': " + e.getMessage());
            }
        }
    }


    public void reloadEvents() {
        loadEvents();
    }


    private void saveEvents() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);
        config.set("events", null); // Clear old

        for (Game event : events) {
            String key = event.getName();
            config.set("events." + key, "");  /* TODO: REMOVE, ONLY TEMPORARY */
            /*config.set("events." + key + ".pvp", event.isPvpEnabled());
            config.set("events." + key + ".loadOnStartup", event.shouldLoadOnStartup());
            config.set("events." + key + ".environment", event.getEnvironment().toString());

            for (() : event.properties) {

            }*/
        }

        try {
            config.save(eventFile);
        } catch (IOException e) {
            Log.logger.warning("Failed to save events.yml");
        }
    }


    public int addEvent(String eventName) {
        if (worldExists(eventName) && (!eventExists(eventName))) {
            /* If world already exists and event does not, add event into the event list */
            SlimeWorldInstance worldInstance = aspAdapter.getWorldInstance(eventName);
            Game event = new Game(eventName, defaultProperties, worldInstance);
            events.add(event);
        } else {
            Log.logger.warning(
                "Cannot add event " + eventName + ", world with this name doesn't exist."
            );
            return Constants.FAIL;
        }

        saveEvents();
        return Constants.SUCCESS;
    }


    // Public API for use in commands
    public int createEvent(String eventName) {
        if (worldExists(eventName) || eventExists(eventName)) {
            return Constants.FAIL;
        }

        /* Try creating Slime world instance */
        SlimeWorldInstance slimeWorldInstance = aspAdapter.createWorld(eventName, defaultProperties);
        if (slimeWorldInstance == null) {
            return Constants.FAIL;
        }

        /* If world creation was successful, add newly created event instance into the event list */
        Game event = new Game(eventName, defaultProperties, slimeWorldInstance);
        events.add(event);

        saveEvents();
        return Constants.SUCCESS;
    }

    public boolean deleteEvent(Game event) {
        if (!eventExists(event)) {
            return false;
        }

        /* TODO: Delete event's world */

        events.remove(event);

        saveEvents();
        return true;
    }

    public Game getEvent(String eventName) {
        for (Game existingEvent : events) {
            if (eventName.equalsIgnoreCase(existingEvent.getName())) {
                return existingEvent;
            }
        }

        return null;
    }

    public List<String> getEventList() {
        List<String> eventList = events.stream().map(Game::getName).collect(Collectors.toList());
        return eventList;
    }

    public List<String> getWorldList() throws IOException {
        return aspAdapter.listWorlds();
    }

    public boolean eventIsValid(String eventName) {
        return (eventExists(eventName) && worldExists(eventName));
    }

    public boolean eventExists(Game event) {
        if (events.contains(event)) {
            return true;
        }

        return eventExists(event.getName());
    }

    public boolean eventExists(String eventName) {
        for (Game existingEvent : events) {
            if (eventName.equalsIgnoreCase(existingEvent.getName())) {
                return true;
            }
        }

        return false;
    }

    public boolean worldExists(String worldName) {
        try {
            return aspAdapter.worldExists(worldName);
        } catch (Exception e) {
            return false;
        }
    }

    public void reload() {
        loadEvents();
    }
}
