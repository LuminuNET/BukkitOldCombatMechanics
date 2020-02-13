package kernitus.plugin.OldCombatMechanics.module;

import kernitus.plugin.OldCombatMechanics.OCMMain;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Allows enchanting without needing lapis.
 */
public class ModuleNoLapisEnchantments extends Module {

    private final ItemStack lapis;

    public ModuleNoLapisEnchantments(OCMMain plugin) {
        super(plugin, "no-lapis-enchantments");
        this.lapis = new ItemStack(Material.LAPIS_LAZULI, 3);
    }

    @EventHandler
    public void inventoryOpen(final InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.ENCHANTING)
            event.getInventory().setItem(1, lapis);
    }

    @EventHandler
    public void inventoryClose(final InventoryCloseEvent event) {
        if (event.getInventory().getType() == InventoryType.ENCHANTING)
            event.getInventory().setItem(1, null);
    }

    @EventHandler
    public void inventoryClick(final InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.ENCHANTING && event.getSlot() == 1)
            event.setCancelled(true);
    }

    @EventHandler
    public void enchantItem(final EnchantItemEvent event) {
        event.getInventory().setItem(1, lapis);
    }
}
