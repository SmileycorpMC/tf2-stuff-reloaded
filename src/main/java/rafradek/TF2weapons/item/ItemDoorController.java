package rafradek.TF2weapons.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor;

public class ItemDoorController extends Item {

	public static final String[] NAMES = { "players", "mobs", "RED", "BLU" };

	public ItemDoorController() {
		this.setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + NAMES[stack.getItemDamage() % NAMES.length];
	}

	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < NAMES.length; i++)
			par3List.add(new ItemStack(this, 1, i));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
			float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			TileEntity ent = world.getTileEntity(pos);
			if (ent instanceof TileEntityOverheadDoor) {
				((TileEntityOverheadDoor) ent)
						.setController(NAMES[player.getHeldItem(hand).getItemDamage() % NAMES.length]);
				if (!player.capabilities.isCreativeMode)
					player.getHeldItem(hand).shrink(1);
			} else
				return EnumActionResult.PASS;
		}
		return EnumActionResult.SUCCESS;
	}
}
