package rafradek.TF2weapons.common;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityProjectileSimple;
import rafradek.TF2weapons.item.ItemAirblast;
import rafradek.TF2weapons.item.ItemBackpack;
import rafradek.TF2weapons.item.ItemBulletWeapon;
import rafradek.TF2weapons.item.ItemChargingTarge;
import rafradek.TF2weapons.item.ItemCloak;
import rafradek.TF2weapons.item.ItemFlameThrower;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemJetpack;
import rafradek.TF2weapons.item.ItemKnife;
import rafradek.TF2weapons.item.ItemMedigun;
import rafradek.TF2weapons.item.ItemMinigun;
import rafradek.TF2weapons.item.ItemPDA;
import rafradek.TF2weapons.item.ItemProjectileWeapon;
import rafradek.TF2weapons.item.ItemSniperRifle;
import rafradek.TF2weapons.item.ItemSoldierBackpack;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.item.ItemWrench;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;

public class TF2Attribute {

	public static TF2Attribute[] attributes = new TF2Attribute[256];

	public static List<TF2Attribute> listUpgrades;

	public int id;
	public String name;
	public Type typeOfValue;
	public String effect;
	public float defaultValue;
	public State state;

	private Predicate<ItemStack> canApply = Predicates.alwaysFalse();

	public int numLevels;

	public float perLevel;

	public float perKill;

	public int cost;

	public int weight;

	public float austrUpgrade;

	private boolean fullCost;

	public static final Predicate<ItemStack> ITEM_WEAPON = input -> (input.getItem() instanceof ItemWeapon);
	public static final Predicate<ItemStack> NOT_FLAMETHROWER = input -> (input.getItem() instanceof ItemWeapon
			&& !(input.getItem() instanceof ItemFlameThrower));
	public static final Predicate<ItemStack> FLAMETHROWER = input -> (input.getItem() instanceof ItemFlameThrower);
	public static final Predicate<ItemStack> IGNITE = input -> (input.getItem() instanceof ItemAirblast
			|| getModifier("BurnOnHit", input, 0, null) > 0);
	public static final Predicate<ItemStack> WITH_CLIP = input -> (input.getItem() instanceof ItemWeapon
			&& ((ItemWeapon) input.getItem()).hasClip(input));
	public static final Predicate<ItemStack> WITH_SPREAD = input -> (input.getItem() instanceof ItemWeapon
			&& (((ItemWeapon) input.getItem()).getWeaponSpreadBase(input, null) != 0
					|| ((ItemWeapon) input.getItem()).getWeaponMinDamage(input, null) != 1));
	public static final Predicate<ItemStack> WITH_AMMO = input -> (input.getItem() instanceof ItemUsable
			&& ItemFromData.getData(input).getInt(PropertyType.AMMO_TYPE) != 0);
	public static final Predicate<ItemStack> CHARGE_RATE = input -> (input.getItem() instanceof ItemSniperRifle
			|| input.getItem() instanceof ItemChargingTarge || input.getItem() instanceof ItemCloak);
	public static final Predicate<ItemStack> DURATION = input -> (input.getItem() instanceof ItemChargingTarge
			|| input.getItem() instanceof ItemSoldierBackpack || input.getItem() instanceof ItemCloak);
	public static final Predicate<ItemStack> ITEM_BULLET = input -> {
		if (input.getItem() instanceof ItemBulletWeapon
				&& !ItemFromData.getData(input).hasProperty(PropertyType.PROJECTILE))
			return true;
		else {
			Class<?> clazz = MapList.projectileClasses
					.get(ItemFromData.getData(input).getString(PropertyType.PROJECTILE));
			return clazz != null && EntityProjectileSimple.class.isAssignableFrom(clazz);
		}
	};
	public static final Predicate<ItemStack> ITEM_PROJECTILE = input -> input.getItem() instanceof ItemProjectileWeapon
			|| ItemFromData.getData(input).hasProperty(PropertyType.PROJECTILE);
	public static final Predicate<ItemStack> ITEM_MINIGUN = input -> input.getItem() instanceof ItemMinigun;
	public static final Predicate<ItemStack> ITEM_SNIPER_RIFLE = input -> input.getItem() instanceof ItemSniperRifle;
	public static final Predicate<ItemStack> EXPLOSIVE = input -> (input.getItem() instanceof ItemProjectileWeapon
			|| ItemFromData.getData(input).hasProperty(PropertyType.PROJECTILE))
			&& !(input.getItem() instanceof ItemFlameThrower || (MapList.projectileClasses
					.get(ItemFromData.getData(input).getString(PropertyType.PROJECTILE)) != null
					&& EntityProjectileSimple.class.isAssignableFrom(MapList.projectileClasses
							.get(ItemFromData.getData(input).getString(PropertyType.PROJECTILE)))));
	public static final Predicate<ItemStack> MEDIGUN = input -> input.getItem() instanceof ItemMedigun;
	public static final Predicate<ItemStack> BANNER = input -> input.getItem() instanceof ItemSoldierBackpack;
	public static final Predicate<ItemStack> BACKPACK = input -> input.getItem() instanceof ItemBackpack;
	public static final Predicate<ItemStack> SHIELD = input -> input.getItem() instanceof ItemChargingTarge;
	public static final Predicate<ItemStack> WATCH = input -> input.getItem() instanceof ItemCloak;
	public static final Predicate<ItemStack> WRENCH = input -> input.getItem() instanceof ItemWrench;
	public static final Predicate<ItemStack> JETPACK = input -> input.getItem() instanceof ItemJetpack;
	public static final Predicate<ItemStack> PDA = input -> input.getItem() instanceof ItemPDA;
	public static final Predicate<ItemStack> JUMPER = input -> getModifier("Self Damage", input, 1, null) <= 0;
	public static final Predicate<ItemStack> ROCKET = input -> ItemFromData.isSameType(input, "rocketlauncher");
	public static final Predicate<ItemStack> GRENADE = input -> ItemFromData.isSameType(input, "grenadelauncher");
	public static final Predicate<ItemStack> KNIFE = input -> input.getItem() instanceof ItemKnife;

	public static enum Type {
		PERCENTAGE, INVERTED_PERCENTAGE, ADDITIVE;
	}

	public static enum State {
		POSITIVE, NEGATIVE, NEUTRAL, HIDDEN;
	}

	public TF2Attribute(int id, String name, String effect, Type typeOfValue, float defaultValue, State state) {
		this.id = id;
		attributes[id] = this;
		MapList.nameToAttribute.put(name, this);
		this.name = name;
		this.effect = effect;
		this.typeOfValue = typeOfValue;
		this.defaultValue = defaultValue;
		this.state = state;

	}

	public TF2Attribute setUpgrade(Predicate<ItemStack> canApply, float perLevel, int numLevels, int cost, int weight) {
		this.canApply = canApply;
		this.numLevels = numLevels;
		this.perLevel = perLevel;
		this.cost = cost;
		this.weight = weight;
		return this;
	}

	public TF2Attribute setKillstreak(float perKill) {
		this.perKill = perKill;
		return this;
	}

	public TF2Attribute setAustralium(float austr) {
		this.austrUpgrade = austr;
		return this;
	}

	public TF2Attribute setNoCostReduce() {
		this.fullCost = true;
		return this;
	}

	public static void initAttributes() {
		new TF2Attribute(0, "DamageBonus", "Damage", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(Predicates.and(ITEM_WEAPON, Predicates.not(KNIFE)), 0.20f, 5, 160, 8).setKillstreak(0.04f)
				.setAustralium(1f).setNoCostReduce();
		new TF2Attribute(1, "DamagePenalty", "Damage", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(2, "ClipSizeBonus", "Clip Size", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(WITH_CLIP, 0.5f, 4, 120, 6).setKillstreak(0.09f).setAustralium(1f);
		new TF2Attribute(3, "ClipSizePenalty", "Clip Size", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(4, "MinigunSpinBonus", "Minigun Spinup", Type.INVERTED_PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(5, "MinigunSpinPenalty", "Minigun Spinup", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(6, "FireRateBonus", "Fire Rate", Type.INVERTED_PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(NOT_FLAMETHROWER, -0.08f, 5, 80, 8).setKillstreak(-0.043f).setAustralium(1.5f);
		new TF2Attribute(7, "FireRatePenalty", "Fire Rate", Type.INVERTED_PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(8, "SpreadBonus", "Spread", Type.INVERTED_PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(9, "SpreadPenalty", "Spread", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(10, "PelletBonus", "Pellet Count", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(11, "PelletPenalty", "Pellet Count", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(12, "ReloadRateBonus", "Reload Time", Type.INVERTED_PERCENTAGE, 1f, State.POSITIVE).setUpgrade(
				Predicates.and(WITH_CLIP,
						stack -> !ItemFromData.getData(stack).getBoolean(PropertyType.RELOADS_FULL_CLIP)),
				-0.2f, 3, 100, 5).setKillstreak(-0.05f).setAustralium(0.75f).setNoCostReduce();
		new TF2Attribute(13, "ReloadRatePenalty", "Reload Time", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(14, "KnockbackBonus", "Knockback", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(15, "KnockbackPenalty", "Knockback", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(16, "ChargeBonus", "Charge", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(CHARGE_RATE, 0.25f, 4, 100, 8).setKillstreak(0.055f).setAustralium(1.6f);
		new TF2Attribute(17, "ChargePenalty", "Charge", Type.INVERTED_PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(18, "SpreadAdd", "Spread", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(19, "ProjectileSpeedBonus", "Proj Speed", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(ITEM_PROJECTILE, 0.25f, 4, 80, 6).setAustralium(2f);
		new TF2Attribute(20, "ProjectileSpeedPenalty", "Proj Speed", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(21, "ExplosionRadiusBonus", "Explosion Radius", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(EXPLOSIVE, 0.2f, 4, 80, 2).setAustralium(1.5f);
		new TF2Attribute(22, "ExplosionRadiusPenalty", "Explosion Radius", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(23, "DestroyOnImpact", "Coll Remove", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(24, "AmmoEfficiencyBonus", "Ammo Eff", Type.INVERTED_PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(WITH_AMMO, -0.2f, 3, 120, 6).setKillstreak(-0.04f).setAustralium(0.75f);
		new TF2Attribute(25, "AmmoEfficiencyPenalty", "Ammo Eff", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(26, "Penetration", "Penetration", Type.ADDITIVE, 0, State.POSITIVE).setUpgrade(ITEM_BULLET, 1,
				1, 200, 1);
		new TF2Attribute(27, "HealRateBonus", "Heal", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(MEDIGUN, 0.25f, 4, 100, 8).setAustralium(1f);
		new TF2Attribute(28, "HealRatePenalty", "Heal", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(29, "OverHealBonus", "Overheal", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(MEDIGUN, 0.25f, 4, 100, 4).setAustralium(1f);
		new TF2Attribute(30, "OverHealPenalty", "Overheal", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(31, "BurnTimeBonus", "Burn Time", Type.PERCENTAGE, 1f, State.POSITIVE).setUpgrade(IGNITE, 0.5f,
				4, 140, 3);
		new TF2Attribute(32, "BurnTimePenalty", "Burn Time", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(33, "HealthOnKill", "Health Kill", Type.ADDITIVE, 0, State.POSITIVE).setUpgrade(ITEM_WEAPON,
				2.0f, 4, 80, 2);
		new TF2Attribute(34, "AccuracyBonus", "Accuracy", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(WITH_SPREAD, 0.25f, 3, 160, 4).setKillstreak(0.05f).setAustralium(1f);
		new TF2Attribute(35, "EffectDurationBonus", "Effect Duration", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(36, "FlameRangeBonus", "Flame Range", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(FLAMETHROWER, 0.25f, 4, 80, 5);
		new TF2Attribute(37, "CritBurning", "Crit Burn", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(38, "BurnOnHit", "Burn Hit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(39, "DestroyBlock", "Destroy Block", Type.ADDITIVE, 0, State.POSITIVE)
				.setUpgrade(Predicates.or(ITEM_BULLET, EXPLOSIVE), 1f, 2, 200, 1).setAustralium(0.75f);
		new TF2Attribute(40, "NoRandomCrit", "Random Crit", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(41, "CritRocket", "Crit Rocket", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(42, "CritMini", "Crit Mini", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(43, "UberOnHit", "Uber Hit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(44, "BallRelease", "Ball Release", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(45, "HealthPenalty", "Health", Type.ADDITIVE, 0f, State.HIDDEN);
		new TF2Attribute(46, "MovementBonus", "Speed", Type.PERCENTAGE, 1, State.HIDDEN);
		new TF2Attribute(47, "MarkForDeathSelf", "Mark Death", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(48, "CritOnKill", "Crit Kill", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(49, "FireResistBonus", "Fire Resist", Type.INVERTED_PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(50, "DamageResistPenalty", "Damage Resist", Type.INVERTED_PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(51, "ExplosionResistBonus", "Explosion Resist", Type.INVERTED_PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(52, "DamageNonBurnPenalty", "Damage Non Burn", Type.PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(53, "CollectHeads", "Kill Count", Type.ADDITIVE, 0, State.HIDDEN);
		new TF2Attribute(54, "ExplodeDeath", "Explode Death", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(55, "RechargeBonus", "Charge", Type.PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(56, "UberRateBonus", "Uber Rate", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(MEDIGUN, 0.25f, 4, 120, 1).setAustralium(1f);
		new TF2Attribute(57, "UberRatePenalty", "Uber Rate", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(58, "RangeIncrease", "Range", Type.PERCENTAGE, 1f, State.NEUTRAL);
		new TF2Attribute(59, "ExplosiveBullets", "Explode Bullet", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(60, "WeaponMode", "Weapon Mode", Type.ADDITIVE, 0, State.HIDDEN);
		new TF2Attribute(61, "HealthOnHit", "Health Hit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(62, "Breakable", "Breakable", Type.ADDITIVE, 0, State.HIDDEN);
		new TF2Attribute(63, "Focus", "Focus", Type.ADDITIVE, 0, State.POSITIVE).setUpgrade(ITEM_WEAPON, 1, 2, 200, 1);
		new TF2Attribute(64, "KnockbackFAN", "KnockbackFAN", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(65, "MinicritAirborne", "Minicrit Airborne", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(66, "CannotAirblast", "Cannot Airblast", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(67, "RageCrit", "Rage Crit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(68, "NoAmmo", "No Ammo", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(69, "DamageBurnBonus", "Damage Burning", Type.PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(70, "RingFire", "Ring Fire", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(71, "AmmoDrainSpinned", "Ammo Spinned", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(72, "StickybombBonus", "Stickybomb Count", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(73, "StickyControl", "Weapon Mode", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(74, "ArmTimePenalty", "Arm Time", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(75, "RepairBuilding", "Repair Building", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(76, "PickBuilding", "Pick Building", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(77, "Headshot", "Headshot", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(78, "HealTarget", "Heal Target", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(79, "NeedScope", "Weapon Mode", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(80, "DamageBonusCharged", "Damage Charged", Type.PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(81, "SelfDamageReduced", "Self Damage", Type.PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(82, "StickybombPenalty", "Stickybomb Count", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(83, "SpeedOnHitAlly", "Speed Hit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(84, "ChargedGrenades", "Charged Grenades", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(85, "MetalAsAmmo", "Metal Ammo", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(86, "MetalOnHit", "Metal Hit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(87, "EffectDurationBonus", "Effect Duration", Type.PERCENTAGE, 1, State.POSITIVE)
				.setUpgrade(DURATION, 0.25f, 3, 120, 4).setAustralium(1f);
		new TF2Attribute(88, "CloakRegenBonus", "Charge", Type.PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(89, "CloakDrainActivate", "Cloak Drain", Type.PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(90, "NoExternalCloak", "No External Cloak", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(91, "CritStunned", "Crit Stun", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(92, "BleedingDuration", "Bleed", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(93, "MiniCritDistance", "Minicrit Distance", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(94, "MaxHealthOnKill", "Max Health Kill", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(95, "SpeedOnKill", "Speed Kill", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(96, "ClipOnKill", "Clip Kill", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(97, "AirborneBonus", "Airborne Bonus", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(98, "DestroyProjectiles", "Destroy Projectiles", Type.ADDITIVE, 0, State.POSITIVE)
				.setUpgrade(ITEM_BULLET, 1, 2, 180, 1);
		new TF2Attribute(99, "KnockbackRage", "Knockback Rage", Type.ADDITIVE, 0, State.POSITIVE)
				.setUpgrade(ITEM_MINIGUN, 1, 3, 120, 1);
		new TF2Attribute(100, "DeployTimeBonus", "Deploy Time", Type.INVERTED_PERCENTAGE, 0, State.POSITIVE);
		new TF2Attribute(101, "FireRateHealthBonus", "Fire Rate Health", Type.PERCENTAGE, 0, State.POSITIVE);
		new TF2Attribute(102, "SpreadHealthPenalty", "Spread Health", Type.PERCENTAGE, 0, State.NEGATIVE);
		new TF2Attribute(103, "AutoFireClip", "Auto Fire", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(104, "HealthRegen", "Health Regen", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(105, "Gravity", "Gravity", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(106, "FireRateHitBonus", "Fire Rate Hit", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(107, "ConstructionRateBonus", "Construction Rate", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(WRENCH, 0.4f, 3, 160, 4);
		new TF2Attribute(108, "ConstructionRatePenalty", "Construction Rate", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(109, "UpgradeRatePenalty", "Upgrade Rate", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(110, "TeleportCost", "Teleporter Cost", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(111, "MetalUsedOnHitPenalty", "Metal Used", Type.INVERTED_PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(112, "Looting", "Looting", Type.ADDITIVE, 0, State.POSITIVE).setUpgrade(ITEM_WEAPON, 1, 3, 220,
				3);
		new TF2Attribute(113, "ArmorBonus", "Armor", Type.ADDITIVE, 0, State.HIDDEN).setUpgrade(BACKPACK, 1, 3, 240, 5);
		new TF2Attribute(114, "MeleeResistPenalty", "Melee Resist", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(115, "RangedResistBonus", "Ranged Resist", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(116, "HolsterTimePenalty", "Holster Time", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(117, "Unblockable", "Unblockable", Type.ADDITIVE, 1f, State.POSITIVE);
		new TF2Attribute(118, "SelfPushForceBonus", "Self Push Force", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(Predicates.or(JUMPER, JETPACK), 0.20f, 5, 160, 4).setAustralium(1.25f);
		new TF2Attribute(119, "Jetpack", "Jetpack", Type.ADDITIVE, 0f, State.POSITIVE).setUpgrade(JETPACK, 1f, 1, 250,
				4);
		new TF2Attribute(120, "SentryBonus", "Sentry Bonus", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(121, "BuildingHealthBonus", "Building Health", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(PDA, 0.6f, 5, 240, 6).setAustralium(0.8f);
		new TF2Attribute(122, "SentryFireRateBonus", "Sentry Fire Rate", Type.INVERTED_PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(PDA, -0.1f, 3, 160, 6).setAustralium(1f);
		new TF2Attribute(123, "DispenserRangeBonus", "Dispenser Range", Type.PERCENTAGE, 1f, State.POSITIVE)
				.setUpgrade(PDA, 1, 4, 120, 4);
		new TF2Attribute(124, "ExtraSentryBonus", "Extra Sentry", Type.ADDITIVE, 0f, State.POSITIVE)
				.setUpgrade(PDA, 1, 3, 200, 3).setAustralium(1f);
		new TF2Attribute(125, "OnyxProjectile", "Onyx Projectile", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(126, "AirblastRatePenalty", "Airblast Rate", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(127, "NoHeadshot", "No Headshot", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(128, "JarateOnHit", "Jarate Hit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(129, "NoBackstab", "No Backstab", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(130, "AfterburnReductionBonus", "Afterburn Reduction", Type.INVERTED_PERCENTAGE, 0,
				State.POSITIVE);
		new TF2Attribute(131, "ChargesBonus", "Charges", Type.ADDITIVE, 0f, State.POSITIVE).setUpgrade(JETPACK, 1f, 3,
				200, 6);
		new TF2Attribute(132, "DamageBuildingPenalty", "Damage Building", Type.PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(133, "HitCrit", "Hit Crit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(134, "PiercingShots", "Piercing", Type.ADDITIVE, 0, State.POSITIVE).setUpgrade(PDA, 1, 2, 180,
				3);
		new TF2Attribute(135, "JetpackNoItem", "Jetpack Item", Type.ADDITIVE, 0f, State.POSITIVE).setUpgrade(JETPACK,
				1f, 1, 320, 3);
		new TF2Attribute(136, "SelfDamageIncreased", "Self Damage", Type.PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(137, "MiniCritBurning", "Crit Burn", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(138, "DetonateFlare", "Detonate", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(139, "BombEnemy", "Bomb Enemy", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(140, "SilentKill", "Silent Kill", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(141, "DisguiseBackstab", "Disguise Backstab", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(142, "NoDisguiseKit", "No Disguise Kit", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(143, "SentryRangeBonus", "Sentry Range", Type.PERCENTAGE, 1, State.POSITIVE).setUpgrade(PDA,
				0.1f, 3, 160, 2);
		new TF2Attribute(144, "MetalBonus", "Max Metal", Type.PERCENTAGE, 1, State.POSITIVE).setUpgrade(PDA, 0.5f, 4,
				140, 5);
		new TF2Attribute(145, "DamagePlayerPenalty", "Damage Player", Type.PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(146, "MinicritAirborneSelf", "Minicrit Airborne Self", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(147, "DeployTimePenalty", "Deploy Time", Type.PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(148, "GrantsTripleJump", "Triple Jump", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(149, "DealDamageRage", "Build Rage Damage", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(150, "MinicritRage", "Minicrit Rage", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(151, "RocketSpecialist", "Rocket Specialist", Type.ADDITIVE, 0, State.POSITIVE)
				.setUpgrade(ROCKET, 1f, 4, 80, 3);
		new TF2Attribute(152, "GrenadeSpecialist", "Grenade Specialist", Type.ADDITIVE, 0, State.POSITIVE)
				.setUpgrade(GRENADE, 1f, 3, 160, 3);
		new TF2Attribute(153, "FireSpecialist", "Fire Specialist", Type.ADDITIVE, 0, State.POSITIVE)
				.setUpgrade(FLAMETHROWER, 1f, 2, 120, 2);
		new TF2Attribute(154, "RegenOnHit", "Regen On Hit", Type.ADDITIVE, 0, State.POSITIVE)
				.setUpgrade(ITEM_WEAPON, 1f, 4, 80, 5).setAustralium(1.75f);
		new TF2Attribute(155, "TraceRound", "Trace Round", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(156, "LifeSteal", "Life Steal", Type.ADDITIVE, 0, State.POSITIVE).setUpgrade(MEDIGUN, 1f, 3,
				120, 2);
		new TF2Attribute(157, "BackstabDamageBonus", "Backstab Damage", Type.PERCENTAGE, 1, State.POSITIVE)
				.setUpgrade(KNIFE, 0.25f, 4, 240, 6).setAustralium(1f).setNoCostReduce();
		new TF2Attribute(158, "StunOnHit", "Stun On Hit", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(159, "FuseTimeBonus", "Fuse Time", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(160, "OverHealRatePenalty", "Overheal Rate", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(161, "UberRateOverhealPenalty", "Uber Overheal Rate", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(162, "UberShield", "Uber Shield", Type.ADDITIVE, 0f, State.HIDDEN);
		new TF2Attribute(163, "PassiveShield", "Passive Shield", Type.ADDITIVE, 0f, State.HIDDEN);
		new TF2Attribute(164, "CanOverload", "Overload", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(165, "UberTimeBonus", "Uber Time", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(166, "UberOnHitRemove", "Uber Hit Remove", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(167, "CloakOnHitRemove", "Cloak Hit Remove", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(168, "FireResistPenalty", "Fire Resist", Type.PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(169, "AltFireChargeShot", "Alt Fire Charge Shot", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(170, "CritsBecomeMiniCrits", "Crits Become Mini Crits", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(171, "MedicHealingDecreased", "Medic Heal", Type.PERCENTAGE, 1, State.NEGATIVE);
		new TF2Attribute(172, "DamageHealthBonus", "Damage Health", Type.PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(173, "MoveSpeedHealthBonus", "Move Speed Health", Type.PERCENTAGE, 1, State.POSITIVE);
		new TF2Attribute(174, "IncreaseCaptureRate", "Capture Rate", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(175, "AirblastCostPenalty", "Airblast Cost", Type.PERCENTAGE, 0, State.NEGATIVE);
		new TF2Attribute(176, "CritFromBehind", "Crit Behind", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(177, "HolsterTimeBonus", "Holster Time", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(178, "SelfPushForcePenalty", "Self Push Force", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(179, "DamageBuildingBonus", "Damage Building", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(180, "DamagePlayersPenalty", "Damage Players", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(181, "HealthFromHealersPenalty", "Healh From Healers", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(182, "HealthFromKitsBonus", "Healh From Kits", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(183, "CanRemoveSappers", "Remove Sappers", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(184, "CritWet", "Crit Wet", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(185, "DoubleAttack", "Double Attack", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(186, "SpeedOnHit", "Speed Hit", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(187, "HypeJump", "Hype Jump", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(188, "SpeedRage", "Speed Rage", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(189, "JumpRageReduction", "Jump Add Rage", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(190, "TakeDamageRageReduction", "Take Damage Add Rage", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(191, "MinicritBehind", "Minicrit Behind", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(192, "JumpHeightBonus", "Jump Height", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(193, "WrapRelease", "Ball Release", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(194, "ExplosionResistPenalty", "Explosion Resist", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(195, "DropHealthPackOnHit", "Drop Health Pack On Hit", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(196, "HitSelfOnMiss", "Hit Self On Miss", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(197, "AttackConnectedMedic", "Attack Connected Medic", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(198, "MarkForDeath", "Mark For Death", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(199, "FuseTimeReduced", "Fuse Time", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(200, "GrenadeNoRoll", "Grenade Roll", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(201, "ChargeDamageBonus", "Max Charge Damage", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(202, "ArmTimeBonus", "Arm Time", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(203, "ImpactDamageBonus", "Impact Damage", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(204, "MeleeKillChargeRecharge", "Melee Kill Charge", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(205, "TakeDamageReducesCharge", "Take Damage Charge", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(206, "HealthOnKillRelative", "Health Kill Relative", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(207, "Honorbound", "Honorbound", Type.ADDITIVE, 0, State.NEGATIVE);
		new TF2Attribute(208, "OnHitChargeRecharge", "On Hit Charge Recharge", Type.ADDITIVE, 0, State.POSITIVE);
		new TF2Attribute(209, "MaxAmmoDecreased", "Max Ammo Global", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(210, "AmmoRechargeCharge", "Ammo Charge Recharge", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(211, "DetonateOnHit", "Detonate On Hit", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(212, "SlowOnHit", "Slow On Hit", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(213, "DamageResistHalfHealthSpinningBonus", "Damage Resist Half Health Spinning",
				Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(214, "MovementSpinPenalty", "Speed Spin", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(215, "CritNoDamage", "Crit No Damage", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(216, "CritMakesLaugh", "Crit Laugh", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(217, "RevengeCrits", "Revenge Crits", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(218, "RepairRatePenalty", "Repair Rate", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(219, "SeeEnemyHealth", "Enemy Health", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(220, "OrganHarvest", "Organ Harvest", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(221, "HealRadial", "Heal Radial", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(222, "MovementUberBonus", "Movement Uber", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(223, "ChargeRateOnKill", "Charge Rate On Kill", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(224, "DamageAboveHalfHealthPenalty", "Damage Above Half Health", Type.PERCENTAGE, 1f,
				State.NEGATIVE);
		new TF2Attribute(225, "DamageBelowHalfHealthBonus", "Damage Above Half Health", Type.PERCENTAGE, 1f,
				State.POSITIVE);
		new TF2Attribute(226, "NoFlinchCharged", "No Flinch Charged", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(227, "KnockbackReductionAim", "Knockback Aim", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(228, "IndependentCharge", "Independent Charge", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(229, "BodyDamagePenalty", "Body Damage", Type.PERCENTAGE, 1f, State.NEGATIVE);
		new TF2Attribute(230, "CannotHeadshotUnlessCharged", "Independent Charge", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(231, "FocusRage", "Focus Rage", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(232, "CloakDurationGlobalBonus", "Cloak Duration Global", Type.PERCENTAGE, 1f, State.POSITIVE);
		new TF2Attribute(233, "CloakOnKill", "Cloak On Kill", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(234, "SpeedBoostOnKill", "Speed Boost On Kill", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(235, "MeltsInFire", "Melts In Fire", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(236, "FireProofOnHit", "Fire Proof On Hit", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(237, "AbsorbHealthBackstab", "Absorb Health Backstab", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(238, "CritOnKillSap", "Crit On Kill Sap", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(239, "PierceResistances", "Pierce Resistances", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(240, "DamageWhenDisguisedBonus", "Damage When Disguised", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(241, "ProvideWhenActive", "Provide When Active", Type.ADDITIVE, 0f, State.NEUTRAL);
		new TF2Attribute(242, "ReverseBuilding", "ReverseBuilding", Type.ADDITIVE, 0f, State.POSITIVE);
		new TF2Attribute(243, "HealthDegen", "Health Regen", Type.ADDITIVE, 0f, State.NEGATIVE);
		new TF2Attribute(244, "Pyroland", "Enter Pyroland", Type.ADDITIVE, 0f, State.HIDDEN);

		/*
		 * new TF2Attribute(139, "ChargeStep", "Charge Step", Type.ADDITIVE, 0,
		 * State.POSITIVE, SHIELD, 1f, 1, 250, 3);
		 */
		// new TF2Attribute(23, "He", "Coll Remove", "Additive", 0f, -1);
	}

	public static float addValue(float value, NBTTagCompound attributelist, String effect) {
		for (String name : attributelist.getKeySet()) {
			NBTBase tag = attributelist.getTag(name);
			if (tag instanceof NBTTagFloat) {
				TF2Attribute attribute = attributes[Integer.parseInt(name)];
				if (attribute != null && attribute.effect.equals(effect))
					if (attribute.typeOfValue == Type.ADDITIVE)
						value += ((NBTTagFloat) tag).getFloat();
					else
						value *= ((NBTTagFloat) tag).getFloat();
			}
		}
		return value;
	}

	public static float getModifier(String effect, ItemStack stack, float initial, EntityLivingBase entity) {

		if (!stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null))
			return initial;
		float value = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).getAttributeValue(stack, effect, initial);

		if (entity != null && entity instanceof EntityTF2Character)
			value *= ((EntityTF2Character) entity).getAttributeModifier(effect);

		return value;
	}

	public static float getModifierGlobal(String effect, float initial, EntityLivingBase entity) {
		float value = initial;

		IItemHandler itemHandler = TF2Util.getLoadoutItemHandler(entity);
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if (!stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null))
				continue;
			stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).getAttributeValue(stack, effect, value);
		}

		if (entity != null && entity instanceof EntityTF2Character)
			value *= ((EntityTF2Character) entity).getAttributeModifier(effect);

		return value;
	}

	public String getTranslatedString(float value, boolean withColor) {
		String valueStr = String.valueOf(value);
		if (this.typeOfValue == Type.PERCENTAGE)
			valueStr = Integer.toString(Math.round((value - 1) * 100)) + "%";
		else if (this.typeOfValue == Type.INVERTED_PERCENTAGE)
			valueStr = Integer.toString(Math.round((1 - value) * 100)) + "%";
		else if (this.typeOfValue == Type.ADDITIVE)
			valueStr = new DecimalFormat("##.#").format(value);

		if (withColor) {
			TextFormatting color = this.state == State.POSITIVE ? TextFormatting.AQUA
					: (this.state == State.NEGATIVE ? TextFormatting.RED : TextFormatting.WHITE);
			return color + I18n.format("weaponAttribute." + this.name, new Object[] { valueStr });
		} else
			return I18n.format("weaponAttribute." + this.name, new Object[] { valueStr });

	}

	public static List<TF2Attribute> getAllPassibleAttributesForUpgradeStation() {
		if (listUpgrades != null)
			return listUpgrades;
		List<TF2Attribute> list = new ArrayList<>();
		for (TF2Attribute attr : attributes)
			if (attr != null && attr.canApply != Predicates.<ItemStack>alwaysFalse() && (attr.state != State.NEGATIVE))
				// for (int i = 0; i < attr.weight; i++)
				list.add(attr);
		return list;
	}

	public static void setAttribute(ItemStack stack, TF2Attribute attr, float value) {
		if (!stack.isEmpty() && stack.hasTagCompound() && WeaponData.getCapability(stack) != null) {
			stack.getTagCompound().getCompoundTag("Attributes").setFloat(String.valueOf(attr.id), value);
			WeaponData.getCapability(stack).cached = false;
		}
	}

	public static void upgradeItemStack(ItemStack stack, int value, Random rand) {
		if (WeaponData.getCapability(stack) == null)
			return;
		List<TF2Attribute> list = new ArrayList<>();
		int lowestCost = Integer.MAX_VALUE;
		int maxCount = value / 30;
		for (TF2Attribute attr : attributes)
			if (attr != null && attr.canApply(stack) && attr.state == State.POSITIVE) {
				for (int i = 0; i < attr.weight; i++)
					list.add(attr);
				lowestCost = Math.min(lowestCost, attr.cost);
			}
		if (list.size() > 0) {
			int i = 0;
			NBTTagCompound tag = stack.getTagCompound().getCompoundTag("Attributes");
			while (i < maxCount && value >= lowestCost) {
				i++;
				TF2Attribute attr = list.get(rand.nextInt(list.size()));
				if (attr.cost <= value && attr.calculateCurrLevel(stack) <= attr.numLevels) {
					value -= attr.cost;

					String key = String.valueOf(attr.id);

					if (!tag.hasKey(key))
						tag.setFloat(key, attr.defaultValue);
					tag.setFloat(key, tag.getFloat(key) + attr.getPerLevel(stack));
				}
			}
		}
		WeaponData.getCapability(stack).cached = false;
	}

	public float getPerLevel(ItemStack stack) {
		float def = this.perLevel;
		if (stack.getItem() instanceof ItemMinigun && this.effect.equals("Damage"))
			def *= 0.5f;
		else if (stack.getItem() instanceof ItemJetpack && this.effect.equals("Self Push Force"))
			def *= 0.75f;
		else if (stack.getItem() instanceof ItemChargingTarge && this.effect.equals("Charge"))
			def *= 2.4f;
		else if (stack.getItem() instanceof ItemSniperRifle && this.effect.equals("Destroy Block"))
			def *= 2.52f;
		return def;
	}

	public int calculateCurrLevel(ItemStack stack) {
		if (stack.isEmpty())
			return 0;
		if (!stack.getTagCompound().getCompoundTag("Attributes").hasKey(String.valueOf(this.id)))
			return 0;
		float valueOfAttr = stack.getTagCompound().getCompoundTag("Attributes").getFloat(String.valueOf(this.id));
		float floatVal = (valueOfAttr - this.defaultValue) / this.getPerLevel(stack);
		return Math.round(floatVal - 0.3f);
	}

	public TF2Attribute getAttributeReplacement(ItemStack stack) {
		/*
		 * if(this.name.equals("DamageBonus") && (getModifier("Self Damage", stack, 1,
		 * null) <= 0 || stack.getItem() instanceof ItemJetpack)) return
		 * MapList.nameToAttribute.get("SelfPushForceBonus");
		 */
		return this;
	}

	public int getUpgradeCost(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof ItemFromData))
			return this.cost;
		int baseCost = (int) (this.cost * TF2ConfigVars.xpMult);
		if (stack.getItem() instanceof ItemWeapon) {
			if (ItemFromData.getData(stack).getInt(PropertyType.COST) <= 12)
				baseCost *= this.fullCost ? 0.75f : 0.5f;
			else if (ItemFromData.getData(stack).getInt(PropertyType.COST) <= 18)
				baseCost *= this.fullCost ? 1f : 0.75f;
		}
		if (getModifier("Damage", stack, 1, null) <= 0)
			baseCost /= 2;
		if (this.effect.equals("Fire Rate") && !ItemFromData.getData(stack).getBoolean(PropertyType.RELOADS_CLIP))
			baseCost *= 1.5f;
		if (this.effect.equals("Clip Size") && ItemFromData.getData(stack).getBoolean(PropertyType.RELOADS_FULL_CLIP))
			baseCost *= 1.33335f;
		if (this.effect.equals("Accuracy") && ItemFromData.getData(stack).getFloat(PropertyType.SPREAD) <= 0.025f)
			baseCost /= 2;
		if (stack.getMaxStackSize() > 1)
			baseCost = (baseCost / 3) * stack.getCount();
		if (stack.getItem() instanceof ItemCloak)
			baseCost *= 2;
		else if (stack.getItem() instanceof ItemJetpack && this.effect.equals("Self Push Force"))
			baseCost *= 0.75;
		// int additionalCost = 0;
		int lastUpgradesCost = stack.getTagCompound().getInteger("TotalCost");
		/*
		 * if (lastUpgradesCost > 0) { additionalCost = lastUpgradesCost / 10; baseCost
		 * += baseCost / 10; } baseCost += additionalCost;
		 */
		int baseCostPre = baseCost;
		baseCost *= 1 + (lastUpgradesCost / 800f);
		if (this.calculateCurrLevel(stack) == this.numLevels - 1 && this.numLevels > 1)
			baseCost += baseCostPre * (this.numLevels > 2 ? 2 : 1.25f)
					* (1 + (stack.getTagCompound().getInteger("LastUpgradesCost") / 800f));
		return Math.min(1400, baseCost);
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean canApply(ItemStack stack) {
		return !stack.isEmpty() && this.canApply.apply(stack);
	}

	public static int getMaxExperience(ItemStack stack, EntityPlayer playerIn) {
		if (playerIn != null && playerIn.capabilities.isCreativeMode)
			return 99999;
		int base = 1400;
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Australium"))
			base += 1000;
		if (stack.getItem() instanceof ItemPDA)
			base *= 1.75;
		if (!(stack.getItem() instanceof ItemWeapon))
			base *= 2;
		int strangeLevel = 0;
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Strange")) {
			strangeLevel = stack.getTagCompound().getInteger("StrangeLevel");
		}
		return (int) Math.min(TF2ConfigVars.xpCap, ((base + strangeLevel * strangeLevel * 40) * TF2ConfigVars.xpMult));
	}
}
