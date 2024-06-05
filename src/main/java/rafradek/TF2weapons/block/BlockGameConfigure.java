package rafradek.TF2weapons.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rafradek.TF2weapons.tileentity.TileEntityGameConfigure;
import rafradek.TF2weapons.tileentity.TileEntityResupplyCabinet;

public class BlockGameConfigure extends BlockContainer {

	public BlockGameConfigure() {
		super(Material.IRON);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileEntityGameConfigure upgrades = new TileEntityGameConfigure();
		return upgrades;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {

		}
		return false;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		this.updateState(world, pos, state);
	}

	private void updateState(World world, BlockPos pos, IBlockState state) {}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {

	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		this.updateState(world, fromPos, state);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		TileEntity ent = world.getTileEntity(pos);
		if (ent instanceof TileEntityGameConfigure) {
			((TileEntityGameConfigure) ent).removeGameArena();
		}
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos)
				&& world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up());
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		if (!(world.getTileEntity(pos) instanceof TileEntityResupplyCabinet))
			return 0;
		return ((TileEntityResupplyCabinet) world.getTileEntity(pos)).cooldownUse.size() > 0 ? 15 : 0;
	}
}
