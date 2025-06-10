package com.terminuscraft.eventmanager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Created by Davisiiiik on 09/06/25.
 *
 * @author Copyright (c) Davisiiiik. All Rights Reserved.
 */
public class EventManager extends JavaPlugin {
  
  String ver = "0.0.1";

  private static String currentEvent = "";
  /*private static Map<String, Float> coordinates = new HashMap<String, Float>()
  {{
     put("x", 0.0F);
     put("y", 0.0F);
     put("z", 0.0F);
  }};*/

  @Override
  public void onEnable() {
    getLogger().info("TerminusCraft Event Manager v" + ver + " successfully loaded!");
    getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

    getCommand("evm").setExecutor(new CommandManager());

    //saveDefaultConfig();
  }

  @Override
  public void onDisable() {
      getLogger().info("TerminusCraft Event Manager successfully unloaded!");
  }

  public static void setCurrentEvent(String newEvent) {
    currentEvent = newEvent;
  }

  public static String getCurrentEvent() {
    return currentEvent;
  }
}
