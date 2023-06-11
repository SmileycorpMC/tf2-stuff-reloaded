package rafradek.TF2weapons.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class GuiTooltip extends GuiButton {

	private GuiScreen screen;
	private List<String> list;

	public GuiTooltip(int buttonId, int x, int y, int widthIn, int heightIn, List<String> text, GuiScreen screen) {
		super(buttonId, x, y, widthIn, heightIn, text.get(0));
		this.screen = screen;
		this.list = text;
		this.enabled = false;
	}

	public GuiTooltip(int x, int y, int widthIn, int heightIn, String text, GuiScreen screen) {
		this(99999, x, y, widthIn, heightIn, Arrays.asList(text.split(Pattern.quote("\\n"))), screen);
	}

	public void setText(String string) {
		this.list = Arrays.asList(string.split(Pattern.quote("\\n")));
		this.displayString = string;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
					&& mouseY < this.y + this.height;

			if (this.hovered) {
				this.screen.drawHoveringText(list, mouseX, mouseY);
			}
		}
	}
}
