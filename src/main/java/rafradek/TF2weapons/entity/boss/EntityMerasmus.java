package rafradek.TF2weapons.entity.boss;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.block.BlockProp;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.client.particle.EnumTF2Particles;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemProjectileWeapon;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.TF2Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityMerasmus extends EntityTF2Boss {

	private int begin = 30;
	private int teleportCooldown = 240;
	private int bombCooldown = 160;
	private int bombDuration;
	public boolean hidden;
	public int topBlock;
	public BlockPos hiddenBlock;
	private int hideCount;
	public ArrayList<BlockPos> usedPos = new ArrayList<>();
	private static final DataParameter<Boolean> SPELL_BOMB = EntityDataManager.createKey(EntityMerasmus.class,
			DataSerializers.BOOLEAN);

	public EntityMerasmus(World worldIn) {
		super(worldIn);
		this.setSize(1.15f, 3.5f);
		this.stepHeight = 1.05f;
		this.setNoAI(true);
		this.setHeldItem(EnumHand.MAIN_HAND, ItemFromData.getNewStack("mrsbomb"));
	}

	@Override
	public void entityInit() {
		super.entityInit();
		this.dataManager.register(SPELL_BOMB, false);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(4, new AIAttack(this));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));

		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class,
				3, false, false, input -> input instanceof EntityTF2Character || input instanceof EntityPlayer) {
			@Override
			protected double getTargetDistance() {
				return super.getTargetDistance() * 0.35;
			}

			@Override
			public boolean shouldExecute() {

				return super.shouldExecute();
			}
		});
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 5,
				false, false, input -> input instanceof EntityPlayer) {
			@Override
			public boolean shouldExecute() {
				boolean ex = super.shouldExecute();
				return ex;
			}
		});
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		// this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(256.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.098D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0D);

	}

	protected PathNavigate getNewNavigator(World worldIn) {
		return new PathNavigateClimber(this, worldIn);
	}

	@Override
	public float getEyeHeight() {
		return 2.1f;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		float prevHP = this.getHealth();
		if (super.attackEntityFrom(source, amount)) {
			float newHP = this.getHealth();
			if (this.isEntityAlive() && !this.isBombSpell()) {
				if (this.hideCount == 0 && newHP / this.getMaxHealth() <= 0.55f) {
					this.hide(true);
					this.hideCount = 1;
				} else if (this.hideCount == 1 && newHP / this.getMaxHealth() <= 0.1f) {
					this.hide(true);
					this.hideCount = 2;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onLivingUpdate() {
		if (this.getHeldItemMainhand().isEmpty() || !(this.getHeldItemMainhand().getItem() instanceof ItemWeapon))
			this.setHeldItem(EnumHand.MAIN_HAND, ItemFromData.getNewStack("mrsbomb"));

		super.onLivingUpdate();
		if (this.getActivePotionEffect(TF2weapons.stun) != null)
			this.rotationPitch = 90;
		if (this.begin-- > 20 && this.world.isRemote)
			for (int i = 0; i < 40; i++) {
				Vec3d pos = TF2Util.radiusRandom2D(2.2f, this.rand);
				this.world.spawnParticle(EnumParticleTypes.PORTAL, pos.x + this.posX, this.posY - 0.5,
						pos.y + this.posZ, 0, 0, 0, new int[0]);
			}

		if (!world.isRemote) {
			if (this.begin == 0) {
				this.setNoAI(false);
				this.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(BOSS_ARMOR_SPAWN);
			}
			this.bombDuration--;
			for (EntityLivingBase living : this.world.getEntitiesWithinAABB(EntityLivingBase.class,
					this.getEntityBoundingBox().grow(1.5, 1, 1.5), input -> input.getActivePotionEffect(TF2weapons.bombmrs) != null)) {
				living.removePotionEffect(TF2weapons.bombmrs);
				living.removePotionEffect(TF2weapons.stun);
				living.removePotionEffect(MobEffects.NAUSEA);
				this.playSound(TF2Sounds.MOB_MERASMUS_STUN, 1F, 1f);
				this.attackEntityFrom(new EntityDamageSource("magicb", living).setMagicDamage(), 15);
				this.addPotionEffect(new PotionEffect(TF2weapons.stun, 120, 3));
				this.teleportCooldown = 120;
			}
			if (!this.hidden) {

				if (this.ticksExisted % 5 == 0) {
					if (this.getAttackTarget() != null && !this.getEntitySenses().canSee(this.getAttackTarget())) {
						TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[39], 0.35f);
					} else
						TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[39], 0f);
				}

				if (this.bombCooldown-- <= 0) {
					List<EntityPlayer> list = this.world.getEntitiesWithinAABB(EntityPlayer.class,
							this.getEntityBoundingBox().grow(30, 15, 30), input -> getDistanceSq(input) < 900
									&& !TF2Util.isOnSameTeam(EntityMerasmus.this, input) && EntityAITarget
											.isSuitableTarget(EntityMerasmus.this, input, false, false));
					if (!list.isEmpty()) {
						EntityPlayer living = list.get(this.rand.nextInt(list.size()));
						living.addPotionEffect(new PotionEffect(TF2weapons.bombmrs, 300));
						TF2Util.stun(living, 300, false);
						((EntityPlayerMP) living).connection
								.sendPacket(new SPacketSoundEffect(TF2Sounds.MOB_MERASMUS_HEADBOMB,
										this.getSoundCategory(), living.posX, living.posY, living.posZ, 4F, 1f));
						// this.teleportCooldown=90;
					}
					this.bombCooldown = Math.max(680 - this.playersAttacked * 60, 480);
				}

				if (this.teleportCooldown-- <= 0 && !this.isBombSpell()) {
					this.teleport();
				}
				if (this.isBombSpell()) {
					float prevPitch = this.rotationPitch;
					float prevYaw = this.rotationYawHead;
					if (this.ticksExisted % 8 == 0) {
						for (int i = 0; i < 16; i++) {
							this.rotationPitch = -70 + this.rand.nextFloat() * 120f;
							this.rotationYawHead = i * 22.5f + this.rand.nextFloat() * 22.5f;
							((ItemProjectileWeapon) this.getHeldItemMainhand().getItem())
									.shoot(this.getHeldItemMainhand(), this, this.world, 0, EnumHand.MAIN_HAND);
						}
					}
					if (this.posY < topBlock) {
						this.motionY = 0.15f;
					} else {
						this.motionY = 0;
					}
					if (this.bombDuration == 200 || !this.getMoveHelper().isUpdating()) {

						Random random = this.getRNG();
						BlockPos pos = this.world.getTopSolidOrLiquidBlock(this.getPosition());
						double d0 = this.posX;
						double d2 = this.posZ;
						if (pos.getY() + 7 < this.posY) {
							d0 += (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
							d2 += (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
						}
						double d1 = pos.getY() + 7 + random.nextInt(3);
						this.getMoveHelper().setMoveTo(d0, this.topBlock, d2, 1.0D);

					}
					this.rotationPitch = prevPitch;
					this.rotationYawHead = prevYaw;
					if (this.bombDuration < 40) {
						this.setBombSpell(false);
					}
				}
			}

			if (this.hidden) {
				if (this.ticksExisted % 5 == 0)
					this.heal(this.getMaxHealth() * 0.001f);
				if (this.world.getBlockState(this.hiddenBlock).getBlock() != TF2weapons.blockProp) {
					this.hide(false);
				}
			}
		}

	}

	public void teleport() {
		this.teleportCooldown = 240;
		this.playSound(TF2Sounds.MOB_MERASMUS_DISAPPEAR, 1F, 1f);

		for (int i = 0; i < 10; i++) {
			double x;
			double z;
			if (this.getAttackTarget() != null) {
				x = this.getAttackTarget().posX + rand.nextDouble() * 40 - 20;
				z = this.getAttackTarget().posZ + rand.nextDouble() * 40 - 20;
			} else {
				x = this.posX + rand.nextDouble() * 40 - 20;
				z = this.posZ + rand.nextDouble() * 40 - 20;
			}
			double y = this.world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() + 3;
			if (this.attemptTeleport(x, y, z)) {
				this.playSound(TF2Sounds.MOB_MERASMUS_APPEAR, 1F, 1f);
				for (int j = 0; j < 40; j++) {
					Vec3d pos = TF2Util.radiusRandom3D(2.7f, this.rand);
					this.world.spawnParticle(EnumParticleTypes.PORTAL, pos.x + this.posX, pos.y + this.posY,
							pos.z + this.posZ, 0, 0, 0, new int[0]);
				}
				this.teleportCooldown += 200 + rand.nextInt(80);
				break;
			}
		}
	}

	public boolean isBombSpell() {
		return this.getDataManager().get(SPELL_BOMB);
	}

	public void setBombSpell(boolean bomb) {
		this.getDataManager().set(SPELL_BOMB, bomb);
		this.getNavigator().clearPath();
		if (bomb) {
			this.moveHelper = new FloatingMoveHelper(this);
			this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemFromData.getNewStack("bombinomicon"));
			this.playSound(TF2Sounds.MOB_MERASMUS_BOMBINOMICON, 3.3F, 1F);
		} else {
			this.moveHelper = new EntityMoveHelper(this);
			this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
		}
		TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[19], bomb ? 0.65f : 1);
		TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[39], bomb ? 0f : 0.3f);
	}

	@Override
	public void dropFewItems(boolean hit, int looting) {
		ItemStack hat = ItemFromData.getNewStack("merasmushat");
		hat.getTagCompound().setShort("BossLevel", (short) this.level);
		this.entityDropItem(hat, 0);
	}

	/*
	 * public void addAchievement(EntityPlayer player){
	 * super.addAchievement(player); player.addStat(TF2Achievements.MERASMUS); }
	 */
	@Override
	public SoundEvent getDeathSound() {
		return TF2Sounds.MOB_MERASMUS_DEFEAT;
	}

	@Override
	public SoundEvent getAppearSound() {
		return TF2Sounds.MOB_MERASMUS_START;
	}

	@Override
	public void setDead() {
		super.setDead();
		for (BlockPos pos : this.usedPos) {
			if (this.world.getBlockState(pos).getBlock() == TF2weapons.blockProp)
				this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setBoolean("Hidden", this.hidden);
		if (hidden) {
			nbt.setIntArray("HiddenPos",
					new int[] { this.hiddenBlock.getX(), this.hiddenBlock.getY(), this.hiddenBlock.getZ() });
			NBTTagList list = new NBTTagList();
			nbt.setTag("Props", list);
			for (BlockPos pos : this.usedPos)
				list.appendTag(new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
		}
		nbt.setShort("Begin", (short) this.begin);
		nbt.setShort("Teleport", (short) this.teleportCooldown);
		nbt.setShort("BombCooldown", (short) this.bombCooldown);
		nbt.setShort("BombDuration", (short) this.bombDuration);
		nbt.setShort("TopBlock", (short) this.topBlock);
		nbt.setByte("HideCount", (byte) this.hideCount);
		nbt.setBoolean("Bomb", this.isBombSpell());

	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.begin = nbt.getShort("Begin");
		this.setBombSpell(nbt.getBoolean("Bomb"));
		this.bombCooldown = nbt.getShort("BombCooldown");
		this.bombDuration = nbt.getShort("BombDuration");
		this.topBlock = nbt.getShort("TopBlock");
		this.teleportCooldown = nbt.getShort("Teleport");
		this.hidden = nbt.getBoolean("Hidden");
		this.hideCount = nbt.getByte("HideCount");
		if (hidden) {
			this.setNoAI(true);
			int[] pos = nbt.getIntArray("HiddenPos");
			this.hiddenBlock = new BlockPos(pos[0], pos[1], pos[2]);
			NBTTagList list = nbt.getTagList("Props", 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] arr = list.getIntArrayAt(i);
				this.usedPos.add(new BlockPos(arr[0], arr[1], arr[2]));
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void hide(boolean hide) {
		this.hidden = hide;
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		if (hide) {
			this.navigator.clearPath();
			// this.moveHelper=null;
			this.playSound(TF2Sounds.MOB_MERASMUS_HIDE, this.getSoundVolume(), 1F);
			this.setInvisible(true);
			int blockCount = (int) Math.min(100,
					10 * (0.7f + 0.3f * this.playersAttacked) * (0.9f + 0.1f * this.level));
			BlockPos initial = this.getPosition();
			for (int i = 0; i < blockCount; i++) {
				BlockPos pos = initial.add(
						this.rand.nextInt((int) (40 + blockCount * 0.3f)) - 20 - (int) (blockCount * 0.15f), 0,
						this.rand.nextInt((int) (40 + blockCount * 0.3f)) - 20 - (int) (blockCount * 0.15f));
				pos = this.world.getTopSolidOrLiquidBlock(pos);
				// pos=pos.add(0, 1, 0);
				if (this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos)) {
					this.world.setBlockState(pos, TF2weapons.blockProp
							.getStateFromMeta(this.rand.nextInt(BlockProp.EnumBlockType.values().length)));
				}
				if (i == 0)
					this.hiddenBlock = pos;
				this.setPositionAndUpdate(pos.getX(), -20, pos.getZ());
				this.usedPos.add(pos);
			}

		} else {
			this.setInvisible(false);
			for (BlockPos pos : this.usedPos) {
				if (this.world.getBlockState(pos).getBlock() == TF2weapons.blockProp)
					this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
			this.usedPos.clear();
			this.setPositionAndUpdate(this.hiddenBlock.getX() + 0.5, this.hiddenBlock.getY(),
					this.hiddenBlock.getZ() + 0.5);
			this.teleportCooldown = 20;
		}
		this.setNoAI(hide);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void travel(float strafe, float forward, float par3) {
		if (!this.isBombSpell() && !this.hidden) {
			super.travel(strafe, forward, par3);
			return;
		}
		if (this.isInWater()) {
			this.moveRelative(strafe, forward, par3, 0.02F);
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.800000011920929D;
			this.motionY *= 0.800000011920929D;
			this.motionZ *= 0.800000011920929D;
		} else if (this.isInLava()) {
			this.moveRelative(strafe, forward, par3, 0.02F);
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.5D;
			this.motionY *= 0.5D;
			this.motionZ *= 0.5D;
		} else {
			float f = 0.91F;

			if (this.onGround)
				f = this.world
						.getBlockState(new BlockPos(MathHelper.floor(this.posX),
								MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ)))
						.getBlock().slipperiness * 0.91F;

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(strafe, forward, par3, this.onGround ? 0.1F * f1 : 0.02F);
			f = 0.91F;

			if (this.onGround)
				f = this.world
						.getBlockState(new BlockPos(MathHelper.floor(this.posX),
								MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ)))
						.getBlock().slipperiness * 0.91F;

			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= f;
			this.motionY *= f;
			this.motionZ *= f;
		}

		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d1 = this.posX - this.prevPosX;
		double d0 = this.posZ - this.prevPosZ;
		float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

		if (f2 > 1.0F)
			f2 = 1.0F;

		this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
		this.limbSwing += this.limbSwingAmount;
	}

	public static class AIAttack extends EntityAIBase {

		public EntityMerasmus host;

		public int attacksMade;
		public int attackDuration;
		public boolean lastAttackMagic;
		public boolean liftup;

		public AIAttack(EntityMerasmus host) {
			this.host = host;
		}

		@Override
		public boolean shouldExecute() {
			return (this.host.getAttackTarget() != null && this.host.getActivePotionEffect(TF2weapons.stun) == null)
					|| host.envDamage > 0;
		}

		@Override
		public void updateTask() {
			EntityLivingBase target = this.host.getAttackTarget();
			if (target == null)
				target = this.host;
			World world = this.host.world;
			this.host.getLookHelper().setLookPositionWithEntity(target, 30F, 90F);
			if (attackDuration < 20) {
				this.host.getNavigator().tryMoveToEntityLiving(target, 1f);
			}
			if (--this.attackDuration <= 0) {
				if (host.envDamage > 0)
					host.envDamage -= 6;
				this.host.swingArm(EnumHand.MAIN_HAND);
				boolean sup = this.host.level > 1 && (this.attacksMade % 7 == 0 || (this.attacksMade - 1) % 7 == 0);
				if (this.attacksMade > 0 && this.attacksMade % 13 == 0) {
					this.host.setBombSpell(true);
					this.host.bombDuration = 200;
					this.host.teleport();
					BlockPos pos = this.host.world.getTopSolidOrLiquidBlock(this.host.getPosition());
					this.host.topBlock = pos.getY() + 7 + this.host.rand.nextInt(3);
					this.attackDuration = 200;
				} else if (this.attacksMade % 2 == 0) {
					this.attackDuration = 20 - this.host.level / 4;
					if (target != host && this.host.getDistanceSq(target) < 6) {
						if (this.host.attackEntityAsMob(target)) {
							target.knockBack(this.host, 1.5f, MathHelper.sin(this.host.rotationYaw * 0.017453292F),
									(-MathHelper.cos(this.host.rotationYaw * 0.017453292F)));
						}
					} else {
						this.host.faceEntity(target, 180, 90);
						((ItemProjectileWeapon) this.host.getHeldItemMainhand().getItem())
								.shoot(this.host.getHeldItemMainhand(), this.host, world, 0, EnumHand.MAIN_HAND);
						if (sup) {
							Vec3d right = this.host.getVectorForRotation(0, this.host.rotationYawHead + 90);
							this.host.rotationYawHead -= 24;
							for (int i = -2; i <= 2; i++) {
								this.host.posX += right.x * i;
								this.host.posZ += right.z * i;
								this.host.rotationYawHead += 12;
								((ItemProjectileWeapon) this.host.getHeldItemMainhand().getItem()).shoot(
										this.host.getHeldItemMainhand(), this.host, world, 0, EnumHand.MAIN_HAND);
							}
							this.host.rotationYawHead -= 24;
						}
					}
				} else {
					this.attackDuration = (int) (55 / (0.91 + this.host.level * 0.09f));
					this.host.getNavigator().clearPath();
					this.host.playSound(TF2Sounds.MOB_MERASMUS_SPELL, 2F, 1F);
					boolean attacked = false;
					double range = 10d + this.host.level * 0.8;
					for (EntityLivingBase living : world.getEntitiesWithinAABB(EntityLivingBase.class,
							this.host.getEntityBoundingBox().grow(range, range * 0.4, range),
							input -> input.getDistanceSq(host) < range * range
									&& !TF2Util.isOnSameTeam(host, input)
									&& EntityAITarget.isSuitableTarget(host, input, false, false))) {
						TF2Util.sendParticle(EnumTF2Particles.BULLET_TRACER, this.host, this.host.posX,
								this.host.posY + this.host.height / 2, this.host.posZ, living.posX,
								living.posY + living.height / 2, living.posZ, 1, 0, 0x60FF60, 10000);
						living.attackEntityFrom(
								new EntityDamageSource("magicm", this.host).setMagicDamage().setDifficultyScaled(),
								4.4f + this.host.level * 0.7f);
						living.addVelocity(0, 0.7, 0);
						living.onGround = false;
						if (living instanceof EntityPlayerMP)
							TF2Util.sendTracking(new TF2Message.VelocityAddMessage(new Vec3d(0, 0.7, 0), true), living);
						if (living.hasCapability(TF2weapons.WEAPONS_CAP, null))
							WeaponsCapability.get(living).setExpJump(true);
						else
							living.fallDistance = -10;
						attacked = true;
					}
					if (!attacked)
						this.host.teleportCooldown -= 20;
				}
				if (sup)
					this.attackDuration *= 0.35f;
				this.attacksMade++;
			}
		}
	}

	static class FloatingMoveHelper extends EntityMoveHelper {
		private final EntityMerasmus parentEntity;
		private int courseChangeCooldown;

		public FloatingMoveHelper(EntityMerasmus ghast) {
			super(ghast);
			this.parentEntity = ghast;
		}

		@Override
		public void onUpdateMoveHelper() {
			if (this.action == EntityMoveHelper.Action.MOVE_TO) {
				double d0 = this.posX - this.parentEntity.posX;
				double d1 = this.posY - this.parentEntity.posY;
				double d2 = this.posZ - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				if (this.courseChangeCooldown-- <= 0) {
					this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
					d3 = MathHelper.sqrt(d3);

					if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
						this.parentEntity.motionX += d0 / d3 * 0.05D;
						this.parentEntity.motionZ += d2 / d3 * 0.05D;
					} else
						this.action = EntityMoveHelper.Action.WAIT;
				}
			}
		}

		/**
		 * Checks if entity bounding box is not colliding with terrain
		 */
		private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
			double d0 = (x - this.parentEntity.posX) / p_179926_7_;
			double d1 = (y - this.parentEntity.posY) / p_179926_7_;
			double d2 = (z - this.parentEntity.posZ) / p_179926_7_;
			AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();

			for (int i = 1; i < p_179926_7_; ++i) {
				axisalignedbb = axisalignedbb.offset(d0, d1, d2);

				if (!this.parentEntity.world.getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty())
					return false;
			}

			return true;
		}
	}

	@Override
	public void setAttackTarget(EntityLivingBase ent) {
		if (this.getAttackTarget() != null && ent instanceof EntityBuilding)
			return;
		super.setAttackTarget(ent);
	}

	@Override
	public void returnSpawnItems() {
		if (!this.usedPos.isEmpty())
			this.setPosition(hiddenBlock.getX(), hiddenBlock.getY(), hiddenBlock.getZ());
		this.entityDropItem(new ItemStack(TF2weapons.itemBossSpawn, 1, 1), 0);
	}
}
