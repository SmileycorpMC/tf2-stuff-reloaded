package rafradek.TF2weapons.entity.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget.Sorter;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.item.ItemDisguiseKit;
import rafradek.TF2weapons.util.TF2Util;

import java.util.Collections;
import java.util.List;

public class EntityAISpotTarget<T extends EntityLivingBase> extends EntityAIBase {

	/** The entity that this task belongs to */
	protected final EntityBuilding taskOwner;
	/**
	 * If true, EntityAI targets must be able to be seen (cannot be blocked by
	 * walls) to be suitable targets.
	 */
	protected boolean shouldCheckSight;
	/**
	 * When true, only entities that can be reached with minimal effort will be
	 * targetted.
	 */
	private final boolean nearbyOnly;
	/**
	 * When nearbyOnly is true: 0 -> No target, but OK to search; 1 -> Nearby target
	 * found; 2 -> Target too far.
	 */
	private int targetSearchStatus;
	/**
	 * When nearbyOnly is true, this throttles target searching to avoid excessive
	 * pathfinding.
	 */
	private int targetSearchDelay;
	/**
	 * If @shouldCheckSight is true, the number of ticks before the interuption of
	 * this AITastk when the entity does't see the target
	 */
	protected T target;
	protected int unseenMemoryTicks;

	public int targetChoosen = 0;
	private Class<T> targetClass;
	private Sorter theNearestAttackableTargetSorter;
	private Predicate<T> targetEntitySelector;
	public T targetEntity;
	private boolean targetLock;
	private int targetUnseenTicks;

	public EntityAISpotTarget(EntityBuilding p_i1665_1_, Class<T> p_i1665_2_, boolean p_i1665_4_, boolean p_i1665_5_,
			final Predicate<T> p_i1665_6_, boolean targetLock, boolean allowBehind) {
		super();
		this.nearbyOnly = p_i1665_4_;
		this.shouldCheckSight = p_i1665_5_;
		this.taskOwner = p_i1665_1_;
		this.targetClass = p_i1665_2_;
		this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(p_i1665_1_);
		this.setMutexBits(1);
		this.targetLock = targetLock;
		this.targetEntitySelector = target -> {
			if (p_i1665_6_ != null && !p_i1665_6_.apply(target))
				return false;
			else {
				// System.out.println("found "+target.getClass().getName()+"
				// "+EntityAINearestChecked.this.taskOwner.getClass().getName());
				if (target instanceof EntityLivingBase) {
					double d0 = EntityAISpotTarget.this.getTargetDistance();

					if (target.isSneaking())
						d0 *= 0.800000011920929D;

					if (target instanceof EntityPlayer && target.isInvisible()) {
						float f = ((EntityPlayer) target).getArmorVisibility();

						if (f < 0.1F)
							f = 0.1F;

						d0 *= 0.7F * f;
					}
					if (target.hasCapability(TF2weapons.WEAPONS_CAP, null)
							&& (target.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks >= 20
									|| ItemDisguiseKit.isDisguised(target, taskOwner)))
						d0 = 0;
					boolean fastCheck = allowBehind;
					if (target.getDistance(taskOwner) > d0 || (!fastCheck && !TF2Util.lookingAtFast(taskOwner, 86,
							target.posX, target.posY + target.getEyeHeight(), target.posZ)))
						return false;

				}

				return EntityAISpotTarget.this.isSuitableTarget(target, false);
			}
		};
	}

	protected double getTargetDistance() {
		IAttributeInstance iattributeinstance = this.taskOwner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
		return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */

	/**
	 * Reset the task's internal state. Called when this task is interrupted by
	 * another one
	 */
	@Override
	public void resetTask() {
		this.taskOwner.setAttackTarget((EntityLivingBase) null);
		this.target = null;
	}

	@Override
	public boolean shouldExecute() {
		double d0 = this.getTargetDistance() / 2;
		if (((this.taskOwner.getAttackTarget() == null)
				|| this.taskOwner.getAttackTarget().getDistanceSq(taskOwner) > d0 * d0))
			this.targetChoosen++;
		if (((this.taskOwner.getAttackTarget() == null) && this.targetChoosen > 1) || this.targetChoosen > 5
				|| !this.targetLock) {
			// System.out.println("executing
			// "+this.taskOwner.getClass().getName());
			this.targetChoosen = 0;
			double d1 = this.getTargetDistance();
			List<T> list = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass,
					this.taskOwner.getEntityBoundingBox().grow(d1, d0, d1), this.targetEntitySelector);
			Collections.sort(list, this.theNearestAttackableTargetSorter);

			if (list.isEmpty())
				// System.out.println("emptylist
				// "+this.taskOwner.getClass().getName());
				return false;
			else {
				this.targetEntity = list.get(0);
				return true;
			}
		}
		return false;
	}

	@Override
	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.targetEntity);
		super.startExecuting();
		this.targetSearchStatus = 0;
		this.targetSearchDelay = 0;
		this.targetUnseenTicks = 0;
	}

	/**
	 * A method used to see if an entity is a suitable target through a number of
	 * checks. Args : entity, canTargetInvinciblePlayer
	 */
	@Override
	public boolean shouldContinueExecuting() {

		EntityLivingBase entitylivingbase = this.taskOwner.getAttackTarget();
		if (entitylivingbase == null)
			return false;
		else if (!entitylivingbase.isEntityAlive())
			return false;
		else if (!this.targetLock && this.taskOwner.ticksExisted % 13 == 0)
			return this.shouldExecute();
		else {
			Team team = this.taskOwner.getTeam();
			Team team1 = entitylivingbase.getTeam();

			if (team != null && team1 == team)
				return false;
			else {
				double d0 = this.getTargetDistance();

				if (this.taskOwner.getDistanceSq(entitylivingbase) > d0 * d0)
					return false;
				else {
					if (this.shouldCheckSight)
						if (this.taskOwner.getEntitySenses().canSee(entitylivingbase))
							this.targetUnseenTicks = 0;
						else if (++this.targetUnseenTicks > 60)
							return false;

					return !(entitylivingbase instanceof EntityPlayer)
							|| !((EntityPlayer) entitylivingbase).capabilities.disableDamage;
				}
			}
		}
	}

	protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles) {
		if (target == null)
			return false;
		else if (target == this.taskOwner)
			return false;
		else if (!target.isEntityAlive())
			return false;
		else if (!this.taskOwner.canAttackClass(target.getClass()))
			return false;
		else {
			Team team = this.taskOwner.getTeam();
			Team team1 = target.getTeam();
			if ((team != null && team1 == team))
				return false;
			else {
				if (this.taskOwner instanceof IEntityOwnable
						&& ((IEntityOwnable) this.taskOwner).getOwnerId() != null) {
					if (target instanceof IEntityOwnable && ((IEntityOwnable) this.taskOwner).getOwnerId()
							.equals(((IEntityOwnable) target).getOwnerId()))
						return false;

					if (target == ((IEntityOwnable) this.taskOwner).getOwner())
						return false;
				}
				if (target instanceof EntityPlayer && !includeInvincibles
						&& ((EntityPlayer) target).capabilities.disableDamage)
					return false;

				return !this.shouldCheckSight || this.taskOwner.getEntitySenses().canSee(target);
			}
		}
	}
}
