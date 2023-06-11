package rafradek.TF2weapons.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;

import javax.annotation.Nullable;
import java.util.UUID;

public class ItemMinigun extends ItemBulletWeapon {

	public static UUID slowdownUUID = UUID.fromString("12843092-A5D6-BBCD-3D4F-A3DD4D8C94C8");
	public static AttributeModifier slowdown = new AttributeModifier(slowdownUUID, "minigun slowdown", -0.5D, 2);

	public ItemMinigun() {
		super();
		this.addPropertyOverride(new ResourceLocation("spin"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				if (entityIn != null && entityIn.hasCapability(TF2weapons.WEAPONS_CAP, null)
						&& entityIn.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks > 0)
					return 1;
				return 0;
			}
		});
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);

		if (par5 && par1ItemStack.getTagCompound() != null) {
			// System.out.println("EntityTicked" + cap.state+ par3Entity);
			if ((cap.state == 0 || cap.state == 4) && cap.chargeTicks > 0) {
				// System.out.println("Draining" + cap.chargeTicks);
				cap.killsSpinning = 0;
				cap.chargeTicks -= 2;
				((EntityLivingBase) par3Entity).getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
						.removeModifier(slowdown);
			}
			if ((cap.state & 4) == 4
					&& this.getRageType(par1ItemStack, (EntityLivingBase) par3Entity) == RageType.KNOCKBACK
					&& !cap.isRageActive(RageType.KNOCKBACK) && cap.getRage(RageType.KNOCKBACK) >= 1f) {
				cap.setRageActive(RageType.KNOCKBACK, true,
						0.15f - 0.03f * TF2Attribute.getModifier("Knockback Rage", par1ItemStack, 0, null));
			}
		}
	}

	/*
	 * public void addInformation(ItemStack par1ItemStack, EntityPlayer
	 * par2EntityPlayer, List par2List, boolean par4) {
	 * super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4); if
	 * (par1ItemStack.hasTagCompound()) {
	 * par2List.add("minigun: "+Integer.toString(par1ItemStack.getTagCompound().
	 * getShort("minigunticks"))); } }
	 */
	@Override
	public float getWeaponDamage(ItemStack stack, EntityLivingBase living, Entity target) {
		float base = super.getWeaponDamage(stack, living, target);
		if (living != null && WeaponsCapability.get(living).isRageActive(RageType.KNOCKBACK))
			base *= 0.6f;
		return base * (living != null
				? TF2Util.lerp(0.5f, 1f, living.getCapability(TF2weapons.WEAPONS_CAP, null).minigunTicks / 20f)
				: 1f);
	}

	@Override
	public float getWeaponSpreadBase(ItemStack stack, EntityLivingBase living) {
		float base = super.getWeaponSpreadBase(stack, living);

		if (TF2Attribute.getModifier("Weapon Mode", stack, 0, living) != 1f) {
			return base * (living != null
					? TF2Util.lerp(1.5f, 1f, living.getCapability(TF2weapons.WEAPONS_CAP, null).minigunTicks / 20f)
					: 1f);
		} else {
			return base * TF2Util.lerp(3f, 0.5f,
					Math.min(1f, (this.getClip(stack) - this.getWeaponClipSize(stack, living) * 0.2f)
							/ (this.getWeaponClipSize(stack, living) * 0.8f)));
		}
	}

	@Override
	public int getFiringSpeed(ItemStack stack, EntityLivingBase living) {
		if (TF2Attribute.getModifier("Weapon Mode", stack, 0, living) != 1f) {
			return super.getFiringSpeed(stack, living);
		} else {
			float mult = Math.max(0f, (this.getClip(stack) - this.getWeaponClipSize(stack, living) * 0f)
					/ (this.getWeaponClipSize(stack, living) * 1f));
			return (int) (super.getFiringSpeed(stack, living) * TF2Util.lerp(0.8f, 3f, mult * mult * mult));
		}
	}

	@Override
	public boolean startUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
		if (world.isRemote && oldState == 0
				&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type != 3))
			// ResourceLocation playSound=new
			// ResourceLocation(getData(stack).get("Wind Up
			// Sound").getString());
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.WIND_UP_SOUND), false, 3,
					stack);
		return false;
	}

	@Override
	public boolean endUse(ItemStack stack, EntityLivingBase living, World world, int action, int newState) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (newState == 0)
			cap.minigunTicks = 0;
		if (world.isRemote && newState == 0 && cap.chargeTicks > 0
				&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type != 4))
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.WIND_DOWN_SOUND), false, 4,
					stack);
		return false;
	}

	@Override
	public boolean fireTick(ItemStack stack, EntityLivingBase living, World world) {
		if (world.isRemote && this.canFire(world, living, stack)) {
			if (TF2Util.calculateCritPre(stack, living) != 2
					&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type != 0))
				ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.FIRE_LOOP_SOUND), true, 0,
						stack);
			else if (TF2Util.calculateCritPre(stack, living) == 2
					&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type != 1)) {
				ResourceLocation playSoundCrit = new ResourceLocation(
						ItemFromData.getData(stack).getString(PropertyType.FIRE_LOOP_SOUND) + ".crit");

				ClientProxy.playWeaponSound(living, SoundEvent.REGISTRY.getObject(playSoundCrit), true, 1, stack);
			}
		}
		// System.out.println("nie");
		this.spinMinigun(stack, living, world);
		return false;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public boolean altFireTick(ItemStack stack, EntityLivingBase living, World world) {
		if (world.isRemote && this.canFire(world, living, stack)
				&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type > 2))
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.SPIN_SOUND), true, 2, stack);
		/*
		 * System.out.println("start"); ResourceLocation playSound=new
		 * ResourceLocation(getData(stack).get("Spin Sound").getString());
		 * MinigunLoopSound sound=new MinigunLoopSound(playSound, living, false,
		 * getData(stack),false);
		 * Minecraft.getMinecraft().getSoundHandler().playSound(sound);
		 * MapList.spinSounds.put(sound, living); }
		 */
		if ((living.getCapability(TF2weapons.WEAPONS_CAP, null).state & 1) != 1)
			this.spinMinigun(stack, living, world);
		return false;
	}

	public void spinMinigun(ItemStack stack, final EntityLivingBase living, World world) {
		if (super.canFire(world, living, stack)) {
			WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);

			float ammo = TF2Attribute.getModifier("Ammo Spinned", stack, 0, living);
			int spinuptime = (int) TF2Attribute.getModifier("Minigun Spinup", stack, 18, living);
			if (cap.chargeTicks >= spinuptime
					|| (living instanceof EntityPlayer && ((EntityPlayer) living).isCreative())) {
				if (cap.minigunTicks < 20)
					cap.minigunTicks += 1;
				if (ammo > 0 && !this.searchForAmmo(living, stack).isEmpty()) {
					if ((living.ticksExisted % (20 / ammo)) == 0) {
						this.consumeAmmoGlobal(living, stack, 1);
					}
				}

				if (living.ticksExisted % 10 == 0) {

					float flamedmg = TF2Attribute.getModifier("Ring Fire", stack, 0, living);
					if (flamedmg > 0) {
						if (world.isRemote) {
							for (int i = 0; i < 50; i++)
								ClientProxy.spawnFlameParticle(world, living, 0, true);
						} else {
							for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class,
									living.getEntityBoundingBox().grow(4, -0.5, 4).offset(0, -0.5, 0),
									input -> input != living && TF2Util.canHit(living, input)
											&& input.getDistanceSq(living) < 16)) {

								TF2Util.dealDamage(target, world, living, stack, 0, flamedmg,
										TF2Util.causeDirectDamage(stack, living).setFireDamage());
								TF2Util.igniteAndAchievement(target, living, 6, 1);
							}
						}
					}

				}
			}
			if (living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(slowdownUUID) == null)
				living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(slowdown);
			if (living.isSprinting()) {
				living.motionX *= 0.6D;
				living.motionZ *= 0.6D;
				living.setSprinting(false);
			}
			if (world.isRemote)
				ClientProxy.removeSprint();

			if (WeaponData.getCapability(stack).fire1Cool <= 0 && cap.chargeTicks < spinuptime)
				cap.chargeTicks += 1;
		}
	}

	@Override
	public boolean canFire(World world, EntityLivingBase living, ItemStack stack) {
		return super.canFire(world, living, stack) && ((living.getCapability(TF2weapons.WEAPONS_CAP,
				null).chargeTicks >= (int) TF2Attribute.getModifier("Minigun Spinup", stack, 18, living))
				|| (living instanceof EntityPlayer && ((EntityPlayer) living).isCreative()));
	}

	@Override
	public void holster(WeaponsCapability cap, ItemStack stack, EntityLivingBase living, World world) {
		living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);
		super.holster(cap, stack, living, world);
	}

	@Override
	public void onDealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, DamageSource source,
			float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);
		if (attacker instanceof EntityPlayer && !target.isEntityAlive() && target instanceof EntityLivingBase
				&& TF2Util.isEnemy(attacker, (EntityLivingBase) target)) {
			attacker.getCapability(TF2weapons.WEAPONS_CAP, null).killsSpinning++;
			/*
			 * if(attacker.getCapability(TF2weapons.WEAPONS_CAP, null).killsSpinning>=8)
			 * ((EntityPlayer)attacker).addStat(TF2Achievements.REVOLUTION);
			 */
		}
	}

	@Override
	public int getWeaponReloadTime(ItemStack stack, EntityLivingBase living) {
		if (WeaponsCapability
				.get(living).chargeTicks < (int) TF2Attribute.getModifier("Minigun Spinup", stack, 18, living) - 10)
			return super.getWeaponReloadTime(stack, living) / 2;
		else
			return super.getWeaponReloadTime(stack, living);
	}

	static {
		slowdown.setSaved(false);
	}
}
