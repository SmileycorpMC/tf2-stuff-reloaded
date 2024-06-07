package rafradek.TF2weapons.client.gui.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.lwjgl.input.Mouse;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.gui.GuiButtonToggleItem;
import rafradek.TF2weapons.inventory.ContainerTF2Workbench;
import rafradek.TF2weapons.item.crafting.IRecipeTF2;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;

import java.io.IOException;
import java.util.List;

public class GuiTF2Crafting extends GuiContainer {
	private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/gui/container/cabinet.png");

	public GuiButtonToggleItem[] buttonsItem;
	public NonNullList<ItemStack> itemsToRender;
	public int firstIndex;
	public float scroll;
	public int tabid;
	public ItemStack craftingTabStack = new ItemStack(TF2weapons.itemAmmo, 1, 1);
	public ItemStack chestTabStack = new ItemStack(Blocks.CHEST);

	// public TileEntityCabinet cabinet;
	private boolean isScrolling;

	private boolean wasClicking;

	public GuiTF2Crafting(InventoryPlayer playerv, World world, BlockPos blockPosition) {
		super(new ContainerTF2Workbench(Minecraft.getMinecraft().player, playerv, world, blockPosition));
		// this.cabinet=cabinet;
		xSize = 176;
		ySize = 180;
		itemsToRender = NonNullList.<ItemStack>withSize(9, ItemStack.EMPTY);
		buttonsItem = new GuiButtonToggleItem[12];
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int x = 0; x < 3; x++) for (int y = 0; y < 4; y++) buttonList.add(buttonsItem[x + y * 3] = new GuiButtonToggleItem(x + y * 3,
				guiLeft + 7 + x * 18, guiTop + 14 + y * 18, 18, 18));
		setButtons();
	}

	public void setButtons() {
		for (int i = 0; i < 12; i++) {
			// System.out.println("Buttons: "+buttonsItem[i]+" "+firstIndex);
			if (i + firstIndex < TF2CraftingManager.INSTANCE.getRecipeList().size()) {
				buttonsItem[i].stackToDraw = ContainerTF2Workbench.getReplacement(mc.player,
						TF2CraftingManager.INSTANCE.getRecipeList().get(i + firstIndex).getRecipeOutput().copy());
				buttonsItem[i].selected = i + firstIndex == ((ContainerTF2Workbench) inventorySlots).currentRecipe;
			} else {
				buttonsItem[i].stackToDraw = ItemStack.EMPTY;
				buttonsItem[i].selected = false;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		boolean flag = Mouse.isButtonDown(0);
		int i = guiLeft;
		int j = guiTop;
		int k = i + 61;
		int l = j + 14;
		int i1 = k + 14;
		int j1 = l + 72;
		if (!wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) isScrolling = true;
		if (!flag) isScrolling = false;
		wasClicking = flag;
		if (isScrolling) {
			int size = TF2CraftingManager.INSTANCE.getRecipeList().size();
			scroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
			scroll = MathHelper.clamp(scroll, 0.0F, 1.0F);
			firstIndex = Math.round(scroll * (size - 12) / 3) * 3;
			setButtons();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (getSlotUnderMouse() != null && getSlotUnderMouse().getStack().isEmpty() && mc.player.inventory.getItemStack().isEmpty()
				&& getSlotUnderMouse().inventory instanceof InventoryCrafting &! itemsToRender.get(getSlotUnderMouse().slotNumber - 1).isEmpty())
			renderToolTip(itemsToRender.get(getSlotUnderMouse().slotNumber - 1), mouseX, mouseY);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id >= 12) return;
		int currentRecipe = button.id + firstIndex;
		((ContainerTF2Workbench)inventorySlots).currentRecipe = currentRecipe;
		mc.playerController.sendEnchantPacket(inventorySlots.windowId, currentRecipe);
		setButtons();
		inventorySlots.onCraftMatrixChanged(null);
		itemsToRender = NonNullList.withSize(9, ItemStack.EMPTY);
		if (currentRecipe < 0 || currentRecipe >= TF2CraftingManager.INSTANCE.getRecipeList().size()) return;
		IRecipe recipe = TF2CraftingManager.INSTANCE.getRecipeList().get(currentRecipe);
		for (int i = 1; i < 10; i++) {
			this.handleMouseClick(this.inventorySlots.getSlot(i), i, 0, ClickType.QUICK_MOVE);

		}
		if (recipe instanceof IRecipeTF2) {
			for (int i = 0; i < 9; i++) itemsToRender.set(i, ((IRecipeTF2) recipe).getSuggestion(i));
			return;
		}
		List<Ingredient> input = recipe.getIngredients();
		for (int i = 0; i < input.size(); i++) {
			int space = 0;
			if (recipe instanceof ShapedRecipes) space = (3 - ((ShapedRecipes) recipe).recipeWidth)
						* (i / ((ShapedRecipes) recipe).recipeWidth);
			else if (recipe instanceof ShapedOreRecipe) space = (3 - ((ShapedOreRecipe) recipe).getRecipeWidth())
					* (i / ((ShapedOreRecipe) recipe).getRecipeWidth());
			if (input.get(i).getMatchingStacks().length > 0) {
				itemsToRender.set(i + space, input.get(i).getMatchingStacks()[0]);
				if (itemsToRender.get(i + space).getMetadata() == 32767) itemsToRender.get(i + space).setItemDamage(0);
			}
			for (int j = 10; j < 46; j++) if (input.get(i).apply(inventorySlots.getSlot(j).getStack())) {
				handleMouseClick(inventorySlots.getSlot(j), j, 0, ClickType.PICKUP);
				handleMouseClick(inventorySlots.getSlot(i + space + 1), i + space + 1, 1, ClickType.PICKUP);
				handleMouseClick(inventorySlots.getSlot(j), j, 0, ClickType.PICKUP);
				break;
			}
		}
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(I18n.format("container.crafting", new Object[0]), 8, 5, 4210752);
		fontRenderer.drawString(I18n.format("container.inventory", new Object[0]), 8, ySize - 96 + 3,
				4210752);
		for (int i = 0; i < 12; i++) if (buttonsItem[i].stackToDraw != null && buttonsItem[i].isMouseOver())
			renderToolTip(buttonsItem[i].stackToDraw, mouseX - guiLeft, mouseY - guiTop);
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		// this.drawTab(0);
		// this.drawTab(1);
		x = guiLeft + 62;
		y = guiTop + 15;
		int k = y + 72;
		drawTexturedModalRect(x, y + (int) ((k - y - 17) * scroll), 232, 0, 12, 15);
		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.zLevel = 0;
		for (x = 0; x < 3; x++) for (y = 0; y < 3; y++) {
			ItemStack stack = itemsToRender.get(x + y * 3);
			if (stack != null && !stack.isEmpty()) itemRender.renderItemIntoGUI(stack, guiLeft + 86 + 18 * x, guiTop + 23 + 18 * y);
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		zLevel = 120;
		drawTexturedModalRect(85 + guiLeft, 22 + guiTop, 85, 22, 54, 54);
		zLevel = 0;
	}

}