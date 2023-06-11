package rafradek.TF2weapons.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.projectile.EntityStickybomb;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;

import java.util.ArrayList;

public class ItemStickyLauncher extends ItemProjectileWeapon {

	// public static HashMap<EntityLivingBase, ArrayList<EntityStickybomb>>
	// activeBombs = new HashMap<EntityLivingBase, ArrayList<EntityStickybomb>>();

	/*
	 * @Override public boolean use(ItemStack stack, EntityLivingBase living, World
	 * world, EnumHand hand, PredictionMessage message) { WeaponsCapability cap =
	 * living.getCapability(TF2weapons.WEAPONS_CAP, null); if (cap.charging) //
	 * System.out.println("Firing"); super.use(stack, living, world, hand, message);
	 * else { // System.out.println("Start charging"); cap.charging = true;
	 * cap.chargeTicks = 0; if (world.isRemote) ClientProxy.playWeaponSound(living,
	 * ItemFromData.getSound(stack, PropertyType.CHARGE_SOUND), false, 0, stack); }
	 * return true; }
	 */

	public boolean usePrediction() {
		return false;
	}

	/*
	 * @Override public boolean canFire(World world, EntityLivingBase living,
	 * ItemStack stack) { return !living.getCapability(TF2weapons.WEAPONS_CAP,
	 * null).charging && super.canFire(world, living, stack); }
	 */

	@Override
	public float getProjectileSpeed(ItemStack stack, EntityLivingBase living) {
		return super.getProjectileSpeed(stack, living)
				* (1 + living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks * 0.02f);
	}

	@Override
	public void shoot(ItemStack stack, EntityLivingBase living, World world, int thisCritical, EnumHand hand) {
		if (!world.isRemote) {
			EntityStickybomb bomb = new EntityStickybomb(world);
			bomb.initProjectile(living, hand, stack);
			bomb.setCritical(thisCritical);
			world.spawnEntity(bomb);
			ArrayList<EntityStickybomb> list = living.getCapability(TF2weapons.WEAPONS_CAP, null).activeBomb;
			list.add(bomb);
			if (list.size() > TF2Attribute.getModifier("Stickybomb Count", stack, 8, living)) {
				EntityStickybomb firstBomb = list.get(0);
				firstBomb.explode(firstBomb.posX, firstBomb.posY, firstBomb.posZ, null, 1);
			}
		}
	}

	/*
	 * @Override public boolean endUse(ItemStack stack, EntityLivingBase living,
	 * World world, int action, int newState) { WeaponsCapability cap =
	 * living.getCapability(TF2weapons.WEAPONS_CAP, null); if ((newState & 1) == 0
	 * && cap.charging) { // System.out.println("stop charging "+newState);
	 * 
	 * cap.fire1Cool = this.getFiringSpeed(stack, living);
	 * 
	 * if (world.isRemote && ClientProxy.fireSounds.get(living) != null)
	 * ClientProxy.fireSounds.get(living).setDone(); //
	 * Minecraft.getMinecraft().getSoundHandler().stopSound(ClientProxy.fireSounds.
	 * get(living)); super.use(stack, living, world, EnumHand.MAIN_HAND, null);
	 * cap.charging = false; cap.lastFire = 1250; if (world.isRemote) sps++;
	 * cap.reloadCool = 0; if ((cap.state & 8) != 0) cap.state -= 8; } return false;
	 * }
	 */

	/*
	 * @Override public boolean fireTick(ItemStack stack, EntityLivingBase living,
	 * World world) { WeaponsCapability cap =
	 * living.getCapability(TF2weapons.WEAPONS_CAP, null); if (cap.charging) //
	 * System.out.println("charging "+tag.getShort("chargeticks")); if
	 * (cap.chargeTicks < 80) cap.chargeTicks += living instanceof EntityPlayer?1:4;
	 * else this.endUse(stack, living, world, 1, 0); return false; }
	 */

	@Override
	public boolean altFireTick(ItemStack stack, EntityLivingBase living, World world) {
		if (!world.isRemote) {
			ArrayList<EntityStickybomb> list = living.getCapability(TF2weapons.WEAPONS_CAP, null).activeBomb;
			if (list == null || list.isEmpty())
				return false;
			boolean exploded = false;

			TF2PlayerCapability cap = living.getCapability(TF2weapons.PLAYER_CAP, null);
			if (cap != null) {
				cap.engineerKilled = false;
				cap.dispenserKilled = false;
				cap.sentryKilled = false;
				cap.stickybombKilled = 0;
			}
			for (int i = 0; i < list.size(); i++) {
				EntityStickybomb bomb = list.get(i);
				if (bomb.ticksExisted > bomb.getArmTime() && (bomb.getType() != 1
						|| living.getEntityBoundingBox().grow(1.5, 0.25, 1.5).offset(0, -1.25, 0)
								.contains(bomb.getPositionVector())
						|| TF2Util.lookingAt(living, 30, bomb.posX, bomb.posY, bomb.posZ))) {
					bomb.explode(bomb.posX, bomb.posY + bomb.height / 2, bomb.posZ, null, 1);
					i--;
					exploded = true;
				}
			}
			if (exploded) {
				living.playSound(ItemFromData.getSound(stack, PropertyType.DETONATE_SOUND), 1f, 1);
				/*
				 * if(living instanceof EntityPlayer && cap.stickybombKilled>=3)
				 * ((EntityPlayer)living).addStat(TF2Achievements.PIPEBAGGER); if(living
				 * instanceof EntityPlayer && cap.engineerKilled && cap.dispenserKilled &&
				 * cap.sentryKilled) ((EntityPlayer)living).addStat(TF2Achievements.ARGYLE_SAP);
				 */
			}
		}
		return false;
	}

	@Override
	public void onDealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, DamageSource source,
			float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);
		TF2PlayerCapability cap = attacker.getCapability(TF2weapons.PLAYER_CAP, null);
		if (attacker instanceof EntityPlayer && !target.isEntityAlive()) {
			if (target instanceof EntityLivingBase && !(target instanceof EntityBuilding))
				cap.stickybombKilled++;
			if (target instanceof EntityEngineer)
				cap.engineerKilled = true;
			else if (target instanceof EntitySentry)
				cap.sentryKilled = true;
			else if (target instanceof EntityDispenser)
				cap.dispenserKilled = true;
		}
	}

	/*
	 * @Override public void holster(WeaponsCapability cap, ItemStack stack,
	 * EntityLivingBase living, World world) { super.holster(cap, stack, living,
	 * world); }
	 */
	@Override
	public int holdingMode(ItemStack stack, EntityLivingBase shooter) {
		return 80;
	}
}
