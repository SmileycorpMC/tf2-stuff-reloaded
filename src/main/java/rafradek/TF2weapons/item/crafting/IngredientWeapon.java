package rafradek.TF2weapons.item.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import rafradek.TF2weapons.item.ItemFromData;

import javax.annotation.Nullable;

public class IngredientWeapon extends Ingredient {

	private final ItemStack stack;

	protected IngredientWeapon(String string) {
		super(ItemFromData.getNewStack(string));
		this.stack = ItemFromData.getNewStack(string);
	}

	@Override
	public boolean apply(@Nullable ItemStack input) {
		if (input == null)
			return false;
		// Can't use areItemStacksEqualUsingNBTShareTag because it compares stack size
		// as well
		return this.stack.getItem() == input.getItem() && this.stack.getItemDamage() == input.getItemDamage()
				&& ItemFromData.getData(input).equals(ItemFromData.getData(stack));
	}

	@Override
	public boolean isSimple() {
		return false;
	}
}
