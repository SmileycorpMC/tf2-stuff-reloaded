package rafradek.TF2weapons.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import rafradek.TF2weapons.TF2weapons;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandChangeConfig extends CommandBase {

	private static String[] files;

	@Override
	public String getUsage(ICommandSender p_71518_1_) {
		return "commands.cmodc.usage";
	}

	@Override
	public String getName() {
		return "cmodc";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 3;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		args = joinStrings(args);
		if (args.length < 3)
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		File file = new File(TF2weapons.instance.weaponDir.getParentFile(), args[0]);
		try {
			Configuration conf = new Configuration(file);
			ConfigCategory cat = conf.getCategory(args[1]);
			int i = 2;

			for (; i < args.length; i++) {
				boolean foundc = false;
				for (ConfigCategory child : cat.getChildren()) {
					if (args[i].equals(child.getName())) {
						cat = conf.getCategory(args[i]);
						foundc = true;
						break;
					}
				}
				if (!foundc)
					break;
			}
			Property prop = cat.get(args[i]);
			if (args.length > i + 1) {
				if (args[i + 1].equals("default"))
					prop.setToDefault();
				else if (prop.isList()) {
					prop.setValues(Arrays.copyOfRange(args, i + 1, args.length));
				} else
					prop.setValue(args[i + 1]);
			}
			notifyCommandListener(sender, this, "commands.cmodc.success",
					new Object[] { prop.getName(), prop.getString() });
			conf.save();
		} catch (Exception e) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {
		args = joinStrings(args);
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, files);
		} else if (args.length == 2) {
			try {
				Configuration conf = new Configuration(new File(new File("./config"), args[0]));
				String[] ret = conf.getCategoryNames().toArray(new String[conf.getCategoryNames().size()]);
				for (int i = 0; i < ret.length; i++) {
					if (ret[i].contains(" "))
						ret[i] = "\"" + ret[i] + "\"";
				}
				return getListOfStringsMatchingLastWord(args, ret);
			} catch (Exception e) {

			}
			return Collections.emptyList();
		} else if (args.length == 3) {
			try {
				Configuration conf = new Configuration(new File(new File("./config"), args[0]));
				ConfigCategory cat = conf.getCategory(args[1]);
				String[] ret = cat.keySet().toArray(new String[cat.keySet().size()]);
				for (int i = 0; i < ret.length; i++) {
					if (ret[i].contains(" "))
						ret[i] = "\"" + ret[i] + "\"";
				}
				return getListOfStringsMatchingLastWord(args, ret);
			} catch (Exception e) {

			}
			return Collections.emptyList();
		}
		return Collections.emptyList();
	}

	public static String[] joinStrings(String[] args) {
		ArrayList<String> build = new ArrayList<>();
		String joined = null;
		boolean open = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("\"")) {
				open = true;
				joined = "";
				args[i] = args[i].substring(1, args[i].length());
			}
			if (open) {
				joined = joined.concat(args[i]);
				if (args[i].endsWith("\"")) {
					joined = joined.substring(0, joined.length() - 1);
					open = false;
					build.add(joined);
				} else {
					joined = joined.concat(" ");
				}
			} else {
				build.add(args[i]);
			}

		}
		return build.toArray(new String[build.size()]);
	}

	static {
		File dir = new File("./config");
		files = dir.list((dirn, name) -> name.endsWith(".cfg"));
	}
}
