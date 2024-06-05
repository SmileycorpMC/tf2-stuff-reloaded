package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ItemKnife extends ItemMeleeWeapon {

	public ItemKnife() {
		super();
		this.addPropertyOverride(new ResourceLocation("backstab"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
				if (entityIn == Minecraft.getMinecraft().player && Minecraft.getMinecraft().objectMouseOver != null
						&& Minecraft.getMinecraft().objectMouseOver.entityHit != null
						&& TF2Util.getDistanceSqBox(Minecraft.getMinecraft().objectMouseOver.entityHit, entityIn.posX,
								entityIn.posY, entityIn.posZ, entityIn.width,
								entityIn.height) <= getMaxRange(stack) * getMaxRange(stack)
						&& isBackstab(entityIn, Minecraft.getMinecraft().objectMouseOver.entityHit, stack))
					return 1;
				return 0;
			}
		});
	}

	@Override
	public void handleShoot(EntityLivingBase living, ItemStack stack, World world, HashMap<Entity, float[]> map,
			int critical, int flags) {
		for (Entity target : map.keySet()) {
			if (this.isBackstab(living, target, stack)) {
				flags += TF2DamageSource.BACKSTAB;
				break;
			}
		}
		super.handleShoot(living, stack, world, map, critical, flags);
	}

	public boolean isBackstab(EntityLivingBase living, Entity target, ItemStack stack) {
		if (target != null && target instanceof EntityLivingBase
				&& !(target instanceof IEntityTF2 && !((IEntityTF2) target).isBackStabbable(living, stack))) {
			float ourAngle = 180 + MathHelper.wrapDegrees(living.rotationYawHead);
			float angle2 = (float) (MathHelper.atan2(living.posX - target.posX, living.posZ - target.posZ) * 180.0D
					/ Math.PI);
			// System.out.println(angle2);
			if (angle2 >= 0)
				angle2 = 180 - angle2;
			else
				angle2 = -180 - angle2;
			angle2 += 180;
			float enemyAngle = 180 + MathHelper.wrapDegrees(target.getRotationYawHead());
			float difference = 180 - Math.abs(Math.abs(ourAngle - enemyAngle) - 180);
			float difference2 = 180 - Math.abs(Math.abs(angle2 - enemyAngle) - 180);
			// System.out.println(angle2+" "+difference2+" "+difference);
			if (difference < 90 && difference2 < 90)
				return true;
		}
		return false;
	}

	public float getBackstabBonusDamage(ItemStack stack, EntityLivingBase living, Entity target) {
		float base = 4f;
		if (living instanceof EntityPlayer && ((EntityPlayer) living).getCooldownTracker().hasCooldown(this)) {
			base -= (base - 1) * (((EntityPlayer) living).getCooldownTracker().getCooldown(this, 0));
		}
		base *= Math.pow(TF2Attribute.getModifier("Backstab Damage", stack, 1, living), 2.0f);
		if (target instanceof IEntityTF2) {
			base = ((IEntityTF2) target).getBackstabDamageReduction(living, stack, base);
		}
		if (target.getEntityData().hasKey(NBTLiterals.BACKSTAB_MULT))
			base *= target.getEntityData().getFloat(NBTLiterals.BACKSTAB_MULT);
		return Math.max(1f, base);
	}

	@Override
	public float getWeaponDamage(ItemStack stack, EntityLivingBase living, Entity target) {
		return super.getWeaponDamage(stack, living, target)
				* (this.isBackstab(living, target, stack) ? this.getBackstabBonusDamage(stack, living, target) : 1);
	}

	@Override
	public int setCritical(ItemStack stack, EntityLivingBase shooter, Entity target, int old, DamageSource source) {
		return super.setCritical(stack, shooter, target, this.isBackstab(shooter, target, stack) ? 2 : old, source);
	}

	/*
	 * @SideOnly(Side.CLIENT) public ModelResourceLocation getModel(ItemStack stack,
	 * EntityPlayer player, int useRemaining) { if (
	 * Minecraft.getMinecraft().player!=null&&Minecraft.getMinecraft(
	 * ).player.getHeldItem(EnumHand.MAIN_HAND)==stack&& stack.getItem() instanceof
	 * ItemKnife&& /Minecraft.getMinecraft().objectMouseOver.entityHit != null &&
	 * this.isBackstab(player, Minecraft.getMinecraft().objectMouseOver.entityHit))
	 * return ClientProxy.nameToModel.get(stack.getTagCompound().getString("Type") +
	 * "/b"); return null; }
	 */
	@Override
	public void onDealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, DamageSource source,
			float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);
		if (attacker instanceof EntityPlayer && isBackstab(attacker, target, stack) && target.isEntityAlive()
				&& !(target instanceof EntityTF2Character && ((EntityTF2Character) target).isGiant())) {
			if (target instanceof EntityLiving) {
				if (target.getEntityData().hasKey(NBTLiterals.BACKSTAB_MULT)) {
					target.getEntityData().setFloat(NBTLiterals.BACKSTAB_MULT,
							Math.max(0.5f, target.getEntityData().getFloat(NBTLiterals.BACKSTAB_MULT) * 0.9f));
				} else
					target.getEntityData().setFloat(NBTLiterals.BACKSTAB_MULT, 0.9f);
			}
			((EntityPlayer) attacker).getCooldownTracker().setCooldown(this, this.getFiringSpeed(stack, attacker) / 12);
		}
		boolean isBackstab = isBackstab(attacker, target, stack);

		if (attacker instanceof EntityPlayerMP && isBackstab && target instanceof EntityLivingBase
				&& !target.isEntityAlive() && TF2Util.isEnemy(attacker, (EntityLivingBase) target)) {
			((EntityPlayerMP) attacker).addStat(TF2Achievements.KILLED_BACKSTAB);
			if (TF2Attribute.getModifier("Disguise Backstab", stack, 0, attacker) != 0) {

				WeaponsCapability.get(attacker).stabbedDisguise = true;
				if (!(target instanceof EntityPlayer))
					WeaponsCapability.get(attacker).setDisguiseType("M:" + EntityList.getKey(target).toString());
				else
					WeaponsCapability.get(attacker).setDisguiseType("P:" + target.getName());
				((EntityLivingBase) target).deathTime = 15;
				// TF2Util.sendTracking(new TF2Message.ActionMessage(19, (EntityLivingBase)
				// target), target);
				WeaponsCapability.get(attacker).setDisguised(true);
			}
			/*
			 * if(((EntityPlayerMP)
			 * attacker).getStatFile().readStat(TF2Achievements.KILLED_BACKSTAB)>=400)
			 * ((EntityPlayerMP) attacker).addStat(TF2Achievements.SPYMASTER);
			 * if(attacker.getCapability(TF2weapons.PLAYER_CAP, null).sapperTime>0 &&
			 * attacker.getCapability(TF2weapons.PLAYER_CAP,
			 * null).buildingOwnerKill==target) ((EntityPlayerMP)
			 * attacker).addStat(TF2Achievements.SAP_STAB);
			 */
		}
	}
}
