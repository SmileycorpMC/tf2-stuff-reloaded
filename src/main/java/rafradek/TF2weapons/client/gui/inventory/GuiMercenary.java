package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.inventory.ContainerMercenary;

import java.io.IOException;

public class GuiMercenary extends GuiMerchant {

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/mercenary.png");

	public EntityTF2Character mercenary;
	public GuiButton hireBtn;
	public GuiButton shareBtn;
	public GuiButton orderBtn;
	public GuiButton[] mainWeaponButton = new GuiButton[3];
	public InventoryPlayer inv;

	public GuiMercenary(InventoryPlayer inv, EntityTF2Character mercenary, World world) {
		super(inv, mercenary, world);
		this.mercenary = mercenary;
		this.inv = inv;
		inventorySlots = new ContainerMercenary(Minecraft.getMinecraft().player, mercenary, world);
		xSize += 54;
		// merInv=mercenary.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
		// null);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(hireBtn = new GuiButton(60, guiLeft + (xSize / 2) - 75, guiTop - 25, 150, 20,
				"Hire mercenary (1 Australium ingot)"));
		buttonList.add(shareBtn = new GuiButton(62, guiLeft + (xSize / 2) - 75,
				guiTop + ySize + 5, 150, 20, "Share loot (1 Australium ingot)"));
		buttonList.add(orderBtn = new GuiButton(61, guiLeft + 179, guiTop + 123, 48, 20, "Order"));
		for (int i = 0; i < 3; i++) buttonList.add(mainWeaponButton[i] = new GuiButton(63 + i, guiLeft + 223,
					guiTop + 12 + i * 18, 4, 6, ""));
		updateButtons();
	}

	public void updateButtons() {
		// hireBtn.visible = mercenary.getOwner() == null;
		if (mercenary.isRobot()) {
			hireBtn.visible = false;
			shareBtn.visible = false;
		}
		if (mercenary.getOwnerId() == null) {
			hireBtn.enabled = inv.hasItemStack(new ItemStack(TF2weapons.itemTF2, 1, 2));
			hireBtn.displayString = "Hire this mercenary (1 Australium ingot)";
			orderBtn.enabled = false;
			shareBtn.visible = false;
		} else if (mercenary.getOwnerId().equals(mc.player.getUniqueID())) {
			hireBtn.enabled = true;
			hireBtn.displayString = "Fire this mercenary";
			orderBtn.enabled = true;
			orderBtn.displayString = mercenary.getOrder().toString();
			if (!mercenary.isRobot()) {
				shareBtn.visible = true;
				shareBtn.enabled = !mercenary.isSharing()
						&& inv.hasItemStack(new ItemStack(TF2weapons.itemTF2, 1, 2));
			}
		} else {
			hireBtn.enabled = false;
			hireBtn.displayString = mercenary.ownerName != null ? "Hired mercenary"
					: "Hired by: " + mercenary.ownerName;
			orderBtn.enabled = false;
			shareBtn.visible = false;
		}
		for (int i = 0; i < 3; i++) {
			mainWeaponButton[i].enabled = mc.player.getUniqueID().equals(mercenary.getOwnerId()) || mc.player.isCreative();
			if (mercenary.getMainWeapon() == i) mainWeaponButton[i].displayString = "|";
			else mainWeaponButton[i].displayString = "";
		}

	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 61) {
			if (mercenary.getOrder() == Order.FOLLOW) mercenary.setOrder(Order.HOLD);
			else mercenary.setOrder(Order.FOLLOW);
			mc.playerController.sendEnchantPacket(inventorySlots.windowId, 10 + mercenary.getOrder().ordinal());
		} else if (button.id == 60) {
			mc.playerController.sendEnchantPacket(inventorySlots.windowId, 0);
			if (mercenary.getOwnerId() == null) mercenary.setOwner(mc.player);
			else mercenary.setOwner(null);
		} else if (button.id == 62) {
			mc.playerController.sendEnchantPacket(inventorySlots.windowId, 1);
			mercenary.setSharing(true);
		} else if (button.id >= 63 && button.id < 66) {
			mc.playerController.sendEnchantPacket(inventorySlots.windowId, button.id - 13);
			if (mercenary.getMainWeapon() == button.id - 63) mercenary.setMainWeapon(-1);
			else mercenary.setMainWeapon(button.id - 63);
		}
		updateButtons();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (hireBtn.isMouseOver()) drawHoveringText("Lost australium can be recovered at Mann Co store", mouseX, mouseY);
		else if (shareBtn.isMouseOver()) drawHoveringText("Allows the owner to collect loot from enemies", mouseX, mouseY);

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(GUI_TEXTURES);
		int i = guiLeft + xSize - 54;
		int j = guiTop;
		drawTexturedModalRect(i, j, 176, 0, 54, 146);
		fontRenderer.drawString("Refill", i + 7, j + 80, 4210752);
		fontRenderer.drawString(Integer.toString(((ContainerMercenary) this.inventorySlots).primaryAmmo), i + 10, j + 113, 4210752);
		fontRenderer.drawString(Integer.toString(((ContainerMercenary) this.inventorySlots).secondaryAmmo), i + 33, j + 113, 4210752);
		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.zLevel = 0;
		for (int k = 0; k < 4; k++) {
			ItemStack stack = mercenary.loadout.getStackInSlot(k);
			if (!stack.isEmpty()) {
				if (k < 4 && !inventorySlots.getSlot(k + 43).getHasStack()) itemRender.renderItemIntoGUI(stack,
						inventorySlots.getSlot(k + 43).xPos + guiLeft, inventorySlots.getSlot(k + 43).yPos + guiTop);
				else if (k == 3 && !inventorySlots.getSlot(k + 36).getHasStack())
					itemRender.renderItemIntoGUI(stack, inventorySlots.getSlot(k - 3).yPos, inventorySlots.getSlot(k - 3).xPos);
			}

		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		mc.getTextureManager().bindTexture(GUI_TEXTURES);
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		/*
		 * MerchantRecipeList merchantrecipelist = this.m.getRecipes(this.mc.player);
		 * 
		 * if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) { int k =
		 * this.selectedMerchantRecipe;
		 * 
		 * if (k < 0 || k >= merchantrecipelist.size()) { return; }
		 * 
		 * MerchantRecipe merchantrecipe = (MerchantRecipe)merchantrecipelist.get(k);
		 * 
		 * if (merchantrecipe.isRecipeDisabled()) {
		 * this.mc.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
		 * GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		 * GlStateManager.disableLighting(); this.drawTexturedModalRect(this.guiLeft +
		 * 83, this.guiTop + 21, 212, 0, 28, 21);
		 * this.drawTexturedModalRect(this.guiLeft + 83, this.guiTop + 51, 212, 0, 28,
		 * 21); } }
		 */
	}

	@Override
	protected void renderHoveredToolTip(int mouseX, int mouseY) {
		super.renderHoveredToolTip(mouseX, mouseY);
		if (mc.player.inventory.getItemStack().isEmpty() && getSlotUnderMouse() != null && !getSlotUnderMouse().getHasStack()) {
			int id = getSlotUnderMouse().slotNumber;
			if (id >= 43 && id < 47 && !mercenary.loadout.getStackInSlot(id - 43).isEmpty())
				renderToolTip(mercenary.loadout.getStackInSlot(id - 43), mouseX, mouseY);
		}
	}
}
