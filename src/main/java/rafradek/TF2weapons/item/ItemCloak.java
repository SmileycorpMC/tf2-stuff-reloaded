package rafradek.TF2weapons.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntitySpy;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.ReflectionAccess;
import rafradek.TF2weapons.util.TF2Class;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCloak extends ItemFromData {

	public ItemCloak() {
		super();
		this.addPropertyOverride(new ResourceLocation("active"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				if (entityIn != null && isFeignDeath(stack, entityIn) && WeaponsCapability.get(entityIn).isFeign())
					return 1;
				return 0;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		if (par1ItemStack.getTagCompound().getBoolean("Active") && WeaponsCapability.get(par3Entity).isInvisible()) {
			// System.out.println("uncharge");
			int maxdamage = getMaxDamage(par1ItemStack);
			if (!(par3Entity instanceof EntityPlayer && ((EntityPlayer) par3Entity).capabilities.isCreativeMode)
					&& !(par3Entity instanceof EntityTF2Character && ((EntityTF2Character) par3Entity).isRobot())) {
				par1ItemStack.setItemDamage(Math.min(maxdamage, par1ItemStack.getItemDamage() + 3));
				if (par5 || par1ItemStack == ((EntityLivingBase) par3Entity).getHeldItemOffhand())
					try {
						((NonNullList<ItemStack>) ReflectionAccess.entityHandInv.get(par3Entity)).get(par5 ? 0 : 1)
								.setItemDamage(par1ItemStack.getItemDamage());
					} catch (Exception e) {}
			}

			if (par1ItemStack.getTagCompound().getBoolean("Strange") && par3Entity.ticksExisted % 20 == 0) {
				// par1ItemStack.getTagCompound().setInteger("CloakTicks",
				// par1ItemStack.getTagCompound().getInteger("CloakTicks") + 1);
				// if (par1ItemStack.getTagCompound().getInteger("CloakTicks") % 20 == 0)
				TF2EventsCommon.onStrangeUpdate(par1ItemStack, (EntityLivingBase) par3Entity);
			}
			if (par1ItemStack.getItemDamage() >= maxdamage) {
				par1ItemStack.setItemDamage(maxdamage);
				this.setCloak(false, par1ItemStack, (EntityLivingBase) par3Entity, par2World);
			}

		} else if (par1ItemStack.getTagCompound().getBoolean("Active")
				&& !WeaponsCapability.get(par3Entity).isInvisible())
			par1ItemStack.getTagCompound().setBoolean("Active", false);
		else if (par3Entity.ticksExisted % 2 == 0) {
			par1ItemStack
					.setItemDamage(Math.max(
							par1ItemStack.getItemDamage() - (int) TF2Attribute.getModifier("Effect Duration",
									par1ItemStack, TF2Attribute.getModifier("Charge", par1ItemStack, 2, null), null),
							0));
			if (par5 || par1ItemStack == ((EntityLivingBase) par3Entity).getHeldItemOffhand())
				try {
					((NonNullList<ItemStack>) ReflectionAccess.entityHandInv.get(par3Entity)).get(par5 ? 0 : 1)
							.setItemDamage(par1ItemStack.getItemDamage());
				} catch (Exception e) {}
		}
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return (int) TF2Attribute.getModifier("Effect Duration", stack, 600, null);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer living, EnumHand hand) {
		ItemStack stack = living.getHeldItem(hand);
		if (ItemToken.allowUse(living, TF2Class.SPY)) {
			if (living.isInvisible()
					|| (!isFeignDeath(stack, living) && stack.getItemDamage() < this.getMaxDamage(stack) - 72)) {
				this.setCloak(!WeaponsCapability.get(living).isInvisible(), stack, living, world);
				return new ActionResult<>(EnumActionResult.SUCCESS, stack);
			} else if (!living.isInvisible() && this.isFeignDeath(stack, living) && stack.getItemDamage() == 0) {
				WeaponsCapability.get(living).setFeign(!WeaponsCapability.get(living).isFeign());
				if (WeaponsCapability.get(living).isFeign())
					living.playSound(getSound(stack, PropertyType.CHARGE_SOUND), 1.0f, 1.0f);
				return new ActionResult<>(EnumActionResult.SUCCESS, stack);
			}
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}

	public boolean isFeignDeath(ItemStack stack, EntityLivingBase living) {
		return TF2Attribute.getModifier("Weapon Mode", stack, 0, living) == 1;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		if (item.getTagCompound().getBoolean("Active"))
			this.setCloak(false, item, player, player.world);
		return super.onDroppedByPlayer(item, player);
	}

	public static Tuple<Integer, ItemStack> searchForWatches(EntityLivingBase living) {
		if (living instanceof EntitySpy)
			return new Tuple<>(3, ((EntitySpy) living).loadout.getStackInSlot(3));
		if (living instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) living;
			if (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() instanceof ItemCloak
					&& player.getHeldItemOffhand().getTagCompound().getBoolean("Active"))
				// System.out.println("Found offhand");
				return new Tuple<>(40, player.getHeldItemOffhand());
			for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
				ItemStack stack = player.inventory.mainInventory.get(i);
				if (!stack.isEmpty() && stack.getItem() instanceof ItemCloak
						&& stack.getTagCompound().getBoolean("Active"))
					// System.out.println("Found hand");
					return new Tuple<>(i, stack);
			}

		}
		return new Tuple<>(-1, ItemStack.EMPTY);
	}

	public static ItemStack getFeignDeathWatch(EntityLivingBase living) {
		ItemStack stack = living.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemCloak && ((ItemCloak) stack.getItem()).isFeignDeath(stack, living)
				&& stack.getItemDamage() == 0) {
			return stack;
		} else {
			stack = living.getHeldItemOffhand();
			if (stack.getItem() instanceof ItemCloak && ((ItemCloak) stack.getItem()).isFeignDeath(stack, living)
					&& stack.getItemDamage() == 0) {
				return stack;
			}
			IItemHandler items = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for (int i = 0; i < items.getSlots(); i++) {
				stack = items.getStackInSlot(i);
				if (stack.getItem() instanceof ItemCloak && ((ItemCloak) stack.getItem()).isFeignDeath(stack, living)
						&& stack.getItemDamage() == 0) {
					return stack;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	public void setCloak(boolean active, ItemStack stack, EntityLivingBase living, World world) {
		// System.out.println("set active: "+active);
		if (!active || !(living instanceof EntityPlayer) || searchForWatches(living).getSecond().isEmpty()) {
			if (!active) {
				living.setInvisible(false);
				living.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks = 20;
			}
			// System.out.println("ok: "+active);
			stack.getTagCompound().setBoolean("Active", active);
			WeaponsCapability.get(living).setInvisible(active);

			// setInvisiblity(living);
			if (active) {
				living.playSound(ItemFromData.getSound(stack, PropertyType.CLOAK_SOUND), 1.5f, 1);
				stack.setItemDamage(Math.min(this.getMaxDamage(stack), stack.getItemDamage() + this.getMaxDamage(stack)
						- (int) TF2Attribute.getModifier("Cloak Drain", stack, this.getMaxDamage(stack), living)));
			} else {
				living.playSound(ItemFromData.getSound(stack, PropertyType.DECLOAK_SOUND), 1.5f, 1);
				if (this.isFeignDeath(stack, living))
					living.setSilent(false);
			}
			if (!world.isRemote) {
				// TF2weapons.sendTracking(new
				// TF2Message.PropertyMessage("IsCloaked",
				// (byte)(active?1:0),living),living);
			}
		}
	}

	public static void setInvisiblity(EntityLivingBase living) {
		boolean cloaked = living.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks >= 20;
		boolean disguised = WeaponsCapability.get(living).isDisguised();
		living.setInvisible(cloaked || (disguised && living.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks == 0
				&& !WeaponsCapability.get(living).isInvisible()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, world, tooltip, advanced);

		tooltip.add("Charge: "
				+ (100 - (int) (100 * ((float) stack.getItemDamage() / (float) this.getMaxDamage(stack)))) + "%");
	}

	@Override
	public boolean showInfoBox(ItemStack stack, EntityPlayer player) {
		return true;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, EntityPlayer player) {
		return new String[] { "CLOAK",
				(100 - (int) (100 * ((float) stack.getItemDamage() / (float) this.getMaxDamage(stack)))) + "%" };
	}
}
