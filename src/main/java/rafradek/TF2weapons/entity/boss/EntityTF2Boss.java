package rafradek.TF2weapons.entity.boss;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemMinigun;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public abstract class EntityTF2Boss extends EntityMob implements IEntityTF2 {

	protected final BossInfoServer bossInfo = (new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE,
			BossInfo.Overlay.PROGRESS));
	public int level = 1;
	public int timeLeft = 2400;
	public HashSet<EntityPlayer> attackers = new HashSet<>();
	public HashSet<UUID> participateUUID = new HashSet<>();
	public int playersAttacked = 0;
	private int blockBreakCounter = 27;
	public BlockPos spawnPos;
	protected float envDamage;
	protected float healthForPlayer;
	public boolean summoned;
	public float desiredHealth = 0;
	protected static final UUID BOSS_ARMOR_SPAWN = UUID.fromString("2d4ac0f2-a8f5-4a13-b04d-67d97bd71320");

	public WeaponsCapability weaponCap;

	public EntityTF2Boss(World worldIn) {
		super(worldIn);
		if (!this.world.isRemote)
			this.setGlowing(true);
		this.inventoryHandsDropChances = new float[] { 0, 0 };
		this.weaponCap = new WeaponsCapability(this);
		this.weaponCap.setCanExpJump(false);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source == DamageSource.DROWN || source == DamageSource.LAVA || source == DamageSource.ON_FIRE)
			return false;
		if (source instanceof TF2DamageSource) {
			if (source.getTrueSource() == this)
				return false;
			if (((TF2DamageSource) source).getCritical() > 0) {
				amount *= 0.7f;
			}
			if (!((TF2DamageSource) source).getWeapon().isEmpty()
					&& ((TF2DamageSource) source).getWeapon().getItem() instanceof ItemMinigun)
				amount *= 0.36f;
		}
		if (source.getImmediateSource() instanceof EntitySentry
				&& !((EntitySentry) source.getImmediateSource()).fromPDA) {
			amount *= 0.3f;
		}
		if (source.getTrueSource() == null || WeaponsCapability.get(source.getTrueSource()) == null) {
			amount *= 0.5f;
		}
		if (super.attackEntityFrom(source, amount * (this.getMaxHealth() / this.desiredHealth))) {
			if (source.getTrueSource() != null && source.getTrueSource() instanceof EntityPlayer)
				this.attackers.add((EntityPlayer) source.getTrueSource());
			if (!(source.getTrueSource() instanceof EntityLivingBase))
				this.envDamage += amount;
			return true;
		}
		return false;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		super.fall(distance, 0);
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {

	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	protected float getWaterSlowDown() {
		return 1F;
	}

	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
		int count = this.playersAttacked;
		if (count > 4) {
			count = 2 + this.playersAttacked / 2;
		}
		for (int i = 0; i < count; i++) {
			if (i > 0)
				this.dropFewItems(wasRecentlyHit, lootingModifier);
			this.entityDropItem(new ItemStack(TF2weapons.itemTF2, this.level / 2 + MathHelper.log2(this.level) + 2, 2),
					0);
			ItemStack weapon = ItemFromData.getRandomWeapon(this.rand, ItemFromData.VISIBLE_WEAPON);
			if (this.level > 1)
				TF2Attribute.upgradeItemStack(weapon, 40 + (this.level - 2) * 55 + MathHelper.log2(this.level) * 40,
						rand);
			this.entityDropItem(weapon, 0);
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.timeLeft--;
		if (!this.world.isRemote) {
			if (this.ticksExisted % 20 == 0) {
				for (EntityLivingBase living : this.world.getEntitiesWithinAABB(EntityLivingBase.class,
						this.getEntityBoundingBox().grow(100, 28, 100),
						input -> input.hasCapability(TF2weapons.WEAPONS_CAP, null)
								&& (TF2Util.getOwnerIfOwnable(input) instanceof EntityPlayer
										|| input.getDistanceSq(EntityTF2Boss.this) < 900))) {

					if (!this.participateUUID.contains(living.getUniqueID())) {
						this.participateUUID.add(living.getUniqueID());
						this.desiredHealth += this.healthForPlayer;

						if (living instanceof EntityPlayer) {
							this.desiredHealth += this.healthForPlayer * 3;
						}
						float per = this.getHealth() / this.getMaxHealth();

						this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
								.setBaseValue(Math.min(1000, desiredHealth));

						this.setHealth(per * this.getMaxHealth());

					}
				}
			}
			if (this.getAttackTarget() != null && !this.getAttackTarget().isEntityAlive()) {
				this.setAttackTarget(null);
			}
			if (timeLeft == 2250)
				this.setGlowing(false);
			if (timeLeft == 1200)
				this.playSound(TF2Sounds.MOB_BOSS_ESCAPE_60, 4F, 1f);
			else if (timeLeft == 200)
				this.playSound(TF2Sounds.MOB_BOSS_ESCAPE_10, 4F, 1f);
			else if (timeLeft <= 0) {
				this.playSound(TF2Sounds.MOB_BOSS_ESCAPE, 4F, 1f);
				if (this.summoned)
					this.returnSpawnItems();
				this.setDead();
			}
			this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
			--this.blockBreakCounter;

			if (this.blockBreakCounter <= 0 && this.world.getGameRules().getBoolean("mobGriefing")) {
				this.blockBreakCounter = 27;
				breakBlocks();
			}
		}

	}

	public void returnSpawnItems() {

	}

	public boolean breakBlocks() {
		return this.breakBlocks(this.getBreakingBB());
	}

	public boolean breakBlocks(AxisAlignedBB box) {
		boolean flag = false;
		for (int x = MathHelper.floor(box.minX); x <= MathHelper.floor(box.maxX); ++x)
			for (int y = MathHelper.floor(box.minY); y <= MathHelper.floor(box.maxY); ++y)
				for (int z = MathHelper.floor(box.minZ); z <= MathHelper.floor(box.maxZ); ++z) {
					BlockPos blockpos = new BlockPos(x, y, z);
					IBlockState iblockstate = this.world.getBlockState(blockpos);
					Block block = iblockstate.getBlock();

					if (!block.isAir(iblockstate, this.world, blockpos) && !iblockstate.getMaterial().isLiquid()
							&& EntityWither.canDestroyBlock(block)
							&& block.canEntityDestroy(iblockstate, world, blockpos, this))
						flag = this.world.destroyBlock(blockpos, true) || flag;
				}

		if (flag)
			this.world.playEvent((EntityPlayer) null, 1022, new BlockPos(this), 0);
		return flag;
	}

	@Override
	public float getSoundVolume() {
		return TF2ConfigVars.bossVolume;
	}

	public AxisAlignedBB getBreakingBB() {
		return this.getEntityBoundingBox();
	}

	@Override
	public boolean isNonBoss() {
		return false;
	}

	public SoundEvent getAppearSound() {
		return null;
	}

	@Override
	public Team getTeam() {
		return this.world.getScoreboard().getTeam("TF2Bosses");
	}

	@Override
	public void setFire(int time) {
		super.setFire(1);
	}

	@Override
	public void travel(float m1, float m2, float m3) {
		float move = this.getAIMoveSpeed();
		super.travel(m1 / move, m2, m3 / move);
	}

	public boolean attemptTeleportForce(double x, double y, double z) {
		this.breakBlocks(this.getEntityBoundingBox().offset(x - this.posX, y - this.posY, z - this.posZ));
		double d0 = this.posX;
		double d1 = this.posY;
		double d2 = this.posZ;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		boolean flag = false;
		BlockPos blockpos = new BlockPos(this);
		World world = this.world;
		Random random = this.getRNG();

		if (world.isBlockLoaded(blockpos)) {
			boolean flag1 = false;

			while (!flag1 && blockpos.getY() > 0) {
				BlockPos blockpos1 = blockpos.down();
				IBlockState iblockstate = world.getBlockState(blockpos1);

				if (iblockstate.getMaterial().blocksMovement()) {
					flag1 = true;
				} else {
					--this.posY;
					blockpos = blockpos1;
				}
			}

			if (flag1) {
				this.setPositionAndUpdate(this.posX, this.posY, this.posZ);

				if (world.getCollisionBoxes(null, this.getEntityBoundingBox()).isEmpty()
						&& !world.containsAnyLiquid(this.getEntityBoundingBox())) {
					flag = true;
				}
			}
		}

		if (!flag) {
			this.setPositionAndUpdate(d0, d1, d2);
			return false;
		} else {
			int i = 128;

			for (int j = 0; j < 128; ++j) {
				double d6 = j / 127.0D;
				float f = (random.nextFloat() - 0.5F) * 0.2F;
				float f1 = (random.nextFloat() - 0.5F) * 0.2F;
				float f2 = (random.nextFloat() - 0.5F) * 0.2F;
				double d3 = d0 + (this.posX - d0) * d6 + (random.nextDouble() - 0.5D) * this.width * 2.0D;
				double d4 = d1 + (this.posY - d1) * d6 + random.nextDouble() * this.height;
				double d5 = d2 + (this.posZ - d2) * d6 + (random.nextDouble() - 0.5D) * this.width * 2.0D;
				world.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, f, f1, f2);
			}

			if (this instanceof EntityCreature) {
				((EntityCreature) this).getNavigator().clearPath();
			}

			return true;
		}
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance diff, IEntityLivingData p_110161_1_) {
		int players = 0;
		int highestLevel = 0;
		int statmult = 0;
		this.spawnPos = this.getPosition();
		for (EntityLivingBase living : this.world.getEntitiesWithinAABB(EntityLivingBase.class,
				this.getEntityBoundingBox().grow(100, 28, 100),
				input -> input.hasCapability(TF2weapons.WEAPONS_CAP, null)
						&& (TF2Util.getOwnerIfOwnable(input) instanceof EntityPlayer
								|| input.getDistanceSq(EntityTF2Boss.this) < 900))) {

			statmult++;
			this.participateUUID.add(living.getUniqueID());
			if (living instanceof EntityPlayer) {
				players++;
				statmult += 3;
				EntityPlayer player = (EntityPlayer) living;
				if (player.getCapability(TF2weapons.PLAYER_CAP, null).highestBossLevel.get(this.getClass()) == null) {
					player.getCapability(TF2weapons.PLAYER_CAP, null).highestBossLevel.put(this.getClass(), (short) 0);
				}
				int level = player.getCapability(TF2weapons.PLAYER_CAP, null).highestBossLevel.get(this.getClass());
				if (level > highestLevel)
					highestLevel = level;
				player.sendMessage(new TextComponentTranslation("tf2boss.appear",
						new Object[] { this.getDisplayName(), Math.min(30, highestLevel + 1) }));
			}
		}
		highestLevel++;
		this.level = Math.min(30, highestLevel);
		// System.out.println("Level: " + this.level + " player: " + players);
		this.healthForPlayer = (float) (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue()
				* 0.1f * (0.5f + highestLevel * 0.5f));
		float desiredHealth = (float) this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue()
				* (0.6f + statmult * 0.1f) * (0.5f + highestLevel * 0.5f);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Math.min(1000, desiredHealth));
		this.desiredHealth = desiredHealth;
		this.setHealth(this.getMaxHealth());
		TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[19],
				1 * (0.7f + highestLevel * 0.3f));
		this.experienceValue = (int) (200 * (0.5f + players * 0.5f) * (0.35f + highestLevel * 0.65f));
		this.playersAttacked = players;
		this.playSound(this.getAppearSound(), 4F, 1);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR)
				.applyModifier(new AttributeModifier(EntityTF2Boss.BOSS_ARMOR_SPAWN, "armorspawn", 20, 0));
		return p_110161_1_;
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		for (EntityPlayer player : this.attackers) {
			int level = player.getCapability(TF2weapons.PLAYER_CAP, null).highestBossLevel.get(this.getClass()) != null
					? player.getCapability(TF2weapons.PLAYER_CAP, null).highestBossLevel.get(this.getClass())
					: 0;
			if (this.level > level)
				player.getCapability(TF2weapons.PLAYER_CAP, null).highestBossLevel.put(this.getClass(),
						(short) this.level);
			player.sendMessage(
					new TextComponentTranslation("tf2boss.death", new Object[] { this.getDisplayName(), this.level }));
		}
	}

	/*
	 * public void addAchievement(EntityPlayer player){ if(this.level>=30)
	 * player.addStat(TF2Achievements.BOSS_30_LVL); }
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setShort("Level", (short) this.level);
		nbt.setShort("Players", (short) this.playersAttacked);
		nbt.setShort("TimeLeft", (short) this.timeLeft);
		nbt.setFloat("DamageMult", this.desiredHealth);
		nbt.setBoolean("Summoned", this.summoned);
		nbt.setFloat("HealthForPlayer", this.healthForPlayer);

		NBTTagList playerleast = new NBTTagList();
		NBTTagList playermost = new NBTTagList();
		for (EntityPlayer player : this.attackers) {
			playerleast.appendTag(new NBTTagLong(player.getUniqueID().getLeastSignificantBits()));
			playermost.appendTag(new NBTTagLong(player.getUniqueID().getMostSignificantBits()));
		}
		nbt.setTag("PlayerLeast", playerleast);
		nbt.setTag("PlayerMost", playermost);

		NBTTagList participateleast = new NBTTagList();
		NBTTagList participatemost = new NBTTagList();
		for (UUID participate : this.participateUUID) {
			participateleast.appendTag(new NBTTagLong(participate.getLeastSignificantBits()));
			participatemost.appendTag(new NBTTagLong(participate.getMostSignificantBits()));
		}
		nbt.setTag("ParticipateLeast", participateleast);
		nbt.setTag("ParticipateMost", participatemost);

		if (this.spawnPos != null)
			nbt.setIntArray("SpawnPos", new int[] { this.spawnPos.getX(), this.spawnPos.getY(), this.spawnPos.getZ() });
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		level = nbt.getShort("Level");
		this.playersAttacked = nbt.getShort("Players");
		this.timeLeft = nbt.getShort("TimeLeft");
		this.desiredHealth = nbt.getFloat("DamageMult");
		this.summoned = nbt.getBoolean("Summoned");
		this.healthForPlayer = nbt.getFloat("HealthForPlayer");

		NBTTagList playerleast = nbt.getTagList("PlayerLeast", 4);
		NBTTagList playermost = nbt.getTagList("PlayerMost", 4);
		for (int i = 0; i < playerleast.tagCount(); i++) {
			this.attackers.add(this.world.getPlayerEntityByUUID(
					new UUID(((NBTTagLong) playermost.get(i)).getLong(), ((NBTTagLong) playerleast.get(i)).getLong())));
		}
		NBTTagList participateleast = nbt.getTagList("ParticipateLeast", 4);
		NBTTagList participatemost = nbt.getTagList("ParticipateMost", 4);
		for (int i = 0; i < participateleast.tagCount(); i++) {
			this.participateUUID.add(new UUID(((NBTTagLong) participatemost.get(i)).getLong(),
					((NBTTagLong) participateleast.get(i)).getLong()));
		}

		if (this.timeLeft < 2250)
			this.setGlowing(false);
		if (nbt.hasKey("SpawnPos")) {
			int[] arr = nbt.getIntArray("SpawnPos");
			this.spawnPos = new BlockPos(arr[0], arr[1], arr[2]);
		}
	}

	@Override
	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	@Override
	public boolean isPotionApplicable(PotionEffect potioneffectIn) {
		return potioneffectIn.getPotion() == TF2weapons.stun && potioneffectIn.getAmplifier() >= 3;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
		if (capability == TF2weapons.WEAPONS_CAP) {
			return (T) this.weaponCap;
		}
		/*
		 * else if (capability == TF2weapons.INVENTORY_CAP){ return (T)
		 * this.wearablesCap; }
		 */
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing) {
		return capability == TF2weapons.WEAPONS_CAP
				/* || capability == TF2weapons.INVENTORY_CAP */ || super.hasCapability(capability, facing);
	}

	@Override
	public boolean hasHead() {
		return true;
	}

	@Override
	public AxisAlignedBB getHeadBox() {
		return null;
	}

	@Override
	public boolean hasDamageFalloff() {
		return false;
	}

	@Override
	public boolean isBuilding() {
		return false;
	}

	@Override
	public boolean isBackStabbable(EntityLivingBase attacker, ItemStack knife) {
		return true;
	}
}