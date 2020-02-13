package kernitus.plugin.OldCombatMechanics.module;

import kernitus.plugin.OldCombatMechanics.OCMMain;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Adds knockback to eggs, snowballs and ender pearls.
 */
public class ModuleProjectileKnockback extends Module {

    private double snowballDamage;
    private double eggDamage;
    private double enderpearlDamage;

    public ModuleProjectileKnockback(OCMMain plugin) {
        super(plugin, "projectile-knockback");
    }

    @Override
    public void reload() {
        snowballDamage = module().getDouble("damage.snowball");
        eggDamage = module().getDouble("damage.egg");
        enderpearlDamage = module().getDouble("damage.ender_pearl");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityHit(EntityDamageByEntityEvent e) {
        EntityType type = e.getDamager().getType();

        switch (type) {
            case SNOWBALL:
                e.setDamage(snowballDamage);
                break;
            case EGG:
                e.setDamage(eggDamage);
                break;
            case ENDER_PEARL:
                e.setDamage(enderpearlDamage);
                break;
            default:
                break;
        }

    }
}