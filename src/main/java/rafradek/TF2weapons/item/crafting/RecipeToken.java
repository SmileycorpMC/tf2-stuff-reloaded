package rafradek.TF2weapons.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.TF2Class;

public class RecipeToken extends ShapelessOreRecipe {

	public RecipeToken(ResourceLocation group, ItemStack result, Object[] recipe, TF2Class clazz) {
		super(group, result, recipe);
		output.setTagCompound(new NBTTagCompound());
		output.getTagCompound().setByte("Token", (byte) clazz.getIndex());
		input.add(Ingredient.fromStacks(new ItemStack(TF2weapons.itemToken, 1, clazz.getIndex())));
	}

}
