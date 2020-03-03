package kernitus.plugin.OldCombatMechanics.module;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import kernitus.plugin.OldCombatMechanics.OCMMain;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

import java.util.EnumMap;

/**
 * Brings back the old fishing knockback.
 */
public class ModuleFishingKnockback extends Module {

    private boolean cancelDraggingIn;
    private boolean useEntityDamageEvent;
    private double damage;

    public ModuleFishingKnockback(OCMMain plugin) {
        super(plugin, "old-fishing-knockback");
    }

    @Override
    public void reload() {
        cancelDraggingIn = isSettingEnabled("cancelDraggingIn");
        useEntityDamageEvent = module().getBoolean("useEntityDamageEvent");
        damage = module().getDouble("damage");
        if (damage < 0) damage = 0.2;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRodLand(ProjectileHitEvent e) {
        if (e.getEntityType() != EntityType.FISHING_HOOK)
            return;

        Entity hookEntity = e.getEntity();

        Entity hitEntity = e.getHitEntity();
        if (!(hitEntity instanceof Player)) return;

        FishHook hook = (FishHook) hookEntity;
        Player rodder = (Player) hook.getShooter();
        Player player = (Player) hitEntity;

        //debug("You were hit by a fishing rod!", player);

        if (player.equals(rodder))
            return;

        if (player.getGameMode() == GameMode.CREATIVE) return;

        //Check if cooldown time has elapsed
        if (player.getNoDamageTicks() > player.getMaximumNoDamageTicks() / 2f) {
            return;
        }

        EntityDamageEvent event = makeEvent(rodder, player, damage);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            player.damage(damage);
            player.setVelocity(calculateKnockbackVelocity(player.getVelocity(), player.getLocation(), hook.getLocation()));
        }
    }

    private Vector calculateKnockbackVelocity(Vector currentVelocity, Location player, Location hook) {
        double xDistance = hook.getX() - player.getX();
        double zDistance = hook.getZ() - player.getZ();

        // ensure distance is not zero and randomise in that case (I guess?)
        while (xDistance * xDistance + zDistance * zDistance < 0.0001) {
            xDistance = (Math.random() - Math.random()) * 0.01D;
            zDistance = (Math.random() - Math.random()) * 0.01D;
        }

        double distance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);

        double y = currentVelocity.getY() / 2;
        double x = currentVelocity.getX() / 2;
        double z = currentVelocity.getZ() / 2;

        // Normalize distance to have similar knockback, no matter the distance
        x -= xDistance / distance * 0.4;

        // slow the fall or throw upwards
        y += 0.4;

        // Normalize distance to have similar knockback, no matter the distance
        z -= zDistance / distance * 0.4;

        // do not shoot too high up
        if (y >= 0.4) {
            y = 0.4;
        }

        return new Vector(x, y, z);
    }

    /**
     * This is to cancel dragging the player closer when you reel in
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onReelIn(PlayerFishEvent e) {
        if (!cancelDraggingIn || e.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
        e.getHook().remove(); // Nuke the bobber and don't do anything else
        e.setCancelled(true);
    }

    private EntityDamageEvent makeEvent(Player rodder, Player player, double damage) {
        if (useEntityDamageEvent) {
            return new EntityDamageEvent(player,
                    EntityDamageEvent.DamageCause.PROJECTILE,
                    new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damage)),
                    new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(damage))));
        } else {
            return new EntityDamageByEntityEvent(rodder, player,
                    EntityDamageEvent.DamageCause.PROJECTILE,
                    new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damage)),
                    new EnumMap<>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(damage))));
        }
    }
}