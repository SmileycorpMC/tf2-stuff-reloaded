package rafradek.TF2weapons.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.arena.GameArena;
import rafradek.TF2weapons.item.ItemToken;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandGameArena extends CommandBase {

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "commands.gamearena.usage";
	}

	@Override
	public String getName() {
		return "gamearena";
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
			EntityPlayerMP player = getCommandSenderAsPlayer(sender);
			switch (args[0]) {
			case "add":
				addGameArena(args[1], player.world, sender);
				break;
			case "remove":
				removeGameArena(args[1], player.world, sender);
				break;
			case "list":
				listGameArena(player.world, sender);
				break;
			case "activate":
				activateGameArena(args[1], player.world, sender);
				break;
			case "deactivate":
				deactivateGameArena(args[1], player.world, sender);
				break;
			case "config":
				configGameArena(args[1], args, player.world, sender);
				break;
			}

		} catch (Exception e) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}

	public void addGameArena(String name, World world, ICommandSender sender) throws CommandException {
		if (!world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.containsKey(name)) {
			world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.put(name, new GameArena(world, name, null));
			notifyCommandListener(sender, this, "commands.gamearena.add.success", new Object[] { name });
		} else {
			throw new CommandException("commands.gamearena.add.alreadyExists", new Object[] { name });
		}
	}

	public void removeGameArena(String name, World world, ICommandSender sender) throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.containsKey(name)) {
			GameArena arena = world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(name);
			arena.setActive(false);
			world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.remove(name);
			notifyCommandListener(sender, this, "commands.gamearena.remove.success", new Object[] { name });
		} else {
			throw new CommandException("commands.gamearena.remove.notExist", new Object[] { name });
		}
	}

	public void listGameArena(World world, ICommandSender sender) throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.isEmpty())
			notifyCommandListener(sender, this, "commands.gamearena.list.empty");
		for (String name : world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.keySet()) {
			notifyCommandListener(sender, this, name);
		}
	}

	public void activateGameArena(String name, World world, ICommandSender sender) throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.containsKey(name)) {
			GameArena arena = world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(name);
			arena.setActive(true);
			notifyCommandListener(sender, this, "commands.gamearena.activate.success", new Object[] { name });
		} else {
			throw new CommandException("commands.gamearena.activate.notExist", new Object[] { name });
		}
	}

	public void deactivateGameArena(String name, World world, ICommandSender sender) throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.containsKey(name)) {
			GameArena arena = world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(name);
			arena.setActive(false);
			notifyCommandListener(sender, this, "commands.gamearena.deactivate.success", new Object[] { name });
		} else {
			throw new CommandException("commands.gamearena.deactivate.notExist", new Object[] { name });
		}
	}

	public void configGameArena(String name, String[] args, World world, ICommandSender sender)
			throws CommandException {
		if (world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.containsKey(name)) {
			GameArena arena = world.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(name);
			NBTTagCompound configtag = arena.writeConfig(new NBTTagCompound());
			if (args[2].equals("list")) {
				notifyCommandListener(sender, this, configtag.toString());
				// for (String key : configtag.getKeySet()) {
				// notifyCommandListener(sender, this, key+" =
				// "+configtag.getTag(key).toString());
				// }
			} else {
				try {
					configtag.merge(JsonToNBT.getTagFromJson(buildString(args, 2)));
				} catch (NBTException e) {
					throw new CommandException("commands.gamearena.config.fail", new Object[] { name });
				}
				arena.readConfig(configtag);
				notifyCommandListener(sender, this, "commands.gamearena.config.success", new Object[] { name });
			}
		} else {
			throw new CommandException("commands.gamearena.config.notExist", new Object[] { name });
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, ItemToken.CLASS_NAMES);
		} else if (args.length == 2) {
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		} else {
			return Collections.emptyList();
		}
	}
}
