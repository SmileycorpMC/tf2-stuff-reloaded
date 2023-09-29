package rafradek.TF2weapons.item;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;

public class ItemFlameThrower extends ItemAirblast {

	@Override
	public boolean canAltFire(World worldObj, EntityLivingBase player, ItemStack item) {
		return super.canAltFire(worldObj, player, item) || (TF2Attribute.getModifier("Rage Crit", item, 0, player) != 0
				&& this.getRage(item, player) >= this.getMaxRage(item, player));
	}

	@Override
	public boolean canFire(World world, EntityLivingBase living, ItemStack stack) {
		return super.canFire(world, living, stack);
	}

	@Override
	public boolean startUse(ItemStack stack, EntityLivingBase living, World world, int action, int newState) {
		if (world.isRemote && (newState & 1) - (action & 1) == 1 && this.canFire(world, living, stack)) {
			SoundEvent playSound = ItemFromData.getSound(stack, PropertyType.FIRE_START_SOUND);
			ClientProxy.playWeaponSound(living, playSound, false, 2, stack);
		}
		return false;
	}

	@Override
	public boolean endUse(ItemStack stack, EntityLivingBase living, World world, int action, int newState) {
		if ((action & 1) == 1) {
			if (world.isRemote)
				// System.out.println("called"+ClientProxy.fireSounds.get(living));
				if (ClientProxy.fireSounds.get(living) != null)
					// System.out.println("called2"+ClientProxy.fireSounds.get(living).type);
					ClientProxy.fireSounds.get(living).setDone();
			// Minecraft.getMinecraft().getSoundHandler().stopSound(ClientProxy.fireSounds.get(living));
			living.playSound(ItemFromData.getSound(stack, PropertyType.FIRE_STOP_SOUND), 1f, 1f);
		}
		return false;
	}

	@Override
	public boolean fireTick(ItemStack stack, EntityLivingBase living, World world) {
		if (world.isRemote && living.getCapability(TF2weapons.WEAPONS_CAP, null).getPrimaryCooldown() <= 50
				&& this.canFire(world, living, stack)) {
			if (living.getCapability(TF2weapons.WEAPONS_CAP, null).startedPress()) {
				SoundEvent playSound = ItemFromData.getSound(stack, PropertyType.FIRE_START_SOUND);
				ClientProxy.playWeaponSound(living, playSound, false, 2, stack);
			}
			if (living.isInsideOfMaterial(Material.WATER)) {
				Vec3d look = living.getLookVec();
				Vec3d tangent = new Vec3d(-look.z, 0, look.x);
				Vec3d pos = living.getPositionVector().addVector(tangent.x*0.3, living.getEyeHeight(), tangent.z*0.3)
						.add(look.scale(0.5));
				world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, pos.x,
						pos.y, pos.z, living.motionX, 0.2D + living.motionY,
						living.motionZ, new int[0]);
			} else {
				ClientProxy.spawnFlameParticle(world, living, 0f, false);
				ClientProxy.spawnFlameParticle(world, living, 0.5f, false);
				if (TF2Attribute.getModifier("Spawns Bubbles", stack, 0, living) > 0 && living.ticksExisted%3==0) {
					Vec3d look = living.getLookVec();
					Vec3d tangent = new Vec3d(-look.z, 0, look.x);
					Vec3d motion = new Vec3d(living.motionX, 0.02, living.motionZ);
					ClientProxy.spawnBubbleParticle(world, living.getPositionVector()
							.addVector(tangent.x*0.25, living.getEyeHeight(), tangent.z*0.2)
							.add(look.scale(0.5)), motion.add(tangent.scale(-0.02)));
					ClientProxy.spawnBubbleParticle(world, living.getPositionVector()
							.addVector(tangent.x*0.35, living.getEyeHeight(), tangent.z*0.3)
							.add(look.scale(0.5)), motion.add(tangent.scale(0.02)));
				}
			}
			if (TF2Util.calculateCritPre(stack, living) != 2 && (!ClientProxy.fireSounds.containsKey(living)
					|| !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(ClientProxy.fireSounds.get(living))
					|| (ClientProxy.fireSounds.get(living).type != 0 && ClientProxy.fireSounds.get(living).type != 2)))
				ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.FIRE_LOOP_SOUND), true, 0,
						stack);
			else if (TF2Util.calculateCritPre(stack, living) == 2 && (!ClientProxy.fireSounds.containsKey(living)
					|| !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(ClientProxy.fireSounds.get(living))
					|| (ClientProxy.fireSounds.get(living).type != 1))) {
				ResourceLocation playSoundCrit = new ResourceLocation(
						ItemFromData.getData(stack).getString(PropertyType.FIRE_LOOP_SOUND) + ".crit");

				ClientProxy.playWeaponSound(living, SoundEvent.REGISTRY.getObject(playSoundCrit), true, 1, stack);
			}
		}
		// System.out.println("nie");
		return false;
	}

	@Override
	public float getProjectileSpeed(ItemStack stack, EntityLivingBase living) {
		float speed = super.getProjectileSpeed(stack, living);
		return speed * 0.6f + TF2Attribute.getModifier("Flame Range", stack, speed * 0.4f, living);
	}

	@Override
	public RageType getRageType(ItemStack stack, EntityLivingBase living) {
		return TF2Attribute.getModifier("Rage Crit", stack, 0, null) == 1f ? RageType.PHLOG
				: super.getRageType(stack, living);
	}

	@Override
	public float getMaxRage(ItemStack stack, EntityLivingBase living) {
		return TF2Attribute.getModifier("Rage Crit", stack, 0, null) == 1f ? 20f : super.getMaxRage(stack, living);
	}

	@Override
	public void onDealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, DamageSource source,
			float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);

		if (target instanceof EntityLivingBase && TF2Attribute.getModifier("Rage Crit", stack, 0, attacker) != 0
				&& !WeaponsCapability.get(attacker).isRageActive(RageType.PHLOG)) {
			float mult = 1f;
			if (attacker instanceof EntityPlayer) {
				if (target instanceof EntityPlayer)
					mult = 1f;
				else if (TF2Util.isEnemy(attacker, (EntityLivingBase) target))
					mult = 0.4f;
				else
					mult = 0.1f;
			} else {
				if (target instanceof EntityPlayer)
					mult = 4f;
			}
			this.addRage(stack, attacker, amount * mult);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(stack, par2World, par3Entity, par4, par5);
		if (WeaponsCapability.get(par3Entity).isRageActive(RageType.PHLOG)) {
			if (par5 && par3Entity.ticksExisted % 5 == 0) {
				((EntityLivingBase) par3Entity).addPotionEffect(new PotionEffect(TF2weapons.critBoost, 5));
			}
		}

	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		// stack.getTagCompound().setFloat("Rage", 0f);
		// stack.getTagCompound().setBoolean("RageActive", true);
		return stack;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, EntityPlayer player) {
		return super.showInfoBox(stack, player) || TF2Attribute.getModifier("Rage Crit", stack, 0, player) != 0;
	}

	/*
	 * public String[] getInfoBoxLines(ItemStack stack, EntityPlayer player){
	 * if(TF2Attribute.getModifier("Rage Crit", stack, 0, player)==0) return
	 * super.getInfoBoxLines(stack, player); else { String[] result=new String[2];
	 * result[0]="MMMPH"; int focus=(int) TF2Attribute.getModifier("Focus", stack,
	 * 0, player); if(focus!=0){ result[0]=result[0]+" "; int progress=(int)
	 * (((float)player.getCapability(TF2weapons.WEAPONS_CAP,
	 * null).focusShotTicks/(float)(70-focus*23+((ItemUsable)stack.getItem()).
	 * getFiringSpeed(stack, player)/50))*3f); for(int i=0;i<progress && i<3;i++){
	 * result[0]=result[0]+"\u2588"; } }
	 * result[1]=(int)((stack.getTagCompound().getFloat("Rage")/20f)*100)+"%";
	 * return result; } }
	 */

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		/*
		 * if (TF2Attribute.getModifier("Rage Crit", itemStackIn, 0, playerIn)!=0
		 * &&playerIn.getCapability(TF2weapons.WEAPONS_CAP, null).getPhlogRage()>=20f) {
		 * playerIn.setActiveHand(hand); playerIn.addPotionEffect(new
		 * PotionEffect(TF2weapons.stun,40,1)); TF2Util.addAndSendEffect(playerIn, new
		 * PotionEffect(TF2weapons.uber,40,0)); playerIn.addPotionEffect(new
		 * PotionEffect(TF2weapons.noKnockback,40,0));
		 * playerIn.playSound(ItemFromData.getSound(itemStackIn,
		 * PropertyType.CHARGE_SOUND), 1f, 1f);
		 * itemStackIn.getTagCompound().setBoolean("RageActive", true); return new
		 * ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn); }
		 */
		return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
	}

	@Override
	public void altUse(ItemStack stack, EntityLivingBase living, World world) {
		if (TF2Attribute.getModifier("Rage Crit", stack, 0, living) != 0
				&& this.getRage(stack, living) >= this.getMaxRage(stack, living)) {
			living.setActiveHand(EnumHand.MAIN_HAND);
			living.addPotionEffect(new PotionEffect(TF2weapons.stun, 40, 1));
			TF2Util.addAndSendEffect(living, new PotionEffect(TF2weapons.uber, 40, 0));
			living.addPotionEffect(new PotionEffect(TF2weapons.noKnockback, 40, 0));
			living.playSound(ItemFromData.getSound(stack, PropertyType.CHARGE_SOUND), 1f, 1f);
			WeaponsCapability.get(living).setRageActive(RageType.PHLOG, true, 2f);
		} else
			super.altUse(stack, living, world);
	}
}
