package kernitus.plugin.OldCombatMechanics.utilities.damage;

import kernitus.plugin.OldCombatMechanics.OCMMain;
import kernitus.plugin.OldCombatMechanics.module.Module;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener extends Module {

    private static boolean enabled;

    public EntityDamageByEntityListener(OCMMain plugin) {
        super(plugin, "entity-damage-listener");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public static void setEnabledStatic(final boolean enabled) {
        EntityDamageByEntityListener.enabled = enabled;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        OCMEntityDamageByEntityEvent e = new OCMEntityDamageByEntityEvent
                (damager, event.getEntity(), event.getCause(), event.getDamage());

        plugin.getServer().getPluginManager().callEvent(e);

        //Re-calculate modified damage and set it back to original event
        // Damage order: base + potion effects + critical hit + enchantments + armour effects
        double newDamage = e.getBaseDamage();

        //Weakness potion
        double weaknessModifier = e.getWeaknessModifier();
        if (e.isWeaknessModifierMultiplier()) newDamage *= weaknessModifier;
        else newDamage += weaknessModifier;

        //Strength potion
        double strengthModifier = e.getStrengthModifier() * e.getStrengthLevel();
        if (!e.isStrengthModifierMultiplier()) newDamage += strengthModifier;
        else if (e.isStrengthModifierAddend()) newDamage *= ++strengthModifier;
        else newDamage *= strengthModifier;

        //Critical hit
        newDamage *= e.getCriticalMultiplier();

        //Enchantments
        newDamage += e.getMobEnchantmentsDamage() + e.getSharpnessDamage();

        if (newDamage < 0) {
            newDamage = 0;
        }

        event.setDamage(newDamage);
    }
}
