package rafradek.TF2weapons.item;

import net.minecraft.block.BlockFence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.util.PlayerPersistStorage;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

import javax.annotation.Nullable;

public class ItemPDA extends ItemFromData implements IItemSlotNumber, IItemOverlay, IItemNoSwitch {

	@SuppressWarnings("unchecked")
	private static final DataParameter<NBTTagCompound>[] VIEWS = new DataParameter[] { TF2PlayerCapability.SENTRY_VIEW,
			TF2PlayerCapability.DISPENSER_VIEW, TF2PlayerCapability.TELEPORTERA_VIEW,
			TF2PlayerCapability.TELEPORTERB_VIEW };
	private static final String[] GUI_BUILD_NAMES = new String[] { "gui.build.sentry", "gui.build.dispenser",
			"gui.build.entrance", "gui.build.exit", "gui.build.disposable" };

	public ItemPDA() {
		this.setMaxStackSize(1);
		this.addPropertyOverride(new ResourceLocation("building"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				if (!stack.hasTagCompound() || (stack.getTagCompound().getByte("Building") == 0)) {
					return 0f;
				} else {
					int building = stack.getTagCompound().getByte("Building");
					if (building == 1 || building == 5) {
						return 0.33f;
					} else if (building == 2) {
						return 0.66f;
					} else {
						return 1f;
					}
				}
			}
		});
	}

	@Override
	public boolean catchSlotHotkey(ItemStack stack, EntityPlayer player) {
		return ItemToken.allowUse(player, TF2Class.ENGINEER) && !stack.hasTagCompound()
				|| stack.getTagCompound().getByte("Building") == 0;
	}

	@Override
	public void onSlotSelection(ItemStack stack, EntityPlayer player, int slot) {
		if (!player.world.isRemote && TF2PlayerCapability.get(player).carrying == null && slot < 5) {
			if (slot == 4 && TF2PlayerCapability.get(player).calculateMaxSentries() <= 0)
				return;
			if (!PlayerPersistStorage.get(player).hasBuilding(slot)) {
				int metal = EntityBuilding.getCost(slot, TF2Util.getFirstItem(player.inventory,
						stackL -> (TF2Attribute.getModifier("Teleporter Cost", stackL, 1, player) != 1)));

				if (WeaponsCapability.get(player).hasMetal(metal)) {
					if (!stack.hasTagCompound())
						stack.setTagCompound(new NBTTagCompound());
					stack.getTagCompound().setByte("Building", (byte) (slot + 1));
				}
			} else {
				PlayerPersistStorage.get(player).buildings[slot] = null;
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		this.forceItemSlot(stack, worldIn, entityIn, itemSlot, isSelected);
		if (!worldIn.isRemote) {

			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			PlayerPersistStorage storage = PlayerPersistStorage.get(((EntityPlayer) entityIn));
			stack.getTagCompound().setBoolean("IsC", TF2PlayerCapability.get((EntityPlayer) entityIn).carrying != null);
			if (TF2PlayerCapability.get((EntityPlayer) entityIn).carrying != null) {
				stack.getTagCompound().setByte("Building",
						(byte) ((byte) TF2PlayerCapability.get((EntityPlayer) entityIn).carryingType + 1));
			} else if (stack.getTagCompound().getByte("Building") > 0) {
				int metal = EntityBuilding.getCost(stack.getTagCompound().getByte("Building") - 1, TF2Util.getFirstItem(
						((EntityPlayer) entityIn).inventory, stackL -> stackL.getItem() instanceof ItemWrench));
				if (!WeaponsCapability.get(entityIn).hasMetal(metal)
						|| storage.hasBuilding(stack.getTagCompound().getByte("Building") - 1))
					stack.getTagCompound().setByte("Building", (byte) 0);
			}

		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || oldStack.getItem() != newStack.getItem();

	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = playerIn.getHeldItem(hand);
		if (worldIn.isRemote) {
			if ((!stack.hasTagCompound() || stack.getTagCompound().getByte("Building") == 0))
				return EnumActionResult.PASS;
			else
				return EnumActionResult.SUCCESS;
		} else if (!playerIn.canPlayerEdit(pos.offset(facing), facing, stack) || !stack.hasTagCompound()
				|| stack.getTagCompound().getByte("Building") == 0)
			return EnumActionResult.PASS;
		else {
			IBlockState iblockstate = worldIn.getBlockState(pos);

			pos = pos.offset(facing);
			double d0 = 0.0D;

			if (facing == EnumFacing.UP && iblockstate.getBlock() instanceof BlockFence)
				d0 = 0.5D;

			boolean disposable = stack.getTagCompound().getByte("Building") == 5;

			int id = 16 + stack.getTagCompound().getByte("Building") * 2;
			if (stack.getTagCompound().getByte("Building") == 4)
				id -= 2;
			else if (disposable)
				id = 18;

			EntityBuilding entity = (EntityBuilding) ItemMonsterPlacerPlus.spawnCreature(playerIn, worldIn, id,
					pos.getX() + 0.5D, pos.getY() + d0, pos.getZ() + 0.5D, TF2PlayerCapability.get(playerIn).carrying);

			if (entity != null) {

				entity.setEntTeam(TF2Util.getTeamForDisplay(playerIn));
				entity.setOwner(playerIn);
				if (entity instanceof EntitySentry) {
					((EntitySentry) entity).attackRateMult = TF2Attribute.getModifier("Sentry Fire Rate", stack, 1,
							playerIn);
					TF2Util.addModifierSafe(entity, SharedMonsterAttributes.FOLLOW_RANGE, new AttributeModifier(
							"upgraderange", TF2Attribute.getModifier("Sentry Range", stack, 1f, entity) - 1f, 2), true);
					((EntitySentry) entity).setHeat((int) TF2Attribute.getModifier("Piercing", stack, 0, playerIn));
					if (disposable || !TF2Util
							.getFirstItem(playerIn.inventory,
									stackL -> stackL.getItem() instanceof ItemWrench
											&& TF2Attribute.getModifier("Weapon Mode", stackL, 0, playerIn) == 2)
							.isEmpty()) {
						((EntitySentry) entity).setMini(true);
						if (entity.getLevel() > 1)
							entity.onDeath(DamageSource.GENERIC);
					}
				}
				if (TF2PlayerCapability.get(playerIn).carrying != null) {
					entity.setConstructing(true);
					entity.redeploy = true;
				}
				TF2Util.addModifierSafe(entity, SharedMonsterAttributes.MAX_HEALTH,
						new AttributeModifier(EntityBuilding.UPGRADE_HEALTH_UUID, "upgradehealth",
								TF2Attribute.getModifier("Building Health", stack, 1f, entity) - 1f, 2),
						true);
				if (entity instanceof EntityDispenser) {
					((EntityDispenser) entity).setRange(TF2Attribute.getModifier("Dispenser Range", stack, 1, entity));
				}

				entity.rotationYaw = playerIn.rotationYawHead;
				entity.renderYawOffset = playerIn.rotationYawHead;
				entity.rotationYawHead = playerIn.rotationYawHead;
				entity.fromPDA = true;
				if (stack.getTagCompound().getByte("Building") == 5 && entity.getDisposableID() == -1)
					entity.setDisposableID(PlayerPersistStorage.get(playerIn).disposableBuildings.size());

				if (entity instanceof EntityTeleporter) {
					((EntityTeleporter) entity).setID(127);
					((EntityTeleporter) entity).setExit(stack.getTagCompound().getByte("Building") == 4);
				}
				PlayerPersistStorage.get(playerIn).setBuilding(entity,
						TF2PlayerCapability.get(playerIn).calculateMaxSentries());
				TF2PlayerCapability.get(playerIn).carrying = null;
				if (!playerIn.capabilities.isCreativeMode && TF2PlayerCapability.get(playerIn).carrying == null)
					WeaponsCapability.get(playerIn).consumeMetal(EntityBuilding.getCost(
							stack.getTagCompound().getByte("Building") - 1,
							TF2Util.getFirstItem(playerIn.inventory, stackL -> stackL.getItem() instanceof ItemWrench)),
							false);
			}

			stack.getTagCompound().setByte("Building", (byte) 0);
			return EnumActionResult.SUCCESS;
		}
	}

	@Override
	public boolean showInfoBox(ItemStack stack, EntityPlayer player) {
		return true;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, EntityPlayer player) {
		return new String[] { "METAL",
				Integer.toString(player.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal()) };
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer living, EnumHand hand) {
		if (living.isSneaking()) {
			living.getHeldItem(hand).getTagCompound().setBoolean("ShowHud",
					!living.getHeldItem(hand).getTagCompound().getBoolean("ShowHud"));
			System.out.println(living.getHeldItem(hand).getTagCompound().getBoolean("ShowHud"));
			return new ActionResult<>(EnumActionResult.SUCCESS, living.getHeldItem(hand));
		}
		return new ActionResult<>(EnumActionResult.FAIL, living.getHeldItem(hand));
	}

	@Override
	public boolean canSwitchTo(ItemStack stack) {
		return true;
	}

	@Override
	public void drawOverlay(ItemStack stack, EntityPlayer player, Tessellator tessellator, BufferBuilder buffer,
			ScaledResolution resolution) {
		if (!stack.hasTagCompound() || (stack.getTagCompound().getByte("Building") == 0)) {
			TF2PlayerCapability plcap = TF2PlayerCapability.get(player);
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.blueprintTexture);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
			GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
			boolean hasTag = stack.hasTagCompound();
			int buildCount = TF2Attribute.getModifier("Extra Sentry", stack, 0, player) > 0 ? 5 : 4;
			for (int i = 0; i < buildCount; i++) {
				int cost = EntityBuilding.getCost(i,
						TF2Util.getFirstItem(player.inventory, stackL -> stackL.getItem() instanceof ItemWrench));
				if (hasTag && i < 4 && plcap.dataManager.get(VIEWS[i]).getSize() != 0) {
					gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 140 + i * 72,
							resolution.getScaledHeight() / 2, 0, 64, 64, 64);
					gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 132 + i * 72,
							resolution.getScaledHeight() / 2 + 12, 208, 64 + i * 48, 48, 48);
				} else if (WeaponsCapability.get(player).getMetal() >= cost) {
					// gui.drawString(gui.getFontRenderer(),
					// gui.getFontRenderer().getStringWidth(Integer.toString(cost));
					gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 140 + i * 72,
							resolution.getScaledHeight() / 2, i == 4 ? 0 : i * 64, 0, 64, 64);
				}
				/*
				 * else gui.drawTexturedModalRect(resolution.getScaledWidth()/2-140 + i * 72,
				 * resolution.getScaledHeight()/2, 0, 0, 64, 64);
				 */

			}
			for (int i = 0; i < buildCount; i++) {
				int cost = EntityBuilding.getCost(i,
						TF2Util.getFirstItem(player.inventory, stackL -> stackL.getItem() instanceof ItemWrench));
				gui.drawString(gui.getFontRenderer(), Integer.toString(cost),
						resolution.getScaledWidth() / 2 - 72
								- gui.getFontRenderer().getStringWidth(Integer.toString(cost)) + i * 72,
						resolution.getScaledHeight() / 2 - 8, 0xFFFFFFFF);
				gui.drawCenteredString(gui.getFontRenderer(), "[" + (i + 1) + "]",
						resolution.getScaledWidth() / 2 - 108 + i * 72, resolution.getScaledHeight() / 2 + 72,
						0xFFFFFFFF);
				gui.drawString(gui.getFontRenderer(), I18n.format(GUI_BUILD_NAMES[i]),
						resolution.getScaledWidth() / 2 - 140 + i * 72, resolution.getScaledHeight() / 2 - 18,
						0xFFFFFFFF);
				if (WeaponsCapability.get(player).getMetal() < cost
						&& !(hasTag && i < 4 && plcap.dataManager.get(VIEWS[i]).getSize() != 0)) {
					gui.getFontRenderer().drawSplitString(I18n.format("gui.build.nometal"),
							resolution.getScaledWidth() / 2 - 140 + i * 72, resolution.getScaledHeight() / 2 + 20, 80,
							0xFFF00F0F);
				}

			}
			/*
			 * gui.drawTexturedModalRect(resolution.getScaledWidth()/2-68,
			 * resolution.getScaledHeight()/2-32, 64, 0, 64, 64);
			 * gui.drawTexturedModalRect(resolution.getScaledWidth()/2+4,
			 * resolution.getScaledHeight()/2-32, 128, 0, 64, 64);
			 * gui.drawTexturedModalRect(resolution.getScaledWidth()/2+72,
			 * resolution.getScaledHeight()/2-32, 192, 0, 64, 64);
			 * 
			 * gui.drawCenteredString(gui.getFontRenderer(), "[2]",
			 * resolution.getScaledWidth()/2-36, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); gui.drawCenteredString(gui.getFontRenderer(), "[3]",
			 * resolution.getScaledWidth()/2+36, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); gui.drawCenteredString(gui.getFontRenderer(), "[4]",
			 * resolution.getScaledWidth()/2+108, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); gui.drawCenteredString(gui.getFontRenderer(),
			 * I18n.format("gui.build.sentry"), resolution.getScaledWidth()/2-108,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * gui.drawCenteredString(gui.getFontRenderer(),
			 * I18n.format("gui.build.dispenser"), resolution.getScaledWidth()/2-36,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * gui.drawCenteredString(gui.getFontRenderer(),
			 * I18n.format("gui.build.entrance"), resolution.getScaledWidth()/2+36,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * gui.drawCenteredString(gui.getFontRenderer(), I18n.format("gui.build.exit"),
			 * resolution.getScaledWidth()/2+108, resolution.getScaledHeight()/2-40,
			 * 0xFFFFFFFF);
			 */
			gui.drawCenteredString(gui.getFontRenderer(), I18n.format("gui.build"), resolution.getScaledWidth() / 2,
					resolution.getScaledHeight() / 2 - 40, 0xFFFFFFFF);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}

	}

	@Override
	public boolean stopSlotSwitch(ItemStack stack, EntityLivingBase living) {
		return stack.getTagCompound().getBoolean("IsC");
	}

}
