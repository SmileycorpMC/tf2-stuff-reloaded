package rafradek.TF2weapons.item;

import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.UUID;

public class ItemBackpack extends ItemFromData {

	private UUID ARMOR_MOD = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
	private UUID MAX_HEALTH_MOD = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
	private UUID ARMOR_TOUGHNESS_MOD = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");

	public ItemBackpack() {
		this.addPropertyOverride(new ResourceLocation("bodyModel"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				if (ItemWearable.usedModel == 1)
					return 1;
				return 0;
			}
		});
		this.addPropertyOverride(new ResourceLocation("headModel"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				if (ItemWearable.usedModel == 2)
					return 1;
				return 0;
			}
		});
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return stack.getTagCompound().getShort("Cooldown") > 0 || super.showDurabilityBar(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return stack.getTagCompound().getShort("Cooldown") > 0
				? stack.getTagCompound().getShort("Cooldown") / this.getCooldown(stack)
				: super.getDurabilityForDisplay(stack);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EntityEquipmentSlot.CHEST) {
			multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MOD, "Armor modifier",
					TF2Attribute.getModifier("Armor", stack, getData(stack).getFloat(PropertyType.ARMOR), null), 0));
			multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier(MAX_HEALTH_MOD,
					"Health modifier", TF2Attribute.getModifier("Health", stack, 0, null), 0));
			multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(),
					new AttributeModifier(ARMOR_TOUGHNESS_MOD, "Armor toughness modifier",
							TF2Attribute.getModifier("Armor", stack, 0, null) * 0.5f
									+ getData(stack).getFloat(PropertyType.ARMOR_TOUGHNESS),
							0));
		}

		return multimap;
	}

	@Override
	public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity) {
		return armorType == EntityEquipmentSlot.CHEST;
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
		this.onArmorTickAny(world, player, itemStack);
	}

	@Override
	public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EntityEquipmentSlot.CHEST;
	}

	@Override
	public int getVisibilityFlags(ItemStack stack, EntityLivingBase living) {
		return stack.getTagCompound().getShort("Cooldown") == 0 ? ItemFromData.getData(stack).getInt(PropertyType.WEAR)
				: 0;
	}

	public int getCooldown(ItemStack stack) {
		return 1200;
	}

	public ItemStack getBackpackItemToUse(ItemStack stack, EntityLivingBase player) {
		return ItemStack.EMPTY;
	}

	public void onArmorTickAny(World world, EntityLivingBase player, ItemStack itemStack) {
		if (!world.isRemote) {
			if (player.ticksExisted % 20 == 0) {
				float heal = TF2Attribute.getModifier("Health Regen", itemStack, 0, player);
				if (heal > 0) {
					int lastHitTime = player.ticksExisted - player.getEntityData().getInteger("lasthit");
					if (lastHitTime >= 120)
						player.heal(heal);
					else if (lastHitTime >= 60)
						player.heal(TF2Util.lerp(heal, heal / 4f, (lastHitTime - 60) / 60f));
					else
						player.heal(heal / 4f);
				}
			}
			if (itemStack.getTagCompound().getShort("Cooldown") > 0) {
				itemStack.getTagCompound().setShort("Cooldown",
						(short) (itemStack.getTagCompound().getShort("Cooldown") - 1));
			}
		}
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment)
				|| enchantment.type == EnumEnchantmentType.ARMOR_CHEST || enchantment.type == EnumEnchantmentType.ARMOR
				|| enchantment.type == EnumEnchantmentType.WEARABLE;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer living, EnumHand hand) {
		if (!world.isRemote)
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new ActionResult<>(EnumActionResult.SUCCESS, living.getHeldItem(hand));
	}

	public static ItemStack getBackpack(EntityLivingBase living) {
		if (living.hasCapability(TF2weapons.INVENTORY_CAP, null) && living.getCapability(TF2weapons.INVENTORY_CAP, null)
				.getStackInSlot(2).getItem() instanceof ItemBackpack) {
			return living.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(2);
		}
		if (living instanceof EntityTF2Character) {
			ItemStackHandler loadout = ((EntityTF2Character) living).loadout;
			for (int i = 0; i < loadout.getSlots(); i++) {
				if (loadout.getStackInSlot(i).getItem() instanceof ItemBackpack)
					return loadout.getStackInSlot(i);
			}
		}
		if (living.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemBackpack)
			return living.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		else
			return ItemStack.EMPTY;
	}
}
