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

	public GuiConfirm(String p_i1082_2_, String p_i1082_3_) {
		this.messageLine1 = p_i1082_2_;
		this.messageLine2 = p_i1082_3_;
		this.confirmButtonText = I18n.format("gui.ok", new Object[0]);
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called
	 * when the GUI is displayed and when the window resizes, the buttonList is
	 * cleared beforehand.
	 */
	@Override
	public void initGui() {
		this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 75, this.height / 6 + 96, this.confirmButtonText));
		this.listLines.clear();
		this.listLines.addAll(this.fontRenderer.listFormattedStringToWidth(this.messageLine2, this.width - 50));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed
	 * for buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		this.mc.displayGuiScreen(null);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.messageLine1, this.width / 2, 70, 16777215);
		int i = 90;

		for (String s : this.listLines) {
			this.drawCenteredString(this.fontRenderer, s, this.width / 2, i, 16777215);
			i += this.fontRenderer.FONT_HEIGHT;
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
