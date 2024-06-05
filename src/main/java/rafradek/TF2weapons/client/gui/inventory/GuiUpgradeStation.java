package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.gui.GuiTooltip;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.TF2Attribute.Type;
import rafradek.TF2weapons.inventory.ContainerUpgrades;
import rafradek.TF2weapons.tileentity.TileEntityUpgrades;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiUpgradeStation extends GuiContainer {
	public GuiUpgradeStation(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}

	private static final ResourceLocation UPGRADES_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/upgrades.png");

	// public ItemStack[] itemsToRender;
	public ArrayList<GuiTooltip> tooltip = new ArrayList<>();
	public GuiButton[] buttons = new GuiButton[12];
	public GuiButton refund;
	public int firstIndex;
	public float scroll;
	public int tabid;
	public ItemStack craftingTabStack = new ItemStack(TF2weapons.itemAmmo, 1, 1);
	public ItemStack chestTabStack = new ItemStack(Blocks.CHEST);

	public TileEntityUpgrades station;
	private boolean isScrolling;

	private boolean wasClicking;

	public GuiUpgradeStation(InventoryPlayer playerv, TileEntityUpgrades station, World world,
			BlockPos blockPosition) {
		super(new ContainerUpgrades(Minecraft.getMinecraft().player, playerv, station, world, blockPosition));
		this.station = station;
		this.xSize = 230;
		this.ySize = 225;
		// this.itemsToRender=new ItemStack[9];
	}

	@Override
	public void initGui() {
		super.initGui();
		this.tooltip.clear();
		for (int x = 0; x < 2; x++)
			for (int y = 0; y < 3; y++) {
				this.buttonList.add(buttons[x * 2 + y * 4] = new GuiButton(x * 2 + y * 4, this.guiLeft + 81 + x * 101,
						this.guiTop + 47 + y * 30, 12, 12, "+"));
				this.buttonList.add(buttons[x * 2 + y * 4 + 1] = new GuiButton(x * 2 + y * 4 + 1,
						this.guiLeft + 94 + x * 101, this.guiTop + 47 + y * 30, 12, 12, "-"));
			}
		this.tooltip.add(new GuiTooltip(this.guiLeft + 128, this.guiTop + 15, 100, 12,
				I18n.format("container.upgrades.info"), this));
		this.buttonList.add(refund = new GuiButton(12, this.guiLeft + 123, this.guiTop + 121, 100, 20,
				I18n.format("container.upgrades.refund")));
		setButtons();
	}

	public void setButtons() {
		/*
		 * for(int i=0;i<12;i++){
		 * //System.out.println("Buttons: "+buttonsItem[i]+" "+firstIndex);
		 * if(i+firstIndex<TF2CraftingManager.INSTANCE.getRecipeList().size()){
		 * buttonsItem[i].stackToDraw=TF2CraftingManager.INSTANCE.getRecipeList(
		 * ).get(i+firstIndex).getRecipeOutput();
		 * buttonsItem[i].selected=i+firstIndex==((ContainerTF2Workbench)this.
		 * inventorySlots).currentRecipe; } else{ buttonsItem[i].stackToDraw=null;
		 * buttonsItem[i].selected=false; } }
		 */
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		boolean flag = Mouse.isButtonDown(0);
		int i = this.guiLeft;
		int j = this.guiTop;
		int k = i + 209;
		int l = j + 30;
		int i1 = k + 14;
		int j1 = l + 96;

		if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1)
			this.isScrolling = true;

		if (!flag)
			this.isScrolling = false;

		this.wasClicking = flag;

		if (this.isScrolling) {
			int size = ((ContainerUpgrades) this.inventorySlots).applicable.size();
			if (size >= 6) {
				this.scroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
				this.scroll = MathHelper.clamp(this.scroll, 0.0F, 1.0F);
				this.firstIndex = Math.round(this.scroll * (size - 6) / 2) * 2;
				this.setButtons();
			}
		}

		this.refund.enabled = !this.inventorySlots.inventorySlots.get(0).getStack().isEmpty()
				&& this.inventorySlots.inventorySlots.get(0).getStack().getTagCompound().getInteger("TotalSpent") > 0;
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
		for (GuiTooltip tooltip : this.tooltip) {
			tooltip.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		for (int m = 0; m < 6; m++) {

		}
	}

	@Override
	public void drawHoveringText(List<String> textLines, int x, int y) {
		drawHoveringText(textLines, x, y, fontRenderer);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id < 12)
			this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id + this.firstIndex * 2);
		else if (button.id == 12)
			this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, -1);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int expPoints = ((ContainerUpgrades) this.inventorySlots).playerCredits;
		int size = ((ContainerUpgrades) this.inventorySlots).applicable.size();
		ItemStack stack = this.inventorySlots.inventorySlots.get(0).getStack();
		for (int i = 0; i < 6; i++) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.getTextureManager().bindTexture(UPGRADES_GUI_TEXTURES);
			if (i + firstIndex < size) {
				TF2Attribute attr = ((ContainerUpgrades) this.inventorySlots).applicable.get(i + firstIndex)
						.getAttributeReplacement(stack);
				TF2Attribute attrorig = ((ContainerUpgrades) this.inventorySlots).applicable.get(i + firstIndex);
				if (attr == null) {
					continue;
				}

				int xOffset = 101 * (i % 2);
				int yOffset = (i / 2) * 30;
				int currLevel = attr.calculateCurrLevel(stack);

				int austrUpgrade = stack.getTagCompound().hasKey(NBTLiterals.AUSTR_UPGRADED)
						? stack.getTagCompound().getShort(NBTLiterals.AUSTR_UPGRADED)
						: -1;
				boolean austr = currLevel == attr.numLevels && attr.austrUpgrade != 0f
						&& stack.getTagCompound().getBoolean("Australium") && austrUpgrade != attr.id;
				boolean hasAustr = austrUpgrade == attr.id;

				for (int j = 0; j < this.station.attributes.get(attrorig); j++) {
					// System.out.println("render: "+currLevel+"
					// "+this.inventorySlots.inventorySlots.get(0).getStack());
					this.drawTexturedModalRect(9 + xOffset + j * 9, 50 + yOffset, currLevel > j ? 240 : 248,
							!hasAustr ? 24 : 32, 8, 8);

				}
				int cost = austr ? 0 : attr.getUpgradeCost(stack);

				if (currLevel < this.station.attributes.get(attrorig))
					this.fontRenderer.drawString(String.valueOf(cost), 56 + xOffset, 50 + yOffset, 16777215);
				this.fontRenderer.drawSplitString(
						attr.getTranslatedString((attr.typeOfValue == Type.ADDITIVE ? 0 : 1)
								+ attr.getPerLevel(stack) * (austr ? attr.austrUpgrade : 1f), false),
						9 + xOffset, 32 + yOffset, 98, 16777215);
				this.buttons[i * 2].visible = true;
				this.buttons[i * 2 + 1].visible = true;

				if (!attr.canApply(stack) || (currLevel >= this.station.attributes.get(attrorig) && !austr)
						|| cost > expPoints || cost + stack.getTagCompound().getInteger("TotalSpent") > TF2Attribute
								.getMaxExperience(stack, mc.player)) {
					// System.out.println("DrawingRect");
					this.buttons[i * 2].enabled = false;
					this.buttons[i * 2 + 1].enabled = currLevel > 0;
					this.drawGradientRect(8 + xOffset, 31 + yOffset, 107 + xOffset, 59 + yOffset, 0x77000000,
							0x77000000);
				} else {
					this.buttons[i * 2].enabled = true;
					this.buttons[i * 2 + 1].enabled = currLevel > 0;
				}
			} else {
				this.buttons[i * 2].visible = false;
				this.buttons[i * 2 + 1].visible = false;
			}
		}
		this.fontRenderer.drawString(I18n.format("container.upgrades", new Object[0]), 8, 5, 4210752);
		this.fontRenderer.drawString(I18n.format("container.currency", new Object[] { String.valueOf(expPoints) }), 128,
				5, 4210752);
		if (!stack.isEmpty())
			this.fontRenderer.drawString(
					I18n.format("container.currencyLeft",
							new Object[] { String.valueOf(stack.getTagCompound().getInteger("TotalSpent")),
									String.valueOf(TF2Attribute.getMaxExperience(stack, mc.player)) }),
					128, 15, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 36, this.ySize - 96 + 3,
				4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(UPGRADES_GUI_TEXTURES);
		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
		// this.drawTab(0);
		// this.drawTab(1);

		x = this.guiLeft + 210;
		y = this.guiTop + 31;
		int k = y + 96;

		this.drawTexturedModalRect(x, y + (int) ((k - y - 17) * this.scroll), 232, 0, 12, 15);
	}
}