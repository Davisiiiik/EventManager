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
import org.bukkit.entity.Player;

import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.terminuscraft.eventmanager.EventManager;
import com.terminuscraft.eventmanager.communication.Lang;
import com.terminuscraft.eventmanager.communication.Log;
import com.terminuscraft.eventmanager.miscellaneous.Constants;
import com.terminuscraft.eventmanager.miscellaneous.Utils;

public class GameHandler {

    private final File eventFile;
    private final SlimePropertyMap defaultProperties;

    private List<Game> events = new ArrayList<>();
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

    public int setCurrentEvent(String eventName) {
        if (this.eventExists(eventName.toLowerCase())) {
            currentEvent = getEvent(eventName);
            return Constants.SUCCESS;
        }
        return Constants.FAIL;
    }

    public void resetCurrentEvent() {
        currentEvent = null;
    }

    public Game getCurrentEvent() {
        if (currentEvent == null) {
            return null;
        }
        return currentEvent.copy();
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

                if (event.hasExistingWorld()) {
                    this.events.add(event);
                    Log.logger.info(
                        Lang.get("console.event_load.success", Map.of("event", worldName))
                    );
                } else {
                    Log.logger.info(
                        Lang.get("console.event_load.no_world", Map.of("world", worldName))
                    );
                }
            } catch (Exception e) {
                Map<String, String> dict = Map.of("{event}", worldName, "{reason}", e.getMessage());
                Log.logger.warning(Lang.get("console.event_load.fail", dict));
            }
        }
    }


    private int saveEvent(Game event) {
        return saveEvents(List.of(event));
    }


    private int saveEvents() {
        /* Clear old data and save all current events into the events.yml */
        YamlConfiguration.loadConfiguration(eventFile).set("events", null);
        return saveEvents(events);
    }


    private int saveEvents(List<Game> eventList) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);

        for (Game event : eventList) {
            SlimePropertyMap properties = event.getPropertyMap();
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
            Log.logger.warning("Failed to save events.yml: " + e);
            return Constants.FAIL;
        }

        return Constants.SUCCESS;
    }

    private int removeEventFromConfig(Game event) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(eventFile);
        config.set("events." + event.getName(), null);

        try {
            config.save(eventFile);
        } catch (IOException e) {
            Log.logger.warning("Failed to save events.yml: " + e);
            return Constants.FAIL;
        }
        return Constants.SUCCESS;
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

        return saveEvent(event);
    }

    public int saveAndUnloadEventWorld(Game event) {
        return unloadEventWorld(event, true);
    }

    private int unloadEventWorld(Game event, boolean saveWorld) {
        /* Send all players to spawn */
        for (Player player : event.getWorld().getPlayers()) {
            Utils.getInstance().sendToSpawn(player);
            player.sendMessage(Lang.pget("system.unload_tp", Map.of("event", event.getName())));
        }

        /* Try to save and unload world and return action status */
        return event.unloadWorld(saveWorld);
    }

    public int removeEvent(Game event) {
        if (!eventExists(event)) {
            return Constants.FAIL;
        }

        if (event == currentEvent) {
            resetCurrentEvent();
        }

        saveAndUnloadEventWorld(event);
        events.remove(event);

        return removeEventFromConfig(event);
    }

    public int deleteEvent(Game event) {
        if (!eventExists(event)) {
            return Constants.FAIL;
        }

        if (event == currentEvent) {
            resetCurrentEvent();
        }

        unloadEventWorld(event, false);
        event.deleteWorld();
        events.remove(event);

        return removeEventFromConfig(event);
    }

    public Game getEvent(String eventName) {
        for (Game existingEvent : events) {
            if (eventName.equalsIgnoreCase(existingEvent.getName())) {
                return existingEvent;
            }
        }

        return null;
    }

    public List<Game> getEventList() {
        return new ArrayList<Game>(events);
    }

    public List<String> getEventNameList() {
        List<String> eventList = events.stream().map(Game::getName).collect(Collectors.toList());
        return eventList;
    }

    public List<String> getWorldList() throws IOException {
        return Game.aspAdapter.listWorlds();
    }

    public List<String> getLoadedWorldList() {
        return Game.aspAdapter.getLoadedWorldList().stream().map(
            SlimeWorldInstance::getName).collect(
                Collectors.toList()
        );
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
        return Game.aspAdapter.worldExists(worldName);
    }

    public boolean worldIsLoaded(String worldName) {
        return getLoadedWorldList().contains(worldName);
    }
}
