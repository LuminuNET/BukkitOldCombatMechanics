package kernitus.plugin.OldCombatMechanics.module;

import kernitus.plugin.OldCombatMechanics.OCMMain;
import kernitus.plugin.OldCombatMechanics.utilities.damage.DamageUtils;
import kernitus.plugin.OldCombatMechanics.utilities.damage.OCMEntityDamageByEntityEvent;
import kernitus.plugin.OldCombatMechanics.utilities.damage.WeaponDamages;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;

import java.util.EnumSet;

/**
 * Restores old tool damage.
 */
public class ModuleOldToolDamage extends Module {

    private final EnumSet<Material> tools = EnumSet.of(Material.DIAMOND_SWORD, Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD,
            Material.DIAMOND_HOE, Material.GOLDEN_HOE, Material.IRON_HOE, Material.STONE_HOE, Material.WOODEN_HOE,
            Material.DIAMOND_SHOVEL, Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.STONE_SHOVEL, Material.WOODEN_SHOVEL,
            Material.DIAMOND_PICKAXE, Material.GOLDEN_PICKAXE, Material.IRON_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE,
            Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.STONE_AXE, Material.WOODEN_AXE);

    public ModuleOldToolDamage(OCMMain plugin) {
        super(plugin, "old-tool-damage");
    }

    @Override
    public void reload() {
        WeaponDamages.reload();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamaged(OCMEntityDamageByEntityEvent event) {
        Material weaponMaterial = event.getWeapon().getType();

        if (!tools.contains(weaponMaterial)) return;

        double weaponDamage = WeaponDamages.getDamage(weaponMaterial);
        if (weaponDamage <= 0) weaponDamage = 1;

        double oldBaseDamage = event.getBaseDamage();

        event.setBaseDamage(weaponDamage);

        // Set sharpness to 1.8 damage value
        double newSharpnessDamage = DamageUtils.getOldSharpnessDamage(event.getSharpnessLevel());
        event.setSharpnessDamage(newSharpnessDamage);
    }
}
