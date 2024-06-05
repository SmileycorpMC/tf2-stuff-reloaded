package rafradek.TF2weapons.inventory;

import com.google.common.base.Predicates;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.crafting.TF2CraftingManager;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.WeaponData;

public class SlotCraftingTF2 extends SlotCrafting {

	private InventoryCrafting craftMatrix;
	private EntityPlayer player;

	public SlotCraftingTF2(EntityPlayer player, InventoryCrafting craftingInventory, IInventory inventoryIn,
			int slotIndex, int xPosition, int yPosition) {
		super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
		this.player = player;
		craftMatrix = craftingInventory;
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack) {
		if (player.world.isRemote) return stack;
		if (stack.getItem() == TF2weapons.itemTF2 && stack.getMetadata() == 9) {
			if (stack.hasTagCompound()) {
				TF2Class clazz = TF2Class.getClass(stack.getTagCompound().getByte("Token"));
				stack = ItemFromData.getRandomWeapon(player.getRNG(), Predicates.<WeaponData>and(
						ItemFromData.VISIBLE_WEAPON, test -> test.get(PropertyType.SLOT).containsKey(clazz.getName())));
			} else
				stack = ItemFromData.getRandomWeapon(player.getRNG(), ItemFromData.VISIBLE_WEAPON);
			// player.addStat(TF2Achievements.HOME_MADE);
			player.inventory.setItemStack(stack);
		} else if (stack.getItem() == TF2weapons.itemTF2 && stack.getMetadata() == 10) {
			stack = ItemFromData.getRandomWeaponOfType("cosmetic", player.getRNG(), false);
			player.inventory.setItemStack(stack);
		}
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Australium")) {
			// player.addStat(TF2Achievements.SHINY);
		}
		net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(player, stack, craftMatrix);
		this.onCrafting(stack);
		net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
		NonNullList<ItemStack> aitemstack = TF2CraftingManager.INSTANCE.getRemainingItems(this.craftMatrix,
				player.world);
		net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

		for (int i = 0; i < aitemstack.size(); ++i) {
			ItemStack itemstack = this.craftMatrix.getStackInSlot(i);
			ItemStack itemstack1 = aitemstack.get(i);

			if (!itemstack.isEmpty()) {
				this.craftMatrix.decrStackSize(i, 1);
				itemstack = this.craftMatrix.getStackInSlot(i);
			}

			if (!itemstack1.isEmpty())
				if (itemstack.isEmpty())
					this.craftMatrix.setInventorySlotContents(i, itemstack1);
				else if (ItemStack.areItemsEqual(itemstack, itemstack1)
						&& ItemStack.areItemStackTagsEqual(itemstack, itemstack1)) {
					itemstack1.grow(itemstack.getCount());
					this.craftMatrix.setInventorySlotContents(i, itemstack1);
				} else if (!this.player.inventory.addItemStackToInventory(itemstack1))
					this.player.dropItem(itemstack1, false);
		}
		return stack;
	}
}
