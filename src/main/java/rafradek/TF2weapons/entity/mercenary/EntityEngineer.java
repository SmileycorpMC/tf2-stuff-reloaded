package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.ai.EntityAINearestChecked;
import rafradek.TF2weapons.entity.ai.EntityAIRepair;
import rafradek.TF2weapons.entity.ai.EntityAISetup;
import rafradek.TF2weapons.entity.ai.EntityAIUseWrangler;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWrangler;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

public class EntityEngineer extends EntityTF2Character {

	public EntitySentry sentry;
	public EntityDispenser dispenser;

	public int buildCount;
	public NBTTagCompound grabbed;
	public int grabbedid;

	public EntityEngineer(World p_i1738_1_) {
		super(p_i1738_1_);
		// this.ammoLeft = 24;
		this.experienceValue = 15;
		this.rotation = 15;
		this.tasks.addTask(2, new EntityAIUseWrangler(this, 1, 50f));
		this.tasks.addTask(3, new EntityAIRepair(this, 1, 2f));
		this.tasks.addTask(5, new EntityAISetup(this));
		this.tasks.removeTask(wander);
		this.getCapability(TF2weapons.WEAPONS_CAP, null).setMetal(TF2ConfigVars.maxMetalEngineer);
		this.targetTasks.addTask(5, new EntityAINearestChecked<EntityLivingBase>(this, EntityLivingBase.class, true,
				false, this::isValidTarget, true, false) {

			@Override
			public boolean shouldExecute() {
				return loadout.getStackInSlot(1).getItem() instanceof ItemWrangler && super.shouldExecute();
			}

			@Override
			protected double getTargetDistance() {
				return 50;
			}
		});
		if (this.attack != null)
			attack.setRange(20);
	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootEngineer;
	}

	@Override
	protected void addWeapons() {
		super.addWeapons();

	}

	@Override
	public float[] getDropChance() {
		return new float[] { 0.12f, 0.12f, 0.08f, 0.02f };
	}

	@Override
	public int[] getValidSlots() {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(20.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.5D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.15D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1329D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_ENGINEER_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_ENGINEER_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_ENGINEER_DEATH;
	}

	@Override
	public void switchSlot(int slot, boolean noAmmoSwitch, boolean forceRefresh) {
		super.switchSlot(slot, noAmmoSwitch, forceRefresh);
	}

	public boolean shouldUseWrangler() {
		if (this.sentry != null && this.sentry.getHealth() > 0.3 * this.sentry.getMaxHealth()
				&& this.getAttackTarget() != null && this.sentry.getAmmo() > 0
				&& this.loadout.getStackInSlot(1).getItem() instanceof ItemWrangler) {
			EntityLivingBase target = this.getAttackTarget();
			double sentryRange = this.sentry.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
					.getAttributeValue();
			sentryRange *= sentryRange;

			if (sentryRange < this.sentry.getDistanceSq(this.getAttackTarget())) {
				Entity entityhit = TF2Util
						.pierce(world, sentry, sentry.posX, sentry.posY + sentry.getEyeHeight(), sentry.posZ,
								target.posX, target.posY + target.getEyeHeight(), target.posZ, false, 0, false)
						.get(0).entityHit;

				return entityhit == target || entityhit == this
						|| (entityhit != null && !TF2Util.isOnSameTeam(sentry, entityhit));
			}
		}

		return false;
	}

	@Override
	public void onLivingUpdate() {
		if (!this.world.isRemote && this.ticksExisted == 2) {
			for (EntityBuilding building : this.world.getEntitiesWithinAABB(EntityBuilding.class,
					this.getEntityBoundingBox().grow(32), building -> (building.getOwnerId() == null
							&& building.getOwner() == null && this.getUniqueID().equals(building.ownerEntityID)))) {
				if (building instanceof EntitySentry && this.sentry == null) {
					this.sentry = (EntitySentry) building;
					building.setOwner(this);
				} else if (building instanceof EntityDispenser && this.dispenser == null) {
					this.dispenser = (EntityDispenser) building;
					building.setOwner(this);
				}
			}
			;
		}
		if (!this.world.isRemote) {
			if (this.getMaximumHomeDistance() == 0 && this.getDistanceSq(this.getHomePosition()) < 1) {
				this.setHomePosAndDistance(this.getHomePosition(), 8);
			}
			if (this.getOwner() != null && this.getOrder() == Order.FOLLOW && this.grabbed == null) {
				if (this.sentry != null && this.sentry.isEntityAlive() && !this.sentry.isMini()
						&& this.getDistanceSq(sentry) < 16) {
					this.sentry.grab();
				} else if (this.dispenser != null && this.dispenser.isEntityAlive()
						&& this.getDistanceSq(dispenser) < 16) {
					this.dispenser.grab();
				}
			}
		}

		/*
		 * else if(this.getOwner() != null && this.getOrder() == Order.HOLD) { if
		 * (this.sentry != null && this.sentry.isEntityAlive() &&
		 * !this.isWithinHomeDistanceFromPosition(this.sentry.getPosition())) {
		 * this.sentry.detonate(); } else if (this.dispenser != null &&
		 * this.dispenser.isEntityAlive() &&
		 * !this.isWithinHomeDistanceFromPosition(this.dispenser.getPosition())) {
		 * this.dispenser.detonate(); } }
		 */
		super.onLivingUpdate();
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.rand.nextFloat() < 0.10f + p_70628_2_ * 0.05f)
			this.entityDropItem(ItemFromData.getNewStack("pistol"), 0);
		if (this.rand.nextFloat() < 0.10f + p_70628_2_ * 0.05f)
			this.entityDropItem(ItemFromData.getNewStack("shotgun"), 0);
		if (this.rand.nextFloat() < 0.10f + p_70628_2_ * 0.05f)
			this.entityDropItem(ItemFromData.getNewStack("wrench"), 0);
		this.entityDropItem(
				new ItemStack(TF2weapons.itemBuildingBox, 1, 18 + this.rand.nextInt(3) * 2 + this.getEntTeam()), 0);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setShort("BuildCount", (short) buildCount);
		par1NBTTagCompound.setByte("GrabbedID", (byte) this.grabbedid);
		if (this.grabbed != null)
			par1NBTTagCompound.setTag("Grabbed", this.grabbed);
		/*
		 * if (this.sentry != null && this.sentry.isEntityAlive()) { NBTTagCompound
		 * sentryTag = new NBTTagCompound();
		 * this.sentry.writeToNBTAtomically(sentryTag);
		 * par1NBTTagCompound.setTag("Sentry", sentryTag); } if (this.dispenser != null
		 * && this.dispenser.isEntityAlive()) { NBTTagCompound dispenserTag = new
		 * NBTTagCompound(); this.dispenser.writeToNBTAtomically(dispenserTag);
		 * par1NBTTagCompound.setTag("Dispenser", dispenserTag); }
		 */

	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		if (par1NBTTagCompound.hasKey("Grabbed"))
			this.grabbed = par1NBTTagCompound.getCompoundTag("Grabbed");
		super.readEntityFromNBT(par1NBTTagCompound);
		this.buildCount = par1NBTTagCompound.getShort("BuildCount");
		this.grabbedid = par1NBTTagCompound.getByte("GrabbedID");

		/*
		 * if (par1NBTTagCompound.hasKey("Sentry") && this.sentry == null) { //
		 * System.out.println(par1NBTTagCompound.getCompoundTag("Sentry")); this.sentry
		 * = (EntitySentry)
		 * EntityList.createEntityFromNBT(par1NBTTagCompound.getCompoundTag("Sentry"),
		 * this.world); this.sentry.forceSpawn=true; this.world.spawnEntity(sentry); }
		 * // this.world.spawnEntity(sentry); if (par1NBTTagCompound.hasKey("Dispenser")
		 * && this.dispenser == null) this.dispenser = (EntityDispenser)
		 * AnvilChunkLoader.readWorldEntityPos(
		 * par1NBTTagCompound.getCompoundTag("Dispenser"), this.world, this.posX,
		 * this.posY, this.posZ, true);
		 */
		// dispenser.readFromNBT(par1NBTTagCompound.getCompoundTag("Dispenser"));
		// this.world.spawnEntity(dispenser);
	}

	@Override
	public TF2Class getTF2Class() {
		return TF2Class.ENGINEER;
	}

	@Override
	public boolean isAmmoFull() {
		return this.getWepCapability().getMetal() >= (hasSentryAndDispenser() ? 40
				: Math.min(TF2ConfigVars.maxMetalEngineer, WeaponsCapability.MAX_METAL)) && super.isAmmoFull();
	}

	public boolean hasSentryAndDispenser() {
		return this.sentry != null && this.sentry.isEntityAlive() && this.dispenser != null
				&& this.dispenser.isEntityAlive();
	}

	@Override
	public boolean canBecomeGiant() {
		return false;
	}
}
