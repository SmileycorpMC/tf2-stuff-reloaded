package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.PropertyType;

public class ItemBonk extends ItemFromData {
	public ItemBonk() {
		super();
		this.setMaxStackSize(64);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean showDurabilityBar(ItemStack stack) {
		Integer value = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(stack).getName());
		return value != null && value > 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getDurabilityForDisplay(ItemStack stack) {
		Integer value = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(stack).getName());
		return (double) (value != null ? value : 0) / (double) ((int) (getData(stack).getInt(PropertyType.COOLDOWN)
				* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemStackIn = player.getHeldItem(hand);
		Integer value = player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(itemStackIn).getName());
		if (value == null || value <= 0) {
			player.setActiveHand(hand);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
		}
		return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {

		entityLiving.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool.put(getData(stack).getName(),
				(int) (getData(stack).getInt(PropertyType.COOLDOWN)
						* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
		entityLiving.addPotionEffect(new PotionEffect(
				Potion.getPotionFromResourceLocation(getData(stack).getString(PropertyType.EFFECT_TYPE)),
				TF2ConfigVars.longDurationBanner ? ItemFromData.getData(stack).getInt(PropertyType.DURATION) : 160));
		if (!TF2ConfigVars.freeUseItems
				&& !(entityLiving instanceof EntityPlayer && ((EntityPlayer) entityLiving).capabilities.isCreativeMode))
			stack.shrink(1);
		return stack;
	}
}
