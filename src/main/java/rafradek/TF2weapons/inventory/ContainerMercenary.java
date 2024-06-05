package rafradek.TF2weapons.inventory;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.item.ItemAmmo;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.item.ItemWearable;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerMercenary extends ContainerMerchant {

	private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {
			EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };

	public EntityTF2Character mercenary;
	public int primaryAmmo;
	public int secondaryAmmo;

	public ContainerMercenary(EntityPlayer player, EntityTF2Character merc, World world) {
		super(player.inventory, merc, world);
		this.mercenary = merc;
		if (this.mercenary.hasHeldInventory()) {
			for (int i = 0; i < 4; i++) {
				if (!this.mercenary.loadoutHeld.getStackInSlot(i).isEmpty()) {
					ItemStack buf = this.mercenary.loadout.getStackInSlot(i);
					this.mercenary.loadout.setStackInSlot(i, this.mercenary.loadoutHeld.getStackInSlot(i));
					this.mercenary.loadoutHeld.setStackInSlot(i, buf);
				}
				/*
				 * if (!world.isRemote) TF2weapons.network.sendTo(new
				 * TF2Message.WearableChangeMessage(merc, i + 20,
				 * this.mercenary.loadout.getStackInSlot(i)), (EntityPlayerMP) player);
				 */
			}
			{
				int i = 3;
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					if (slot.getSlotType() == Type.ARMOR) {
						if (this.mercenary.loadoutHeld.getStackInSlot(4 + i).isEmpty() && !this.mercenary.isEmpty[i]) {
							this.mercenary.loadoutHeld.setStackInSlot(4 + i, this.mercenary.getItemStackFromSlot(slot));
							this.mercenary.setItemStackToSlot(slot, ItemStack.EMPTY);
						}
						i--;
					}
				}
			}
		}
		for (int k = 0; k < 4; ++k) {
			final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
			this.addSlotToContainer(new SlotItemHandler(
					merc.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.NORTH), 3 - k, 184,
					8 + k * 18) {
				/**
				 * Returns the maximum stack size for a given slot (usually the same as
				 * getInventoryStackLimit(), but 1 in the case of armor slots)
				 */
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				/**
				 * Check if the stack is a valid item for this slot. Always true beside for the
				 * armor slots.
				 */
				@Override
				@Nonnull
				public ItemStack getStack() {
					if (entityequipmentslot == EntityEquipmentSlot.HEAD && this.getItemHandler()
							.getStackInSlot(this.getSlotIndex()).getItem() instanceof ItemWearable)
						return ItemStack.EMPTY;

					return this.getItemHandler().getStackInSlot(this.getSlotIndex());
				}

				/*
				 * @Override public boolean canTakeStack(EntityPlayer player) { return
				 * merc.getDropChance(); }
				 */

				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					if (stack.isEmpty() || merc.isRobot()
							|| (merc.getOwner() != player && !player.capabilities.isCreativeMode))
						return false;
					else
						return stack.getItem().isValidArmor(stack, entityequipmentslot, merc);
				}

				@Override
				@Nullable
				@SideOnly(Side.CLIENT)
				public String getSlotTexture() {
					return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
				}
			});
		}
		for (int i = 0; i < 4; i++) {
			final int index = i;
			this.addSlotToContainer(
					new SlotItemHandler(merc.hasHeldInventory() ? merc.loadoutHeld : merc.loadout, i, 206, 8 + i * 18) {

						@Override
						public int getSlotStackLimit() {
							return 64;
						}

						@Override
						public boolean canTakeStack(EntityPlayer player) {
							return super.canTakeStack(player);
						}

						@Override
						public boolean isItemValid(@Nullable ItemStack stack) {
							if (stack.isEmpty() || (merc.getOwner() != player && !player.capabilities.isCreativeMode))
								return false;
							else {
								return TF2Util.isWeaponOfClass(stack, this.getSlotIndex(),
										merc.getTF2Class());
							}
						}

						/*
						 * @Override
						 * 
						 * @Nullable
						 * 
						 * @SideOnly(Side.CLIENT) public String getSlotTexture() { return
						 * TF2weapons.MOD_ID + ":items/weapon_empty_"+index; }
						 */
					});
		}

		this.addSlotToContainer(new SlotItemHandler(merc.refill, 0, 206, 93) {
			@Override
			public boolean isItemValid(@Nullable ItemStack stack) {
				if (stack.isEmpty()
						|| (!TF2Util.isOnSameTeam(merc.getOwner(), player) && !player.capabilities.isCreativeMode))
					return false;
				if (TF2Util.isOre("ingotLead", stack))
					return true;
				if (stack.getItem() instanceof ItemFood) {
					return true;
				}
				if (stack.getItem() instanceof ItemAmmo || stack.getItem() == Items.ARROW) {
					// int type = ((ItemAmmo)stack.getItem()).getTypeInt(stack);
					return true/*
								 * type ==
								 * ItemFromData.getData(merc.loadout.getStackInSlot(0)).getInt(PropertyType.
								 * AMMO_TYPE) || type ==
								 * ItemFromData.getData(merc.loadout.getStackInSlot(1)).getInt(PropertyType.
								 * AMMO_TYPE)
								 */;
				}
				return false;
			}

			/*
			 * @Override
			 * 
			 * @Nullable
			 * 
			 * @SideOnly(Side.CLIENT) public String getSlotTexture() { return
			 * TF2weapons.MOD_ID + ":items/refill_empty"; }
			 */
		});
		for (int i = 0; i < 4; i++) {
			final int index = i;
			this.addSlotToContainer(new SlotItemHandler(merc.loadout, i, -888888, -566788) {

				@Override
				public boolean canTakeStack(EntityPlayer player) {
					return false;
				}

				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					return false;
				}

				/*
				 * @Override
				 * 
				 * @Nullable
				 * 
				 * @SideOnly(Side.CLIENT) public String getSlotTexture() { return
				 * TF2weapons.MOD_ID + ":items/weapon_empty_"+index; }
				 */
			});
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (IContainerListener listener : this.listeners) {
			ItemStack weapon = this.mercenary.loadout.getStackInSlot(0);
			int loaded = weapon.getItem() instanceof ItemWeapon ? ItemWeapon.getWeapon(weapon).getClip(weapon) : 0;
			if (this.primaryAmmo != this.mercenary.getAmmo(0)) {
				this.primaryAmmo = this.mercenary.getAmmo(0);
				// System.out.println("ammo in:");

				listener.sendWindowProperty(this, 0, this.primaryAmmo);
			}
			weapon = this.mercenary.loadout.getStackInSlot(0);
			loaded = weapon.getItem() instanceof ItemWeapon ? ItemWeapon.getWeapon(weapon).getClip(weapon) : 0;
			if (this.secondaryAmmo != this.mercenary.getAmmo(1) + loaded) {
				this.secondaryAmmo = this.mercenary.getAmmo(1) + loaded;
				listener.sendWindowProperty(this, 1, this.secondaryAmmo);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data) {
		if (id == 0)
			this.primaryAmmo = data;
		else if (id == 1)
			this.secondaryAmmo = data;
	}

	@Override
	public boolean enchantItem(EntityPlayer player, int id) {
		if (id == 0 && !mercenary.isRobot()) {
			if (mercenary.getOwner() == player) {
				ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(TF2weapons.itemTF2, 1, 2));
				this.mercenary.setOwner(null);
			} else if (mercenary.getOwnerId() == null
					&& player.inventory.hasItemStack(new ItemStack(TF2weapons.itemTF2, 1, 2))) {
				player.inventory.clearMatchingItems(TF2weapons.itemTF2, 2, 1, null);
				this.mercenary.setOwner(player);
			}
			this.mercenary.applySpeed();
		} else if (id == 1 && !mercenary.isRobot()) {
			if (mercenary.getOwner() == player) {
				player.inventory.clearMatchingItems(TF2weapons.itemTF2, 2, 1, null);
				this.mercenary.setSharing(true);
			}
		} else if (id >= 10 && id < 20 && mercenary.getOwner() == player) {
			int index = (id + 100) % EntityTF2Character.Order.values().length;
			this.mercenary.setOrder(Order.values()[index]);
		} else if (id >= 50 && id < 53) {
			if (this.mercenary.getMainWeapon() == id - 50) {
				this.mercenary.setMainWeapon(-1);
			} else {
				this.mercenary.setMainWeapon(id - 50);
				this.mercenary.switchSlot(id - 50);
			}
		}
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!this.mercenary.world.isRemote) {
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				if (slot.getSlotType() == Type.ARMOR) {
					this.mercenary.setDropChance(slot,
							!this.mercenary.getItemStackFromSlot(slot).isEmpty() ? 2.0f : 0.25f);
				}
			}
			if (this.mercenary.hasHeldInventory()) {
				for (int i = 0; i < 4; i++) {
					if (!this.mercenary.loadoutHeld.getStackInSlot(i).isEmpty()) {
						ItemStack buf = this.mercenary.loadout.getStackInSlot(i);
						this.mercenary.loadout.setStackInSlot(i, this.mercenary.loadoutHeld.getStackInSlot(i));
						this.mercenary.loadoutHeld.setStackInSlot(i, buf);
					}
				}

				int i = 7;
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					if (slot.getSlotType() == Type.ARMOR) {
						if (this.mercenary.getItemStackFromSlot(slot)
								.isEmpty()/*
											 * && this.mercenary.loadoutHeld.getStackInSlot(3).getItem() instanceof
											 * ItemWearable
											 */) {
							this.mercenary.setItemStackToSlot(slot, this.mercenary.loadoutHeld.getStackInSlot(i));
							this.mercenary.loadoutHeld.setStackInSlot(i, ItemStack.EMPTY);
						}
						i--;
					}
				}
			}

			this.mercenary.switchSlot(this.mercenary.getMainWeapon() != -1 ? this.mercenary.getMainWeapon()
					: this.mercenary.preferredSlot, false, true);

		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			EntityEquipmentSlot equip = EntityLiving.getSlotForItemStack(itemstack);

			if (index == 2) {
				if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index != 0 && index != 1 && index < 39) {
				if (equip.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
					if (!this.getSlot(43 - equip.getSlotIndex()).isItemValid(itemstack1) || !this
							.mergeItemStack(itemstack1, 43 - equip.getSlotIndex(), 44 - equip.getSlotIndex(), false))
						return ItemStack.EMPTY;
				} else if (itemstack1.getItem() instanceof ItemUsable) {
					for (int i = 0; i < 4; i++) {
						if (!this.getSlot(i + 43).isItemValid(itemstack1)
								|| !this.mergeItemStack(itemstack1, i + 43, i + 44, false))
							return ItemStack.EMPTY;
					}
				} else if (this.getSlot(47).isItemValid(itemstack1)) {
					if (!this.mergeItemStack(itemstack1, 47, 48, false))
						return ItemStack.EMPTY;
				} else if (index >= 3 && index < 30) {
					if (!this.mergeItemStack(itemstack1, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= 30 && index < 39 && !this.mergeItemStack(itemstack1, 3, 30, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 3, 39, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}
}
