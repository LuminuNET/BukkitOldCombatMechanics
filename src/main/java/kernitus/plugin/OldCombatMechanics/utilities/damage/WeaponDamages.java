package kernitus.plugin.OldCombatMechanics.utilities.damage;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import kernitus.plugin.OldCombatMechanics.OCMMain;
import kernitus.plugin.OldCombatMechanics.utilities.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class WeaponDamages {

    private static Object2DoubleMap<Material> damages;

    public static void reload() {
        ConfigurationSection section = OCMMain.getInstance().getConfig().getConfigurationSection("old-tool-damage.damages");

        final Map<String, Double> damages = ConfigUtils.loadDoubleMap(section);
        WeaponDamages.damages = new Object2DoubleOpenHashMap<>();
        for (Map.Entry<String, Double> entry : damages.entrySet()) {
            final Material material = Material.getMaterial(entry.getKey().toUpperCase());
            if (material == null) {
                OCMMain.getInstance().getLogger().warning("Unknown material in damages list: " + entry.getKey());
                continue;
            }
            WeaponDamages.damages.put(material, (double) entry.getValue());
        }
    }

    public static double getDamage(Material mat) {
        //Replace 1.14 material names to ones used in config.yml
        return damages.getOrDefault(mat, 1);
    }
}
