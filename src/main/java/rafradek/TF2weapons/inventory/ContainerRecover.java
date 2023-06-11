package rafradek.TF2weapons.inventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import rafradek.TF2weapons.TF2weapons;

import javax.annotation.Nullable;

public class ContainerRecover extends Container {

	private final ItemStackHandler lostItemsHandler;

	public ContainerRecover(IInventory playerInventory, ItemStackHandler dispenserInventoryIn) {
		this.lostItemsHandler = dispenserInventoryIn;

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new SlotItemHandler(dispenserInventoryIn, j + i * 9, 8 + j * 18, 18 + i * 18) {
					@Override
					public boolean isItemValid(@Nullable ItemStack stack) {
						return false;
					}
				});
			}
		}

		for (int k = 0; k < 3; ++k) {
			for (int i1 = 0; i1 < 9; ++i1) {
				this.addSlotToContainer(new Slot(playerInventory, i1 + k * 9 + 27, 8 + i1 * 18, 85 + k * 18));
			}
		}

		for (int l = 0; l < 9; ++l) {
			this.addSlotToContainer(new Slot(playerInventory, l, 8 + l * 18, 143));
		}
	}

	public static int getAustraliumCount(EntityPlayer player) {
		int count = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() == TF2weapons.itemTF2 && stack.getItemDamage() == 6)
					count++;
				else if (stack.getItem() == TF2weapons.itemTF2 && stack.getItemDamage() == 2)
					count += 9;
				else if (Block.getBlockFromItem(stack.getItem()) == TF2weapons.blockAustralium)
					count += 81;
			}
		}
		return count;
	}

	/**
	 * Determines whether supplied player can use this container
	 */
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
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

			if (index < 27) {
				if (!this.mergeItemStack(itemstack1, 27, 63, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}

}
