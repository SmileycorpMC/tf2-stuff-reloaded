package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.inventory.ContainerDispenser;
import rafradek.TF2weapons.message.TF2Message;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class GuiDispenser extends GuiContainer {

	public EntityDispenser dispenser;
	public GuiButton doneBtn;
	public GuiButton grab;
	public int channel;
	public boolean exit;
	public int attackFlags;

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/building.png");

	public GuiDispenser(EntityDispenser dispenser) {
		super(new ContainerDispenser(dispenser, Minecraft.getMinecraft().player.inventory));
		this.dispenser = dispenser;
		this.xSize = 212;
		this.ySize = 195;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(this.grab = new GuiButton(0, this.guiLeft + 86, this.guiTop + 90, 40, 20,
				I18n.format("gui.teleporter.drop", new Object[0])));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled)
			if (button.id == 0) {
				this.mc.displayGuiScreen(null);
				TF2weapons.network.sendToServer(
						new TF2Message.BuildingConfigMessage(this.dispenser.getEntityId(), (byte) 127, 1));
			}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.fontRenderer.drawString(I18n.format("container.dispenser", new Object[0]), 8, 5, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 25, 99, 4210752);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		this.drawDefaultBackground();

		super.drawScreen(mouseX, mouseY, partialTicks);
		if (mouseX >= this.guiLeft + 7 && mouseX < this.guiLeft + 23 && mouseY >= this.guiTop + 15
				&& mouseY < guiTop + 75) {
			if (ClientProxy.buildingsUseEnergy)
				this.drawHoveringText(
						"Energy: " + this.dispenser.getInfoEnergy() + "/" + this.dispenser.energy.getMaxEnergyStored(),
						mouseX, mouseY);
			else
				this.drawHoveringText("Energy use is disabled", mouseX, mouseY);
		}
		if (mouseX >= this.guiLeft + 5 && mouseX < this.guiLeft + 23 && mouseY >= this.guiTop + 112
				&& mouseY < guiTop + 130) {
			this.drawHoveringText(
					Arrays.asList(I18n.format("gui.dispenser.help", new Object[0]).split(Pattern.quote("\\n"))), mouseX,
					mouseY);
		}
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
		this.drawTexturedModalRect(x + 79, y + 14, 0, 202, 54, 54);
		this.drawGradientRect(this.guiLeft + 7, this.guiTop + 75
				- (int) (((float) this.dispenser.getInfoEnergy() / (float) this.dispenser.energy.getMaxEnergyStored())
						* 60f),
				this.guiLeft + 23, this.guiTop + 75, 0xFFBF0000, 0xFF7F0000);
	}
}
