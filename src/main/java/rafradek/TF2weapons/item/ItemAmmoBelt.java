package rafradek.TF2weapons.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.TF2weapons.TF2weapons;

public class ItemAmmoBelt extends Item {

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer living, EnumHand hand) {
		if (!world.isRemote)
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new ActionResult<>(EnumActionResult.SUCCESS, living.getHeldItem(hand));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return TF2weapons.MOD_ID + ":textures/models/tf2/ammo_belt.png";
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new Provider();
	}

	public static class Provider extends ItemStackHandler implements ICapabilityProvider {

		public Provider() {
			super(9);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
			return null;
		}

	}
}
