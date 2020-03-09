package kernitus.plugin.OldCombatMechanics;

import kernitus.plugin.OldCombatMechanics.module.*;
import kernitus.plugin.OldCombatMechanics.utilities.Config;
import kernitus.plugin.OldCombatMechanics.utilities.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OCMMain extends JavaPlugin {

    private static OCMMain INSTANCE;
    private final Logger logger = getLogger();
    private final OCMConfigHandler CH = new OCMConfigHandler(this);
    private final List<Runnable> disableListeners = new ArrayList<>();
    private final List<Runnable> enableListeners = new ArrayList<>();

    public static OCMMain getInstance() {
        return INSTANCE;
    }

    public static String getVersion() {
        return INSTANCE.getDescription().getVersion();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        PluginDescriptionFile pdfFile = this.getDescription();

        // Setting up config.yml
        CH.setupConfig();

        // Initialise ModuleLoader utility
        ModuleLoader.initialise(this);

        // Register all the modules
        registerModules();

        // Initialise the Messenger utility
        Messenger.initialise(this);

        // Initialise Config utility
        Config.initialise(this);

        for (Runnable enableListener : enableListeners) {
            enableListener.run();
        }

        // Properly handle Plugman load/unload.
        List<RegisteredListener> joinListeners = Arrays.stream(PlayerJoinEvent.getHandlerList().getRegisteredListeners())
                .filter(registeredListener -> registeredListener.getPlugin().equals(this))
                .collect(Collectors.toList());

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerJoinEvent event = new PlayerJoinEvent(player, "");

            // Trick all the modules into thinking the player just joined in case the plugin was loaded with Plugman.
            // This way attack speeds, item modifications, etc. will be applied immediately instead of after a re-log.
            for (RegisteredListener registeredListener : joinListeners) {
                try {
                    registeredListener.callEvent(event);
                } catch (EventException e) {
                    e.printStackTrace();
                }
            }
        }

        // Logging to console the enabling of OCM
        logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " has been enabled");
    }

    @Override
    public void onDisable() {

        PluginDescriptionFile pdfFile = this.getDescription();

        for (Runnable disableListener : disableListeners) {
            disableListener.run();
        }

        // Properly handle Plugman load/unload.
        List<RegisteredListener> quitListeners = Arrays.stream(PlayerQuitEvent.getHandlerList().getRegisteredListeners())
                .filter(registeredListener -> registeredListener.getPlugin().equals(this))
                .collect(Collectors.toList());

        // Trick all the modules into thinking the player just quit in case the plugin was unloaded with Plugman.
        // This way attack speeds, item modifications, etc. will be restored immediately instead of after a disconnect.
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerQuitEvent event = new PlayerQuitEvent(player, "");

            for (RegisteredListener registeredListener : quitListeners) {
                try {
                    registeredListener.callEvent(event);
                } catch (EventException e) {
                    e.printStackTrace();
                }
            }
        }

        // Logging to console the disabling of OCM
        logger.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " has been disabled");
    }

    private void registerModules() {
        // Module listeners
        ModuleLoader.addModule(new ModuleAttackCooldown(this));

        //Listeners registered after with same priority appear to be called later

        //Then ModuleSwordBlocking to calculate blocking
        ModuleLoader.addModule(new ModuleShieldDamageReduction(this));

        ModuleLoader.addModule(new ModuleSwordBlocking(this));
        ModuleLoader.addModule(new ModuleGoldenApple(this));
        ModuleLoader.addModule(new ModuleFishingKnockback(this));
        ModuleLoader.addModule(new ModulePlayerRegen(this));

        ModuleLoader.addModule(new ModuleDisableCrafting(this));
        ModuleLoader.addModule(new ModuleDisableBowBoost(this));
        ModuleLoader.addModule(new ModuleProjectileKnockback(this));
        ModuleLoader.addModule(new ModuleNoLapisEnchantments(this));
        //ModuleLoader.addModule(new ModuleDisableEnderpearlCooldown(this));
    }

    public void upgradeConfig() {
        CH.upgradeConfig();
    }

    public boolean doesConfigExist() {
        return CH.doesConfigExist();
    }

    /**
     * Registers a runnable to run when the plugin gets disabled.
     *
     * @param action the {@link Runnable} to run when the plugin gets disabled
     */
    public void addDisableListener(Runnable action) {
        disableListeners.add(action);
    }

    /**
     * Registers a runnable to run when the plugin gets enabled.
     *
     * @param action the {@link Runnable} to run when the plugin gets enabled
     */
    public void addEnableListener(Runnable action) {
        enableListeners.add(action);
    }
}
