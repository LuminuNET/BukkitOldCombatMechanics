package kernitus.plugin.OldCombatMechanics.module;

import kernitus.plugin.OldCombatMechanics.OCMMain;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Disables the attack cooldown.
 */
public class ModuleAttackCooldown extends Module {

    private double cooldown;

    public ModuleAttackCooldown(OCMMain plugin) {
        super(plugin, "disable-attack-cooldown");
    }

    @Override
    public void reload() {
        cooldown = module().getDouble("generic-attack-speed");
        for (Player player : Bukkit.getOnlinePlayers()) {
            adjustAttackSpeed(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerJoinEvent e) {
        adjustAttackSpeed(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        adjustAttackSpeed(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        resetAttackSpeed(player);
    }

    /**
     * Adjusts the attack speed to the default or configured value, depending on whether the module is enabled.
     *
     * @param player the player to set it for
     */
    private void adjustAttackSpeed(Player player) {
        if (!isEnabled()) {
            resetAttackSpeed(player);
        } else {
            setAttackSpeed(player, cooldown);
        }
    }

    /**
     * Sets the attack speed to the given value.
     *
     * @param player      the player to set it for
     * @param attackSpeed the attack speed to set it to
     */
    private void setAttackSpeed(Player player, double attackSpeed) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        double baseValue = attribute.getBaseValue();

        if (baseValue != attackSpeed) {
            attribute.setBaseValue(attackSpeed);
            player.saveData();
        }
    }

    private void resetAttackSpeed(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attribute.getBaseValue() != attribute.getDefaultValue()) {
            attribute.setBaseValue(attribute.getDefaultValue());
            player.saveData();
        }
    }
}
