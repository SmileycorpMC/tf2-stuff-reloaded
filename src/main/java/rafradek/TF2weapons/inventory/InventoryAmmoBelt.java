package rafradek.TF2weapons.inventory;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import rafradek.TF2weapons.TF2weapons;

public class InventoryAmmoBelt extends InventoryBasic implements ICapabilityProvider, INBTSerializable<NBTTagList> {

	public InventoryAmmoBelt() {
		super("AmmoBelt", false, 9);
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < this.getSizeInventory(); i++)
			if (this.getStackInSlot(0) != null)
				return false;
		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return TF2weapons.INVENTORY_BELT_CAP != null && capability == TF2weapons.INVENTORY_BELT_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (TF2weapons.INVENTORY_BELT_CAP != null && capability == TF2weapons.INVENTORY_BELT_CAP)
			return TF2weapons.INVENTORY_BELT_CAP.cast(this);
		return null;
	}

	@Override
	public NBTTagList serializeNBT() {
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < this.getSizeInventory(); i++) {
			ItemStack itemstack = this.getStackInSlot(i);

			if (!itemstack.isEmpty()) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				itemstack.writeToNBT(nbttagcompound);
				list.appendTag(nbttagcompound);
			}
		}
		// System.out.println("Saving ");
		return list;
	}

	@Override
	public void deserializeNBT(NBTTagList nbt) {

		for (int i = 0; i < nbt.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbt.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot");
			this.setInventorySlotContents(j, new ItemStack(nbttagcompound));
		}
		// System.out.println("Reading ");
	}
}
