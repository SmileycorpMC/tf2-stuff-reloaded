package rafradek.TF2weapons.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemCleaver;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.tileentity.TileEntityUpgrades;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ContainerUpgrades extends Container {
	/** The crafting matrix inventory (3x3). */
	// public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
	public IInventory upgradedItem = new InventoryBasic("", false, 1);
	private final World world;
	/** Position of the workbench */
	private final BlockPos pos;
	public EntityPlayer player;
	public int currentRecipe = -1;
	public TileEntityUpgrades station;
	public ArrayList<TF2Attribute> applicable;// = new boolean[TileEntityUpgrades.UPGRADES_COUNT];
	public int playerCredits;
	public int[] transactions;// = new int[TileEntityUpgrades.UPGRADES_COUNT];
	public int[] transactionsCost;// = new int[TileEntityUpgrades.UPGRADES_COUNT];
	// public ArrayList<Integer> guiShowId = new ArrayList<>();

	public ContainerUpgrades(EntityPlayer player, InventoryPlayer playerInventory, TileEntityUpgrades station,
			World worldIn, BlockPos posIn) {
		this.station = station;
		this.player = player;
		this.world = worldIn;
		this.pos = posIn;
		this.applicable = new ArrayList<>();
		transactions = new int[0];
		transactionsCost = new int[0];
		this.addSlotToContainer(new Slot(upgradedItem, 0, 108, 8) {
			@Override
			public void onSlotChanged() {
				super.onSlotChanged();
				refreshData();
				transactions = new int[applicable.size()];
				transactionsCost = new int[applicable.size()];

			}

			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				if (stack.isEmpty())
					return false;
				else
					return stack.getItem() instanceof ItemFromData
							&& (stack.getMaxStackSize() == 1 || stack.getItem() instanceof ItemCleaver);
			}
		});

		/*
		 * for (int k = 0; k < 3; ++k) { for (int i1 = 0; i1 < 9; ++i1) {
		 * this.addSlotToContainer(new Slot(cabinet, i1 + k * 9 + 9, 8 + i1 * 18, 91 + k
		 * * 18)); } }
		 */

		for (int k = 0; k < 3; ++k)
			for (int i1 = 0; i1 < 9; ++i1)
				this.addSlotToContainer(new Slot(playerInventory, i1 + k * 9 + 9, 36 + i1 * 18, 143 + k * 18));

		for (int l = 0; l < 9; ++l)
			this.addSlotToContainer(new Slot(playerInventory, l, 36 + l * 18, 201));
	}

	public void refreshData() {
		this.applicable.clear();
		for (int i = 0; i < this.station.attributeList.size(); i++) {

			if (!this.upgradedItem.getStackInSlot(0).isEmpty() && station.attributeList.get(i) != null
					&& station.attributeList.get(i).canApply(this.upgradedItem.getStackInSlot(0)))
				this.applicable.add(station.attributeList.get(i));
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		int exp = TF2Util.getExperiencePoints(this.player);
		if (this.playerCredits != exp) {
			this.playerCredits = exp;
			for (IContainerListener listener : this.listeners) {
				listener.sendWindowProperty(this, 0, exp);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data) {
		if (id == 0)
			this.playerCredits = data;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);

		if (!this.world.isRemote) {
			ItemStack itemstack = this.upgradedItem.removeStackFromSlot(0);

			if (!itemstack.isEmpty())
				playerIn.dropItem(itemstack, false);
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return this.world.getBlockState(this.pos).getBlock() != TF2weapons.blockUpgradeStation ? false
				: playerIn.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
						this.pos.getZ() + 0.5D) <= 64.0D;
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	@Nullable
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 0) {
				if (!this.mergeItemStack(itemstack1, 1, 28, true))
					return ItemStack.EMPTY;

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 1 && index < 28) {
				if (!this.mergeItemStack(itemstack1, 0, 1, false))
					return ItemStack.EMPTY;
			} else if (!this.mergeItemStack(itemstack1, 1, 28, false))
				return ItemStack.EMPTY;

			if (itemstack1.isEmpty())
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();

			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean enchantItem(EntityPlayer playerIn, int id) {
		ItemStack stack = this.upgradedItem.getStackInSlot(0);

		NBTTagCompound stacktag = stack.getTagCompound();

		int expPoints = TF2Util.getExperiencePoints(playerIn);
		if (id >= 0) {
			int idEnch = Math.min(this.applicable.size() - 1, Math.max(id / 2, 0));
			boolean adding = id % 2 == 0;
			TF2Attribute attr = this.applicable.get(idEnch).getAttributeReplacement(stack);
			TF2Attribute attrorig = this.applicable.get(idEnch);
			if (attr == null || stack.isEmpty() || !attr.canApply(stack))
				return false;

			int cost = attr.getUpgradeCost(stack);

			int currLevel = attr.calculateCurrLevel(stack);

			int austrUpgrade = stacktag.hasKey(NBTLiterals.AUSTR_UPGRADED)
					? stacktag.getShort(NBTLiterals.AUSTR_UPGRADED)
					: -1;
			boolean austrAdd = currLevel == attr.numLevels && attr.austrUpgrade != 0f
					&& stacktag.getBoolean("Australium") && austrUpgrade != attr.id;

			if (adding && currLevel < this.station.attributes.get(attrorig) && expPoints >= cost
					&& cost + stacktag.getInteger("TotalSpent") <= TF2Attribute.getMaxExperience(stack, playerIn)) {
				NBTTagCompound tag = stacktag.getCompoundTag("Attributes");
				String key = String.valueOf(attr.id);

				if (!stacktag.hasKey("AttributesOrig")) {
					stacktag.setTag("AttributesOrig", stacktag.getCompoundTag("Attributes").copy());
				}

				if (!tag.hasKey(key))
					tag.setFloat(key, attr.defaultValue);
				tag.setFloat(key, tag.getFloat(key) + attr.getPerLevel(stack));

				stacktag.setInteger("TotalCost", stacktag.getInteger("TotalCost") + attr.cost);
				stacktag.setInteger("TotalSpent", stacktag.getInteger("TotalSpent") + cost);

				if (currLevel == attr.numLevels - 1 && attr.numLevels > 1)
					stacktag.setInteger("LastUpgradesCost", (int) (stacktag.getInteger("LastUpgradesCost")
							+ attr.cost * (attr.numLevels > 2 ? 3 : 1.5f)));

				// stacktag.setInteger("TotalCostReal", stacktag.getInteger("TotalCostReal") +
				// attr.cost);
				TF2Util.setExperiencePoints(playerIn, expPoints - cost);
				this.transactions[idEnch]++;
				this.transactionsCost[idEnch] += cost;

				/*
				 * playerIn.addStat(TF2Achievements.WEAPON_UPGRADE); if (attr.numLevels > 1 &&
				 * attr.calculateCurrLevel(stack) == attr.numLevels)
				 * playerIn.addStat(TF2Achievements.FULLY_UPGRADED);
				 */
			} else if (adding && austrAdd) {
				NBTTagCompound tag = stacktag.getCompoundTag("Attributes");
				String key = String.valueOf(attr.id);

				if (!stacktag.hasKey("AttributesOrig")) {
					stacktag.setTag("AttributesOrig", stacktag.getCompoundTag("Attributes").copy());
				}

				tag.setFloat(key, tag.getFloat(key) + attr.getPerLevel(stack) * attr.austrUpgrade);

				// this.transactions[idEnch]++;

				if (austrUpgrade != -1)
					tag.setFloat(String.valueOf(austrUpgrade),
							tag.getFloat(String.valueOf(austrUpgrade))
									- TF2Attribute.attributes[austrUpgrade].getPerLevel(stack)
											* TF2Attribute.attributes[austrUpgrade].austrUpgrade);
				stacktag.setShort(NBTLiterals.AUSTR_UPGRADED, (short) attr.id);
			} else if (!adding && this.transactions[idEnch] > 0) {
				cost = this.transactionsCost[idEnch];
				int count = this.transactions[idEnch];
				NBTTagCompound tag = stacktag.getCompoundTag("Attributes");
				String key = String.valueOf(attr.id);

				if (!tag.hasKey(key))
					return false;

				if (attr.id == austrUpgrade) {
					tag.setFloat(key, tag.getFloat(key) - attr.getPerLevel(stack) * attr.austrUpgrade);
					stacktag.removeTag(NBTLiterals.AUSTR_UPGRADED);
				}

				tag.setFloat(key, tag.getFloat(key) - attr.getPerLevel(stack) * count);
				if (tag.getFloat(key) == attr.defaultValue)
					tag.removeTag(key);

				stacktag.setInteger("TotalCost", stacktag.getInteger("TotalCost") - attr.cost * count);
				stacktag.setInteger("TotalSpent", stacktag.getInteger("TotalSpent") - cost);

				if (currLevel >= attr.numLevels && attr.numLevels > 1)
					stacktag.setInteger("LastUpgradesCost", (int) (stacktag.getInteger("LastUpgradesCost")
							- attr.cost * (attr.numLevels > 2 ? 3 : 1.5f)));
				// stacktag.setInteger("TotalCostReal", stacktag.getInteger("TotalCostReal") -
				// this.transactionsCost[idEnch]);
				TF2Util.setExperiencePoints(playerIn, expPoints + cost);
				this.transactions[idEnch] = 0;
				this.transactionsCost[idEnch] = 0;

			}
		} else if (id == -1) {
			if (stacktag.hasKey("AttributesOrig") && stacktag.getInteger("TotalSpent") > 0) {
				stacktag.setTag("Attributes", stacktag.getTag("AttributesOrig").copy());
				stacktag.setInteger("LastUpgradesCost", 0);
				TF2Util.setExperiencePoints(playerIn, expPoints + stacktag.getInteger("TotalSpent"));
				stacktag.setInteger("TotalSpent", 0);
				stacktag.setInteger("TotalCost", 0);
				for (int i = 0; i < this.transactions.length; i++) {
					this.transactions[i] = 0;
					this.transactionsCost[i] = 0;
				}
				stacktag.removeTag(NBTLiterals.AUSTR_UPGRADED);
			}
		}
		return true;
	}
}
