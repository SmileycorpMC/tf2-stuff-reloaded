package rafradek.TF2weapons.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rafradek.TF2weapons.block.BlockAmmoFurnace;
import rafradek.TF2weapons.inventory.ContainerAmmoFurnace;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.List;

public abstract class TileEntityAbstractAmmoFurnace extends TileEntityLockable implements ITickable, ISidedInventory {
	protected int cookTime;
	protected int totalCookTime;
	protected String furnaceCustomName;

	private ShapelessOreRecipe cachedRecipe;
	private int cachedSlot = -1;

	@Override
	public int getSizeInventory() {
		return getInventory().size();
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	@Nullable
	public ItemStack getStackInSlot(int index) {
		return getInventory().get(index);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns
	 * them in a new stack.
	 */
	@Override
	@Nullable
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(getInventory(), index, count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	@Nullable
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(getInventory(), index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
		boolean flag = !stack.isEmpty() && stack.isItemEqual(getInventory().get(index))
				&& ItemStack.areItemStackTagsEqual(stack, getInventory().get(index));
		getInventory().set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
			stack.setCount(this.getInventoryStackLimit());

		if (index < 9 && !flag) {
			if (index == cachedSlot) {
				this.totalCookTime = this.getCookTime(stack);
				this.cookTime = 0;
				this.markDirty();
			}
		}
	}
	
	@Override
	public boolean hasCustomName() {
		return this.furnaceCustomName != null && !this.furnaceCustomName.isEmpty();
	}
	
	public void setCustomInventoryName(String p_145951_1_) {
		this.furnaceCustomName = p_145951_1_;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.cookTime = compound.getInteger("CookTime");
		this.totalCookTime = compound.getInteger("CookTimeTotal");
		if (compound.hasKey("CustomName", 8))
			this.furnaceCustomName = compound.getString("CustomName");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("CookTime", this.cookTime);
		compound.setInteger("CookTimeTotal", this.totalCookTime);
		if (this.hasCustomName())
			compound.setString("CustomName", this.furnaceCustomName);

		return compound;
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64,
	 * possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	@Override
	public void update() {
		boolean burning = isBurning();
		boolean flag1 = false;
		if (!this.world.isRemote) {
			if (canSmelt()) {
				tickFuel();
			}

			if (isBurning() && canSmelt()) {
				++this.cookTime;

				if (this.cookTime == this.totalCookTime) {
					this.cookTime = 0;
					this.totalCookTime = this.getCookTime(getInventory().get(0));
					this.smeltItem();
					flag1 = true;
				}
			} else
				this.cookTime = 0;
		} else if (!isBurning() && this.cookTime > 0)
			this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);

		if (isBurning() != burning) {
			flag1 = true;
			BlockAmmoFurnace.setState(burning, this.world, this.pos);
		}
		if (flag1)
			this.markDirty();
	}

	public int getCookTime(@Nullable ItemStack stack) {
		return 200;
	}

	/**
	 * Returns true if the furnace can smelt an item, i.e. has a source item,
	 * destination stack isn't full, etc.
	 */

	protected boolean canSmelt() {
		if (cachedSlot == -1 || cachedRecipe == null |! TF2Util.matches(cachedRecipe.getRecipeOutput(), getInventory().get(cachedSlot))) {
			for (int i = 0; i < 9; i++) if (canFitRecipe(i)) return true;
			if (cachedSlot != -1) cachedSlot = -1;
			if (cachedRecipe != null) cachedRecipe = null;
			return false;
		}
		return TF2Util.matches(cachedRecipe.getRecipeOutput(), getInventory().get(cachedSlot));
	}

	private boolean canFitRecipe(int slot) {
		ItemStack base = getInventory().get(slot);
		if (base != null &! base.isEmpty()) {
			for (ShapelessOreRecipe recipe : TF2CraftingManager.AMMO_RECIPES) {
				if (recipe == null || recipe.getRecipeOutput() == null) continue;
				if (TF2Util.matches(recipe.getRecipeOutput(), base)) {
					int[] sizes = new int[9];
					List<ItemStack> items = Lists.newArrayList(getOutput());
					for (int i = 0; i < 9; i++) sizes[i] = items.get(i).getCount();
					for (Ingredient output : recipe.getIngredients()) {
						if (output.getMatchingStacks().length > 0) {
							ItemStack stack1 = output.getMatchingStacks()[0];
							int count = stack1.getCount();
							for (int i = 0; i < 9; i++) {
								ItemStack stack2 = items.get(i);
								if (stack1.getItem() == stack2.getItem() && stack1.getMetadata() == stack2.getMetadata()) {
									if (sizes[i] + count <= stack1.getMaxStackSize()) {
										sizes[i] += count;
										break;
									} else {
										count = sizes[i] + count - stack1.getMaxStackSize();
										sizes[i] = stack1.getMaxStackSize();
									}
								} else if (stack2.isEmpty()) {
									sizes[i] = count;
									items.set(i, stack1.copy());
									break;
								}
								if (i == 8) return false;
							}
						} else break;
					}
					cachedSlot = slot;
					cachedRecipe = recipe;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Turn one item from the furnace source stack into the appropriate smelted item
	 * in the furnace result stack
	 */
	public void smeltItem() {
		if (canSmelt()) {
			int ammoToConsume = cachedRecipe.getRecipeOutput().getCount();
			ItemStack base = getInventory().get(cachedSlot);
			int ammoConsumed = Math.min(base.getCount(), ammoToConsume);
			base.shrink(ammoConsumed);
			ammoToConsume -= ammoConsumed;
			if (base.getCount() <= 0)
				setInventorySlotContents(cachedSlot, ItemStack.EMPTY);

			if (ammoToConsume <= 0) {
				for (Ingredient obj : cachedRecipe.getIngredients()) {
					ItemStack out = obj.getMatchingStacks()[0];
					for (int j = 10; j < 19; j++) {
						boolean handled = false;
						ItemStack inSlot = this.getStackInSlot(j);
						if (inSlot.isEmpty()) {
							this.setInventorySlotContents(j, out.copy());
							handled = true;
						} else if (out.isItemEqual(inSlot) && ItemStack.areItemStackTagsEqual(out, inSlot)) {
							int size = out.getCount() + inSlot.getCount();

							if (size <= out.getMaxStackSize()) {
								inSlot.setCount(size);
								handled = true;
							}
						}
						if (handled)
							break;
					}
				}
				return;
			}
		}
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.world.getTileEntity(this.pos) != this ? false
				: player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return this.isItemValidForSlot(index, itemStackIn);
	}

	@Override
	public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
		return new ContainerAmmoFurnace(inventory, this);
	}

	@Override
	public void clear() {
		for (int i = 0; i < getInventory().size(); ++i)
			getInventory().set(i, ItemStack.EMPTY);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : getInventory()) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public abstract boolean isBurning();

	protected abstract void tickFuel();

	protected abstract NonNullList<ItemStack> getInventory();

	protected abstract List<ItemStack> getOutput();
	
}
