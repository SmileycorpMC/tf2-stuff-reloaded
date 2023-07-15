package rafradek.TF2weapons.item.crafting;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TF2CraftingManager {
	public static final ShapelessOreRecipe[] AMMO_RECIPES = new ShapelessOreRecipe[14];
	public static final TF2CraftingManager INSTANCE = new TF2CraftingManager();
	private final List<IRecipe> recipes = Lists.<IRecipe>newArrayList();
	private final Map<IRecipe, Predicate<EntityPlayer>> recipeConditions = new HashMap<>();

	public TF2CraftingManager() {

		Ingredient sentrybuilding = Ingredient.fromStacks(new ItemStack(TF2weapons.itemBuildingBox, 1, 18),
				new ItemStack(TF2weapons.itemBuildingBox, 1, 19));
		Ingredient dispenserbuilding = Ingredient.fromStacks(new ItemStack(TF2weapons.itemBuildingBox, 1, 20),
				new ItemStack(TF2weapons.itemBuildingBox, 1, 21));
		Ingredient teleporterbuilding = Ingredient.fromStacks(new ItemStack(TF2weapons.itemBuildingBox, 1, 22),
				new ItemStack(TF2weapons.itemBuildingBox, 1, 23));

		ItemStack bonk = ItemFromData.getNewStack("bonk");
		bonk.setCount(2);
		ItemStack cola = ItemFromData.getNewStack("critcola");
		cola.setCount(2);
		addRecipe(TF2CraftingManager.AMMO_RECIPES[1] = new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmo, 16, 1), new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(/* TF2CraftingManager.AMMO_RECIPES[2] = */new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmoMinigun, 1, 0), new Object[] { "ingotCopper", "ingotLead", "gunpowder",
						"ingotCopper", "ingotLead", "gunpowder", "gunpowder" }));
		addRecipe(/* TF2CraftingManager.AMMO_RECIPES[3] = */new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmoPistol, 5, 0),
				new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[4] = new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmo, 20, 4), new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[5] = new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmoSMG, 4, 0), new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[6] = new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmo, 5, 6), new Object[] { "ingotCopper", "ingotLead", "gunpowder" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[7] = new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmo, 22, 7), new Object[] { "ingotIron", "ingotIron", Blocks.TNT }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[8] = new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmo, 22, 8), new Object[] { "ingotIron", "ingotIron", Blocks.TNT }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[11] = new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmo, 18, 11), new Object[] { "ingotIron", "ingotIron", Blocks.TNT }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemAmmo, 11, 13),
				new Object[] { " R ", "RIR", " R ", 'I', "ingotIron", 'R', "dustRedstone" }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemAmmo, 8, 14),
				new Object[] { " S ", "SLS", " S ", 'S', "string", 'L', "leather" }));
		addShapelessRecipe(new ItemStack(TF2weapons.itemAmmoMedigun, 1),
				new Object[] { Items.SPECKLED_MELON, Items.GHAST_TEAR, new ItemStack(Items.DYE, 1, 15) });
		addRecipe(new ShapelessOreRecipe(null, new ItemStack(TF2weapons.itemAmmoFire, 1),
				new Object[] { "ingotIron", Items.MAGMA_CREAM, "ingotIron" }));
		addRecipe(TF2CraftingManager.AMMO_RECIPES[9] = new ShapelessOreRecipe(null,
				new ItemStack(TF2weapons.itemAmmoSyringe, 2, 0), new Object[] { "ingotIron", "paper", "paper", "paper",
						"paper", "paper", "paper", "paneGlass", "paneGlass" }));
		ItemStack cleaver = ItemFromData.getNewStack("cleaver");
		cleaver.setCount(1);
		addRecipe(new ShapedOreRecipe(null, cleaver, new Object[] { "I", "W", 'I', "ingotIron", 'W', "stickWood" }));
		addRecipe(new AustraliumRecipe());
		addRecipe(new RecipeApplyEffect(TF2weapons.itemStrangifier));
		addRecipe(new RecipeApplyEffect(TF2weapons.itemKillstreak));
		addRecipe(new JumperRecipe("rocketlauncher", "rocketjumper"));
		addRecipe(new JumperRecipe("stickybomblauncher", "stickyjumper"));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("cloak"),
				new Object[] { "AAA", "LGL", "AAA", 'A', "ingotAustralium", 'G', "blockGlass", 'L', "leather" }));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("deadringer"),
				new Object[] { " A ", "AGA", " A ", 'A', "ingotAustralium", 'G', "blockGlass" }));
		addRecipe(new ShapedOreRecipe(null, TF2weapons.itemDisguiseKit, new Object[] { "I I", "PAG", "I I", 'A',
				"ingotAustralium", 'I', "ingotIron", 'G', "blockGlass", 'P', "paper" }));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("sapper"),
				new Object[] { " R ", "IRI", " R ", 'I', "ingotIron", 'R', "dustRedstone" }));
		// addRecipe(new ShapedOreRecipe(null,new ItemStack(TF2weapons.itemHorn),
		// new Object[] { "CLC", "C C", " C ", 'C', "ingotCopper", 'L', "leather" }));
		// addRecipe(new ShapedOreRecipe(null,ItemFromData.getNewStack("trigger"),
		// new Object[] { "I ", "I ", "IS", 'I', "ingotIron", 'S', "stickWood" }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemBuildingBox, 1, 18),
				new Object[] { "IDI", "GRG", "III", 'D', new ItemStack(Blocks.DISPENSER), 'I', "ingotIron", 'G',
						"gunpowder", 'R', new ItemStack(TF2weapons.itemTF2, 1, 11) }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemBuildingBox, 1, 20),
				new Object[] { "MDR", "SIm", "rIG", 'D', new ItemStack(Blocks.DISPENSER), 'I', "ingotIron", 'M',
						new ItemStack(TF2weapons.itemAmmoMedigun), 'G', new ItemStack(TF2weapons.itemAmmo, 1, 8), 'R',
						new ItemStack(TF2weapons.itemTF2, 1, 11), 'r', new ItemStack(TF2weapons.itemAmmo, 1, 7), 'S',
						new ItemStack(TF2weapons.itemAmmo, 1, 1), 'm', new ItemStack(TF2weapons.itemAmmo, 1, 6) }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemBuildingBox, 1, 22),
				new Object[] { "IAI", "ARA", "IAI", 'I', "ingotIron", 'A', new ItemStack(TF2weapons.itemTF2, 1, 6), 'R',
						new ItemStack(TF2weapons.itemTF2, 1, 11) }));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("pda"),
				new Object[] { "SDT", "AGA", "BRB", 'G', "paneGlass", 'A', new ItemStack(TF2weapons.itemTF2, 1, 2), 'R',
						new ItemStack(TF2weapons.itemTF2, 1, 3), 'B', new ItemStack(Blocks.STONE_BUTTON), 'S',
						sentrybuilding, 'D', dispenserbuilding, 'T', teleporterbuilding }));
		addRecipe(new ShapedOreRecipe(null,new
				ItemStack(TF2weapons.blockAmmoFurnace), new Object[] { "RIG", "SFr", "sIM",
						'F', new ItemStack(Blocks.FURNACE), 'I', "ingotIron", 'M', new
						ItemStack(TF2weapons.itemAmmo, 1, 2), 'G', new ItemStack(TF2weapons.itemAmmo,
								1, 8), 'R', new ItemStack(TF2weapons.itemAmmo, 1, 7), 'r', new
						ItemStack(TF2weapons.itemAmmo, 1, 6), 's', new ItemStack(TF2weapons.itemAmmo,
								1, 1), 'S', new ItemStack(TF2weapons.itemAmmo, 1, 11) }));

		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemAmmoBelt),
				new Object[] { " IL", "IL ", "L  ", 'I', "ingotIron", 'L', "leather" }));
		addRecipe(new ShapedOreRecipe(null, bonk,
				new Object[] { "SDS", "IWI", "SAS", 'I', "ingotIron", 'A', new ItemStack(TF2weapons.itemTF2, 1, 6), 'W',
						new ItemStack(Items.WATER_BUCKET), 'S', new ItemStack(Items.SUGAR), 'D', "dyeYellow" }));
		addRecipe(new ShapedOreRecipe(null, cola,
				new Object[] { "SDS", "IWI", "SAS", 'I', "ingotIron", 'A', new ItemStack(TF2weapons.itemTF2, 1, 6), 'W',
						new ItemStack(Items.WATER_BUCKET), 'S', new ItemStack(Items.SUGAR), 'D', "dyeMagenta" }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemSandvich),
				new Object[] { " B ", "LHL", " B ", 'B', new ItemStack(Items.BREAD), 'L',
						new ItemStack(Blocks.TALLGRASS, 1, 1), 'H', new ItemStack(Items.PORKCHOP) }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemChocolate, 2), new Object[] { "CCC", "CCC",
				"MII", 'C', new ItemStack(Items.DYE, 1, 3), 'M', new ItemStack(Items.MILK_BUCKET), 'I', "paper" }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemScoutBoots), new Object[] { "FFF", "FBF",
				"FFF", 'F', new ItemStack(Items.FEATHER), 'B', new ItemStack(Items.LEATHER_BOOTS) }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemMantreads),
				new Object[] { "LBL", " I ", 'I', "ingotIron", 'B', new ItemStack(Items.IRON_BOOTS), 'L', "leather" }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemGunboats),
				new Object[] { " B ", "III", 'I', "ingotIron", 'B', new ItemStack(Items.IRON_BOOTS) }));
		addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemTF2, 1, 11), new Object[] { "C r", "CRr",
				"C r", 'C', Items.COMPARATOR, 'R', "blockRedstone", 'r', Items.REPEATER }));

		ItemStack jarate = ItemFromData.getNewStack("jarate");
		jarate.getTagCompound().setBoolean("IsEmpty", true);
		addRecipe(new ShapedOreRecipe(null, jarate, new Object[] { " G ", "G G", "GGG", 'G', "paneGlass" }));
		ItemStack madmilk = ItemFromData.getNewStack("madmilk");
		madmilk.getTagCompound().setBoolean("IsEmpty", true);
		addRecipe(new ShapedOreRecipe(null, madmilk, new Object[] { " G ", "G G", "GGG", 'G', "paneGlass" }));
		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("basejumper"), new Object[] { "LLL", "S S", " s ",
				'L', "leather", 'S', "string", 's', new ItemStack(TF2weapons.itemTF2, 1, 3) }));

		ItemStack banner = new ItemStack(Items.BANNER, 1, EnumDyeColor.RED.getDyeDamage());
		banner.getOrCreateSubCompound("BlockEntityTag").setTag("Patterns", new NBTTagList());
		banner.setCount(2);
		addRecipe(new ShapedOreRecipe(null, banner, new Object[] { "WWW", "WWW", "AS ", 'W', new ItemStack(Blocks.WOOL),
				'A', new ItemStack(TF2weapons.itemTF2, 1, 2), 'S', Items.STICK }));

		ItemStack bannern = new ItemStack(Items.BANNER, 1, EnumDyeColor.GRAY.getDyeDamage());
		bannern.getOrCreateSubCompound("BlockEntityTag").setTag("Patterns", new NBTTagList());
		bannern.setCount(4);

		NBTTagCompound pattern = new NBTTagCompound();

		pattern.setString("Pattern", "nb");
		pattern.setInteger("Color", 15);
		bannern.getOrCreateSubCompound("BlockEntityTag").getTagList("Patterns", 10).appendTag(pattern);
		addRecipe(new ShapedOreRecipe(null, bannern, new Object[] { "WWW", "WWW", " SA", 'W',
				new ItemStack(Blocks.WOOL), 'A', new ItemStack(TF2weapons.itemTF2, 1, 2), 'S', Items.STICK }));

		/*
		 * addRecipe(new ShapedOreRecipe(null,new
		 * ItemStack(TF2weapons.blockOverheadDoor), new Object[] { "BI", "B ", "B ",
		 * 'B', Blocks.IRON_BARS, 'I',"ingotIron" })); addRecipe(new
		 * ShapedOreRecipe(null,new ItemStack(TF2weapons.itemDoorController,1,0), new
		 * Object[] { "RIR", "IOI", "RIR", 'R', "dustRedstone", 'I',"ingotIron", 'O',
		 * Blocks.OBSERVER })); addRecipe(new ShapedOreRecipe(null,new
		 * ItemStack(TF2weapons.itemDoorController,1,1), new Object[] { "SIS", "IOI",
		 * "SIS", 'S', "gunpowder", 'I',"ingotIron", 'O', Blocks.OBSERVER }));
		 * addRecipe(new ShapedOreRecipe(null,new
		 * ItemStack(TF2weapons.itemDoorController,1,2), new Object[] { "RIR", "IOI",
		 * "RIR", 'R', "dyeRed", 'I',"ingotIron", 'O', Blocks.OBSERVER }));
		 * addRecipe(new ShapedOreRecipe(null,new
		 * ItemStack(TF2weapons.itemDoorController,1,3), new Object[] { "RIR", "IOI",
		 * "RIR", 'R', "dyeBlue", 'I',"ingotIron", 'O', Blocks.OBSERVER }));
		 */

		addRecipe(new ShapedOreRecipe(null, ItemFromData.getNewStack("startwrench"),
				new Object[] { " II", " S ", "I  ", 'I', "ingotIron", 'S', new ItemStack(TF2weapons.itemTF2, 1, 3) }));
		if (!TF2ConfigVars.disableBossSpawnItems) {
			addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemBossSpawn, 1, 0), new Object[] { "II",
					"SA", "S ", 'I', "ingotIron", 'S', "stickWood", 'A', new ItemStack(TF2weapons.itemTF2, 1, 2) }));
			addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemBossSpawn, 1, 1),
					new Object[] { "EAE", "ESE", "EAE", 'E', Items.SPIDER_EYE, 'A',
							new ItemStack(TF2weapons.itemTF2, 1, 6), 'S', new IngredientWeapon("bottle") }));
			addRecipe(new ShapedOreRecipe(null, new ItemStack(TF2weapons.itemBossSpawn, 1, 2),
					new Object[] { "BEG", "NAN", "GEB", 'E', "gemEmerald", 'A', new ItemStack(TF2weapons.itemTF2, 1, 2),
							'B', Items.BOOK, 'G', Items.GLASS_BOTTLE, 'N', "cropNetherWart" }));
		}
		addRecipe(new ShapelessOreRecipe(null, new ItemStack(TF2weapons.blockUpgradeStation),
				new Object[] { new IngredientWeapon("headtaker"), new IngredientWeapon("monoculus"),
						new IngredientWeapon("merasmushat") }));
		// addRecipe(new ShapedOreRecipe(null,new
		// ItemStack(TF2weapons.blockRobotDeploy), new Object[] { "LLL", "G G", "AIA",
		// 'L', new ItemStack(TF2weapons.itemTF2, 1, 11), 'G', "blockGlass", 'I',
		// "blockIron","A",new ItemStack(TF2weapons.itemTF2, 1, 2)}));
		addShapelessRecipe(new ItemStack(TF2weapons.itemTF2, 1, 4), new ItemStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemTF2, 1, 3), new ItemStack(TF2weapons.itemTF2, 1, 3));
		addShapelessRecipe(new ItemStack(TF2weapons.itemTF2, 1, 5), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), new ItemStack(TF2weapons.itemTF2, 1, 4));
		addShapelessRecipe(new ItemStack(TF2weapons.itemTF2, 3, 4), new ItemStack(TF2weapons.itemTF2, 1, 5));
		addShapelessRecipe(new ItemStack(TF2weapons.itemTF2, 3, 3), new ItemStack(TF2weapons.itemTF2, 1, 4));
		addShapelessRecipe(new ItemStack(TF2weapons.itemTF2, 1, 9), new ItemStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemTF2, 1, 3));
		addShapelessRecipe(new ItemStack(TF2weapons.itemTF2, 1, 10), new ItemStack(TF2weapons.itemTF2, 1, 5),
				new ItemStack(TF2weapons.itemTF2, 1, 5), new ItemStack(TF2weapons.itemTF2, 1, 5));

		addRecipe(new OpenCrateRecipe());
		addRecipe(new RecipeToScrap(-1));
		for (int i = 0; i < 9; i++)
			addRecipe(new RecipeToScrap(i));
		addRecipe(new RecipeToken(null, new ItemStack(TF2weapons.itemTF2, 1, 9),
				new Object[] { new ItemStack(TF2weapons.itemTF2, 1, 3),
						new ItemStack(TF2weapons.itemToken, 1, OreDictionary.WILDCARD_VALUE) }));

		//TF2 Blueprint Recipes
		addShapelessRecipe(ItemFromData.getNewStack("cleaver"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("madmilk"), ItemFromData.getNewStack("madmilk"), ItemFromData.getNewStack("madmilk"));
		addShapelessRecipe(ItemFromData.getNewStack("babyfaceblaster"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("shortstop"), ItemFromData.getNewStack("shortstop"));
		addShapelessRecipe(ItemFromData.getNewStack("prettyboyspocketpistol"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("madmilk"), ItemFromData.getNewStack("winger"));
		/*addShapelessRecipe(ItemFromData.getNewStack("wrapassassin"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("southernhospitality"));*/
		addShapelessRecipe(ItemFromData.getNewStack("sodapopper"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("bonk"), ItemFromData.getNewStack("forceanature"));
		addShapelessRecipe(ItemFromData.getNewStack("winger"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("shortstop"));
		addShapelessRecipe(ItemFromData.getNewStack("atomizer"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("bonk"), ItemFromData.getNewStack("sandman"));
		/*addShapelessRecipe(ItemFromData.getNewStack("fanowar"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("madmilk"));*/
		addShapelessRecipe(ItemFromData.getNewStack("sunonastick"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("bostonbasher"));
		addShapelessRecipe(ItemFromData.getNewStack("candycane"), ItemFromData.getNewStack("kritzkrieg"),
				ItemFromData.getNewStack("paintrain"));
		addShapelessRecipe(ItemFromData.getNewStack("shortstop"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("forceanature"));
		addShapelessRecipe(ItemFromData.getNewStack("madmilk"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("jarate"));
		addShapelessRecipe(ItemFromData.getNewStack("holymackerel"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("sandman"));
		addShapelessRecipe(ItemFromData.getNewStack("critcola"), ItemFromData.getNewStack("kritzkrieg"),
				ItemFromData.getNewStack("bonk"));
		/*addShapelessRecipe(ItemFromData.getNewStack("backscatter"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("shortstop"), ItemFromData.getNewStack("critcola"));*/
		addShapelessRecipe(ItemFromData.getNewStack("beggarbazooka"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("directhit"), ItemFromData.getNewStack("directhit"), ItemFromData.getNewStack("directhit"));
		/*addShapelessRecipe(ItemFromData.getNewStack("escape"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("disciplinary"));*/
		addShapelessRecipe(ItemFromData.getNewStack("original"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemTF2, 1, 3), new ItemStack(TF2weapons.itemTF2, 1, 4));
		addShapelessRecipe(ItemFromData.getNewStack("cowmangler"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("blackbox"));
		addShapelessRecipe(new ItemStack(TF2weapons.itemMantreads), new ItemStack(TF2weapons.itemTF2, 1, 5),
				new ItemStack(TF2weapons.itemGunboats));
		/*addShapelessRecipe(ItemFromData.getNewStack("reserveshooter"), new ItemStack(TF2weapons.itemTF2, 1, 5),
				new ItemStack(TF2weapons.itemTF2, 1, 5), ItemFromData.getNewStack("frontierjustice"));*/
		addShapelessRecipe(ItemFromData.getNewStack("disciplinary"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("paintrain"));
		addShapelessRecipe(ItemFromData.getNewStack("marketgarden"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemGunboats), ItemFromData.getNewStack("paintrain"));
		addShapelessRecipe(ItemFromData.getNewStack("halfzatoichi"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eyelander"), ItemFromData.getNewStack("eyelander"));
		addShapelessRecipe(ItemFromData.getNewStack("concheror"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("battalionbackup"));
		addShapelessRecipe(ItemFromData.getNewStack("blackbox"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("directhit"));
		addShapelessRecipe(ItemFromData.getNewStack("battalionbackup"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("buffbanner"));
		addShapelessRecipe(new ItemStack(TF2weapons.itemGunboats), ItemFromData.getNewStack("razorback"),
				ItemFromData.getNewStack("chargintarge"));
		addShapelessRecipe(ItemFromData.getNewStack("paintrain"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("sandman"));
		addShapelessRecipe(ItemFromData.getNewStack("rocketjumper"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemMantreads), new ItemStack(TF2weapons.itemMantreads), new ItemStack(TF2weapons.itemMantreads));
		addShapelessRecipe(ItemFromData.getNewStack("airstrike"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("beggarbazooka"), new ItemStack(TF2weapons.itemGunboats));
		addShapelessRecipe(ItemFromData.getNewStack("basejumper"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("buffbanner"), ItemFromData.getNewStack("stickybomblauncher"));
		/*addShapelessRecipe(ItemFromData.getNewStack("panicattack"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("backscatter"));*/
		addShapelessRecipe(ItemFromData.getNewStack("neonannihilator"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("thirddegree"), ItemFromData.getNewStack("thirddegree"));
		/*addShapelessRecipe(ItemFromData.getNewStack("scorchshot"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("flaregun"), ItemFromData.getNewStack("degreaser"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("phlog"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("sodapopper"), ItemFromData.getNewStack("backburner"));*/
		addShapelessRecipe(ItemFromData.getNewStack("manmelter"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("detonator"));
		addShapelessRecipe(ItemFromData.getNewStack("thirddegree"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("axtinguisher"), ItemFromData.getNewStack("powerjack"));
		/*addShapelessRecipe(ItemFromData.getNewStack("reserveshooter"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("frontierjustice"));*/
		addShapelessRecipe(ItemFromData.getNewStack("flaregun"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("detonator"));
		addShapelessRecipe(ItemFromData.getNewStack("sharpenedvolcanofragment"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("axtinguisher"));
		/*addShapelessRecipe(ItemFromData.getNewStack("backscratcher"), ItemFromData.getNewStack("axtinguisher"),
				ItemFromData.getNewStack("scotsmansskullcutter"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("degreaser"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("backburner"));*/
		addShapelessRecipe(ItemFromData.getNewStack("powerjack"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("axtinguisher"));
		/*addShapelessRecipe(ItemFromData.getNewStack("homewrecker"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("equalizer"));*/
		/*addShapelessRecipe(new ItemStack(TF2weapons.itemWeeBooties), new ItemStack(TF2weapons.itemMantreads),
				ItemFromData.getNewStack("gru"));*/
		addShapelessRecipe(ItemFromData.getNewStack("splendidscreen"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("chargintarge"));
		/*addShapelessRecipe(ItemFromData.getNewStack("persianpursuader"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("halfzatoichi"), ItemFromData.getNewStack("halfzatoichi"));*/
		addShapelessRecipe(ItemFromData.getNewStack("halfzatoichi"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eyelander"), ItemFromData.getNewStack("eyelander"));
		addShapelessRecipe(ItemFromData.getNewStack("lochnload"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("scottishresistance"));
		/*addShapelessRecipe(ItemFromData.getNewStack("caber"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("paintrain"));*/
		addShapelessRecipe(ItemFromData.getNewStack("caber"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("paintrain"));
		/*addShapelessRecipe(ItemFromData.getNewStack("claidheamhmor"), ItemFromData.getNewStack("homewrecker"),
				ItemFromData.getNewStack("chargintarge"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("headtaker"), new ItemStack(TF2weapons.itemTF2, 1, 5),
				new ItemStack(TF2weapons.itemTF2, 1, 5), new ItemStack(TF2weapons.itemTF2, 1, 8),
				ItemFromData.getNewStack("scotsmansskullcutter"));*/
		addShapelessRecipe(ItemFromData.getNewStack("scotsmansskullcutter"), ItemFromData.getNewStack("axtinguisher"),
				ItemFromData.getNewStack("jarate"));
		addShapelessRecipe(ItemFromData.getNewStack("paintrain"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("sandman"));
		addShapelessRecipe(ItemFromData.getNewStack("loosecannon"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("lochnload"), ItemFromData.getNewStack("lochnload"), ItemFromData.getNewStack("lochnload"));
		addShapelessRecipe(ItemFromData.getNewStack("stickyjumper"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("caber"), ItemFromData.getNewStack("caber"), ItemFromData.getNewStack("caber"));
		/*addShapelessRecipe(ItemFromData.getNewStack("tideturner"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemWeeBooties), ItemFromData.getNewStack("chargintarge"));*/
		addShapelessRecipe(ItemFromData.getNewStack("ironbomber"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("airstrike"));
		/*addShapelessRecipe(ItemFromData.getNewStack("quickiebomblauncher"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("tideturner"));*/
		addShapelessRecipe(ItemFromData.getNewStack("heater"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("familybusiness"), ItemFromData.getNewStack("familybusiness"),
				ItemFromData.getNewStack("familybusiness"));
		/*addShapelessRecipe(ItemFromData.getNewStack("holidaypunch"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("holymackerel"), ItemFromData.getNewStack("gru"));*/
		addShapelessRecipe(ItemFromData.getNewStack("tomislav"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("brassbeast"));
		addShapelessRecipe(ItemFromData.getNewStack("familybusiness"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("frontierjustice"), ItemFromData.getNewStack("homewrecker"));
		addShapelessRecipe(ItemFromData.getNewStack("evictionnotice"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("fistofsteel"));
		addShapelessRecipe(ItemFromData.getNewStack("brassbeast"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("natascha"));
		/*addShapelessRecipe(new ItemStack(TF2weapons.itemBuffaloSandvich), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemSandvich));*/
		addShapelessRecipe(ItemFromData.getNewStack("warriorsspirit"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("gru"));
		addShapelessRecipe(ItemFromData.getNewStack("fistofsteel"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("gru"));
		addShapelessRecipe(new ItemStack(TF2weapons.itemChocolate), new ItemStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemSandvich));
		addShapelessRecipe(ItemFromData.getNewStack("pomson"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("righterousbison"), ItemFromData.getNewStack("shortcircuit"));
		addShapelessRecipe(ItemFromData.getNewStack("eurekaeffect"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("jag"), ItemFromData.getNewStack("jag"));
		addShapelessRecipe(ItemFromData.getNewStack("widowmaker"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 4), ItemFromData.getNewStack("letranger"));
		addShapelessRecipe(ItemFromData.getNewStack("shortcircuit"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("gunslinger"));
		addShapelessRecipe(ItemFromData.getNewStack("jag"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("southernhospitality"));
		addShapelessRecipe(ItemFromData.getNewStack("southernhospitality"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("ambassador"));
		addShapelessRecipe(ItemFromData.getNewStack("rescueranger"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eurekaeffect"), ItemFromData.getNewStack("eurekaeffect"),
				ItemFromData.getNewStack("eurekaeffect"));
		addShapelessRecipe(ItemFromData.getNewStack("overdose"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("gru"), ItemFromData.getNewStack("crossbow"));
		addShapelessRecipe(ItemFromData.getNewStack("quickfix"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("madmilk"), ItemFromData.getNewStack("kritzkrieg"));
		/*addShapelessRecipe(ItemFromData.getNewStack("solemnvow"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("jarate"),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("jarate"),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("jarate"),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("jarate"));*/
		addShapelessRecipe(ItemFromData.getNewStack("crossbow"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("huntsman"));
		/*addShapelessRecipe(ItemFromData.getNewStack("amputator"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("vitasaw"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("vitasaw"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				new ItemStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("ubersaw"));*/
		addShapelessRecipe(ItemFromData.getNewStack("vaccinator"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("quickfix"), ItemFromData.getNewStack("quickfix"), ItemFromData.getNewStack("quickfix"));
		/*addShapelessRecipe(ItemFromData.getNewStack("heatmaker"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("machina"), ItemFromData.getNewStack("bazaarbargain"));*/
		addShapelessRecipe(ItemFromData.getNewStack("cleanerscarbine"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("bushwacka"), ItemFromData.getNewStack("bushwacka"), ItemFromData.getNewStack("bushwacka"));
		addShapelessRecipe(ItemFromData.getNewStack("cozycamper"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("jarate"), ItemFromData.getNewStack("razorback"));
		addShapelessRecipe(ItemFromData.getNewStack("machina"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("righterousbison"), ItemFromData.getNewStack("sydneysleeper"));
		/*addShapelessRecipe(ItemFromData.getNewStack("bazaarbargain"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("sydneysleeper"), ItemFromData.getNewStack("eyelander"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("shahanshah"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("amputator"), ItemFromData.getNewStack("bushwacka"));*/
		addShapelessRecipe(ItemFromData.getNewStack("sydneysleeper"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("huntsman"));
		addShapelessRecipe(ItemFromData.getNewStack("dangershield"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("razorback"));
		addShapelessRecipe(ItemFromData.getNewStack("bushwacka"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eyelander"));
		addShapelessRecipe(ItemFromData.getNewStack("tribalmansshiv"), ItemFromData.getNewStack("deadringer"),
				ItemFromData.getNewStack("huntsman"));
		/*addShapelessRecipe(ItemFromData.getNewStack("classic"),  new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("huntsman"), ItemFromData.getNewStack("bazaarbargain"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("recorder"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("spycicle"), ItemFromData.getNewStack("spycicle"),
				ItemFromData.getNewStack("spycicle"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("spycicle"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("eternalreward"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("diamondback"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("frontierjustice"), ItemFromData.getNewStack("deadringer"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("enforcer"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				new ItemStack(TF2weapons.itemTF2, 1, 3), ItemFromData.getNewStack("lestranger"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("bigearner"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("kunai"), ItemFromData.getNewStack("lestranger"));*/
		addShapelessRecipe(ItemFromData.getNewStack("kunai"), new ItemStack(TF2weapons.itemTF2, 1, 3),
				ItemFromData.getNewStack("eternalreward"));
		/*addShapelessRecipe(ItemFromData.getNewStack("lestranger"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("deadringer"));*/
		/*addShapelessRecipe(ItemFromData.getNewStack("eternalreward"), new ItemStack(TF2weapons.itemTF2, 1, 4),
				ItemFromData.getNewStack("cloakanddagger"));*/
	}

	public ShapedRecipes addRecipe(ItemStack stack, Object... recipeComponents) {
		ShapedPrimer primer = CraftingHelper.parseShaped(recipeComponents);
		ShapedRecipes recipe;
		recipe = new ShapedRecipes("", primer.width, primer.height, primer.input, stack);
		this.recipes.add(recipe);
		return recipe;
	}

	/**
	 * Adds a shapeless crafting recipe to the the game.
	 */
	public IRecipe addShapelessRecipe(ItemStack stack, Object... recipeComponents) {
		NonNullList<Ingredient> list = NonNullList.create();

		for (Object object : recipeComponents)
			list.add(CraftingHelper.getIngredient(object));

		ShapelessRecipes recipe;
		recipe = new ShapelessRecipes("", stack, list);
		this.recipes.add(recipe);
		return recipe;
	}

	/**
	 * Adds an IRecipe to the list of crafting recipes.
	 */
	public IRecipe addRecipe(IRecipe recipe) {
		this.recipes.add(recipe);
		return recipe;
	}

	public void addRecipeCondition(IRecipe recipe, Predicate<EntityPlayer> predicate) {
		this.recipeConditions.put(recipe, predicate);
	}

	/**
	 * Retrieves an ItemStack that has multiple recipes for it.
	 */
	@Nullable
	public ItemStack findMatchingRecipe(InventoryCrafting craftMatrix, World worldIn, EntityPlayer player) {
		for (IRecipe irecipe : this.recipes)
			if (!(this.recipeConditions.containsKey(irecipe) && !this.recipeConditions.get(irecipe).apply(player))
					&& irecipe.matches(craftMatrix, worldIn))
				return irecipe.getCraftingResult(craftMatrix);

		return ItemStack.EMPTY;
	}

	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting craftMatrix, World worldIn) {
		for (IRecipe irecipe : this.recipes)
			if (irecipe.matches(craftMatrix, worldIn))
				return irecipe.getRemainingItems(craftMatrix);

		NonNullList<ItemStack> aitemstack = NonNullList.withSize(craftMatrix.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < aitemstack.size(); ++i)
			aitemstack.set(i, craftMatrix.getStackInSlot(i));

		return aitemstack;
	}

	public List<IRecipe> getRecipeList() {
		return this.recipes;
	}
}
