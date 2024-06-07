package rafradek.TF2weapons.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message;

import java.io.IOException;
import java.util.List;

public class GuiTeamSelect extends GuiScreen {

	private List<String> teams;
	private int[] numbers;
	private boolean[] allowed;

	public GuiTeamSelect(List<String> teams, int[] numbers, boolean[] allowed) {
		this.teams = teams;
		this.numbers = numbers;
		this.allowed = allowed;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when
	 * the GUI is displayed and when the window resizes, the buttonList is cleared
	 * beforehand.
	 */
	@Override
	public void initGui() {
		for (int i = 0; i < teams.size(); i++) {
			GuiButton button = new GuiButton(i, width / 2 - (int) ((teams.size() / 2f) * 100) - 35 + i * 100, 110, 70, 20, teams.get(i));
			button.enabled = allowed[i];
			buttonList.add(button);
		}
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for
	 * buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		TF2weapons.network.sendToServer(new TF2Message.ActionMessage(120 + button.id));
		mc.displayGuiScreen(null);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, "Team Selection", width / 2, 70, 16777215);
		for (int i = 0; i < teams.size(); i++) drawCenteredString(fontRenderer, "x" + numbers[i], width / 2 - (int) ((teams.size() / 2f) * 100) - 35 + i * 100, 98, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
