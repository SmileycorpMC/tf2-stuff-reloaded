package rafradek.TF2weapons.item;

import com.google.common.collect.Sets;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.TF2Class;

import java.util.Set;
import java.util.UUID;

public class ItemToken extends Item {

	public static final UUID SPEED_UUID = UUID.fromString("769e0d47-bb57-45ed-8fe4-ab84592d842b");
	public static final UUID HEALTH_UUID = UUID.fromString("ff0b26f3-cf6d-49e6-9989-3886ec3d3b83");
	public static final UUID HEALTH_MULT_UUID = UUID.fromString("ff0b26f3-cf6d-49e6-9989-3886ec3d3e83");

	public static final float[] SPEED_VALUES = { 0.7638f, 0.0583f, 0.329f, 0.2347f, 0f, 0.329f, 0.4111f, 0.329f,
			0.4111f };
	public static final float[] FOV_VALUES = { 0.7638f, 0.0583f, 0.329f, 0.2347f, 0f, 0.329f, 0.4111f, 0.329f,
			0.4111f };
	public static final float[] HEALTH_VALUES = { -7.5f, 0f, -2.5f, -2.5f, 10f, -7.5f, -5f, -7.5f, -7.5f };
	public static final double[] EXPLOSION_VALUES = { 1D, 0.667D, 0.95D, 0.75D, 0.5D, 1D, 0.95D, 1D, 1D };

	public ItemToken() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".token");
	}

	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 9; i++)
			par3List.add(new ItemStack(this, 1, i));
	}

	public void updateAttributes(ItemStack stack, EntityLivingBase living) {
		living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SPEED_UUID);
		living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(HEALTH_UUID);
		living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(HEALTH_MULT_UUID);
		if (!stack.isEmpty() && stack.getMetadata() >= 0 && stack.getMetadata() < TF2Class.getClasses().size()) {
			WeaponsCapability.get(living).setUsedToken(stack.getMetadata());
			float livinghealth = living.getHealth() / living.getMaxHealth();
			living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(
					new AttributeModifier(SPEED_UUID, "tokenspeed", SPEED_VALUES[stack.getMetadata()], 1));
			living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(
					new AttributeModifier(HEALTH_UUID, "tokenhealth", HEALTH_VALUES[stack.getMetadata()], 0));
			living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(
					new AttributeModifier(HEALTH_MULT_UUID, "tokenhealthmult", TF2ConfigVars.damageMultiplier - 1, 2));
			living.setHealth(living.getMaxHealth() * living.getHealth());
		} else {
			WeaponsCapability.get(living).setUsedToken(-1);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer living, EnumHand hand) {
		if (!world.isRemote)
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new ActionResult<>(EnumActionResult.SUCCESS, living.getHeldItem(hand));
	}

	public static boolean allowUse(EntityLivingBase living, TF2Class clazz) {
		return allowUse(living, Sets.newHashSet(clazz));
	}

	public static boolean allowUse(EntityLivingBase living, Set<TF2Class> clazz) {
		return !(living instanceof EntityPlayer) || allowUse(WeaponsCapability.get(living).getUsedToken(), clazz);
	}

	public static boolean allowUse(int livingclass, Set<TF2Class> clazz) {
		return clazz.isEmpty()
				|| !(livingclass >= 0 && livingclass < TF2Class.getClasses().size() && !clazz.contains(TF2Class.getClass(livingclass)));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + TF2Class.getClass(stack.getMetadata()).getName();
	}

}
