package rafradek.TF2weapons.entity.mercenary;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.TF2weapons.*;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.ai.*;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.inventory.InventoryLoadout;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.util.*;

import javax.annotation.Nullable;
import java.util.*;

public abstract class EntityTF2Character extends EntityCreature implements IMob, IMerchant, IEntityTF2, IEntityOwnable {

	public static final UUID SPEED_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b44");
	public static final UUID SPEED_GIANT_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b41");
	public static final UUID HEALTH_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b32");
	public static final UUID KNOCKBACK_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b35");
	public static final UUID FOLLOW_MULT_UUID = UUID.fromString("8ca1776e-72e8-4394-9d0f-0564fdec0b51");
	public static final UUID ARMOR_MULT_UUID = UUID.fromString("8ca1776e-72e8-4452-9d0f-0564fdec0b51");

	public float[] lastRotation;
	public boolean jump;
	public boolean friendly;
	public boolean ranged;
	public EntityAIUseRangedWeapon attack;
	public EntityAIMoveAttack moveAttack;
	public EntityAINearestChecked<EntityLivingBase> findplayer = new EntityAINearestChecked<>(this,
			EntityLivingBase.class, true, false, this::isValidTarget, true, false);
	protected EntityAIAttackMelee attackMeele = new EntityAIAttackMelee(this, 1.1F, false);
	public EntityAIWander wander;
	// public int ammoLeft;
	public boolean unlimitedAmmo;
	public boolean natural;
	private boolean noAmmo;
	public boolean alert;
	public static int nextEntTeam = -1;
	public EntityPlayer trader;
	public EntityPlayer lastTrader;
	public Map<EntityPlayer, Integer> tradeCount;
	public InventoryLoadout loadout;
	public ItemStackHandler loadoutHeld;
	public ItemStackHandler refill;
	// public int heldWeaponSlot;

	public int followTicks;
	public MerchantRecipeList tradeOffers;
	public double[] targetPrevPos = new double[9];
	private static final DataParameter<Byte> VIS_TEAM = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BYTE);
	private static final DataParameter<Byte> DIFFICULTY = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BYTE);
	protected static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager
			.createKey(EntityTF2Character.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<Byte> ORDER = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BYTE);
	private static final DataParameter<Byte> MAIN_WEAPON = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BYTE);
	private static final DataParameter<Byte> ACTIVE_SLOT = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BYTE);
	private static final DataParameter<Boolean> SHARE = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BOOLEAN);
	private static final DataParameter<Byte> ROBOT = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BYTE);
	private static final DataParameter<Byte> FRONT = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BYTE);
	private static final DataParameter<Byte> SIDE = EntityDataManager.createKey(EntityTF2Character.class,
			DataSerializers.BYTE);
	public float rotation;
	public int traderFollowTicks;
	public int usedSlot = 0;
	public int bannerTeam = -1;
	private UUID followID;
	public int tradeLevel;
	public String ownerName;
	private int[] ammoCount;
	public int preferredSlot = 0;
	public float eating = 0;
	public EntityAIAvoidEntity<EntitySentry> avoidSentry;
	private int alertTime;
	private EntityLivingBase alertTarget;
	public int difficulty = 0;
	public ArrayList<AttributeModifier> playerAttributes = new ArrayList<>();
	public boolean[] isEmpty;
	protected boolean noEquipment;
	public boolean spawnMedic;
	private int robotSizeEnsured;
	public float robotStrength = 1f;
	public BlockPos spawnPos = BlockPos.ORIGIN;
	public InvasionEvent event;
	public boolean damagedByEnv;
	private WeaponsCapability weaponCap;
	private int moneyDrop;
	// private InventoryWearables wearablesCap;

	public EntityTF2Character(World p_i1738_1_) {
		super(p_i1738_1_);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIMoveTowardsRestriction2(this, 1.25f));
		this.tasks.addTask(1, avoidSentry = new EntityAIAvoidEntity<EntitySentry>(this, EntitySentry.class, sentry -> {
			double range = sentry.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue() + 1;
			range *= range;
			return !TF2Util.isOnSameTeam(this, sentry) && !sentry.isDisabled() && sentry.getDistanceSq(this) < range;
		}, 25, 1.0f, 1.0f) {
			@Override
			public boolean shouldContinueExecuting() {
				double range = closestLivingEntity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
						.getAttributeValue() + 1;
				range *= range;
				return super.shouldContinueExecuting() && !(this.closestLivingEntity == null
						|| this.closestLivingEntity.getDistanceSq(EntityTF2Character.this) > range);
			}
		});
		this.tasks.addTask(2, new EntityAIFollowTrader(this));
		this.tasks.addTask(3, new EntityAIFindDispenser(this, 20f));
		this.tasks.addTask(6, wander = new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityTF2Character.class, 8.0F));
		this.tasks.addTask(7, new EntityAISeek(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(5, findplayer);
		this.targetTasks.addTask(3, new EntityAIOwnerHurt(this));
		this.targetTasks.addTask(4,
				new EntityAINearestChecked<EntityLiving>(this, EntityLiving.class, true, false,
						target -> ((target instanceof IMob && target.getDistanceSq(this.getOwner()) < 400)
								|| target.getAttackTarget() == this.getOwner()),
						true, false) {
					@Override
					public boolean shouldExecute() {
						return ((EntityTF2Character) this.taskOwner).getOwner() != null && super.shouldExecute();
					}
				});
		// this.lookHelper=new
		// this.motionSensitivity=4;
		this.rotation = 17;
		this.lastRotation = new float[20];
		this.loadout = new InventoryLoadout(5, this);// NonNullList.withSize(5,ItemStack.EMPTY);
		this.loadoutHeld = new ItemStackHandler(8);
		this.refill = new ItemStackHandler(1);
		this.ammoCount = new int[4];
		this.isEmpty = new boolean[4];
		this.inventoryHandsDropChances[0] = 0;
		this.inventoryArmorDropChances[0] = 0.25f;
		// this.jumpMovementFactor = 0.1f;
		if (p_i1738_1_ != null) {
			// this.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().setTag("Attributes",
			// (NBTTagCompound)
			// ((ItemUsable)this.getHeldItem(EnumHand.MAIN_HAND).getItem()).buildInAttributes.copy());

			this.tasks.addTask(4, this.attack = new EntityAIUseRangedWeapon(this, 1.0F, 19.9F));
			this.tasks.addTask(4, this.moveAttack = new EntityAIMoveAttack(this, 1.0F, 20.0F));
			// this.setCombatTask(true);
		}
		this.tradeCount = new HashMap<>();
		/*
		 * for (int i = 0; i < this.e.length; ++i) { this.equipmentDropChances[i] =
		 * 0.0F; }
		 */
	}

	/*
	 * public EntityLookHelper getLookHelper() { return this.lookHelper; }
	 */
	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		ItemStack stack = new ItemStack(TF2weapons.itemPlacer, 1, getTF2Class().getIndex() + 9 * (this.getEntTeam()));

		// stack.setTagCompound(new NBTTagCompound());
		// stack.getTagCompound().setTag("SavedEntity", new NBTTagCompound());
		// this.writeEntityToNBT(stack.getTagCompound().getCompoundTag("SavedEntity"));
		return stack;
	}

	public float getWidth() {
		return 0.6f;
	}

	public float getHeight() {
		return 1.8f;
	}

	protected void addWeapons() {
		TF2Class clazz = getTF2Class();
		// System.out.println("Class name: "+className);
		this.loadout.setStackInSlot(0, ItemFromData.getRandomWeaponOfSlotMob(clazz, 0, this.rand, false,
				this.getStockWeight(0), this.noEquipment));
		this.loadout.setStackInSlot(1, ItemFromData.getRandomWeaponOfSlotMob(clazz, 1, this.rand, false,
				this.getStockWeight(1), this.noEquipment));
		this.loadout.setStackInSlot(2, ItemFromData.getRandomWeaponOfSlotMob(clazz, 2, this.rand, false,
				this.getStockWeight(2), this.noEquipment));
		this.loadout.setStackInSlot(3, ItemFromData.getRandomWeaponOfSlotMob(clazz, 3, this.rand, false,
				this.getStockWeight(3), this.noEquipment));
		if (!this.noEquipment && !this.isRobot()) {
			if (this.rand.nextInt(Math.max(1, (int) ((14 - this.world.getDifficulty().getDifficultyId() * 3)
					/ TF2ConfigVars.hatMercenaryMult))) == 0) {
				this.tradeLevel = 1;
				this.difficulty = 1;
				this.experienceValue *= 2;
				ItemStack hat = ItemFromData.getRandomWeaponOfSlotMob(clazz, 9, this.rand, false,
						this.getStockWeight(9), false);

				if (!hat.isEmpty() && this.rand.nextInt(9) == 0) {
					hat.getTagCompound().setByte("UEffect", (byte) this.rand.nextInt(10));
					this.inventoryArmorDropChances[0] = 0.35f;
					this.tradeLevel = 2;
					this.difficulty = 2;
					this.experienceValue *= 1.5;
					for (int i = 0; i < this.loadout.getSlots(); i++) {
						TF2Attribute.upgradeItemStack(this.loadout.getStackInSlot(i),
								Math.min(1600, 640 + (int) (this.world.getWorldTime() / 2000)), rand);
					}

				}

				this.setItemStackToSlot(EntityEquipmentSlot.HEAD, hat);
				if (this.world.getWorldTime() > 48000)
					for (int i = 0; i < this.loadout.getSlots(); i++) {
						TF2Attribute.upgradeItemStack(this.loadout.getStackInSlot(i),
								Math.min(800, 232 + (int) (this.world.getWorldTime() / 4000)), rand);
					}
			} else if (this.rand.nextInt(2 + Math.min(5, (int) (this.world.getWorldTime() / 160000))) == 0) {
				ItemStack hat = ItemFromData.getRandomWeapon(rand, data -> data.getBoolean(PropertyType.F2P));
				this.setItemStackToSlot(EntityEquipmentSlot.HEAD, hat);
				this.inventoryArmorDropChances[0] = 0.35f;
				this.difficulty = -1;
				this.tradeLevel = -1;
			}
		}
	}

	public float getStockWeight(int slot) {
		return this.isRobot() ? 4.5f - Math.min(2.5f, this.robotStrength / 2f) : 2f;
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		if (this.noEquipment || this.isRobot())
			return;

		float chance = 1f + 3f * this.tradeLevel + this.tradeLevel * this.tradeLevel * 2f;
		if (this.rand.nextFloat() < TF2ConfigVars.armorMult * chance * difficulty.getAdditionalDifficulty() * 0.3f) {
			int i = this.rand.nextInt(2);
			float f = (this.world.getDifficulty() == EnumDifficulty.HARD ? 0.1F : 0.25F) / chance;

			if (this.rand.nextFloat() < 0.095F * chance) {
				++i;
			}

			if (this.rand.nextFloat() < 0.095F * chance) {
				++i;
				this.getEntityAttribute(SharedMonsterAttributes.ARMOR)
						.applyModifier(new AttributeModifier("hatarmor", 1, 0));
			}

			if (this.rand.nextFloat() < 0.095F * chance) {
				++i;
				this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS)
						.applyModifier(new AttributeModifier("hatarmort", 2, 0));
			}

			boolean flag = true;

			for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
				if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
					ItemStack itemstack = this.getItemStackFromSlot(entityequipmentslot);

					if (!flag && this.rand.nextFloat() < f) {
						break;
					}

					flag = false;

					if (itemstack.isEmpty() && entityequipmentslot != EntityEquipmentSlot.HEAD) {
						Item item = getArmorByChance(entityequipmentslot, i);

						if (item != null) {
							this.setItemStackToSlot(entityequipmentslot, new ItemStack(item));
						}
					}
				}
			}
		}
	}

	/*
	 * public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn){
	 * if(slotIn==EntityEquipmentSlot.MAINHAND){
	 * //System.out.println("Held item: "+this.loadout[this.heldWeaponSlot]); return
	 * this.loadout[this.heldWeaponSlot]; } else{ return
	 * super.getItemStackFromSlot(slotIn); } }
	 */
	/*
	 * public void setItemStackToSlot(EntityEquipmentSlot slotIn, @Nullable
	 * ItemStack stack) { if(slotIn==EntityEquipmentSlot.MAINHAND){
	 * this.loadout[this.heldWeaponSlot]=stack; } else{
	 * super.setItemStackToSlot(slotIn, stack); } }
	 */
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(VIS_TEAM, (byte) this.rand.nextInt(2));
		this.dataManager.register(DIFFICULTY, (byte) 0);
		this.dataManager.register(OWNER_UUID, Optional.absent());
		this.dataManager.register(ORDER, (byte) 0);
		this.dataManager.register(SHARE, false);
		this.dataManager.register(ROBOT, (byte) 0);
		this.dataManager.register(FRONT, (byte) 0);
		this.dataManager.register(SIDE, (byte) 0);
		this.dataManager.register(MAIN_WEAPON, (byte) -1);
		this.weaponCap = new WeaponsCapability(this);
		// this.wearablesCap = new InventoryWearables(this);
	}

	public int getEntTeam() {
		return this.isRobot() ? 2 : this.dataManager.get(VIS_TEAM);
	}

	public int getDiff() {
		return Math.max(1, this.dataManager.get(DIFFICULTY) + this.difficulty + (this.isGiant() ? 2 : 0));
	}

	public void setEntTeam(int team) {
		this.dataManager.set(VIS_TEAM, (byte) team);
	}

	public void setDiff(int diff) {
		this.dataManager.set(DIFFICULTY, (byte) diff);
	}

	public Order getOrder() {
		return Order.values()[this.dataManager.get(ORDER)];
	}

	public void setOrder(Order order) {
		this.dataManager.set(ORDER, (byte) order.ordinal());
	}

	public int getMainWeapon() {
		return this.dataManager.get(MAIN_WEAPON);
	}

	public void setMainWeapon(int slot) {
		this.dataManager.set(MAIN_WEAPON, (byte) slot);
	}

	public boolean isSharing() {
		return this.dataManager.get(SHARE);
	}

	public boolean isRobot() {
		return this.dataManager.get(ROBOT) != 0;
	}

	public int getRobotSize() {
		return this.dataManager.get(ROBOT);
	}

	public boolean isGiant() {
		return this.dataManager.get(ROBOT) > 1;
	}

	public void setRobot(int robot) {
		this.dataManager.set(ROBOT, (byte) robot);
		if (robot > 0) {
			// this.unlimitedAmmo = true;
			if (this.getOwnerId() != null)
				TF2Util.addModifierSafe(this, SharedMonsterAttributes.FOLLOW_RANGE,
						new AttributeModifier(FOLLOW_MULT_UUID, "GiantRange", 0.4, 2), true);
			if (robot > 1) {
				this.stepHeight = 1f;
				this.tasks.removeTask(avoidSentry);
				TF2Util.addModifierSafe(this, SharedMonsterAttributes.ARMOR,
						new AttributeModifier(ARMOR_MULT_UUID, "GiantArmor", -0.5, 2), true);
				TF2Util.addModifierSafe(this, SharedMonsterAttributes.MAX_HEALTH,
						new AttributeModifier(HEALTH_MULT_UUID, "GiantHealth", (0.25 + this.robotStrength) * 2.5, 1),
						true);
				TF2Util.addModifierSafe(this, SharedMonsterAttributes.KNOCKBACK_RESISTANCE,
						new AttributeModifier(KNOCKBACK_MULT_UUID, "GiantKnockback", 0.6, 0), true);
				TF2Util.addModifierSafe(this, SharedMonsterAttributes.FOLLOW_RANGE,
						new AttributeModifier(FOLLOW_MULT_UUID, "GiantRange", 0.8, 2), true);
				this.setHealth(this.getMaxHealth());
				if (!(this instanceof EntityScout || this instanceof EntityMedic))
					TF2Util.addModifierSafe(this, SharedMonsterAttributes.MOVEMENT_SPEED,
							new AttributeModifier(SPEED_GIANT_MULT_UUID, "GiantSpeed", -0.43, 2), true);
			}
		}
	}

	public void setSharing(boolean share) {
		this.dataManager.set(SHARE, share);
	}

	public int getAmmo() {
		return this.getAmmo(this.usedSlot);
	}

	@Override
	public void setAIMoveSpeed(float speedIn) {
		if (this.isGiant()) {
			speedIn = Math.max(speedIn,
					(float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue()
							* (this.getRobotSize() == 2 ? 0.4f : 0.5f));
		}
		super.setAIMoveSpeed(speedIn);
	}

	@Override
	public boolean isPotionApplicable(PotionEffect potioneffectIn) {
		return !(this.isGiant() && potioneffectIn.getPotion() == TF2weapons.stun);
	}

	@Override
	public void setAttackTarget(EntityLivingBase target) {

		super.setAttackTarget(target);
		if (this.isTrading() && target != null && this.getCustomer() != this.getOwner()) {
			this.setCustomer(null);
			// System.out.println(target);
		}
		if (!this.alert)
			for (EntityTF2Character ent : this.world.getEntitiesWithinAABB(EntityTF2Character.class, new AxisAlignedBB(
					this.posX - 15, this.posY - 6, this.posZ - 15, this.posX + 15, this.posY + 6, this.posZ + 15)))
				if (TF2Util.isOnSameTeam(this, ent) && !TF2Util.isOnSameTeam(this, target)
						&& (ent.getAttackTarget() == null || ent.getAttackTarget().isDead)) {
					ent.alert = true;
					ent.alertTarget = target;
					ent.alertTime = ent.ticksExisted + 8 + this.getRNG().nextInt(14);

				}
	}

	public void useAmmo(int amount) {
		if (!this.unlimitedAmmo)
			this.ammoCount[this.usedSlot] -= amount;

	}

	public boolean shouldScaleAttributes() {
		return this.getAttackTarget() != null && TF2ConfigVars.scaleAttributes
				&& (this.getAttackTarget() instanceof EntityPlayer || this.getAttackTarget() instanceof IEntityOwnable
						&& ((IEntityOwnable) this.getAttackTarget()).getOwnerId() != null)
				&& this.getOwnerId() == null;
	}

	public float getAttributeModifier(String attribute) {
		float base = 1f;
		if (this.getActivePotionEffect(TF2weapons.sapped) != null)
			base *= 2f;
		if (shouldScaleAttributes())
			if (attribute.equals("Knockback"))
				base *= this.scaleWithDifficulty(0.4f, 1f);
			else if (attribute.equals("Fire Rate"))
				base *= this.scaleWithDifficulty(1.9f, 1f);
			else if (attribute.equals("Spread")) {
				base *= this.scaleWithDifficulty(1.9f, 1f);
			} else if (attribute.equals("Damage") && this.getHeldItemMainhand().getItem() instanceof ItemMeleeWeapon)
				base *= 1.25f;
		if (attribute.equals("Auto Fire"))
			return 0f;
		if (attribute.equals("Overload"))
			return 0f;
		return base;
	}

	public boolean hasHeldInventory() {
		return !(this.isRobot() && this.getOwnerId() != null);
	}

	public boolean isReloadPressed() {
		return true;
	}

	@Override
	public void onLivingUpdate() {

		if (this.world.isRemote && !Minecraft.getMinecraft().isIntegratedServerRunning()) {
			this.moveForward = this.dataManager.get(FRONT) / 128f;
			this.moveStrafing = this.dataManager.get(SIDE) / 128f;
		}

		super.onLivingUpdate();

		if (!this.world.isRemote) {
			this.dataManager.set(FRONT, (byte) MathHelper.clamp(Math.round(this.moveForward * 128f), -128, 127));
			this.dataManager.set(SIDE, (byte) MathHelper.clamp(Math.round(this.moveStrafing * 128f), -128, 127));
		}

		this.updateArmSwingProgress();
		ItemStack hat = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if (!hat.isEmpty() && hat.getItem() instanceof ItemWearable) {
			((ItemWearable) hat.getItem()).onUpdateWearing(hat, this.world, this);
		}
		ItemStack backpack = ItemBackpack.getBackpack(this);
		if (!backpack.isEmpty() && backpack.getItem() instanceof ItemBackpack) {
			((ItemBackpack) backpack.getItem()).onArmorTickAny(world, this, backpack);
		}
		if (this.getAttackTarget() != null && !this.getAttackTarget().isEntityAlive())
			this.setAttackTarget(null);
		if (!this.friendly && this.getAttackTarget() instanceof EntityTF2Character
				&& TF2Util.isOnSameTeam(this, this.getAttackTarget()))
			this.setAttackTarget(null);
		if (this.jump && this.onGround)
			this.jump();
		if (this.spawnMedic) {

			// medic.setEntTeam(event.getWorld().rand.nextInt(2));

			TF2CharacterAdditionalData data = new TF2CharacterAdditionalData();
			data.team = this.getEntTeam();
			data.natural = true;
			data.allowGiant = this.isGiant();

			EntityMedic medic = new EntityMedic(this.world);
			medic.setLocationAndAngles(this.posX + this.rand.nextDouble() * 0.5 - 0.25, this.posY,
					this.posZ + this.rand.nextDouble() * 0.5 - 0.25, this.rand.nextFloat() * 360.0F, 0.0F);
			medic.onInitialSpawn(this.world.getDifficultyForLocation(this.getPos()), data);

			this.world.spawnEntity(medic);
			this.spawnMedic = false;
		}

		if (!this.noAmmo && this.getAttackTarget() != null && Math.abs(this.rotationYaw
				- this.rotationYawHead) > 60/*
											 * TF2ActionHandler.playerAction.get ().get(this)!=null&&(
											 * TF2ActionHandler.playerAction.get ().get(this)&3)>0
											 */)
			if (this.rotationYawHead - this.rotationYaw > 60)
				this.rotationYaw = this.rotationYawHead + 60;
			else
				this.rotationYaw = this.rotationYawHead - 60;
		// if (this.hasHome()) {

		if (this.firstUpdate) {
			this.spawnPos = this.getPosition();
		}

		if (!this.world.isRemote) {
			if (this.isReloadPressed())
				this.getWepCapability().state |= 4;
			else if (!this.isReloadPressed()) {
				this.getWepCapability().state &= ~4;
			}
			if (this.ticksExisted % 2 == 0) {
				this.loadout.updateSlots();
			}
			if (this.isRobot()) {
				if (this.getAttackTarget() == null
						|| (this.friendly && this.getAttackTarget().getAttackingEntity() == null)) {
					this.idleTime += 1;
					if (this.idleTime > 800 + this.getRobotSize() * 500)
						this.setDead();
				}
				if (this.getActivePotionEffect(TF2weapons.sapped) != null) {
					this.rotationPitch = 40;
				}
			}
			if (this.getOrder() == Order.HOLD && !this.hasHome())
				this.setHomePosAndDistance(this.getPos(), 12);
			else if (this.getOrder() == Order.FOLLOW && this.hasHome())
				this.detachHome();

			this.setDiff(this.world.getDifficulty().getDifficultyId());
			if (this.isTrading() && (this.trader.getDistanceSq(trader) > 100 || !this.isEntityAlive()))
				this.setCustomer(null);

			if (this.getHeldItemMainhand().getItem() instanceof ItemUsable) {

				if (!((ItemUsable) this.getHeldItemMainhand().getItem()).isAmmoSufficient(this.getHeldItemMainhand(),
						this, true)) {
					if (!this.refill(this.usedSlot))
						this.switchSlot(this.getFirstSlotWithAmmo(), true, false);
				}

				// if(this.getAmmo() == 0)
				// System.out.println("IsSufficient "+
				// ((ItemUsable)this.getHeldItemMainhand().getItem()).isAmmoSufficient(this.getHeldItemMainhand(),
				// this, true));
				// this.noAmmo = true;
			}
			if (this.preferredSlot != this.usedSlot) {
				this.switchSlot(this.preferredSlot);
			}
			if (this.traderFollowTicks > 0) {
				this.traderFollowTicks--;
				if (this.followID != null && this.lastTrader == null) {
					this.lastTrader = this.world.getPlayerEntityByUUID(this.followID);
				}
			}
			if (this.refill.getStackInSlot(0).getItem() instanceof ItemAmmo
					|| this.refill.getStackInSlot(0).getItem() instanceof ItemArrow) {
				ItemStack ammoStack = this.refill.getStackInSlot(0);
				int ammoType = ammoStack.getItem() instanceof ItemAmmo
						? ((ItemAmmo) ammoStack.getItem()).getTypeInt(ammoStack)
						: 1000;
				for (int i = 0; i < this.ammoCount.length; i++) {
					if (this.getAmmo(i) < this.getMaxAmmo(i) && ammoType == ItemFromData
							.getData(this.loadout.getStackInSlot(i)).getInt(PropertyType.AMMO_TYPE)) {
						if (ammoStack.isItemStackDamageable()) {
							int oldAmmo = this.getMaxAmmo(i) - this.ammoCount[i];
							this.ammoCount[i] += Math.min((ammoStack.getMaxDamage() - ammoStack.getItemDamage()) * 2,
									this.getMaxAmmo(i) - this.ammoCount[i]);
							ammoStack.setItemDamage(ammoStack.getItemDamage() + oldAmmo);
							if (ammoStack.getItemDamage() > ammoStack.getMaxDamage())
								this.refill.setStackInSlot(0, ItemStack.EMPTY);

						} else
							this.ammoCount[i] += this.refill
									.extractItem(0, (this.getMaxAmmo(i) - this.ammoCount[i]) / 2, false).getCount() * 2;
					}

				}
			} else if (this.getActiveItemStack().isEmpty() && this.getAttackTarget() == null
					&& this.getHealth() / this.getMaxHealth() < 0.7f
					&& this.refill.getStackInSlot(0).getItem() instanceof ItemFood) {
				this.setHeldItem(EnumHand.OFF_HAND, this.refill.getStackInSlot(0));
				this.setActiveHand(EnumHand.OFF_HAND);
				// this.eating =
				// ((ItemFood)this.refill.getStackInSlot(0).getItem()).getHealAmount(this.refill.getStackInSlot(0));
			}

			/*
			 * if(this.eating > 0 && this.ticksExisted % 20 == 0) { this.heal(Math.min(3,
			 * this.eating)); this.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,
			 * 20)); this.playSound(SoundEvents.PLAYER, volume, pitch); this.eating
			 * -=Math.min(3, this.eating); }
			 */
			if (this.alert && this.alertTarget != null && this.alertTime == this.ticksExisted) {
				this.setAttackTarget(this.alertTarget);
				this.alert = false;
			}
		}

		for (int i = 19; i > 0; i--)
			this.lastRotation[i] = this.lastRotation[i - 1];
		this.lastRotation[0] = (float) Math.sqrt((this.rotationYawHead - this.prevRotationYawHead)
				* (this.rotationYawHead - this.prevRotationYawHead)
				+ (this.rotationPitch - this.prevRotationPitch) * (this.rotationPitch - this.prevRotationPitch));

	}

	@Override
	protected float getWaterSlowDown() {
		return this.getOwnerId() != null ? 1.0F : 0.9F;
	}

	@Override
	public void travel(float m1, float m2, float m3) {
		float move = this.getAIMoveSpeed();
		super.travel(m1 / move, m2, m3 / move);
		if (!this.isServerWorld() && !Minecraft.getMinecraft().isIntegratedServerRunning()) {
			if (!this.isInWater()) {
				if (!this.isInLava()) {
					if (this.isElytraFlying()) {
						if (this.motionY > -0.5D) {
							this.fallDistance = 1.0F;
						}

						Vec3d vec3d = this.getLookVec();
						float f = this.rotationPitch * 0.017453292F;
						double d6 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
						double d8 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
						double d1 = vec3d.lengthVector();
						float f4 = MathHelper.cos(f);
						f4 = (float) ((double) f4 * (double) f4 * Math.min(1.0D, d1 / 0.4D));
						this.motionY += -0.08D + f4 * 0.06D;

						if (this.motionY < 0.0D && d6 > 0.0D) {
							double d2 = this.motionY * -0.1D * f4;
							this.motionY += d2;
							this.motionX += vec3d.x * d2 / d6;
							this.motionZ += vec3d.z * d2 / d6;
						}

						if (f < 0.0F) {
							double d10 = d8 * (-MathHelper.sin(f)) * 0.04D;
							this.motionY += d10 * 3.2D;
							this.motionX -= vec3d.x * d10 / d6;
							this.motionZ -= vec3d.z * d10 / d6;
						}

						if (d6 > 0.0D) {
							this.motionX += (vec3d.x / d6 * d8 - this.motionX) * 0.1D;
							this.motionZ += (vec3d.z / d6 * d8 - this.motionZ) * 0.1D;
						}

						this.motionX *= 0.9900000095367432D;
						this.motionY *= 0.9800000190734863D;
						this.motionZ *= 0.9900000095367432D;
						this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

						if (this.collidedHorizontally && !this.world.isRemote) {
							double d11 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
							double d3 = d8 - d11;
							float f5 = (float) (d3 * 10.0D - 3.0D);

							if (f5 > 0.0F) {
								this.playSound(this.getFallSound((int) f5), 1.0F, 1.0F);
								this.attackEntityFrom(DamageSource.FLY_INTO_WALL, f5);
							}
						}

						if (this.onGround && !this.world.isRemote) {
							this.setFlag(7, false);
						}
					} else {
						float f6 = 0.91F;
						BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos
								.retain(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ);

						if (this.onGround) {
							IBlockState underState = this.world.getBlockState(blockpos$pooledmutableblockpos);
							f6 = underState.getBlock().getSlipperiness(underState, this.world,
									blockpos$pooledmutableblockpos, this) * 0.91F;
						}

						float f7 = 0.16277136F / (f6 * f6 * f6);
						float f8;

						if (this.onGround) {
							f8 = f7;
						} else {
							f8 = this.jumpMovementFactor;
						}

						this.moveRelativeLook(m1, m2, m3, f8);
						f6 = 0.91F;

						if (this.onGround) {
							IBlockState underState = this.world.getBlockState(blockpos$pooledmutableblockpos
									.setPos(this.posX, this.getEntityBoundingBox().minY - 1.0D, this.posZ));
							f6 = underState.getBlock().getSlipperiness(underState, this.world,
									blockpos$pooledmutableblockpos, this) * 0.91F;
						}

						if (this.isOnLadder()) {
							float f9 = 0.15F;
							this.motionX = MathHelper.clamp(this.motionX, -0.15000000596046448D, 0.15000000596046448D);
							this.motionZ = MathHelper.clamp(this.motionZ, -0.15000000596046448D, 0.15000000596046448D);
							this.fallDistance = 0.0F;

							if (this.motionY < -0.15D) {
								this.motionY = -0.15D;
							}
						}

						this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

						if (this.collidedHorizontally && this.isOnLadder()) {
							this.motionY = 0.2D;
						}

						if (this.isPotionActive(MobEffects.LEVITATION)) {
							this.motionY += (0.05D
									* (this.getActivePotionEffect(MobEffects.LEVITATION).getAmplifier() + 1)
									- this.motionY) * 0.2D;
						} else {
							blockpos$pooledmutableblockpos.setPos(this.posX, 0.0D, this.posZ);

							if (!this.world.isRemote || this.world.isBlockLoaded(blockpos$pooledmutableblockpos)
									&& this.world.getChunkFromBlockCoords(blockpos$pooledmutableblockpos).isLoaded()) {
								if (!this.hasNoGravity()) {
									this.motionY -= 0.08D;
								}
							} else if (this.posY > 0.0D) {
								this.motionY = -0.1D;
							} else {
								this.motionY = 0.0D;
							}
						}

						this.motionY *= 0.9800000190734863D;
						this.motionX *= f6;
						this.motionZ *= f6;
						blockpos$pooledmutableblockpos.release();
					}
				} else {
					double d4 = this.posY;

					this.moveRelative(m1, m2, m3, 0.02F);
					this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
					this.motionX *= 0.5D;
					this.motionY *= 0.5D;
					this.motionZ *= 0.5D;

					if (!this.hasNoGravity()) {
						this.motionY -= 0.02D;
					}

					if (this.collidedHorizontally && this.isOffsetPositionInLiquid(this.motionX,
							this.motionY + 0.6000000238418579D - this.posY + d4, this.motionZ)) {
						this.motionY = 0.30000001192092896D;
					}
				}
			} else {
				double d0 = this.posY;
				float f1 = this.getWaterSlowDown();
				float f2 = 0.02F;
				float f3 = EnchantmentHelper.getDepthStriderModifier(this);

				if (f3 > 3.0F) {
					f3 = 3.0F;
				}

				if (!this.onGround) {
					f3 *= 0.5F;
				}

				if (f3 > 0.0F) {
					f1 += (0.54600006F - f1) * f3 / 3.0F;
					f2 += (this.getAIMoveSpeed() - f2) * f3 / 3.0F;
				}

				this.moveRelative(m1, m2, m3, f2);
				this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
				this.motionX *= f1;
				this.motionY *= 0.800000011920929D;
				this.motionZ *= f1;

				if (!this.hasNoGravity()) {
					this.motionY -= 0.02D;
				}

				if (this.collidedHorizontally && this.isOffsetPositionInLiquid(this.motionX,
						this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ)) {
					this.motionY = 0.30000001192092896D;
				}
			}
			this.prevLimbSwingAmount = this.limbSwingAmount;
			double d5 = this.posX - this.prevPosX;
			double d7 = this.posZ - this.prevPosZ;
			double d9 = this instanceof net.minecraft.entity.passive.EntityFlying ? this.posY - this.prevPosY : 0.0D;
			float f10 = MathHelper.sqrt(d5 * d5 + d9 * d9 + d7 * d7) * 4.0F;

			if (f10 > 1.0F) {
				f10 = 1.0F;
			}

			this.limbSwingAmount += (f10 - this.limbSwingAmount) * 0.4F;
			this.limbSwing += this.limbSwingAmount;
		}

	}

	private void moveRelativeLook(float strafe, float up, float forward, float friction) {
		float f = strafe * strafe + up * up + forward * forward;
		if (f >= 1.0E-4F) {
			f = MathHelper.sqrt(f);
			if (f < 1.0F)
				f = 1.0F;
			f = friction / f;
			strafe = strafe * f;
			up = up * f;
			forward = forward * f;
			if (this.isInWater() || this.isInLava()) {
				strafe = strafe * (float) this.getEntityAttribute(SWIM_SPEED).getAttributeValue();
				up = up * (float) this.getEntityAttribute(SWIM_SPEED).getAttributeValue();
				forward = forward * (float) this.getEntityAttribute(SWIM_SPEED).getAttributeValue();
			}
			float f1 = MathHelper.sin(this.rotationYawHead * 0.017453292F);
			float f2 = MathHelper.cos(this.rotationYawHead * 0.017453292F);
			this.motionX += strafe * f2 - forward * f1;
			this.motionY += up;
			this.motionZ += forward * f2 + strafe * f1;
		}
	}

	@Override
	protected void onItemUseFinish() {
		if (!this.activeItemStack.isEmpty() && this.isHandActive()
				&& this.activeItemStack.getItemUseAction() == EnumAction.EAT) {
			this.heal(((ItemFood) this.activeItemStack.getItem()).getHealAmount(activeItemStack));
		}
		super.onItemUseFinish();
		this.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
	}

	public boolean canBecomeGiant() {
		return true;
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance p_180482_1_, IEntityLivingData entityLivingData) {
		super.onInitialSpawn(p_180482_1_, entityLivingData);
		TF2CharacterAdditionalData data;
		if (!(entityLivingData instanceof TF2CharacterAdditionalData)) {
			data = new TF2CharacterAdditionalData();
			data.natural = true;
			if (nextEntTeam >= 0) {
				data.team = nextEntTeam;
				nextEntTeam = -1;
			}

			if (this.bannerTeam != -1) {
				data.team = this.bannerTeam;
			} else {
				List<EntityTF2Character> list = this.world.getEntitiesWithinAABB(EntityTF2Character.class,
						this.getEntityBoundingBox().grow(40, 4.0D, 40), null);

				boolean found = false;
				for (EntityTF2Character ent : list) {
					if (ent.getOwnerId() == null) {
						found = true;
						data.team = list.get(0).getEntTeam();
						break;
					}
				}
				if (!found)
					data.team = this.rand.nextInt(2);
			}
		} else
			data = (TF2CharacterAdditionalData) entityLivingData;
		if (entityLivingData instanceof TF2CharacterAdditionalData) {
			this.natural = (data.natural);
			this.setEntTeam(data.team);
			this.noEquipment = (data.noEquipment);
		}
		if (this.natural) {

		}
		if (data.team == 2) {
			this.setRobot(1);
			this.setMoneyDrop(7);
			if (data.isGiant /* || (data.natural && this.canBecomeGiant() && data.allowGiant) */) {
				// this.spawnMedic = data.natural && this.rand.nextFloat() < 0.15 *
				// this.robotStrength + 0.15;
				this.setRobot(2);
				this.setMoneyDrop(14 + (this.difficulty * 10));
			}
		}
		this.addWeapons();
		this.setEquipmentBasedOnDifficulty(p_180482_1_);
		this.switchSlot(this.getDefaultSlot(), false, true);

		int i = 3;
		for (ItemStack stack : this.getArmorInventoryList()) {
			this.isEmpty[i] = stack.isEmpty();
			i--;
		}
		if (!this.world.getGameRules().getBoolean("doTF2AI")) {
			this.tasks.taskEntries.clear();
			this.targetTasks.taskEntries.clear();
		}
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
				.applyModifier(new AttributeModifier("damageModifier", TF2ConfigVars.damageMultiplier - 1, 2));
		this.applySpeed();
		this.setHealth(this.getMaxHealth());
		for (int j = 0; j < this.ammoCount.length; j++) {
			this.ammoCount[j] = this.getMaxAmmo(j);
			if (this.isRobot())
				this.ammoCount[j] *= 20;
		}

		return data;
	}

	public void applySpeed() {
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SPEED_MULT_UUID);
		if (this.getOwnerId() == null && this.getRobotSize() < 2)
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(
					new AttributeModifier(SPEED_MULT_UUID, "speedModifier", TF2ConfigVars.speedMult - 1, 2));
	}

	public int getDefaultSlot() {
		return 0;
	}

	@Override
	protected float getJumpUpwardsMotion() {
		return 0.5f;
	}

	@Override
	public void onDeath(DamageSource s) {
		if (s.getTrueSource() != null && s.getTrueSource() instanceof EntityPlayerMP
				&& !TF2Util.isOnSameTeam(this, s.getTrueSource())) {
			EntityPlayerMP player = (EntityPlayerMP) s.getTrueSource();
			if (s.getTrueSource().getTeam() != null || TF2ConfigVars.neutralAttack) {
				if (!this.isRobot()) {
					player.addStat(TF2Achievements.KILLED_MERC);
					if (player.getStatFile().readStat(TF2Achievements.KILLED_MERC) >= 5 && player.getStatFile()
							.readStat(TF2Achievements.CONTRACT_DAY) == 0/*
																		 * player.getCapability(TF2weapons.PLAYER_CAP,
																		 * null).nextContractDay == -1
																		 */)
						player.addStat(TF2Achievements.CONTRACT_DAY, (int) (this.world.getWorldTime() / 24000 + 1));
				} else {
					player.addStat(TF2weapons.robotsKilled);
					TF2PlayerCapability.get(player).robotsKilledInvasion += 1;
				}
			}
			// player.addStat(TF2Achievements.FIRST_ENCOUNTER);

		}
		if (this.attackingPlayer != null && TF2Util.isOnSameTeam(this, this.attackingPlayer))
			this.experienceValue = 0;
		super.onDeath(s);
	}

	public boolean isValidTarget(EntityLivingBase living) {
		boolean hostilemob = false;
		if (TF2ConfigVars.attackMobs && !this.isRobot() && living instanceof EntityLiving
				&& TF2Util.isHostile(living)) {
			double rangesq = living.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue() + 1;
			rangesq *= rangesq;
			hostilemob = living.getDistanceSq(EntityTF2Character.this) < rangesq;
		}
		if (living.getTeam() != null || (TF2ConfigVars.neutralAttack && living instanceof EntityPlayer) || hostilemob) {
			return !TF2Util.isOnSameTeam(EntityTF2Character.this, living)
					&& (!(living instanceof EntityTF2Character && TF2ConfigVars.naturalCheck.equals("Never"))
							|| (!((EntityTF2Character) living).natural || !natural));
		}
		return false;
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (!(player.getHeldItemMainhand() != null
				&& player.getHeldItemMainhand().getItem() instanceof ItemMonsterPlacerPlus)
				&& (this.getOwner() == player || this.getAttackTarget() == null || this.friendly)
				&& this.isEntityAlive() && !this.isTrading() && !this.isChild() && !player.isSneaking()) {
			if (this.world.isRemote && !this.isRobot() && player.getTeam() == null
					&& ((this.getCapability(TF2weapons.WEAPONS_CAP, null).state & 1) == 0 || this.friendly)
					&& !player.isCreative())
				ClientProxy.displayScreenConfirm("Choose a team to interact",
						"Visit the Mann Co. Store located in a village");
			if (!this.world.isRemote && (TF2Util.isOnSameTeam(this, player) || player.isCreative())) {
				this.setCustomer(player);
				FMLNetworkHandler.openGui(player, TF2weapons.instance, 4, world, this.getEntityId(), 0, 0);
			}

			player.addStat(StatList.TALKED_TO_VILLAGER);
			return true;
		} else
			return super.processInteract(player, hand);
	}

	public boolean isTrading() {
		return this.trader != null;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);

		if (tag.getCompoundTag("ForgeCaps").hasKey(TF2weapons.MOD_ID + ":weaponscap")) {
			tag.setTag("WeaponsCap", tag.getCompoundTag("ForgeCaps").getCompoundTag(TF2weapons.MOD_ID + ":weaponscap"));
			tag.getCompoundTag("ForgeCaps").removeTag(TF2weapons.MOD_ID + ":weaponscap");
		}
		this.weaponCap.deserializeNBT(tag.getCompoundTag("WeaponsCap"));
		// this.wearablesCap.deserializeNBT(tag.getTagList("WearablesCap", 10));

		if (tag.hasKey("AmmoC"))
			this.ammoCount = tag.getIntArray("AmmoC");
		if (this.ammoCount.length == 0)
			this.ammoCount = new int[3];
		this.unlimitedAmmo = tag.getBoolean("UnlimitedAmmo");
		this.setEntTeam(tag.getByte("Team"));
		this.natural = tag.getBoolean("Natural");
		this.robotStrength = tag.getFloat("RobotStrength");
		this.setRobot(tag.getByte("Robot"));
		this.setMoneyDrop(tag.getInteger("MoneyDrop"));

		if (tag.getTag("Loadout") instanceof NBTTagList) {
			NBTTagList list = (NBTTagList) tag.getTag("Loadout");

			if (list != null) {
				for (int i = 0; i < list.tagCount(); ++i) {
					NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
					int j = nbttagcompound.getByte("Slot");
					this.loadout.setStackInSlot(j, new ItemStack(nbttagcompound));
				}
			}
		} else
			this.loadout.deserializeNBT(tag.getCompoundTag("Loadout"));
		this.loadoutHeld.deserializeNBT(tag.getCompoundTag("LoadoutHeld"));

		if (this.loadoutHeld.getSlots() < 8) {
			this.loadoutHeld.setSize(8);
			this.loadoutHeld.setStackInSlot(7, this.loadoutHeld.getStackInSlot(6));
			this.loadoutHeld.setStackInSlot(6, this.loadoutHeld.getStackInSlot(5));
			this.loadoutHeld.setStackInSlot(5, this.loadoutHeld.getStackInSlot(4));
			this.loadoutHeld.setStackInSlot(4, this.loadoutHeld.getStackInSlot(3));
		}

		this.refill.setStackInSlot(0, new ItemStack(tag.getCompoundTag("Refill")));

		if (tag.hasKey("Offers")) {
			this.tradeOffers = new MerchantRecipeList();
			this.tradeOffers.readRecipiesFromTags(tag.getCompoundTag("Offers"));
		}
		this.preferredSlot = tag.getByte("PSlot");
		this.switchSlot(tag.getByte("Slot"), true, true);
		this.setMainWeapon(tag.getByte("MainWeapon"));
		if (tag.hasKey("FollowTrader")) {
			this.followID = tag.getUniqueId("FollowTrader");
			this.traderFollowTicks = tag.getInteger("FollowTraderTicks");
		}

		if (!this.world.getGameRules().getBoolean("doTF2AI")) {
			this.tasks.taskEntries.clear();
			this.targetTasks.taskEntries.clear();
		}

		if (tag.hasUniqueId("Owner")) {
			UUID ownerID = tag.getUniqueId("Owner");
			this.dataManager.set(OWNER_UUID, Optional.of(ownerID));
			this.ownerName = tag.getString("OwnerName");
			this.getOwner();
			this.enablePersistence();
			this.setOrder(Order.values()[tag.getByte("Order")]);

			// this.world.getCapability(TF2weapons.WORLD_CAP,
			// null).lostMercPos.remove(this.getOwnerId(), this.getPos());
			PlayerPersistStorage.get(this.world, this.getOwnerId()).medicMercPos.remove(this.getPos());
			PlayerPersistStorage.get(this.world, this.getOwnerId()).restMercPos.remove(this.getPos());
		}

		this.tradeLevel = tag.getByte("TradeLevel");
		this.difficulty = tag.getByte("Difficulty");
		byte[] empty = tag.getByteArray("Empty");
		for (int i = 0; i < empty.length; i++) {
			this.isEmpty[i] = empty[i] != 0;
		}

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		IAttributeInstance speed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		for (AttributeModifier modifier : playerAttributes) {
			speed.removeModifier(modifier);
		}

		super.writeEntityToNBT(tag);
		tag.setTag("WeaponsCap", this.weaponCap.serializeNBT());
		// tag.setTag("WearablesTag", this.wearablesCap.serializeNBT());

		tag.setIntArray("AmmoC", ammoCount);
		tag.setBoolean("UnlimitedAmmo", this.unlimitedAmmo);
		tag.setByte("Team", (byte) this.getEntTeam());
		tag.setBoolean("Natural", this.natural);
		/*
		 * NBTTagList list = new NBTTagList();
		 *
		 * for (int i = 0; i < this.loadout.getSlots(); i++) { ItemStack itemstack =
		 * this.loadout.getStackInSlot(i);
		 *
		 * if (!itemstack.isEmpty()) { NBTTagCompound nbttagcompound = new
		 * NBTTagCompound(); nbttagcompound.setByte("Slot", (byte) i);
		 * itemstack.writeToNBT(nbttagcompound); list.appendTag(nbttagcompound); } }
		 * tag.setTag("Loadout", list);
		 */
		tag.setByte("Robot", (byte) this.getRobotSize());
		tag.setTag("Loadout", this.loadout.serializeNBT());
		tag.setTag("LoadoutHeld", loadoutHeld.serializeNBT());
		tag.setTag("Refill", this.refill.getStackInSlot(0).serializeNBT());
		if (this.tradeOffers != null)
			tag.setTag("Offers", this.tradeOffers.getRecipiesAsTags());
		if (this.usedSlot == -1)
			this.usedSlot = this.getDefaultSlot();
		tag.setByte("Slot", (byte) this.usedSlot);
		tag.setByte("PSlot", (byte) this.preferredSlot);
		if (this.lastTrader != null) {
			tag.setUniqueId("TraderFollow", this.lastTrader.getUniqueID());
			tag.setInteger("TraderFollowTicks", this.traderFollowTicks);
		}
		if (this.getOwnerId() != null) {
			tag.setUniqueId("Owner", this.getOwnerId());
			tag.setString("OwnerName", this.ownerName);
			tag.setByte("Order", (byte) this.getOrder().ordinal());
			if (this.getOrder() == Order.FOLLOW) {
				this.world.getCapability(TF2weapons.WORLD_CAP, null).getPlayerStorage(this.getOwnerId()).lostMercPos
						.add(this.getPos());
				this.world.getCapability(TF2weapons.WORLD_CAP, null).getPlayerStorage(this.getOwnerId()).setSave();
			} else if (this.getOrder() == Order.HOLD && this instanceof EntityMedic) {
				this.world.getCapability(TF2weapons.WORLD_CAP, null).getPlayerStorage(this.getOwnerId()).medicMercPos
						.add(this.getPos());
				this.world.getCapability(TF2weapons.WORLD_CAP, null).getPlayerStorage(this.getOwnerId()).setSave();
			} else if (this.getOrder() == Order.HOLD && !(this instanceof EntityEngineer)) {
				this.world.getCapability(TF2weapons.WORLD_CAP, null).getPlayerStorage(this.getOwnerId()).restMercPos
						.add(this.getPos());
				this.world.getCapability(TF2weapons.WORLD_CAP, null).getPlayerStorage(this.getOwnerId()).setSave();
			}
		}
		tag.setByte("MainWeapon", (byte) this.getMainWeapon());
		tag.setByte("TradeLevel", (byte) this.tradeLevel);
		tag.setByteArray("Empty", new byte[] { (byte) (isEmpty[0] ? 1 : 0), (byte) (isEmpty[1] ? 1 : 0),
				(byte) (isEmpty[2] ? 1 : 0), (byte) (isEmpty[3] ? 1 : 0) });
		tag.setByte("Difficulty", (byte) this.difficulty);
		tag.setFloat("RobotStrength", (short) this.robotStrength);
		tag.setInteger("MoneyDrop", this.moneyDrop);

	}

	@Override
	public boolean getCanSpawnHere() {
		if (detectBanner())
			return this.world.getDifficulty() != EnumDifficulty.PEACEFUL && super.getCanSpawnHere();

		if (this.isDead || (TF2ConfigVars.overworldOnly && this.dimension != 0)) {
			return false;
		}

		boolean naturalGround = TF2Util.isNaturalBlock(world, this.getPosition().down(),
				world.getBlockState(this.getPosition().down()));
		boolean validLight = this.isValidLightLevel(naturalGround, this.isRobot());

		if (!validLight)
			return false;

		return this.world.getDifficulty() != EnumDifficulty.PEACEFUL && super.getCanSpawnHere();
	}

	public boolean detectBanner() {
		// System.out.println("Bannrs: "+this.world.getCapability(TF2weapons.WORLD_CAP,
		// null).banners.size());
		Iterator<BlockPos> iterator = this.world.getCapability(TF2weapons.WORLD_CAP, null).banners.iterator();
		while (iterator.hasNext()) {
			BlockPos pos = iterator.next();
			if (pos.distanceSq(this.getPosition()) < 1200) {
				TileEntity banner = this.world.getTileEntity(pos);
				if (banner != null && banner instanceof TileEntityBanner) {
					boolean fast = false;
					for (BannerPattern pattern : TF2EventsCommon.getPatterns((TileEntityBanner) banner)) {
						if (pattern == TF2weapons.redPattern)
							this.bannerTeam = 0;
						else if (pattern == TF2weapons.bluPattern)
							this.bannerTeam = 1;
						else if (pattern == TF2weapons.neutralPattern) {
							this.setDead();
							return false;
						} else if (pattern == TF2weapons.fastSpawn)
							fast = true;
					}
					return fast && pos.distanceSq(this.getPosition()) < 800;
				} else {
					iterator.remove();
					return false;
				}
			}
		}
		return false;
	}

	@Override
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
		if (!this.isEntityInvulnerable(damageSrc)) {
			damageAmount = net.minecraftforge.common.ForgeHooks.onLivingHurt(this, damageSrc, damageAmount);
			if (damageAmount <= 0)
				return;
			damageAmount = net.minecraftforge.common.ISpecialArmor.ArmorProperties.applyArmor(this,
					(NonNullList<ItemStack>) this.getArmorInventoryList(), damageSrc, damageAmount);
			if (damageAmount <= 0)
				return;
			damageAmount = this.applyPotionDamageCalculations(damageSrc, damageAmount);
			float f = damageAmount;
			damageAmount = Math.max(damageAmount - this.getAbsorptionAmount(), 0.0F);
			this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - damageAmount));
			damageAmount = net.minecraftforge.common.ForgeHooks.onLivingDamage(this, damageSrc, damageAmount);

			if (damageAmount != 0.0F) {
				float f1 = this.getHealth();
				this.setHealth(this.getHealth() - damageAmount);
				this.getCombatTracker().trackDamage(damageSrc, f1, damageAmount);
			}
		}
	}

	protected boolean isValidLightLevel(boolean isNaturalBlock, boolean event) {

		BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
		int skylight = this.world.getLightFor(EnumSkyBlock.SKY, blockpos);
		if (16 + skylight < this.rand.nextInt(32))
			return false;
		else {
			int lightbl = this.world.getLightFor(EnumSkyBlock.BLOCK, blockpos);
			if ((!isNaturalBlock && skylight < 13) || (lightbl > 1 && !event))
				return false;
			int i = this.world.getLightFromNeighbors(blockpos);
			/*
			 * if (this.world.isThundering()) { int j = this.world.getSkylightSubtracted();
			 * this.world.setSkylightSubtracted(10); i =
			 * this.world.getLightFromNeighbors(blockpos);
			 * this.world.setSkylightSubtracted(j); }
			 */
			Chunk chunk = this.world.getChunkFromBlockCoords(blockpos);
			boolean spawnDay = event
					|| (this.rand.nextInt(32) == 0 && chunk.getRandomWithSeed(987234911L).nextInt(10) == 0);
			return spawnDay || i <= 4 + this.rand.nextInt(4);
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.world.isRemote && this.getOwnerId() == null && this.world.getDifficulty() == EnumDifficulty.PEACEFUL)
			this.setDead();
	}

	@Override
	public Team getTeam() {
		if (this.getOwner() != null)
			return this.getOwner().getTeam();
		else if (this.ownerName != null && !this.ownerName.isEmpty())
			return this.world.getScoreboard().getPlayersTeam(this.ownerName);
		return this.getEntTeam() == 0 ? this.world.getScoreboard().getTeam("RED")
				: this.getEntTeam() == 1 ? this.world.getScoreboard().getTeam("BLU")
						: this.world.getScoreboard().getTeam("Robots");
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.ENTITY_HOSTILE_SWIM;
	}

	@Override
	protected SoundEvent getSplashSound() {
		return SoundEvents.ENTITY_HOSTILE_SPLASH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block blockIn) {
		this.playSound(SoundEvents.ENTITY_ZOMBIE_STEP, 0.15F, 1.0F);
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.recentlyHit > 0 && source == DamageSource.MAGIC)
			this.recentlyHit = Math.max(20, this.recentlyHit);
		if (this.isEntityInvulnerable(source))
			return false;
		else if (super.attackEntityFrom(source, amount)) {
			if (event != null && (!(source.getTrueSource() instanceof EntityLivingBase)
					|| source.getTrueSource() == TF2weapons.dummyEnt)) {
				event.onDamageEnv(this, source, amount);
				this.damagedByEnv = true;
			}
			if (event != null && (source.getImmediateSource() instanceof EntitySentry)) {
				event.onDamageSentry(this, (EntitySentry) source.getImmediateSource(), source, amount);
			}
			Entity entity = source.getTrueSource();
			return this.getRidingEntity() != entity && this.getRidingEntity() != entity ? true : true;
		} else
			return false;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_HOSTILE_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_HOSTILE_DEATH;
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		float f = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
		int i = 0;

		if (entityIn instanceof EntityLivingBase) {
			f += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(),
					((EntityLivingBase) entityIn).getCreatureAttribute());
			i += EnchantmentHelper.getKnockbackModifier(this);
		}

		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

		if (flag) {
			if (i > 0 && entityIn instanceof EntityLivingBase) {
				((EntityLivingBase) entityIn).knockBack(this, i * 0.5F, MathHelper.sin(this.rotationYaw * 0.017453292F),
						(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
				this.motionX *= 0.6D;
				this.motionZ *= 0.6D;
			}

			int j = EnchantmentHelper.getFireAspectModifier(this);

			if (j > 0)
				entityIn.setFire(j * 4);

			if (entityIn instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer) entityIn;
				ItemStack itemstack = this.getHeldItemMainhand();
				ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : null;

				if (!itemstack.isEmpty() && itemstack1 != null && itemstack.getItem() instanceof ItemAxe
						&& itemstack1.getItem() == Items.SHIELD) {
					float f1 = 0.25F + EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

					if (this.rand.nextFloat() < f1) {
						entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
						this.world.setEntityState(entityplayer, (byte) 30);
					}
				}
			}

			this.applyEnchantments(this, entityIn);
		}

		return flag;
	}

	/**
	 * Checks if the entity's current position is a valid location to spawn this
	 * entity.
	 */

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
	}

	@Override
	protected boolean canDropLoot() {
		return true;
	}

	@Override
	public int getTalkInterval() {
		return this.getOwnerId() != null ? 1000 : 320;
	}

	@Override
	protected float getSoundPitch() {
		return this.isChild() ? (this.rand.nextFloat() - this.rand.nextFloat()) * 0.08F + 1.5F
				: (this.rand.nextFloat() - this.rand.nextFloat()) * 0.08F + 1.0F;
	}

	@Override
	protected float getSoundVolume() {
		return TF2ConfigVars.mercenaryVolume;
	}

	public float getMotionSensitivity() {
		return this.scaleWithDifficulty(0.18f, 0.03f);
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
		if (!this.world.isRemote && slotIn == EntityEquipmentSlot.MAINHAND)
			return this.loadout.getStackInSlot(this.usedSlot);
		else
			return super.getItemStackFromSlot(slotIn);
	}

	public void onShot() {

	}

	@Override
	protected boolean canDespawn() {
		return this.natural && this.getOwnerId() == null;
	}

	/*
	 * @Nullable protected ResourceLocation getLootTable() { return
	 * TF2weapons.lootTF2Character; }
	 */
	@Override
	public void setCustomer(EntityPlayer player) {
		this.trader = player;
	}

	@Override
	public EntityPlayer getCustomer() {
		return this.trader;
	}

	@Override
	public MerchantRecipeList getRecipes(EntityPlayer player) {
		if (this.tradeOffers == null)
			makeOffers();
		return tradeOffers;
	}

	public void makeOffers() {
		this.tradeOffers = new MerchantRecipeList();
		if (this.isRobot() || this.tradeLevel == -1)
			return;
		int weaponCount = 1 + this.rand.nextInt(2 + this.tradeLevel);
		for (int i = 0; i < weaponCount; i++) {

			boolean buyItem = this.rand.nextBoolean();
			int slot = getValidSlots()[this.rand.nextInt(getValidSlots().length)];

			ItemStack item = ItemFromData.getRandomWeaponOfSlotMob(getTF2Class(), slot, this.getRNG(), false, 1, false);

			if (!item.isEmpty()) {
				int cost = ItemFromData.getData(item).getInt(PropertyType.COST);
				int basecost = cost;
				if (!buyItem) {
					if (this.tradeLevel == 1 && this.rand.nextFloat() < 0.6f) {
						int upgrade = Math.min(800, 232 + (int) (this.world.getWorldTime() / 4000));
						TF2Attribute.upgradeItemStack(this.loadout.getStackInSlot(0), upgrade, rand);
						cost += upgrade / cost > 13 ? 5 : 10;
						if (this.rand.nextFloat() < 0.25f) {
							item.getTagCompound().setBoolean("Strange", true);
							cost += basecost * 8 + cost * 0.12f;
						}
					} else if (this.tradeLevel == 2) {
						int upgrade = Math.min(800, 232 + (int) (this.world.getWorldTime() / 4000));
						TF2Attribute.upgradeItemStack(this.loadout.getStackInSlot(0),
								Math.min(3000, 1000 + (int) (this.world.getWorldTime() / 2000)), rand);
						cost += upgrade / cost > 13 ? 5 : 10;
						if (this.rand.nextFloat() < 0.6f) {
							item.getTagCompound().setBoolean("Strange", true);
							cost += basecost * 8 + cost * 0.12f;
						}
						if (this.rand.nextFloat() < 0.25f) {
							item.getTagCompound().setBoolean("Australium", true);
							cost += basecost * 16 + cost * 0.06f;
						}
						if (this.rand.nextFloat() < 0.3f) {
							ArrayList<TF2Attribute> list = new ArrayList<>(Arrays.asList(TF2Attribute.attributes));
							list.removeIf(attr -> attr == null || attr.perKill == 0);
							item.getTagCompound().setShort("StreakAttrib",
									(short) list.get(this.rand.nextInt(list.size())).id);
							float rand = this.rand.nextFloat();
							int level = (rand < 0.15 ? 3 : rand < 0.5 ? 2 : 1);
							item.getTagCompound().setByte("StreakLevel", (byte) level);
							cost += basecost * 8 * level + cost * 0.08f;
						}
					}
				}
				int costmult = cost < 27 || buyItem ? 0 : cost < 81 ? 1 : 2;
				ItemStack metal = new ItemStack(TF2weapons.itemTF2,
						Math.max(1, (cost / (costmult == 0 ? 9 : costmult == 1 ? 27 : 81))), 3 + costmult);
				ItemStack metal2 = new ItemStack(TF2weapons.itemTF2, Math.max(1, (cost / (costmult == 1 ? 9 : 27)) % 3),
						2 + costmult);
				this.tradeOffers.add(new MerchantRecipe(buyItem ? item : metal,
						!buyItem && costmult > 0 ? metal2 : ItemStack.EMPTY, buyItem ? metal : item, 0, 1));
			}
		}
		int hatCount = this.rand.nextInt(2 + this.tradeLevel);

		for (int i = 0; i < hatCount; i++) {

			boolean buyItem = this.rand.nextBoolean();
			ItemStack item = ItemFromData.getRandomWeaponOfType("cosmetic", this.rand, false);
			int cost = Math.max(1, ItemFromData.getData(item).getInt(PropertyType.COST));
			if (i == 0 && this.tradeLevel == 2) {
				((ItemWearable) item.getItem()).applyRandomEffect(item, rand);
				cost *= 6 + this.rand.nextInt(6);
				buyItem = false;
			}
			ItemStack metal = new ItemStack(TF2weapons.itemTF2, cost / 18, 5);
			ItemStack metal2 = new ItemStack(TF2weapons.itemTF2, this.rand.nextInt(3), 4);
			if (metal2.getCount() == 0)
				metal2 = ItemStack.EMPTY;

			this.tradeOffers.add(new MerchantRecipe(buyItem ? item : metal, buyItem ? ItemStack.EMPTY : metal2,
					buyItem ? metal : item, 0, 1));
		}

		if (this.tradeLevel >= 1) {
			boolean buyKey = this.rand.nextBoolean();
			ItemStack item = new ItemStack(TF2weapons.itemTF2, 1, 7);
			ItemStack metal = new ItemStack(TF2weapons.itemTF2, 4, 5);
			ItemStack metal2 = new ItemStack(TF2weapons.itemTF2, this.rand.nextInt(3), 4);
			if (metal2.getCount() == 0)
				metal2 = ItemStack.EMPTY;

			this.tradeOffers.add(new MerchantRecipe(buyKey ? item : metal, buyKey ? ItemStack.EMPTY : metal2,
					buyKey ? metal : item, 0, 1));

			ArrayList<TF2Attribute> list = new ArrayList<>(Arrays.asList(TF2Attribute.attributes));
			list.removeIf(attr -> attr == null || attr.perKill == 0);
			int killstreakCount = this.rand.nextInt(3 * this.tradeLevel);
			for (int i = 0; i < killstreakCount; i++) {
				int level = 0;
				int cost = 18;
				float rand = this.rand.nextFloat();
				if (this.tradeLevel == 2 && rand < 0.05f) {
					level = 2;
					cost = 72;
				} else if (rand < 0.2f * this.tradeLevel)
					level = 1;
				cost = 39;
				item = new ItemStack(TF2weapons.itemKillstreak, 1,
						list.get(this.rand.nextInt(list.size())).id + (level << 9));
				if (i == 0 && (this.tradeLevel > 1)) {

				}

				this.addTradeOffer(item, cost, this.rand.nextBoolean());
			}
		}
	}

	private void addTradeOffer(ItemStack toBuy, int cost, boolean buy) {
		boolean refined = cost >= 9;
		ItemStack ingot = new ItemStack(TF2weapons.itemTF2, cost / (refined ? 9 : 3), refined ? 5 : 4);
		ItemStack nugget = new ItemStack(TF2weapons.itemTF2, cost % (refined ? 9 : 3), 3);
		if (buy)
			this.tradeOffers.add(new MerchantRecipe(ingot.getCount() > 0 ? ingot : nugget,
					nugget.getCount() > 0 ? nugget : ItemStack.EMPTY, toBuy, 0, 100));
		else
			this.tradeOffers
					.add(new MerchantRecipe(toBuy, ItemStack.EMPTY, ingot.getCount() > 0 ? ingot : nugget, 0, 100));
	}

	@Override
	public void setRecipes(MerchantRecipeList recipeList) {
		this.tradeOffers = recipeList;

	}

	@Override
	public float getEyeHeight() {
		return 1.62F * (this.getRobotSize() > 1 ? 1.68f : this.getRobotSize() > 2 ? 1.9f : 1f);
	}

	public int[] getValidSlots() {
		return new int[] { 0, 1, 2 };
	}

	public float[] getDropChance() {
		return new float[] { 0.08f, 0.15f, 0.15f };
	}

	@Override
	public void useRecipe(MerchantRecipe recipe) {
		recipe.incrementToolUses();
		if (this.getOwnerId() != null) {
			if (recipe.getItemToBuy().getItem() instanceof ItemWeapon) {
				this.loadout.setStackInSlot(
						ItemFromData.getSlotForClass(ItemFromData.getData(recipe.getItemToBuy()), this),
						recipe.getItemToBuy());
				this.switchSlot(0);
			}

			else if (recipe.getItemToBuy().getItem() instanceof ItemWearable) {
				this.setItemStackToSlot(EntityEquipmentSlot.HEAD, recipe.getItemToBuy());
				this.inventoryArmorDropChances[0] = 0f;
			}
		}
		this.livingSoundTime = -this.getTalkInterval();
		this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
		int i = 3 + this.rand.nextInt(4);

		this.lastTrader = this.trader;
		this.tradeCount.put(this.trader,
				this.tradeCount.containsKey(trader) ? this.tradeCount.get(this.trader) + 1 : 1);
		this.traderFollowTicks = Math.min(4800, this.tradeCount.get(this.trader) * 250 + 350);
		if (recipe.getRewardsExp())
			this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, i));

	}

	public void switchSlot(int slot) {
		this.switchSlot(slot, false, false);
	}

	public void switchSlot(int slot, boolean noAmmoSwitch, boolean forceRefresh) {
		ItemStack stack = this.loadout.getStackInSlot(slot);

		if (this.getMainWeapon() != -1 && this.getMainWeapon() != slot && !forceRefresh && !noAmmoSwitch)
			return;
		if (stack.isEmpty())
			return;
		if (stack.getItem() instanceof ItemFromData && !((ItemFromData) stack.getItem()).canSwitchTo(stack)) {
			return;
		}
		if (!noAmmoSwitch)
			this.preferredSlot = slot;

		boolean itemChange = !(stack.getItem() instanceof ItemUsable);

		if ((slot != this.usedSlot || forceRefresh) && stack.getItem() instanceof ItemUsable) {
			if (!((ItemUsable) stack.getItem()).isAmmoSufficient(stack, this, true))
				if (!this.refill(this.usedSlot)) {
					this.switchSlot(this.getFirstSlotWithAmmo(), true, forceRefresh);
					return;
				}
			itemChange = true;
		}

		if (itemChange) {
			if (this.getHeldItemMainhand().getItem() instanceof ItemUsable && slot != this.usedSlot)
				((ItemUsable) this.getHeldItemMainhand().getItem()).holster(getWepCapability(), stack, this, world);

			this.setHeldItem(EnumHand.MAIN_HAND, stack);

			if (stack.getItem() instanceof ItemUsable)
				this.onEquipItem(slot, stack);
			if (slot != this.usedSlot)
				this.getCapability(TF2weapons.WEAPONS_CAP, null).setPrimaryCooldown(400);
			this.usedSlot = slot;
		}
	}

	public void switchSlotActual(int slot) {
		this.switchSlot(slot, false, false);
	}

	public void onEquipItem(int slot, ItemStack stack) {

		WeaponData data = ItemFromData.getData(stack);
		this.attack.explosive = TF2Attribute.EXPLOSIVE.apply(stack);
		this.attack.projSpeed = TF2Attribute.getModifier("Proj Speed", stack,
				data.getFloat(PropertyType.PROJECTILE_SPEED), this);
		this.attack.fireAtFeet = 0;
		if (stack.getItem() instanceof ItemHuntsman) {
			this.attack.fireAtFeet = -1;
			this.getWepCapability().chargeTicks = ((ItemWeapon) stack.getItem()).holdingMode(stack, this);
		}

		if (this.isGiant()) {
			this.attack.setRange(data.getFloat(PropertyType.EFFICIENT_RANGE) * 1.75f);
		} else {
			this.attack.setRange(data.getFloat(PropertyType.EFFICIENT_RANGE));
		}

		this.moveAttack.setRange(data.getFloat(PropertyType.EFFICIENT_RANGE), data.get(PropertyType.MINIMAL_RANGE, 3f));
		((ItemUsable) stack.getItem()).draw(getWepCapability(), stack, this, world);
		if (stack.getItem() instanceof ItemProjectileWeapon) {
			String projName = data.getString(PropertyType.PROJECTILE);
			try {
				EntityProjectileBase proj = MapList.projectileClasses.get(projName).getConstructor(World.class)
						.newInstance(world);
				proj.initProjectile(this, EnumHand.MAIN_HAND, stack);
				this.attack.gravity = (float) proj.getGravityOverride();
			} catch (Exception e) {

			}
		}
		this.getWepCapability().chargeTicks = 0;
		/*
		 * if(projName.equals("grenade") || projName.equals("sticky")){
		 * this.attack.gravity=0.0381f; } else if(projName.equals("flare")){
		 * this.attack.gravity=0.019f; }
		 */

	}

	@Override
	public void verifySellingItem(ItemStack stack) {}

	@Override
	public int getMaxSpawnedInChunk() {
		return 4;
	}

	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
		for (int i = 0; i < 4; i++) {
			if (!this.loadoutHeld.getStackInSlot(i).isEmpty() || !this.hasHeldInventory()) {
				if (!this.loadout.getStackInSlot(i).isEmpty())
					this.entityDropItem(this.loadout.getStackInSlot(i), 0).setThrower(ownerName);
				this.loadout.setStackInSlot(i, this.loadoutHeld.getStackInSlot(i));
			}
		}
		for (int i = 0; i < 4; i++)
			if (!this.loadoutHeld.getStackInSlot(i + 4).isEmpty() || !this.hasHeldInventory()) {
				if (!this.getItemStackFromSlot(EntityEquipmentSlot.values()[5 - i]).isEmpty())
					this.entityDropItem(this.getItemStackFromSlot(EntityEquipmentSlot.values()[5 - i]), 0)
							.setThrower(ownerName);
				this.setItemStackToSlot(EntityEquipmentSlot.values()[5 - i], this.loadoutHeld.getStackInSlot(i + 4));
				this.setDropChance(EntityEquipmentSlot.values()[5 - i], i == 0 ? 0.25f : 0f);
			}
		if (!this.isRobot() && this.getOwnerId() != null) {
			ItemStack stack = new ItemStack(TF2weapons.itemTF2, this.isSharing() ? 2 : 1, 2);
			if (this.getOwner() != null && this.getDistanceSq(this.getOwner()) < 100)
				this.entityDropItem(stack, 0);
			else {
				Map<String, MerchantRecipeList> map = this.getWorld().getCapability(TF2weapons.WORLD_CAP,
						null).lostItems;
				if (!map.containsKey(this.ownerName))
					map.put(ownerName, new MerchantRecipeList());
				map.get(ownerName).add(new MerchantRecipe(ItemStack.EMPTY, ItemStack.EMPTY, stack, 0, 1));
			}
		}
		EntityLivingBase attacker = this.getAttackingEntity();
		if (!this.refill.getStackInSlot(0).isEmpty()
				&& (attacker == this.getOwner() || !TF2Util.isOnSameTeam(attacker, this))) {
			this.entityDropItem(this.refill.getStackInSlot(0), 0);
		}

		boolean isPlayerAttacker = (attacker instanceof EntityPlayer
				|| (attacker instanceof IEntityOwnable && ((IEntityOwnable) attacker).getOwnerId() != null))
				&& (attacker.getTeam() != null || TF2ConfigVars.neutralAttack) && TF2Util.isEnemy(attacker, this);
		if (!this.isRobot() && isPlayerAttacker)
			for (int i = 0; i < loadout.getSlots(); i++) {
				ItemStack stack = loadout.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof ItemFromData
						&& this.rand.nextFloat() <= this.getDropChance()[i] * (1 + lootingModifier * 0.4f)) {
					stack.setCount(1);
					if (stack.getItem() instanceof ItemFromData)
						TF2Attribute
								.upgradeItemStack(stack,
										(int) (Math.min(320, world.getWorldTime() / 4000)
												+ rand.nextInt((int) Math.min(720, world.getWorldTime() / 2000))),
										rand);
					this.entityDropItem(stack, 0);
				}
			}
		else if (this.inventoryArmorDropChances[0] < 1) {
			this.inventoryArmorDropChances[0] = 0;
		}
		if (this.getMoneyDrop() > 0 && (isPlayerAttacker || this.getOwnerId() != null)) {
			for (int i = 0; i < this.getMoneyDrop() / 64; i++) {
				this.entityDropItem(new ItemStack(TF2weapons.itemMoney, 64, 0), 0);
			}

			this.entityDropItem(new ItemStack(TF2weapons.itemMoney, this.getMoneyDrop() % 64, 0), 0);
			this.setMoneyDrop(0);
		}
		if (this.isRobot() && this.getOwnerId() != null) {
			this.entityDropItem(new ItemStack(TF2weapons.itemTF2, 1, this.isGiant() ? 13 : 12), 0f);
		}
		super.dropEquipment(wasRecentlyHit, lootingModifier);
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public BlockPos getPos() {
		return this.getPosition();
	}

	public WeaponsCapability getWepCapability() {
		return this.getCapability(TF2weapons.WEAPONS_CAP, null);
	}

	@Override
	protected int getExperiencePoints(EntityPlayer player) {
		if (TF2Util.isOnSameTeam(player, this))
			return 0;
		else if (this.isRobot() && !this.isGiant())
			return super.getExperiencePoints(player) / 2;
		else
			return super.getExperiencePoints(player);

	}
	/*
	 * @Override public void writeSpawnData(ByteBuf buffer) { PacketBuffer
	 * packet=new PacketBuffer(buffer); for(int i=0;i<this.loadout.length;i++){
	 * packet.writeByte(i); packet.writeItemStackToBuffer(this.loadout[i]); } }
	 *
	 * @Override public void readSpawnData(ByteBuf additionalData) { PacketBuffer
	 * packet=new PacketBuffer(additionalData); while(packet.readableBytes()>0){ try
	 * { this.loadout[packet.readByte()]=packet.readItemStackFromBuffer(); } catch
	 * (IOException e) { e.printStackTrace(); } } }
	 */

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (ROBOT.equals(key)) {
			if (this.robotSizeEnsured != this.getRobotSize()) {
				this.robotSizeEnsured = this.getRobotSize();
				float scale = 1f;
				if (this.getRobotSize() == 2)
					scale = 1.65f;
				else if (this.getRobotSize() > 2)
					scale = 1.88f;
				this.setSize(Math.min(0.98f, this.getWidth() * scale), this.getHeight() * scale);
			}
		}
	}

	@Override
	public UUID getOwnerId() {
		return this.getDataManager().get(OWNER_UUID).orNull();
	}

	@Override
	public Entity getOwner() {
		if (this.getOwnerId() != null)
			return this.world.getPlayerEntityByUUID(this.getDataManager().get(OWNER_UUID).orNull());
		else
			return null;
	}

	public void setOwner(EntityLivingBase owner) {
		if (owner == null) {
			this.ownerName = null;
			this.dataManager.set(OWNER_UUID, Optional.absent());
		} else if (owner instanceof EntityPlayer) {
			this.ownerName = owner.getName();
			this.dataManager.set(OWNER_UUID, Optional.of(owner.getUniqueID()));
			this.enablePersistence();
		}

	}

	public void setOwnerID(String name, UUID owner) {
		this.ownerName = name;
		this.dataManager.set(OWNER_UUID, Optional.of(owner));
		this.enablePersistence();
	}

	public static enum Order {
		FOLLOW, HOLD;
	}

	public int getMaxAmmo(int slot) {

		int ammo = ItemFromData.getData(this.loadout.getStackInSlot(slot == -1 ? this.usedSlot : slot))
				.getInt(PropertyType.MAX_AMMO);
		return (int) (this.getOwnerId() != null ? ammo : this.scaleWithDifficulty(0.5f, 1f) * ammo);
	}

	public int getAmmo(int slot) {
		if (slot == 1000 || slot == -1 || slot >= ammoCount.length)
			slot = 0;
		if (this.unlimitedAmmo)
			return this.getMaxAmmo(slot);
		return ammoCount[slot];
	}

	public boolean isAmmoFull() {
		if (this.unlimitedAmmo)
			return true;
		else {
			for (int i = 0; i < ammoCount.length; i++) {
				ItemStack stack = loadout.getStackInSlot(i);
				if (stack.getItem() instanceof ItemUsable && (this.ammoCount[i] < this.getMaxAmmo(i)))
					return false;
			}
		}
		return true;
	}

	public int getFirstSlotWithAmmo() {
		if (this.unlimitedAmmo)
			return this.getMainWeapon() == -1 ? this.getDefaultSlot() : this.getMainWeapon();
		for (int i = 0; i < loadout.getSlots(); i++) {
			ItemStack stack = loadout.getStackInSlot(i);
			if (stack.getItem() instanceof ItemUsable) {
				if (((ItemUsable) stack.getItem()).isAmmoSufficient(stack, this, true))
					return i;
				else if (refill(i))
					return i;
			}
		}
		return 2;
	}

	public boolean refill(int slot) {
		if (!this.refill.getStackInSlot(0).isEmpty() && TF2Util.isOre("ingotLead", this.refill.getStackInSlot(0))) {
			this.refill.extractItem(0, 1, false);
			this.ammoCount[slot] = (int) (this.getMaxAmmo(slot) * 0.4f);
			return true;
		}
		if (this.isRobot() && this.refill.getStackInSlot(0).getItem() instanceof ItemMoney) {
			ItemStack stack = this.refill.extractItem(0, 1, false);
			this.ammoCount[slot] = this.getMaxAmmo(slot) * ((ItemMoney) stack.getItem()).getValue(stack);
			return true;
		}
		return false;
	}

	public boolean restoreAmmo(float p) {
		for (int i = 0; i < ammoCount.length; i++) {
			if (loadout.getStackInSlot(i).getItem() instanceof ItemUsable) {
				int maxAmmo = getMaxAmmo(i);
				ammoCount[i] = Math.min(ammoCount[i] + (int) (maxAmmo * p), maxAmmo);
			}
		}
		return true;
	}

	public int getState(boolean onTarget) {
		return onTarget ? 1 : 0;
	}

	public abstract TF2Class getTF2Class();

	/*
	 * 0% - easy 50% - medium 75% - hard 87.5% - hard + hat 93.75% - hard + unusual
	 */
	public float scaleWithDifficulty(float min, float max) {
		int diff = this.getDiff();
		float div = (1 << (diff - 1));
		if (this.isRobot())
			div = Math.max(div * 0.67f, 1f);
		if (diff < 3 && !this.shouldScaleAttributes())
			diff = 3;
		return TF2Util.lerp(min, max, 1 - (1f / div));
	}

	@Override
	public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
		return forSpawnCount && this.isRobot() ? false : super.isCreatureType(type, forSpawnCount);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
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
		return true;
	}

	@Override
	public boolean isBuilding() {
		return false;
	}

	@Override
	public boolean isBackStabbable(EntityLivingBase attacker, ItemStack knife) {
		return true;
	}

	public boolean isTargetEnemy() {
		return !this.friendly;
	}

	public int getMoneyDrop() {
		return moneyDrop;
	}

	public void setMoneyDrop(int moneyDrop) {
		this.moneyDrop = moneyDrop;
	}

	@Override
	public float getBackstabDamageReduction(EntityLivingBase attacker, ItemStack knife, float mult) {
		if (this.isGiant()) {
			mult *= 0.6f;
			if (this.getEntityData().hasKey(NBTLiterals.BACKSTAB_MULT))
				mult /= this.getEntityData().getFloat(NBTLiterals.BACKSTAB_MULT);
		}
		return mult;
	}

}
