package rafradek.TF2weapons.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor;
import rafradek.TF2weapons.tileentity.TileEntityOverheadDoor.Allow;

import java.util.Random;

public class BlockOverheadDoor extends BlockContainer {

	public static final PropertyBool HOLDER = PropertyBool.create("holder");
	public static final PropertyBool SLIDING = PropertyBool.create("sliding");
	public static final PropertyBool CONTROLLER = PropertyBool.create("controller");
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.03125D);
	protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.96875D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.96875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.03125D, 1.0D, 1.0D);

	public BlockOverheadDoor() {
		super(Material.IRON, MapColor.IRON);
		this.setLightOpacity(TF2ConfigVars.doorBlockLight ? 255 : 0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)
				.withProperty(HOLDER, true).withProperty(SLIDING, false));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if ((meta & 4) == 4)
			return new TileEntityOverheadDoor();
		return null;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
		case NORTH:
			return SOUTH_AABB;
		case EAST:
			return WEST_AABB;
		case SOUTH:
			return NORTH_AABB;
		case WEST:
			return EAST_AABB;
		default:
			return FULL_BLOCK_AABB;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(SLIDING)) {
			return NULL_AABB;
		}
		switch (state.getValue(FACING)) {
		case NORTH:
			return SOUTH_AABB;
		case EAST:
			return WEST_AABB;
		case SOUTH:
			return NORTH_AABB;
		case WEST:
			return EAST_AABB;
		default:
			return NULL_AABB;
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	/*
	 * @SuppressWarnings("deprecation") public void updateTick(World world, BlockPos
	 * pos, IBlockState state, Random rand) { IBlockState above =
	 * world.getBlockState(pos.up()); IBlockState bottom =
	 * world.getBlockState(pos.down()); boolean isTop = !(above.getBlock()
	 * instanceof BlockOverheadDoor); boolean isDown = !(bottom.getBlock()
	 * instanceof BlockOverheadDoor); int slide = state.getValue(SLIDE);
	 * //System.out.println("State: "+state.getValue(SLIDE)+" is down: "+isDown);
	 * if(isDown) { if(!world.getEntitiesWithinAABB(EntityLivingBase.class,
	 * this.getBoundingBox(state, world, pos).offset(pos).grow(2)).isEmpty()) {
	 * if(slide != 3) world.setBlockState(pos, state.withProperty(SLIDE, slide +
	 * 1)); else if (!isTop){ world.scheduleUpdate(pos.up(), this,
	 * this.tickRate(world)); world.setBlockToAir(pos); } } else { if(slide != 0) {
	 * world.setBlockState(pos, state.withProperty(SLIDE, slide - 1)); } else if
	 * (world.isAirBlock(pos.down())) { world.setBlockState(pos.down(),
	 * state.withProperty(SLIDE, 3)); } } } if(isDown) world.scheduleUpdate(pos,
	 * this, this.tickRate(world)); }
	 */

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return face == state.getValue(FACING) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(SLIDING) ? 0 : super.getLightOpacity(state, world, pos);
	}

	/*
	 * @Override public boolean isOpaqueCube(IBlockState state) { return false; }
	 * 
	 * @Override
	 * 
	 * @SideOnly(Side.CLIENT) public BlockRenderLayer getBlockLayer() { return
	 * BlockRenderLayer.CUTOUT; }
	 */

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
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
		return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(2 + (meta & 3)))
				.withProperty(HOLDER, (meta & 4) == 4).withProperty(SLIDING, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex() - 2 + (state.getValue(HOLDER) ? 4 : 0)
				+ (state.getValue(SLIDING) ? 8 : 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING, HOLDER, SLIDING });
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
		BlockPos off = pos;
		while (true) {
			off = off.up();
			if (world.getBlockState(off).getBlock() == this)
				world.destroyBlock(off, true);
			else
				break;
		}
		off = pos;
		while (true) {
			off = off.down();
			if (world.getBlockState(off).getBlock() == this)
				world.destroyBlock(off, true);
			else
				break;
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(HOLDER) ? super.getItemDropped(state, rand, fortune) : Items.AIR;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent instanceof TileEntityOverheadDoor) {
			((TileEntityOverheadDoor) ent).powered = world.isBlockPowered(pos);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public net.minecraft.pathfinding.PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world,
			BlockPos pos) {
		return super.getAiPathNodeType(state, world, pos);
	}

	@Override
	public boolean isPassable(IBlockAccess world, BlockPos pos) {
		if (world.getBlockState(pos).getValue(SLIDING))
			return true;
		for (int y = 0; y < 5; y++) {
			BlockPos off = pos.add(0, y, 0);
			if (world.getTileEntity(off) instanceof TileEntityOverheadDoor) {
				TileEntityOverheadDoor ent = (TileEntityOverheadDoor) world.getTileEntity(off);
				return ent.allow == Allow.ENTITY || ent.allow == Allow.TEAM;
			}
		}
		return false;
	}
}
