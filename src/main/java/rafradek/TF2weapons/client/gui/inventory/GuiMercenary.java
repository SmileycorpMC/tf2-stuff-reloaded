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

	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/mercenary.png");

	public EntityTF2Character mercenary;
	public GuiButton hireBtn;
	public GuiButton shareBtn;
	public GuiButton orderBtn;
	public GuiButton[] mainWeaponButton = new GuiButton[3];
	public InventoryPlayer inv;

	public GuiMercenary(InventoryPlayer inv, EntityTF2Character mercenary, World worldIn) {
		super(inv, mercenary, worldIn);
		this.mercenary = mercenary;
		this.inv = inv;
		this.inventorySlots = new ContainerMercenary(Minecraft.getMinecraft().player, mercenary, worldIn);

		this.xSize += 54;
		// merInv=mercenary.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
		// null);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.add(hireBtn = new GuiButton(60, this.guiLeft + (this.xSize / 2) - 75, this.guiTop - 25, 150, 20,
				"Hire mercenary (1 Australium ingot)"));
		this.buttonList.add(shareBtn = new GuiButton(62, this.guiLeft + (this.xSize / 2) - 75,
				this.guiTop + this.ySize + 5, 150, 20, "Share loot (1 Australium ingot)"));
		this.buttonList.add(orderBtn = new GuiButton(61, this.guiLeft + 179, this.guiTop + 123, 48, 20, "Order"));
		for (int i = 0; i < 3; i++) {
			this.buttonList.add(mainWeaponButton[i] = new GuiButton(63 + i, this.guiLeft + 223,
					this.guiTop + 12 + i * 18, 4, 6, ""));
		}
		this.updateButtons();
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
			orderBtn.displayString = this.mercenary.getOrder().toString();
			if (!mercenary.isRobot()) {
				shareBtn.visible = true;
				shareBtn.enabled = !this.mercenary.isSharing()
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
			this.mainWeaponButton[i].enabled = mc.player.getUniqueID().equals(mercenary.getOwnerId())
					|| mc.player.isCreative();
			if (mercenary.getMainWeapon() == i) {
				this.mainWeaponButton[i].displayString = "|";
			} else {
				this.mainWeaponButton[i].displayString = "";
			}
		}

	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 61) {
			if (this.mercenary.getOrder() == Order.FOLLOW)
				this.mercenary.setOrder(Order.HOLD);
			else
				this.mercenary.setOrder(Order.FOLLOW);
			this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId,
					10 + this.mercenary.getOrder().ordinal());
		} else if (button.id == 60) {
			this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 0);
			if (this.mercenary.getOwnerId() == null)
				this.mercenary.setOwner(mc.player);
			else
				this.mercenary.setOwner(null);
		} else if (button.id == 62) {
			this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 1);
			this.mercenary.setSharing(true);
		} else if (button.id >= 63 && button.id < 66) {
			this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id - 13);
			if (this.mercenary.getMainWeapon() == button.id - 63) {
				this.mercenary.setMainWeapon(-1);
			} else {
				this.mercenary.setMainWeapon(button.id - 63);
			}
		}
		this.updateButtons();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.hireBtn.isMouseOver())
			this.drawHoveringText("Lost australium can be recovered at Mann Co store", mouseX, mouseY);
		else if (this.shareBtn.isMouseOver())
			this.drawHoveringText("Allows the owner to collect loot from enemies", mouseX, mouseY);

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
		int i = this.guiLeft + this.xSize - 54;
		int j = this.guiTop;
		this.drawTexturedModalRect(i, j, 176, 0, 54, 146);
		this.fontRenderer.drawString("Refill", i + 7, j + 80, 4210752);
		this.fontRenderer.drawString(Integer.toString(((ContainerMercenary) this.inventorySlots).primaryAmmo), i + 10,
				j + 113, 4210752);
		this.fontRenderer.drawString(Integer.toString(((ContainerMercenary) this.inventorySlots).secondaryAmmo), i + 33,
				j + 113, 4210752);

		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		this.itemRender.zLevel = 0;
		for (int k = 0; k < 4; k++) {
			ItemStack stack = this.mercenary.loadout.getStackInSlot(k);
			if (!stack.isEmpty()) {
				if (k < 4 && !this.inventorySlots.getSlot(k + 43).getHasStack()) {
					this.itemRender.renderItemIntoGUI(stack, this.inventorySlots.getSlot(k + 43).xPos + this.guiLeft,
							this.inventorySlots.getSlot(k + 43).yPos + this.guiTop);
				} else if (k >= 3 && !this.inventorySlots.getSlot(k + 36).getHasStack()) {
					this.itemRender.renderItemIntoGUI(stack, this.inventorySlots.getSlot(k - 3).yPos,
							this.inventorySlots.getSlot(k - 3).xPos);
				}
			}

		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
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
	protected void renderHoveredToolTip(int p_191948_1_, int p_191948_2_) {
		super.renderHoveredToolTip(p_191948_1_, p_191948_2_);
		if (this.mc.player.inventory.getItemStack().isEmpty() && this.getSlotUnderMouse() != null
				&& !this.getSlotUnderMouse().getHasStack()) {
			int id = this.getSlotUnderMouse().slotNumber;
			if (id >= 43 && id < 47 && !this.mercenary.loadout.getStackInSlot(id - 43).isEmpty())
				this.renderToolTip(this.mercenary.loadout.getStackInSlot(id - 43), p_191948_1_, p_191948_2_);
		}
	}
}
