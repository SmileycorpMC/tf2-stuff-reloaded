package rafradek.TF2weapons.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityAmmoFurnace;

import java.util.Random;

public class BlockAmmoFurnace extends BlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool BURNING = PropertyBool.create("burning");

	public BlockAmmoFurnace() {
		super(Material.IRON);
		this.setSoundType(SoundType.METAL);
		this.setDefaultState(
				this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(BURNING, false));
		setCreativeTab(TF2weapons.tabsurvivaltf2);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityAmmoFurnace();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote)
			FMLNetworkHandler.openGui(playerIn, TF2weapons.instance, 3, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("incomplete-switch")
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (stateIn.getValue(BURNING)) {
			EnumFacing enumfacing = stateIn.getValue(FACING);
			double d0 = pos.getX() + 0.5D;
			double d1 = pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
			double d2 = pos.getZ() + 0.5D;
			double d3 = 0.52D;
			double d4 = rand.nextDouble() * 0.6D - 0.3D;

			if (rand.nextDouble() < 0.1D)
				worldIn.playSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
						SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);

			switch (enumfacing) {
			case WEST:
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D,
						new int[0]);
				worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
				break;
			case EAST:
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D,
						new int[0]);
				worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D, new int[0]);
				break;
			case NORTH:
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D,
						new int[0]);
				worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D, new int[0]);
				break;
			case SOUTH:
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D,
						new int[0]);
				worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D, new int[0]);
			}
		}
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

		if (stack.hasDisplayName()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);

			if (tileentity instanceof TileEntityAmmoFurnace)
				((TileEntityAmmoFurnace) tileentity).setCustomInventoryName(stack.getDisplayName());
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if (tileentity instanceof TileEntityAmmoFurnace) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityAmmoFurnace) tileentity);
			worldIn.updateComparatorOutputLevel(pos, this);
		}

		super.breakBlock(worldIn, pos, state);
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
		EnumFacing facing = EnumFacing.getFront(meta & 7);
		if (facing.getAxis() == EnumFacing.Axis.Y)
			facing = EnumFacing.NORTH;
		return this.getDefaultState().withProperty(FACING, facing).withProperty(BURNING, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() + (state.getValue(BURNING) ? 8 : 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING, BURNING });
	}

	public static void setState(boolean active, World worldIn, BlockPos pos) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		TileEntity tileentity = worldIn.getTileEntity(pos);

		worldIn.setBlockState(pos, iblockstate.withProperty(BURNING, active));

		if (tileentity != null) {
			tileentity.validate();
			worldIn.setTileEntity(pos, tileentity);
		}
	}
}
