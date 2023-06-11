package rafradek.TF2weapons.entity.projectile;

import atomicstryker.dynamiclights.client.DynamicLights;
import atomicstryker.dynamiclights.client.IDynamicLightSource;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.DamageSourceProjectile;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;

import java.util.HashSet;
import java.util.List;

@Optional.Interface(iface = "atomicstryker.dynamiclights.client.IDynamicLightSource", modid = "dynamiclights", striprefs = true)
public abstract class EntityProjectileBase extends Entity
implements IProjectile, IThrowableEntity, IDynamicLightSource, IEntityAdditionalSpawnData {
	public HashSet<Entity> hitEntities = new HashSet<>();
	// private Block field_145790_g;
	/** Seems to be some sort of timer for animating an arrow. */
	/** The owner of this arrow. */
	public EntityLivingBase shootingEntity;
	public ItemStack usedWeapon = ItemStack.EMPTY;
	public ItemStack usedWeaponOrig = ItemStack.EMPTY;
	public double gravity = 0.05;
	public float health = 4f;
	public float distanceTravelled;
	public BlockPos stickedBlock;
	public EntitySentry sentry;

	public boolean reflected;
	public boolean infinite;

	public double cachedGravity = -1;
	public float damageModifier = 1f;
	public float chargeLevel;

	public Entity homingTarget;
	public float homingAngle;
	public float homingSpeed;

	private static final DataParameter<Byte> CRITICAL = EntityDataManager.createKey(EntityProjectileBase.class,
			DataSerializers.BYTE);
	public static final DataParameter<Byte> TYPE = EntityDataManager.createKey(EntityProjectileBase.class,
			DataSerializers.BYTE);
	private static final DataParameter<Boolean> STICK = EntityDataManager.createKey(EntityProjectileBase.class,
			DataSerializers.BOOLEAN);
	private static final DataParameter<Float> STICK_X = EntityDataManager.createKey(EntityProjectileBase.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Float> STICK_Y = EntityDataManager.createKey(EntityProjectileBase.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Float> STICK_Z = EntityDataManager.createKey(EntityProjectileBase.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Float> GRAVITY = EntityDataManager.createKey(EntityProjectileBase.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Boolean> PENETRATE = EntityDataManager.createKey(EntityProjectileBase.class,
			DataSerializers.BOOLEAN);

	public EntityProjectileBase(World p_i1753_1_) {
		super(p_i1753_1_);
		this.setSize(0.5F, 0.5F);
	}

	public void initProjectile(EntityLivingBase shooter, EnumHand hand, ItemStack weapon) {
		this.shootingEntity = shooter;
		this.usedWeapon = weapon.copy();
		this.usedWeaponOrig = weapon;
		this.setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ,
				shooter.rotationYawHead, shooter.rotationPitch + this.getPitchAddition());
		Vec3d look = Vec3d.fromPitchYaw(shooter.rotationPitch + this.getPitchAddition(), shooter.rotationYawHead)
				.scale(80).add(shooter.getPositionEyes(1f));
		Vec3d trace;
		if (shooter instanceof EntityPlayer) {
			RayTraceResult ray = Iterables.getFirst(TF2Util.pierce(world, shooter, this.posX, this.posY, this.posZ,
					look.x, look.y, look.z, false, 0, false), null);
			trace = ray.hitVec;
		} else
			trace = look;
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F
				* (hand == EnumHand.MAIN_HAND ? 1 : -1);
		this.posY -= shooter instanceof EntitySentry ? -0.1D : (shooter instanceof EntityPlayer ? 0.1D : 0D);
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F
				* (hand == EnumHand.MAIN_HAND ? 1 : -1);
		if (trace.lengthSquared() < 2)
			trace = look;
		this.setPosition(this.posX, this.posY, this.posZ);
		boolean nospread = false;
		if (TF2Attribute.getModifier("Onyx Projectile", this.usedWeapon, 0, shooter) != 0) {
			this.damageModifier = TF2Attribute.getModifier("Onyx Projectile", this.usedWeapon, 0, shooter);
			nospread = true;
		}

		this.shoot(trace.x - this.posX, trace.y - this.posY, trace.z - this.posZ,
				((ItemWeapon) this.usedWeapon.getItem()).getProjectileSpeed(usedWeapon, shooter),
				nospread ? 0
						: ((ItemWeapon) this.usedWeapon.getItem()).getWeaponSpread(usedWeapon, shooter)
						* (133.3333333f));
		if (((ItemWeapon) this.usedWeapon.getItem()).canPenetrate(this.usedWeapon, this.shootingEntity)) {
			this.setPenetrate();
		}

		if (((ItemWeapon) this.usedWeapon.getItem()).holdingMode(this.usedWeapon, shooter) != 0) {
			this.chargeLevel = ((ItemWeapon) this.usedWeapon.getItem()).getCharge(shooter, usedWeapon);
		}
		this.getGravityOverride();
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(CRITICAL, (byte) 0);
		this.dataManager.register(TYPE, (byte) 0);
		this.dataManager.register(STICK, false);
		this.dataManager.register(STICK_X, 0f);
		this.dataManager.register(STICK_Y, 0f);
		this.dataManager.register(STICK_Z, 0f);
		this.dataManager.register(PENETRATE, false);
		this.dataManager.register(GRAVITY, -1f);
	}

	public float getPitchAddition() {
		return 0;
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z
	 * direction.
	 */
	@Override
	public void shoot(double x, double y, double z, float speed, float spread) {

		if (spread > 0) {
			float xzlen = MathHelper.sqrt(x * x + z * z);
			float yaw = (float) (MathHelper.atan2(x, z));
			float pitch = (float) (MathHelper.atan2(y, xzlen));
			Vec3d rand = TF2Util.radiusRandom2D(spread * 0.0075f, world.rand, yaw, pitch, speed);
			// System.out.println("motion: "+p_70186_1_+" "+p_70186_3_+"
			// "+p_70186_5_+" "+f2);
			x = rand.x;
			y = rand.y;
			z = rand.z;
		} else {
			double len = MathHelper.sqrt(x * x + y * y + z * z);
			x = x / len * speed;
			y = y / len * speed;
			z = z / len * speed;
		}

		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		float f3 = MathHelper.sqrt(x * x + z * z);
		this.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (MathHelper.atan2(y, f3) * 180.0D / Math.PI);
	}

	public void face(double x, double y, double z, float speedmult) {
		float speed = (float) Math
				.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		x -= this.posX;
		y -= this.posY;
		z -= this.posZ;
		this.shoot(x, y, z, speed * speedmult, 0);
	}

	public void face(EntityLivingBase target, float speedmult) {
		float speed = (float) Math
				.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		double x = target.posX;
		double y = target.posY + target.getEyeHeight();
		double z = target.posZ;

		speed = speed * speedmult;

		double dist = target.getDistance(this);
		float gravity = (float) this.getGravityOverride();
		int ticksToReach = MathHelper.ceil(dist / speed);

		if (ticksToReach != 0) {
			x += target.motionX * ticksToReach * 1;
			z += target.motionZ * ticksToReach * 1;
			// y += this.velTarget.y * moveTicks;
			// double yFall = !target.onGround? -target.motionY : 0;
			if (!target.isInWater()) {
				int ticksGrav = ticksToReach - 1;
				int i = (ticksGrav * ticksGrav + ticksGrav) / 2;
				y += gravity * i;
				/*
				 * if (!target.onGround && target.motionY < 0) yFall += MathHelper.clamp(0.08,
				 * 0, 0.1) * i;
				 */
				/*
				 * for (int i = 1; i <= ticksToReach; i++) { lookY += gravity * i;
				 *
				 *
				 * }
				 */
			}

			/*
			 * RayTraceResult mop =
			 * this.entityHost.world.rayTraceBlocks(this.attackTarget.getPositionVector(),
			 * this.attackTarget.getPositionVector().addVector(0, -0.3 - yFall, 0)); if (mop
			 * != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) yFall =
			 * this.attackTarget.posY - mop.hitVec.y; shouldFireProj = mop != null ||
			 * this.attackTarget.motionY <= 0f; lookY -= yFall;
			 *
			 * if (this.fireAtFeet > 0 && this.entityHost.world.rayTraceBlocks( new
			 * Vec3d(this.entityHost.posX, this.entityHost.posY +
			 * this.entityHost.getEyeHeight(), this.entityHost.posZ), new Vec3d(lookX,
			 * this.attackTarget.posY, lookZ), false, true, false) == null) { lookY -=
			 * (this.attackTarget.height/2)*this.fireAtFeet;
			 *
			 * }
			 */
		}
		this.face(x, y, z, speedmult);
	}
	/**
	 * Sets the position and rotation. Only difference from the other one is no
	 * bounding on the rotation. Args: posX, posY, posZ, yaw, pitch
	 */
	/*
	 * @SideOnly(Side.CLIENT) public void setPositionAndRotation2(double p_70056_1_,
	 * double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int
	 * p_70056_9_) { this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
	 * this.setRotation(p_70056_7_, p_70056_8_); }
	 */

	/**
	 * Sets the velocity to the args. Args: x, y, z
	 */
	// @SideOnly(Side.CLIENT)
	@Override
	public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_) {
		this.motionX = p_70016_1_;
		this.motionY = p_70016_3_;
		this.motionZ = p_70016_5_;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(p_70016_1_ * p_70016_1_ + p_70016_5_ * p_70016_5_);
			this.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(p_70016_1_, p_70016_5_) * 180.0D
					/ Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (MathHelper.atan2(p_70016_3_, f) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
		}
	}

	public void explode(double x, double y, double z, Entity direct, float damageMult) {
		if (world.isRemote || this.shootingEntity == null)
			return;
		this.setDead();
		float size = this.getExplosionSize()
				* TF2Attribute.getModifier("Explosion Radius", this.usedWeapon, 1, this.shootingEntity);
		if (TF2Attribute.getModifier("Airborne Bonus", this.usedWeapon, 0, this.shootingEntity) != 0)
			size *= 0.8f;
		TF2Util.explosion(world, shootingEntity, usedWeapon, this, direct, x, y, z, size,
				damageMult * this.damageModifier, this.getCritical(), this.getDistanceToTarget(null, x, y, z));
	}

	public SoundEvent getExplosionSound() {
		return ItemFromData.getSound(this.usedWeapon, PropertyType.EXPLOSION_SOUND);
	}

	public void addDamageTypes(DamageSource source) {

	}

	public boolean attackDirect(Entity target, double pushForce, boolean headshot, Vec3d hitPos) {
		if (!this.world.isRemote) {
			if (target != null && shootingEntity != null && target.getTeam() != null && target.getTeam().equals(shootingEntity.getTeam()) &!
					(target == shootingEntity || (shootingEntity instanceof IEntityOwnable && ((IEntityOwnable) shootingEntity).getOwner() == target))) return false;
			if(!this.hitEntities.contains(target)) {
				this.hitEntities.add(target);
				float distance = this.getDistanceToTarget(target, hitPos.x, hitPos.y, hitPos.z);
				DamageSourceProjectile src = TF2Util.causeBulletDamage(this.usedWeapon, this.shootingEntity, this);
				int critical = TF2Util.calculateCritPost(
						target, shootingEntity, headshot ? ((ItemWeapon) this.usedWeapon.getItem())
								.getHeadshotCrit(shootingEntity, this.usedWeapon) : this.getCritical(),
								this.usedWeapon, src);
				float dmg = TF2Util.calculateDamage(target, world, this.shootingEntity, usedWeapon, critical, distance)
						* this.damageModifier;
				src.setCritical(critical);
				this.addDamageTypes(src);

				if (headshot)
					((TF2DamageSource) src).addAttackFlag(TF2DamageSource.HEADSHOT);
				boolean proceed = ((ItemWeapon) this.usedWeapon.getItem()).onHit(usedWeapon, this.shootingEntity,
						target, dmg, critical, false);
				if (!proceed || TF2Util.dealDamage(target, this.world, this.shootingEntity, this.usedWeapon, critical,
						dmg, src)) {
					if (!this.canPenetrate())
						this.setDead();
					if (proceed) {
						Vec3d pushvec = new Vec3d(target.posX - hitPos.x, target.posY + target.height / 2 - hitPos.y,
								target.posZ - hitPos.z).normalize();
						pushvec = pushvec.scale(((ItemWeapon) this.usedWeapon.getItem())
								.getKnockbackForDamage(usedWeapon, shootingEntity, dmg, src));
						if (target instanceof EntityLivingBase) {
							pushvec = pushvec.scale(1 - ((EntityLivingBase) target).getAttributeMap()
									.getAttributeInstance(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
									.getAttributeValue());
						}
						target.addVelocity(pushvec.x, pushvec.y, pushvec.z);
						target.isAirBorne = target.isAirBorne || -(pushvec.y) > 0.02D;
						if (target instanceof EntityPlayerMP)
							TF2weapons.network.sendTo(new TF2Message.VelocityAddMessage(pushvec, target.isAirBorne),
									(EntityPlayerMP) target);
					}
					return true;
				}
			}
		}
		return false;
	}

	public float getDistanceToTarget(Entity target, double x, double y, double z) {
		if (target == null)
			return (float) new Vec3d(this.shootingEntity.posX, this.shootingEntity.posY, this.shootingEntity.posZ)
					.distanceTo(new Vec3d(x, y, z));
		else
			return (float) TF2Util.getDistanceBox(this.shootingEntity, target.posX, target.posY, target.posZ,
					target.width + 0.1, target.height + 0.1);
	}

	@Override
	public Entity changeDimension(int dimensionId) {
		return null;
	}

	public float getExplosionSize() {
		return 2.8f;
	}

	public void trace() {
		boolean flag = this.shootingEntity.hasCapability(TF2weapons.WEAPONS_CAP, null);
		if (flag) {
			WeaponsCapability.get(this.shootingEntity).lastHitCharge = this.chargeLevel;
		}
		boolean headshot = this.usedWeapon.isEmpty() || !flag ? false
				: ((ItemWeapon) this.usedWeapon.getItem()).canHeadshot(this.shootingEntity, this.usedWeapon);
		for (RayTraceResult target : TF2Util.pierce(this.world, this.shootingEntity, this.posX, this.posY, this.posZ,
				this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ, headshot,
				this.getCollisionSize(), this.canPenetrate(), this.getCollisionPredicate())) {

			if (target.entityHit != null && target.entityHit instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer) target.entityHit;

				if (entityplayer.capabilities.disableDamage/*
				 * || this.shootingEntity instanceof EntityPlayer &&
				 * !((EntityPlayer)
				 * this.shootingEntity).canAttackPlayer(entityplayer)
				 */)
					continue;
			}

			if (target.entityHit != null && !ForgeEventFactory.onProjectileImpact(this, target)
					&& !(TF2Util.isOnSameTeam(this.shootingEntity, target.entityHit)
							&& (this.ticksExisted < 3 && !world.isRemote && ((ItemWeapon) this.usedWeapon.getItem())
									.onHit(usedWeapon, shootingEntity, target.entityHit, 1, 0, true)))) {
				this.onHitMob(target.entityHit, target);
			}

			else if (target.typeOfHit == Type.BLOCK && !this.useCollisionBox()) {
				if (TF2Attribute.getModifier("Detonate", usedWeapon, 0, shootingEntity) != 0) {
					TF2Attribute.setAttribute(usedWeapon, TF2Attribute.attributes[0], 0);
					this.explode(target.hitVec.x + target.sideHit.getFrontOffsetX() * 0.05,
							target.hitVec.y + target.sideHit.getFrontOffsetY() * 0.05,
							target.hitVec.z + target.sideHit.getFrontOffsetZ() * 0.05, null, 1f);
					return;
				}
				int attr = this.world.isRemote ? 0
						: (int) TF2Attribute.getModifier("Coll Remove", this.usedWeapon, 0, this.shootingEntity);
				if (attr == 0 && !ForgeEventFactory.onProjectileImpact(this, target)) {
					BlockPos blpos = target.getBlockPos();
					this.onHitGround(blpos.getX(), blpos.getY(), blpos.getZ(), target);
				} else if (attr == 2)
					this.explode(target.hitVec.x, target.hitVec.y, target.hitVec.z, null, 1f);
				else
					this.setDead();
			}

		}
		if (flag) {
			WeaponsCapability.get(this.shootingEntity).lastHitCharge = 0;
			if (!this.isDead && (WeaponsCapability.get(this.shootingEntity).state & 2) == 2
					&& TF2Attribute.getModifier("Detonate", usedWeapon, 0, shootingEntity) != 0) {
				this.explode(this.posX + this.motionX * 0.5, this.posY + this.motionY * 0.5,
						this.posZ + this.motionZ * 0.5, null, 1);
				return;
			}
		}
	}

	protected Predicate<Entity> getCollisionPredicate() {
		return TF2Util.TARGETABLE;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		if (this.ticksExisted > this.getMaxTime()) {
			this.setDead();
			return;
		}

		if (this.getCritical() == -1) {
			this.setCritical(0);
			this.explode(posX, posY, posZ, null, 1f);
			return;
		}

		super.onUpdate();

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D
					/ Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, f) * 180.0D / Math.PI);
		}

		if (this.shootingEntity != null) {

			this.trace();

		}
		float f2;
		if (this.isSticked()) {
			this.setPosition(this.dataManager.get(STICK_X), this.dataManager.get(STICK_Y),
					this.dataManager.get(STICK_Z));
			if (!this.world.isRemote && this.ticksExisted % 5 == 0
					&& this.world.getCollisionBoxes(this, this.getEntityBoundingBox().grow(0.1f, 0.1f, 0.1f)).isEmpty())
				this.setSticked(false);
		}
		if (this.moveable()) {
			if (this.shootingEntity != null && this.getHomingAngle() > 0f && !this.world.isRemote) {
				if (this.homingTarget == null || !this.homingTarget.isEntityAlive()) {
					List<Entity> targets = this.world.getEntitiesInAABBexcluding(this,
							this.getEntityBoundingBox().grow(128),
							entity -> (entity.isEntityAlive() && entity instanceof EntityLivingBase
									&& TF2Util.isEnemy(shootingEntity, (EntityLivingBase) entity)
									&& !this.hitEntities.contains(entity)));
					this.homingTarget = TF2Util.getClosestEntityInCone(this.getPositionVector(),
							this.getPositionVector().addVector(this.motionX, this.motionY, this.motionZ), targets,
							this.getHomingAngle());
				}
				if (this.hitEntities.contains(this.homingTarget)) {
					this.homingTarget = null;
				}
				if (this.homingTarget != null) {
					float sq = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
					float yaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
					float pitch = (float) (MathHelper.atan2(this.motionY, sq) * 180.0D / Math.PI);
					Vec3d offset = new Vec3d(this.homingTarget.posX - this.posX,
							this.homingTarget.posY - this.posY + this.homingTarget.height / 2D,
							this.homingTarget.posZ - this.posZ);
					float targetsq = MathHelper.sqrt(offset.x * offset.x + offset.z * offset.z);
					float targetyaw = (float) (MathHelper.atan2(offset.x, offset.z) * 180.0D / Math.PI);
					float targetpitch = (float) (MathHelper.atan2(offset.y, targetsq) * 180.0D / Math.PI);
					yaw += MathHelper.clamp(MathHelper.wrapDegrees(targetyaw - yaw), -this.getHomingTurnSpeed(),
							this.getHomingTurnSpeed());
					pitch += MathHelper.clamp(MathHelper.wrapDegrees(targetpitch - pitch), -this.getHomingTurnSpeed(),
							this.getHomingTurnSpeed());
					offset = Vec3d.fromPitchYaw(pitch, yaw).scale(MathHelper.sqrt(
							this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ));
					this.velocityChanged = true;
					this.motionX = -offset.x;
					this.motionY = -offset.y;
					this.motionZ = offset.z;
				}

			}

			if (!this.useCollisionBox()) {
				this.posX += this.motionX;
				this.posY += this.motionY;
				this.posZ += this.motionZ;
			} else
				this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			float f3 = (float) (1 - this.getGravityOverride() / 5);
			this.motionX *= f3;
			this.motionY *= f3;
			this.motionZ *= f3;
			this.motionY -= this.getGravityOverride();
			f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			if (f2 > 0.1 || Math.abs(this.motionY) > this.getGravityOverride() * 3) {
				this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

				for (this.rotationPitch = (float) (Math.atan2(this.motionY, f2) * 180.0D / Math.PI); this.rotationPitch
						- this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
					;

				while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
					this.prevRotationPitch += 360.0F;

				while (this.rotationYaw - this.prevRotationYaw < -180.0F)
					this.prevRotationYaw -= 360.0F;

				while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
					this.prevRotationYaw += 360.0F;

				this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
				this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			}
		}
		if (this.world.isRemote)
			for (int j = 0; j < this.getSpeed(); ++j) {
				double pX = this.posX - this.motionX * j / this.getSpeed() - this.motionX;
				double pY = this.posY + (this.useCollisionBox() ? this.height / 2 : 0)
						- this.motionY * j / this.getSpeed() - this.motionY;
				double pZ = this.posZ - this.motionZ * j / this.getSpeed() - this.motionZ;
				if (this.getCritical() == 2)
					ClientProxy.spawnCritParticle(this.world, pX, pY, pZ, TF2Util.getTeamColor(this.shootingEntity));
				this.spawnParticles(pX, pY, pZ);

			}
		if (this.isWet())
			this.extinguish();

		if (!this.useCollisionBox()) {
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}
	}

	// @SideOnly(Side.CLIENT)
	@Override
	public void setPositionAndRotationDirect(double p_180426_1_, double p_180426_3_, double p_180426_5_,
			float p_180426_7_, float p_180426_8_, int p_180426_9_, boolean p_180426_10_) {
		if (this.moveable())
			super.setPositionAndRotationDirect(p_180426_1_, p_180426_3_, p_180426_5_, p_180426_7_, p_180426_8_,
					p_180426_9_, p_180426_10_);
	}

	/*
	 * public void setPosition(double x, double y, double z) { this.posX = x;
	 * this.posY = y; this.posZ = z; float f = this.width / 2f; float f1 =
	 * this.height /2f; this.setEntityBoundingBox(new AxisAlignedBB(x - (double)f,
	 * y-(double)f1, z - (double)f, x + (double)f, y + (double)f1, z + (double)f));
	 * }
	 */

	@Override
	public void move(MoverType type, double x, double y, double z) {
		if (this.noClip) {
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
			this.resetPositionToBB();
		} else {
			this.world.profiler.startSection("move");
			// double d3 = this.posX;
			// double d4 = this.posY;
			// double d5 = this.posZ;

			if (this.isInWeb) {
				this.isInWeb = false;
				x *= 0.25D;
				y *= 0.05000000074505806D;
				z *= 0.25D;
				this.motionX = 0.0D;
				this.motionY = 0.0D;
				this.motionZ = 0.0D;
			}

			double d3 = x;
			double d4 = y;
			double d5 = z;

			List<AxisAlignedBB> list1 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().expand(x, y, z));
			AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
			int i = 0;

			for (int j = list1.size(); i < j; ++i)
				y = list1.get(i).calculateYOffset(this.getEntityBoundingBox(), y);

			double limit = y / d4;
			if (!this.isSticky())
				this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
			boolean i_ = this.onGround || d4 != y && d4 < 0.0D;
			int j4 = 0;

			for (int k = list1.size(); j4 < k; ++j4)
				x = list1.get(j4).calculateXOffset(this.getEntityBoundingBox(), x);

			if (this.isSticky()) {
				if (x / d3 < limit)
					limit = x / d3;
			} else
				this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
			j4 = 0;

			for (int k4 = list1.size(); j4 < k4; ++j4)
				z = list1.get(j4).calculateZOffset(this.getEntityBoundingBox(), z);

			if (this.isSticky()) {
				if (z / d5 < limit)
					limit = z / d5;
				this.setEntityBoundingBox(this.getEntityBoundingBox().offset(d3 * limit, d4 * limit, d5 * limit));
			} else
				this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));

			if (this.stepHeight > 0.0F && i_ && (d3 != x || d5 != z)) {
				double d11 = x;
				double d7 = y;
				double d8 = z;
				AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
				this.setEntityBoundingBox(axisalignedbb);
				y = this.stepHeight;
				List<AxisAlignedBB> list = this.world.getCollisionBoxes(this,
						this.getEntityBoundingBox().expand(d3, y, d5));
				AxisAlignedBB axisalignedbb2 = this.getEntityBoundingBox();
				AxisAlignedBB axisalignedbb3 = axisalignedbb2.expand(d3, 0.0D, d5);
				double d9 = y;
				int l = 0;

				for (int i1 = list.size(); l < i1; ++l)
					d9 = list.get(l).calculateYOffset(axisalignedbb3, d9);

				axisalignedbb2 = axisalignedbb2.offset(0.0D, d9, 0.0D);
				double d15 = d3;
				int j1 = 0;

				for (int k1 = list.size(); j1 < k1; ++j1)
					d15 = list.get(j1).calculateXOffset(axisalignedbb2, d15);

				axisalignedbb2 = axisalignedbb2.offset(d15, 0.0D, 0.0D);
				double d16 = d5;
				int l1 = 0;

				for (int i2 = list.size(); l1 < i2; ++l1)
					d16 = list.get(l1).calculateZOffset(axisalignedbb2, d16);

				axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d16);
				AxisAlignedBB axisalignedbb4 = this.getEntityBoundingBox();
				double d17 = y;
				int j2 = 0;

				for (int k2 = list.size(); j2 < k2; ++j2)
					d17 = list.get(j2).calculateYOffset(axisalignedbb4, d17);

				axisalignedbb4 = axisalignedbb4.offset(0.0D, d17, 0.0D);
				double d18 = d3;
				int l2 = 0;

				for (int i3 = list.size(); l2 < i3; ++l2)
					d18 = list.get(l2).calculateXOffset(axisalignedbb4, d18);

				axisalignedbb4 = axisalignedbb4.offset(d18, 0.0D, 0.0D);
				double d19 = d5;
				int j3 = 0;

				for (int k3 = list.size(); j3 < k3; ++j3)
					d19 = list.get(j3).calculateZOffset(axisalignedbb4, d19);

				axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d19);
				double d20 = d15 * d15 + d16 * d16;
				double d10 = d18 * d18 + d19 * d19;

				if (d20 > d10) {
					x = d15;
					z = d16;
					y = -d9;
					this.setEntityBoundingBox(axisalignedbb2);
				} else {
					x = d18;
					z = d19;
					y = -d17;
					this.setEntityBoundingBox(axisalignedbb4);
				}

				int l3 = 0;

				for (int i4 = list.size(); l3 < i4; ++l3)
					y = list.get(l3).calculateYOffset(this.getEntityBoundingBox(), y);

				this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

				if (d11 * d11 + d8 * d8 >= x * x + z * z) {
					x = d11;
					y = d7;
					z = d8;
					this.setEntityBoundingBox(axisalignedbb1);
				}
			}

			this.world.profiler.endSection();
			this.world.profiler.startSection("rest");
			this.resetPositionToBB();
			this.collidedHorizontally = d3 != x || d5 != z;
			this.collidedVertically = d4 != y;
			this.onGround = this.collidedVertically && d4 < 0.0D;
			this.collided = this.collidedHorizontally || this.collidedVertically;
			if (this.isSticky() && !this.world.isRemote && this.collided)
				this.setSticked(true);
			j4 = MathHelper.floor(this.posX);
			int l4 = MathHelper.floor(this.posY - 0.20000000298023224D);
			int i5 = MathHelper.floor(this.posZ);
			BlockPos blockpos = new BlockPos(j4, l4, i5);
			IBlockState iblockstate = this.world.getBlockState(blockpos);

			if (iblockstate.getMaterial() == Material.AIR) {
				BlockPos blockpos1 = blockpos.down();
				IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
				Block block1 = iblockstate1.getBlock();

				if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
					iblockstate = iblockstate1;
					blockpos = blockpos1;
				}
			}
			this.stickedBlock = blockpos;
			this.updateFallState(y, this.onGround, iblockstate, blockpos);
			/*
			 * double limit=y/d7; if(!this.isSticky()){
			 * this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0. 0D, y,
			 * 0.0D)); } if(this.isSticky()){ if(x/d6<limit){ limit=x/d6; } }
			 * if(this.isSticky()){ if(z/d8<limit){ limit=z/d8; }
			 * this.setEntityBoundingBox(this.getEntityBoundingBox().offset(d6*
			 * limit,d7*limit,d8*limit)); }
			 */

			if (d3 != x)
				this.onHitBlockX();

			if (d5 != z)
				this.onHitBlockZ();
			Block block = iblockstate.getBlock();
			if (d4 != y)
				this.onHitBlockY(block);

			if (this.canTriggerWalking() && this.getRidingEntity() == null)
				/*
				 * double d15 = this.posX - d3; double d16 = this.posY - d4; double d17 =
				 * this.posZ - d5;
				 *
				 * if (block1 != Blocks.ladder) { d16 = 0.0D; }
				 */
				if (block != null && this.onGround)
					block.onEntityCollidedWithBlock(this.world, blockpos, iblockstate, this);

			try {
				this.doBlockCollisions();
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
				CrashReportCategory crashreportcategory = crashreport
						.makeCategory("Entity being checked for collision");
				this.addEntityCrashInfo(crashreportcategory);
				throw new ReportedException(crashreport);
			}

			this.world.profiler.endSection();
		}
	}

	/*
	 * public void resetPositionToBB() { AxisAlignedBB axisalignedbb =
	 * this.getEntityBoundingBox(); this.posX = (axisalignedbb.minX +
	 * axisalignedbb.maxX) / 2.0D; this.posY = (axisalignedbb.minY +
	 * axisalignedbb.maxY) / 2.0D; this.posZ = (axisalignedbb.minZ +
	 * axisalignedbb.maxZ) / 2.0D; }
	 */
	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_) {

	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_) {

	}

	public abstract void onHitGround(int x, int y, int z, RayTraceResult mop);

	public abstract void onHitMob(Entity entityHit, RayTraceResult mop);

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	public boolean moveable() {
		return !this.isSticked();
	}

	public float getHomingAngle() {
		return this.homingAngle;
	}

	public float getHomingTurnSpeed() {
		return this.homingSpeed;
	}

	public void setCritical(int critical) {
		this.dataManager.set(CRITICAL, (byte) critical);
	}

	public void setSticked(boolean stick) {
		this.dataManager.set(STICK, stick);
		this.dataManager.set(STICK_X, (float) this.posX);
		this.dataManager.set(STICK_Y, (float) this.posY);
		this.dataManager.set(STICK_Z, (float) this.posZ);
		/*
		 * if(!this.world.isRemote){ EntityTracker
		 * tracker=((WorldServer)this.world).getEntityTracker();
		 * tracker.sendToAllTrackingEntity(this, new S12PacketEntityVelocity(this));
		 * tracker.sendToAllTrackingEntity(this, new S18PacketEntityTeleport(this)); }
		 */
	}

	/**
	 * Whether the arrow has a stream of critical hit particles flying behind it.
	 */
	public int getCritical() {
		return this.dataManager.get(CRITICAL);
	}

	public int getType() {
		return this.dataManager.get(TYPE);
	}

	public boolean isSticked() {
		return this.dataManager.get(STICK);
	}

	public boolean canPenetrate() {
		return this.dataManager.get(PENETRATE);
	}

	protected float getSpeed() {
		return 3;
	}

	public double getGravity() {
		return 0.0381f;
	}

	public double getGravityOverride() {
		if (this.dataManager.get(GRAVITY) == -1f && !this.usedWeapon.isEmpty()) {
			this.dataManager.set(GRAVITY, ((ItemWeapon) this.usedWeapon.getItem()).getAdditionalGravity(shootingEntity,
					usedWeapon, this.getGravity()));
		}
		return this.dataManager.get(GRAVITY);
	}

	@Override
	public Entity getThrower() {
		return this.shootingEntity;
	}

	@Override
	public void setThrower(Entity entity) {
		this.shootingEntity = (EntityLivingBase) entity;

	}

	public void setType(int type) {
		this.dataManager.set(TYPE, (byte) type);
	}

	public void setPenetrate() {
		this.dataManager.set(PENETRATE, true);
	}

	public boolean isSticky() {
		return false;
	}

	public void onHitBlockX() {
		this.motionX = 0;
	}

	public void onHitBlockY(Block block) {
		block.onLanded(this.world, this);
	}

	public void onHitBlockZ() {
		this.motionZ = 0;
	}

	public abstract void spawnParticles(double x, double y, double z);

	public int getMaxTime() {
		return 1000;
	}

	@Override
	public boolean writeToNBTOptional(NBTTagCompound tagCompund) {
		return false;
	}

	public boolean useCollisionBox() {
		return false;
	}

	public float getCollisionSize() {
		return 0.3f;
	}

	public boolean isPushable() {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		} else if (source.getTrueSource() != null && source.getTrueSource() instanceof EntityLivingBase
				&& !TF2Util.isOnSameTeam(source.getTrueSource(), this.shootingEntity)
				&& !(source.isExplosion() || source.isFireDamage())) {
			if (source instanceof TF2DamageSource) {
				damage *= Math.max(1f, TF2Attribute.getModifier("Destroy Projectiles",
						((TF2DamageSource) source).getWeapon(), 0, (EntityLivingBase) source.getTrueSource()));
			}
			this.health -= damage;
			if (this.health <= 0)
				this.setDead();
			return true;
		}
		return false;
	}

	@Override
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength();

		if (Double.isNaN(d0))
			d0 = 1.0D;

		d0 = d0 * 512.0D * getRenderDistanceWeight();
		return distance < d0 * d0;
	}

	@Optional.Method(modid = "dynamiclights")
	public void makeLit() {
		if (TF2ConfigVars.dynamicLightsProj)
			DynamicLights.addLightSource(this);
	}

	@Optional.Method(modid = "dynamiclights")
	@Override
	public Entity getAttachmentEntity() {
		return this;
	}

	@Optional.Method(modid = "dynamiclights")
	@Override
	public int getLightLevel() {
		return 9;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt((int) (this.motionX * 8000D));
		buffer.writeInt((int) (this.motionY * 8000D));
		buffer.writeInt((int) (this.motionZ * 8000D));
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		this.motionX = buffer.readInt() / 8000D;
		this.motionY = buffer.readInt() / 8000D;
		this.motionZ = buffer.readInt() / 8000D;
	}

}