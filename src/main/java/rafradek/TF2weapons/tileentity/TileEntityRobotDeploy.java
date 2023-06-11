package rafradek.TF2weapons.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.block.BlockRobotDeploy;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character.Order;
import rafradek.TF2weapons.entity.mercenary.TF2CharacterAdditionalData;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemMoney;
import rafradek.TF2weapons.item.ItemRobotPart;
import rafradek.TF2weapons.item.ItemToken;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntityRobotDeploy extends TileEntity implements ITickable {

	public static final int[] NORMAL_REQUIRE = { 2, 2, 1 };
	public static final int[] GIANT_REQUIRE = { 4, 5, 3 };
	public static final int MONEY_NORMAL_REQUIRE = 20;
	public static final int MONEY_GIANT_REQUIRE = 100;

	boolean filled;
	private boolean joined;
	UUID owner;
	public ItemStackHandler weapon = new ItemStackHandler(9) {
		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			String name = TF2Util.getWeaponUsedByClass(stack);
			if (name == null || ItemFromData.getSlotForClass(ItemFromData.getData(stack), name) > 4
					|| ItemFromData.getSlotForClass(ItemFromData.getData(stack), name) == -1)
				return stack;
			else {
				return super.insertItem(slot, stack, simulate);
			}

		}

		@Override
		protected void onContentsChanged(int slot) {
			calculateHasInput();
		}
	};
	public ItemStackHandler parts = new ItemStackHandler(ItemRobotPart.LEVEL.length) {
		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (!(stack.getItem() instanceof ItemRobotPart) || stack.getItemDamage() != slot)
				return stack;
			else
				return super.insertItem(slot, stack, simulate);
		}

		@Override
		protected void onContentsChanged(int slot) {
			calculateHasInput();
		}
	};

	public ItemStackHandler money = new ItemStackHandler(3) {
		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (!(stack.getItem() instanceof ItemMoney) || stack.getItemDamage() != slot)
				return stack;
			else
				return super.insertItem(slot, stack, simulate);
		}

		@Override
		protected void onContentsChanged(int slot) {
			calculateHasInput();
		}
	};
	private String ownerName;
	public int progress;
	public int maxprogress;
	int hasWeapon;
	String weaponName;
	public int progressClient;
	public int classType;

	public int getRequirement(int level) {
		if (joined)
			return GIANT_REQUIRE[level];
		else
			return NORMAL_REQUIRE[level];
	}

	public int getCurrencyRequirement() {
		if (joined)
			return MONEY_GIANT_REQUIRE;
		else
			return MONEY_NORMAL_REQUIRE;
	}

	public boolean produceGiant() {
		return joined;
	}

	public void calculateHasInput() {
		hasWeapon = -1;
		weaponName = null;
		for (int i = 0; i < this.weapon.getSlots(); i++) {
			ItemStack stack = this.weapon.getStackInSlot(i);

			if (!stack.isEmpty()) {
				String name = TF2Util.getWeaponUsedByClass(stack);
				if (name != null) {
					hasWeapon = i;
					weaponName = name;
					break;
				}
			}
		}
		filled = true;
		for (int i = 0; i < this.parts.getSlots(); i++) {
			ItemStack stack = this.parts.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ItemRobotPart && stack.getItemDamage() == i
					&& stack.getCount() >= getRequirement(ItemRobotPart.LEVEL[i])) {
				// filled[i] = stack.getCount() == (ItemRobotPart.LEVEL[i] == 2 ? 1:2);
			} else {
				filled = false;
				break;
			}
		}
		int money = TF2Util.getTotalCurrency(this.money);

		if (money < this.getCurrencyRequirement()) {
			filled = false;
		}
	}

	@Override
	public void update() {
		if (!this.world.isRemote) {
			if (hasWeapon >= 0 && filled) {
				if (progress == 0) {
					this.maxprogress = this.joined ? 800 : 500;
				}
				if (progress % 20 == 0)
					this.world.notifyBlockUpdate(getPos(), world.getBlockState(pos), world.getBlockState(pos), 0);
				if (++progress >= this.maxprogress) {
					ItemStack stack = this.weapon.extractItem(hasWeapon, 64, false);
					String weaponName = TF2Util.getWeaponUsedByClass(stack);
					EnumFacing facing = this.getWorld().getBlockState(pos).getValue(BlockRobotDeploy.FACING);
					EntityTF2Character entity = (EntityTF2Character) EntityList
							.createEntityByIDFromName(new ResourceLocation(TF2weapons.MOD_ID, weaponName), this.world);
					BlockPos frontpos = this.pos.offset(facing);
					entity.setPosition(frontpos.getX(), frontpos.getY(), frontpos.getZ());
					TF2CharacterAdditionalData data = new TF2CharacterAdditionalData();
					data.team = 2;
					data.isGiant = this.joined;

					entity.robotStrength = 1f;
					entity.setSharing(true);
					entity.setOwnerID(this.ownerName, this.owner);
					entity.onInitialSpawn(this.world.getDifficultyForLocation(frontpos), data);
					entity.playLivingSound();
					for (int i = 0; i < entity.loadout.getSlots(); i++) {
						entity.loadout.setStackInSlot(i, ItemStack.EMPTY);
					}
					entity.loadout.setStackInSlot(ItemFromData.getSlotForClass(ItemFromData.getData(stack), entity),
							stack);
					entity.switchSlot(ItemFromData.getSlotForClass(ItemFromData.getData(stack), entity));
					entity.setOrder(Order.HOLD);
					entity.setMoneyDrop(this.getCurrencyRequirement());
					this.getWorld().spawnEntity(entity);
					for (int i = 0; i < this.parts.getSlots(); i++) {
						this.parts.extractItem(i, ItemRobotPart.LEVEL[i] == 2 ? 1 : 2, false);
					}
					TF2Util.setTotalCurrency(money, 0, TF2Util.getTotalCurrency(money) - this.getCurrencyRequirement());
					this.progress = 0;
					this.world.notifyBlockUpdate(getPos(), world.getBlockState(pos), world.getBlockState(pos), 0);
				}
			} else {
				if (this.progress > 0) {
					this.progress = 0;
					this.world.notifyBlockUpdate(getPos(), world.getBlockState(pos), world.getBlockState(pos), 0);
				}

			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (owner != null) {
			compound.setUniqueId("Owner", owner);
			compound.setString("OwnerName", ownerName);
		}
		compound.setTag("Weapons", this.weapon.serializeNBT());
		compound.setTag("Parts", this.parts.serializeNBT());
		compound.setTag("Money", this.money.serializeNBT());
		compound.setShort("Progress", (short) this.progress);
		compound.setShort("MaxProgress", (short) this.maxprogress);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);

		if (compound.hasUniqueId("Owner")) {
			this.owner = compound.getUniqueId("Owner");
			this.ownerName = compound.getString("OwnerName");
		} else {
			this.owner = null;
		}
		this.weapon.deserializeNBT(compound.getCompoundTag("Weapons"));
		this.parts.deserializeNBT(compound.getCompoundTag("Parts"));
		this.money.deserializeNBT(compound.getCompoundTag("Money"));
		this.progress = compound.getShort("Progress");
		this.maxprogress = compound.getShort("MaxProgress");
		this.calculateHasInput();
	}

	@Override
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		if (this.maxprogress > 0) {
			tag.setByte("P", (byte) ((float) this.progress / (float) this.maxprogress * 7f));
			if (this.progress > 0)
				tag.setByte("C", (byte) ItemToken
						.getClassID(TF2Util.getWeaponUsedByClass(this.weapon.extractItem(hasWeapon, 64, true))));
		}
		return new SPacketUpdateTileEntity(this.pos, 9999, tag);
	}

	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net,
			net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
		this.progressClient = pkt.getNbtCompound().getByte("P");
		this.classType = pkt.getNbtCompound().getByte("C");
	}

	@Override
	public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability,
			@Nullable net.minecraft.util.EnumFacing facing) {
		if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
			@Nullable net.minecraft.util.EnumFacing facing) {
		if (facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (facing.getAxis() == Axis.Y)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.weapon);
			else if (facing.getAxis() == Axis.X)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.parts);
			else
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.money);
		} else
			return super.getCapability(capability, facing);
	}

	public void setOwner(String name, UUID uniqueID) {
		this.owner = uniqueID;
		this.ownerName = name;
	}

	@Override
	public void onLoad() {
		this.joined = this.getWorld().getBlockState(pos).getValue(BlockRobotDeploy.JOINED);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		if (newSate.getBlock() == oldState.getBlock())
			this.joined = newSate.getValue(BlockRobotDeploy.JOINED);
		return oldState.getBlock() != newSate.getBlock() || !newSate.getValue(BlockRobotDeploy.HOLDER);
	}

	public void dropInventory() {
		for (int i = 0; i < this.parts.getSlots(); i++) {
			InventoryHelper.spawnItemStack(getWorld(), pos.getX(), pos.getY(), pos.getZ(),
					this.parts.extractItem(i, 64, false));
		}

		for (int i = 0; i < this.weapon.getSlots(); i++) {
			InventoryHelper.spawnItemStack(getWorld(), pos.getX(), pos.getY(), pos.getZ(),
					this.weapon.extractItem(i, 64, false));
		}

		for (int i = 0; i < this.money.getSlots(); i++) {
			InventoryHelper.spawnItemStack(getWorld(), pos.getX(), pos.getY(), pos.getZ(),
					this.money.extractItem(i, 64, false));
		}
	}

}
