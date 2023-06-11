package rafradek.TF2weapons.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.tileentity.TileEntityResupplyCabinet;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;

public class BlockResupplyCabinet extends BlockContainer {

	public static final PropertyBool HOLDER = PropertyBool.create("holder");
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockResupplyCabinet() {
		super(Material.IRON);
		this.setDefaultState(
				this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HOLDER, true));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if ((meta & 4) == 4) {
			TileEntityResupplyCabinet upgrades = new TileEntityResupplyCabinet();
			// upgrades.generateUpgrades();
			return upgrades;
		}
		return null;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {}
		return false;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		this.updateState(worldIn, pos, state);
	}

	private void updateState(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity ent = worldIn.getTileEntity(pos);
		if (ent instanceof TileEntityResupplyCabinet)
			if (((TileEntityResupplyCabinet) ent).redstoneActivate)
				((TileEntityResupplyCabinet) ent).setEnabled(worldIn.isBlockPowered(pos));
	}

	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {

	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		this.updateState(worldIn, fromPos, state);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		worldIn.setBlockState(pos, state = state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

		if (placer instanceof EntityPlayer) {

		}
		if (worldIn.isAirBlock(pos.up()))
			worldIn.setBlockState(pos.up(), state.withProperty(HOLDER, false), 2);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		if (state.getValue(HOLDER)) {
			if (worldIn.getBlockState(pos.up()).getBlock() == this) {
				worldIn.setBlockToAir(pos.up());
			}
		} else {
			if (worldIn.getBlockState(pos.down()).getBlock() == this) {
				worldIn.setBlockToAir(pos.down());
			}
			if (worldIn.getBlockState(pos.up()).getBlock() == this
					&& !worldIn.getBlockState(pos.up()).getValue(HOLDER)) {
				worldIn.setBlockToAir(pos.up());
			}
		}
		TileEntity ent = worldIn.getTileEntity(pos);
		if (ent instanceof TileEntityRobotDeploy) {
			((TileEntityRobotDeploy) ent).dropInventory();
		}
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos)
				&& worldIn.getBlockState(pos.up()).getBlock().isReplaceable(worldIn, pos.up());
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getFront((meta & 3) + 2)).withProperty(HOLDER,
				(meta & 4) == 4);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() - 2 + (state.getValue(HOLDER) ? 4 : 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING, HOLDER });
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 4));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		if (!(worldIn.getTileEntity(pos) instanceof TileEntityResupplyCabinet))
			return 0;
		return ((TileEntityResupplyCabinet) worldIn.getTileEntity(pos)).cooldownUse.size() > 0 ? 15 : 0;
	}
}
