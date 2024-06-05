package rafradek.TF2weapons.item.crafting;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import rafradek.TF2weapons.item.ItemFromData;

public class JumperRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>
		implements IRecipe, IRecipeTF2 {

	String nameBefore;
	String nameAfter;

	public JumperRecipe(String nameBefore, String nameAfter) {
		this.nameAfter = nameAfter;
		this.nameBefore = nameBefore;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		int feather = 0;
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (stack.getItem() == Items.FEATHER) {
					if (feather < 8)
						feather++;
					else
						return false;
				} else if (stack2.isEmpty() && ItemFromData.isSameType(stack, nameBefore))
					stack2 = stack;
				else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return feather == 8 && !stack2.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (!(stack.getItem() == Items.FEATHER))
					stack2 = stack;
		}
		// System.out.println("OutPut: "+stack2);
		if (!stack2.isEmpty()) {
			if (ItemFromData.isSameType(stack2, nameBefore))
				stack2 = ItemFromData.getNewStack(nameAfter);
		}
		return stack2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemFromData.getNewStack(nameAfter);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> aitemstack = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < aitemstack.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			aitemstack.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
		}

		return aitemstack;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		return slot == 4 ? ItemFromData.getNewStack(this.nameBefore) : new ItemStack(Items.FEATHER);
	}

}
