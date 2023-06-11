package rafradek.TF2weapons.common;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import rafradek.TF2weapons.entity.projectile.*;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.WeaponData;

import java.util.HashMap;
import java.util.Map;

public class MapList {

	// public static Map<Class<? extends EntityTF2Character>, Integer> classNumbers;
	public static Map<String, Item> weaponClasses;
	public static Map<String, PropertyType<?>> propertyTypes;
	public static Map<String, Class<? extends EntityProjectileBase>> projectileClasses;
	public static Map<String, WeaponData> nameToData;
	public static Map<String, TF2Attribute> nameToAttribute;
	public static Map<String, ItemUsable> specialWeapons;
	public static Map<String, NBTTagCompound> buildInAttributes;

	// public static Map<MinigunLoopSound, EntityLivingBase > fireCritSounds;
	// public static Map<List<SpawnListEntry>, SpawnListEntry> scoutSpawn;

	public static void initMaps() {
		weaponClasses = new HashMap<>();
		projectileClasses = new HashMap<>();
		nameToData = new HashMap<>();
		propertyTypes = new HashMap<>();
		nameToAttribute = new HashMap<>();
		buildInAttributes = new HashMap<>();
		specialWeapons = new HashMap<>();
		/*
		 * WeaponData.propertyDeserializers = new HashMap<String,
		 * JsonDeserializer<ICapabilityProvider>>();
		 * WeaponData.propertyDeserializers.put("Attributes", new
		 * ItemFromData.AttributeSerializer());
		 * WeaponData.propertyDeserializers.put("Content", new
		 * ItemCrate.CrateSerializer());
		 */
		// classNumbers = new HashMap<>();

		// classNumbers.put(EntityScout.class, 0);
		// scoutSpawn=new HashMap<List<SpawnListEntry>, SpawnListEntry>();
		weaponClasses.put("sniperrifle", new ItemSniperRifle());
		weaponClasses.put("bullet", new ItemBulletWeapon());
		weaponClasses.put("minigun", new ItemMinigun());
		weaponClasses.put("projectile", new ItemProjectileWeapon());
		weaponClasses.put("stickybomb", new ItemStickyLauncher());
		weaponClasses.put("flamethrower", new ItemFlameThrower());
		weaponClasses.put("knife", new ItemKnife());
		weaponClasses.put("medigun", new ItemMedigun());
		weaponClasses.put("cloak", new ItemCloak());
		weaponClasses.put("wrench", new ItemWrench());
		weaponClasses.put("bonk", new ItemBonk());
		weaponClasses.put("cosmetic", new ItemWearable());
		weaponClasses.put("melee", new ItemMeleeWeapon());
		weaponClasses.put("sapper", new ItemSapper());
		weaponClasses.put("backpack", new ItemSoldierBackpack());
		weaponClasses.put("crate", new ItemCrate());
		weaponClasses.put("jar", new ItemJar());
		weaponClasses.put("wrangler", new ItemWrangler());
		weaponClasses.put("shield", new ItemChargingTarge());
		weaponClasses.put("cleaver", new ItemCleaver());
		weaponClasses.put("parachute", new ItemParachute());
		weaponClasses.put("huntsman", new ItemHuntsman());
		weaponClasses.put("jetpack", new ItemJetpack());
		weaponClasses.put("jetpacktrigger", new ItemJetpackTrigger());
		weaponClasses.put("pda", new ItemPDA());
		weaponClasses.put("gas", new ItemGas());
		weaponClasses.put("shortcircuit", new ItemBulletWeapon());
		weaponClasses.put("airblast", new ItemAirblast());
		weaponClasses.put("backpackgeneric", new ItemBackpack());
		weaponClasses.put("grapplinghook", new ItemGrapplingHook());
		/*
		 * weaponDatas.put("sniperrifle", ); weaponDatas.put("bullet", new
		 * ItemBulletWeapon()); weaponDatas.put("minigun", new ItemMinigun());
		 * weaponDatas.put("projectile", new Itdew ItemFlameThrower());
		 * weaponDatas.put("knife", new ItemKnife()); weaponDatas.put("medigun", new
		 * ItemMedigun()); weaponDatas.put("cloak", new ItemCloak());
		 * weaponDatas.put("wrench", new ItemWrench()); weaponDatas.put("bonk", new
		 * ItemBonk()); weaponDatas.put("cosmetic", new ItemWearable());
		 */

		projectileClasses.put("rocket", EntityRocket.class);
		projectileClasses.put("cowmangler", EntityRocket.class);
		projectileClasses.put("fire", EntityFlame.class);
		projectileClasses.put("gas", EntityJar.class);
		projectileClasses.put("fireball", EntityFuryFireball.class);
		projectileClasses.put("flare", EntityFlare.class);
		projectileClasses.put("grenade", EntityGrenade.class);
		projectileClasses.put("syringe", EntityStickProjectile.class);
		projectileClasses.put("jar", EntityJar.class);
		projectileClasses.put("ball", EntityBall.class);
		projectileClasses.put("repairclaw", EntityStickProjectile.class);
		projectileClasses.put("arrow", EntityStickProjectile.class);
		projectileClasses.put("cleaver", EntityCleaver.class);
		projectileClasses.put("hhhaxe", EntityProjectileSimple.class);
		projectileClasses.put("energy", EntityProjectileEnergy.class);
		projectileClasses.put("onyx", EntityOnyx.class);
		projectileClasses.put("pomson", EntityProjectileSimple.class);
		projectileClasses.put("grapplinghook", EntityGrapplingHook.class);
	}
}
