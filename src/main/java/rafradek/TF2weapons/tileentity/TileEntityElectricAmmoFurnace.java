package rafradek.TF2weapons.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.inventory.ContainerAmmoFurnace;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityElectricAmmoFurnace extends TileEntityAbstractAmmoFurnace implements IEnergyStorage {
	private static int max_energy = 32000;
	private static final int[] SLOTS_TOP = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] SLOTS_BOTTOM = new int[] { 10, 11, 12, 13, 14, 15, 16, 17, 18, 9 };
	private static final int[] SLOTS_SIDES = new int[] { 9 };

	private NonNullList<ItemStack> furnaceItemStacks = NonNullList.withSize(19, ItemStack.EMPTY);
	private int furnaceBurnTime;
	private int storedEnergy;

	/**
	 * Get the name of this object. For players this returns their username
	 */
	@Override
	public String getName() {
		return this.hasCustomName() ? furnaceCustomName : "container.ammofurnace";
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.furnaceBurnTime = compound.getInteger("BurnTime");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("BurnTime", this.furnaceBurnTime);
		ItemStackHelper.saveAllItems(compound, furnaceItemStacks);
		return compound;
	}

	@Override
	public boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	@Override
	protected void tickFuel() {
		furnaceBurnTime++;
		storedEnergy -= 200;
	}

	@Override
	protected NonNullList<ItemStack> getInventory() {
		return furnaceItemStacks;
	}

	@Override
	protected List<ItemStack> getOutput() {
		return furnaceItemStacks.subList(10, 19);
	}

	@SideOnly(Side.CLIENT)
	public static boolean isBurning(IInventory inventory) {
		return inventory.getField(0) > 0;
	}

	public int getCookTime(@Nullable ItemStack stack) {
		return 200;
	}
	
	public static int getItemBurnTime(ItemStack stack) {
		return TileEntityFurnace.getItemBurnTime(stack);
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return side == EnumFacing.DOWN ? SLOTS_BOTTOM : (side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES);
	}

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
		return TF2weapons.MOD_ID + ":ammo_furnace";
	}

	@Override
	public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
		return new ContainerAmmoFurnace(inventory, this);
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return false;
	}

	@Override
	public int getField(int id) {
		switch (id) {
		case 0:
			return this.furnaceBurnTime;
		case 1:
			return this.storedEnergy;
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
			this.storedEnergy = value;
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
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) return (T) this;
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
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return 0;
	}
	
	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return 0;
	}
	
	@Override
	public int getEnergyStored() {
		return 0;
	}
	
	@Override
	public int getMaxEnergyStored() {
		return 0;
	}
	
	@Override
	public boolean canExtract() {
		return false;
	}
	
	@Override
	public boolean canReceive() {
		return false;
	}
}
