package kernitus.plugin.OldCombatMechanics.utilities.damage;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OCMEntityDamageByEntityEvent extends Event implements Cancellable {

    private boolean cancelled;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Entity damager;
    private final Entity damagee;
    private final DamageCause cause;
    private final double rawDamage;

    private ItemStack weapon;
    private int sharpnessLevel;
    private int strengthLevel;


    private double baseDamage = 0, mobEnchantmentsDamage = 0, sharpnessDamage = 0, criticalMultiplier = 1;
    private double strengthModifier = 0, weaknessModifier = 0;

    // In 1.9 strength modifier is an addend, in 1.8 it is a multiplier and addend (+130%)
    private boolean isStrengthModifierMultiplier = false;
    private boolean isStrengthModifierAddend = true;
    private boolean isWeaknessModifierMultiplier = false;

    public OCMEntityDamageByEntityEvent(Entity damager, Entity damagee, DamageCause cause, double rawDamage) {

        this.damager = damager;
        this.damagee = damagee;
        this.cause = cause;
        this.rawDamage = rawDamage;

        if (!(damager instanceof LivingEntity)) {
            setCancelled(true);
            return;
        }

        LivingEntity le = (LivingEntity) damager;

        EntityEquipment equipment = le.getEquipment();
        weapon = equipment.getItemInMainHand();
        // Yay paper. Why do you need to return null here?
        if (weapon == null) {
            weapon = new ItemStack(Material.AIR);
        }

        EntityType entity = damagee.getType();

        mobEnchantmentsDamage = MobDamage.applyEntityBasedDamage(entity, weapon, rawDamage) - rawDamage;

        sharpnessLevel = weapon.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        sharpnessDamage = DamageUtils.getNewSharpnessDamage(sharpnessLevel);

        //Amount of damage including potion effects and critical hits
        double tempDamage = rawDamage - mobEnchantmentsDamage - sharpnessDamage;

        //Check if it's a critical hit
        if (le instanceof Player) {
            Player player = (Player) le;
            if (DamageUtils.isCriticalHit(player)) {
                criticalMultiplier = 1.5;
                tempDamage /= 1.5;
            }
        }

        //amplifier 0 = Strength I    amplifier 1 = Strength II
        final PotionEffect potionEffect = le.getPotionEffect(PotionEffectType.INCREASE_DAMAGE);
        int amplifier = potionEffect != null ? potionEffect.getAmplifier() : -1;

        strengthLevel = ++amplifier;

        strengthModifier = strengthLevel * 3;

        if (le.hasPotionEffect(PotionEffectType.WEAKNESS)) weaknessModifier = -4;

        baseDamage = tempDamage + weaknessModifier - strengthModifier;
    }

    public Entity getDamager() {
        return damager;
    }

    public Entity getDamagee() {
        return damagee;
    }

    public DamageCause getCause() {
        return cause;
    }

    public double getRawDamage() {
        return rawDamage;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public int getSharpnessLevel() {
        return sharpnessLevel;
    }

    public double getStrengthModifier() {
        return strengthModifier;
    }

    public void setStrengthModifier(double strengthModifier) {
        this.strengthModifier = strengthModifier;
    }

    public int getStrengthLevel() {
        return strengthLevel;
    }

    public double getWeaknessModifier() {
        return weaknessModifier;
    }

    public void setWeaknessModifier(double weaknessModifier) {
        this.weaknessModifier = weaknessModifier;
    }

    public boolean isStrengthModifierMultiplier() {
        return isStrengthModifierMultiplier;
    }

    public void setIsStrengthModifierMultiplier(boolean isStrengthModifierMultiplier) {
        this.isStrengthModifierMultiplier = isStrengthModifierMultiplier;
    }

    public void setIsStrengthModifierAddend(boolean isStrengthModifierAddend) {
        this.isStrengthModifierAddend = isStrengthModifierAddend;
    }

    public boolean isWeaknessModifierMultiplier() {
        return isWeaknessModifierMultiplier;
    }

    public void setIsWeaknessModifierMultiplier(boolean weaknessModifierMultiplier) {
        isWeaknessModifierMultiplier = weaknessModifierMultiplier;
    }

    public boolean isStrengthModifierAddend() {
        return isStrengthModifierAddend;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(double baseDamage) {
        this.baseDamage = baseDamage;
    }

    public double getMobEnchantmentsDamage() {
        return mobEnchantmentsDamage;
    }

    public void setMobEnchantmentsDamage(double mobEnchantmentsDamage) {
        this.mobEnchantmentsDamage = mobEnchantmentsDamage;
    }

    public double getSharpnessDamage() {
        return sharpnessDamage;
    }

    public void setSharpnessDamage(double sharpnessDamage) {
        this.sharpnessDamage = sharpnessDamage;
    }

    public double getCriticalMultiplier() {
        return criticalMultiplier;
    }

    public void setCriticalMultiplier(double criticalMultiplier) {
        this.criticalMultiplier = criticalMultiplier;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
