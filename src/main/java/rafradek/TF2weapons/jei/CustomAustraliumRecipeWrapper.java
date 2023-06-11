package rafradek.TF2weapons.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.crafting.AustraliumRecipe;

import java.util.List;

public class CustomAustraliumRecipeWrapper extends TF2CrafterRecipeWrapper {

	protected final Class<? extends Item> clazz;

	public CustomAustraliumRecipeWrapper(Class<? extends Item> clazz) {
		super(new AustraliumRecipe());
		this.clazz = clazz;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		List<List<ItemStack>> inputs = Lists.newArrayList();
		upgradeInputSlot = 4;
		for (int i = 0; i < 9; i++) {
			if (i == 4) {
				List<ItemStack> weapons = Lists.newArrayList();
				for (Item item : ForgeRegistries.ITEMS)
					if (clazz.isAssignableFrom(item.getClass()))
						weapons.add(new ItemStack(item));
				inputs.add(weapons);
			} else
				inputs.add(Lists.newArrayList(new ItemStack(TF2weapons.itemTF2, 1, 2)));
		}
		List<ItemStack> outputs = Lists.newArrayList();
		for (ItemStack stack : inputs.get(upgradeInputSlot)) {
			outputs.add(upgradeStack(stack.copy()));
		}
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
	}

	@Override
	public ItemStack upgradeStack(ItemStack stack) {
		stack = stack.copy();
		NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		tag.setBoolean("Australium", true);
		stack.setTagCompound(tag);
		return stack;
	}

	@Override
	public ItemStack downgradeStack(ItemStack stack) {
		stack = stack.copy();
		if (stack.getTagCompound() == null)
			return stack;
		if (stack.getTagCompound().hasKey("Australium"))
			stack.getTagCompound().removeTag("Australium");
		return stack;
	}
}