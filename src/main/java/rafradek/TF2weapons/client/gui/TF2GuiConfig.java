package rafradek.TF2weapons.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import rafradek.TF2weapons.TF2weapons;

import java.util.ArrayList;
import java.util.List;

public class TF2GuiConfig extends GuiConfig {

	public TF2GuiConfig(GuiScreen parentScreen) {
		super(parentScreen, getElements(), "rafradek_tf2_weapons", false, false, "TF2 Stuff Configuration");
	}

	public static List<IConfigElement> getElements() {
		ArrayList<IConfigElement> configElements = new ArrayList<>();
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("modcompatibility")));
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("spawn rate")));
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("world gen")));
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("mercenary")));
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("default building targets")));
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("sound volume")));
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("adaption")));
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("building")));
		configElements.add(new ConfigElement(TF2weapons.conf.getCategory("miscellaneous")));
		configElements.addAll(new ConfigElement(TF2weapons.conf.getCategory("gameplay")).getChildElements());
		return configElements;
		/*
		 * List<IConfigElement> list = new
		 * ConfigElement(TF2weapons.conf.getCategory("gameplay")).getChildElements();
		 * list.addAll(new
		 * ConfigElement(TF2weapons.conf.getCategory("modcompatibility")).
		 * getChildElements()); return list;
		 */
	}
}
