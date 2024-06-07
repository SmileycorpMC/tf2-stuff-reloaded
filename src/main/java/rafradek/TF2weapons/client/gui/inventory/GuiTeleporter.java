package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.inventory.ContainerEnergy;
import rafradek.TF2weapons.message.TF2Message;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class GuiTeleporter extends GuiContainer {

	public EntityTeleporter teleporter;
	public GuiButton doneBtn;
	public GuiButton teleportUpBtn;
	public GuiButton teleportDownBtn;
	public GuiButton exitToggle;
	public GuiButton grab;
	private GuiTextField teleportField;
	public int channel;
	public boolean exit;

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/building.png");

	public GuiTeleporter(EntityTeleporter teleporter) {
		super(new ContainerEnergy(teleporter, Minecraft.getMinecraft().player.inventory));
		this.teleporter = teleporter;
		exit = teleporter.isExit();
		channel = teleporter.getID();
		xSize = 212;
		ySize = 195;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		teleportField = new GuiTextField(5, fontRenderer, width / 2 - 40, height / 2 - 40, 30, 20);
		teleportField.setMaxStringLength(3);
		teleportField.setFocused(true);
		teleportField.setText(Integer.toString(channel + 1));
		buttonList.add(teleportUpBtn = new GuiButton(1, width / 2 - 60, height / 2 - 40, 20, 20, "+"));
		buttonList.add(teleportDownBtn = new GuiButton(2, width / 2 - 10, height / 2 - 40, 20, 20, "-"));
		buttonList.add(exitToggle = new GuiButton(3, width / 2 + 10, height / 2 - 40, 50, 20, "Exit"));
		buttonList.add(grab = new GuiButton(4, guiLeft + 86, guiTop + 90, 40, 20, I18n.format("gui.teleporter.drop", new Object[0])));
		if (channel == 127) {
			teleportField.setEnabled(false);
			teleportUpBtn.enabled = false;
			teleportDownBtn.enabled = false;
			exitToggle.enabled = false;
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (!button.enabled) return;
		if (button.id == 1) {
			channel++;
			if (channel >= EntityTeleporter.TP_PER_PLAYER - 1) channel = 0;
			teleportField.setText(Integer.toString(channel + 1));
		} else if (button.id == 2) {
			channel--;
			if (channel < 0) channel = EntityTeleporter.TP_PER_PLAYER - 2;
			teleportField.setText(Integer.toString(channel + 1));
		} else if (button.id == 3 && teleporter.isExit()) exit = !exit;
		else if (button.id == 4) {
			mc.displayGuiScreen(null);
			TF2weapons.network.sendToServer(new TF2Message.BuildingConfigMessage(teleporter.getEntityId(), (byte) 127, 0));
		}
	}

	@Override
	public void onGuiClosed() {
		TF2weapons.network.sendToServer(new TF2Message.BuildingConfigMessage(teleporter.getEntityId(), (byte) 0, channel));
		TF2weapons.network.sendToServer(new TF2Message.BuildingConfigMessage(teleporter.getEntityId(), (byte) 1, exit ? 1 : 0));
		super.onGuiClosed();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		teleportField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		teleportField.textboxKeyTyped(typedChar, keyCode);
		if (channel == 127) return;
		try {
			channel = MathHelper.clamp(Integer.parseInt(this.teleportField.getText()) - 1, 0, EntityTeleporter.TP_PER_PLAYER - 2);
		} catch (Exception ex) {}
		// this.teleportField.setText(Integer.toString(channel));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		fontRenderer.drawString(I18n.format("gui.teleporter.info", new Object[0]), 8, 5, 4210752);
		fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 25, 99, 4210752);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		// this.drawCenteredString(this.fontRendererObj, Integer.toString(channel),
		// this.width / 2 - 25, this.height / 2,
		// 16777215);
		exitToggle.displayString = exit ? "Exit" : "Entry";
		super.drawScreen(mouseX, mouseY, partialTicks);
		teleportField.drawTextBox();
		if (mouseX >= guiLeft + 7 && mouseX < guiLeft + 23 && mouseY >= guiTop + 15 && mouseY < guiTop + 75) {
			if (ClientProxy.buildingsUseEnergy) drawHoveringText("Energy: " + teleporter.getInfoEnergy() + "/" + teleporter.energy.getMaxEnergyStored(), mouseX, mouseY);
			else drawHoveringText("Energy use is disabled", mouseX, mouseY);
		}
		if (mouseX >= guiLeft + 5 && mouseX < guiLeft + 23 && mouseY >=guiTop + 112 && mouseY < guiTop + 130) drawHoveringText(
					Arrays.asList(I18n.format("gui.teleporter.help", new Object[0]).split(Pattern.quote("\\n"))), mouseX, mouseY);
		// System.out.println("dfs");
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		teleportField.updateCursorCounter();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(GUI_TEXTURES);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		this.drawGradientRect(guiLeft + 7, guiTop + 75 - (int) (((float) teleporter.getInfoEnergy() /
						(float) teleporter.energy.getMaxEnergyStored()) * 60f), guiLeft + 23, guiTop + 75, 0xFFBF0000, 0xFF7F0000);
	}
}
