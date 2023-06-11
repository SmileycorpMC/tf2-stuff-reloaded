package rafradek.TF2weapons.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import rafradek.TF2weapons.entity.mercenary.EntityMedic;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;

public class EntityAIOwnerHurt extends EntityAITarget {

	public EntityAIOwnerHurt(EntityCreature creature) {
		super(creature, false, false);
	}

	@Override
	public boolean shouldExecute() {
		EntityPlayer owner=(EntityPlayer) ((EntityTF2Character) this.taskOwner).getOwner();
		return owner != null && (owner.getRevengeTarget() != null || owner.getLastAttackedEntity() != null);
	}

	public void startExecuting()
    {
		EntityPlayer owner=(EntityPlayer) ((EntityTF2Character) this.taskOwner).getOwner();
		if(owner.getRevengeTarget() != null && this.isSuitableTarget(owner.getRevengeTarget(), false))
			this.taskOwner.setAttackTarget(owner.getRevengeTarget());
		else if(owner.getLastAttackedEntity() != null && this.isSuitableTarget(owner.getLastAttackedEntity(), false))
			this.taskOwner.setAttackTarget(owner.getLastAttackedEntity());
    }
	
	@Override
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

			if ((team != null && team1 == team) && !(this.taskOwner instanceof EntityMedic))
				return false;
			else {
				if (this.taskOwner instanceof IEntityOwnable
						&& ((IEntityOwnable) this.taskOwner).getOwnerId() != null) {
					if (target instanceof IEntityOwnable && ((IEntityOwnable) this.taskOwner).getOwnerId()
							.equals(((IEntityOwnable) target).getOwnerId()))
						return false;

					if (target == ((IEntityOwnable) this.taskOwner).getOwner())
						return false;
				} else if (target instanceof EntityPlayer && !includeInvincibles
						&& ((EntityPlayer) target).capabilities.disableDamage)
					return false;

				return !this.shouldCheckSight || this.taskOwner.getEntitySenses().canSee(target);
			}
		}
	}
}
