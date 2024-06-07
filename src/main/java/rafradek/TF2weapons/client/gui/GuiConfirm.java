package rafradek.TF2weapons.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.List;

public class GuiConfirm extends GuiScreen {
	protected String messageLine1;
	private final String messageLine2;
	private final List<String> listLines = Lists.<String>newArrayList();
	/** The text shown for the first button in GuiYesNo */
	protected String confirmButtonText;
	/** The text shown for the second button in GuiYesNo */
	protected int parentButtonClickedId;

	public GuiConfirm(String line1, String line2) {
		messageLine1 = line1;
		messageLine2 = line2;
		confirmButtonText = I18n.format("gui.ok", new Object[0]);
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called
	 * when the GUI is displayed and when the window resizes, the buttonList is
	 * cleared beforehand.
	 */
	@Override
	public void initGui() {
		buttonList.add(new GuiOptionButton(0, width / 2 - 75, height / 6 + 96, confirmButtonText));
		listLines.clear();
		listLines.addAll(fontRenderer.listFormattedStringToWidth(messageLine2, width - 50));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed
	 * for buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		mc.displayGuiScreen(null);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, messageLine1, width / 2, 70, 16777215);
		int i = 90;
		for (String s : listLines) {
			drawCenteredString(fontRenderer, s, width / 2, i, 16777215);
			i += fontRenderer.FONT_HEIGHT;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
