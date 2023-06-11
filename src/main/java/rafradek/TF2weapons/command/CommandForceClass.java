package rafradek.TF2weapons.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemToken;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandForceClass extends CommandBase {

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "commands.forceclass.usage";
	}

	@Override
	public String getName() {
		return "forceclass";
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
			EntityPlayerMP player = args.length > 1 ? getPlayer(server, sender, args[1])
					: getCommandSenderAsPlayer(sender);
			if (args[0].equals("clear")) {
				((ItemToken) TF2weapons.itemToken).updateAttributes(ItemStack.EMPTY, player);
				WeaponsCapability.get(player).forcedClass = false;
				notifyCommandListener(sender, this, "commands.forceclass.success2", new Object[] { player.getName() });
			} else {
				int clazz = ItemToken.getClassID(args[0]);
				((ItemToken) TF2weapons.itemToken).updateAttributes(new ItemStack(TF2weapons.itemToken, 1, clazz),
						player);
				WeaponsCapability.get(player).forcedClass = true;
				notifyCommandListener(sender, this, "commands.forceclass.success",
						new Object[] { player.getName(), ItemToken.CLASS_NAMES[clazz] });
			}

		} catch (Exception e) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
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
