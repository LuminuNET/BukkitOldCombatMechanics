package kernitus.plugin.OldCombatMechanics.module;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedParticle;
import kernitus.plugin.OldCombatMechanics.OCMMain;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

/**
 * A module to disable the sweep attack.
 */
public class ModuleSwordSweep extends Module {

    private final ParticleListener particleListener;

    public ModuleSwordSweep(OCMMain plugin) {
        super(plugin, "disable-sword-sweep");

        particleListener = new ParticleListener(plugin);
        ProtocolLibrary.getProtocolManager().addPacketListener(particleListener);
    }

    @Override
    public void reload() {
        particleListener.enabled = isEnabled();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamaged(final EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        if (!(damager instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            event.setCancelled(true);
        }
    }

    private static final class ParticleListener extends PacketAdapter {

        private boolean enabled;

        private ParticleListener(final Plugin plugin) {
            super(plugin, PacketType.Play.Server.WORLD_PARTICLES);
        }

        @Override
        public void onPacketSending(final PacketEvent event) {
            if (!enabled) return;
            final WrappedParticle<?> particle = event.getPacket().getNewParticles().read(0);
            if (particle.getParticle() == Particle.SWEEP_ATTACK) {
                event.setCancelled(true);
            }
        }
    }
}