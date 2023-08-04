package rafradek.TF2weapons.entity.mercenary;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.entity.ai.EntityAINearestChecked;
import rafradek.TF2weapons.entity.ai.EntityAISeek;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemKnife;
import rafradek.TF2weapons.item.ItemMonsterPlacerPlus;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;

import java.util.ArrayList;
import java.util.List;

public class EntitySaxtonHale extends EntityCreature implements INpc, IMerchant {

	public EntityPlayer trader;
	public MerchantRecipeList tradeOffers;
	public static List<MerchantRecipe> addRecipes = new ArrayList<>();
	public static List<MerchantRecipe> removeRecipes = new ArrayList<>();
	public float rage;
	public boolean hostile;
	public boolean superJump;
	public int jumpCooldown;
	public boolean endangered;
	public int lastWeekCheck;

	private int targetAirborneTicks;

	private int noPathTime = 0;
	private final BossInfoServer bossInfo = (new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE,
			BossInfo.Overlay.PROGRESS));

	public EntitySaxtonHale(World worldIn) {
		super(worldIn);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.1F, false));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityTF2Character.class, 8.0F));
		this.tasks.addTask(7, new EntityAISeek(this));
		this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, true));
		this.experienceValue = 1500;
		this.stepHeight = 1f;
		this.setSize(0.6F, 1.99F);
	}

	@Override
	public void setCustomer(EntityPlayer player) {
		this.trader = player;
	}

	@Override
	public EntityPlayer getCustomer() {
		return trader;
	}

	@Override
	public MerchantRecipeList getRecipes(EntityPlayer player) {
		if (this.tradeOffers == null || this.world.getTotalWorldTime() / 96000L != this.lastWeekCheck)
			makeOffers();
		MerchantRecipeList list = new MerchantRecipeList();
		list.addAll(this.tradeOffers);
		if (!TF2ConfigVars.disableInvasionItems) {
			for (int i = 0; i <= TF2PlayerCapability.get(player).maxInvasionBeaten; i++) {
				if (!(i == 4 && ((EntityPlayerMP) player).getStatFile().readStat(TF2weapons.robotsKilled) < 2000)
						&& i != InvasionEvent.DIFFICULTY.length)
					this.addTradeOffer(new ItemStack(TF2weapons.itemEventMaker, 1, i), 27 + i * 9, list, i + 1);
			}
		}
		return list;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootHale;
	}

	public void makeOffers() {
		this.lastWeekCheck = (int) (this.world.getTotalWorldTime() / 96000L);
		this.tradeOffers = new MerchantRecipeList();
		this.tradeOffers.addAll(addRecipes);
		this.tradeOffers.add(new MerchantRecipe(new ItemStack(TF2weapons.itemTF2, 5, 2), ItemStack.EMPTY,
				new ItemStack(TF2weapons.itemTF2, 1, 7), 0, 100));
		int weaponCount = 13 + this.rand.nextInt(2);
		List<ItemStack> weapons = ItemFromData.getRandomWeapons(this.rand, ItemFromData.VISIBLE_WEAPON, weaponCount);
		for (int i = 0; i < weaponCount; i++) {
			ItemStack item = weapons.get(i);
			int cost = ItemFromData.getData(item).getInt(PropertyType.COST);
			this.addTradeOffer(item, cost);
		}
		int hatCount = 2 + this.rand.nextInt(3);

		for (int i = 0; i < hatCount; i++) {

			ItemStack item = ItemFromData.getRandomWeaponOfClass("cosmetic", this.rand, false);
			int cost = ItemFromData.getData(item).getInt(PropertyType.COST);
			this.addTradeOffer(item, cost);
		}

		for (MerchantRecipe toRemove : removeRecipes) {
			this.tradeOffers.removeIf(recipe -> recipe.getItemToBuy().isItemEqual(toRemove.getItemToBuy()));
		}

		/*
		 * ArrayList<TF2Attribute> list = new
		 * ArrayList<>(Arrays.asList(TF2Attribute.attributes)); list.removeIf(attr ->
		 * attr == null || attr.perKill == 0); for (int i = 0; i < 3; i++) { int level =
		 * 0; float rand = this.rand.nextFloat(); if (rand < 0.02) level = 2; else if
		 * (rand < 0.2) level = 1; ItemStack item = new
		 * ItemStack(TF2weapons.itemKillstreak, 1,
		 * list.get(this.rand.nextInt(list.size())).id + level << 9); int cost = level *
		 * 24; this.addTradeOffer(item, cost); }
		 */
	}

	private void addTradeOffer(ItemStack toBuy, int cost) {
		cost *= TF2ConfigVars.costMult;
		ItemStack ingot = new ItemStack(TF2weapons.itemTF2, cost / 9, 2);
		ItemStack nugget = new ItemStack(TF2weapons.itemTF2, cost % 9, 6);
		this.tradeOffers.add(new MerchantRecipe(ingot.getCount() > 0 ? ingot : nugget,
				nugget.getCount() > 0 ? nugget : ItemStack.EMPTY, toBuy, 0, 100));
	}

	private void addTradeOffer(ItemStack toBuy, int cost, MerchantRecipeList list, int index) {
		cost *= TF2ConfigVars.costMult;
		ItemStack ingot = new ItemStack(TF2weapons.itemTF2, cost / 9, 2);
		ItemStack nugget = new ItemStack(TF2weapons.itemTF2, cost % 9, 6);
		list.add(index, new MerchantRecipe(ingot.getCount() > 0 ? ingot : nugget,
				nugget.getCount() > 0 ? nugget : ItemStack.EMPTY, toBuy, 0, 100));
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source))
			return false;
		else if (super.attackEntityFrom(source, amount)) {
			if (source == DamageSource.DROWN || source == DamageSource.LAVA) {
				this.superJump = true;
				this.jump();
			}
			Entity entity = source.getTrueSource();
			if (entity instanceof EntityPlayerMP)
				this.bossInfo.addPlayer((EntityPlayerMP) entity);
			this.rage += amount / 100f;
			if (source instanceof TF2DamageSource && ((TF2DamageSource) source).getCritical() == 2
					&& !((TF2DamageSource) source).getWeapon().isEmpty()
					&& ((TF2DamageSource) source).getWeapon().getItem() instanceof ItemKnife)
				this.playSound(TF2Sounds.MOB_SAXTON_STAB, 2.5F, 1f);
			return this.getRidingEntity() != entity && this.getRidingEntity() != entity ? true : true;

		} else
			return false;
	}

	public void setHostile() {
		this.targetTasks.addTask(1, new EntityAINearestChecked<>(this, EntityLivingBase.class, true,
				false, input -> input instanceof EntityPlayer || input instanceof EntityTF2Character, true, false));
		this.hostile = true;
	}

	@Override
	public void setRecipes(MerchantRecipeList recipeList) {
		this.tradeOffers = recipeList;
	}

	@Override
	public void useRecipe(MerchantRecipe recipe) {}

	@Override
	public void verifySellingItem(ItemStack stack) {
		if (!stack.isEmpty() && stack.hasTagCompound())
			stack.getTagCompound().setBoolean("Bought", true);
		/*
		 * if (this.trader != null && !stack.isEmpty() &&stack.getItem() instanceof
		 * ItemWeapon) this.trader.addStat(TF2Achievements.MANN_CO_MADE);
		 */

	}

	@Override
	public void travel(float m1, float m2, float m3) {
		float move = this.getAIMoveSpeed();
		super.travel(m1 / move, m2, m3 / move);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			this.jumpCooldown--;

			if (this.rand.nextInt(20) == 0) {
				this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
						.setBaseValue(0.12 * TF2Util.lerp(1.5f, 1, this.getHealth() / this.getMaxHealth()));
			}
			this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
			// System.out.println("Has path: "+this.getNavigator().noPath());

			if (this.getAttackTarget() == null)
				this.heal(0.35f);
			else if (this.getAttackTarget().isEntityAlive()) {

				if (!this.getAttackTarget().onGround) {
					this.targetAirborneTicks++;
				}

				if (!this.onGround) {
					Vec3d forward = new Vec3d(this.getAttackTarget().posX - this.posX, 0.,
							this.getAttackTarget().posZ - this.posZ).normalize().scale(this.getAIMoveSpeed() * 0.42);
					this.motionX += forward.x;
					this.motionZ += forward.z;
				}

				List<AxisAlignedBB> boxes = this.world.getCollisionBoxes(this, getEntityBoundingBox().grow(1, 0, 1));
				boolean obscuredView = false;
				for (AxisAlignedBB box : boxes)
					if (box.calculateIntercept(this.getPositionVector().addVector(0, this.getEyeHeight(), 0),
							this.getPositionVector().add(this.getVectorForRotation(0, this.rotationYawHead).addVector(0,
									this.getEyeHeight(), 0))) != null) {
						obscuredView = true;
						break;
					}

				if (this.onGround && this.jumpCooldown <= 0) {
					if (obscuredView)
						this.superJump();
					else if (this.targetAirborneTicks > 25) {
						double height = TF2Util.getHeightAboveGround(this.getAttackTarget(), world, true);
						if (height > 2. && height < 30.)
							this.superJump();
						else if (height >= 30.)
							this.heal(0.35f);
					}
				}
			}

			if (this.ticksExisted % 3 == 0) {
				if (this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()
						&& this.getNavigator().getPathToEntityLiving(this.getAttackTarget()) == null) {
					this.noPathTime += 1;
					Vec3d forward = this.getVectorForRotation(0, this.rotationYawHead);
					if (this.noPathTime > 2) {
						for (int x = (int) this.posX; x <= this.posX + forward.x; x++) {
							for (int y = (int) Math.max(this.getAttackTarget().posY, this.posY - 1); y <= this.posY
									+ 2; y++) {
								for (int z = (int) this.posZ; z <= this.posZ + forward.z; z++) {
									BlockPos pos = new BlockPos(x, y, z);
									if (this.world.getBlockState(pos).getBlockHardness(world, pos) != -1)
										this.world.destroyBlock(pos, true);
								}
							}
						}
						this.noPathTime = 1;
					}
				} else
					this.noPathTime = 0;
			}
			if (this.rage > 1) {
				List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class,
						this.getEntityBoundingBox().grow(12, 12, 12), input -> !(input instanceof EntitySaxtonHale)
								&& !(input instanceof EntityPlayer && ((EntityPlayer) input).isCreative())
								&& input.getDistanceSq(EntitySaxtonHale.this) < 144);
				if (!list.isEmpty()) {
					this.rage = 0;
					this.playSound(TF2Sounds.MOB_SAXTON_RAGE, 2.5F, 1F);
					for (EntityLivingBase living : list)
						TF2Util.stun(living, 160, false);
					this.superJump();
				}
			}
		}
	}

	@Override
	public void setAttackTarget(EntityLivingBase living) {
		super.setAttackTarget(living);
		if (!endangered) {
			this.endangered = true;
			this.playSound(TF2Sounds.MOB_SAXTON_START, 2F, 1F);
		}
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
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_HOSTILE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SAXTON_DEATH;
	}

	@Override
	public boolean isNonBoss() {
		return !hostile;
	}

	@Override
	public Team getTeam() {
		return this.hostile ? this.world.getScoreboard().getTeam("TF2Bosses") : null;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		super.fall(distance, 0);
	}

	@Override
	protected float getJumpUpwardsMotion() {
		if (superJump)
			return 2.7F;
		return 0.7F;
	}

	public void superJump() {
		if (this.jumpCooldown > 0)
			return;

		this.playSound(TF2Sounds.MOB_SAXTON_JUMP, 2F, 1F);
		this.motionY = 0;

		this.jumpCooldown = 25;

		this.superJump = true;
		super.jump();
		this.superJump = false;
	}

	@Override
	public void jump() {
		/*
		 * if(this.getAttackTarget()!=null&&this.getAttackTarget().posY-this. posY>=3){
		 * this.superJump=true; }
		 */

	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1000.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.12D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(20D);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		float f = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
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

			if (entityIn instanceof EntityLivingBase && ((EntityLivingBase) entityIn).getHealth() <= 0)
				if (entityIn instanceof EntityBuilding)
					this.playSound(TF2Sounds.MOB_SAXTON_DESTROY, 2.2F, 1f);
				else
					this.playSound(TF2Sounds.MOB_SAXTON_KILL, 2.2F, 1f);
		}

		return flag;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);
		if (par1NBTTagCompound.getBoolean("Hostile"))
			this.setHostile();
		if (par1NBTTagCompound.hasKey("Offers")) {
			this.tradeOffers = new MerchantRecipeList();
			this.tradeOffers.readRecipiesFromTags(par1NBTTagCompound.getCompoundTag("Offers"));
		}
		this.endangered = par1NBTTagCompound.getBoolean("Endangered");
		this.lastWeekCheck = par1NBTTagCompound.getInteger("LastWeek");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		if (this.tradeOffers != null)
			par1NBTTagCompound.setTag("Offers", this.tradeOffers.getRecipiesAsTags());
		par1NBTTagCompound.setBoolean("Hostile", hostile);
		par1NBTTagCompound.setBoolean("Endangered", this.endangered);
		par1NBTTagCompound.setInteger("LastWeek", (short) this.lastWeekCheck);
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (!(player.getHeldItemMainhand() != null
				&& player.getHeldItemMainhand().getItem() instanceof ItemMonsterPlacerPlus)
				&& this.getAttackTarget() == null && this.isEntityAlive() && !this.isTrading() && !this.isChild()
				&& !player.isSneaking()) {
			boolean canTrade = player.getTeam() != null || player.capabilities.isCreativeMode || !TF2ConfigVars.canJoin;
			if (this.world.isRemote && !canTrade)
				ClientProxy.displayScreenJoinTeam();
			else if (!this.world.isRemote && (canTrade) && (this.tradeOffers == null || !this.tradeOffers.isEmpty())) {
				this.setCustomer(player);
				player.displayVillagerTradeGui(this);
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
	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		if (this.hostile)
			this.bossInfo.addPlayer(player);
	}

	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	@Override
	public World getWorld() {
		return this.world;
	}

	@Override
	public BlockPos getPos() {
		return this.getPos();
	}
}
