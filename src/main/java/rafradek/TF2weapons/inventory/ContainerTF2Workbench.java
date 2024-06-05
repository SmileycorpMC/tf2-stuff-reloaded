package rafradek.TF2weapons.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeContainer;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemBuildingBox;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWrench;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.util.TF2Util;

import java.util.List;

public class ContainerTF2Workbench extends Container implements IRecipeContainer {
	/** The crafting matrix inventory (3x3). */
	public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
	public InventoryCraftResult craftResult = new InventoryCraftResult();
	private final World world;
	/** Position of the workbench */
	private final BlockPos pos;
	public EntityPlayer player;
	public int currentRecipe = -1;

	public ContainerTF2Workbench(EntityPlayer player, InventoryPlayer inventory, World world, BlockPos posIn) {
		this.player = player;
		this.world = world;
		this.pos = posIn;
		this.addSlotToContainer(
				new SlotCraftingTF2(inventory.player, this.craftMatrix, this.craftResult, 0, 148, 41));

		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
				this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 86 + j * 18, 23 + i * 18) {

				});

		/*
		 * for (int k = 0; k < 3; ++k) { for (int i1 = 0; i1 < 9; ++i1) {
		 * this.addSlotToContainer(new Slot(cabinet, i1 + k * 9 + 9, 8 + i1 * 18, 91 + k
		 * * 18)); } }
		 */

		for (int k = 0; k < 3; ++k)
			for (int i1 = 0; i1 < 9; ++i1)
				this.addSlotToContainer(new Slot(inventory, i1 + k * 9 + 9, 8 + i1 * 18, 98 + k * 18));

		for (int l = 0; l < 9; ++l)
			this.addSlotToContainer(new Slot(inventory, l, 8 + l * 18, 156));

		this.onCraftMatrixChanged(this.craftMatrix);
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		ItemStack stack = ItemStack.EMPTY;
		List<IRecipe> recipes = TF2CraftingManager.INSTANCE.getRecipeList();
		if (currentRecipe >= 0 && currentRecipe < recipes.size()
				&& recipes.get(currentRecipe).matches(this.craftMatrix, world))
			stack = getReplacement(player, recipes.get(currentRecipe).getCraftingResult(this.craftMatrix));
		// ?TF2CraftingManager.INSTANCE.getRecipeList().get(currentRecipe)TF2CraftingManager.INSTANCE.findMatchingRecipe(this.craftMatrix,
		// this.world);
		else
			stack = getReplacement(player,
					TF2CraftingManager.INSTANCE.findMatchingRecipe(this.craftMatrix, this.world, this.player));
		this.craftResult.setInventorySlotContents(0, stack);
	}

	public static ItemStack getReplacement(EntityPlayer player, ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof ItemBuildingBox
				&& player.getTeam() == player.world.getScoreboard().getTeam("BLU"))
			stack.setItemDamage(stack.getItemDamage() + 1);
		if (!stack.isEmpty() && stack.getItem() instanceof ItemBanner
				&& stack.getItemDamage() == EnumDyeColor.RED.getDyeDamage()) {
			NBTTagCompound pattern = new NBTTagCompound();
			if (player.getTeam() == player.world.getScoreboard().getTeam("BLU")) {
				stack.setItemDamage(EnumDyeColor.BLUE.getDyeDamage());
				pattern.setString("Pattern", "bb");
				pattern.setInteger("Color", 15);
			} else {
				pattern.setString("Pattern", "rb");
				pattern.setInteger("Color", 15);
			}

			stack.getSubCompound("BlockEntityTag").getTagList("Patterns", 10).appendTag(pattern);
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ItemBuildingBox) {
			if (stack.getItemDamage() / 2 == 11) {
				ItemStack wrench = TF2Util.getFirstItem(player.inventory,
						stackL -> TF2Attribute.getModifier("Teleporter Cost", stackL, 1, player) != 1);
				if (!wrench.isEmpty()) {
					stack.setCount((int) TF2Attribute.getModifier("Teleporter Cost", wrench, 1, player));
				}
			} else if (stack.getItemDamage() / 2 == 9) {
				ItemStack gunslinger = TF2Util.getFirstItem(player.inventory,
						stackL -> stackL.getItem() instanceof ItemWrench
								&& TF2Attribute.getModifier("Weapon Mode", stackL, 0, player) == 2);
				ItemStack bonusSentry = TF2Util.getFirstItem(player.inventory,
						stackL -> TF2Attribute.getModifier("Sentry Bonus", stackL, 1, player) != 1);
				if (!gunslinger.isEmpty()) {
					stack.setTagCompound(new NBTTagCompound());
					stack.getTagCompound().setBoolean("Mini", true);
					stack.setCount(2);
				}

				if (!bonusSentry.isEmpty()) {
					stack.setCount((int) TF2Attribute.getModifier("Sentry Bonus", bonusSentry, 1, player));
				}
			}
		}
		return stack;
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);

		if (!this.world.isRemote)
			for (int i = 0; i < 9; ++i) {
				ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);

				if (!itemstack.isEmpty())
					player.dropItem(itemstack, false);
			}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.world.getBlockState(this.pos).getBlock() != TF2weapons.blockCabinet ? false
				: player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
						this.pos.getZ() + 0.5D) <= 64.0D;
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();

			if (itemstack1.getItem() == TF2weapons.itemTF2 && itemstack1.getMetadata() == 9 &! player.world.isRemote)
				itemstack1 = ItemFromData.getRandomWeapon(itemstack1, player.getRNG());
			else if (itemstack1.getItem() == TF2weapons.itemTF2 && itemstack1.getMetadata() == 10 &! player.world.isRemote)
				itemstack1 = ItemFromData.getRandomWeaponOfType("cosmetic", player.getRNG(), false);
			itemstack = itemstack1.copy();

			if (index == 0) {
				if (!this.mergeItemStack(itemstack1, 10, 46, true))
					return ItemStack.EMPTY;

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 10 && index < 37) {
				if (!this.mergeItemStack(itemstack1, 37, 46, false))
					return ItemStack.EMPTY;
			} else if (index >= 37 && index < 46) {
				if (!this.mergeItemStack(itemstack1, 10, 37, false))
					return ItemStack.EMPTY;
			} else if (!this.mergeItemStack(itemstack1, 10, 46, false))
				return ItemStack.EMPTY;

			if (itemstack1.isEmpty())
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();

			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}

	/**
	 * Called to determine if the current slot is valid for the stack merging
	 * (double-click) code. The stack passed in is null for the initial slot that
	 * was double-clicked.
	 */
	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
	}

	@Override
	public boolean enchantItem(EntityPlayer player, int id) {
		this.currentRecipe = id;
		this.onCraftMatrixChanged(null);
		return true;
	}

	@Override
	public InventoryCraftResult getCraftResult() {
		return this.craftResult;
	}

	@Override
	public InventoryCrafting getCraftMatrix() {
		return this.craftMatrix;
	}
}
