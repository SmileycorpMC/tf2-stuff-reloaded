package rafradek.TF2weapons.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.*;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.tileentity.IEntityConfigurable;

import java.util.ArrayList;
import java.util.Map.Entry;

public class GuiConfigurable2 extends GuiConfig {

	public NBTTagCompound tag;
	public ConfigCategory category;
	public ConfigCategory outcategory;
	public BlockPos pos;
	public static ConfigCategory copyConfig;
	private GuiButton pasteButton;
	private static final String[] CLASS_SELECT_NAMES = { "none", "scout", "soldier", "pyro", "demoman", "heavy",
			"engineer", "medic", "sniper", "spy" };

	public GuiConfigurable2(GuiScreen parentScreen, ArrayList<IConfigElement> elements, ConfigCategory category,
			ConfigCategory outcategory, BlockPos pos) {
		super(parentScreen, elements, "", false, false, "TF2 Stuff Configuration");
		this.category = category;
		this.pos = pos;
		this.outcategory = outcategory;
	}

	public static GuiConfigurable2 create(NBTTagCompound tag, BlockPos pos) {
		IEntityConfigurable entity = (IEntityConfigurable) Minecraft.getMinecraft().world.getTileEntity(pos);
		ArrayList<IConfigElement> configElements = new ArrayList<>();
		ConfigCategory category = new ConfigCategory("root");

		for (String key : tag.getKeySet()) {
			if (!key.equals("Outputs")) {
				Property prop = null;
				switch (tag.getTagId(key)) {
				case 8:
					prop = addString(category, key, (NBTTagString) tag.getTag(key));
					break;
				case 1:
					prop = addByte(category, key, (NBTTagByte) tag.getTag(key));
					break;
				case 3:
					prop = addPrimitive(category, key, (NBTTagInt) tag.getTag(key));
					break;
				case 5:
					prop = addPrimitive(category, key, (NBTTagFloat) tag.getTag(key));
					break;
				case 6:
					prop = addPrimitive(category, key, (NBTTagDouble) tag.getTag(key));
					break;
				case 7:
					prop = addPrimitive(category, key, (NBTTagDouble) tag.getTag(key));
					break;
				case 11:
					prop = addIntArray(category, key, (NBTTagIntArray) tag.getTag(key));
					break;
				}
				if (prop != null) {
					String[] allowed = entity.getAllowedValues(key);
					if (allowed != null)
						prop.setValidValues(allowed);
					category.put(key, prop);
				}
			}
		}

		ConfigCategory outputcat = new ConfigCategory("Outputs");
		NBTTagCompound outputtag = tag.getCompoundTag("Outputs");
		for (String outname : entity.getOutputs()) {
			Property outprop = new Property(outname, new String[0], Type.STRING);
			NBTTagList outlist = outputtag.getTagList(outname, 11);
			String[] outvalues = new String[outlist.tagCount()];
			for (int i = 0; i < outlist.tagCount(); i++) {
				int[] coord = ((NBTTagIntArray) outlist.get(i)).getIntArray();
				outvalues[i] = coord[0] + " " + coord[1] + " " + coord[2] + " " + coord[3];
			}
			outprop.set(outvalues);
			outputcat.put(outname, outprop);
		}
		configElements.add(new ConfigElement(outputcat));
		configElements.addAll(new ConfigElement(category).getChildElements());
		return new GuiConfigurable2(null, configElements, category, outputcat, pos);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.pasteButton = new GuiButton(2501, this.width - 60, 0, 50, 20, "Paste");
		this.buttonList.add(new GuiButton(2500, 10, 0, 50, 20, "Copy"));
		this.buttonList.add(pasteButton);
		if (copyConfig == null)
			pasteButton.enabled = false;
	}

	public static Property addString(ConfigCategory cat, String name, NBTTagString tag) {
		Property prop = new Property(name, tag.getString(), Type.STRING);
		if (name.startsWith("T:")) {
			Scoreboard scoreboard = Minecraft.getMinecraft().world.getScoreboard();
			String[] teams = new String[scoreboard.getTeams().size() + 1];
			teams[0] = "";
			int i = 1;
			for (ScorePlayerTeam team : scoreboard.getTeams()) {
				teams[i] = team.getName();
				i++;
			}
			prop.setValidValues(teams);
		} else if (name.startsWith("C:")) {
			prop.setValidValues(CLASS_SELECT_NAMES);
		}
		return prop;
	}

	public static Property addIntArray(ConfigCategory cat, String name, NBTTagIntArray tag) {
		if (name.startsWith("L:")) {
			String[] values = new String[tag.getIntArray().length];
			for (int i = 0; i < values.length; i++) {
				values[i] = String.valueOf(tag.getIntArray()[i]);
			}
			Property prop = new Property(name, values, Type.INTEGER);
			return prop;
		} else {
			StringBuilder builder = new StringBuilder();
			int[] array = tag.getIntArray();
			for (int i = 0; i < array.length; i++) {
				builder.append(array[i]);
				if (i != array.length - 1) {
					builder.append(' ');
				}
			}
			Property prop = new Property(name, builder.toString(), Type.STRING);
			prop.setComment("position");
			return prop;
		}

	}

	public static Property addPrimitive(ConfigCategory cat, String name, NBTPrimitive tag) {

		Property prop = new Property(name, tag.toString(),
				(tag instanceof NBTTagFloat || tag instanceof NBTTagDouble) ? Type.DOUBLE : Type.INTEGER);
		return prop;

	}

	public static Property addByte(ConfigCategory cat, String name, NBTTagByte tag) {
		Property prop = new Property(name, tag.getByte() != 0 ? "true" : "false", Type.BOOLEAN);
		return prop;
	}

	private void copyCategory(ConfigCategory src, ConfigCategory dest) {
		for (Entry<String, Property> entry : src.entrySet()) {
			if (dest.containsKey(entry.getKey())) {
				dest.put(entry.getKey(), entry.getValue());
			}
		}
		for (ConfigCategory config : src.getChildren()) {
			String name = config.getName();
			for (ConfigCategory config2 : dest.getChildren()) {
				if (name.equals(config2.getName()))
					this.copyCategory(config, config2);
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button.id == 2000) {
			sendChanges();
		} else if (button.id == 2500) {
			copyConfig = new ConfigCategory(category.getName());
			copyConfig.putAll(category);
			pasteButton.enabled = true;
		} else if (button.id == 2501) {
			if (copyConfig != null) {
				this.copyCategory(copyConfig, category);
				this.sendChanges();
				this.mc.displayGuiScreen(this.parentScreen);
			}
		}
	}

	public void sendChanges() {
		NBTTagCompound newtag = new NBTTagCompound();
		for (Entry<String, Property> entry : category.entrySet()) {
			Property prop = entry.getValue();
			if (prop.getType() == Type.STRING) {
				if (prop.getComment().equals("position")) {
					String[] propvalues = prop.getString().split(" ");
					int[] intarray = new int[propvalues.length];
					for (int i = 0; i < propvalues.length; i++) {
						intarray[i] = Integer.parseInt(propvalues[i]);
					}
					newtag.setIntArray(entry.getKey(), intarray);
				} else
					newtag.setString(entry.getKey(), prop.getString());
			}
			if (prop.getType() == Type.BOOLEAN) {
				newtag.setByte(entry.getKey(), (byte) (prop.getBoolean() ? 1 : 0));
			}
			if (prop.getType() == Type.INTEGER) {
				if (prop.isList()) {
					newtag.setIntArray(entry.getKey(), prop.getIntList());
				} else {
					newtag.setInteger(entry.getKey(), prop.getInt());
				}
			}
		}
		NBTTagCompound outtag = new NBTTagCompound();
		for (Entry<String, Property> entry : outcategory.entrySet()) {
			Property prop = entry.getValue();
			NBTTagList list = new NBTTagList();
			for (String value : prop.getStringList()) {
				try {
					String[] decode = value.split(" ");
					int[] coord = new int[] { Integer.parseInt(decode[0]), Integer.parseInt(decode[1]),
							Integer.parseInt(decode[2]), 15 };
					if (decode.length >= 4)
						coord[3] = Integer.parseInt(decode[3]);
					list.appendTag(new NBTTagIntArray(coord));
				} catch (Exception e) {
					TF2weapons.LOGGER.error("Invalid output target");
				}
			}
			outtag.setTag(entry.getKey(), list);
		}
		newtag.setTag("Outputs", outtag);
		TF2weapons.network.sendToServer(new TF2Message.GuiConfigMessage(newtag, pos));
	}
}
