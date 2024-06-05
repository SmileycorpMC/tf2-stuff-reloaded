package rafradek.TF2weapons.entity.building;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.item.ItemWrench;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;

public class EntitySapper extends EntityBuilding {

	public EntityBuilding sappedBuilding;
	public ItemStack sapperItem;

	public EntitySapper(World world) {
		super(world);
		this.setSize(1f, 1.1f);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			if (this.sappedBuilding == null || !this.sappedBuilding.isEntityAlive()) {
				this.setDead();
				return;
			}
			TF2Util.dealDamage(this.sappedBuilding, this.world, this.getOwner(), sapperItem, 0, 0.25f,
					TF2Util.causeBulletDamage(sapperItem, this.getOwner(), this));

			if (!this.isEntityAlive() && this.sappedBuilding != null)
				this.sappedBuilding.sapper = null;
		}

	}

	@Override
	public SoundEvent getSoundNameForState(int state) {
		switch (state) {
		case 0:
			return TF2Sounds.MOB_SAPPER_IDLE;
		default:
			return null;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if ((source instanceof TF2DamageSource) && !((TF2DamageSource) source).getWeapon().isEmpty()
				&& ((TF2DamageSource) source).getWeapon().getItem() instanceof ItemWrench)
			super.attackEntityFrom(source, amount);
		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12D);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SAPPER_DEATH;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);
	}
}
