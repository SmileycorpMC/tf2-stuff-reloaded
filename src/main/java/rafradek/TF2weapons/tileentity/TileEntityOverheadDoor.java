package rafradek.TF2weapons.tileentity;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.block.BlockOverheadDoor;
import rafradek.TF2weapons.util.TF2Util;

public class TileEntityOverheadDoor extends TileEntity implements ITickable {

	public float amountScrolled = 1;
	public Team team;
	public Allow allow;
	boolean entitySome;
	// boolean master;
	public BlockPos minBounds;
	public BlockPos maxBounds;
	public float motion;
	public boolean powered;
	public boolean hasEntity;
	public boolean clientOpen;
	public boolean lastIsEntity;
	public long tickTime;

	public enum Allow {
		ENTITY, PLAYER, TEAM
	}

	public TileEntityOverheadDoor() {}

	public float getUpSpeed() {
		return 0.25f;
	}

	public float getDownSpeed() {
		return 0.25f;
	}

	@Override
	public void invalidate() {

		if (this.hasWorld()) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				BlockPos sidepos = this.pos.offset(facing);
				TileEntity ent = this.world.getTileEntity(sidepos);
				if (ent instanceof TileEntityOverheadDoor) {
					((TileEntityOverheadDoor) ent).updateMasterStatus();
				}
			}
			this.dropController();
		}
		super.invalidate();
	}

	@Override
	public void update() {

		if (minBounds == null)
			minBounds = this.pos;
		if (minBounds != null) {
			IBlockState state = this.world.getBlockState(minBounds.down());
			if (state.getBlock().isAir(state, world, minBounds.down()))
				this.minBounds = minBounds.down();
			else if (this.world.getBlockState(this.minBounds.down()).getBlock() instanceof BlockOverheadDoor) {
				this.minBounds = minBounds.down();
				this.amountScrolled += 1;
			} else if (!(this.world.getBlockState(this.minBounds).getBlock() instanceof BlockOverheadDoor
					|| world.isAirBlock(this.minBounds))) {
				this.minBounds = new BlockPos(this.minBounds.getX(), this.pos.getY(), this.minBounds.getZ());
			}
		}

		if (!this.world.isRemote && this.allow != null && !powered) {
			hasEntity = this.world.getEntitiesWithinAABB(EntityLivingBase.class,
					new AxisAlignedBB(this.maxBounds, this.minBounds).grow(2, 1, 2),
					input -> ((allow == Allow.PLAYER && input instanceof EntityPlayer)
							|| (allow == Allow.TEAM && input.getTeam() == team)
							|| (allow == Allow.ENTITY && input instanceof EntityLivingBase)))
					.size() > 0;

		} else
			hasEntity = false;
		this.motion = 0;
		boolean isEntity = powered || this.hasEntity || this.entitySome || this.clientOpen;

		if (isEntity && !this.entitySome && !this.world.isRemote)
			this.updateAmountScrolled(15, false);

		if (isEntity && amountScrolled > 0.25) {
			this.motion = -this.getUpSpeed();

		}
		if (!isEntity && !entitySome && pos.getY() - this.minBounds.getY() + 1 > amountScrolled) {
			this.motion = this.getDownSpeed();

		}
		if (this.motion != 0) {
			amountScrolled += this.motion;

		}
		if (lastIsEntity != isEntity) {
			this.world.addBlockEvent(this.pos, this.getBlockType(), 1, isEntity ? 1 : 0);
			if (!entitySome) {
				this.updateAmountScrolled(15, true);
			}
		}

		boolean isClosed = this.world.getBlockState(this.minBounds).getBlock() == this.getBlockType();
		// this.world.setBlockState(pos,
		// this.world.getBlockState(pos).withProperty(BlockOverheadDoor.SLIDING,
		// !isClosed));
		if (isClosed && pos.getY() - this.minBounds.getY() + 1 > amountScrolled) {
			if (!this.world.isRemote) {
				for (int y = pos.getY() - 1; y >= this.minBounds.getY(); y--) {
					// IBlockState state = this.world.getBlockState(pos);
					BlockPos doorpos = new BlockPos(pos.getX(), y, pos.getZ());
					if (this.world.getBlockState(doorpos).getBlock() == this.getBlockType())
						this.world.setBlockToAir(doorpos);
				}
				this.world.setBlockState(pos,
						this.world.getBlockState(pos).withProperty(BlockOverheadDoor.SLIDING, true));
			}
		} else if (!isClosed && pos.getY() - this.minBounds.getY() + 1 <= amountScrolled) {

			IBlockState state = this.world.getBlockState(pos).withProperty(BlockOverheadDoor.HOLDER, false)
					.withProperty(BlockOverheadDoor.SLIDING, false);
			for (int y = pos.getY() - 1; y >= this.minBounds.getY(); y--) {
				BlockPos doorpos = new BlockPos(pos.getX(), y, pos.getZ());
				if (this.world.isAirBlock(doorpos)) {
					if (!this.world.isRemote)
						this.world.setBlockState(doorpos, state);
				}

				else
					this.minBounds = new BlockPos(pos.getX(), doorpos.getY() + 1, pos.getZ());
			}
			if (!this.world.isRemote)
				this.world.setBlockState(pos,
						this.world.getBlockState(pos).withProperty(BlockOverheadDoor.SLIDING, false));
		}
		// System.out.println("done"+this.minBounds.getY()+" "+this.pos.getY()+" ");
		this.lastIsEntity = isEntity;
		this.tickTime = this.world.getTotalWorldTime();
		entitySome = false;
	}

	public boolean isPowered() {
		return this.hasEntity || this.powered || this.clientOpen || this.entitySome;
	}

	public void updateAmountScrolled(int reach, boolean updateEvents) {
		EnumFacing facing = this.world.getBlockState(pos).getValue(BlockHorizontal.FACING).rotateAround(Axis.Y);
		if (facing.getAxisDirection() == AxisDirection.NEGATIVE)
			facing = facing.getOpposite();
		int axispos = TF2Util.getValueOnAxis(this.minBounds, facing.getAxis());
		int minReach = axispos - reach;
		int maxReach = axispos + reach;
		for (int i = axispos - 1; i >= minReach; i--) {
			BlockPos pos = TF2Util.setValueOnAxis(this.pos, facing.getAxis(), i);
			TileEntity ent = this.world.getTileEntity(pos);
			if (ent instanceof TileEntityOverheadDoor) {
				if (((TileEntityOverheadDoor) ent).amountScrolled >= this.amountScrolled) {
					if (!updateEvents)
						((TileEntityOverheadDoor) ent).entitySome = true;
					else
						this.world.addBlockEvent(pos, this.getBlockType(), 1,
								((TileEntityOverheadDoor) ent).isPowered() ? 1 : 0);
				}
			} else
				break;
		}
		for (int i = axispos + 1; i <= maxReach; i++) {
			BlockPos pos = TF2Util.setValueOnAxis(this.pos, facing.getAxis(), i);
			TileEntity ent = this.world.getTileEntity(pos);
			if (ent instanceof TileEntityOverheadDoor) {
				if (((TileEntityOverheadDoor) ent).amountScrolled >= this.amountScrolled) {
					if (!updateEvents)
						((TileEntityOverheadDoor) ent).entitySome = true;
					else
						this.world.addBlockEvent(pos, this.getBlockType(), 1,
								((TileEntityOverheadDoor) ent).isPowered() ? 1 : 0);
				}
			} else
				break;
		}
	}

	@Override
	public void onLoad() {
		updateMasterStatus();
	}

	public void updateMasterStatus() {
		EnumFacing facing = this.world.getBlockState(pos).getValue(BlockHorizontal.FACING).rotateAround(Axis.Y);
		if (facing.getAxisDirection() == AxisDirection.NEGATIVE)
			facing = facing.getOpposite();
		BlockPos minBounds = this.pos;
		BlockPos maxBounds = this.pos;
		/*
		 * for(int i = 1; i <= 16; i++) { Vec3i offset = facing.getDirectionVec();
		 * offset = new BlockPos(offset.getX()*i, offset.getY()*i, offset.getZ()*i);
		 * BlockPos nearpos = this.pos.add(offset); if
		 * (this.world.getBlockState(nearpos).getBlock() == this.getBlockType()) {
		 * TileEntity ent = this.world.getTileEntity(nearpos); if (ent instanceof
		 * TileEntityOverheadDoor && ((TileEntityOverheadDoor)ent).master) { BlockPos
		 * pos = ((TileEntityOverheadDoor)ent).minBounds; pos =
		 * TF2Util.setValueOnAxis(pos, facing.getAxis(),
		 * Math.min(TF2Util.getValueOnAxis(pos, facing.getAxis()),
		 * TF2Util.getValueOnAxis(this.pos, facing.getAxis())));
		 * ((TileEntityOverheadDoor)ent).minBounds = pos; this.amountScrolled =
		 * ((TileEntityOverheadDoor)ent).amountScrolled; this.minBounds = new
		 * BlockPos(this.pos.getX(),((TileEntityOverheadDoor)ent).minBounds.getY(),this.
		 * pos.getZ()); //this.master = false; return; } maxBounds = nearpos; } else
		 * break; } for(int i = -1; i >= -16; i--) { Vec3i offset =
		 * facing.getDirectionVec(); offset = new BlockPos(offset.getX()*i,
		 * offset.getY()*i, offset.getZ()*i); BlockPos nearpos = this.pos.add(offset);
		 * 
		 * if (this.world.getBlockState(nearpos).getBlock() == this.getBlockType()) {
		 * TileEntity ent = this.world.getTileEntity(nearpos);
		 * 
		 * if (ent instanceof TileEntityOverheadDoor &&
		 * ((TileEntityOverheadDoor)ent).master) { BlockPos pos =
		 * ((TileEntityOverheadDoor)ent).maxBounds; pos = TF2Util.setValueOnAxis(pos,
		 * facing.getAxis(), Math.max(TF2Util.getValueOnAxis(pos, facing.getAxis()),
		 * TF2Util.getValueOnAxis(this.pos, facing.getAxis())));
		 * ((TileEntityOverheadDoor)ent).maxBounds = pos; this.amountScrolled =
		 * ((TileEntityOverheadDoor)ent).amountScrolled; this.minBounds = new
		 * BlockPos(this.pos.getX(),((TileEntityOverheadDoor)ent).minBounds.getY(),this.
		 * pos.getZ()); //this.master = false; return; } minBounds = nearpos; } else
		 * break; }
		 */
		// this.master = true;
		this.minBounds = minBounds;
		this.maxBounds = maxBounds;
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			if (this.world.isRemote) {
				if (type == 0)
					this.clientOpen = false;
				else
					this.clientOpen = true;
			}
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock() || !newSate.getValue(BlockOverheadDoor.HOLDER);
	}

	@Override
	public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
		if (this.minBounds == null)
			return new AxisAlignedBB(pos);
		else
			return new AxisAlignedBB(pos, this.minBounds).grow(1);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		// compound.setBoolean("Master", this.master);
		if (team != null)
			compound.setString("Team", this.team.getName());
		if (allow != null)
			compound.setByte("Allow", (byte) this.allow.ordinal());
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		// this.master = compound.getBoolean("Master");
		if (this.hasWorld() && compound.hasKey("Team"))
			this.team = this.world.getScoreboard().getTeam(compound.getString("Team"));
		if (compound.hasKey("Allow"))
			this.allow = Allow.values()[compound.getByte("Allow")];
	}

	public boolean setController(String string) {
		this.dropController();
		if (string.equals("players"))
			allow = Allow.PLAYER;
		else if (string.equals("mobs"))
			allow = Allow.ENTITY;
		else {
			Team target = this.world.getScoreboard().getTeam(string);
			if (target == null)
				return false;
			allow = Allow.TEAM;
			team = target;
		}
		return true;
	}

	public void dropController() {
		if (allow == null)
			return;
		int meta = 0;
		if (allow == Allow.ENTITY)
			meta = 1;
		else if (allow == Allow.TEAM) {
			if (this.team != null && team.getName().equals("RED"))
				meta = 2;
			else if (this.team != null && team.getName().equals("BLU"))
				meta = 3;
		}
		this.getWorld().spawnEntity(new EntityItem(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
				new ItemStack(TF2weapons.itemDoorController, 1, meta)));
	}

	@Override
	protected void setWorldCreate(World world) {
		this.setWorld(world);
	}
}
