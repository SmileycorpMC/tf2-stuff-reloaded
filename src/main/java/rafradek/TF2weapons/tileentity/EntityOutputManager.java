package rafradek.TF2weapons.tileentity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;

public class EntityOutputManager {
	public World world;
	public Multimap<String, Tuple<BlockPos, Integer>> outputs = HashMultimap.create();
	public String name = "";
	public IEntityConfigurable entity;

	public EntityOutputManager(IEntityConfigurable entity) {
		this.entity = entity;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public void loadOutputs(NBTTagCompound tag) {
		outputs.clear();
		for (String key : tag.getKeySet()) {
			NBTTagList list = tag.getTagList(key, 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] arr = ((NBTTagIntArray) list.get(i)).getIntArray();
				outputs.put(key, new Tuple<>(new BlockPos(arr[0], arr[1], arr[2]), arr[3]));
			}
		}
	}

	public NBTTagCompound saveOutputs(NBTTagCompound tag) {
		for (String key : outputs.keySet()) {
			NBTTagList list = new NBTTagList();
			for (Tuple<BlockPos, Integer> pos : outputs.get(key)) {
				list.appendTag(new NBTTagIntArray(new int[] { pos.getFirst().getX(), pos.getFirst().getY(),
						pos.getFirst().getZ(), pos.getSecond() }));
			}
			tag.setTag(key, list);
		}
		return tag;
	}

	public void readConfig(NBTTagCompound tag) {
		this.loadOutputs(tag.getCompoundTag("Outputs"));
		this.name = tag.getString("Link Name");
		this.entity.readConfig(tag);
	}

	public NBTTagCompound writeConfig(NBTTagCompound tag) {
		tag.setTag("Outputs", this.saveOutputs(new NBTTagCompound()));
		tag.setString("Link Name", name);
		this.entity.writeConfig(tag);
		return tag;
	}

	public void activateOutput(String output, float power, int minTime) {
		TF2weapons.LOGGER.info("activated " + output);
		for (Tuple<BlockPos, Integer> tup : outputs.get(output)) {
			BlockPos pos = tup.getFirst();
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof BlockButton) {
				world.setBlockState(pos, state.withProperty(BlockButton.POWERED, Boolean.valueOf(true)), 3);
				world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
				world.notifyNeighborsOfStateChange(pos.offset(state.getValue(BlockDirectional.FACING).getOpposite()),
						state.getBlock(), false);
				world.scheduleUpdate(pos, state.getBlock(),
						(int) (state.getBlock().tickRate(world) * power * 15f / tup.getSecond()));
			} else if (state.getBlock() instanceof BlockLever) {
				state = state.withProperty(BlockLever.POWERED, tup.getSecond() != 0);
				EnumFacing enumfacing = state.getValue(BlockLever.FACING).getFacing();
				world.setBlockState(pos, state, 3);
				world.notifyNeighborsOfStateChange(pos.offset(enumfacing.getOpposite()), state.getBlock(), false);
				world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
			} else if (state.getBlock() instanceof BlockRedstoneComparator) {
				world.setBlockState(pos, state.withProperty(BlockButton.POWERED, Boolean.valueOf(true)), 3);
				if (world.getTileEntity(pos) != null)
					((TileEntityComparator) world.getTileEntity(pos)).setOutputSignal((int) (tup.getSecond() * power));

				world.scheduleUpdate(pos, state.getBlock(), minTime);
				world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
			} else if (state.getBlock() instanceof BlockRedstoneWire) {
				state = state.withProperty(BlockRedstoneWire.POWER, 15);
				world.setBlockState(pos, state, 3);
				world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
			}
		}
	}
}
