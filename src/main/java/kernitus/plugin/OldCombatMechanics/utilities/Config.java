package kernitus.plugin.OldCombatMechanics.utilities;

import com.google.common.base.Preconditions;
import kernitus.plugin.OldCombatMechanics.ModuleLoader;
import kernitus.plugin.OldCombatMechanics.OCMMain;
import kernitus.plugin.OldCombatMechanics.module.Module;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Rayzr522 on 6/14/16.
 */

public class Config {

    private static OCMMain plugin;
    private static FileConfiguration config;
    private static EnumSet<Material> interactive;

    private static boolean loaded;

    public static void initialise(OCMMain plugin) {
        Config.plugin = plugin;
        config = plugin.getConfig();

        reload();
    }

    /**
     * @return Whether config was changed or not
     */
    private static boolean checkConfigVersion() {
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("config.yml")));

        if (config.getInt("config-version") != defaultConfig.getInt("config-version")) {
            plugin.getLogger().warning("Config version does not match, backing up old config and creating a new one");
            plugin.upgradeConfig();
            reload();
            return true;
        }

        return false;
    }


    public static void reload() {
        Preconditions.checkArgument(!loaded, "The plugin does not properly clean up old tasks and data - reloading is not supported.");
        if (plugin.doesConfigExist()) {
            plugin.reloadConfig();
            config = plugin.getConfig();
        } else
            plugin.upgradeConfig();

        if (checkConfigVersion()) {
            // checkConfigVersion will call #reload() again anyways
            return;
        }

        // Load all interactive blocks (used by sword blocking and elytra modules)
        reloadInteractiveBlocks();

        for (Module module : ModuleLoader.getModules()) {
            final boolean enabled = moduleEnabled(module.getConfigName());
            module.setEnabled(enabled);

            if (!enabled) continue;

            try {
                module.reload();
            } catch (Exception e) {
                plugin.getLogger()
                        .log(Level.WARNING, "Error reloading module '" + module.toString() + "'", e);
            }
        }

        // Dynamically registers / unregisters all event listeners for optimal performance!
        ModuleLoader.toggleModules();

        loaded = true;
    }

    public static boolean moduleEnabled(String name) {
        ConfigurationSection section = config.getConfigurationSection(name);

        if (section == null) {
            //plugin.getLogger().warning("Tried to check module '" + name + "', but it didn't exist!");
            return false;
        }

        return section.getBoolean("enabled");
    }

    public static boolean debugEnabled() {
        return moduleEnabled("debug");
    }

    public static List<?> getWorlds(String moduleName) {
        return config.getList(moduleName + ".worlds");
    }

    public static boolean moduleSettingEnabled(String moduleName, String moduleSettingName) {
        return config.getBoolean(moduleName + "." + moduleSettingName);
    }

    public static void setModuleSetting(String moduleName, String moduleSettingName, boolean value) {
        config.set(moduleName + "." + moduleSettingName, value);
        plugin.saveConfig();
    }

    private static void reloadInteractiveBlocks() {
        interactive = ConfigUtils.loadMaterialList(config, "interactive");
    }

    public static EnumSet<Material> getInteractiveBlocks() {
        return interactive;
    }

    /**
     * Only use if you can't access config through plugin instance
     *
     * @return config.yml instance
     */
    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
