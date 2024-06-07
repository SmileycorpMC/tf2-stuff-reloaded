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
	private static final ResourceLocation CONTRACTS_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/contracts.png");

	public GuiContracts() {}

	@Override
	public void initGui() {
		mc.getConnection().sendPacket(new CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS));
		mc.player.getCapability(TF2weapons.PLAYER_CAP, null).newContracts = false;
		mc.player.getCapability(TF2weapons.PLAYER_CAP, null).newRewards = false;
		super.initGui();
		guiLeft = width / 2 - 128;
		guiTop = height / 2 - 108;
		buttonList.add(new GuiButton(0, guiLeft + 7, guiTop + 189, 100, 20, I18n.format("gui.contracts.accept")));
		buttonList.add(new GuiButton(1, guiLeft + 107, guiTop + 189, 71, 20, I18n.format("gui.contracts.reject")));
		buttonList.add(new GuiButton(2, guiLeft + 178, guiTop + 189, 71, 20, I18n.format("gui.done")));
		if (selectedContract != null) {
			buttonList.get(1).enabled = true;
			buttonList.get(0).enabled = !selectedContract.active || selectedContract.rewards > 0;
			buttonList.get(0).displayString = I18n.format(selectedContract.active ? "gui.contracts.claim" : "gui.contracts.accept");
		} else {
			buttonList.get(1).enabled = false;
			buttonList.get(0).enabled = false;
			buttonList.get(0).displayString = I18n.format("gui.contracts.select");
		}
		for (int i = 0; i < mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts.size(); i++) {
			Contract contract = mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts.get(i);
			buttonList.add(new GuiButton(i + 3, this.guiLeft + 7, this.guiTop + 16 + i * 20, 74, 20,
					I18n.format("gui.contracts." + contract.className, new Object[0]) + ": " + contract.progress + " CP"));
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id > 2) {
			selectedId = button.id - 3;
			selectedContract = mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts.get(button.id - 3);
			// this.displayItem(((GuiButtonToggleItem)button).stackToDraw);
		} else if (button.id == 0 && selectedId >= 0) {
			if (!selectedContract.active) {
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(32 + selectedId));
				selectedContract.active = true;
			} else if (selectedContract.rewards > 0) {
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(48 + selectedId));
				selectedContract.rewards = 0;
				if (selectedContract.progress >= Contract.REWARD_HIGH) {
					mc.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts.remove(selectedId);
					selectedContract = null;
					selectedId = -1;
					buttonList.clear();
					initGui();
				}
			}
		} else if (button.id == 1 && selectedId >= 0) {
			mc.displayGuiScreen(new GuiYesNo((result, id) -> {
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
				Minecraft.getMinecraft().displayGuiScreen(this);
			}, "Confirm", "Are you sure you want to decline this contract?", 0));
		} else if (button.id == 2) mc.displayGuiScreen(null);
		if (selectedContract != null) {
			buttonList.get(1).enabled = true;
			buttonList.get(0).enabled = !selectedContract.active || selectedContract.rewards > 0;
			buttonList.get(0).displayString = I18n.format(selectedContract.active ? "gui.contracts.claim" : "gui.contracts.accept");
		} else {
			buttonList.get(1).enabled = false;
			buttonList.get(0).enabled = false;
			buttonList.get(0).displayString = I18n.format("gui.contracts.select");
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(CONTRACTS_GUI_TEXTURES);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 216);
		super.drawScreen(mouseX, mouseY, partialTicks);
		int contractDay = mc.player.getStatFileWriter().readStat(TF2Achievements.CONTRACT_DAY);
		fontRenderer.drawString(I18n.format("gui.contracts", new Object[0]), guiLeft + 8, guiTop + 5, 4210752);
		if (contractDay != 0) {
			String str = I18n.format("gui.contracts.contractDay", contractDay - mc.world.getWorldTime() / 24000);
			fontRenderer.drawString(str, guiLeft + 251 - fontRenderer.getStringWidth(str), guiTop + 5, 4210752);
		}
		if (selectedContract != null) {
			fontRenderer.drawString(I18n.format("gui.contracts." + selectedContract.className, new Object[0]), guiLeft + 83, guiTop + 18, 0xFFFFFF);
			this.fontRenderer.drawString(I18n.format("gui.contracts.objectives"), guiLeft + 83, guiTop + 35, 0xFFFFFF);
			if (!selectedContract.className.equals("kill")) fontRenderer.drawString(I18n.format("gui.contracts.objectives_t",
							I18n.format("entity." + selectedContract.className + ".name")), guiLeft + 83, guiTop + 47, 0xFFFFFF);
			for (int i = 0; i < selectedContract.objectives.length; i++) {
				String str = I18n.format("objective." + selectedContract.objectives[i].toString().toLowerCase());
				if (selectedContract.objectives[i].advanced) str = I18n.format("objective.advanced") + " " + str;
				str += " (" + selectedContract.objectives[i].getPoints() + " CP)";
				fontRenderer.drawSplitString(str, guiLeft + 83, guiTop + 67 + i * 20, 164, 0xFFFFFF);
			}
			fontRenderer.drawString(I18n.format("gui.contracts.rewards"), guiLeft + 9, guiTop + 135, 0xFFFFFF);
			fontRenderer.drawString(I18n.format("gui.contracts.reward1"), guiLeft + 9, guiTop + 148, 0xFFFFFF);
			fontRenderer.drawString(I18n.format("gui.contracts.reward1m"), guiLeft + 9, guiTop + 157, 0xFFFFFF);
			fontRenderer.drawString(I18n.format("gui.contracts.reward2"), guiLeft + 9, guiTop + 170, 0xFFFFFF);
			fontRenderer.drawString(I18n.format("gui.contracts.reward2m"), guiLeft + 9, guiTop + 179, 0xFFFFFF);
		} else if (contractDay == 0) fontRenderer.drawSplitString(I18n.format("gui.contracts.require"),
				guiLeft + 83, guiTop + 18, 164, 0xFFFFFF);
	}
}
