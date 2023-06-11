package rafradek.TF2weapons.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.EntityStatue;

import java.util.List;

@SuppressWarnings("deprecation")
public class ItemStatue extends Item {

	public ItemStatue() {
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".statue");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		if (!worldIn.isRemote && stack.hasTagCompound() && stack.getTagCompound().hasKey("Statue")) {
			EntityStatue statue = new EntityStatue(worldIn);
			statue.readEntityFromNBT(stack.getTagCompound().getCompoundTag("Statue"));
			BlockPos off = pos.offset(facing);
			statue.setPosition(off.getX() + 0.5, off.getY(), off.getZ() + 0.5);
			statue.rotationYaw = playerIn.rotationYawHead;
			statue.renderYawOffset = playerIn.rotationYawHead;

			statue.ticksLeft = -1;
			worldIn.spawnEntity(statue);
			if (!playerIn.capabilities.isCreativeMode)
				stack.shrink(1);
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.SUCCESS;
	}

	public static ItemStack getStatue(EntityStatue statue) {
		ItemStack stack = new ItemStack(TF2weapons.itemStatue);
		stack.setTagCompound(new NBTTagCompound());
		NBTTagCompound tag = new NBTTagCompound();
		statue.writeEntityToNBT(tag);
		stack.getTagCompound().setTag("Statue", tag);
		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (!stack.hasTagCompound())
			return super.getItemStackDisplayName(stack);
		if (!stack.getTagCompound().getCompoundTag("Statue").getBoolean("Player"))
			return I18n.translateToLocal("entity."
					+ EntityList.getTranslationName(new ResourceLocation(
							stack.getTagCompound().getCompoundTag("Statue").getCompoundTag("Entity").getString("id")))
					+ ".name") + " " + I18n.translateToLocal(this.getUnlocalizedName() + ".name");
		else
			return I18n.translateToLocal(this.getUnlocalizedName() + ".player.name");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		if (stack.hasTagCompound()) {
			if (stack.getTagCompound().getCompoundTag("Statue").getBoolean("Player")) {
				tooltip.add(
						stack.getTagCompound().getCompoundTag("Statue").getCompoundTag("Profile").getString("Name"));
			}
		}
	}

}
