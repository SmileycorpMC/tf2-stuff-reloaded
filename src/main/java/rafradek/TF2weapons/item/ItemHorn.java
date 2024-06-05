package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

public class ItemHorn extends Item implements IBackpackItem {

	public ItemHorn() {
		this.setMaxStackSize(1);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		this.checkItem(par1ItemStack, par2World, par3Entity, par4, par5);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft) {
		ItemStack backpack = ItemBackpack.getBackpack(entityLiving);
		if (backpack.getItem() instanceof ItemSoldierBackpack
				&& this.getMaxItemUseDuration(stack) - timeLeft >= ItemFromData.getData(backpack)
						.getInt(PropertyType.FIRE_SPEED)
				&& WeaponsCapability.get(entityLiving).getRage(RageType.BANNER) >= 1f)
			((ItemSoldierBackpack) backpack.getItem()).setActive(entityLiving, backpack);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemStackIn = player.getHeldItem(hand);
		ItemStack backpack = ItemBackpack.getBackpack(player);
		if (ItemToken.allowUse(player, TF2Class.SOLDIER) && backpack.getItem() instanceof ItemSoldierBackpack
				&& (WeaponsCapability.get(player).getRage(RageType.BANNER) >= 1f)) {
			player.setActiveHand(hand);
			if (TF2Util.getTeamForDisplay(player) == 1)
				player.playSound(ItemFromData.getSound(backpack, PropertyType.HORN_BLU_SOUND), 0.8f, 1f);
			else
				player.playSound(ItemFromData.getSound(backpack, PropertyType.HORN_RED_SOUND), 0.8f, 1f);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
		}
		return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean showDurabilityBar(ItemStack stack) {
		if (!(ItemBackpack.getBackpack(Minecraft.getMinecraft().player).getItem() instanceof ItemSoldierBackpack))
			return false;
		return WeaponsCapability.get(Minecraft.getMinecraft().player).getRage(RageType.BANNER) != 1f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getDurabilityForDisplay(ItemStack stack) {
		if (!(ItemBackpack.getBackpack(Minecraft.getMinecraft().player).getItem() instanceof ItemSoldierBackpack))
			return 0;
		return 1 - WeaponsCapability.get(Minecraft.getMinecraft().player).getRage(RageType.BANNER);
	}
}
