package rafradek.TF2weapons.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rafradek.TF2weapons.block.BlockAmmoFurnace;
import rafradek.TF2weapons.inventory.ContainerAmmoFurnace;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityAmmoFurnace extends TileEntityLockable implements ITickable, ISidedInventory {
	private static final int[] SLOTS_TOP = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] SLOTS_BOTTOM = new int[] { 10, 11, 12, 13, 14, 15, 16, 17, 18, 9 };
	private static final int[] SLOTS_SIDES = new int[] { 9 };
	/**
	 * The ItemStacks that hold the items currently being used in the furnace
	 */
	private NonNullList<ItemStack> furnaceItemStacks = NonNullList.withSize(19, ItemStack.EMPTY);
	/** The number of ticks that the furnace will keep burning */
	private int furnaceBurnTime;
	/**
	 * The number of ticks that a fresh copy of the currently-burning item would
	 * keep the furnace burning for
	 */
	private int currentItemBurnTime;
	private int cookTime;
	private int totalCookTime;
	private String furnaceCustomName;

	private ShapelessOreRecipe cachedRecipe;
	private int cachedSlot = -1;

	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory() {
		return this.furnaceItemStacks.size();
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	@Nullable
	public ItemStack getStackInSlot(int index) {
		return this.furnaceItemStacks.get(index);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns
	 * them in a new stack.
	 */
	@Override
	@Nullable
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.furnaceItemStacks, index, count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	@Nullable
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.furnaceItemStacks, index);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
		boolean flag = !stack.isEmpty() && stack.isItemEqual(this.furnaceItemStacks.get(index))
				&& ItemStack.areItemStackTagsEqual(stack, this.furnaceItemStacks.get(index));
		this.furnaceItemStacks.set(index, stack);

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

	/**
	 * Get the name of this object. For players this returns their username
	 */
	@Override
	public String getName() {
		return this.hasCustomName() ? this.furnaceCustomName : "container.ammofurnace";
	}

	/**
	 * Returns true if this thing is named
	 */
	@Override
	public boolean hasCustomName() {
		return this.furnaceCustomName != null && !this.furnaceCustomName.isEmpty();
	}

	public void setCustomInventoryName(String p_145951_1_) {
		this.furnaceCustomName = p_145951_1_;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		// NBTTagList nbttaglist = compound.getTagList("Items", 10);
		ItemStackHelper.loadAllItems(compound, furnaceItemStacks);
		/*
		 * this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];
		 *
		 * for (int i = 0; i < nbttaglist.tagCount(); ++i) { NBTTagCompound
		 * nbttagcompound = nbttaglist.getCompoundTagAt(i); int j =
		 * nbttagcompound.getByte("Slot");
		 *
		 * if (j >= 0 && j < this.furnaceItemStacks.length) this.furnaceItemStacks[j] =
		 * ItemStack.loadItemStackFromNBT(nbttagcompound); }
		 */

		this.furnaceBurnTime = compound.getInteger("BurnTime");
		this.cookTime = compound.getInteger("CookTime");
		this.totalCookTime = compound.getInteger("CookTimeTotal");
		this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks.get(1));

		if (compound.hasKey("CustomName", 8))
			this.furnaceCustomName = compound.getString("CustomName");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("BurnTime", this.furnaceBurnTime);
		compound.setInteger("CookTime", this.cookTime);
		compound.setInteger("CookTimeTotal", this.totalCookTime);
		ItemStackHelper.saveAllItems(compound, furnaceItemStacks);
		/*
		 * NBTTagList nbttaglist = new NBTTagList();
		 *
		 * for (int i = 0; i < this.furnaceItemStacks.length; ++i) if
		 * (this.furnaceItemStacks[i] != null) { NBTTagCompound nbttagcompound = new
		 * NBTTagCompound(); nbttagcompound.setByte("Slot", (byte) i);
		 * this.furnaceItemStacks[i].writeToNBT(nbttagcompound);
		 * nbttaglist.appendTag(nbttagcompound); }
		 *
		 * compound.setTag("Items", nbttaglist);
		 */

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
	 * Furnace isBurning
	 */
	public boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	@SideOnly(Side.CLIENT)
	public static boolean isBurning(IInventory inventory) {
		return inventory.getField(0) > 0;
	}

	/**
	 * Like the old updateEntity(), except more generic.
	 */
	@Override
	public void update() {
		boolean flag = this.isBurning();
		boolean flag1 = false;

		if (this.isBurning())
			--this.furnaceBurnTime;

		if (!this.world.isRemote) {
			if (this.isBurning() || this.furnaceItemStacks.get(9) != null) {
				if (!this.isBurning() && this.canSmelt()) {
					this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks.get(9));
					this.currentItemBurnTime = this.furnaceBurnTime;

					if (this.isBurning()) {
						flag1 = true;

						if (this.furnaceItemStacks.get(9) != null) {
							this.furnaceItemStacks.get(9).shrink(1);
							;

							if (this.furnaceItemStacks.get(9).getCount() == 0)
								this.furnaceItemStacks.set(9,
										furnaceItemStacks.get(9).getItem().getContainerItem(furnaceItemStacks.get(9)));
						}
					}
				}

				if (this.isBurning() && this.canSmelt()) {
					++this.cookTime;

					if (this.cookTime == this.totalCookTime) {
						this.cookTime = 0;
						this.totalCookTime = this.getCookTime(this.furnaceItemStacks.get(0));
						this.smeltItem();
						flag1 = true;
					}
				} else
					this.cookTime = 0;
			} else if (!this.isBurning() && this.cookTime > 0)
				this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);

			if (flag != this.isBurning()) {
				flag1 = true;
				BlockAmmoFurnace.setState(this.isBurning(), this.world, this.pos);
			}
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

	private boolean canSmelt() {
		if (cachedSlot == -1 || cachedRecipe == null |! TF2Util.matches(cachedRecipe.getRecipeOutput(), furnaceItemStacks.get(cachedSlot))) {
			for (int i = 0; i < 9; i++) if (canFitRecipe(i)) return true;
			if (cachedSlot != -1) cachedSlot = -1;
			if (cachedRecipe != null) cachedRecipe = null;
			return false;
		}
		return TF2Util.matches(cachedRecipe.getRecipeOutput(), furnaceItemStacks.get(cachedSlot));
	}

	private boolean canFitRecipe(int slot) {
		ItemStack base = this.furnaceItemStacks.get(slot);
		if (base != null &! base.isEmpty()) {
			for (ShapelessOreRecipe recipe : TF2CraftingManager.AMMO_RECIPES) {
				if (recipe == null || recipe.getRecipeOutput() == null) continue;
				if (TF2Util.matches(recipe.getRecipeOutput(), base)) {
					int[] sizes = new int[9];
					List<ItemStack> items = Lists.newArrayList(furnaceItemStacks.subList(10, 19));
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
			ItemStack base = furnaceItemStacks.get(cachedSlot);
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

	/**
	 * Returns the number of ticks that the supplied fuel item will keep the furnace
	 * burning, or 0 if the item isn't fuel
	 */
	public static int getItemBurnTime(ItemStack stack) {
		return TileEntityFurnace.getItemBurnTime(stack);
	}

	public static boolean isItemFuel(ItemStack stack) {
		/**
		 * Returns the number of ticks that the supplied fuel item will keep the furnace
		 * burning, or 0 if the item isn't fuel
		 */
		return getItemBurnTime(stack) > 0;
	}

	/**
	 * Do not make give this method the name canInteractWith because it clashes with
	 * Container
	 */
	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.world.getTileEntity(this.pos) != this ? false
				: player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring
	 * stack size) into the given slot.
	 */
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index > 9)
			return false;
		else if (index != 9)
			return true;
		else {
			ItemStack itemstack = this.furnaceItemStacks.get(9);
			return isItemFuel(stack)
					|| SlotFurnaceFuel.isBucket(stack) && (itemstack.isEmpty() || itemstack.getItem() != Items.BUCKET);
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return side == EnumFacing.DOWN ? SLOTS_BOTTOM : (side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES);
	}

	/**
	 * Returns true if automation can insert the given item in the given slot from
	 * the given side.
	 */
	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return this.isItemValidForSlot(index, itemStackIn);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot from
	 * the given side.
	 */
	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		if (direction == EnumFacing.DOWN && index == 9) {
			Item item = stack.getItem();

			if (item != Items.WATER_BUCKET && item != Items.BUCKET)
				return false;
		}

		return true;
	}

	@Override
	public String getGuiID() {
		return "minecraft:furnace";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return new ContainerAmmoFurnace(playerInventory, this);
	}

	@Override
	public int getField(int id) {
		switch (id) {
		case 0:
			return this.furnaceBurnTime;
		case 1:
			return this.currentItemBurnTime;
		case 2:
			return this.cookTime;
		case 3:
			return this.totalCookTime;
		default:
			return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch (id) {
		case 0:
			this.furnaceBurnTime = value;
			break;
		case 1:
			this.currentItemBurnTime = value;
			break;
		case 2:
			this.cookTime = value;
			break;
		case 3:
			this.totalCookTime = value;
		}
	}

	@Override
	public int getFieldCount() {
		return 4;
	}

	@Override
	public void clear() {
		for (int i = 0; i < this.furnaceItemStacks.size(); ++i)
			this.furnaceItemStacks.set(9, ItemStack.EMPTY);
	}

	IItemHandler handlerTop = new SidedInvWrapper(this,
			EnumFacing.UP);
	IItemHandler handlerBottom = new SidedInvWrapper(this,
			EnumFacing.DOWN);
	IItemHandler handlerSide = new SidedInvWrapper(this,
			EnumFacing.WEST);

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
			EnumFacing facing) {
		if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			if (facing == EnumFacing.DOWN)
				return (T) handlerBottom;
			else if (facing == EnumFacing.UP)
				return (T) handlerTop;
			else
				return (T) handlerSide;
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.furnaceItemStacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}
}
