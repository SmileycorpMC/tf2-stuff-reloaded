package rafradek.TF2weapons.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemCrate;
import rafradek.TF2weapons.item.ItemFromData;

public class OpenCrateRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>
		implements IRecipe, IRecipeTF2 {

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		boolean key = false;
		ItemStack crate = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (stack.getItem() == TF2weapons.itemTF2 && stack.getMetadata() == 7) {
					if (!key)
						key = true;
					else
						return false;
				} else if (crate.isEmpty() && stack.getItem() instanceof ItemCrate)
					crate = stack;
				else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return key && crate != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (!(stack.getItem() == TF2weapons.itemTF2 && stack.getMetadata() == 7))
					stack2 = stack;
		}
		// System.out.println("OutPut: "+stack2);
		if (!stack2.isEmpty()) {
			stack2 = stack2.copy();
			stack2.getTagCompound().setBoolean("Open", true);
			stack2.setCount(1);
		}
		return stack2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		// ItemStack stack=ItemFromData.getNewStack("crate1");
		// stack.getTagCompound().setBoolean("Open", true);
		return new ItemStack(TF2weapons.itemTF2, 1, 8);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			nonnulllist.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
		}

		return nonnulllist;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		return slot == 0 ? new ItemStack(TF2weapons.itemTF2, 1, 7)
				: (slot == 1 ? ItemFromData.getNewStack("crate1") : ItemStack.EMPTY);
	}

}
