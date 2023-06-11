package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;

import java.util.Set;
import java.util.UUID;

public abstract class ItemUsable extends ItemFromData {
	// public ConfigCategory data;
	// public String render;
	public static int sps;
	// public static int tickleft;
	// public static boolean addedIcons;
	// public static ThreadLocalMap<EntityLivingBase, NBTTagCompound>
	// itemProperties=new ThreadLocalMap<EntityLivingBase, NBTTagCompound>();

	public ItemUsable() {
		super();
		this.setCreativeTab(TF2weapons.tabweapontf2);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		return new ActionResult<>((this.canAltFire(worldIn, playerIn, itemStackIn)
				&& this.getAltFiringSpeed(itemStackIn, playerIn) != Short.MAX_VALUE) || TF2ConfigVars.swapAttackButton
				|| playerIn.getCapability(TF2weapons.WEAPONS_CAP, null).getPrimaryCooldown() > 0
						? EnumActionResult.SUCCESS
						: EnumActionResult.PASS,
				itemStackIn);
	}

	public abstract boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand,
			PredictionMessage message);

	public boolean startUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
		living.getCapability(TF2weapons.WEAPONS_CAP, null).pressedStart = true;
		return false;
	}

	public boolean endUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
		if ((oldState & 1) == 1 && (newState & 1) == 0)
			WeaponsCapability.get(living).hitNoMiss = 0;
		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(stack, par2World, par3Entity, par4, par5);
		if (stack.isEmpty())
			return;
		/*
		 * if(itemProperties.get(par2World.isRemote).get(par3Entity)==null){
		 * itemProperties.get(par2World.isRemote).put((EntityLivingBase) par3Entity, new
		 * NBTTagCompound()); }
		 */
		WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
		WeaponData.WeaponDataCapability stackcap = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null);
		EntityLivingBase living = (EntityLivingBase) par3Entity;

		int mincool = 0;

		if (living instanceof EntityPlayer && !par2World.isRemote)
			mincool = WeaponsCapability.PLAYER_MINCOOL;

		if (stackcap.fire1Cool > mincool)
			stackcap.fire1Cool -= 50;
		else
			stackcap.fire1Cool = mincool;

		if (stackcap.fire2Cool > mincool)
			stackcap.fire2Cool -= 50;
		else
			stackcap.fire2Cool = mincool;
		boolean offhand = stack == living.getHeldItemOffhand();
		boolean doublewield = offhand && isDoubleWielding(living);
		if (stackcap.active == 0 && (par5 || doublewield)) {
			stackcap.active = 1;
			// itemProperties.get(par2World.isRemote).get(par3Entity).setShort("reloadd",
			// (short) 800);

			if (!par2World.isRemote && living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH)
					.getModifier(ItemWeapon.HEALTH_MODIFIER) != null) {
				float addHealth = (float) living.getAttributeMap()
						.getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH)
						.getModifier(ItemWeapon.HEALTH_MODIFIER).getAmount();
				living.setHealth((living.getMaxHealth()) / (living.getMaxHealth() - addHealth) * living.getHealth());
			}
			stackcap.fire1Cool = Math.max(stackcap.fire1Cool, this.getDeployTime(stack, living));
			stackcap.fire2Cool = Math.max(stackcap.fire2Cool, this.getDeployTime(stack, living));
		} else if (stackcap.active > 0 && ((offhand && !doublewield) || (!offhand && !par5))) {
			cap.setInactiveHand(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, stack);
			cap.lastWeapon = stack;
		}
		if (par3Entity.ticksExisted % 5 == 0 && stackcap.active == 2
				&& TF2Attribute.getModifier("Mark Death", stack, 0, living) > 0)
			living.addPotionEffect(new PotionEffect(TF2weapons.markDeath,
					(int) TF2Attribute.getModifier("Mark Death", stack, 0, living) * 20));
	}

	public void draw(WeaponsCapability weaponsCapability, ItemStack stack, EntityLivingBase living, World world) {
		if (living instanceof EntityPlayerMP)
			TF2weapons.network.sendTo(
					new TF2Message.UseMessage(-1, false, this.getAmmoAmount(living, stack), EnumHand.MAIN_HAND),
					(EntityPlayerMP) living);
	}

	public void holster(WeaponsCapability cap, ItemStack stack, EntityLivingBase living, World world) {
		cap.chargeTicks = 0;
		cap.setCharging(false);
	}

	public static double calculateModifiers(IAttributeInstance attribute, UUID except, double initial,
			double additionToMult) {
		double initialO = initial;
		for (AttributeModifier modifier : attribute.getModifiersByOperation(0)) {
			if (!modifier.getID().equals(except)) {
				initial += initialO * modifier.getAmount() * additionToMult;
			}
		}
		for (AttributeModifier modifier : attribute.getModifiersByOperation(1)) {
			if (!modifier.getID().equals(except)) {
				initial += initialO * modifier.getAmount();
			}
		}
		for (AttributeModifier modifier : attribute.getModifiersByOperation(2)) {
			if (!modifier.getID().equals(except)) {
				initial *= 1 + modifier.getAmount();
			}
		}
		return initial;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		/*
		 * item.getTagCompound().removeTag("active");
		 * player.getCapability(TF2weapons.WEAPONS_CAP, null).state = 0;
		 * if(!player.world.isRemote) TF2weapons.network.sendTo(new
		 * TF2Message.WeaponDroppedMessage(ItemFromData.getData(item).getName()),
		 * (EntityPlayerMP) player);
		 * this.holster(player.getCapability(TF2weapons.WEAPONS_CAP, null), item,
		 * player, player.world);
		 */
		return true;
	}

	public boolean canFireInternal(World world, EntityLivingBase living, ItemStack stack, EnumHand hand) {
		if (world.isRemote && living != Minecraft.getMinecraft().player)
			return WeaponsCapability.get(living).canFire(hand, true);
		return canFire(world, living, stack);
	}

	public boolean canFire(World world, EntityLivingBase living, ItemStack stack) {

		return stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active > 0
				&& ItemToken.allowUse(living, this.getUsableClasses(stack))
				&& (living.getActiveItemStack().isEmpty() || this.getDoubleWieldBonus(stack, living) != 1)
				&& this.getFiringSpeed(stack, living) != Integer.MAX_VALUE;
	}

	public Set<String> getUsableClasses(ItemStack stack) {
		if ((getData(stack).get(PropertyType.SLOT).isEmpty()) && getData(stack).hasProperty(PropertyType.BASED_ON)) {
			stack = getNewStack(getData(stack).getString(PropertyType.BASED_ON));
		}
		return getData(stack).get(PropertyType.SLOT).keySet();
	}

	public abstract boolean fireTick(ItemStack stack, EntityLivingBase living, World world);

	public abstract boolean altFireTick(ItemStack stack, EntityLivingBase living, World world);

	/*
	 * public void registerIcons(IIconRegister par1IconRegister) { this.itemIcon =
	 * par1IconRegister.registerIcon(this.getIconString()); if(addedIcons) return;
	 * Iterator<String> iterator=MapList.nameToCC.keySet().iterator();
	 * addedIcons=true; while(iterator.hasNext()){ String name=iterator.next();
	 * //System.out.println(MapList.nameToCC.get(name).get("Render").getString()
	 * +" "+name); MapList.nameToIcon.put(name,
	 * par1IconRegister.registerIcon(MapList.nameToCC.get(name).get("Render").
	 * getString())); } //this.itemIcon =
	 * par1IconRegister.registerIcon(getData(stack).get("Render").getString()); }
	 */
	/*
	 * @SideOnly(Side.CLIENT) public boolean requiresMultipleRenderPasses() { return
	 * true; } public int getRenderPasses(int metadata) { return 1; }
	 */

	public int getFiringSpeed(ItemStack stack, EntityLivingBase living) {
		int speed = (int) (TF2Attribute.getModifier("Fire Rate", stack,
				ItemFromData.getData(stack).getInt(PropertyType.FIRE_SPEED), living));
		if (living != null && isDoubleWielding(living))
			speed *= this.getDoubleWieldBonus(stack, living) * 2;
		if (TF2Attribute.getModifier("Fire Rate Health", stack, 1f, living) != 1f)
			speed *= this.getHealthBasedBonus(stack, living,
					TF2Attribute.getModifier("Fire Rate Health", stack, 1f, living));
		if (living != null && (WeaponsCapability.get(living).isExpJump() || living.isElytraFlying())
				&& TF2Attribute.getModifier("Airborne Bonus", stack, 0, living) != 0)
			speed *= 0.35f;
		if (living != null && living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED) != null) {
			// System.out.println("Pre speed "+speed+"
			// "+living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue());
			double modifiers = calculateModifiers(living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED),
					ATTACK_SPEED_MODIFIER,
					living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue(), 1.4);
			speed *= (living instanceof EntityPlayer ? 4
					: living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue()) / modifiers;
			// System.out.println("Post speed "+speed);
		}
		if (speed <= 0) {
			return Integer.MAX_VALUE;
		}
		return speed;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return (player.world.isRemote && !TF2ConfigVars.swapAttackButton)
				|| (!player.world.isRemote && !TF2PlayerCapability.get(player).breakBlocks);
	}

	public static boolean isDoubleWielding(EntityLivingBase living) {
		return ItemFromData.getData(living.getHeldItemMainhand()) != ItemFromData.BLANK_DATA
				&& living.getHeldItemMainhand().getItem() instanceof ItemUsable
				&& living.getHeldItemOffhand().getItem() instanceof ItemUsable
				&& ((ItemUsable) living.getHeldItemOffhand().getItem()).getDoubleWieldBonus(living.getHeldItemOffhand(),
						living) != 1
				&& ((ItemUsable) living.getHeldItemMainhand().getItem())
						.getDoubleWieldBonus(living.getHeldItemMainhand(), living) != 1;
	}

	public float getDoubleWieldBonus(ItemStack stack, EntityLivingBase living) {
		// System.out.println("Double wield type:
		// "+ItemFromData.getData(stack).hasProperty(PropertyType.DUAL_WIELD_SPEED)+"
		// "+ItemFromData.getData(stack).getFloat(PropertyType.DUAL_WIELD_SPEED));
		return !ItemFromData.getData(stack).hasProperty(PropertyType.DUAL_WIELD_SPEED) ? 1f
				: ItemFromData.getData(stack).getFloat(PropertyType.DUAL_WIELD_SPEED);
	}

	public boolean canAltFireInternal(World worldObj, EntityLivingBase player, ItemStack item, EnumHand hand) {
		if (worldObj.isRemote && player != Minecraft.getMinecraft().player)
			return WeaponsCapability.get(player).canFire(hand, false);
		return canAltFire(worldObj, player, item);
	}

	public boolean canAltFire(World worldObj, EntityLivingBase player, ItemStack item) {
		return item.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active > 0
				&& player.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks == 0
				&& ItemToken.allowUse(player, this.getUsableClasses(item))
				&& (player.getActiveItemStack().isEmpty() || this.getDoubleWieldBonus(item, player) != 1);
	}

	public void altUse(ItemStack stack, EntityLivingBase living, World world) {

	}

	public float getHealthBasedBonus(ItemStack item, EntityLivingBase living, float maxbonus) {
		if (living != null && living.getHealth() < living.getMaxHealth() * 0.8f) {
			float multiplier = MathHelper
					.clamp(TF2Util.position(0.1f, 0.8f, living.getHealth() / living.getMaxHealth()), 0f, 1f);

			return TF2Util.lerp(1, maxbonus, multiplier);
		}
		return 1f;
	}

	public short getAltFiringSpeed(ItemStack item, EntityLivingBase player) {
		return Short.MAX_VALUE;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (newStack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null) && !slotChanged) {
			newStack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active = oldStack
					.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active;
			newStack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire1Cool = oldStack
					.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire1Cool;
			newStack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire2Cool = oldStack
					.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire2Cool;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	public int getDeployTime(ItemStack stack, EntityLivingBase living) {
		return (int) TF2Attribute.getModifier("Deploy Time", stack, 750, living);
	}

	public int getStateOverride(ItemStack stack, EntityLivingBase living, int original) {
		if (TF2Attribute.getModifier("Auto Fire", stack, 0, living) != 0) {
			// System.out.println("Act pre: "+original);
			boolean fire = ((original & 1) == 0) || WeaponsCapability.get(living).autoFire;
			if (fire) {
				return 1;
			} else {
				return 4;
			}
			// System.out.println("Act post: "+original);
		}
		return original;
	}

	public boolean shouldEntityFire(ItemStack stack, EntityLivingBase living, EntityLivingBase target) {
		/*
		 * if(TF2Attribute.getModifier("Auto Fire", stack, 0, living) != 0) { return
		 * stack.getItemDamage() > 0 && (stack.getItemDamage() == stack.getMaxDamage()
		 * || WeaponsCapability.get(living).reloadingHand != null); }
		 */

		return true;
	}

	@Override
	public boolean canSwitchTo(ItemStack stack) {
		return true;
	}
}
