package rafradek.TF2weapons.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.util.ResourceLocation;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.util.Contract;

import java.io.IOException;

public class GuiContracts extends GuiScreen {

	private int guiLeft;
	private int guiTop;

	public Contract selectedContract;
	public int selectedId = -1;
	private static final ResourceLocation CONTRACTS_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/contracts.png");

	public GuiContracts() {}

	@Override
	public void initGui() {
		this.mc.getConnection().sendPacket(new CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS));
		this.mc.player.getCapability(TF2weapons.PLAYER_CAP, null).newContracts = false;
		this.mc.player.getCapability(TF2weapons.PLAYER_CAP, null).newRewards = false;

		super.initGui();
		this.guiLeft = this.width / 2 - 128;
		this.guiTop = this.height / 2 - 108;
		this.buttonList.add(
				new GuiButton(0, this.guiLeft + 7, this.guiTop + 189, 100, 20, I18n.format("gui.contracts.accept")));
		this.buttonList.add(
				new GuiButton(1, this.guiLeft + 107, this.guiTop + 189, 71, 20, I18n.format("gui.contracts.reject")));
		this.buttonList.add(new GuiButton(2, this.guiLeft + 178, this.guiTop + 189, 71, 20, I18n.format("gui.done")));
		if (this.selectedContract != null) {
			this.buttonList.get(1).enabled = true;
			this.buttonList.get(0).enabled = !this.selectedContract.active || this.selectedContract.rewards > 0;
			this.buttonList.get(0).displayString = I18n
					.format(this.selectedContract.active ? "gui.contracts.claim" : "gui.contracts.accept");
		} else {
			this.buttonList.get(1).enabled = false;
			this.buttonList.get(0).enabled = false;
			this.buttonList.get(0).displayString = I18n.format("gui.contracts.select");
		}
		for (int i = 0; i < this.mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts.size(); i++) {
			Contract contract = this.mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts.get(i);
			this.buttonList.add(new GuiButton(i + 3, this.guiLeft + 7, this.guiTop + 16 + i * 20, 74, 20,
					I18n.format("gui.contracts." + contract.className, new Object[0]) + ": " + contract.progress
							+ " CP"));
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id > 2) {
			this.selectedId = button.id - 3;
			this.selectedContract = this.mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts
					.get(button.id - 3);

			// this.displayItem(((GuiButtonToggleItem)button).stackToDraw);
		} else if (button.id == 0 && this.selectedId >= 0) {
			if (!this.selectedContract.active) {
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(32 + this.selectedId));
				this.selectedContract.active = true;
			} else if (this.selectedContract.rewards > 0) {
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(48 + this.selectedId));
				this.selectedContract.rewards = 0;
				if (this.selectedContract.progress >= Contract.REWARD_HIGH) {
					this.mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts.remove(this.selectedId);
					this.selectedContract = null;
					this.selectedId = -1;
					this.buttonList.clear();
					this.initGui();
				}
			}
		} else if (button.id == 1 && this.selectedId >= 0) {
			this.mc.displayGuiScreen(new GuiYesNo((result, id) -> {
				if (result) {
					TF2weapons.network.sendToServer(new TF2Message.ActionMessage(64 + selectedId));
					mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts.remove(selectedId);
					selectedContract = null;
					selectedId = -1;
					buttonList.clear();
					initGui();
					mc.player.getStatFileWriter().unlockAchievement(mc.player, TF2Achievements.CONTRACT_DAY,
							(int) (mc.world.getWorldTime() / 24000 + 1));

				}
				Minecraft.getMinecraft().displayGuiScreen(GuiContracts.this);
			}, "Confirm", "Are you sure you want to decline this contract?", 0));
		} else if (button.id == 2) {
			this.mc.displayGuiScreen(null);
		}
		if (this.selectedContract != null) {
			this.buttonList.get(1).enabled = true;
			this.buttonList.get(0).enabled = !this.selectedContract.active || this.selectedContract.rewards > 0;
			this.buttonList.get(0).displayString = I18n
					.format(this.selectedContract.active ? "gui.contracts.claim" : "gui.contracts.accept");
		} else {
			this.buttonList.get(1).enabled = false;
			this.buttonList.get(0).enabled = false;
			this.buttonList.get(0).displayString = I18n.format("gui.contracts.select");
		}

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(CONTRACTS_GUI_TEXTURES);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 256, 216);
		super.drawScreen(mouseX, mouseY, partialTicks);
		int contractDay = this.mc.player.getStatFileWriter().readStat(TF2Achievements.CONTRACT_DAY);
		this.fontRenderer.drawString(I18n.format("gui.contracts", new Object[0]), this.guiLeft + 8, this.guiTop + 5,
				4210752);
		if (contractDay != 0) {
			String str = I18n.format("gui.contracts.contractDay", contractDay - this.mc.world.getWorldTime() / 24000);
			this.fontRenderer.drawString(str, this.guiLeft + 251 - this.fontRenderer.getStringWidth(str),
					this.guiTop + 5, 4210752);
		}
		if (this.selectedContract != null) {
			this.fontRenderer.drawString(I18n.format("gui.contracts." + this.selectedContract.className, new Object[0]),
					this.guiLeft + 83, this.guiTop + 18, 0xFFFFFF);
			this.fontRenderer.drawString(I18n.format("gui.contracts.objectives"), this.guiLeft + 83, this.guiTop + 35,
					0xFFFFFF);
			if (!this.selectedContract.className.equals("kill"))
				this.fontRenderer.drawString(
						I18n.format("gui.contracts.objectives_t",
								I18n.format("entity." + this.selectedContract.className + ".name")),
						this.guiLeft + 83, this.guiTop + 47, 0xFFFFFF);
			for (int i = 0; i < this.selectedContract.objectives.length; i++) {
				String str = I18n.format("objective." + this.selectedContract.objectives[i].toString().toLowerCase());
				if (this.selectedContract.objectives[i].advanced)
					str = I18n.format("objective.advanced") + " " + str;
				str += " (" + this.selectedContract.objectives[i].getPoints() + " CP)";
				this.fontRenderer.drawSplitString(str, this.guiLeft + 83, this.guiTop + 67 + i * 20, 164, 0xFFFFFF);
			}
			this.fontRenderer.drawString(I18n.format("gui.contracts.rewards"), this.guiLeft + 9, this.guiTop + 135,
					0xFFFFFF);
			this.fontRenderer.drawString(I18n.format("gui.contracts.reward1"), this.guiLeft + 9, this.guiTop + 148,
					0xFFFFFF);
			this.fontRenderer.drawString(I18n.format("gui.contracts.reward1m"), this.guiLeft + 9, this.guiTop + 157,
					0xFFFFFF);
			this.fontRenderer.drawString(I18n.format("gui.contracts.reward2"), this.guiLeft + 9, this.guiTop + 170,
					0xFFFFFF);
			this.fontRenderer.drawString(I18n.format("gui.contracts.reward2m"), this.guiLeft + 9, this.guiTop + 179,
					0xFFFFFF);
		} else if (contractDay == 0) {
			this.fontRenderer.drawSplitString(I18n.format("gui.contracts.require"), this.guiLeft + 83, this.guiTop + 18,
					164, 0xFFFFFF);
		}
	}
}
