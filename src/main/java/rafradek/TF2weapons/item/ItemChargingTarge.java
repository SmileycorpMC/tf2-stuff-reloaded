package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;

public class ItemChargingTarge extends ItemFromData {

	public ItemChargingTarge() {
		super();
		this.setMaxDamage(600);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer living, EnumHand hand) {
		// if (!living.getCapability(TF2weapons.WEAPONS_CAP,
		// null).effectsCool.containsKey("Charging")) {
		ItemStack stack = living.getHeldItem(hand);
		if (ItemToken.allowUse(living, TF2Class.DEMOMAN)) {

			if (!world.isRemote)
				living.addPotionEffect(new PotionEffect(TF2weapons.charging,
						(int) TF2Attribute.getModifier("Effect Duration", stack, 40, living),
						(int) TF2Attribute.getModifier("Charge Step", stack, 0, living)));
			living.getCooldownTracker().setCooldown(this,
					(int) (280f / TF2Attribute.getModifier("Charge", stack, 1, living)));
		}
		// living.getCapability(TF2weapons.WEAPONS_CAP,
		// null).effectsCool.put("Charging", 280);
		// }
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	public static ItemStack getChargingShield(EntityLivingBase living) {
		if (living.getHeldItemMainhand() != null && living.getHeldItemMainhand().getItem() instanceof ItemChargingTarge)
			return living.getHeldItemMainhand();
		else if (living.getHeldItemOffhand() != null
				&& living.getHeldItemOffhand().getItem() instanceof ItemChargingTarge)
			return living.getHeldItemOffhand();
		else
			return ItemStack.EMPTY;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean showDurabilityBar(ItemStack stack) {

		return super.showDurabilityBar(stack)
				|| Minecraft.getMinecraft().player.getActivePotionEffect(TF2weapons.charging) != null;
		/*
		 * Integer value =
		 * Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP,
		 * null).effectsCool .get("Charging"); return value != null && value > 0;
		 */
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getDurabilityForDisplay(ItemStack stack) {
		if (Minecraft.getMinecraft().player.getActivePotionEffect(TF2weapons.charging) != null)
			return 1 - ((double) Minecraft.getMinecraft().player.getActivePotionEffect(TF2weapons.charging)
					.getDuration() / (double) 40);
		return super.getDurabilityForDisplay(stack);
		/*
		 * Integer value =
		 * Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP,
		 * null).effectsCool .get("Charging"); return (double) (value != null ? value :
		 * 0) / (double) 280;
		 */
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (TF2Util.isOre("plankWood", repair))
			return true;
		return super.getIsRepairable(toRepair, repair);
	}

	@Override
	public boolean isShield(ItemStack stack, @Nullable EntityLivingBase entity) {
		return true;
	}
}
