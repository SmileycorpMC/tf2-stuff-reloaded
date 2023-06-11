package rafradek.TF2weapons.common;

import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.text.TextComponentTranslation;

public class TF2Achievements {

	/*
	 * public static final ArrayList<Achievement> ACHIEVEMENTS = new ArrayList<>();
	 * public static final Achievement JOIN_TEAM = addAchievement( new
	 * Achievement("achievement.joinTeam", "joinTeam", 0, 0, TF2weapons.itemHorn,
	 * null).initIndependentStat()); public static final Achievement FIRST_ENCOUNTER
	 * = addAchievement(new Achievement("achievement.firstEncounter",
	 * "firstEncounter", 2, -1, new ItemStack(TF2weapons.itemAmmo, 1, 6),
	 * JOIN_TEAM)); public static final Achievement WEAPON_CRAFTING =
	 * addAchievement(new Achievement("achievement.weaponCrafting",
	 * "weaponCrafting", 4, -1, TF2weapons.blockCabinet, FIRST_ENCOUNTER)); public
	 * static final Achievement SPOILS_WAR = addAchievement(new
	 * Achievement("achievement.spoilsWar", "spoilsWar", 3, -3,
	 * ItemFromData.getNewStack("rocketlauncher"), FIRST_ENCOUNTER)); public static
	 * final Achievement HOME_MADE = addAchievement(new
	 * Achievement("achievement.homeMade", "homeMade", 6, -1, new
	 * ItemStack(TF2weapons.itemTF2, 1, 3), WEAPON_CRAFTING)); public static final
	 * Achievement SHINY = addAchievement( new Achievement("achievement.shiny",
	 * "shiny", 6, 1, AustraliumRecipe.ITEM, WEAPON_CRAFTING).setSpecial()); public
	 * static final Achievement LOOT_CRATE = addAchievement(new
	 * Achievement("achievement.lootCrate", "lootCrate", 2, -4,
	 * ItemFromData.getNewStack("crate1"), FIRST_ENCOUNTER)); public static final
	 * Achievement CRATES_10 = addAchievement(new
	 * Achievement("achievement.10Crates", "10Crates", 2, -6, new
	 * ItemStack(TF2weapons.itemTF2, 1, 7), LOOT_CRATE).setSpecial()); public static
	 * final Achievement MANN_CO_MADE = addAchievement(new
	 * Achievement("achievement.mannCoMade", "mannCoMade", 2, 1,
	 * ItemFromData.getNewStack("pistol"), JOIN_TEAM)); public static final
	 * Achievement WEAPON_UPGRADE = addAchievement(new
	 * Achievement("achievement.weaponUpgrade", "weaponUpgrade", 0, 4,
	 * TF2weapons.blockUpgradeStation, JOIN_TEAM)); public static final Achievement
	 * FULLY_UPGRADED = addAchievement(new Achievement("achievement.fullyUpgraded",
	 * "fullyUpgraded", 0, 6, TF2weapons.blockUpgradeStation, WEAPON_UPGRADE));
	 * public static final Achievement DODGE_DAMAGE = addAchievement(new
	 * Achievement("achievement.dodgeDamage", "dodgeDamage", -2, 0,
	 * ItemFromData.getNewStack("bonk"), null).initIndependentStat()); public static
	 * final Achievement DOUBLE_JUMP_KILL = addAchievement(new
	 * Achievement("achievement.doubleJumps", "doubleJumps", -2, 2,
	 * TF2weapons.itemScoutBoots, null).initIndependentStat()); public static final
	 * Achievement KILL_STUNNED = addAchievement(new
	 * Achievement("achievement.killStunned", "killStunned", -2, 4, new
	 * ItemStack(TF2weapons.itemAmmo, 1, 14), null).initIndependentStat()); public
	 * static final Achievement GUN_DOWN = addAchievement(new
	 * Achievement("achievement.gunDown", "gunDown", -2, -2,
	 * ItemFromData.getNewStack("pistol"), null).initIndependentStat()); public
	 * static final Achievement CRIT_ROCKET_KILL = addAchievement(new
	 * Achievement("achievement.critRocketKill", "critRocketKill", -4, 0, new
	 * ItemStack(TF2weapons.itemAmmo, 1, 7), null).initIndependentStat()); public
	 * static final Achievement WINGS_OF_GLORY = addAchievement(new
	 * Achievement("achievement.wingsOfGlory", "wingsOfGlory", -4, 2, Items.FEATHER,
	 * null).initIndependentStat()); public static final Achievement
	 * DEATH_FROM_ABOVE = addAchievement(new Achievement("achievement.deathAbove",
	 * "deathAbove", -4, -2, Items.FEATHER, null).initIndependentStat()); public
	 * static final Achievement PILOT_LIGHT = addAchievement(new
	 * Achievement("achievement.pilotLight", "pilotLight", -6, 0,
	 * Items.FLINT_AND_STEEL, null).initIndependentStat()); public static final
	 * Achievement ATTENTION_GETTER = addAchievement(new
	 * Achievement("achievement.attentionGetter", "attentionGetter", -6, 2,
	 * ItemFromData.getNewStack("flaregun"), null).initIndependentStat()); public
	 * static final Achievement HOT_POTATO = addAchievement(new
	 * Achievement("achievement.hotPotato", "hotPotato", -6, -2,
	 * ItemFromData.getNewStack("flamethrower"), null).initIndependentStat());
	 * public static final Achievement ARGYLE_SAP = addAchievement(new
	 * Achievement("achievement.argyleSap", "argyleSap", -2, 8,
	 * ItemFromData.getNewStack("stickybomblauncher"), null).initIndependentStat());
	 * public static final Achievement CHARGE_TARGE = addAchievement(new
	 * Achievement("achievement.chargeTarge", "chargeTarge", 0, 8,
	 * ItemFromData.getNewStack("chargintarge"), null).initIndependentStat());
	 * public static final Achievement PIPEBAGGER = addAchievement(new
	 * Achievement("achievement.pipebagger", "pipebagger", 2, 8, new
	 * ItemStack(TF2weapons.itemAmmo,1,11), null).initIndependentStat()); public
	 * static final Achievement SANDVICH = addAchievement(new
	 * Achievement("achievement.sandvich", "sandvich", -2, 10, new
	 * ItemStack(TF2weapons.itemSandvich),null).initIndependentStat()); public
	 * static final Achievement REVOLUTION = addAchievement(new
	 * Achievement("achievement.revolution", "revolution", 0, 10,
	 * ItemFromData.getNewStack("minigun"),null).initIndependentStat()); public
	 * static final Achievement CRIT_PUNCH= addAchievement(new
	 * Achievement("achievement.critPunch", "critPunch", 2, 10,
	 * ItemFromData.getNewStack("kgb"),null).initIndependentStat()); public static
	 * final Achievement KILL_SNIPER_WRANGLER= addAchievement(new
	 * Achievement("achievement.killSniperWrangler", "killSniperWrangler", -2, 12,
	 * ItemFromData.getNewStack("wrangler"),null).initIndependentStat()); public
	 * static final Achievement SENTRYGUN_KILLS= addAchievement(new
	 * Achievement("achievement.sentryKills", "sentryKills", 0, 12, new
	 * ItemStack(TF2weapons.itemBuildingBox,1,18),null).initIndependentStat());
	 * public static final Achievement TELEPORTS= addAchievement(new
	 * Achievement("achievement.teleports", "teleports", 2, 12, new
	 * ItemStack(TF2weapons.itemBuildingBox,1,22),null).initIndependentStat());
	 * public static final Achievement EFFICIENT_SNIPER= addAchievement(new
	 * Achievement("achievement.efficientS", "efficientS", 8, -2, new
	 * ItemStack(TF2weapons.itemAmmo,1,5),null).initIndependentStat()); public
	 * static final Achievement JARATE_MULTIPLE= addAchievement(new
	 * Achievement("achievement.jarate", "jarate", 8, 0,
	 * ItemFromData.getNewStack("jarate"),null).initIndependentStat()); public
	 * static final Achievement NO_SCOPE= addAchievement(new
	 * Achievement("achievement.noscope", "noscope", 8, 2,
	 * ItemFromData.getNewStack("jarate"),null).initIndependentStat()); public
	 * static final Achievement KILL_SPY_CLOAKED= addAchievement(new
	 * Achievement("achievement.killSpyCloaked", "killSpyCloaked", 8, 4,
	 * ItemFromData.getNewStack("cloak"),null).initIndependentStat()); public static
	 * final Achievement FAST_STABS= addAchievement(new
	 * Achievement("achievement.fastStab", "fastStab", 10, -2,
	 * ItemFromData.getNewStack("butterflyknife"),null).initIndependentStat());
	 * public static final Achievement SAP_STAB= addAchievement(new
	 * Achievement("achievement.sapStab", "sapStab", 10, 0,
	 * ItemFromData.getNewStack("sapper"),null).initIndependentStat()); public
	 * static final Achievement SPYMASTER= addAchievement(new
	 * Achievement("achievement.spymaster", "spymaster", 10, 2,
	 * ItemFromData.getNewStack("butterflyknife"),null).initIndependentStat());
	 * public static final Achievement MONOCULUS= addAchievement(new
	 * Achievement("achievement.monoculus", "monoculus", 6, -6,
	 * ItemFromData.getNewStack("monoculus"),null).initIndependentStat()); public
	 * static final Achievement HHH= addAchievement(new
	 * Achievement("achievement.hhh", "hhh", 8, -6,
	 * ItemFromData.getNewStack("headtaker"),null).initIndependentStat()); public
	 * static final Achievement MERASMUS= addAchievement(new
	 * Achievement("achievement.merasmus", "merasmus", 10, -6,
	 * ItemFromData.getNewStack("merasmushat"),null).initIndependentStat()); public
	 * static final Achievement BOSS_30_LVL= addAchievement(new
	 * Achievement("achievement.boss30lvl", "boss30lvl", 12, -6,
	 * ItemFromData.getNewStack("bombinomicon"),null).initIndependentStat().
	 * setSpecial());
	 */
	public static final StatBase SANDVICH_EATEN = new StatBasic("stat.sandvichEaten",
			new TextComponentTranslation("stat.sandvichEaten", new Object[0])).registerStat();
	public static final StatBase KILLED_WRANGLER_SNIPER = new StatBasic("stat.killedWranglerSniper",
			new TextComponentTranslation("stat.killedWranglerSniper", new Object[0])).registerStat();
	public static final StatBase KILLED_SENTRYGUN = new StatBasic("stat.killedSentrygun",
			new TextComponentTranslation("stat.killedSentryGun", new Object[0])).registerStat();
	public static final StatBase TELEPORTED = new StatBasic("stat.teleported",
			new TextComponentTranslation("stat.teleported", new Object[0])).registerStat();
	public static final StatBase KILLED_DOUBLEJUMP = new StatBasic("stat.killedDoublejump",
			new TextComponentTranslation("stat.killedDoublejump", new Object[0])).registerStat();
	public static final StatBase KILLED_STUNNED = new StatBasic("stat.killedStunned",
			new TextComponentTranslation("stat.killedStunned", new Object[0])).registerStat();
	public static final StatBase FLAREGUN_IGNITED = new StatBasic("stat.flaregunIgnited",
			new TextComponentTranslation("stat.flaregunIgnited", new Object[0])).registerStat();
	public static final StatBase PROJECTILES_REFLECTED = new StatBasic("stat.projectilesReflected",
			new TextComponentTranslation("stat.projectilesReflected", new Object[0])).registerStat();
	public static final StatBase KILLED_NOSCOPE = new StatBasic("stat.killedNoscope",
			new TextComponentTranslation("stat.killedNoscope", new Object[0])).registerStat();
	public static final StatBase KILLED_BACKSTAB = new StatBasic("stat.killedBackstab",
			new TextComponentTranslation("stat.killedBackstab", new Object[0])).registerStat();
	public static final StatBase KILLED_MERC = new StatBasic("stat.killedMercs",
			new TextComponentTranslation("stat.killedMercs", new Object[0])).registerStat();
	public static final StatBase CONTRACT_DAY = new StatBasic("stat.contractDay",
			new TextComponentTranslation("stat.contractDay", new Object[0])).registerStat();

	public static void init() {

	}
	// public static final StatBase KILLED_ABOVE = new StatBasic("killedAbove",new
	// TextComponentTranslation("stat.killedDoublejump", new
	// Object[0])).registerStat();
	/*
	 * public static Achievement addAchievement(Achievement achievement) {
	 * ACHIEVEMENTS.add(achievement); achievement.registerStat(); //
	 * StatList.ALL_STATS.add(achievement); return achievement; }
	 * 
	 * public TF2Achievements() { super("TF2 Stuff", ACHIEVEMENTS.toArray(new
	 * Achievement[ACHIEVEMENTS.size()])); }
	 */

}
