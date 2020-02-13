package kernitus.plugin.OldCombatMechanics.module;

import kernitus.plugin.OldCombatMechanics.OCMMain;
import kernitus.plugin.OldCombatMechanics.utilities.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;

/**
 * Makes the specified materials uncraftable.
 */
public class ModuleDisableCrafting extends Module {

    private EnumSet<Material> denied;

    public ModuleDisableCrafting(OCMMain plugin) {
        super(plugin, "disable-crafting");
    }

    @Override
    public void reload() {
        denied = ConfigUtils.loadMaterialList(module(), "denied");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        if (e.getViewers().size() < 1) return;

        CraftingInventory inv = e.getInventory();
        ItemStack result = inv.getResult();

        if (result != null && denied.contains(result.getType()))
            inv.setResult(null);
    }
}