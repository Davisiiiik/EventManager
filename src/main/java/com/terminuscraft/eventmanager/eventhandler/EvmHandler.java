package com.terminuscraft.eventmanager.eventhandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.hooks.AspAdapter;
import com.terminuscraft.eventmanager.miscellaneous.Constants;
import com.terminuscraft.eventmanager.miscellaneous.Environment;

public class EvmHandler extends AspAdapter {

    private final File eventFile;
    private final Map<String, Event> events = new HashMap<>();
    
    private static Event currentEvent;

    public EvmHandler(EventManager plugin) {
        super(plugin.getLogger());

        this.eventFile = new File(plugin.getDataFolder(), "events.yml");
        if (!eventFile.exists()) {
            // Extract the default from JAR: /"events.yml"
            plugin.saveResource("events.yml", false);
        }

        loadEvents();
    }

    private void loadEvents() {
        this.events.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);
        ConfigurationSection section = config.getConfigurationSection("events");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection evSec = section.getConfigurationSection(key);
            if (evSec == null) {
                continue;
            }

            try {
                String name = key;
                boolean pvp = evSec.getBoolean("pvp", true);
                boolean loadOnStartup = evSec.getBoolean("loadOnStartup", false);
                String envStr = evSec.getString("environment", "normal");

                Environment environment = Environment.fromString(envStr);
                Event event = new Event(name, pvp, loadOnStartup, environment);

                events.put(name.toLowerCase(), event);
            } catch (Exception e) {
                logger.warning("Failed to load event '" + key + "': " + e.getMessage());
            }
        }
    }


    public void reloadEvents() {
        loadEvents();
    }


    private void saveEvents() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);
        config.set("events", null); // Clear old

        for (Event event : events.values()) {
            String key = event.getName();
            config.set("events." + key + ".pvp", event.isPvpEnabled());
            config.set("events." + key + ".loadOnStartup", event.shouldLoadOnStartup());
            config.set("events." + key + ".environment", event.getEnvironment().toString());
        }

        try {
            config.save(eventFile);
        } catch (IOException e) {
            logger.warning("Failed to save events.yml");
        }
    }


    // Public API for use in commands
    public int addEvent(String eventName) {
        boolean worldExists;

        try {
            worldExists = this.worldExists(eventName);
        } catch (IOException e) {
            worldExists = false;
        }

        if (worldExists && (!events.containsKey(eventName.toLowerCase()))) {
            /* If world already exists and event does not, add event into the event list */
            Event event = new Event(eventName);
            events.put(eventName.toLowerCase(), event);
        } else {
            logger.warning(
                "Cannot add event " + eventName + ", world with this name doesn't exist."
            );
            return Constants.FAIL;
        }

        saveEvents();
        return Constants.SUCCESS;
    }


    // Public API for use in commands
    public int createEvent(String eventName) {
        if (events.containsKey(eventName.toLowerCase())) {
            return Constants.FAIL;
        }

        /* Try creating Slime world instance */
        SlimeWorldInstance slimeWorldInstance = this.createWorld(eventName);
        if (slimeWorldInstance == null) {
            return Constants.FAIL;
        }

        /* If world creation was successful, add newly created event into the event list */
        Event event = new Event(eventName);
        events.put(eventName.toLowerCase(), event);

        saveEvents();
        return Constants.SUCCESS;
    }

    public boolean deleteEvent(String eventName) {
        if (!events.containsKey(eventName.toLowerCase())) {
            return false;
        }

        events.remove(eventName.toLowerCase());

        saveEvents();
        return true;
    }

    public Event getEvent(String eventName) {
        return events.get(eventName.toLowerCase());
    }

    /*public Collection<Event> listEvents() {
        return Collections.unmodifiableCollection(events.values());
    }*/

    public List<String> getEventList() {
        return new ArrayList<String>(events.keySet());
    }

    public boolean eventExists(String eventName) {
        try {
            /* Check if the world itself exists and if the event is also defined */
            return (worldExists(eventName) && events.containsKey(eventName.toLowerCase()));
        } catch (IOException e) {
            return false;
        }
    }

    public void reload() {
        loadEvents();
    }

    public static void setCurrentEvent(Event newEvent) {
        currentEvent = newEvent;
    }

    public static Event getCurrentEvent() {
        return currentEvent;
    }
}
