package rafradek.TF2weapons.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

public class ItemJetpack extends ItemBackpack {

	public static ItemStack trigger = ItemStack.EMPTY;

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return stack.getTagCompound().getShort("Charge") > 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) (stack.getTagCompound().getShort("Charge")) / this.getCooldown(stack, null);
	}

	@Override
	public ItemStack getBackpackItemToUse(ItemStack stack, EntityLivingBase player) {
		if (trigger.isEmpty())
			trigger = ItemFromData.getNewStack("trigger");
		return trigger;
	}

	@Override
	public void onArmorTickAny(World world, EntityLivingBase player, ItemStack itemStack) {
		super.onArmorTickAny(world, player, itemStack);
		if (!world.isRemote) {
			if (player.ticksExisted % 4 == 0 && itemStack.getTagCompound().getShort("Charge") > 0
					&& this.getAmmoAmount(player, itemStack) >= this.getActualAmmoUse(itemStack, player, 1)) {
				itemStack.getTagCompound().setShort("Charge",
						(short) (itemStack.getTagCompound().getInteger("Charge") - 4));
				if (player.ticksExisted % (4 * Math.round(this.getCooldown(itemStack, player) / 75f)) == 0)
					this.consumeAmmoGlobal(player, itemStack, this.getActualAmmoUse(itemStack, player, 1));
				if (itemStack.getTagCompound().getShort("Charge") <= 0) {
					itemStack.getTagCompound().setShort("Charges",
							(short) (itemStack.getTagCompound().getShort("Charges") + 1));
				}
			}
			if (itemStack.getTagCompound().getBoolean("Active")) {
				if (player.onGround) {
					TF2Util.playSound(player, getSound(itemStack, PropertyType.FIRE_STOP_SOUND), 1f, 1f);
					itemStack.getTagCompound().setBoolean("Active", false);
				}
				TF2Util.stomp(player);
			}
			if (itemStack.getTagCompound().getByte("Load") > 0) {
				itemStack.getTagCompound().setByte("Load", (byte) (itemStack.getTagCompound().getByte("Load") - 1));
				if (itemStack.getTagCompound().getByte("Load") == 0) {
					itemStack.getTagCompound().setByte("Charges",
							(byte) (itemStack.getTagCompound().getByte("Charges") - 1));
					Vec3d vel = player.getLook(1);
					vel = vel.scale(TF2Attribute.getModifier("Self Push Force", itemStack, 1, player));
					WeaponsCapability.get(player).setExpJump(true);
					player.motionX = vel.x;
					player.motionY = vel.y + (player instanceof EntityPlayer ? 0.1 : 0.25);
					player.motionZ = vel.z;
					TF2Util.playSound(player, getSound(itemStack, PropertyType.FIRE_SOUND), 1f, 1f);
					itemStack.getTagCompound().setBoolean("Active", true);
					if (player instanceof EntityPlayerMP)
						((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityVelocity(player));
				}
			}
			if (itemStack.getTagCompound().getShort("Charge") == 0
					&& itemStack.getTagCompound().getByte("Charges") < this.getMaxCharges(itemStack, player)) {
				itemStack.getTagCompound().setShort("Charge", (short) this.getCooldown(itemStack, player));
			}
		} else if (itemStack.getTagCompound().getBoolean("Active") || itemStack.getTagCompound().getByte("Load") > 0) {
			Vec3d vec = TF2Util.getRotationVector(0, player.renderYawOffset);
			Vec3d vec2 = TF2Util.getRotationVector(0, player.renderYawOffset + 90);
			world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, player.posX - vec.x * 0.4 + vec2.x * 0.3,
					player.posY + 1, player.posZ - vec.z * 0.4 + vec2.z * 0.3, 0, 0, 0);
			world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, player.posX - vec.x * 0.4 - vec2.x * 0.3,
					player.posY + 1, player.posZ - vec.z * 0.4 - vec2.z * 0.3, 0, 0, 0);
		}
	}

	public int getCooldown(ItemStack stack, EntityLivingBase living) {
		return (int) (getData(stack).getInt(PropertyType.COOLDOWN)
				* (TF2ConfigVars.fastItemCooldown ? 1 : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))
				/ (TF2Attribute.getModifier("Charge", stack, 1, living)
						+ TF2Attribute.getModifier("Charges", stack, 0, living) * 0.12f));
	}

	@Override
	public int getAmmoType(ItemStack stack) {
		return TF2ConfigVars.freeUseItems ? 0 : super.getAmmoType(stack);
	}

	public boolean canActivate(ItemStack stack, EntityLivingBase player) {
		return ItemToken.allowUse(player, TF2Class.PYRO)
				&& (!stack.getTagCompound().getBoolean("Active")
						|| TF2Attribute.getModifier("Jetpack", stack, 0f, player) > 0f)
				&& stack.getTagCompound().getByte("Load") <= 0 && stack.getTagCompound().getByte("Charges") > 0;
	}

	public int getMaxCharges(ItemStack stack, EntityLivingBase player) {
		return (int) TF2Attribute.getModifier("Charges", stack, 2, player);
	}

	public void activateJetpack(ItemStack stack, EntityLivingBase player, boolean setTimer) {
		player.motionY = Math.max(player.motionY, 0) + 0.5;
		player.isAirBorne = true;
		player.fallDistance = 0;
		if (!player.world.isRemote) {
			TF2Util.playSound(player, getSound(stack, PropertyType.CHARGE_SOUND), 1f, 1f);
			WeaponsCapability.get(player).setExpJump(true);
			stack.getTagCompound().setByte("Load", (byte) 12);
			if (player instanceof EntityPlayerMP)
				TF2weapons.network.sendTo(new TF2Message.ActionMessage(30, player), (EntityPlayerMP) player);
		} else {
			for (int i = 0; i < 50; i++)
				ClientProxy.spawnFlameParticle(player.world, player, 0, true);
		}
		if (setTimer && !(player.getHeldItemMainhand().getItem() instanceof ItemJetpackTrigger)) {
			WeaponsCapability.get(player).setPrimaryCooldown(1500);
			WeaponsCapability.get(player).setSecondaryCooldown(1500);
		}
	}
}
