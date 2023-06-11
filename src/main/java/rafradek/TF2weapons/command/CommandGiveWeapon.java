package rafradek.TF2weapons.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemHandlerHelper;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemFromData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandGiveWeapon extends CommandBase {

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "commands.giveweapon.usage";
	}

	@Override
	public String getName() {
		return "giveweapon";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1)
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		try {
			EntityPlayerMP entityplayermp = args.length > 1 ? getPlayer(server, sender, args[1])
					: getCommandSenderAsPlayer(sender);
			ItemStack item;
			boolean giveNew = !args[0].startsWith("-");
			if (giveNew)
				item = ItemFromData.getNewStack(args[0]);
			else
				item = entityplayermp.getHeldItemMainhand();
			boolean remove = args[0].equals("-r");
			NBTTagCompound attributes = item.getTagCompound().getCompoundTag("Attributes");
			for (int i = 2; i < args.length; i++) {
				String[] attr = args[i].split(":");
				if (attr.length == 2) {
					boolean streak = attr[1].startsWith("k");

					if (attr[0].equals("u"))
						item.getTagCompound().setByte("UEffect", remove ? 0 : Byte.parseByte(attr[1]));
					else if (MapList.nameToAttribute.containsKey(attr[0]))
						if (!remove) {
							if (streak) {
								item.getTagCompound().setByte(NBTLiterals.STREAK_LEVEL,
										Byte.parseByte(attr[1].substring(1)));
								item.getTagCompound().setShort(NBTLiterals.STREAK_ATTRIB,
										(short) MapList.nameToAttribute.get(attr[0]).id);
							} else
								attributes.setFloat(Integer.toString(MapList.nameToAttribute.get(attr[0]).id),
										Float.parseFloat(attr[1]));
						} else
							attributes.removeTag(Integer.toString(MapList.nameToAttribute.get(attr[0]).id));
					else if (TF2Attribute.attributes[Integer.parseInt(attr[0])] != null)
						if (!remove)
							if (streak) {
								item.getTagCompound().setByte(NBTLiterals.STREAK_LEVEL,
										Byte.parseByte(attr[1].substring(1)));
								item.getTagCompound().setShort(NBTLiterals.STREAK_ATTRIB, Short.parseShort(attr[0]));
							} else
								attributes.setFloat(attr[0], Float.parseFloat(attr[1]));
						else
							attributes.removeTag(attr[0]);

				} else if (attr[0].equals("a"))
					item.getTagCompound().setBoolean("Australium", !remove);
				else if (attr[0].equals("s"))
					item.getTagCompound().setBoolean("Strange", !remove);
				else if (attr[0].equals("v"))
					item.getTagCompound().setBoolean("Valve", !remove);

			}
			if (giveNew) {
				ItemHandlerHelper.giveItemToPlayer(entityplayermp, item);
			} else
				item.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).cached = false;
			/*
			 * Chunk
			 * chunk=entityplayermp.world.getChunkFromBlockCoords(entityplayermp.getPosition
			 * ()); int ausCount=0; int leadCount=0; int coppCount=0; int diaCount=0;
			 * for(int x=0;x<16;x++){ for(int y=0; y<256;y++){ for(int z=0; z<16;z++){
			 * IBlockState state=chunk.getBlockState(x, y, z);
			 * if(state.getBlock()==TF2weapons.blockAustraliumOre) ausCount++; else
			 * if(state.getBlock()==TF2weapons.blockLeadOre) leadCount++; else
			 * if(state.getBlock()==TF2weapons.blockCopperOre) coppCount++; else
			 * if(state.getBlock()==Blocks.DIAMOND_ORE) diaCount++; } } }
			 */

			// notifyCommandListener(sender, this, "Found ores:"+ausCount+" "+leadCount+"
			// "+coppCount+" "+diaCount, new Object[0]);
			// entityitem.func_145797_a(entityplayermp.getCommandSenderName());
			// func_152373_a(p_71515_1_, this, "commands.giveweapon.success",
			// new Object[] {item.func_151000_E(),
			// entityplayermp.getCommandSenderName()});
		} catch (Exception e) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, MapList.nameToData.keySet());
		} else if (args.length == 2) {
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		} else {
			return !args[args.length - 1].contains(":")
					? getListOfStringsMatchingLastWord(args, MapList.nameToAttribute.keySet())
					: Collections.emptyList();
		}
	}
}
