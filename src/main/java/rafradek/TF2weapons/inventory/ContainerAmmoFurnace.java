package rafradek.TF2weapons.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.item.ItemAmmo;

import javax.annotation.Nullable;

public class ContainerAmmoFurnace extends Container {

	private final IInventory tileFurnace;
	private int cookTime;
	private int totalCookTime;
	private int furnaceBurnTime;
	private int currentItemBurnTime;

	public ContainerAmmoFurnace(InventoryPlayer playerInventory, IInventory furnaceInventory) {
		this.tileFurnace = furnaceInventory;
		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(furnaceInventory, i, 9 + (i % 3) * 18, 17 + (i / 3) * 18));
		this.addSlotToContainer(new SlotFurnaceFuel(furnaceInventory, 9, 80, 53));
		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new SlotFurnaceOutput(playerInventory.player, furnaceInventory, i + 10,
					116 + (i % 3) * 18, 17 + (i / 3) * 18));

		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 9; ++j)
				this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

		for (int k = 0; k < 9; ++k)
			this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendAllWindowProperties(this, this.tileFurnace);
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < this.listeners.size(); ++i) {
			IContainerListener icontainerlistener = this.listeners.get(i);

			if (this.cookTime != this.tileFurnace.getField(2))
				icontainerlistener.sendWindowProperty(this, 2, this.tileFurnace.getField(2));

			if (this.furnaceBurnTime != this.tileFurnace.getField(0))
				icontainerlistener.sendWindowProperty(this, 0, this.tileFurnace.getField(0));

			if (this.currentItemBurnTime != this.tileFurnace.getField(1))
				icontainerlistener.sendWindowProperty(this, 1, this.tileFurnace.getField(1));

			if (this.totalCookTime != this.tileFurnace.getField(3))
				icontainerlistener.sendWindowProperty(this, 3, this.tileFurnace.getField(3));
		}

		this.cookTime = this.tileFurnace.getField(2);
		this.furnaceBurnTime = this.tileFurnace.getField(0);
		this.currentItemBurnTime = this.tileFurnace.getField(1);
		this.totalCookTime = this.tileFurnace.getField(3);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data) {
		this.tileFurnace.setField(id, data);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return this.tileFurnace.isUsableByPlayer(playerIn);
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	@Nullable
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index > 9 && index < 19) {
				if (!this.mergeItemStack(itemstack1, 19, 55, true))
					return ItemStack.EMPTY;

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index > 9) {
				if (itemstack1.getItem() instanceof ItemAmmo) {
					if (!this.mergeItemStack(itemstack1, 0, 9, false))
						return ItemStack.EMPTY;
				} else if (TileEntityFurnace.isItemFuel(itemstack1)) {
					if (!this.mergeItemStack(itemstack1, 9, 10, false))
						return ItemStack.EMPTY;
				} else if (index >= 19 && index < 46) {
					if (!this.mergeItemStack(itemstack1, 46, 55, false))
						return ItemStack.EMPTY;
				} else if (index >= 46 && index < 55 && !this.mergeItemStack(itemstack1, 19, 46, false))
					return ItemStack.EMPTY;
			} else if (!this.mergeItemStack(itemstack1, 19, 55, false))
				return ItemStack.EMPTY;

			if (itemstack1.isEmpty())
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();

			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}
}
