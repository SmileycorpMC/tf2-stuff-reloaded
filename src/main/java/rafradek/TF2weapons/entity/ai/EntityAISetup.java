package rafradek.TF2weapons.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.item.ItemPDA;
import rafradek.TF2weapons.util.TF2Util;

import java.util.List;

public class EntityAISetup extends EntityAIBase {

	public EntityEngineer engineer;
	public int buildType;
	public boolean found;
	public Vec3d target;

	public EntityAISetup(EntityEngineer engineer) {
		this.engineer = engineer;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {

		if (this.engineer.getMainWeapon() != -1)
			return false;

		if (this.engineer.getOwner() != null && this.engineer.getOrder() == Order.FOLLOW)
			return false;

		if (this.engineer.grabbed != null) {
			this.buildType = this.engineer.grabbedid + 1;
			return true;
		}
		if (this.engineer.isInWater() || !(this.engineer.loadout.getStackInSlot(3).getItem() instanceof ItemPDA))
			return false;

		boolean dispensernear = TF2Util.findAmmoSource(engineer, 16, false) != null;

		boolean sentryalive = this.engineer.sentry != null && this.engineer.sentry.isEntityAlive();
		boolean dispenseralive = this.engineer.dispenser != null && this.engineer.dispenser.isEntityAlive();

		boolean sentryhome = sentryalive
				&& this.engineer.isWithinHomeDistanceFromPosition(this.engineer.sentry.getPosition());
		boolean dispenserhome = dispenseralive
				&& this.engineer.isWithinHomeDistanceFromPosition(this.engineer.dispenser.getPosition());

		int sentryCost = EntityBuilding.getCost(0, this.engineer.loadout.getStackInSlot(2));
		if (sentryalive)
			sentryCost /= 2;
		int dispenserCost = EntityBuilding.getCost(1, this.engineer.loadout.getStackInSlot(2));
		if (dispenseralive)
			dispenserCost /= 2;

		buildType = (this.engineer.getWepCapability().getMetal() >= sentryCost && !(sentryhome)
				&& (dispensernear || this.engineer.getWepCapability().getMetal() >= sentryCost + dispenserCost)) ? 1
						: (this.engineer.getWepCapability().getMetal() >= dispenserCost && !dispenserhome) ? 2 : 0;
		if (buildType > 0) {
			this.engineer.loadout.getStackInSlot(3).getTagCompound().setByte("Building", (byte) this.buildType);
			this.engineer.switchSlot(3);
			/*
			 * this.engineer.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new
			 * ItemStack(TF2weapons.itemBuildingBox, 1, 16 + buildType * 2 +
			 * this.engineer.getEntTeam()));
			 * this.engineer.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(new
			 * NBTTagCompound());
			 */
		}
		// System.out.println("Promote: "+buildType);

		return buildType > 0;
	}

	@Override
	public void startExecuting() {

	}

	@Override
	public void resetTask() {
		this.engineer.getNavigator().clearPath();
		this.engineer.switchSlot(0);
		if (this.engineer.loadout.getStackInSlot(3).getItem() instanceof ItemPDA)
			this.engineer.loadout.getStackInSlot(3).getTagCompound().setByte("Building", (byte) 0);
	}

	@Override
	public void updateTask() {
		if (this.buildType > 0) {
			if (this.target != null && this.engineer.getDistanceSq(this.target.x, this.target.y, this.target.z) < 2) {
				EntityBuilding building = this.spawn();
				if (this.engineer.grabbed == null)
					this.engineer.getWepCapability().setMetal(this.engineer.getWepCapability().getMetal()
							- EntityBuilding.getCost(this.buildType - 1, this.engineer.loadout.getStackInSlot(2)));

				if (building instanceof EntitySentry)
					this.engineer.sentry = (EntitySentry) building;
				else if (building instanceof EntityDispenser)
					this.engineer.dispenser = (EntityDispenser) building;
				this.engineer.grabbed = null;
				this.engineer.grabbedid = 0;
				return;
			}
			if (this.engineer.getNavigator().noPath()) {

				int size = 1 + buildType == 1 ? 1 : 0;
				Vec3d Vec3d = RandomPositionGenerator.findRandomTarget(this.engineer, size + 1, size);
				if (Vec3d != null) {
					AxisAlignedBB box = new AxisAlignedBB(Vec3d.x - 0.5, Vec3d.y, Vec3d.z - 0.5, Vec3d.x + 0.5,
							Vec3d.y + 1, Vec3d.z + 0.5);
					List<AxisAlignedBB> list = this.engineer.world.getCollisionBoxes(this.engineer, box);

					if (list.isEmpty() && !this.engineer.world.isMaterialInBB(box, Material.WATER)) {
						this.engineer.getNavigator().tryMoveToXYZ(Vec3d.x, Vec3d.y, Vec3d.z, 1);
						this.target = Vec3d;
					}
				}
				/*
				 * for(AxisAlignedBB entry:list){ System.out.println(entry); }
				 */

			}
		}
	}

	public EntityBuilding spawn() {
		EntityBuilding building;
		if (buildType == 1) {
			if (this.engineer.sentry != null && this.engineer.sentry.isEntityAlive()) {
				this.engineer.sentry.detonate();
			}
			building = new EntitySentry(this.engineer.world);
		} else {
			if (this.engineer.dispenser != null && this.engineer.dispenser.isEntityAlive()) {
				this.engineer.dispenser.detonate();
			}
			building = new EntityDispenser(this.engineer.world);
		}

		ItemStack pda = this.engineer.loadout.getStackInSlot(3);
		if (building instanceof EntitySentry) {
			TF2Util.addModifierSafe(building, SharedMonsterAttributes.FOLLOW_RANGE, new AttributeModifier(
					"upgraderange", TF2Attribute.getModifier("Sentry Range", pda, 1f, engineer) - 1f, 2), true);
			((EntitySentry) building).attackRateMult = TF2Attribute.getModifier("Sentry Fire Rate", pda, 1, engineer);
			((EntitySentry) building).setHeat((int) TF2Attribute.getModifier("Piercing", pda, 0, engineer));
		}
		TF2Util.addModifierSafe(building, SharedMonsterAttributes.MAX_HEALTH,
				new AttributeModifier(EntityBuilding.UPGRADE_HEALTH_UUID, "upgradehealth",
						TF2Attribute.getModifier("Building Health", pda, 1f, engineer) - 1f, 2),
				true);
		if (building instanceof EntityDispenser) {
			((EntityDispenser) building).setRange(TF2Attribute.getModifier("Dispenser Range", pda, 1, engineer));
		}

		if (this.engineer.grabbed != null) {
			building.readFromNBT(this.engineer.grabbed);
			building.setConstructing(true);
			building.redeploy = true;
		}

		IBlockState blockTarget = this.engineer.world.getBlockState(new BlockPos(target));
		if (!blockTarget.getBlock().isPassable(this.engineer.world, new BlockPos(target)))
			building.setPosition(target.x, target.y + 1.3, target.z);
		else
			building.setPosition(target.x, target.y + 0.3, target.z);
		building.setEntTeam(this.engineer.getEntTeam());
		building.setOwner(this.engineer);
		if (building instanceof EntitySentry && TF2Attribute.getModifier("Weapon Mode",
				this.engineer.loadout.getStackInSlot(2), 0, this.engineer) == 2)
			((EntitySentry) building).setMini(true);

		this.engineer.world.spawnEntity(building);
		this.target = null;
		this.buildType = 0;
		this.resetTask();
		this.engineer.buildCount += 1;
		return building;
	}
}
