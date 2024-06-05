package rafradek.TF2weapons.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public interface IItemNoSwitch {

	boolean stopSlotSwitch(ItemStack stack, EntityLivingBase living);

	default void forceItemSlot(ItemStack stack, World world, Entity entityIn, int itemSlot, boolean isSelected) {
		if (!world.isRemote) {
			if (!isSelected && this.stopSlotSwitch(stack, (EntityPlayer) entityIn)) {
				ItemStack old = ((EntityPlayer) entityIn).getHeldItemMainhand();
				if (stack == ((EntityPlayer) entityIn).inventory.getStackInSlot(itemSlot)) {
					if (old.getItem() instanceof IItemNoSwitch
							&& ((IItemNoSwitch) old.getItem()).stopSlotSwitch(old, (EntityPlayer) entityIn)) {
						((EntityPlayer) entityIn).inventory.setInventorySlotContents(itemSlot, ItemStack.EMPTY);
						((EntityPlayer) entityIn).dropItem(stack, true);
					} else {
						((EntityPlayer) entityIn).inventory.setInventorySlotContents(itemSlot, old);
						((EntityPlayer) entityIn).setHeldItem(EnumHand.MAIN_HAND, stack);
					}
				} else if (stack == ((EntityPlayer) entityIn).getHeldItemOffhand()) {
					((EntityPlayer) entityIn).dropItem(stack, true);
					((EntityPlayer) entityIn).inventory.offHandInventory.set(0, ItemStack.EMPTY);
				}
			}
		}
	}
}