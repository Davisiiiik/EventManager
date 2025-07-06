package com.terminuscraft.eventmanager.communication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.terminuscraft.eventmanager.miscellaneous.Constants;

import net.md_5.bungee.api.ChatColor;

public final class Lang {

    private static JavaPlugin plugin;
    private static FileConfiguration defaultConfig;
    private static boolean initFlag = false;

    private static String langFileName;
    private static File langFolder;
    private static FileConfiguration dictionary;

    public static int init(JavaPlugin javaPlugin) {
        plugin = javaPlugin;

        /* Get languages folder and language set by user */
        langFolder = new File(plugin.getDataFolder(), "languages");
        /* Set the static attribute langFileName to "language" option from config or to default */
        langFileName = plugin.getConfig().getString("language", "en.yml");
        
        /* Try making langFolder dir, if it doesn't exist and raise exception if unsuccessful */
        if (!(langFolder.exists() || langFolder.mkdirs())) {
            plugin.getLogger().severe("Error: Unable to create language directory!");
            return Constants.FAIL;
        }

        /* Load default lang file from inside JAR */
        InputStream defaultStream = plugin.getResource("languages/" + langFileName);
        if (defaultStream == null) {
            defaultStream = plugin.getResource("languages/en.yml");
            if (defaultStream == null) {
                plugin.getLogger().severe("Error: Could not load default language file!");
                return Constants.FAIL;
            }
        }

        defaultConfig = YamlConfiguration.loadConfiguration(
            new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
        );

        File langFile = new File(langFolder, langFileName);
        if (!langFile.exists()) {
            // Extract the default from JAR: /languages/lang.yml
            plugin.saveResource("languages/" + langFileName, false);
        }

        dictionary = YamlConfiguration.loadConfiguration(langFile);
        initFlag = true;

        return Constants.SUCCESS;
    }

    public static int reloadLanguage() {
        if (!initFlag) {
            return Constants.FAIL;
        }

        return init(plugin);
    }

    private static File getLangFileName() {
        return new File(langFolder, langFileName);
    }

    public static String pget(String key) {
        return get(key, Map.of(), true);
    }

    public static String pget(String key, Map<String, String> placeholders) {
        return get(key, placeholders, true);
    }

    public static String get(String key) {
        return get(key, Map.of(), false);
    }

    public static String get(String key, Map<String, String> placeholders) {
        return get(key, placeholders, false);
    }

    private static String get(String key, Map<String, String> placeholders, boolean usePrefix) {
        if (!initFlag) {
            return "&c[Lang not initialized]";
        }

        String raw = dictionary.getString(key);

        if (raw == null) {
            raw = defaultConfig.getString(key);
            if (raw != null) {
                // Save it into the language file on disk
                dictionary.set(key, raw);
                try {
                    dictionary.save(getLangFileName());
                } catch (IOException e) {
                    Bukkit.getLogger().warning("&c[Lang] Failed to append missing key: " + key);
                }
            } else {
                raw = "&cMissing lang key: " + key;
            }
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        if (usePrefix) {
            raw = dictionary.getString("prefix") + raw;
        }

        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}


