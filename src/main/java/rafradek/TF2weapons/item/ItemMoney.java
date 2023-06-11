package rafradek.TF2weapons.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.inventory.InventoryWearables;

import java.util.List;

public class ItemMoney extends Item {

	public ItemMoney() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2money");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + stack.getMetadata();
	}

	public int getValue(ItemStack stack) {
		switch (stack.getMetadata()) {
		case 0:
			return 1 * stack.getCount();
		case 1:
			return 9 * stack.getCount();
		case 2:
			return 81 * stack.getCount();
		}
		return stack.getCount();
	}

	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 3; i++)
			par3List.add(new ItemStack(this, 1, i));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(I18n.format("item.tf2money.desc", getValue(stack)));
	}

	public static void collect(ItemStack stack, EntityPlayer player) {
		int type = stack.getMetadata();
		InventoryWearables inv = player.getCapability(TF2weapons.INVENTORY_CAP, null);
		int total;
		if (type == 0) {
			total = stack.getCount() + inv.getStackInSlot(type + 5).getCount();
			if (total > stack.getMaxStackSize()) {
				int subtract = MathHelper.ceil((total - stack.getMaxStackSize()) / 9f);
				total -= subtract * 9;
				type = 1;
				stack.setCount(subtract);
				stack.setItemDamage(1);
			} else {
				inv.setInventorySlotContents(5, stack.copy());
				stack.setCount(0);
			}
			inv.getStackInSlot(5).setCount(total);
		}
		if (type == 1) {
			total = stack.getCount() + inv.getStackInSlot(type + 5).getCount();
			if (total > stack.getMaxStackSize()) {
				int subtract = MathHelper.ceil((total - stack.getMaxStackSize()) / 9f);
				total -= subtract * 9;
				type = 2;
				stack.setCount(subtract);
				stack.setItemDamage(2);
			} else {
				inv.setInventorySlotContents(6, stack.copy());
				stack.setCount(0);
			}
			inv.getStackInSlot(6).setCount(total);
		}
		if (type == 2) {
			total = stack.getCount() + inv.getStackInSlot(type + 5).getCount();
			if (total > stack.getMaxStackSize()) {
				stack.setCount(total - stack.getMaxStackSize());
				inv.getStackInSlot(type + 5).setCount(stack.getMaxStackSize());
			} else {
				inv.setInventorySlotContents(type + 5, stack.copy());
				inv.getStackInSlot(type + 5).setCount(total);
				stack.setCount(0);
			}

		}
	}
}
