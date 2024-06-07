package rafradek.TF2weapons.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.*;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class GuiPages extends GuiScreen {

	private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/cabinet.png");

	private int guiLeft;
	private int guiTop;
	public GuiButtonToggleItem[] buttonsItem;
	public NonNullList<ItemStack> itemsToRender;
	public int firstIndex;
	public float scroll;
	public int selectedIndex = -1;

	private boolean wasClicking;
	private boolean isScrolling;

	@Override
	public void initGui() {
		super.initGui();
		guiLeft = width / 2 - 146;
		guiTop = height / 2 - 120;
		buttonsItem = new GuiButtonToggleItem[16 * 6];
		itemsToRender = NonNullList.<ItemStack>create();
		TF2weapons.tabweapontf2.displayAllRelevantItems(itemsToRender);
		TF2weapons.tabutilitytf2.displayAllRelevantItems(itemsToRender);
		TF2weapons.tabsurvivaltf2.displayAllRelevantItems(itemsToRender);
		for (int x = 0; x < 16; x++) for (int y = 0; y < 6; y++) if (x + y * 16 < itemsToRender.size()) {
					GuiButtonToggleItem button = buttonsItem[x + y * 16] = new GuiButtonToggleItem(x + y * 16,
							guiLeft + 4 + x * 18, guiTop + 34 + y * 18, 18, 18);
					buttonList.add(button);
				}
		buttonList.add(new GuiButton(96, guiLeft + 200, guiTop + 14, "Back"));
		// this.buttonList.get(96).visible=false;
		setButtons();

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		if (selectedIndex == -1) {
			boolean flag = Mouse.isButtonDown(0);
			int i = guiLeft;
			int j = guiTop;
			int k = i + 292;
			int l = j + 34;
			int i1 = k + 14;
			int j1 = l + 108;
			if (!wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) isScrolling = true;
			if (!flag) isScrolling = false;
			wasClicking = flag;
			if (isScrolling) {
				scroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
				scroll = MathHelper.clamp(scroll, 0.0F, 1.0F);
				int rows = -(-itemsToRender.size() / 16) - 5;
				firstIndex = Math.round(scroll * rows) * 16;
				setButtons();
			}
			mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
			k = guiLeft + 293;
			l = guiTop + 35;
			i = l + 108;
			drawTexturedModalRect(k, l + (int) ((i - l - 17) * scroll), 232, 0, 12, 15);
		} else displayItem(itemsToRender.get(selectedIndex));
		super.drawScreen(mouseX, mouseY, partialTicks);
		for (int i = 0; i < 16 * 6; i++) if (buttonsItem[i].stackToDraw != null && buttonsItem[i].isMouseOver() && buttonsItem[i].visible)
				drawHoveringText(buttonsItem[i].stackToDraw.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL), mouseX, mouseY);
	}

	public void setButtons() {
		for (int i = 0; i < 6 * 16; i++)
			// System.out.println("Buttons: "+buttonsItem[i]+" "+firstIndex);
			if (selectedIndex == -1 && i + firstIndex < itemsToRender.size()) {
				buttonsItem[i].stackToDraw = itemsToRender.get(i + firstIndex);
				buttonsItem[i].selected = i + firstIndex == selectedIndex;
			} else {
				buttonsItem[i].stackToDraw = ItemStack.EMPTY;
				buttonsItem[i].selected = false;
			}
		buttonList.get(96).visible = true;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id < 96) selectedIndex = button.id + firstIndex;
			// this.displayItem(((GuiButtonToggleItem)button).stackToDraw);
		if (button.id == 96) selectedIndex = -1;
		setButtons();
	}

	public void displayItem(ItemStack stack) {
		ArrayList<String> list = new ArrayList<>();
		list.add(stack.getDisplayName());
		stack.getItem().addInformation(stack, Minecraft.getMinecraft().world, list, ITooltipFlag.TooltipFlags.NORMAL);
		for (int i = 0; i < list.size(); i++) fontRenderer.drawStringWithShadow(list.get(i), guiLeft + 70, guiTop + 34 + 10 * i, 0xFFFFFF);
		int yStart = guiTop + 36 + Math.max(64, list.size() * 10);
		this.drawHorizontalLine(guiLeft + 4, guiLeft + 292, yStart, 0xFFFFFF);
		list.clear();
		list.add("Tips:");
		if (stack.getItem() instanceof ItemSniperRifle) {
			list.add("Press RMB to Zoom in and charge");
			list.add("Charged shots deal increased damage");
			list.add("When scoped, headshots deal critical damage");
		} else if (stack.getItem() instanceof ItemStickyLauncher) {
			list.add("Hold LMB to shoot farther");
			list.add("Hold RMB to detonate bombs");
			list.add("Jump and detonate a bomb below your feet to perform a sticky jump");
		} else if (stack.getItem() instanceof ItemMinigun) {
			list.add("Hold RMB to spin up the minigun");
			list.add("Minigun slows down the user when used");
		} else if (stack.getItem() instanceof ItemMedigun) {
			list.add("Hold LMB to heal allies and build ubercharge");
			list.add("Press RMB to use the ubercharge");
			list.add("Ubercharge grants invulnerability for 8 second");
		}
		DecimalFormat format = new DecimalFormat("#.##");
		format.setRoundingMode(RoundingMode.HALF_UP);
		if (stack.getItem() instanceof ItemWeapon) {
			boolean fast = ((ItemWeapon) stack.getItem()).getFiringSpeed(stack, null) <= 250;
			String nameS;
			if (fast) nameS = "Damage / Second: (Base, Crit, Mini-Crit";
			else nameS = "Damage: (Base, Crit, Mini-Crit";
			String damageS;
			float damage = ((ItemWeapon) stack.getItem()).getWeaponDamage(stack, null, null)
					* (fast ? 1000f / ((ItemWeapon) stack.getItem()).getFiringSpeed(stack, null) : 1f);
			int pellets = ((ItemWeapon) stack.getItem()).getWeaponPelletCount(stack, null);
			if (pellets > 1) damageS = format.format(damage * pellets) + " (" + damage + "*" + pellets + ")";
			else damageS = format.format(damage);
			damageS = damageS + ", " + format.format(damage * pellets * 3) + ", "
					+ format.format(damage * pellets * 1.35) + "-" + format.format(
							damage * pellets * ((ItemWeapon) stack.getItem()).getWeaponMaxDamage(stack, null) * 1.35);
			if (((ItemWeapon) stack.getItem()).getWeaponMaxDamage(stack, null) != 1) {
				float maxDmg = ((ItemWeapon) stack.getItem()).getWeaponMaxDamage(stack, null);
				nameS = nameS + ", Point blank";
				damageS = damageS + ", " + format.format(damage * ((maxDmg + 1) / 2) * ((pellets + 0.5f) / 1.5f)) + "-"
						+ format.format(damage * maxDmg * pellets);
			}
			if (stack.getItem() instanceof ItemSniperRifle) {
				nameS = nameS + ", Charged";
				damageS = damageS + ", "
						+ format.format(TF2Attribute.getModifier("Damage Charged", stack, damage * pellets * 3, null));
			}
			if (((ItemWeapon) stack.getItem()).getWeaponMinDamage(stack, null) != 1) {
				nameS = nameS + ", Far";
				float minDmg = ((ItemWeapon) stack.getItem()).getWeaponMinDamage(stack, null);
				damageS = damageS + ", " + format.format(damage * minDmg) + "-"
						+ format.format(damage * ((minDmg + 1) / 2) * ((pellets + 3f) / 4f));
			}
			list.add(nameS + ")");
			list.add(damageS);
			list.add("");
			list.add("Function times:");
			list.add("Attack interval: " + format.format(((ItemWeapon) stack.getItem()).getFiringSpeed(stack, null) * 0.001f) + "s");
			if (((ItemWeapon) stack.getItem()).hasClip(stack)) {
				if (!((ItemWeapon) stack.getItem()).IsReloadingFullClip(stack)) list.add("Reload time (first): "
						+ format.format(((ItemWeapon) stack.getItem()).getWeaponFirstReloadTime(stack, null) * 0.001f) + "s");
				list.add("Reload time: " + format.format(((ItemWeapon) stack.getItem()).getWeaponReloadTime(stack, null) * 0.001f) + "s");
			}
		}
		for (int i = 0; i < list.size(); i++) fontRenderer.drawStringWithShadow(list.get(i), guiLeft + 4, yStart + 14 + i * 10, 0xFFFFFF);
		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.scale(4, 4, 4);
		mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (guiLeft + 4) / 4, (guiTop + 36) / 4);
		mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, guiLeft + 4, guiTop + 36);
		GlStateManager.scale(0.25, 0.25, 0.25);
		GlStateManager.disableLighting();
		RenderHelper.disableStandardItemLighting();
	}
}
