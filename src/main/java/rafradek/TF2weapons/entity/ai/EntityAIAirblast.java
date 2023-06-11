package rafradek.TF2weapons.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemAirblast;
import rafradek.TF2weapons.item.ItemFlameThrower;
import rafradek.TF2weapons.item.ItemMeleeWeapon;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.TF2Util;

import java.util.List;

public class EntityAIAirblast extends EntityAIBase {

	public int delay;
	public EntityTF2Character host;

	public EntityAIAirblast(EntityTF2Character entity) {
		host = entity;
		this.setMutexBits(0);
	}

	@Override
	public boolean shouldExecute() {
		// System.out.println("executing
		// "+TF2ActionHandler.playerAction.server.get(host));
		// System.out.println("should execute:
		// "+(host.world.getDifficulty().getDifficultyId()>=2));
		return this.host.getHeldItemMainhand().getItem() instanceof ItemFlameThrower
				&& ((ItemFlameThrower) this.host.getHeldItemMainhand().getItem()).canAltFire(this.host.world, host,
						this.host.getHeldItemMainhand())
				&& host.world.getDifficulty().getDifficultyId() >= 2;

	}

	@Override
	public void updateTask() {
		// System.out.println("executing
		// "+TF2ActionHandler.playerAction.server.get(host));
		boolean easier = host.world.getDifficulty() == EnumDifficulty.NORMAL;
		delay--;
		if (delay > 0 || this.host.getRNG().nextFloat() > (easier ? 0.22f : 0.28f)) {
			host.getCapability(TF2weapons.WEAPONS_CAP, null).state &= 5;// System.out.println("reset:");
			return;
		}
		Vec3d eyeVec = new Vec3d(host.posX, host.posY + host.getEyeHeight(), host.posZ);
		List<Entity> list = host.world.getEntitiesWithinAABB(Entity.class,
				new AxisAlignedBB(eyeVec.x - 5, eyeVec.y - 5, eyeVec.z - 5, eyeVec.x + 5, eyeVec.y + 5, eyeVec.z + 5));
		boolean airblast = false;
		for (Entity entity : list) {
			// System.out.println(entity+"
			// "+ItemFlameThrower.isPushable(host,entity));
			boolean hasmelee = false;
			if (entity instanceof EntityLivingBase) {
				ItemStack stack = ((EntityLivingBase) entity).getHeldItemMainhand();
				hasmelee = stack.getItem() instanceof ItemMeleeWeapon || (!(stack.getItem() instanceof ItemWeapon)
						&& stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
								.containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName()));
			}
			if (ItemAirblast.isPushable(host, entity)
					&& (hasmelee || entity instanceof EntityThrowable || entity instanceof IProjectile)) {
				// System.out.println(entity);
				// System.out.println("dystans:
				// "+(entity.getDistanceSq(host.posX, host.posY +
				// (double)host.getEyeHeight(), host.posZ)<25));
				// System.out.println(TF2weapons.getTeam(entity)+"
				// "+TF2weapons.getTeam(host));
				airblast = entity.getDistanceSq(host.posX, host.posY + host.getEyeHeight(),
						host.posZ) < (easier ? 16 : 25)
						&& TF2Util.lookingAt(host, (easier ? 40 : 60), entity.posX, entity.posY + entity.height / 2,
								entity.posZ);
				if (airblast)
					break;
			}
		}
		if (airblast) {
			// System.out.println("airblast:");
			((ItemFlameThrower) this.host.getHeldItemMainhand().getItem()).altUse(this.host.getHeldItemMainhand(), host,
					this.host.world);
			this.delay = easier ? 30 : 18;
		} else
			host.getCapability(TF2weapons.WEAPONS_CAP, null).state &= 5;
	}
}
