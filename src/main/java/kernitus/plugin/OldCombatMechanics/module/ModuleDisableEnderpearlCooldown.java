package kernitus.plugin.OldCombatMechanics.module;

import kernitus.plugin.OldCombatMechanics.OCMMain;
//import net.luminu.paper.event.PlayerItemCooldownEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Allows you to throw enderpearls as often as you like, not only after a cooldown.
 */
public class ModuleDisableEnderpearlCooldown extends Module {

    public ModuleDisableEnderpearlCooldown(OCMMain plugin) {
        super(plugin, "disable-enderpearl-cooldown");
    }

    /*@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerShoot(PlayerItemCooldownEvent event) {
        if (event.getType() == Material.ENDER_PEARL)
            event.setCancelled(true);
    }*/
}
