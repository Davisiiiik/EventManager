package com.terminuscraft.eventmanager.miscellaneous;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public final class Lang {

    private static JavaPlugin plugin;
    private static Lang instance;
    private static String langFileName;

    private final File langFolder;
    private final FileConfiguration defaultConfig;
    private FileConfiguration dictionary;

    public static void init(JavaPlugin javaPlugin) {
        try {
        instance = new Lang(javaPlugin);
        } catch (IOException e) {
            plugin.getLogger().severe("Error: Could not load default language file!");
        }
    }

    private Lang(JavaPlugin javaPlugin) throws IOException {
        plugin = javaPlugin;

        this.langFolder = new File(plugin.getDataFolder(), "languages");
        
        /* Try making langFolder dir, if it doesn't exist and raise exception if unsuccessful */
        if (!(this.langFolder.exists() || this.langFolder.mkdirs())) {
            throw new IOException("Unable to create path");
        }

        /* Load default lang file from inside JAR */
        InputStream defaultStream = plugin.getResource("languages/en.yml");
        if (defaultStream == null) {
            throw new IOException("Error: Could not load default language file!");
        }

        this.defaultConfig = YamlConfiguration.loadConfiguration(
            new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
        );

        /* Load lang file by name specified in plugin config */
        this.loadDictionary();
    }

    private void loadDictionary() {
        /* Set the static attribute langFileName to "language" option from config or to default */
        langFileName = plugin.getConfig().getString("language", "en.yml");
        File langFile = new File(this.langFolder, langFileName);
        if (!langFile.exists()) {
            // Extract the default from JAR: /languages/lang.yml
            plugin.saveResource("languages/" + langFileName, false);
        }

        this.dictionary = YamlConfiguration.loadConfiguration(langFile);
    }

    public static void reload() {
        instance.loadDictionary();
    }

    public static String get(String key) {
        return get(key, Map.of());
    }

    public static String get(String key, Map<String, String> placeholders) {
        if (instance == null) {
            return "§c[Lang not initialized]";
        }

        String raw = instance.dictionary.getString(key);

        if (raw == null) {
            raw = instance.defaultConfig.getString(key);
            if (raw != null) {
                // Save it into the language file on disk
                instance.dictionary.set(key, raw);
                try {
                    instance.dictionary.save(getLangFileName());
                } catch (IOException e) {
                    Bukkit.getLogger().warning("[Lang] Failed to append missing key: " + key);
                }
            } else {
                raw = "§cMissing lang key: " + key;
            }
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public static List<String> getList(String key) {
        if (instance == null) {
            return List.of("§c[Lang not initialized]");
        }

        List<String> result = instance.dictionary.getStringList(key);

        if (result.isEmpty() && instance.defaultConfig.contains(key)) {
            result = instance.defaultConfig.getStringList(key);
            instance.dictionary.set(key, result);
            try {
                instance.dictionary.save(getLangFileName());
            } catch (IOException e) {
                Bukkit.getLogger().warning("[Lang] Failed to append missing list key: " + key);
            }
        }

        return result.stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .toList();
    }

    private static File getLangFileName() {
        return new File(instance.langFolder, langFileName);
    }
}


