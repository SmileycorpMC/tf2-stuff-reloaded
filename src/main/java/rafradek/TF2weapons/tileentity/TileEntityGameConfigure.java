package rafradek.TF2weapons.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.arena.GameArena;

import javax.annotation.Nullable;

public class TileEntityGameConfigure extends TileEntity implements ITickable, IEntityConfigurable {

	private static final String[] OUTPUT_NAMES = {};
	private EntityOutputManager outputManager = new EntityOutputManager(this);
	private String name = "";

	@Override
	public void update() {

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("Config", this.getOutputManager().writeConfig(new NBTTagCompound()));

		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.getOutputManager().readConfig(compound.getCompoundTag("Config"));

	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		return super.receiveClientEvent(id, type);
	}

	@Override
	public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability,
			@Nullable net.minecraft.util.EnumFacing facing) {
		if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
			@Nullable net.minecraft.util.EnumFacing facing) {
		return super.getCapability(capability, facing);
	}

	@Override
	public void onLoad() {}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return super.shouldRefresh(world, pos, oldState, newSate);
	}

	@Override
	protected void setWorldCreate(World worldIn) {
		this.setWorld(worldIn);
	}

	@Override
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		this.getOutputManager().world = worldIn;
	}

	@Override
	public NBTTagCompound writeConfig(NBTTagCompound tag) {
		tag.setString("Arena Name", this.getName());
		GameArena arena = this.world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(this.getName());
		if (arena != null) {
			arena.writeConfig(tag);
		} else {
			tag.setIntArray("Min Bounds", new int[3]);
			tag.setIntArray("Max Bounds", new int[3]);
		}
		return tag;
	}

	@Override
	public void readConfig(NBTTagCompound tag) {
		this.name = tag.getString("Arena Name").trim();
		if (!this.name.isEmpty()) {
			GameArena arena = this.world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(this.getName());
			if (arena != null) {
				arena.readConfig(tag);
			} else {
				arena = new GameArena(this.getWorld(), this.name, this.getPos());
				arena.readConfig(tag);
				this.world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.put(this.name, arena);
			}
		}
	}

	@Override
	public EntityOutputManager getOutputManager() {
		return this.outputManager;
	}

	@Override
	public String[] getOutputs() {
		return OUTPUT_NAMES;
	}

	public String getName() {
		return name;
	}

	public void removeGameArena() {
		GameArena arena = this.world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(this.getName());
		if (arena != null && arena.getName().equals(this.name)) {
			arena.markDelete = true;
		}
	}

}
