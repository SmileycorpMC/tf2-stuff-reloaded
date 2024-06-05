package rafradek.TF2weapons.block;

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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;

public class BlockRobotDeploy extends BlockContainer {

	public static final PropertyBool HOLDER = PropertyBool.create("holder");
	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	public static final PropertyBool JOINED = PropertyBool.create("joined");
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockRobotDeploy() {
		super(Material.IRON);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
				.withProperty(HOLDER, true).withProperty(JOINED, false).withProperty(ACTIVE, false));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if ((meta & 4) == 4) {
			TileEntityRobotDeploy upgrades = new TileEntityRobotDeploy();
			// upgrades.generateUpgrades();
			return upgrades;
		}
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			if (state.getValue(HOLDER))
				FMLNetworkHandler.openGui(player, TF2weapons.instance, 6, world, pos.getX(), pos.getY(),
						pos.getZ());
			for (int x = -1; x < 2; x++)
				for (int y = -1; y < 1; y++)
					for (int z = -1; z < 2; z++)
						if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockRobotDeploy
								&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
							FMLNetworkHandler.openGui(player, TF2weapons.instance, 6, world, pos.getX() + x,
									pos.getY() + y, pos.getZ() + z);
							return true;
						}

		}
		return true;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {

	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		world.setBlockState(pos, state = state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

		if (placer instanceof EntityPlayer) {
			TileEntity ent = world.getTileEntity(pos);
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				BlockPos offpos = pos.offset(facing);
				TileEntity entoff = world.getTileEntity(offpos);
				if (entoff instanceof TileEntityRobotDeploy && !world.getBlockState(offpos).getValue(JOINED)
						&& world.getBlockState(offpos).getValue(HOLDER)) {
					EnumFacing placefacing = facing.rotateY();
					if (placefacing == placer.getHorizontalFacing()) {
						NBTTagCompound tag = new NBTTagCompound();
						entoff.writeToNBT(tag);
						tag.setInteger("x", pos.getX());
						tag.setInteger("y", pos.getY());
						tag.setInteger("z", pos.getZ());
						((TileEntityRobotDeploy) ent).readFromNBT(tag);
						world.setBlockState(offpos, world.getBlockState(offpos).withProperty(HOLDER, false));
						world.setTileEntity(offpos, null);
						placefacing = placefacing.getOpposite();
					} else {
						state = state.withProperty(HOLDER, false);
					}
					state = state.withProperty(FACING, placefacing).withProperty(JOINED, true);
					world.setBlockState(pos, state);
					world.setBlockState(offpos,
							world.getBlockState(offpos).withProperty(FACING, placefacing).withProperty(JOINED, true));
					if (world.getBlockState(offpos.up()).getBlock() == this)
						world.setBlockState(offpos.up(), world.getBlockState(offpos.up())
								.withProperty(FACING, placefacing).withProperty(JOINED, true));
					break;
				}
			}

			/*
			 * TileEntity entbelow = world.getTileEntity(pos.down()); TileEntity entup =
			 * world.getTileEntity(pos.up()); IBlockState statebelow =
			 * world.getBlockState(pos.down()); IBlockState stateup =
			 * world.getBlockState(pos.up()); if (entbelow instanceof
			 * TileEntityRobotDeploy && !world.getBlockState(pos.down()).getValue(JOINED))
			 * { world.setBlockState(pos, state = state.withProperty(JOINED, true));
			 * world.setBlockState(pos, state = state.withProperty(HOLDER, false));
			 * world.setTileEntity(pos, null); world.setBlockState(pos.down(),
			 * statebelow = statebelow.withProperty(JOINED, true)); } if (entup instanceof
			 * TileEntityRobotDeploy && !world.getBlockState(pos.up()).getValue(JOINED)) {
			 * world.setBlockState(pos, state.withProperty(JOINED, true)); NBTTagCompound
			 * tag=new NBTTagCompound();
			 * ((TileEntityRobotDeploy)ent).readFromNBT(entup.writeToNBT(tag));
			 * world.setBlockState(pos.up(), stateup = stateup.withProperty(HOLDER,
			 * false)); world.setTileEntity(pos.up(), null);
			 * world.setBlockState(pos.up(), stateup = stateup.withProperty(JOINED,
			 * true)); }
			 */
			if (ent instanceof TileEntityRobotDeploy)
				((TileEntityRobotDeploy) ent).setOwner(placer.getName(), placer.getUniqueID());
		}
		if (world.isAirBlock(pos.up()))
			world.setBlockState(pos.up(), state.withProperty(HOLDER, false), 2);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		if (state.getValue(HOLDER)) {
			if (world.getBlockState(pos.up()).getBlock() == this) {
				world.setBlockToAir(pos.up());
			}
		} else {
			if (world.getBlockState(pos.down()).getBlock() == this) {
				world.setBlockToAir(pos.down());
			}
			if (world.getBlockState(pos.up()).getBlock() == this
					&& !world.getBlockState(pos.up()).getValue(HOLDER)) {
				world.setBlockToAir(pos.up());
			}
		}

		if (state.getValue(JOINED)) {
			if (state.getValue(HOLDER) || (world.getBlockState(pos.down()).getBlock() == this
					&& world.getBlockState(pos.down()).getValue(HOLDER)))
				world.destroyBlock(pos.offset(state.getValue(FACING).rotateY()), true);
			else
				world.destroyBlock(pos.offset(state.getValue(FACING).rotateYCCW()), true);
		}

		TileEntity ent = world.getTileEntity(pos);
		if (ent instanceof TileEntityRobotDeploy) {
			((TileEntityRobotDeploy) ent).dropInventory();
		}
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos)
				&& world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up());
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
		return this.getDefaultState().withProperty(FACING, EnumFacing.getFront((meta & 3) + 2))
				.withProperty(HOLDER, (meta & 4) == 4).withProperty(JOINED, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() - 2 + (state.getValue(HOLDER) ? 4 : 0)
				+ (state.getValue(JOINED) ? 8 : 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING, HOLDER, JOINED, ACTIVE });
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
}
