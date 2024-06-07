package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.inventory.ContainerRobotDeploy;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;

public class GuiRobotDeploy extends GuiContainer {

	private static final ResourceLocation ROBOT_DEPLOY_TEXTURE = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/robotdeploy.png");

	TileEntityRobotDeploy tileEntity;
	InventoryPlayer player;

	public GuiRobotDeploy(InventoryPlayer player, TileEntityRobotDeploy tileEntity) {
		super(new ContainerRobotDeploy(player, tileEntity));
		this.tileEntity = tileEntity;
		this.player = player;
		ySize = 193;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(ROBOT_DEPLOY_TEXTURE);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
		float pr = (float) ((ContainerRobotDeploy) inventorySlots).progress / ((ContainerRobotDeploy) inventorySlots).maxprogress;
		if (tileEntity.produceGiant()) {
			drawTexturedModalRect(i + 142, j + 23, 192, 31, 24, 48);
			drawTexturedModalRect(i + 142, j + 23 + (int) (48 * (1 - pr)), 216, 31 + (int) (48 * (1 - pr)), 24, (int) (48 * pr));
		} else drawTexturedModalRect(i + 146, j + 38 + (int) (32 * (1 - pr)), 176, 31 + (int) (32 * (1 - pr)), 16, (int) (32 * pr));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = "container.robotdeploy";
		fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2,
				4210752);
		fontRenderer.drawString("x" + tileEntity.getRequirement(0), 64, 18, 4210752);
		fontRenderer.drawString("x" + tileEntity.getRequirement(1), 46, 39, 4210752);
		fontRenderer.drawString("x" + tileEntity.getRequirement(2), 46, 60, 4210752);
		fontRenderer.drawString("x" + tileEntity.getCurrencyRequirement(), 64, 81, 4210752);
		/*
		 * if (this.tileEntity.produceGiant())
		 * this.fontRenderer.drawString("x2"+this.tileEntity.getRequirement(2), 135, 18,
		 * 4210752);
		 */
	}
}
