package rafradek.TF2weapons.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.item.crafting.*;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.WeaponData;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

public class TF2CrafterRecipeWrapper implements IRecipeWrapper {

	protected final IRecipe recipe;
	protected int upgradeInputSlot = -1;
	protected Function<ItemStack, ItemStack> upgradeStackFunction = (stack) -> stack;
	protected Function<ItemStack, ItemStack> downgradeStackFunction = (stack) -> stack;

	public TF2CrafterRecipeWrapper(IRecipe recipe) {
		this.recipe = recipe;
	}

	public boolean isShapeless() {
		return recipe instanceof ShapelessRecipes;
	}

	public int getUpgradeInputSlot() {
		return upgradeInputSlot;
	}

	public ItemStack upgradeStack(ItemStack stack) {
		return upgradeStackFunction.apply(stack.copy());
	}

	public ItemStack downgradeStack(ItemStack stack) {
		return downgradeStackFunction.apply(stack.copy());
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
		List<List<ItemStack>> inputs = Lists.newArrayList();

		if (recipe instanceof AustraliumRecipe) {
			upgradeInputSlot = 4;
			upgradeStackFunction = (stack) -> {
				stack.getTagCompound().setBoolean("Australium", true);
				return stack;
			};
			downgradeStackFunction = (stack) -> {
				if (stack.getTagCompound().hasKey("Australium")) stack.getTagCompound().removeTag("Australium");
				return stack;
			};
			for (int i = 0; i < 9; i++) {
				if (i == 4) {
					List<ItemStack> weapons = Lists.newArrayList();
					for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
						if (!entry.getValue().getBoolean(PropertyType.HIDDEN)) {
							ItemStack weapon = ItemFromData.getNewStack(entry.getValue());
							Item item = weapon.getItem();
							if (item instanceof ItemUsable || item instanceof ItemCloak
									|| item instanceof ItemPDA || item instanceof ItemBackpack) weapons.add(weapon);
						}
					}
					inputs.add(weapons);
				} else inputs.add(Lists.newArrayList(new ItemStack(TF2weapons.itemTF2, 1, 2)));
			}
		} else if (recipe instanceof RecipeApplyEffect) {
			List<ItemStack> effectItems = Lists.newArrayList();
			ItemApplicableEffect type = (ItemApplicableEffect) ((RecipeApplyEffect) recipe).type;
			if (type instanceof ItemKillstreakKit) {
				for (TF2Attribute attribute: TF2Attribute.attributes) {
					if (attribute != null && attribute.perKill != 0) {
						for(int level = 0; level < 3; level++) {
							effectItems.add(new ItemStack(type, 1, attribute.id + (level<<9)));
						}
					}
				}
			} else effectItems.add(new ItemStack(type));
			inputs.add(effectItems);
			upgradeInputSlot = 1;
			upgradeStackFunction = (stack) -> {
				type.apply(new ItemStack(type), stack);
				return stack;
			};
			downgradeStackFunction = (stack) -> {
				return ItemFromData.getNewStack(ItemFromData.getData(stack));
			};
			List<ItemStack> weapons = Lists.newArrayList();
			for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
				if (!entry.getValue().getBoolean(PropertyType.HIDDEN) &! (entry.getValue().getString(PropertyType.CLASS).equals("crate")
						&! (entry.getValue().getString(PropertyType.CLASS).equals("cosmetic"))))
					weapons.add(ItemFromData.getNewStack(entry.getValue()));
			}
			inputs.add(weapons);
		} else if (recipe instanceof JumperRecipe) {
			ItemStack reference = recipe.getRecipeOutput();
			String weaponClass = ItemFromData.getData(reference).getString(PropertyType.CLASS);
			for (int i = 0; i < 9; i++) {
				if (i == 4) {
					List<ItemStack> weapons = Lists.newArrayList();
					for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
						if (!entry.getValue().getBoolean(PropertyType.HIDDEN) && (entry.getValue().getString(PropertyType.CLASS).equals(weaponClass))) {
							ItemStack stack = ItemFromData.getNewStack(entry.getValue());
							if (stack.getItem() == reference.getItem()) weapons.add(stack);
						}
					}
					inputs.add(weapons);
				} else inputs.add(Lists.newArrayList(new ItemStack(Items.FEATHER)));
			}
		} else if (recipe instanceof OpenCrateRecipe) {
			inputs.add(Lists.newArrayList(new ItemStack(TF2weapons.itemTF2, 1, 7)));
			List<ItemStack> crates = Lists.newArrayList();
			for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
				if (!entry.getValue().getBoolean(PropertyType.HIDDEN) && (entry.getValue().getString(PropertyType.CLASS).equals("crate")))
					crates.add(ItemFromData.getNewStack(entry.getValue()));
			}
			inputs.add(crates);
		} else if (recipe instanceof RecipeToScrap) {
			int token = ((RecipeToScrap) recipe).token;
			TF2Class clazz = (token >= 0 && token < 9) ? TF2Class.getClass(token) : TF2Class.NONE;
			List<ItemStack> weapons = Lists.newArrayList();
			for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
				if (!entry.getValue().getBoolean(PropertyType.HIDDEN) && (entry.getValue().get(PropertyType.SLOT).containsKey(clazz.getName()) || clazz == TF2Class.NONE)
						& !(entry.getValue().getString(PropertyType.CLASS).equals("crate")))
					weapons.add(ItemFromData.getNewStack(entry.getValue()));

			}
			for (int i = 0; i < (clazz == TF2Class.NONE ? 2 : 3); i++) {
				List<ItemStack> input = Lists.newArrayList(weapons);
				Collections.shuffle(input);
				inputs.add(input);
			}
		} else {
			for (Ingredient ingredient : recipe.getIngredients()) inputs.add(Lists.newArrayList(ingredient.getMatchingStacks()));
		}
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
	}

}