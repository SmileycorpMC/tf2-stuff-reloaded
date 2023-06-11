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
