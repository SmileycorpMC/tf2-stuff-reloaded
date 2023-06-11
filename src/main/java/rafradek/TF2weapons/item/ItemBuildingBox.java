package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;

import java.util.List;

@SuppressWarnings("deprecation")
public class ItemBuildingBox extends ItemMonsterPlacerPlus implements IItemNoSwitch {
	public ItemBuildingBox() {
		this.setCreativeTab(TF2weapons.tabspawnertf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".buildingbox");
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("MaxStack")
				? stack.getTagCompound().getInteger("MaxStack")
				: 1;
	}

	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 18; i < 24; i++)
			par3List.add(new ItemStack(this, 1, i));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (ItemToken.allowUse(playerIn, "engineer")) {
			return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		} else {
			return EnumActionResult.FAIL;
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (ItemToken.allowUse(playerIn, "engineer")) {
			return super.onItemRightClick(worldIn, playerIn, hand);
		} else {
			return new ActionResult<>(EnumActionResult.FAIL, playerIn.getHeldItem(hand));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		// (I18n.translateToLocal(this.getUnlocalizedName()+".name")).trim();
		int i = stack.getItemDamage() / 2;
		String s1 = "sentry";
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Mini"))
			s1 += "mini";
		switch (i) {
		case 10:
			s1 = "dispenser";
			break;
		case 11:
			s1 = "teleporter";
			break;
		}
		return I18n.translateToLocal(this.getUnlocalizedName() + "." + s1 + ".name");
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(ItemStack p_82790_1_, int p_82790_2_) {
		return 16777215;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative()) {
			tooltip.add("Hold " + KeyBinding.getDisplayString("key.sneak").get() + " to spawn natural building");
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		this.forceItemSlot(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public boolean stopSlotSwitch(ItemStack stack, EntityLivingBase living) {
		return stack.hasTagCompound() && stack.getTagCompound().getCompoundTag("SavedEntity") != null;
	}
}
