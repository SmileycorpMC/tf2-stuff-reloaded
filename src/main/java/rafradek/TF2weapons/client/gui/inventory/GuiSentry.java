package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.inventory.ContainerEnergy;
import rafradek.TF2weapons.message.TF2Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class GuiSentry extends GuiContainer {

	public EntitySentry sentry;
	public GuiButton doneBtn;
	public GuiButton grab;
	public int channel;
	public boolean exit;
	private GuiButton attackEnemyTeamBtn;
	private GuiButton attackOnHurtBtn;
	private GuiButton attackOtherPlayersBtn;
	private GuiButton attackHostileMobsBtn;
	private GuiButton attackFriendlyMobsBtn;
	public int attackFlags;

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/building.png");

	public GuiSentry(EntitySentry sentry) {
		super(new ContainerEnergy(sentry, Minecraft.getMinecraft().player.inventory));
		this.sentry = sentry;
		this.attackFlags = sentry.getAttackFlags();
		this.xSize = 212;
		this.ySize = 195;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList
				.add(this.attackOnHurtBtn = new GuiButton(1, this.guiLeft + 181, this.guiTop + 14, 25, 20, "no"));
		this.buttonList
				.add(this.attackOtherPlayersBtn = new GuiButton(2, this.guiLeft + 181, this.guiTop + 34, 25, 20, "no"));
		this.buttonList
				.add(this.attackHostileMobsBtn = new GuiButton(3, this.guiLeft + 181, this.guiTop + 54, 25, 20, "no"));
		this.buttonList
				.add(this.attackFriendlyMobsBtn = new GuiButton(4, this.guiLeft + 181, this.guiTop + 74, 25, 20, "no"));
		this.buttonList.add(this.grab = new GuiButton(5, this.guiLeft + 86, this.guiTop + 90, 40, 20,
				I18n.format("gui.teleporter.drop", new Object[0])));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled)
			if (button.id >= 1 && button.id <= 4) {
				attackFlags ^= 1 << (button.id - 1);
				if ((attackFlags & (1 << (button.id - 1))) == 0)
					button.displayString = "no";
				else
					button.displayString = "yes";
				TF2weapons.network.sendToServer(
						new TF2Message.BuildingConfigMessage(this.sentry.getEntityId(), (byte) 0, attackFlags));
			} else if (button.id == 5) {
				this.mc.displayGuiScreen(null);
				TF2weapons.network
						.sendToServer(new TF2Message.BuildingConfigMessage(this.sentry.getEntityId(), (byte) 127, 1));
			}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.fontRenderer.drawString(I18n.format("gui.sentry.info", new Object[0]), 8, 5, 4210752);
		this.fontRenderer.drawString(I18n.format("gui.sentry.onhurt", new Object[0]), 25, 20, 4210752);
		this.fontRenderer.drawString(I18n.format("gui.sentry.player", new Object[0]), 25, 40, 4210752);
		this.fontRenderer.drawString(I18n.format("gui.sentry.hostile", new Object[0]), 25, 60, 4210752);
		this.fontRenderer.drawString(I18n.format("gui.sentry.friendly", new Object[0]), 25, 80, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 25, 99, 4210752);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.attackOnHurtBtn.displayString = (attackFlags & 1) == 0 ? "no" : "yes";
		this.attackOtherPlayersBtn.displayString = (attackFlags & 2) == 0 ? "no" : "yes";
		this.attackHostileMobsBtn.displayString = (attackFlags & 4) == 0 ? "no" : "yes";
		this.attackFriendlyMobsBtn.displayString = (attackFlags & 8) == 0 ? "no" : "yes";

		this.drawDefaultBackground();

		super.drawScreen(mouseX, mouseY, partialTicks);
		if (mouseX >= this.guiLeft + 7 && mouseX < this.guiLeft + 23 && mouseY >= this.guiTop + 15
				&& mouseY < guiTop + 75) {
			if (ClientProxy.buildingsUseEnergy)
				this.drawHoveringText(
						"Energy: " + this.sentry.getInfoEnergy() + "/" + this.sentry.energy.getMaxEnergyStored(),
						mouseX, mouseY);
			else
				this.drawHoveringText("Energy use is disabled", mouseX, mouseY);
		}
		if (mouseX >= this.guiLeft + 5 && mouseX < this.guiLeft + 23 && mouseY >= this.guiTop + 112
				&& mouseY < guiTop + 130) {
			List<String> list = new ArrayList<>(
					Arrays.asList(I18n.format("gui.sentry.help", this.sentry.getLevel() == 1 ? "6.4" : "12.8")
							.split(Pattern.quote("\\n"))));
			if (this.sentry.getLevel() > 2)
				list.add(I18n.format("gui.sentry.help.rockets"));
			this.drawHoveringText(list, mouseX, mouseY);
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
		this.drawGradientRect(this.guiLeft + 7, this.guiTop + 75
				- (int) (((float) this.sentry.getInfoEnergy() / (float) this.sentry.energy.getMaxEnergyStored()) * 60f),
				this.guiLeft + 23, this.guiTop + 75, 0xFFBF0000, 0xFF7F0000);
	}
}
