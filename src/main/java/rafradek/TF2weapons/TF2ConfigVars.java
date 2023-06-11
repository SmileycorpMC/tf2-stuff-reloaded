package rafradek.TF2weapons;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import rafradek.TF2weapons.entity.mercenary.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TF2ConfigVars {

	public static Configuration conf;
	public static int destTerrain;
	public static boolean medigunLock;
	public static boolean swapAttackButton;
	public static boolean fastMetalProduction;
	public static boolean shootAttract;
	public static boolean disableSpawn;
	public static boolean disableBossSpawn;
	public static boolean disableBossSpawnItems;
	public static float invasionChance;
	public static int invasionMinDay;
	public static int invasionMaxWaves;
	public static boolean disableInvasionItems;
	public static boolean disableLoot;
	public static int bossReappear;
	public static boolean disableContracts;
	public static boolean disableGeneration;
	public static int mannCoChance;
	public static int baseChance;
	public static int randomCrits;
	public static boolean overworldOnly;
	public static String spawnOres;
	public static String naturalCheck;
	public static String biomeCheck;
	public static int batchDamage;
	public static float damageMultiplier;
	public static float healthScale;
	public static boolean dynamicLights;
	public static boolean dynamicLightsProj;
	public static boolean deadRingerTrigger;
	public static boolean thermalExpansion;
	public static float medicChance;
	public static int sentryTargets;
	public static float dispenserRepair;
	public static boolean dispenserPlayers;
	public static boolean teleporterPlayers;
	public static boolean teleporterEntities;
	public static boolean enableUdp;
	public static boolean targetSentries;
	public static float dropAmmo;
	public static float speedMult;
	public static float armorMult;
	public static float costMult;
	public static float xpMult;
	public static int xpCap;
	public static float mercenaryVolume;
	public static float bossVolume;
	public static float gunVolume;
	public static boolean enchantedExplosion;
	public static float maxEnergy;
	public static boolean buildingsUseEnergy;
	public static int sentryUseEnergy;
	public static int dispenserUseEnergy;
	public static int teleporterUseEnergy;
	public static boolean fastItemCooldown;
	public static boolean freeUseItems;
	public static int maxMetalEngineer;
	public static boolean fastBuildEngineer;
	public static boolean longDurationBanner;
	public static boolean headCountDrop;
	public static boolean killstreakDrop;
	public static boolean disableBuildingGui;
	public static boolean limitedDisguise;

	public static boolean oldDispenser;
	public static boolean scaleAttributes;
	public static boolean mustReload;
	public static boolean autoReload;
	public static float accurracyMult;
	public static int updateDelay;
	public static float worldScale;
	public static float explosionKnockback;
	public static float explosiveJumpGravity;
	public static float hatMercenaryMult;
	public static int allowTrimp;
	public static boolean australiumStatue;
	public static boolean doorBlockLight;
	public static boolean neutralAttack;
	public static boolean attackMobs;
	public static boolean canJoin;
	public static boolean mouseBackpackItemSwitch;
	public static Multimap<ConfigCategory, Property> propertyNetworked;

	public static Map<Class<? extends EntityLiving>, Integer> spawnRate;

	public static ArrayList<ResourceLocation> repairBlacklist;
	public static ArrayList<ResourceLocation> hostileBlacklist;

	public static void createConfig(boolean save) {
		propertyNetworked = HashMultimap.create();
		if (conf.get("gameplay", "Destructible terrain", "Upgrade only", "Explosions can destroy blocks")
				.getType() == Property.Type.BOOLEAN)
			conf.getCategory("gameplay").remove("Destructible terrain");
		String destr = conf.get("gameplay", "Destructible terrain", "Upgrade only", "Explosions can destroy blocks")
				.setValidValues(new String[] { "Always", "Upgrade only", "Never" }).getString();
		if (destr.equalsIgnoreCase("Always"))
			destTerrain = 2;
		else if (destr.equalsIgnoreCase("Upgrade only"))
			destTerrain = 1;
		else
			destTerrain = 0;
		medigunLock = conf.getBoolean("Medigun lock target", "gameplay", false, "Left Click selects healing target");
		swapAttackButton = conf.getBoolean("Swap mouse buttons", "gameplay", false,
				"Swaps attack and use buttons on weapons");

		fastMetalProduction = conf.getBoolean("Fast metal production", "adaption", false,
				"Dispensers produce metal every 5 seconds");
		healthScale = conf.getInt("TF2 - Minecraft health translation", "adaption", 200, -10000, 10000,
				"How much 10 minecraft hearts are worth in TF2 health");
		setNetworked(conf.getCategory("adaption"),
				conf.getCategory("adaption").get("TF2 - Minecraft health translation"));
		damageMultiplier = 200f / healthScale;

		fastItemCooldown = conf.getBoolean("Fast item cooldown", "adaption", true,
				"Sandman balls and cleavers recover faster than normal");
		freeUseItems = conf.getBoolean("Free use items", "adaption", false,
				"Throwable and consumable items are free to use");
		longDurationBanner = conf.getBoolean("Long duration banner", "adaption", true,
				"Banner buffs build longer and last longer");
		worldScale = conf.getFloat("World scale", "adaption", 0.9f, 0f, Float.MAX_VALUE,
				"How many meters long minecraft block is");
		explosionKnockback = conf.getFloat("Explosion knockback", "adaption", 0.14f, 0f, Float.MAX_VALUE,
				"Velocity added for each point of explosive damage. 0.115 is TF2 original");
		explosiveJumpGravity = conf.getFloat("Blast jump gravity", "adaption", 0.046f, 0f, Float.MAX_VALUE,
				"Gravity applied when rocket jumping. 0.0381 is original TF2 gravity");
		allowTrimp = getIndexSelected(conf.get("adaption", "Simulate ramps", "Horizontal only",
				"Simulate ramps when rocket jumping or charging",
				new String[] { "Always", "Horizontal only", "Never" }), 0);
		headCountDrop = conf.getBoolean("Head count drop", "adaption", true,
				"If enabled, head count on eyelander will drop over time");
		killstreakDrop = conf.getBoolean("Killstreak count drop", "adaption", true,
				"If enabled, killstreak count will drop over time. When disabled, bonuses from killstreak weapons are no longer applied");
		disableBuildingGui = conf.getBoolean("Disable building gui", "adaption", false,
				"When disabled, right click will always pick up the building");
		limitedDisguise = conf.getBoolean("Limited disguise", "adaption", false,
				"Disguise is limited to class selection");
		setNetworked(conf.getCategory("adaption"), conf.getCategory("adaption").get("Limited disguise"));

		boolean old = conf.hasKey("gameplay", "Fast metal production");
		if (old) {
			conf.moveProperty("gameplay", "TF2 - Minecraft health translation", "adaption");
			conf.moveProperty("gameplay", "Fast metal production", "adaption");
		}

		disableSpawn = conf.getBoolean("Disable mob spawning", "spawn rate", false,
				"Disable mod-specific mobs spawning");
		overworldOnly = conf.getBoolean("Spawn only in overworld", "spawn rate", false,
				"Disable spawning in custom dimensions");
		biomeCheck = conf
				.get("spawn rate", "Spawn in biomes", "Default", "Default - biomes that spawn vanilla monsters")
				.setValidValues(new String[] { "All", "Default", "Vanilla only" }).getString();
		disableBossSpawn = conf.getBoolean("Disable boss spawn", "spawn rate", false, "Disable random tf2 boss spawn");
		disableBossSpawnItems = conf.getBoolean("Disable boss spawn items", "spawn rate", false,
				"Disable boss spawning items");
		invasionChance = conf.getFloat("Invasion chance", "spawn rate", 0.06f, 0f, 1f,
				"Chance of robot invasion occuring at the beginning of each day.");
		invasionMinDay = conf.getInt("Invasion minimum delay", "spawn rate", 10, 0, Integer.MAX_VALUE,
				"Minimal delay between invasions, in days");
		invasionMaxWaves = conf.getInt("Invasion max waves", "spawn rate", 6, 1, Integer.MAX_VALUE,
				"Maximum amount of waves");
		disableInvasionItems = conf.getBoolean("Disable invasion event items", "spawn rate", false,
				"Disable invasion items");
		disableContracts = conf.getBoolean("Disable contracts", "gameplay", false, "Stop new contracts from appearing");
		disableGeneration = conf.getBoolean("Disable structures", "world gen", false,
				"Disable structures generation, such as Mann Co. building");
		mannCoChance = conf.getInt("Mann Co. building chance", "world gen", 32, 1, Integer.MAX_VALUE,
				"Median chunk distance between Mann Co. buildings");
		baseChance = conf.getInt("Base building chance", "world gen", 32, 1, Integer.MAX_VALUE,
				"Median chunk distance between base buildings");
		disableLoot = conf.getBoolean("Disable chest loot", "world gen", false,
				"Disable chest loot generated by this mod");
		mustReload = conf.getBoolean("Reload in creative", "gameplay", true,
				"Weapons have to be reloaded in creative mode");
		setNetworked(conf.getCategory("gameplay"), conf.getCategory("gameplay").get("Reload in creative"));
		autoReload = conf.getBoolean("Auto reload", "gameplay", true, "Always reload weapons automatically");
		TF2weapons.weaponVersion = TF2weapons.conf.getInt("Weapon Config Version", "internal",
				TF2weapons.getCurrentWeaponVersion(), 0, 1000, "");
		conf.get("world gen", "Disable structures", false).setRequiresMcRestart(true);
		conf.get("world gen", "Mann Co. building chance", 32).setRequiresWorldRestart(true);
		conf.get("world gen", "Mann Co. building chance", 32).setRequiresWorldRestart(true);

		old = conf.hasKey("gameplay", "Natural mob detection");
		if (old) {
			conf.moveProperty("gameplay", "Natural mob detection", "mercenary");
		}

		naturalCheck = conf.get("mercenary", "Natural mob detection", "Always")
				.setValidValues(new String[] { "Always", "Fast", "Never" }).getString();
		shootAttract = conf.getBoolean("Shooting attracts mobs", "gameplay", true,
				"Gunfire made by players attracts mobs");
		randomCrits = getIndexSelected(conf.get("gameplay", "Random critical hits", "Players only",
				"Enables randomly appearing critical hits that deal 3x more damage",
				new String[] { "Always", "Players Only", "Never" }), 1);

		deadRingerTrigger = conf.getBoolean("Feign death events", "gameplay", true,
				"Does feign death trigger death events, set to false in case of mod conflicts");
		australiumStatue = conf.getBoolean("Enable australium statues", "gameplay", true,
				"Disable in case of mod conflicts");
		doorBlockLight = conf.getBoolean("Overhead door blocks light", "miscellaneous", true,
				"Should overhead doors block light");

		dynamicLights = conf.getBoolean("Dynamic Lights", "modcompatibility", true,
				"Enables custom light sources for AtomicStryker's Dynamic Lights mod")
				&& Loader.isModLoaded("dynamiclights");
		thermalExpansion = conf.getBoolean("Thermal Expansion", "modcompatibility", true,
				"Adds centrifudge and smelter recipes for smelting ammo, gas acts as a refined fuel container")
				&& Loader.isModLoaded("thermalexpansion");
		dynamicLightsProj = conf.getBoolean("Dynamic Lights - Projectiles", "modcompatibility", true,
				"Should projectiles emit light");
		bossReappear = conf.getInt("Boss respawn cooldown", "gameplay", 30000, 1100, Integer.MAX_VALUE,
				"Maximum boss reappear time in ticks. Bosses always spawn in full moon");

		String[] hostileblacklist = conf.getStringList("Hostile entity blacklist", "gameplay",
				new String[] { "minecraft:enderman", "minecraft:zombie_pigman" },
				"Entity IDs that should not be considered hostile");

		enableUdp = conf.getBoolean("Enable UDP (experimental)", "gameplay", false, "");
		batchDamage = getIndexSelected(conf.get("gameplay", "Attack invulnerability period", "Ignore",
				"Ignore, respect, or batch damage dealt during mob invulnerability",
				new String[] { "Ignore", "Batch", "Respect" }), 0);
		costMult = conf.getFloat("Weapon cost multiplier", "gameplay", 1f, 0f, 100f,
				"Weapon cost multiplier in Mann Co. store");
		xpMult = conf.getFloat("Upgrade cost multiplier", "gameplay", 1f, 0f, 100f, "Upgrade cost multiplier");
		setNetworked(conf.getCategory("gameplay"), conf.getCategory("gameplay").get("Upgrade cost multiplier"));
		xpCap = conf.getInt("Max upgrade limit", "gameplay", Integer.MAX_VALUE, 0, Integer.MAX_VALUE,
				"Maximum upgrade limit, applied after other limits");
		setNetworked(conf.getCategory("gameplay"), conf.getCategory("gameplay").get("Max upgrade limit"));
		bossVolume = conf.getFloat("Boss volume (radius)", "sound volume", 4f, 0, 10,
				"Values above 1 increase sound radius only");
		mercenaryVolume = conf.getFloat("Mercenary volume (radius)", "sound volume", 0.6f, 0, 10,
				"Values above 1 increase sound radius only");
		gunVolume = conf.getFloat("Gun volume (radius)", "sound volume", 2f, 0, 10,
				"Applies only to players, values above 1 increase sound radius only");

		enchantedExplosion = conf.getBoolean("Enchanted blast jumping", "gameplay", true,
				"Strafing, no air resistance and reduced gravity when blast jumping");
		dropAmmo = conf.getFloat("Ammo drop chance", "gameplay", 0.15f, 0f, 1f,
				"Chance of dropping ammo from non-TF2 hostile creature");
		canJoin = conf.getBoolean("Allow team joining", "gameplay", true, "Allow players to join teams");
		mouseBackpackItemSwitch = conf.getBoolean("Backpack activation in hotbar", "gameplay", true,
				"Activate backpack by switching slot in hotbar");

		speedMult = conf.getFloat("Mercenary speed multiplier", "mercenary", 0.8f, 0f, 2f,
				"Speed multiplier of mercenaries. Does not apply to owned mercenaries");
		armorMult = conf.getFloat("Armored mercenary chance", "mercenary", 0.06f, 0f, 10f,
				"Base chance of armored mercenaries. Altered by difficulty level");
		hatMercenaryMult = conf.getFloat("Hatted mercenary chance", "mercenary", 1f, 0f, 10f,
				"Base chance of hatted mercenaries. Altered by difficulty level");
		maxMetalEngineer = conf.getInt("Engineer max metal", "mercenary", 500, 0, Integer.MAX_VALUE,
				"Starting metal for engineers");
		fastBuildEngineer = conf.getBoolean("Engineer fast build", "mercenary", true,
				"Should engineers build at extra speed");
		scaleAttributes = conf.getBoolean("Reduce vs player damage", "mercenary", true,
				"Reduce weapon effectiveness versus players");
		accurracyMult = conf.getFloat("Accurracy multiplier", "mercenary", 1f, 0f, 10f,
				"Mercenary accurracy multiplier");
		updateDelay = conf.getInt("Update delay", "mercenary", 3, 1, 20,
				"Delay in sending position updates. Only for dedicated server");
		neutralAttack = conf.getBoolean("Attack neutral players", "mercenary", false,
				"Should mercenaries attack neutral players");
		attackMobs = conf.getBoolean("Attack mobs", "mercenary", true, "Should mercenaries attack mobs");
		// conf.getCategory("mercenary").get("Update delay").setRequiresMcRestart(true);
		old = conf.hasKey("gameplay", "Buildings use energy");
		if (old) {
			conf.moveProperty("gameplay", "Dispenser repair rate", "building");
			conf.moveProperty("gameplay", "Repair blacklist", "building");
			conf.moveProperty("gameplay", "Buildings use energy", "building");
			conf.moveProperty("gameplay", "Sentry energy use", "building");
			conf.moveProperty("gameplay", "Dispenser energy use", "building");
			conf.moveProperty("gameplay", "Teleport energy use", "building");
			conf.moveProperty("gameplay", "Mobs target sentries", "building");
		}
		oldDispenser = conf.getBoolean("Old dispenser behavior", "building", false,
				"When enabled, no material is being used to repair");
		buildingsUseEnergy = conf.getBoolean("Buildings use energy", "building", false, "");
		sentryUseEnergy = conf.getInt("Sentry energy use", "building", 100, 0, 40000, "Energy use on attack");
		dispenserUseEnergy = conf.getInt("Dispenser energy use", "building", 15, 0, 40000,
				"Energy use on repairs and heals");
		teleporterUseEnergy = conf.getInt("Teleport energy use", "building", 20000, 0, 40000, "Energy use on teleport");
		dispenserRepair = conf.getFloat("Dispenser repair rate", "building", 3, 0, 10000,
				"Repair per 1 metal. Reduced by enchants");
		String[] blacklist = conf.getStringList("Repair blacklist", "building", new String[0],
				"Item IDs that should not be repairable by dispensers");
		targetSentries = conf.getBoolean("Mobs target sentries", "building", true,
				"Mobs will attack sentries that dont shoot");

		if (!buildingsUseEnergy) {
			sentryUseEnergy = 0;
			dispenserUseEnergy = 0;
			teleporterUseEnergy = 0;
		}

		conf.getBoolean("Attack on hurt", "default building targets", true, "");
		conf.getBoolean("Attack other players", "default building targets", false, "");
		conf.getBoolean("Attack hostile mobs", "default building targets", true, "");
		conf.getBoolean("Attack friendly creatures", "default building targets", false, "");
		conf.getBoolean("Dispensers heal neutral players", "default building targets", true, "");
		conf.getBoolean("Neutral players can teleport", "default building targets", true, "");
		conf.getBoolean("Entities can teleport", "default building targets", true, "");

		spawnRate = new HashMap<>();
		spawnRate.put(EntityScout.class, conf.getInt("Scout", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntityPyro.class, conf.getInt("Pyro", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntitySoldier.class, conf.getInt("Soldier", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntityHeavy.class, conf.getInt("Heavy", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntityDemoman.class, conf.getInt("Demoman", "spawn rate", 12, 0, 1000, ""));
		spawnRate.put(EntitySpy.class, conf.getInt("Spy", "spawn rate", 9, 0, 1000, ""));
		spawnRate.put(EntityEngineer.class, conf.getInt("Engineer", "spawn rate", 9, 0, 1000, ""));
		spawnRate.put(EntitySniper.class, conf.getInt("Sniper", "spawn rate", 9, 0, 1000, ""));
		TF2weapons.updateOreGenStatus();

		TF2weapons.updateMobSpawning();
		medicChance = conf.getFloat("Medic spawn chance", "spawn rate", 1, 0, 1000, "Medic spawn chance multiplier");

		repairBlacklist = new ArrayList<>();
		for (String id : blacklist) {
			repairBlacklist.add(new ResourceLocation(id));
		}
		hostileBlacklist = new ArrayList<>();
		for (String id : hostileblacklist) {
			hostileBlacklist.add(new ResourceLocation(id));
		}

		if (save && conf.hasChanged())
			conf.save();
	}

	public static void setNetworked(ConfigCategory cat, Property prop) {
		propertyNetworked.put(cat, prop);
	}

	public static int getIndexSelected(Property prop, int def) {
		if (prop.getValidValues() != null) {
			for (int i = 0; i < prop.getValidValues().length; i++) {
				if (prop.getString().equalsIgnoreCase(prop.getValidValues()[i]))
					return i;
			}
		}
		return def;
	}
}
