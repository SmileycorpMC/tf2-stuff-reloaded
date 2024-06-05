package rafradek.TF2weapons.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.tileentity.IEntityConfigurable;

import javax.annotation.Nullable;

public class ContainerConfigurable extends Container {
	private final World world;
	private final BlockPos pos;
	public EntityPlayer player;
	public IEntityConfigurable config;

	public ContainerConfigurable(EntityPlayer player, InventoryPlayer inventory, IEntityConfigurable config,
			World world, BlockPos posIn) {
		this.config = config;
		this.player = player;
		this.world = world;
		this.pos = posIn;
		/*
		 * this.addSlotToContainer(new Slot(upgradedItem, 0, 108, 8) {
		 * 
		 * @Override public void onSlotChanged() { super.onSlotChanged(); refreshData();
		 * transactions = new int[applicable.size()]; transactionsCost = new
		 * int[applicable.size()];
		 * 
		 * }
		 * 
		 * @Override public boolean isItemValid(@Nullable ItemStack stack) { if
		 * (stack.isEmpty()) return false; else return stack.getItem() instanceof
		 * ItemFromData && (stack.getMaxStackSize() == 1 || stack.getItem() instanceof
		 * ItemCleaver); } });
		 */

		/*
		 * for (int k = 0; k < 3; ++k) { for (int i1 = 0; i1 < 9; ++i1) {
		 * this.addSlotToContainer(new Slot(cabinet, i1 + k * 9 + 9, 8 + i1 * 18, 91 + k
		 * * 18)); } }
		 */

		for (int k = 0; k < 3; ++k)
			for (int i1 = 0; i1 < 9; ++i1)
				this.addSlotToContainer(new Slot(inventory, i1 + k * 9 + 9, 36 + i1 * 18, 143 + k * 18 + 300));

		for (int l = 0; l < 9; ++l)
			this.addSlotToContainer(new Slot(inventory, l, 36 + l * 18, 201 + 300));
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data) {}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);

		if (!this.world.isRemote) {}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.world.getTileEntity(pos) != this.config ? false
				: player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
						this.pos.getZ() + 0.5D) <= 64.0D;
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	@Nullable
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 0) {
				if (!this.mergeItemStack(itemstack1, 1, 28, true))
					return ItemStack.EMPTY;

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 1 && index < 28) {
				if (!this.mergeItemStack(itemstack1, 0, 1, false))
					return ItemStack.EMPTY;
			} else if (!this.mergeItemStack(itemstack1, 1, 28, false))
				return ItemStack.EMPTY;

			if (itemstack1.isEmpty())
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();

			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean enchantItem(EntityPlayer player, int id) {

		return true;
	}
}
