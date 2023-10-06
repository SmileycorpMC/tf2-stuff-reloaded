package rafradek.TF2weapons.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.TF2Util;

public class ItemAmmoPackage extends Item {

	public static final int[] AMMO_PACKAGE_MIN = new int[] { 1, 3, 10, 10, 3, 20, 1, 2, 2, 20, 60, 2, 200, 2, 1 };
	public static final int[] AMMO_PACKAGE_MAX = new int[] { 1, 3, 10, 10, 4, 25, 2, 3, 3, 40, 60, 3, 200, 3, 2 };

	public ItemAmmoPackage() {
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
	}

	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		par3List.add(new ItemStack(this, 1, 1));
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return stack.getMetadata() % 16 == 0 ? 64 : 1;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (!worldIn.isRemote) {
			int ammoType = itemStackIn.getMetadata() % 16;
			for (ItemStack stack : playerIn.inventory.mainInventory) {
				if (!stack.isEmpty() && stack.getItem() instanceof ItemUsable
						&& ((ItemUsable) stack.getItem()).getAmmoType(stack) != 0
						&& ((ItemUsable) stack.getItem()).getAmmoType(stack) < ItemAmmo.AMMO_TYPES.length) {
					ammoType = ((ItemUsable) stack.getItem()).getAmmoType(stack);
					// itemStackIn.setItemDamage();
					break;
				}
			}
			ItemStack out = convertPackage(itemStackIn, playerIn, ammoType);
			out = TF2Util.mergeStackByDamage(
					playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), out);
			if (!playerIn.inventory.addItemStackToInventory(out))
				playerIn.entityDropItem(out, 0);
			itemStackIn.shrink(1);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
	}

	public static ItemStack getAmmoForType(int ammoType, int amount) {
		Item item;
		switch (ammoType) {
		case 2:
			item = TF2weapons.itemAmmoMinigun;
			break;
		case 3:
			item = TF2weapons.itemAmmoPistol;
			break;
		case 5:
			item = TF2weapons.itemAmmoSMG;
			break;
		case 9:
			item = TF2weapons.itemAmmoSyringe;
			break;
		case 10:
			item = TF2weapons.itemAmmoFire;
			break;
		case 12:
			item = TF2weapons.itemAmmoMedigun;
			break;
		default:
			item = TF2weapons.itemAmmo;
		}
		ItemStack ammo = new ItemStack(item);
		if (item instanceof ItemFireAmmo) {
			if (amount > ammo.getMaxDamage() + 1) {
				ammo.setCount(Math.round((float) amount / (ammo.getMaxDamage() + 1)));
			} else
				ammo.setItemDamage(ammo.getMaxDamage() + 1 - amount);
		} else {
			ammo.setItemDamage(ammoType);
			ammo.setCount(amount);
		}
		return ammo;
	}

	public static ItemStack convertPackage(ItemStack stack, EntityPlayer player, int ammoType) {

		if (ammoType == 0)
			ammoType = player.getRNG().nextInt(13) + 1;

		int amount = AMMO_PACKAGE_MIN[ammoType] + player.getRNG().nextInt(AMMO_PACKAGE_MAX[ammoType] + 1);
		amount *= stack.getMetadata() / 16 + 1;
		ItemStack ammo = getAmmoForType(ammoType, amount);
		WeaponsCapability.get(player)
				.setMetal(WeaponsCapability.get(player).getMetal() + 40 * (stack.getMetadata() / 16 + 1));
		return ammo;
		/*
		 * if(stack.getMetadata()==10){ stack=new ItemStack(TF2weapons.itemAmmoFire,1);
		 * stack.setItemDamage((int)(stack.getMaxDamage()*(0.3f+player.getRNG().
		 * nextFloat()*0.3f))); return stack; } else if(stack.getMetadata()==12){
		 * stack=new
		 * ItemStack(TF2weapons.itemAmmoMedigun,1,(int)(stack.getMaxDamage()*(0.4f+
		 * player.getRNG().nextFloat()*0.4f)));
		 * stack.setItemDamage((int)(stack.getMaxDamage()*(0.3f+player.getRNG().
		 * nextFloat()*0.3f))); return stack; } else{ return new
		 * ItemStack(TF2weapons.itemAmmo,AMMO_PACKAGE_MIN[stack.getMetadata()]+player.
		 * getRNG().nextInt(AMMO_PACKAGE_MAX[stack.getMetadata()]+1),stack.getMetadata()
		 * ); }
		 */
	}
}
