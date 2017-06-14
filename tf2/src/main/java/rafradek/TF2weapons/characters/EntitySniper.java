package rafradek.TF2weapons.characters;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.weapons.ItemSniperRifle;

public class EntitySniper extends EntityTF2Character {

	public EntitySniper(World par1World) {
		super(par1World);
		this.ammoLeft = 18;
		this.experienceValue = 15;
		this.rotation = 3;
		if (this.attack != null)
			attack.setRange(50);

	}

	/*
	 * protected void addWeapons() {
	 * this.loadout[0]=ItemFromData.getNewStack("sniperrifle");
	 * this.loadout[1]=ItemFromData.getNewStack("sniperrifle"); }
	 */
	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootSniper;
	}
	
	public float[] getDropChance() {
		return new float[] { 0.045f, 0.12f, 0.11f};
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(60.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	public void onLivingUpdate() {
		if (!this.world.isRemote && (this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive())
				&& this.getCapability(TF2weapons.WEAPONS_CAP, null).charging)
			((ItemSniperRifle) this.getHeldItem(EnumHand.MAIN_HAND).getItem())
					.disableZoom(this.getHeldItem(EnumHand.MAIN_HAND), this);
		if (this.getHeldItem(EnumHand.MAIN_HAND) != null
				&& !this.getCapability(TF2weapons.WEAPONS_CAP, null).charging) {
			this.ignoreFrustumCheck = false;
			this.rotation = 15;
		} else {
			this.ignoreFrustumCheck = true;
			this.rotation = 3;
		}
		if(world.isRemote && this.getCapability(TF2weapons.WEAPONS_CAP, null).charging && 
				this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(ItemSniperRifle.slowdownUUID) == null){
			this.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks = 0;
			this.getCapability(TF2weapons.WEAPONS_CAP, null).charging = false;
		}
		// System.out.println("state:
		// "+this.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean("Zoomed")+"
		// "+this.world.isRemote);
		// System.out.println(this.getAttackTarget()!=null&&!this.getAttackTarget().isEntityAlive());
		/*
		 * if(this.getAttackTarget()!=null&&this.getEntitySenses().canSee(this.
		 * getAttackTarget())&&){
		 * ((ItemRangedWeapon)this.getHeldItem(EnumHand.MAIN_HAND).getItem()).
		 * altUse(getHeldItem(EnumHand.MAIN_HAND), this, worldObj); } else
		 * if(this.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean(
		 * "Zoomed")){
		 * ((ItemRangedWeapon)this.getHeldItem(EnumHand.MAIN_HAND).getItem()).
		 * altUse(getHeldItem(EnumHand.MAIN_HAND), this, worldObj); }
		 */
		super.onLivingUpdate();

	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_SNIPER_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound() {
		return TF2Sounds.MOB_SNIPER_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SNIPER_DEATH;
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.rand.nextFloat() < 0.15f + p_70628_2_ * 0.075f)
			this.entityDropItem(ItemFromData.getNewStack("smg"), 0);
		if (this.rand.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.entityDropItem(ItemFromData.getNewStack("sniperrifle"), 0);
	}

	@Override
	public float getAttributeModifier(String attribute) {
		if (this.getAttackTarget() != null && this.getAttackTarget() instanceof EntityPlayer)
			if (attribute.equals("Spread"))
				return super.getAttributeModifier(attribute);
		return super.getAttributeModifier(attribute);
	}

	@Override
	public float getMotionSensitivity() {
		return this.getDiff() == 1 ? 0.75f : (this.getDiff() == 3 ? 0.45f : 0.55f);
	}
}
