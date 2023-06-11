package rafradek.TF2weapons.entity.boss;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EntityHHH extends EntityTF2Boss {

	public int targetTime;
	private int attackTick;
	private int scareTick;
	private int teleportTime;
	private int toTeleportTime;
	public int begin = 30;
	private float damageTakenNoPath;
	private int aboveGroundTicks;
	private int dashCool;
	private int dashTick;
	private int targetInAir;
	private Vec3d dashMotion;
	private List<EntityLivingBase> possibleTargets = new ArrayList<>();
	private static UUID slowDash = UUID.fromString("03edb08b-0a2f-4040-9c9c-062d0c2e2a85");
	private static UUID fasterSpeed = UUID.fromString("03edb08b-0a2f-4040-9c9c-062d0c2e2b85");
	private Vec3d lastPos = Vec3d.ZERO;

	public EntityHHH(World worldIn) {
		super(worldIn);
		this.setSize(0.9f, 2.85f);
		this.stepHeight = 1.05f;
		this.setNoAI(true);
		this.setPathPriority(PathNodeType.LAVA, 8.0F);
		this.setHeldItem(EnumHand.MAIN_HAND, ItemFromData.getNewStack("headtaker"));
		this.getHeldItemMainhand().addEnchantment(Enchantments.KNOCKBACK, 10);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIAirAttack());
		this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		/*
		 * this.targetTasks.addTask(1, new
		 * EntityAINearestAttackableTarget<EntityLivingBase>(this,
		 * EntityLivingBase.class,2, false,false,new Predicate<EntityLivingBase>(){
		 *
		 * @Override public boolean apply(EntityLivingBase input) { return
		 * input.getActivePotionEffect(TF2weapons.it)!=null; }
		 *
		 * }));
		 */
		this.targetTasks.addTask(2,
				new EntityAINearestAttackableTarget<>(this, EntitySentry.class, 2, false, false, null));
		/*
		 * this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new
		 * Class[0])); this.targetTasks.addTask(3, new
		 * EntityAINearestAttackableTarget<EntityLivingBase>(this,
		 * EntityLivingBase.class,2, false,false,new Predicate<EntityLivingBase>(){
		 *
		 * @Override public boolean apply(EntityLivingBase input) { return (input
		 * instanceof EntityTF2Character || input instanceof EntityPlayer) &&
		 * getDistanceSq(input)<600; }
		 *
		 * }));
		 */

	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		// this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(128.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(105);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.10D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(13D);

	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance diff, IEntityLivingData p_110161_1_) {
		p_110161_1_ = super.onInitialSpawn(diff, p_110161_1_);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
				.setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue()
						* (0.88 + this.level * 0.12));
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
				.setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue()
						* (0.9 + this.level * 0.1));

		return p_110161_1_;
	}

	@Override
	public void dropFewItems(boolean hit, int looting) {
		if (this.rand.nextBoolean() || this.level == 1)
			this.entityDropItem(ItemFromData.getNewStack("headtaker"), 0);
		// ItemStack hat=ItemFromData.getNewStack("monoculus");
		// hat.getTagCompound().setShort("BossLevel",(short)this.level);
		// this.entityDropItem(hat, 0);
	}

	@Override
	public void onLivingUpdate() {

		if (this.getHeldItemMainhand().isEmpty() || !(this.getHeldItemMainhand().getItem() instanceof ItemWeapon))
			this.setHeldItem(EnumHand.MAIN_HAND, ItemFromData.getNewStack("headtaker"));

		super.onLivingUpdate();
		if (this.begin-- > 20 && this.world.isRemote)
			for (int i = 0; i < 40; i++) {
				Vec3d pos = TF2Util.radiusRandom2D(2.2f, this.rand);
				this.world.spawnParticle(EnumParticleTypes.PORTAL, pos.x + this.posX, this.posY - 0.5,
						pos.y + this.posZ, 0, 0, 0, new int[0]);
			}

		if (!world.isRemote && this.begin <= 0) {
			if (this.rand.nextInt(20) == 0) {
				TF2Util.addModifierSafe(this, SharedMonsterAttributes.MOVEMENT_SPEED, new AttributeModifier(fasterSpeed,
						"fastspeed", TF2Util.lerp(0.2f, 0.0f, this.getHealth() / this.getMaxHealth()), 2), false);
			}
			if (this.begin == 0) {
				this.setNoAI(false);
				this.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(BOSS_ARMOR_SPAWN);
			}
			if (this.getAttackTarget() != null && !(this.getAttackTarget() instanceof EntitySentry)
					&& this.getAttackTarget().getActivePotionEffect(TF2weapons.it) == null) {
				EntityLivingBase lastTarget = this.getAttackTarget().getLastAttackedEntity();
				this.setAttackTarget(null);
				if (lastTarget != null && lastTarget.getActivePotionEffect(TF2weapons.it) != null)
					this.setAttackTarget(lastTarget);
			}
			this.scareTick--;
			if (this.getAttackTarget() == null && this.ticksExisted % 5 == 0) {
				List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class,
						this.getEntityBoundingBox().grow(20, 10, 20),
						input -> (input instanceof EntityTF2Character || input instanceof EntityPlayer
								|| input == getRevengeTarget()) && getDistanceSq(input) < 600
								&& EntityAITarget.isSuitableTarget(EntityHHH.this, input, false, false));
				Collections.sort(list, new EntityAINearestAttackableTarget.Sorter(this));
				if (list.size() > 0)
					this.setAttackTarget(list.get(0));
				// list.get(0).addPotionEffect(new PotionEffect(TF2weapons.it,600));
				else if (this.getRevengeTarget() != null) {
					this.setAttackTarget(this.getRevengeTarget());
				}
				if (this.scareTick <= 0) {
					boolean one = false;
					List<EntityLivingBase> lists = this.world.getEntitiesWithinAABB(EntityLivingBase.class,
							this.getEntityBoundingBox().grow(10, 10, 10),
							input -> getDistanceSq(input) < 100 && !TF2Util.isOnSameTeam(EntityHHH.this, input)
									&& !(input.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()
											&& input.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Item
													.getItemFromBlock(Blocks.PUMPKIN)));
					if (lists.size() > 1) {
						for (EntityLivingBase living : lists) {
							one = true;
							TF2Util.stun(living, 80, false);
						}
					}
					if (one)
						this.playSound(TF2Sounds.MOB_HHH_ALERT, 1.75F, 1);
					this.scareTick = 200;
				}
			}
			if (this.getAttackTarget() != null) {
				if (!this.getAttackTarget().onGround)
					this.targetInAir += 1;
				if (this.level > 1) {
					if (this.dashCool-- <= 0) {
						this.dashCool = 300;
						this.dashTick = 35;
						this.dashMotion = this.getVectorForRotation(0, this.rotationYawHead).scale(0.45);
						TF2Util.addModifierSafe(this, SharedMonsterAttributes.MOVEMENT_SPEED,
								new AttributeModifier(slowDash, "dash", -0.5, 2), false);
					}
					if (this.dashTick-- > 0) {
						if (this.dashTick < 20) {
							this.motionX += dashMotion.x;
							this.motionY += dashMotion.y;
							this.motionZ += dashMotion.z;
						}
						if (this.dashTick < 12
								&& this.getDistanceSq(this.getAttackTarget()) < this.getAttackTarget().getDistanceSq(
										this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ))
							this.dashTick = 0;
					} else
						this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowDash);
				}

			}
			if (--this.teleportTime <= 0 && this.getAttackTarget() != null && this.ticksExisted % 5 == 0) {
				Path path = this.getNavigator().getPathToEntityLiving(this.getAttackTarget());
				boolean shouldTeleport = false;
				if (path == null)
					shouldTeleport = this.getDistanceSq(this.getAttackTarget()) > 1.5 && this
							.getDistanceSq(this.getAttackTarget().posX, this.posY, this.getAttackTarget().posZ) < 1;
				else {
					if (this.ticksExisted % 20 == 0) {
						if (this.lastPos.squareDistanceTo(this.getPositionVector()) < 2) {
							shouldTeleport = true;
						}
						this.lastPos = this.getPositionVector();
					}
					if (!shouldTeleport)
						shouldTeleport = Math.abs(path.getFinalPathPoint().y - this.getAttackTarget().posY) > 1.5
								&& this.getAttackTarget().onGround;
				}
				if (shouldTeleport) {
					this.toTeleportTime = 40;
					this.teleportTime = 250;
					this.setNoAI(true);
					this.setSneaking(true);
				}
			}
			/*
			 * if(--this.teleportTime<=0&&this.getAttackTarget()!=null&&(this.getNavigator()
			 * .noPath()||this.getDistanceSq(this.getAttackTarget())>1.5&&this.getDistanceSq
			 * (this.getAttackTarget().posX, this.posY, this.getAttackTarget().posZ)<0.75)){
			 *
			 * }
			 */
			if (this.toTeleportTime > 0) {
				if (this.world instanceof WorldServer) {
					WorldServer world = (WorldServer) this.world;
					world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY - 0.5, this.posZ, 8, 2.7, 0, 2.7,
							0, new int[0]);
				}
				if (--this.toTeleportTime == 0) {
					this.setSneaking(false);
					this.setNoAI(false);
					Vec3d pos = null;
					if (this.getAttackTarget() != null)
						pos = new Vec3d(this.getAttackTarget().posX, this.getAttackTarget().posY,
								this.getAttackTarget().posZ);
					/*
					 * else pos = RandomPositionGenerator.findRandomTarget(this, 16, 6);
					 */
					if (pos != null && this.attemptTeleportForce(pos.x, pos.y, pos.z)) {
						this.addPotionEffect(new PotionEffect(TF2weapons.stun, 30, 3));
						for (int i = 0; i < 40; i++) {
							Vec3d partpos = TF2Util.radiusRandom2D(2.7f, this.rand);
							this.world.spawnParticle(EnumParticleTypes.PORTAL, partpos.x + this.posX, this.posY - 0.5,
									partpos.y + this.posZ, 0, 0, 0, new int[0]);
						}
					}
				}
			}

			if (--this.attackTick <= 0) {
				List<EntityLivingBase> ents = this.world
						.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(3, 1, 3),
								input -> getDistanceSq(input) < 4 && !TF2Util.isOnSameTeam(EntityHHH.this, input)
										&& (TF2Util.lookingAt(EntityHHH.this, 30, input.posX,
												input.posY + input.getEyeHeight(), input.posZ)
												|| input.getCollisionBoundingBox() != null));
				if (!ents.isEmpty()) {
					this.swingArm(EnumHand.MAIN_HAND);
					this.attackTick = 20;
					for (EntityLivingBase living : ents) {
						this.attackEntityAsMob(living);
					}
				}
			}
		}

	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if (super.attackEntityFrom(source, amount)) {
			if (this.getAttackTarget() == null || this.getNavigator().noPath()) {
				this.damageTakenNoPath += amount;
				if (this.damageTakenNoPath >= 20 && this.teleportTime <= 50) {
					this.damageTakenNoPath = 0;
					this.toTeleportTime = 40;
					this.teleportTime = 250;
					this.setNoAI(true);
					this.setSneaking(true);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		if (super.attackEntityAsMob(entityIn)) {

			this.playSound(TF2Sounds.MOB_HHH_HIT, 1F, 1F);
			if (!entityIn.isEntityAlive())
				this.playSound(TF2Sounds.MOB_HHH_ATTACK, 1.75f, 1);
			return true;
		}
		return false;
	}

	@Override
	public AxisAlignedBB getBreakingBB() {
		if (this.getAttackTarget() != null) {
			AxisAlignedBB orig = this.getEntityBoundingBox();
			return new AxisAlignedBB(orig.minX - 0.5, orig.minY + 1, orig.minZ - 0.5, orig.maxX + 0.5, orig.maxY,
					orig.maxZ + 0.5);
		} else
			return super.getBreakingBB();
	}

	@Override
	public boolean breakBlocks() {
		boolean flag = super.breakBlocks();
		if (flag) {
			this.swingArm(EnumHand.MAIN_HAND);
			this.playSound(TF2Sounds.MOB_HHH_MISS, 1, 1);
		}
		return flag;
	}

	@Override
	public int getTalkInterval() {
		return 130;
	}

	@Override
	public void setDead() {
		if (this.getAttackTarget() != null) {
			this.getAttackTarget().removePotionEffect(TF2weapons.it);
		}
		super.setDead();
	}

	@Override
	public void setAttackTarget(EntityLivingBase ent) {
		if (this.getAttackTarget() != null && ent instanceof EntityBuilding)
			return;
		if (ent != null && ent.hasCapability(TF2weapons.WEAPONS_CAP, null)
				&& ent.getCapability(TF2weapons.WEAPONS_CAP, null).itProtection > 0)
			return;

		if (ent != this.getAttackTarget() && this.getAttackTarget() != null) {
			boolean hadeffect = this.getAttackTarget().getActivePotionEffect(TF2weapons.it) != null;
			this.getAttackTarget().removePotionEffect(TF2weapons.it);
			if (ent == null && hadeffect)
				this.getAttackTarget().addPotionEffect(new PotionEffect(TF2weapons.it, 12));
		}
		if (ent != null) {
			/*
			 * if(ent.getActivePotionEffect(TF2weapons.it)==null&&this.scareTick<=0){
			 * for(EntityLivingBase
			 * living:this.world.getEntitiesWithinAABB(EntityLivingBase.class,
			 * this.getEntityBoundingBox().grow(10, 10, 10), new
			 * Predicate<EntityLivingBase>(){
			 *
			 * @Override public boolean apply(EntityLivingBase input) { return
			 * getDistanceSq(input)<100 && !TF2weapons.isOnSameTeam(EntityHHH.this, input)
			 * && !(input.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null &&
			 * input.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()==Item.
			 * getItemFromBlock(Blocks.PUMPKIN)); }
			 *
			 * })){ TF2weapons.stun(living, 100, false); } this.scareTick=200; }
			 */
			this.targetTime = 300;
			ent.addPotionEffect(new PotionEffect(TF2weapons.it, 600));
			if (ent != this.getAttackTarget() && this.rand.nextBoolean()) {

			}
		}
		super.setAttackTarget(ent);
	}

	/*
	 * public void addAchievement(EntityPlayer player){
	 * super.addAchievement(player); player.addStat(TF2Achievements.HHH); }
	 */
	@Override
	public SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_HHH_SAY;
	}

	@Override
	public SoundEvent getDeathSound() {
		return TF2Sounds.MOB_HHH_DEFEAT;
	}

	@Override
	public SoundEvent getAppearSound() {
		return TF2Sounds.MOB_HHH_START;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setShort("Begin", (short) this.begin);
		nbt.setShort("Scare", (short) this.scareTick);
		nbt.setShort("Teleport", (short) this.teleportTime);
		nbt.setFloat("NoPathDamange", this.damageTakenNoPath);
		nbt.setShort("DashCool", (short) this.dashCool);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.damageTakenNoPath = nbt.getFloat("NoPathDamage");
		this.begin = nbt.getShort("Begin");
		this.scareTick = nbt.getShort("Scare");
		this.teleportTime = nbt.getShort("Teleport");
		this.dashCool = nbt.getShort("DashCool");
	}

	@Override
	public void returnSpawnItems() {
		this.entityDropItem(new ItemStack(TF2weapons.itemBossSpawn, 1, 0), 0);
	}

	public class EntityAIAirAttack extends EntityAIBase {

		public int ticksInAir;
		public int attackTime;
		public int attackCooldown;

		@Override
		public boolean shouldExecute() {
			return getAttackTarget() != null;
		}

		@Override
		public void updateTask() {
			if (getAttackTarget() == null)
				return;
			EntityLivingBase target = getAttackTarget();
			if (!target.onGround) {
				this.ticksInAir++;
				if (this.ticksInAir > 30) {
					this.attackTime = 60;
				}
			} else
				this.ticksInAir--;

			if (--this.attackTime > 0) {
				this.setMutexBits(3);
				this.ticksInAir = 0;
				EntityHHH.this.faceEntity(target, 45, 90);
				EntityHHH.this.getLookHelper().setLookPositionWithEntity(target, 90, 90);
				if (this.attackTime == 30) {
					ItemStack stack = ItemFromData.getNewStack("hhhaxe");
					try {
						EntityProjectileBase proj = MapList.projectileClasses
								.get(ItemFromData.getData(stack).getString(PropertyType.PROJECTILE))
								.getConstructor(World.class).newInstance(EntityHHH.this.world);
						proj.initProjectile(EntityHHH.this, EnumHand.MAIN_HAND, stack);
						double x = target.posX;
						double y = target.posY + target.getEyeHeight();
						double z = target.posZ;
						// float speed = TF2Attribute.getModifier("Proj Speed", stackW,
						// ItemFromData.getData(stackW).getFloat(PropertyType.PROJECTILE_SPEED),
						// living);
						proj.face(target, 1);
						EntityHHH.this.world.spawnEntity(proj);
					} catch (Exception e) {

					}
				}
			} else
				this.setMutexBits(0);
		}

	}
}
