package rafradek.TF2weapons.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import rafradek.TF2weapons.TF2weapons;

import javax.annotation.Nullable;

public class BlockCabinet extends Block {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockCabinet() {
		super(Material.IRON);
		this.setSoundType(SoundType.METAL);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, @Nullable EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote)
			FMLNetworkHandler.openGui(playerIn, TF2weapons.instance, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 */
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
		return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(2 + (meta & 3)));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() - 2;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}
}
