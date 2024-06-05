package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;

import javax.annotation.Nullable;
import java.util.List;

public class ItemJar extends ItemProjectileWeapon {

	public ItemJar() {
		super();
		this.setMaxStackSize(64);
		this.addPropertyOverride(new ResourceLocation("empty"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
				if (stack.getTagCompound().getBoolean("IsEmpty"))
					return 1;
				return 0;
			}
		});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public CreativeTabs getCreativeTab() {
		return TF2weapons.tabutilitytf2;
	}

	@Override
	public boolean canFire(World world, EntityLivingBase living, ItemStack stack) {
		return !stack.getTagCompound().getBoolean("IsEmpty") && super.canFire(world, living, stack)
				&& !(living instanceof EntityPlayer && ((EntityPlayer) living).getCooldownTracker().hasCooldown(this));
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String string = super.getItemStackDisplayName(stack);
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("IsEmpty"))
			string = "Empty Jar - ".concat(string);
		return string;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("IsEmpty"))
			tooltip.add("Right click to fill the container");
		super.addInformation(stack, world, tooltip, advanced);
	}

	@Override
	public boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand,
			PredictionMessage message) {
		if (super.use(stack, living, world, hand, message) && !world.isRemote) {
			if (living instanceof EntityPlayer && !((EntityPlayer) living).capabilities.isCreativeMode
					&& !TF2ConfigVars.freeUseItems)
				stack.shrink(1);
			if (living instanceof EntityPlayer)
				((EntityPlayer) living).getCooldownTracker().setCooldown(this, (int) (this.getFiringSpeed(stack, living)
						/ 50
						* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean showDurabilityBar(ItemStack stack) {
		Integer value = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(stack).getName());
		return stack.getTagCompound().getBoolean("IsEmpty") && value != null && value > 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getDurabilityForDisplay(ItemStack stack) {
		Integer value = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(stack).getName());
		return (double) (value != null ? value : 0) / (double) 1600;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public void onUpdate(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(stack, par2World, par3Entity, par4, par5);
		if (!par2World.isRemote && par3Entity instanceof EntityPlayer && stack.getTagCompound().getBoolean("IsEmpty")) {
			Integer value = WeaponsCapability.get(par3Entity).effectsCool.get(getData(stack).getName());
			if (value == null || value <= 0) {
				ItemStack newStack = stack.copy();
				newStack.setCount(1);
				newStack.getTagCompound().removeTag("IsEmpty");
				String name = getData(stack).getName();
				if (((EntityPlayer) par3Entity).inventory.addItemStackToInventory(newStack) || stack.getCount() == 1) {
					if (stack.getCount() == 1)
						((EntityPlayer) par3Entity).inventory.setInventorySlotContents(par4, newStack);
					stack.shrink(1);
					WeaponsCapability.get(par3Entity).addEffectCooldown(name, 1600);
				}
			}
		}
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, EntityLivingBase attacker, EnumHand hand) {
		return false;
	}
}
