package rafradek.TF2weapons.command;

import com.google.common.collect.Iterables;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.InvasionEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class CommandInvasion extends CommandBase {

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "commands.invasion.usage";
	}

	@Override
	public String getName() {
		return "invasion";
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
			if (args[0].equalsIgnoreCase("stop")) {
				for (Entry<UUID, InvasionEvent> entry : sender.getEntityWorld().getCapability(TF2weapons.WORLD_CAP,
						null).invasions.entrySet()) {
					entry.getValue().finish();
				}
			} else {
				int difficulty = parseInt(args[0], 1, InvasionEvent.DIFFICULTY.length) - 1;
				BlockPos pos = sender.getPosition();
				if (args.length > 1) {
					pos = parseBlockPos(sender, args, 1, false);
				}
				EntityPlayer player = Iterables.getFirst(server.getPlayerList().getPlayers(), null);
				if (sender.getCommandSenderEntity() instanceof EntityPlayer) {
					player = (EntityPlayer) sender.getCommandSenderEntity();
				}
				// sender.getEntityWorld().getCapability(TF2weapons.WORLD_CAP,
				// null).startInvasion(player, difficulty, true);
				for (Entry<UUID, InvasionEvent> entry : sender.getEntityWorld().getCapability(TF2weapons.WORLD_CAP,
						null).invasions.entrySet()) {
					if (entry.getKey().equals(player.getUniqueID()))
						entry.getValue().finish();
				}
				InvasionEvent event = new InvasionEvent(sender.getEntityWorld(), pos, difficulty);
				sender.getEntityWorld().getCapability(TF2weapons.WORLD_CAP, null).invasions.put(player.getUniqueID(),
						event);
			}
		} catch (Exception e) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, "stop");
		} else if (args.length > 1) {
			BlockPos pos = Minecraft.getMinecraft().player.getPosition();
			switch (args.length) {
			case 2:
				return Arrays.asList(Integer.toString(pos.getX()));
			case 3:
				return Arrays.asList(Integer.toString(pos.getY()));
			case 4:
				return Arrays.asList(Integer.toString(pos.getZ()));
			default:
				return Collections.emptyList();
			}
		} else {
			return Collections.emptyList();
		}
	}
}
