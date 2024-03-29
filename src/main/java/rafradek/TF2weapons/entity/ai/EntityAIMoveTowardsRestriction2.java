package rafradek.TF2weapons.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityAIMoveTowardsRestriction2 extends EntityAIBase {

	private final EntityCreature creature;
	private double movePosX;
	private double movePosY;
	private double movePosZ;
	private final double movementSpeed;

	public EntityAIMoveTowardsRestriction2(EntityCreature creatureIn, double speedIn) {
		this.creature = creatureIn;
		this.movementSpeed = speedIn;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {

		if (this.creature.isWithinHomeDistanceCurrentPosition()) {
			return false;
		} else {

			BlockPos blockpos = this.creature.getHomePosition();

			Vec3d vec3d = this.creature.getMaximumHomeDistance() == 0 ? new Vec3d(blockpos)
					: RandomPositionGenerator.findRandomTargetBlockTowards(this.creature,
							(int) Math.min(this.creature.getMaximumHomeDistance(), 16), 7,
							new Vec3d(blockpos.getX(), blockpos.getY(), blockpos.getZ()));

			if (vec3d == null) {

				return false;
			} else {
				this.movePosX = vec3d.x;
				this.movePosY = vec3d.y;
				this.movePosZ = vec3d.z;
				return true;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean shouldContinueExecuting() {
		return !this.creature.getNavigator().noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		this.creature.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.movementSpeed);
	}

}
