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

	private static final ResourceLocation UPGRADES_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/upgrades.png");

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
		xSize = 230;
		ySize = 225;
		// this.itemsToRender=new ItemStack[9];
	}

	@Override
	public void initGui() {
		super.initGui();
		tooltip.clear();
		for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) {
			buttonList.add(buttons[x * 2 + y * 4] = new GuiButton(x * 2 + y * 4, guiLeft + 81 + x * 101, guiTop + 47 + y * 30, 12, 12, "+"));
			buttonList.add(buttons[x * 2 + y * 4 + 1] = new GuiButton(x * 2 + y * 4 + 1, guiLeft + 94 + x * 101, guiTop + 47 + y * 30, 12, 12, "-"));
		}
		tooltip.add(new GuiTooltip(guiLeft + 128, guiTop + 15, 100, 12, I18n.format("container.upgrades.info"), this));
		buttonList.add(refund = new GuiButton(12, guiLeft + 123, guiTop + 121, 100, 20, I18n.format("container.upgrades.refund")));
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
		int i = guiLeft;
		int j = guiTop;
		int k = i + 209;
		int l = j + 30;
		int i1 = k + 14;
		int j1 = l + 96;
		if (!wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) isScrolling = true;
		if (!flag) isScrolling = false;
		wasClicking = flag;
		if (isScrolling) {
			int size = ((ContainerUpgrades) inventorySlots).applicable.size();
			if (size >= 6) {
				scroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
				scroll = MathHelper.clamp(scroll, 0.0F, 1.0F);
				firstIndex = Math.round(scroll * (size - 6) / 2) * 2;
				setButtons();
			}
		}
		refund.enabled = !inventorySlots.inventorySlots.get(0).getStack().isEmpty()
				&& inventorySlots.inventorySlots.get(0).getStack().getTagCompound().getInteger("TotalSpent") > 0;
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
		for (GuiTooltip tooltip : tooltip) tooltip.drawButton(mc, mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawHoveringText(List<String> textLines, int x, int y) {
		drawHoveringText(textLines, x, y, fontRenderer);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id < 12) mc.playerController.sendEnchantPacket(inventorySlots.windowId, button.id + firstIndex * 2);
		else if (button.id == 12) mc.playerController.sendEnchantPacket(inventorySlots.windowId, -1);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int expPoints = ((ContainerUpgrades) inventorySlots).playerCredits;
		int size = ((ContainerUpgrades) inventorySlots).applicable.size();
		ItemStack stack = inventorySlots.inventorySlots.get(0).getStack();
		for (int i = 0; i < 6; i++) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(UPGRADES_GUI_TEXTURES);
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

				for (int j = 0; j < station.attributes.get(attrorig); j++) drawTexturedModalRect(9 + xOffset + j * 9, 50 + yOffset,
						currLevel > j ? 240 : 248, !hasAustr ? 24 : 32, 8, 8);
				int cost = austr ? 0 : attr.getUpgradeCost(stack);
				if (currLevel < station.attributes.get(attrorig)) fontRenderer.drawString(String.valueOf(cost), 56 + xOffset, 50 + yOffset, 16777215);
				fontRenderer.drawSplitString(attr.getTranslatedString((attr.typeOfValue == Type.ADDITIVE ? 0 : 1)
						+ attr.getPerLevel(stack) * (austr ? attr.austrUpgrade : 1f), false), 9 + xOffset, 32 + yOffset, 98, 16777215);
				buttons[i * 2].visible = true;
				buttons[i * 2 + 1].visible = true;
				if (!attr.canApply(stack) || (currLevel >= station.attributes.get(attrorig) && !austr)
						|| cost > expPoints || cost + stack.getTagCompound().getInteger("TotalSpent") > TF2Attribute.getMaxExperience(stack, mc.player)) {
					// System.out.println("DrawingRect");
					buttons[i * 2].enabled = false;
					buttons[i * 2 + 1].enabled = currLevel > 0;
					drawGradientRect(8 + xOffset, 31 + yOffset, 107 + xOffset, 59 + yOffset, 0x77000000, 0x77000000);
				} else {
					buttons[i * 2].enabled = true;
					buttons[i * 2 + 1].enabled = currLevel > 0;
				}
			} else {
				buttons[i * 2].visible = false;
				buttons[i * 2 + 1].visible = false;
			}
		}
		fontRenderer.drawString(I18n.format("container.upgrades", new Object[0]), 8, 5, 4210752);
		fontRenderer.drawString(I18n.format("container.currency", new Object[] { String.valueOf(expPoints) }), 128, 5, 4210752);
		if (!stack.isEmpty()) fontRenderer.drawString(I18n.format("container.currencyLeft",
						new Object[] { String.valueOf(stack.getTagCompound().getInteger("TotalSpent")),
									String.valueOf(TF2Attribute.getMaxExperience(stack, mc.player)) }),
					128, 15, 4210752);
		fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 36, ySize - 96 + 3, 4210752);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(UPGRADES_GUI_TEXTURES);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		// this.drawTab(0);
		// this.drawTab(1);
		x = guiLeft + 210;
		y = guiTop + 31;
		int k = y + 96;
		drawTexturedModalRect(x, y + (int) ((k - y - 17) * scroll), 232, 0, 12, 15);
	}
	
}