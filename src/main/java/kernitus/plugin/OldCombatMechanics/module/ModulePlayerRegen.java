package kernitus.plugin.OldCombatMechanics.module;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import kernitus.plugin.OldCombatMechanics.OCMMain;
import kernitus.plugin.OldCombatMechanics.utilities.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Establishes custom health regeneration rules.
 */
public class ModulePlayerRegen extends Module {

    private final Object2LongMap<UUID> healTimes = new Object2LongOpenHashMap<>();
    private long frequency;
    private int amount;
    private float exhaustion;

    public ModulePlayerRegen(OCMMain plugin) {
        super(plugin, "old-player-regen");
    }

    @Override
    public void reload() {
        frequency = module().getInt("frequency") * 1000L;
        amount = module().getInt("amount");
        exhaustion = (float) module().getDouble("exhaustion");
        amount = module().getInt("amount");
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        healTimes.removeLong(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRegen(EntityRegainHealthEvent e) {

        if (e.getEntityType() != EntityType.PLAYER || e.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }

        final Player p = (Player) e.getEntity();

        e.setCancelled(true);

        long lastHealTime = getLastHealTime(p);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastHealTime < frequency) return;

        double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (p.getHealth() < maxHealth) {
            p.setHealth(MathHelper.clamp(p.getHealth() + amount, 0.0, maxHealth));
            healTimes.put(p.getUniqueId(), currentTime);
        }

        final float previousExhaustion = p.getExhaustion();
        //TODO prolly also to do above the earlier return
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            //This is because bukkit doesn't stop the exhaustion change when cancelling the event
            p.setExhaustion(previousExhaustion + exhaustion);
        }, 1L);
    }

    private long getLastHealTime(Player p) {
        return healTimes.getLong(p.getUniqueId());
    }
}
