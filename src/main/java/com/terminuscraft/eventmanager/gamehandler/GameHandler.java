package com.terminuscraft.eventmanager.gamehandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.miscellaneous.Constants;

public class GameHandler {

    private final File eventFile;
    private final SlimePropertyMap defaultProperties;

    private final List<Game> events = new ArrayList<>();
    private Game currentEvent;

    public GameHandler(EventManager plugin) {
        this.eventFile = new File(plugin.getDataFolder(), "events.yml");
        if (!eventFile.exists()) {
            // Extract the default from JAR: /"events.yml"
            plugin.saveResource("events.yml", false);
        }

        /* Read Default Properties from config */
        defaultProperties = GameProperties.getDefaultMap(plugin.getConfig());

        loadEvents();
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

    public void reloadEvents() {
        loadEvents();
    }

    public void saveEventConfigs() {
        saveEvents();
    }

    private void loadEvents() {
        this.events.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);
        ConfigurationSection section = config.getConfigurationSection("events");
        if (section == null) {
            return;
        }

        for (String worldName : section.getKeys(false)) {
            if (section.getConfigurationSection(worldName) == null) {
                continue;
            }

            SlimePropertyMap propertyMap = GameProperties.getWorldMap(config, worldName);

            try {
                Game event = new Game(worldName, propertyMap);

                if (event.hasValidWorld()) {
                    this.events.add(event);
                    Log.logger.info(
                        Lang.get("consile.event_load.success", Map.of("event", worldName))
                    );
                }
            } catch (Exception e) {
                Log.logger.warning(Lang.get("console.event_load.fail",
                                            Map.of("{event}", worldName,
                                                   "{reason}", e.getMessage()
                    )));
            }
        }
    }


    private void saveEvents() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);
        config.set("events", null); // Clear old

        for (Game event : events) {
            SlimePropertyMap properties = event.getProperties();
            Set<String> propertyKeys = properties.getProperties().keySet();

            GameProperties.getSlimePropertyList().forEach((property) -> {
                String key = property.getKey();

                if (propertyKeys.contains(key)) {
                    String path = "events." + event.getName() + "." + key;
                    config.set(path, properties.getValue(property));
                }
            });

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
            Game event = new Game(eventName, defaultProperties);
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

    public int createEvent(String eventName) {
        if (worldExists(eventName) || eventExists(eventName)) {
            return Constants.FAIL;
        }

        /* Create a new event instance and add it into the existing event list */
        Game event = new Game(eventName, defaultProperties);
        events.add(event);

        saveEvents();
        return Constants.SUCCESS;
    }

    public int setEventSpawn(Game event, Location pos) {
        SlimePropertyMap map = new SlimePropertyMap();

        map.setValue(GameProperties.SPAWN_X, (int) pos.getX());
        map.setValue(GameProperties.SPAWN_Y, (int) pos.getY());
        map.setValue(GameProperties.SPAWN_Z, (int) pos.getZ());
        map.setValue(GameProperties.SPAWN_YAW, pos.getYaw());

        /* Change currently loaded world Spawn Location */
        if (!event.getWorld().setSpawnLocation(pos)) {
            return Constants.FAIL;
        }

        /* Change persistent world Spawn Location */
        event.addProperties(map);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);
        Set<String> propertyKeys = map.getProperties().keySet();
        
        GameProperties.getSlimePropertyList().forEach((property) -> {
            String key = property.getKey();

            if (propertyKeys.contains(key)) {
                String path = "events." + event.getName() + "." + key;
                config.set(path, map.getValue(property));
            }
        });

        try {
            config.save(eventFile);
        } catch (IOException e) {
            Log.logger.warning("Failed to save events.yml");
            return Constants.FAIL;
        }

        return Constants.SUCCESS;
    }

    public int removeEvent(Game event) {
        if (!eventExists(event)) {
            return Constants.FAIL;
        }

        events.remove(event);

        saveEvents();
        return Constants.SUCCESS;
    }

    public int deleteEvent(Game event) {
        if (!eventExists(event)) {
            return Constants.FAIL;
        }

        event.deleteWorld();
        events.remove(event);

        saveEvents();
        return Constants.SUCCESS;
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
        return Game.aspAdapter.listWorlds();
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
            return Game.aspAdapter.worldExists(worldName);
        } catch (Exception e) {
            return false;
        }
    }
}
