package rafradek.TF2weapons.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;

import java.util.Collections;
import java.util.List;

public class ItemWrangler extends ItemUsable {

	@Override
	public boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand,
			PredictionMessage message) {
		return true;
	}

	@Override
	public boolean fireTick(ItemStack stack, EntityLivingBase living, World world) {
		EntitySentry sentry = living.getCapability(TF2weapons.WEAPONS_CAP, null).controlledSentry;
		if (sentry != null && sentry.isEntityAlive())
			sentry.shootBullet(living);
		return false;
	}

	@Override
	public boolean altFireTick(ItemStack stack, EntityLivingBase living, World world) {
		EntitySentry sentry = living.getCapability(TF2weapons.WEAPONS_CAP, null).controlledSentry;
		if (sentry != null && sentry.isEntityAlive())
			sentry.shootRocket(living);
		return false;
	}

	@Override
	public void draw(WeaponsCapability weaponsCapability, ItemStack stack, final EntityLivingBase living, World world) {
		super.draw(weaponsCapability, stack, living, world);
		if (!world.isRemote) {
			weaponsCapability.controlledSentry = null;
			List<EntitySentry> list = world.getEntitiesWithinAABB(EntitySentry.class,
					living.getEntityBoundingBox().grow(128, 128, 128),
					input -> input.getOwner() == living && !input.isDisabled());
			Collections.sort(list, new EntityAINearestAttackableTarget.Sorter(living));
			if (!list.isEmpty()) {
				list.get(0).setControlled(true);
				weaponsCapability.controlledSentry = list.get(0);
			}
		}
	}

	@Override
	public void holster(WeaponsCapability weaponsCapability, ItemStack stack, final EntityLivingBase living,
			World world) {
		if (weaponsCapability.controlledSentry != null)
			weaponsCapability.controlledSentry.setControlled(false);
		weaponsCapability.controlledSentry = null;
		super.holster(weaponsCapability, stack, living, world);
	}
}
