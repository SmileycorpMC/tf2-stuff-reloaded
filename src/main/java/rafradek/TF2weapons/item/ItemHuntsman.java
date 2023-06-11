package rafradek.TF2weapons.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.UUID;

public class ItemHuntsman extends ItemProjectileWeapon {

	public static UUID slowdownUUID = UUID.fromString("12843092-A5D6-BBCD-3D4F-A3DD4ABC65A9");
	public static AttributeModifier slowdown = new AttributeModifier(slowdownUUID, "sniper slowdown", -0.51D, 2);

	public ItemHuntsman() {
		super();
		this.addPropertyOverride(new ResourceLocation("loaded"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				if (ItemHuntsman.this.getClip(stack) > 0)
					return 1;
				return 0;
			}
		});
		this.addPropertyOverride(new ResourceLocation("lighted"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("ArrowLit"))
					return 1;
				return 0;
			}
		});
		this.addPropertyOverride(new ResourceLocation("charge"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return getCharge(entityIn, stack);
			}
		});
	}

	@Override
	public int holdingMode(ItemStack stack, EntityLivingBase shooter) {
		return (int) ((shooter instanceof EntityTF2Character
				? ((EntityTF2Character) shooter).scaleWithDifficulty(60, 20)
						: 20) * TF2Attribute.getModifier("Fire Rate", stack, 1f, shooter));
	}

	@Override
	public boolean shouldKeepCharged(ItemStack stack, EntityLivingBase shooter) {
		return true;
	}

	@Override
	public float getProjectileSpeed(ItemStack stack, EntityLivingBase living) {
		return super.getProjectileSpeed(stack, living) * (0.7f + 0.3f * (this.getCharge(living, stack)));
	}

	@Override
	public float getWeaponSpreadBase(ItemStack stack, EntityLivingBase living) {
		float base = living != null && WeaponsCapability.get(living).chargeTicks >= this.holdingMode(stack, living) * 5
				? super.getWeaponSpreadBase(stack, living)
						: 0;
		return base + this.getClip(stack) * 0.06f;
	}

	@Override
	public boolean canHeadshot(EntityLivingBase living, ItemStack stack) {
		return this.getCharge(living, stack) > 0;
	}

	@Override
	public boolean shootAllAtOnce(ItemStack stack, EntityLivingBase living) {
		return true;
	}

	@Override
	public float getWeaponDamage(ItemStack stack, EntityLivingBase living, Entity target) {
		return super.getWeaponDamage(stack, living, target) * (this.getCharge(living, stack) * 1.4f + 1f);
	}

	@Override
	public float getAdditionalGravity(EntityLivingBase living, ItemStack stack, double initial) {
		return super.getAdditionalGravity(living, stack, initial) * (1 - this.getCharge(living, stack) * 0.5f);
	}

	@Override
	public float getCharge(EntityLivingBase living, ItemStack stack) {
		if (living == null)
			return 0f;
		/*
		 * if (living instanceof EntityTF2Character) return 1f;
		 */
		WeaponsCapability cap = WeaponsCapability.get(living);
		if (cap == null) return 0;
		if (cap.lastHitCharge != 0)
			return cap.lastHitCharge;
		int chargeTicks = cap.chargeTicks;
		int maxCharge = this.holdingMode(stack, living);

		return chargeTicks <= maxCharge * 5 ? MathHelper.clamp((float) chargeTicks / (float) maxCharge, 0f, 1f) : 0f;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		if (!par2World.isRemote && par3Entity.ticksExisted % 10 == 0) {
			WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
			if (par5 && (OreDictionary.itemMatches(new ItemStack(Blocks.TORCH),
					((EntityLivingBase) par3Entity).getHeldItemOffhand(), false)
					|| OreDictionary.itemMatches(new ItemStack(Blocks.TORCH),
							new ItemStack(par2World.getBlockState(par3Entity.getPosition()).getBlock()), false)
					|| OreDictionary.itemMatches(new ItemStack(Blocks.TORCH),
							new ItemStack(par2World.getBlockState(par3Entity.getPosition().up()).getBlock()), false))) {
				par1ItemStack.getTagCompound().setBoolean("ArrowLit", true);
			}
		}
	}

	@Override
	public boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand,
			PredictionMessage message) {
		boolean use = super.use(stack, living, world, hand, message);
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (cap.isCharging()) {
			TF2Util.addModifierSafe(living, SharedMonsterAttributes.MOVEMENT_SPEED, slowdown, false);
			// living.playSound(getSound(stack, PropertyType.CHARGE_SOUND), 1f, 1f);
		}

		return use;
	}

	@Override
	public void onProjectileShoot(ItemStack stack, EntityProjectileBase proj, EntityLivingBase living, World world,
			int thisCritical, EnumHand hand) {
		if (this.getClip(stack) != 0) {
			proj.damageModifier *= 0.66f;
			/*
			 * proj.rotationYaw += (this.getClip(stack) % 2 == 0 ? 1 : -1 ) *
			 * this.getClip(stack)+1/2 * 8; Vec3d rot = new Vec3d(proj.motionX,
			 * proj.motionY, proj.motionZ); rot = rot.rotateYaw((this.getClip(stack) % 2 ==
			 * 0 ? 1 : -1 ) * this.getClip(stack)+1/2 * 8); proj.motionX = rot.x;
			 * proj.motionY = rot.y; proj.motionZ = rot.z;
			 */
		}
	}

	@Override
	public void shoot(ItemStack stack, EntityLivingBase living, World world, int thisCritical, EnumHand hand) {
		if (!world.isRemote) {
			ItemStack arrow = new ItemStack(stack.getTagCompound().getCompoundTag("LastLoaded"));
			if (arrow.isEmpty() || arrow.getItem() == Items.ARROW)
				super.shoot(stack, living, world, thisCritical, hand);
			else if (arrow.getItem() instanceof ItemArrow) {
				EntityArrow entityarrow = ((ItemArrow) arrow.getItem()).createArrow(world, arrow, living);
				float motion = this.getProjectileSpeed(stack, living) * 2.6f - super.getProjectileSpeed(stack, living);
				entityarrow.shoot(living, living.rotationPitch, living.rotationYaw, 0.0F, motion,
						this.getWeaponSpread(stack, living) * 66);
				entityarrow.pickupStatus = living instanceof EntityPlayer
						&& !((ItemArrow) arrow.getItem()).isInfinite(arrow, stack, (EntityPlayer) living)
						? PickupStatus.ALLOWED
								: PickupStatus.DISALLOWED;
				entityarrow.setDamage(
						entityarrow.getDamage() - 2f + this.getWeaponDamage(stack, living, null) / motion * 0.975f);
				if (stack.getTagCompound().getBoolean("ArrowLit")) {
					entityarrow.setFire(500);
					stack.getTagCompound().setBoolean("ArrowLit", false);
				}
				entityarrow.getEntityData().setBoolean("TF2Arrow", true);
				world.spawnEntity(entityarrow);
			}

		}
	}

	@Override
	public boolean endUse(ItemStack stack, EntityLivingBase living, World world, int action, int newState) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);

		boolean charging = cap.isCharging();
		boolean ret = super.endUse(stack, living, world, action, newState);

		if (charging)
			living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);

		return ret;
	}

	@Override
	public boolean altFireTick(ItemStack stack, EntityLivingBase living, World world) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (cap.isCharging()) {
			cap.setCharging(false);
			cap.setPrimaryCooldown(750);
			living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);
			living.playSound(getSound(stack, PropertyType.WIND_DOWN_SOUND), 1f, 1f);
		}
		return false;
	}

	static {
		slowdown.setSaved(false);
	}
}
