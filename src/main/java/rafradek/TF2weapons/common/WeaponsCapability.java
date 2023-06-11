package rafradek.TF2weapons.common;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.client.renderer.entity.RenderTF2Character;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.mercenary.EntityEngineer;
import rafradek.TF2weapons.entity.mercenary.EntityMedic;
import rafradek.TF2weapons.entity.mercenary.EntityScout;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityGrapplingHook;
import rafradek.TF2weapons.entity.projectile.EntityStickybomb;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.potion.PotionTF2;
import rafradek.TF2weapons.potion.PotionTF2Item;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WeaponsCapability implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

	public static final int MAX_METAL = 200;
	public static final int MAX_METAL_ENGINEER = 500;
	public static final int PLAYER_MINCOOL = -200;

	public EntityLivingBase owner;
	public int state;
	public int minigunTicks;
	public EnumHand reloadingHand;
	public int reloadCool;
	public int lastFire;
	// public int critTime;
	// public int healTarget = -1;
	public boolean mainHand;
	public HashMap<String, Integer> effectsCool = new HashMap<>();
	public int chargeTicks;
	// public boolean charging;
	public int critTimeCool;
	@SuppressWarnings("unchecked")
	public Deque<TF2Message.PredictionMessage>[] predictionList = new Deque[8];
	public float recoil;
	public int invisTicks;
	public int disguiseTicks;
	public boolean pressedStart;
	public int airJumps;
	public EntitySentry controlledSentry;
	public ResourceLocation skinDisguise;
	public boolean skinRetrieved;
	public String lastDisguiseValue;
	public boolean lastDisgused;
	public String skinType;
	// public int zombieHuntTicks;
	public int ticksBash;
	public boolean bashCritical;
	// public int collectedHeads;
	public int collectedHeadsTime;
	public boolean wornEye;
	public int killsSpinning;
	// public int cratesOpened;
	// public float dodgedDmg;
	public int tickAirblasted;
	// public int killsAirborne;
	public int itProtection;
	public int killsAirborne;
	public int focusShotTicks;
	public int focusShotRemaining;
	public int fanCool;
	public boolean appliedMouseSlow;
	public int hitNoMiss;
	public int sentryTargets = 5;
	public boolean dispenserPlayer;
	public boolean teleporterPlayer;
	public boolean teleporterEntity;
	public boolean forcedClass;
	public float lastHitCharge;
	public EntityDataManager dataManager;

	public EntityLivingBase entityDisguise;

	public ArrayList<EntityStickybomb> activeBomb = new ArrayList<>();
	public float oldFactor;
	public int expJumpGround;
	public double lastPosX;
	public double lastPosY;
	public double lastPosZ;
	public ItemStack lastWeapon = ItemStack.EMPTY;
	public long ticksTotal;
	public boolean fireCoolReduced;
	public boolean autoFire;
	public EntityLivingBase lastAttacked;
	public boolean stabbedDisguise;
	private boolean canExpJump = true;

	public double gravity = -0.08;
	public float maxmetal = 1;
	public float damageArmorMin;

	public EnumMap<EnumHand, ItemStack> stackActive = new EnumMap<>(EnumHand.class);

	private float[] rageDrain = new float[RageType.values().length];
	public int disguiseCounter;

	private EntityGrapplingHook grapplingHook;

	private static final DataParameter<Boolean> EXP_JUMP = new DataParameter<>(6, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> CHARGING = new DataParameter<>(11, DataSerializers.BOOLEAN);
	private static final DataParameter<String> DISGUISE_TYPE = new DataParameter<>(7, DataSerializers.STRING);
	private static final DataParameter<Boolean> DISGUISED = new DataParameter<>(8, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> INVIS = new DataParameter<>(9, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> FEIGN = new DataParameter<>(10, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> CRIT_TIME = new DataParameter<>(0, DataSerializers.VARINT);
	private static final DataParameter<Integer> HEADS = new DataParameter<>(1, DataSerializers.VARINT);
	private static final DataParameter<Integer> HEAL_TARGET = new DataParameter<>(2, DataSerializers.VARINT);
	private static final DataParameter<Integer> METAL = new DataParameter<>(3, DataSerializers.VARINT);
	private static final DataParameter<Integer> TOKEN_USED = new DataParameter<>(12, DataSerializers.VARINT);
	private static final DataParameter<Byte> CAN_FIRE = new DataParameter<>(13, DataSerializers.BYTE);
	private static final DataParameter<Float> UBER_VIEW = new DataParameter<>(14, DataSerializers.FLOAT);
	private static final DataParameter<Boolean> GRAPPLING = new DataParameter<>(15, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> GRAPPLED = new DataParameter<>(16, DataSerializers.BOOLEAN);
	private static final EnumMap<RageType, DataParameter<Float>> RAGE = new EnumMap<>(RageType.class);
	private static final EnumMap<RageType, DataParameter<Boolean>> RAGE_ACTIVE = new EnumMap<>(RageType.class);
	public static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES,
			new LinkedBlockingQueue<Runnable>());

	// public int killsSpinning;

	/*
	 * public HashMap<Class<? extends Entity>, Short> highestBossLevel = new
	 * HashMap<>(); public int nextBossTicks; public int stickybombKilled; public
	 * boolean engineerKilled; public boolean sentryKilled; public boolean
	 * dispenserKilled; public int[] cachedAmmoCount=new int[15]; public int
	 * headshotsRow; public int sapperTime; public EntityLivingBase
	 * buildingOwnerKill;
	 */

	public WeaponsCapability(EntityLivingBase entity) {
		this.owner = entity;

		for (int i = 0; i < this.predictionList.length; i++)
			this.predictionList[i] = new ArrayDeque<>();
		this.dataManager = new EntityDataManager(entity);
		this.dataManager.register(CRIT_TIME, 0);
		this.dataManager.register(HEADS, 0);
		this.dataManager.register(HEAL_TARGET, -1);
		this.dataManager.register(METAL, MAX_METAL);
		this.dataManager.register(FEIGN, false);
		this.dataManager.register(INVIS, false);
		this.dataManager.register(DISGUISED, false);
		this.dataManager.register(DISGUISE_TYPE, "");
		this.dataManager.register(EXP_JUMP, false);
		this.dataManager.register(CHARGING, false);
		this.dataManager.register(TOKEN_USED, -1);
		this.dataManager.register(CAN_FIRE, (byte) 0);
		this.dataManager.register(UBER_VIEW, 0f);
		this.dataManager.register(GRAPPLED, false);
		this.dataManager.register(GRAPPLING, false);
		for (RageType type : RageType.values()) {
			this.dataManager.register(RAGE.get(type), 0f);
			this.dataManager.register(RAGE_ACTIVE.get(type), false);
		}
		// this.nextBossTicks = (int) (entity.world.getWorldTime() +
		// entity.getRNG().nextInt(360000));
	}

	public int getCritTime() {
		return this.dataManager.get(CRIT_TIME);
	}

	public void setCritTime(int time) {
		this.dataManager.set(CRIT_TIME, time);
	}

	public int getHealTarget() {
		return this.dataManager.get(HEAL_TARGET);
	}

	public void setHealTarget(int target) {
		this.dataManager.set(HEAL_TARGET, target);
	}

	public float getUberView() {
		return this.dataManager.get(UBER_VIEW);
	}

	public void setUberView(float uber) {
		this.dataManager.set(UBER_VIEW, uber);
	}

	public int getHeads() {
		return this.dataManager.get(HEADS);
	}

	public int getMetal() {
		return this.dataManager.get(METAL);
	}

	public boolean hasMetal(int metal) {
		boolean hasIngot = this.owner instanceof EntityPlayer && TF2Util.hasEnoughItem(
				((EntityPlayer) this.owner).inventory, stackL -> stackL.getItem() == Items.IRON_INGOT,
				MathHelper.ceil((metal - this.getMetal()) / 50f));
		return hasIngot || this.getMetal() >= metal;
	}

	public int consumeMetal(int metal, boolean allowPartial) {
		int usedMetal = 0;

		if (this.owner instanceof EntityPlayer) {
			ItemStack ingot = new ItemStack(Items.IRON_INGOT);
			while (((allowPartial && this.getMetal() == 0) || (!allowPartial && this.getMetal() < metal))
					&& ((EntityPlayer) this.owner).inventory.hasItemStack(ingot)) {
				((EntityPlayer) this.owner).inventory.clearMatchingItems(Items.IRON_INGOT, 0, 1, null);
				this.setMetal(this.getMetal() + 50);
			}
		}
		if (allowPartial || this.getMetal() >= metal) {
			usedMetal = Math.min(this.getMetal(), metal);
			this.setMetal(this.getMetal() - usedMetal);
		}
		return usedMetal;
	}

	public void setMetal(int metal) {
		this.dataManager.set(METAL, MathHelper.clamp(metal, 0, this.getMaxMetal()));
	}

	public void giveMetal(int metal) {
		this.dataManager.set(METAL, MathHelper.clamp(this.getMetal() + metal, 0, this.getMaxMetal()));
	}

	public int getMaxMetal() {
		return (int) ((this.owner instanceof EntityEngineer ? TF2ConfigVars.maxMetalEngineer
				: WeaponsCapability.MAX_METAL) * this.maxmetal);
	}

	public float getRage(RageType type) {
		if (type == null)
			return 0;
		return this.dataManager.get(RAGE.get(type));
	}

	public void setRage(RageType type, float rage) {
		this.dataManager.set(RAGE.get(type), rage);
	}

	public boolean isRageActive(RageType type) {
		if (type == null)
			return false;
		return this.dataManager.get(RAGE_ACTIVE.get(type));
	}

	public void setRageActive(RageType type, boolean active, float drain) {

		this.dataManager.set(RAGE_ACTIVE.get(type), active);
		if (active)
			this.rageDrain[type.ordinal()] = drain * 0.1f;
		else
			this.rageDrain[type.ordinal()] = 0f;
	}

	/*
	 * public float getPhlogRage() { return this.dataManager.get(PHLOG_RAGE); }
	 * 
	 * public void setPhlogRage(float rage) { this.dataManager.set(PHLOG_RAGE,
	 * rage); }
	 * 
	 * public float getKnockbackRage() { return
	 * this.dataManager.get(KNOCKBACK_RAGE); }
	 * 
	 * public void setKnockbackRage(float rage) {
	 * this.dataManager.set(KNOCKBACK_RAGE, rage); }
	 */
	public boolean isInvisible() {
		return this.dataManager.get(INVIS);
	}

	public void setInvisible(boolean invis) {
		this.dataManager.set(INVIS, invis);
	}

	public void setDisguised(boolean val) {
		this.dataManager.set(DISGUISED, val);
		this.disguiseTicks = 0;
		if (!val) {
			this.stabbedDisguise = false;
		}
	}

	public boolean isDisguised() {
		return this.dataManager.get(DISGUISED);
	}

	public void setExpJump(boolean val) {
		this.dataManager.set(EXP_JUMP, val);
		if (val && !this.owner.world.isRemote) {
			this.expJumpGround = 2;
			TF2Util.sendTracking(new TF2Message.ActionMessage(28, this.owner), this.owner);
		}
	}

	public boolean isExpJump() {
		return this.dataManager.get(EXP_JUMP);
	}

	public void setDisguiseType(String val) {
		this.dataManager.set(DISGUISE_TYPE, val);
	}

	public String getDisguiseType() {
		return this.dataManager.get(DISGUISE_TYPE);
	}

	public void setFeign(boolean val) {
		this.dataManager.set(FEIGN, val);
	}

	public boolean isFeign() {
		return this.dataManager.get(FEIGN);
	}

	public void setCharging(boolean val) {
		this.dataManager.set(CHARGING, val);
		if (!val)
			this.chargeTicks = 0;
	}

	public boolean isCharging() {
		return this.dataManager.get(CHARGING);
	}

	public void setUsedToken(int val) {
		this.dataManager.set(TOKEN_USED, val);
	}

	public int getUsedToken() {
		return this.dataManager.get(TOKEN_USED);
	}

	public boolean canFire(EnumHand hand, boolean primary) {
		int flags = this.dataManager.get(CAN_FIRE);
		return (flags & (1 << hand.ordinal())) == (1 << hand.ordinal())
				&& (((flags & 4) == 4 && primary) || ((flags & 8) == 8 && !primary));

	}

	public void setCanFire(boolean fire, EnumHand hand, boolean primary) {
		int flag = 0;
		if (hand == EnumHand.MAIN_HAND)
			flag += 1;
		else
			flag += 2;
		if (primary)
			flag += 4;
		else
			flag += 8;
		if (fire)
			this.dataManager.set(CAN_FIRE, (byte) (this.dataManager.get(CAN_FIRE) | flag));
		else
			this.dataManager.set(CAN_FIRE, (byte) (this.dataManager.get(CAN_FIRE) & ~(flag)));
	}

	public void addEffectCooldown(String name, int time) {
		this.effectsCool.put(name, time);
		if (this.owner instanceof EntityPlayerMP)
			TF2weapons.network.sendTo(new TF2Message.EffectCooldownMessage(name, time), (EntityPlayerMP) this.owner);
	}

	public void addHead(ItemStack weapon) {

		this.collectedHeadsTime = owner.ticksExisted;
		this.dataManager.set(HEADS, this.dataManager.get(HEADS) + 1);
		/*
		 * this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.
		 * MAX_HEALTH) .applyModifier(new AttributeModifier(HEADS_HEALTH,
		 * "Heads modifier", collectedHeads, 0));
		 * this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.
		 * MOVEMENT_SPEED) .applyModifier(new AttributeModifier(HEADS_SPEED,
		 * "Heads modifier", collectedHeads * 0.04, 2));
		 */
		this.owner.heal(TF2Attribute.getModifier("Max Health Kill", weapon, 0, this.owner));
	}

	public boolean focusedShot(ItemStack stack) {
		int stackLevel = (int) TF2Attribute.getModifier("Focus", stack, 0, owner);
		return stackLevel > 0 && this.focusShotTicks > this.focusShotTime(stack);
	}

	public int focusShotTime(ItemStack stack) {
		int stackLevel = (int) TF2Attribute.getModifier("Focus", stack, 0, owner);
		return (68 - stackLevel * 21 + ((ItemUsable) stack.getItem()).getFiringSpeed(stack, owner) / 50) * 2;
	}

	public void onChangeValue(DataParameter<?> param, Object newValue) {
		if (param.getId() == 11 && !((Boolean) newValue)) {
			this.chargeTicks = 0;
		}
		if (param.getId() == 6 && (Boolean) newValue)
			this.expJumpGround = 2;
	}

	public boolean isUsingParachute() {
		ItemStack stack = this.owner.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		return stack.getItem() instanceof ItemParachute && stack.getTagCompound().getBoolean("Deployed");
	}

	public void tick() {
		// System.out.println("graczin"+state);

		Iterator<Entry<String, Integer>> efiterator = effectsCool.entrySet().iterator();
		while (efiterator.hasNext()) {
			Entry<String, Integer> entry = efiterator.next();
			entry.setValue(entry.getValue() - 1);
			if (entry.getValue() <= 0)
				efiterator.remove();
		}
		if (!this.owner.world.isRemote && TF2ConfigVars.headCountDrop && this.dataManager.get(HEADS) > 0
				&& collectedHeadsTime < this.owner.ticksExisted
						- Math.max(100, 2000 - MathHelper.log2(this.dataManager.get(HEADS)) * 300)) {
			this.dataManager.set(HEADS, this.dataManager.get(HEADS) - 1);
			collectedHeadsTime = this.owner.ticksExisted;

			/*
			 * this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.
			 * MAX_HEALTH) .applyModifier(new AttributeModifier(HEADS_HEALTH,
			 * "Heads modifier", collectedHeads, 0));
			 * this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.
			 * MOVEMENT_SPEED) .applyModifier(new AttributeModifier(HEADS_SPEED,
			 * "Heads modifier", collectedHeads * 0.04, 2));
			 */
		}

		/*
		 * if(itemProperties.get(client).get(player)==null){
		 * itemProperties.get(client).put(player, new NBTTagCompound()); }
		 */
		// player.getEntityData().setTag("TF2", tag);
		// this.zombieHuntTicks--;

		this.ticksBash--;
		this.itProtection--;
		this.fanCool--;
		this.ticksTotal++;
		this.lastFire -= 50;
		if (this.airJumps > 0 && this.owner.onGround) {
			this.airJumps = 0;
		}

		if (this.owner.isSprinting() && this.getUsedToken() >= 0) {
			if (this.owner.onGround) {
				owner.motionX *= 0.82D;
				owner.motionZ *= 0.82D;
			}
			// owner.setSprinting(false);
		}

		if (!this.owner.world.isRemote) {
			if (this.owner.ticksExisted % 20 == 0 && (this.owner instanceof EntityMedic || this.getUsedToken() == 6)) {
				int lastHitTime = this.owner.ticksExisted - this.owner.getEntityData().getInteger("lasthit");
				if (lastHitTime >= 120)
					this.owner.heal(0.6f);
				else if (lastHitTime >= 60)
					this.owner.heal(TF2Util.lerp(0.6f, 0.3f, (lastHitTime - 60) / 60f));
				else
					this.owner.heal(0.3f);
			}
		}

		if (this.owner.world.isRemote && this.owner == Minecraft.getMinecraft().player) {
			if (this.owner.getHeldItemMainhand().getItem() instanceof ItemSniperRifle && this.isCharging()
					&& !this.appliedMouseSlow) {
				Minecraft.getMinecraft().gameSettings.mouseSensitivity *= 0.4f;
				this.appliedMouseSlow = true;
			} else if (!this.isCharging() && this.appliedMouseSlow) {
				Minecraft.getMinecraft().gameSettings.mouseSensitivity *= 2.5f;
				this.appliedMouseSlow = false;
			}
		}

		if (this.owner.ticksExisted % 2 == 0)
			for (RageType type : RageType.values()) {
				if (this.isRageActive(type)) {
					this.setRage(type, Math.max(this.getRage(type) - this.rageDrain[type.ordinal()], 0f));
					if (this.getRage(type) <= 0f)
						this.setRageActive(type, false, 0f);
				}
			}

		if (this.reloadCool > 0)
			this.reloadCool -= 50;
		boolean hadItem = false;
		boolean continueReload = false;

		if (!this.owner.world.isRemote && this.owner instanceof EntityPlayer && this.owner.ticksExisted % 20 == 0) {
			this.maxmetal = TF2Attribute.getModifier("Max Metal",
					TF2Util.getBestItem(((EntityPlayer) this.owner).inventory, (stack1,
							stack2) -> (int) Math.signum(TF2Attribute.getModifier("Max Metal", stack1, 1, this.owner)
									- TF2Attribute.getModifier("Max Metal", stack2, 1, this.owner)),
							stackl -> TF2Attribute.getModifier("Max Metal", stackl, 1, this.owner) != 1),
					1, this.owner);
		}

		if (this.grapplingHook != null) {

			if (!this.grapplingHook.isEntityAlive()) {
				this.setGrapplingHook(null);
			}

			else if (this.grapplingHook.sticked) {
				// if (this.owner.ticksExisted % 40 == 0 && this.grapplingHook.stickedEntity ==
				// null && TF2Util.pierce(this.owner.world, this.owner, this.owner.posX,
				// this.owner.posY + this.owner.getEyeHeight(), this.owner.posZ,
				// this.grapplingHook.posX, this.grapplingHook.posY, this.grapplingHook.posZ,
				// false, 0f, false).get(0).typeOfHit == RayTraceResult.Type.BLOCK)
				// this.setGrapplingHook(null);
				// else {
				Vec3d motionVec = new Vec3d(this.grapplingHook.posX - this.owner.posX,
						this.grapplingHook.posY - this.owner.posY - this.owner.getEyeHeight(),
						this.grapplingHook.posZ - this.owner.posZ);
				if (motionVec.lengthSquared() > 0.77 * 0.77)
					motionVec = motionVec.normalize().scale(0.77);
				this.owner.motionX = motionVec.x;
				this.owner.motionY = motionVec.y;
				this.owner.motionZ = motionVec.z;
				this.owner.isAirBorne = true;
				this.owner.onGround = false;
				this.owner.velocityChanged = true;
				this.setGrappled(true);
				// }
			} else {
				this.setGrappled(false);
			}
		}

		for (EnumHand hand : EnumHand.values()) {
			ItemStack stack = owner.getHeldItem(hand);

			boolean isUseableWeapon = !stack.isEmpty() && stack.getItem() instanceof ItemUsable
					&& (hand == EnumHand.MAIN_HAND
							|| (hand == EnumHand.OFF_HAND && !hadItem
									&& ((ItemUsable) stack.getItem()).getDoubleWieldBonus(stack, owner) != 1)
							|| ItemUsable.isDoubleWielding(owner));

			if (isUseableWeapon) {

				hadItem = true;
				ItemUsable item = (ItemUsable) stack.getItem();

				int state = item.getStateOverride(stack, owner, this.state);
				if (this.fireCoolReduced && this.owner.world.isRemote) {
					this.setPrimaryCooldown(hand,
							this.getPrimaryCooldown(hand) - (int) (item.getFiringSpeed(stack, this.owner)
									* (1 - (1 / TF2Attribute.getModifier("Fire Rate Hit", stack, 1, this.owner)))));
					this.fireCoolReduced = false;
				}

				if (!(item instanceof ItemWeapon) || ((ItemWeapon) item).getClip(stack) == 0)
					this.autoFire = false;

				if (!(this.owner instanceof EntityPlayer)
						|| (this.owner.world.isRemote && this.owner != ClientProxy.getLocalPlayer()))
					item.onUpdate(stack, owner.world, owner, 0, true);
				WeaponData.WeaponDataCapability stackcap = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null);
				if (TF2Attribute.getModifier("Focus", stack, 0, owner) != 0) {
					this.focusShotTicks += this.owner.isSprinting() ? 1 : 2;
					if (this.owner.isSprinting() && this.focusShotTicks >= (this.focusShotTime(stack) * 3 / 4)) {
						this.focusShotTicks = this.focusShotTime(stack) * 3 / 4;
					}
					this.focusShotRemaining--;
				}
				if (stackcap.active == 1) {
					if (!this.lastWeapon.isEmpty()) {
						stackcap.fire1Cool = stackcap.fire2Cool = (int) Math.max(stackcap.fire1Cool,
								((TF2Attribute.getModifier("Holster Time", this.lastWeapon, 1f, this.owner) - 1f)
										* item.getDeployTime(stack, owner)));

						this.lastWeapon = ItemStack.EMPTY;
					}
					if (stackcap.fire1Cool <= 0) {
						this.setActiveHand(hand, stack);
					}
				}

				if (stackcap.active == 2) {
					this.stackActive.put(hand, stack);
				}
				if (owner.world.isRemote)
					stack.setAnimationsToGo(0);
				if (owner instanceof EntityTF2Character) {
					EntityTF2Character shooter = ((EntityTF2Character) owner);
					if (shooter.getAttackTarget() != null) {
						shooter.targetPrevPos[1] = shooter.targetPrevPos[0];
						shooter.targetPrevPos[3] = shooter.targetPrevPos[2];
						shooter.targetPrevPos[5] = shooter.targetPrevPos[4];
						shooter.targetPrevPos[0] = shooter.getAttackTarget().posX;
						shooter.targetPrevPos[2] = shooter.getAttackTarget().posY;
						shooter.targetPrevPos[4] = shooter.getAttackTarget().posZ;
					}
				}

				this.setCanFire(item.canFireInternal(owner.world, owner, stack, hand), hand, true);
				this.setCanFire(item.canAltFireInternal(owner.world, owner, stack, hand), hand, false);

				this.stateDo(owner, stack, hand, state);

				if ((!owner.world.isRemote || owner != Minecraft.getMinecraft().player)
						&& stack.getItem() instanceof ItemWeapon && ((ItemWeapon) stack.getItem()).hasClip(stack)
						&& (!item.searchForAmmo(owner, stack).isEmpty() || owner.world.isRemote)) {
					ItemWeapon weapon = (ItemWeapon) item;
					boolean full = (weapon.getClip(stack) >= weapon.getWeaponClipSize(stack, owner)
							&& TF2Attribute.getModifier("Overload", stack, 0, owner) == 0);
					boolean emptyMag = weapon.getClip(stack) == 0
							&& TF2Attribute.getModifier("Auto Fire", stack, 0, owner) == 0;
					if (((state & 4) != 0 || emptyMag || continueReload) && stackcap.active == 2
							&& this.reloadingHand == null && !full && this.reloadCool <= 0
							&& (this.getPrimaryCooldown() <= 0)
							&& owner.getActivePotionEffect(TF2weapons.stun) == null) {
						this.reloadingHand = hand;
						this.reloadCool = ((ItemWeapon) stack.getItem()).getWeaponFirstReloadTime(stack, owner);

						if (!owner.world.isRemote && owner instanceof EntityPlayerMP)
							TF2weapons.network.sendTo(new TF2Message.UseMessage(weapon.getClip(stack), true, -1, hand),
									(EntityPlayerMP) owner);

						if (owner.world.isRemote && ((ItemWeapon) stack.getItem()).IsReloadingFullClip(stack))
							TF2weapons.proxy.playReloadSound(owner, stack);

					} else if (this.getPrimaryCooldown() <= 0
							|| ((ItemWeapon) stack.getItem()).IsReloadingFullClip(stack))
						while (this.reloadingHand == hand && this.reloadCool <= 0 && !full) {
							// System.out.println("On client:
							// "+owner.world.isRemote);
							int maxAmmoUse = item.getAmmoAmount(owner, stack);
							int consumeAmount = 0;

							boolean overload = weapon.getClip(stack) >= weapon.getWeaponClipSize(stack, owner);
							if (((ItemWeapon) stack.getItem()).IsReloadingFullClip(stack)) {
								consumeAmount += Math.min(
										weapon.getWeaponClipSize(stack, owner) - weapon.getClip(stack), maxAmmoUse);
								weapon.consumeClip(stack, -consumeAmount, owner, hand);
								maxAmmoUse -= consumeAmount;

								/*
								 * if (maxAmmoUse > 0 && ItemUsable.isDoubleWielding(owner)) { consumeAmount +=
								 * Math.min(owner.getHeldItemOffhand().getItemDamage(), maxAmmoUse);
								 * owner.getHeldItemOffhand() .setItemDamage(Math.max(0, stack.getItemDamage() -
								 * consumeAmount)); }
								 */

							} else {
								consumeAmount = 1;
								weapon.consumeClip(stack, -1, owner, hand);
								// TF2weapons.proxy.playReloadSound(owner, stack);
							}

							if (overload) {
								consumeAmount = 0;
								weapon.consumeClip(stack, 1, owner, hand);
								weapon.onOverload(stack, owner, hand);
							}
							if (!owner.world.isRemote)
								item.consumeAmmoGlobal(owner, stack, consumeAmount);
							if (!owner.world.isRemote && owner instanceof EntityPlayerMP)
								TF2weapons.network.sendTo(new TF2Message.UseMessage(weapon.getClip(stack),
										weapon.getClip(stack) != weapon.getWeaponClipSize(stack, owner), -1, hand),
										(EntityPlayerMP) owner);

							this.reloadCool += ((ItemWeapon) stack.getItem()).getWeaponReloadTime(stack, owner);

							if (weapon.getClip(stack) == weapon.getWeaponClipSize(stack, owner)) {
								this.stopReload();
								continueReload = true;
							}
						}
				} else if (this.reloadingHand == hand
						&& !(owner.world.isRemote && owner == Minecraft.getMinecraft().player))
					this.reloadingHand = null;
			}
			if (this.stackActive.get(hand) != null) {
				if (owner.world.isRemote && !(owner instanceof EntityPlayer) && isUseableWeapon
						&& stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null)
						&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active != 2
						&& stack.getItem().shouldCauseReequipAnimation(this.stackActive.get(hand), stack, false)) {
					stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active = 2;
				}
				if (!(isUseableWeapon && stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null)
						&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active == 2)) {
					boolean revert = false;
					ItemStack activestack = this.stackActive.get(hand);
					if (activestack.getCount() == 0) {
						activestack.setCount(1);
						revert = true;
					}
					if (activestack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active == 2) {
						this.setInactiveHand(hand, activestack);
					}
					if (revert) {
						activestack.setCount(0);
					}
				}
			}
		}
		if (!hadItem) {
			owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(ItemMinigun.slowdown);
			owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(ItemSniperRifle.slowdown);
			owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(ItemHuntsman.slowdown);
		}

		if (!this.owner.world.isRemote && this.dataManager.isDirty()) {
			TF2Util.sendTracking(new TF2Message.CapabilityMessage(this.owner, false), this.owner);
		}

		PotionEffect charging = owner.getActivePotionEffect(TF2weapons.charging);

		/*
		 * if (owner instanceof EntityPlayer && owner.onGround && owner.motionY < 0) {
		 * this.gravity = owner.motionY/0.98; System.out.println(this.gravity); }
		 */

		if (!owner.onGround && charging != null)
			this.setExpJump(true);

		if (this.isExpJump() && this.canExpJump()) {
			boolean enchanted = TF2ConfigVars.enchantedExplosion && !owner.isElytraFlying() && !this.isUsingParachute()
					&& !(owner instanceof EntityPlayer && ((EntityPlayer) owner).capabilities.isFlying)
					&& !owner.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem().getRegistryName().toString()
							.equals("tconstruct:slime_boots");
			if (owner.onGround) {
				BlockPos pos = owner.getPosition().down();
				IBlockState block = owner.world.getBlockState(pos);
				owner.motionX *= block.getBlock().getSlipperiness(block, owner.world, pos, owner);
				owner.motionZ *= block.getBlock().getSlipperiness(block, owner.world, pos, owner);
			}

			if (this.expJumpGround > 0) {
				this.expJumpGround--;
				owner.onGround = false;
				owner.isAirBorne = true;
			} else if (owner.onGround || owner.isInWater()) {
				this.killsAirborne = 0;
				this.setExpJump(false);
				if (enchanted && this.oldFactor != 0) {
					owner.jumpMovementFactor = this.oldFactor;
					enchanted = false;
				}
			}

			if (!enchanted && owner.jumpMovementFactor == 0 && this.oldFactor != 0) {
				owner.jumpMovementFactor = this.oldFactor;
			}
			if (enchanted) {
				if (owner.jumpMovementFactor != 0) {
					this.oldFactor = owner.jumpMovementFactor;
					owner.jumpMovementFactor = 0;
				}
				// System.out.println(""+owner.motionX+ " " + owner.motionY +" "+ owner.motionZ
				// + " "+ (owner.posX - this.lastPosX));

				/*
				 * if(!owner.world.isRemote && owner instanceof EntityPlayer &&
				 * !TF2weapons.server.isSinglePlayer()) { boolean loaded =
				 * owner.world.isBlockLoaded(owner.getPosition()); owner.motionX = loaded ? 50 :
				 * 5; owner.motionZ = loaded ? 50 : 5; }
				 */
				if (owner.world.isRemote && owner instanceof EntityPlayer
						&& !owner.world.getChunkFromBlockCoords(owner.getPosition()).isLoaded()) {
					owner.motionX *= 0.99;
					owner.motionZ *= 0.99;
				}
				if ((!owner.world.isRemote || owner instanceof EntityPlayer)
						&& !(TF2weapons.squakeLoaded && owner instanceof EntityPlayer)) {

					double speed = Math.sqrt(owner.motionX * owner.motionX + owner.motionZ * owner.motionZ);

					Vec3d moveDir = TF2Util.getMovementVector(owner);
					double combSpeed = owner.motionX * moveDir.x + owner.motionZ * moveDir.y;

					double maxSpeed = charging != null ? owner.getAIMoveSpeed() * 2.16 : 0.026;
					double friction = charging != null ? 0.08 : 0.4;

					combSpeed = (maxSpeed) - combSpeed;
					double accel = Math.max(speed, maxSpeed) * friction;
					if (accel > combSpeed)
						accel = combSpeed;

					if (accel > 0) {
						owner.motionX += moveDir.x * accel;
						owner.motionZ += moveDir.y * accel;
					}
					owner.motionX /= 0.91;
					owner.motionZ /= 0.91;

				}
				owner.motionY /= 0.98;
				owner.motionY += 0.08 - TF2ConfigVars.explosiveJumpGravity;

			}
			owner.getEntityBoundingBox();
			// owner.motionX += owner.motionX -livin
			// owner.motionZ = owner.motionZ * 1.035;
		} else if (this.oldFactor != 0) {
			owner.jumpMovementFactor = this.oldFactor;
			this.oldFactor = 0;
		}

		if (this.isExpJump() || charging != null) {
			Vec3d vec = new Vec3d(owner.motionX, owner.motionY, owner.motionZ);
			Vec3d vec2 = new Vec3d(owner.motionX, 0, owner.motionZ);
			if (owner.onGround)
				vec = vec2;
			double motion = vec.lengthVector();
			// double motiona = vec2.lengthVector();
			if (motion >= 0.375 && (TF2ConfigVars.allowTrimp == 2 || (TF2ConfigVars.allowTrimp == 1 && EnumFacing
					.getFacingFromVector((float) vec.x, (float) vec.y, (float) vec.z) != EnumFacing.DOWN))) {
				vec2 = vec2.normalize();
				Vec3d entPos = owner.getPositionVector();
				Vec3d vecOff = vec.add(vec2.scale(owner.width * 1.5));
				RayTraceResult rayTrace = owner.world.rayTraceBlocks(entPos, entPos.add(vecOff));
				BlockPos pos = new BlockPos(vecOff.add(entPos));
				if (rayTrace != null) {
					pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);
					// System.out.println(rayTrace.getBlockPos());
				}
				if (owner.world.isBlockFullCube(pos.down())) {
					Vec3d step = TF2Util.getHeightVec(owner.world, pos);
					Vec3d normal = step.normalize();
					if (normal.y != 1D && normal.dotProduct(vecOff) < 0 && !(charging != null && normal.y > 0.75)) {
						double backoff = vec.dotProduct(normal);
						vec = new Vec3d(vec.x - normal.x * backoff, vec.y - normal.y * backoff,
								vec.z - normal.z * backoff);
						owner.motionX = vec.x;
						owner.motionY = vec.y;
						owner.motionZ = vec.z;
						if (charging != null) {
							double mult = motion / vec.lengthVector();
							owner.motionX *= mult;
							owner.motionY *= mult;
							owner.motionZ *= mult;
						}
						rayTrace = owner.world.rayTraceBlocks(vecOff.add(entPos), vecOff.add(entPos).add(vec));
						if (rayTrace != null) {
							owner.setPosition(owner.posX,
									owner.posY + 1 - rayTrace.hitVec.y + rayTrace.getBlockPos().getY(), owner.posZ);
						}

					}
				}
			}
		}
		if (!owner.world.isRemote && this.disguiseTicks > 0) {

			if (++this.disguiseTicks >= 40) {
				this.disguise();
			} else if (this.invisTicks < 20) {
				((WorldServer) owner.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, owner.posX, owner.posY,
						owner.posZ, 2, 0.2, 1, 0.2, 0.04f, new int[0]);
			}
		}

		if (owner.world.isRemote) {

			ClientProxy.doChargeTick(owner);
		}
		if (!owner.world.isRemote && charging != null) {
			if (ItemChargingTarge.getChargingShield(owner).isEmpty()) {
				owner.removePotionEffect(TF2weapons.charging);
			}
			Vec3d start = owner.getPositionVector().addVector(0, owner.height / 2, 0);
			Vec3d end = start.addVector(-MathHelper.sin(owner.rotationYaw / 180.0F * (float) Math.PI) * 0.7, 0,
					MathHelper.cos(owner.rotationYaw / 180.0F * (float) Math.PI) * 0.7);
			RayTraceResult result = TF2Util
					.pierce(owner.world, owner, start.x, start.y, start.z, end.x, end.y, end.z, false, 0.5f, false)
					.get(0);
			if (result.entityHit != null) {
				float damage = 5;
				if (charging.getDuration() > 30) {
					damage *= 0.5f;
				}
				TF2Util.dealDamage(result.entityHit, result.entityHit.world, owner,
						ItemChargingTarge.getChargingShield(owner), 0, damage,
						TF2Util.causeDirectDamage(ItemChargingTarge.getChargingShield(owner), owner));

				this.bashCritical = charging.getDuration() < 20;
				if (charging.getDuration() < 12)
					TF2Util.playSound(owner, TF2Sounds.WEAPON_SHIELD_HIT_RANGE, 3F, 1F);
				else
					TF2Util.playSound(owner, TF2Sounds.WEAPON_SHIELD_HIT, 0.8F, 1F);
				this.ticksBash = 20;
				owner.motionX = 0;
				owner.motionZ = 0;
				owner.removePotionEffect(TF2weapons.charging);

			}
		}
		if (owner.world.isRemote && WeaponsCapability.get(owner).isDisguised() != this.lastDisgused) {
			this.lastDisgused = WeaponsCapability.get(owner).isDisguised();
			if (owner instanceof EntityPlayer)
				((EntityPlayer) owner).refreshDisplayName();
		}
		String disguisetype = this.getDisguiseType();

		if (owner.world.isRemote && !disguisetype.equals(this.lastDisguiseValue)) {
			if (owner instanceof EntityPlayer) {
				((EntityPlayer) owner).refreshDisplayName();
			}
			this.disguiseCounter += 1;
			this.lastDisguiseValue = disguisetype;
			if (this.getDisguiseType().startsWith("P:")) {
				this.skinDisguise = null;
				EntityPlayer playerDisguise = this.owner.world.getPlayerEntityByName(getDisguiseType().substring(2));
				if (playerDisguise != null && TF2PlayerCapability.get(playerDisguise).isForceClassTexture()
						&& WeaponsCapability.get(playerDisguise).getUsedToken() >= 0) {
					this.skinType = "default";
					if (TF2Util.getTeamForDisplay(playerDisguise) == 0) {
						this.skinDisguise = RenderTF2Character.RED_TEXTURES[WeaponsCapability.get(playerDisguise)
								.getUsedToken()];
					} else {
						this.skinDisguise = RenderTF2Character.BLU_TEXTURES[WeaponsCapability.get(playerDisguise)
								.getUsedToken()];
					}
				} else {
					this.skinType = DefaultPlayerSkin.getSkinType(owner.getUniqueID());
					THREAD_POOL.submit(() -> {
						GameProfile profile = TileEntitySkull.updateGameprofile(
								new GameProfile(owner.getUniqueID(), getDisguiseType().substring(2)));
						if (profile.getId() != null) {
							skinType = DefaultPlayerSkin.getSkinType(profile.getId());
							skinDisguise = DefaultPlayerSkin.getDefaultSkin(profile.getId());
						}
						Minecraft.getMinecraft().getSkinManager().loadProfileTextures(profile,
								(typeIn, location, profileTexture) -> {
if (typeIn == Type.SKIN) {
								if (typeIn == Type.SKIN) {
									skinDisguise = location;
								}
								skinType = profileTexture.getMetadata("model");

								if (skinType == null) {
									skinType = "default";
								}
}
}, false);
					});
				}
			}

		}

		/*
		 * Minecraft.getMinecraft().getSkinManager().loadSkin(new
		 * MinecraftProfileTexture( "http://skins.minecraft.net/MinecraftSkins/"+owner.
		 * getDataManager().get(TF2EventBusListener.ENTITY_DISGUISE_TYPE
		 * ).substring(2)+".png",null), Type.SKIN,new SkinAvailableCallback(){
		 * 
		 * @Override public void skinAvailable(Type typeIn, ResourceLocation location,
		 * MinecraftProfileTexture profileTexture) { if(typeIn==Type.SKIN){
		 * this.skinDisguise=location; System.out.println("RetrieveD"); } }
		 * 
		 * });
		 */
		if (owner.world.isRemote && owner != ClientProxy.getLocalPlayer()) {
			// System.out.println("uber
			// "+owner.getActivePotionEffect(TF2weapons.uber).getDuration());
			Iterator<PotionEffect> iterator = owner.getActivePotionEffects().iterator();
			while (iterator.hasNext()) {
				PotionEffect effect = iterator.next();
				if (effect.getDuration() <= 0
						&& (effect.getPotion() instanceof PotionTF2 || effect.getPotion() instanceof PotionTF2Item)) {
					iterator.remove();
				}
			}
		}
		if (!owner.world.isRemote && owner.ticksExisted % 10 == 0 && this.isFeign()
				&& ItemCloak.getFeignDeathWatch(owner).isEmpty()) {
			this.setFeign(false);
		}
		if (!owner.world.isRemote && owner.fallDistance > 0
				&& owner.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == TF2weapons.itemMantreads) {
			TF2Util.stomp(owner);
		}
		if (this.isInvisible()) {
			// System.out.println("cloak");
			ItemStack cloak = ItemCloak.searchForWatches(owner).getSecond();
			boolean feign = !cloak.isEmpty() && ((ItemCloak) cloak.getItem()).isFeignDeath(cloak, owner);
			boolean visible = owner.hurtTime == 10 && !feign;
			if (!visible && !feign) {
				List<Entity> closeEntities = owner.world.getEntitiesInAABBexcluding(owner,
						owner.getEntityBoundingBox().grow(1, 2, 1), input -> input instanceof EntityLivingBase && !TF2Util.isOnSameTeam(owner, input));
				for (Entity ent : closeEntities) {
					if (ent.getDistanceSq(owner) < 1) {
						visible = true;
					}
					break;
				}
			}
			if (visible) {
				// System.out.println("reveal");
				this.invisTicks = Math.min(10, this.invisTicks);
				owner.setInvisible(false);
			}
			if (feign)
				this.invisTicks = 20;
			this.invisTicks += 2;
			if (owner.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks < 20) {} else if (!owner.isInvisible()) {
				// System.out.println("full");
				owner.setInvisible(true);
			}
			boolean active = owner.world.isRemote || !cloak.isEmpty();
			if (!active) {
				this.setInvisible(false);
				owner.setInvisible(false);
				// System.out.println("decloak");
			}
		} else if (owner.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks > 0) {
			this.invisTicks = Math.min(20, this.invisTicks - 1);
			if (owner.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks == 0) {
				owner.setInvisible(false);
			}
		}

		this.lastPosX = owner.posX;
		this.lastPosY = owner.posY;
		this.lastPosZ = owner.posZ;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return TF2weapons.WEAPONS_CAP != null && capability == TF2weapons.WEAPONS_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (TF2weapons.WEAPONS_CAP != null && capability == TF2weapons.WEAPONS_CAP)
			return TF2weapons.WEAPONS_CAP.cast(this);
		return null;
	}

	public boolean startedPress() {
		if (this.pressedStart) {
			this.pressedStart = false;
			return true;
		}
		return false;

	}

	public boolean shouldShoot(EntityLivingBase player, int state, EnumHand hand, int actualState) {
		return (!(!player.world.isRemote && player instanceof EntityPlayer
				&& this.predictionList[hand.ordinal() + (state == 1 ? 0 : 2)].isEmpty()))
				&& !((player.world.isRemote || !(player instanceof EntityPlayer)) && (actualState & state) != state);
	}

	public void stateDo(EntityLivingBase player, ItemStack stack, EnumHand hand, int state) {
		if (!TF2Util.canInteract(player))
			return;

		ItemUsable item = (ItemUsable) stack.getItem();
		WeaponData.WeaponDataCapability stackcap = WeaponData.getCapability(stack);

		if ((state & 1) != 0 && stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active == 2)

			item.fireTick(stack, player, player.world);
		while (stackcap.fire1Cool <= 0 && shouldShoot(player, 1, hand, state)) {

			TF2Message.PredictionMessage message = null;
			if (!player.world.isRemote && player instanceof EntityPlayer) {
				message = this.predictionList[hand.ordinal()].pollLast();
				// this.predictionList[hand.ordinal()] = null;
			}

			if (message == null) {
				boolean canFire = item.canFireInternal(player.world, player, stack, hand);
				if (!canFire)
					break;
			} else {

				this.mainHand = message.hand == EnumHand.MAIN_HAND;
				if (message.hand != hand || !item.canFireInternal(player.world, player, stack, hand))
					break;
			}
			int fireRate = item.getFiringSpeed(stack, player);
			stackcap.fire1Cool += Math.max(1, fireRate);
			if (ItemUsable.isDoubleWielding(player)) {
				if (hand == EnumHand.MAIN_HAND)
					this.setPrimaryCooldown(EnumHand.OFF_HAND,
							Math.max(this.getPrimaryCooldown(EnumHand.OFF_HAND), fireRate / 2 - 50));
				else if (hand == EnumHand.OFF_HAND)
					this.setPrimaryCooldown(EnumHand.MAIN_HAND,
							Math.max(this.getPrimaryCooldown(EnumHand.MAIN_HAND), fireRate / 2 - 50));
			}

			if (player instanceof EntityTF2Character)
				((EntityTF2Character) player).onShot();

			if (!(item instanceof ItemSapper))
				this.setDisguised(false);

			double oldX = player.posX, oldY = player.posY, oldZ = player.posZ;
			float oldPitch = player.rotationPitch, oldYaw = player.rotationYawHead;

			if (!player.world.isRemote && player instanceof EntityPlayer)
				this.preparePlayerPrediction(player, message);
			if (player.world.isRemote && player == ClientProxy.getLocalPlayer())
				message = new TF2Message.PredictionMessage(player.posX, player.posY, player.posZ, player.rotationPitch,
						player.rotationYawHead, 1, hand);

			item.use(stack, player, player.world, hand, message);

			player.removePotionEffect(TF2weapons.charging);

			if (player.world.isRemote && player == ClientProxy.getLocalPlayer()) {
				TF2weapons.network.sendToServer(message);
			}

			player.posX = oldX;
			player.posY = oldY;
			player.posZ = oldZ;
			player.rotationYawHead = oldYaw;
			player.rotationPitch = oldPitch;

			this.lastFire = 1250;
			this.fireCoolReduced = false;
			if (stack.getItem() instanceof ItemWeapon) {
				this.stopReload();
			}

			if (stackcap.fire1Cool > 200) {
				this.predictionList[hand.ordinal()].clear();
			}
		}
		if ((state & 2) != 0 && stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active == 2)
			((ItemUsable) stack.getItem()).altFireTick(stack, player, player.world);
		while (stackcap.fire2Cool <= 0 && shouldShoot(player, 2, hand, state)) {

			TF2Message.PredictionMessage message = null;
			if (!player.world.isRemote && player instanceof EntityPlayer) {
				message = this.predictionList[hand.ordinal() + 2].pollLast();
				// this.predictionList[hand.ordinal() + 2] = null;
			}

			if (item.getAltFiringSpeed(stack, player) == Short.MAX_VALUE
					|| !item.canAltFireInternal(player.world, player, stack, hand))
				break;
			stackcap.fire2Cool += item.getAltFiringSpeed(stack, player);

			this.setDisguised(false);

			double oldX = player.posX, oldY = player.posY, oldZ = player.posZ;
			float oldPitch = player.rotationPitch, oldYaw = player.rotationYawHead;

			if (!player.world.isRemote && player instanceof EntityPlayer)
				this.preparePlayerPrediction(player, message);

			((ItemUsable) stack.getItem()).altUse(stack, player, player.world);

			player.removePotionEffect(TF2weapons.charging);
			if (player.world.isRemote && player == ClientProxy.getLocalPlayer())
				TF2weapons.network.sendToServer(new TF2Message.PredictionMessage(player.posX, player.posY, player.posZ,
						player.rotationPitch, player.rotationYawHead, 2, EnumHand.MAIN_HAND));

			player.posX = oldX;
			player.posY = oldY;
			player.posZ = oldZ;
			player.rotationYawHead = oldYaw;
			player.rotationPitch = oldPitch;

			if (!player.world.isRemote)
				ItemUsable.sps++;
			if (stack.getItem() instanceof ItemWeapon) {
				this.stopReload();
			}

			if (stackcap.fire2Cool > 200) {
				this.predictionList[hand.ordinal() + 2].clear();
			}
		}
	}

	public void updateExpJump() {

	}

	public void preparePlayerPrediction(EntityLivingBase player, TF2Message.PredictionMessage message) {
		player.posX = message.x;
		player.posY = message.y;
		player.posZ = message.z;
		player.rotationYawHead = message.yaw;
		player.rotationPitch = message.pitch;
		this.mainHand = message.hand == EnumHand.MAIN_HAND;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByte("VisTicks", (byte) this.invisTicks);
		tag.setByte("DisguiseTicks", (byte) this.disguiseTicks);
		NBTTagCompound cld = new NBTTagCompound();
		for (Entry<String, Integer> entry : this.effectsCool.entrySet())
			cld.setInteger(entry.getKey(), entry.getValue());
		tag.setTag("Cooldowns", cld);
		tag.setInteger("HealTarget", this.getHealTarget());
		tag.setShort("Heads", this.dataManager.get(HEADS).shortValue());
		tag.setShort("HeadsCool", (short) (this.collectedHeadsTime - this.owner.ticksExisted));
		tag.setBoolean("Cloaked", this.isInvisible());
		tag.setBoolean("Disguised", this.isDisguised());
		tag.setString("DisguiseType", this.getDisguiseType());
		tag.setInteger("KillsSpinning", this.killsSpinning);
		tag.setInteger("FocusedShot", this.focusShotTicks);
		tag.setInteger("KnockbackFANCool", this.fanCool);
		tag.setInteger("Metal", this.getMetal());
		tag.setFloat("MaxMetal", this.maxmetal);
		for (RageType rage : RageType.values()) {
			tag.setFloat(rage.toString(), this.getRage(rage));
			tag.setBoolean(rage.toString() + "active", this.isRageActive(rage));
			if (this.isRageActive(rage))
				tag.setFloat(rage.toString() + "drain", this.rageDrain[rage.ordinal()]);
		}
		tag.setLong("TicksTotal", this.ticksTotal);
		if (this.forcedClass)
			tag.setByte("Token", (byte) this.getUsedToken());
		// tag.setBoolean("Uber",
		// this.owner.getDataManager().get(TF2EventBusListener.ENTITY_UBER));
		// tag.setFloat("DodgedDmg", this.dodgedDmg);
		// tag.setInteger("KillsSpinning", this.killsSpinning);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.invisTicks = nbt.getByte("VisTicks");
		this.disguiseTicks = nbt.getByte("DisguiseTicks");
		// this.setHealTarget(nbt.getInteger("HealTarget"));
		NBTTagCompound cld = nbt.getCompoundTag("Cooldowns");
		for (String key : cld.getKeySet())
			this.effectsCool.put(key, cld.getInteger(key));
		this.dataManager.set(HEADS, (int) nbt.getShort("Heads"));
		this.collectedHeadsTime = nbt.getShort("HeadsCool");
		this.setInvisible(nbt.getBoolean("Cloaked"));
		this.setDisguised(nbt.getBoolean("Disguised"));
		// this.owner.getDataManager().set(TF2EventBusListener.ENTITY_UBER,
		// nbt.getBoolean("Uber"));
		this.setDisguiseType(nbt.getString("DisguiseType"));
		if (this.isDisguised())
			this.disguise();
		this.killsSpinning = nbt.getInteger("KillsSpinning");
		this.focusShotTicks = nbt.getInteger("FocusedShot");
		this.fanCool = nbt.getInteger("KnockbackFANCool");
		// this.dodgedDmg=nbt.getFloat("DodgedDmg");
		this.maxmetal = nbt.getFloat("MaxMetal");
		this.setMetal(nbt.getInteger("Metal"));
		this.ticksTotal = nbt.getLong("TicksTotal");
		for (RageType rage : RageType.values()) {
			this.setRage(rage, nbt.getFloat(rage.toString()));
			this.setRageActive(rage, nbt.getBoolean(rage.toString() + "active"),
					nbt.getFloat(rage.toString() + "drain"));
		}
		if (nbt.hasKey("Token")) {
			((ItemToken) TF2weapons.itemToken)
					.updateAttributes(new ItemStack(TF2weapons.itemToken, 1, nbt.getByte("Token")), this.owner);
			this.forcedClass = true;
		}
		// this.killsSpinning=nbt.getInteger("KillsSpinning");
	}

	public int getMaxAirJumps() {
		int amount = 0;
		if (this.owner instanceof EntityScout)
			amount += 1;
		else if (this.owner instanceof EntityPlayer && (this.getUsedToken() == 0 || (ItemToken.allowUse(owner, "scout")
				&& owner.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == TF2weapons.itemScoutBoots))) {
			amount += 1;
		}
		amount = (int) TF2Attribute.getModifier("Triple Jump", this.owner.getHeldItemMainhand(), amount, this.owner);
		return amount;
	}

	public static EntityDataManager getDataManager(Entity ent) {
		return ent.getCapability(TF2weapons.WEAPONS_CAP, null).dataManager;
	}

	public static WeaponsCapability get(Entity ent) {
		return ent.getCapability(TF2weapons.WEAPONS_CAP, null);
	}

	public int getMinimalCooldown() {
		if (!this.owner.world.isRemote && this.owner instanceof EntityPlayer)
			return PLAYER_MINCOOL;
		else
			return 0;
	}

	public int getPrimaryCooldown() {
		return Math.max(this.getPrimaryCooldown(EnumHand.MAIN_HAND), this.getPrimaryCooldown(EnumHand.OFF_HAND));
	}

	public void setPrimaryCooldown(int fire1Cool) {
		this.setPrimaryCooldown(EnumHand.MAIN_HAND, fire1Cool);
		this.setPrimaryCooldown(EnumHand.OFF_HAND, fire1Cool);
	}

	public int getPrimaryCooldown(EnumHand hand) {
		if (this.owner.getHeldItem(hand).getItem() instanceof ItemUsable)
			return this.owner.getHeldItem(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire1Cool
					- getMinimalCooldown();
		else
			return 0;
	}

	public void setPrimaryCooldown(EnumHand hand, int fire1Cool) {
		if (this.owner.getHeldItem(hand).hasCapability(TF2weapons.WEAPONS_DATA_CAP, null))
			this.owner.getHeldItem(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire1Cool = fire1Cool
					+ getMinimalCooldown();
	}

	public int getSecondaryCooldown() {
		return Math.max(this.getSecondaryCooldown(EnumHand.MAIN_HAND), this.getSecondaryCooldown(EnumHand.OFF_HAND));
	}

	public void setSecondaryCooldown(int fire1Cool) {
		this.setSecondaryCooldown(EnumHand.MAIN_HAND, fire1Cool);
		this.setSecondaryCooldown(EnumHand.OFF_HAND, fire1Cool);
	}

	public int getSecondaryCooldown(EnumHand hand) {
		if (this.owner.getHeldItem(hand).getItem() instanceof ItemUsable)
			return this.owner.getHeldItem(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire2Cool
					- getMinimalCooldown();
		else
			return 0;
	}

	public void setSecondaryCooldown(EnumHand hand, int fire1Cool) {
		if (this.owner.getHeldItem(hand).hasCapability(TF2weapons.WEAPONS_DATA_CAP, null))
			this.owner.getHeldItem(hand).getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire2Cool = fire1Cool
					+ getMinimalCooldown();
	}

	public void stopReload() {
		this.reloadCool = 0;
		this.reloadingHand = null;
	}

	public boolean canExpJump() {
		return canExpJump;
	}

	public void setCanExpJump(boolean canExpJump) {
		this.canExpJump = canExpJump;
	}

	public void setActiveHand(EnumHand hand, ItemStack stack) {
		this.stackActive.put(hand, stack);
		stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active = 2;
		((ItemUsable) stack.getItem()).draw(this, stack, owner, owner.world);

		if ((state & 3) > 0) {
			state = state & 7;
			if ((state & 3) > 0)
				((ItemUsable) stack.getItem()).startUse(stack, owner, owner.world, 0, state & 3);
		}
	}

	public void setInactiveHand(EnumHand hand, ItemStack stack) {
		this.stackActive.remove(hand);
		if (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active == 2 && (this.state & 3) > 0)
			((ItemUsable) stack.getItem()).endUse(stack, this.owner, this.owner.world, this.state, 0);

		stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active = 0;
		((ItemUsable) stack.getItem()).holster(this, stack, this.owner, this.owner.world);
	}

	public void disguise() {
		this.setDisguised(true);
		if (this.invisTicks < 20 && this.owner.world instanceof WorldServer)
			((WorldServer) this.owner.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.owner.posX,
					this.owner.posY, this.owner.posZ, 20, 0.2, 1, 0.2, 0.04f, new int[0]);
	}

	public boolean isGrappling() {
		return this.dataManager.get(GRAPPLING);
	}

	public boolean isGrappled() {
		return this.dataManager.get(GRAPPLED);
	}

	public void setGrappled(boolean grappled) {
		this.dataManager.set(GRAPPLED, grappled);
	}

	public EntityGrapplingHook getGrapplingHook() {
		return grapplingHook;
	}

	public void setGrapplingHook(EntityGrapplingHook grapplingHook) {
		if (this.grapplingHook != null) {
			if (this.grapplingHook.isEntityAlive())
				this.grapplingHook.setDead();
		}
		this.setGrappled(false);
		this.dataManager.set(GRAPPLING, grapplingHook != null);
		this.grapplingHook = grapplingHook;
	}

	public static enum RageType {
		PHLOG, MINICRIT, KNOCKBACK, BANNER
	}

	static {

		for (RageType type : RageType.values()) {
			RAGE.put(type, new DataParameter<>(127 - type.ordinal() * 2, DataSerializers.FLOAT));
			RAGE_ACTIVE.put(type, new DataParameter<>(126 - type.ordinal() * 2, DataSerializers.BOOLEAN));
		}
	}

}
