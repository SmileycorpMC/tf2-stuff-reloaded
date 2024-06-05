package rafradek.TF2weapons.entity;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.util.TF2Util;

public class EntityPickup extends Entity {

	public int age = 0;
	public int lifespan = 1200;
	public boolean rematerialize;
	private int rematerializeTime = 0;
	public float hoverStart;
	private static final DataParameter<Boolean> DISABLED = EntityDataManager.<Boolean>createKey(EntityPickup.class,
			DataSerializers.BOOLEAN);
	private static final DataParameter<Byte> TYPE = EntityDataManager.<Byte>createKey(EntityPickup.class,
			DataSerializers.BYTE);

	public enum Type {
		AMMO_SMALL(0, 0.2f, new ItemStack(TF2weapons.itemPickup, 1, 0), true),
		AMMO_NORMAL(0, 0.5f, new ItemStack(TF2weapons.itemPickup, 1, 1), true),
		AMMO_BIG(0, 1f, new ItemStack(TF2weapons.itemPickup, 1, 2), true),
		HEALTH_SMALL(0.205f, 0f, new ItemStack(TF2weapons.itemPickup, 1, 3), true),
		HEALTH_MEDIUM(0.5f, 0f, new ItemStack(TF2weapons.itemPickup, 1, 4), true),
		HEALTH_BIG(1f, 0f, new ItemStack(TF2weapons.itemPickup, 1, 5), true),
		SANDVICH(0.5f, 0f, new ItemStack(TF2weapons.itemSandvich, 1, 2), false);

		float healthRegen;
		float ammoRegen;
		public ItemStack model;
		public boolean visible;

		Type(float healthRegen, float ammoRegen, ItemStack model, boolean visible) {
			this.healthRegen = healthRegen;
			this.ammoRegen = ammoRegen;
			this.model = model;
			this.visible = visible;
		}
	}

	public EntityPickup(World world, Type type, boolean rematerialize) {
		this(world);
		this.setType(type);
		this.rematerialize = rematerialize;
	}

	public EntityPickup(World world) {
		super(world);
		this.setSize(0.25f, 0.25f);
		this.hoverStart = (float) (Math.random() * Math.PI * 2.0D);
	}

	public void setCollected() {
		this.dataManager.set(DISABLED, true);
		this.rematerializeTime = 300;
		if (!this.rematerialize)
			this.setDead();
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		if (!this.world.isRemote && !this.isDisabled()) {
			if (this.getType().healthRegen > 0 && entityIn.getHealth() < entityIn.getMaxHealth()) {
				entityIn.heal(this.getType().healthRegen * entityIn.getMaxHealth());
				this.setCollected();
			}
			if (this.getType().ammoRegen > 0) {
				if (TF2Util.restoreAmmoToWeapons(entityIn, this.getType().ammoRegen))
					this.setCollected();
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!this.world.isRemote && this.isDisabled() && --this.rematerializeTime <= 0) {
			this.dataManager.set(DISABLED, false);
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		double d0 = this.motionX;
		double d1 = this.motionY;
		double d2 = this.motionZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.03999999910593033D;
		}

		if (this.world.isRemote) {
			this.noClip = false;
		} else {
			this.noClip = this.pushOutOfBlocks(this.posX,
					(this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
		}

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		boolean flag = (int) this.prevPosX != (int) this.posX || (int) this.prevPosY != (int) this.posY
				|| (int) this.prevPosZ != (int) this.posZ;

		if (flag || this.ticksExisted % 25 == 0) {
			if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
				this.motionY = 0.20000000298023224D;
				this.motionX = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F;
				this.motionZ = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F;
				this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
			}
		}

		float f = 0.98F;

		if (this.onGround) {
			BlockPos underPos = new BlockPos(MathHelper.floor(this.posX),
					MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
			net.minecraft.block.state.IBlockState underState = this.world.getBlockState(underPos);
			f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;
		}

		this.motionX *= f;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= f;

		if (this.onGround) {
			this.motionY *= -0.5D;
		}

		if (this.age != -32768) {
			++this.age;
		}

		this.handleWaterMovement();

		if (!this.world.isRemote) {
			double d3 = this.motionX - d0;
			double d4 = this.motionY - d1;
			double d5 = this.motionZ - d2;
			double d6 = d3 * d3 + d4 * d4 + d5 * d5;

			if (d6 > 0.01D) {
				this.isAirBorne = true;
			}
			if (this.age > this.lifespan && !this.rematerialize)
				this.setDead();
		}

	}

	public void setType(Type type) {
		this.dataManager.set(TYPE, (byte) type.ordinal());
	}

	public Type getType() {
		return Type.values()[this.dataManager.get(TYPE)];
	}

	public boolean isDisabled() {
		return this.dataManager.get(DISABLED);
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(DISABLED, false);
		this.dataManager.register(TYPE, (byte) 0);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.setType(Type.values()[compound.getByte("Type")]);
		this.age = compound.getInteger("Age");
		this.lifespan = compound.getInteger("Lifespan");
		this.rematerialize = compound.getBoolean("Rematerialize");
		this.dataManager.set(DISABLED, compound.getBoolean("Disabled"));
		this.rematerializeTime = compound.getInteger("RematerializeTime");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setByte("Type", (byte) this.getType().ordinal());
		compound.setInteger("Age", this.age);
		compound.setInteger("Lifespan", this.lifespan);
		compound.setInteger("RematerializeTime", this.rematerializeTime);
		compound.setBoolean("Rematerialize", this.rematerialize);
		compound.setBoolean("Disabled", this.isDisabled());
	}

}
