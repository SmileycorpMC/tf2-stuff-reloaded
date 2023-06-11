package rafradek.TF2weapons.item.crafting;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IRecipeTF2 {

	@Nonnull
	public ItemStack getSuggestion(int slot);

}
