package kernitus.plugin.OldCombatMechanics.module;

import kernitus.plugin.OldCombatMechanics.OCMMain;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Customize the golden apple effects.
 */
public class ModuleGoldenApple extends Module {

    private List<PotionEffect> enchantedGoldenAppleEffects, goldenAppleEffects;
    private boolean oldPotionEffects;

    public ModuleGoldenApple(OCMMain plugin) {
        super(plugin, "old-golden-apples");
    }

    @Override
    public void reload() {
        enchantedGoldenAppleEffects = getPotionEffects("napple");
        goldenAppleEffects = getPotionEffects("gapple");
        oldPotionEffects = isSettingEnabled("old-potion-effects");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemConsume(PlayerItemConsumeEvent e) {
        if (e.getItem() == null) return;
        if (!oldPotionEffects) return;
        if (e.getItem().getType() != Material.GOLDEN_APPLE && e.getItem().getType() != Material.ENCHANTED_GOLDEN_APPLE)
            return;

        e.setCancelled(true);

        ItemStack originalItem = e.getItem();

        ItemStack item = e.getItem();

        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();

        //Hunger level
        int foodLevel = p.getFoodLevel();
        foodLevel = Math.min(foodLevel + 4, 20);

        item.setAmount(item.getAmount() - 1);

        p.setFoodLevel(foodLevel);

        // Saturation
        // Gapple and Napple saturation is 9.6
        float saturation = p.getSaturation() + 9.6f;
        // "The total saturation never gets higher than the total number of hunger points"
        if (saturation > foodLevel)
            saturation = foodLevel;

        p.setSaturation(saturation);

        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            applyEffects(p, enchantedGoldenAppleEffects);
        } else {
            applyEffects(p, goldenAppleEffects);
        }

        if (item.getAmount() <= 0)
            item = null;

        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();

        if (mainHand.equals(originalItem))
            inv.setItemInMainHand(item);

        else if (offHand.equals(originalItem))
            inv.setItemInOffHand(item);

        else if (mainHand.getType() == Material.GOLDEN_APPLE || mainHand.getType() == Material.ENCHANTED_GOLDEN_APPLE)
            inv.setItemInMainHand(item);
        // The bug occurs here, so we must check which hand has the apples
        // A player can't eat food in the offhand if there is any in the main hand
        // On this principle if there are gapples in the mainhand it must be that one, else it's the offhand
    }

    private List<PotionEffect> getPotionEffects(String apple) {
        List<PotionEffect> appleEffects = new ArrayList<>();

        ConfigurationSection sect = module().getConfigurationSection(apple + "-effects");
        for (String key : sect.getKeys(false)) {
            int duration = sect.getInt(key + ".duration");
            int amplifier = sect.getInt(key + ".amplifier");

            PotionEffectType type = PotionEffectType.getByName(key);
            Objects.requireNonNull(type, String.format("Invalid potion effect type '%s'!", key));

            PotionEffect fx = new PotionEffect(type, duration, amplifier);
            appleEffects.add(fx);
        }
        return appleEffects;
    }

    private void applyEffects(LivingEntity target, List<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            final PotionEffect potionEffect = target.getPotionEffect(effect.getType());
            // the active one is stronger, so do not apply the weaker one
            if (potionEffect != null && potionEffect.getAmplifier() > effect.getAmplifier()) continue;

            // remove it, as the active one is weaker
            target.removePotionEffect(effect.getType());
            target.addPotionEffect(effect);
        }
    }
}
