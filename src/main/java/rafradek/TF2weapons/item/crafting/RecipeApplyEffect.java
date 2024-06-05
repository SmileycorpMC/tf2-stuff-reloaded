package rafradek.TF2weapons.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemApplicableEffect;
import rafradek.TF2weapons.item.ItemFromData;

public class RecipeApplyEffect implements IRecipe, IRecipeTF2 {

	public static final ItemStack ITEM = ItemFromData.getNewStack("minigun");

	public Item type;

	public RecipeApplyEffect(Item type) {
		this.type = type;

	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		ItemStack effect = ItemStack.EMPTY;
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (effect.isEmpty() && stack.getItem() == type) {
					effect = stack;
				} else if (stack2.isEmpty() && stack.getItem() != type)
					stack2 = stack;
				else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return !effect.isEmpty() && !stack2.isEmpty()
				&& ((ItemApplicableEffect) effect.getItem()).isApplicable(effect, stack2);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack effect = ItemStack.EMPTY;
		ItemStack stack2 = ItemStack.EMPTY;

		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (effect.isEmpty() && stack.getItem() == type) {
					effect = stack;
				} else if (stack2.isEmpty() && stack.getItem() != type)
					stack2 = stack;
		}
		// System.out.println("OutPut: "+stack2);
		if (!stack2.isEmpty()) {
			stack2 = stack2.copy();
			if (!stack2.hasTagCompound())
				stack2.setTagCompound(new NBTTagCompound());
			stack2.setCount(1);
			((ItemApplicableEffect) effect.getItem()).apply(effect, stack2);

			// stack2.getTagCompound().setBoolean("Australium", true);
			//
		}
		return stack2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		ItemStack stack = ITEM.copy();
		((ItemApplicableEffect) type).apply(new ItemStack(type), stack);
		return stack;
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
	public IRecipe setRegistryName(ResourceLocation name) {
		return null;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return new ResourceLocation(TF2weapons.MOD_ID, this.type.toString());
	}

	@Override
	public Class<IRecipe> getRegistryType() {
		return IRecipe.class;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		return slot == 0 ? ITEM : slot == 1 ? new ItemStack(this.type) : ItemStack.EMPTY;
	}

}
