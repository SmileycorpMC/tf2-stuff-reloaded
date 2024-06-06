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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityUpgrades;

import java.util.Random;

public class BlockUpgradeStation extends BlockContainer {

	public static final PropertyBool HOLDER = PropertyBool.create("holder");
	public static final PropertyBool PLACED = PropertyBool.create("placed");
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockUpgradeStation() {
		super(Material.IRON);
		setSoundType(SoundType.METAL);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
				.withProperty(HOLDER, true).withProperty(PLACED, false));
		setCreativeTab(TF2weapons.tabutilitytf2);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return  ((meta & 8) == 8) ? new TileEntityUpgrades(world) : null;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote)
			if (state.getValue(HOLDER)) FMLNetworkHandler.openGui(player, TF2weapons.instance, 2, world, pos.getX(), pos.getY(), pos.getZ());
			else for (int x = -1; x < 2; x++)
				for (int y = -1; y < 2; y++)
					for (int z = -1; z < 2; z++)
						if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
								&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
							FMLNetworkHandler.openGui(player, TF2weapons.instance, 2, world, pos.getX() + x,
									pos.getY() + y, pos.getZ() + z);
							return true;
						}
		return true;
	}

	@Override
	@Deprecated
	public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
		return blockState.getValue(PLACED) ? blockHardness : -1;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (state.getValue(PLACED) && tileentity instanceof TileEntityUpgrades) {
			ItemStack itemstack = new ItemStack(Item.getItemFromBlock(this));
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagCompound tetag = new NBTTagCompound();
			((TileEntityUpgrades) tileentity).writeToNBT(tetag);
			nbt.removeTag("id");
			nbt.setTag("BlockEntityTag", tetag);
			itemstack.setTagCompound(nbt);
			spawnAsEntity(world, pos, itemstack);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		state = state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(PLACED, true);
		world.setBlockState(pos, state, 2);
		EnumFacing dirop = placer.getHorizontalFacing().rotateY();
		IBlockState statehelp = state.withProperty(HOLDER, false);
		if (world.isAirBlock(pos.offset(dirop)))
			world.setBlockState(pos.offset(dirop), statehelp);
		if (world.isAirBlock(pos.offset(dirop, -1)))
			world.setBlockState(pos.offset(dirop, -1), statehelp);
		if (world.isAirBlock(pos.offset(dirop, -1).up()))
			world.setBlockState(pos.offset(dirop, -1).up(), statehelp);
		if (world.isAirBlock(pos.up()))
			world.setBlockState(pos.up(), statehelp);
		if (world.isAirBlock(pos.offset(dirop).up()))
			world.setBlockState(pos.offset(dirop).up(), statehelp);
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
		super.onBlockDestroyedByPlayer(world, pos, state);
		if (state.getValue(HOLDER))
			breakBlockAround(world, pos, state);
		for (int x = -1; x < 2; x++)
			for (int y = -1; y < 2; y++)
				for (int z = -1; z < 2; z++)
					if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
							&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
						breakBlockAround(world, pos.add(x, y, z), state);
						world.destroyBlock(pos.add(x, y, z), true);
						return;
					}
	}

	public void breakBlockAround(World world, BlockPos pos, IBlockState state) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
							&& !world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
						world.setBlockToAir(pos.add(x, y, z));
					}
				}
			}
		}

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
		return this.getDefaultState().withProperty(FACING, EnumFacing.getFront((meta & 3) + 2))
				.withProperty(HOLDER, (meta & 8) == 8).withProperty(PLACED, (meta & 4) == 4);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() - 2 + (state.getValue(HOLDER) ? 8 : 0)
				+ (state.getValue(PLACED) ? 4 : 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING, HOLDER, PLACED });
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 8));
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
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}
}
