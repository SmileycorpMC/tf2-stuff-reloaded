package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.lwjgl.opengl.GL11;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.building.EntityTeleporter.TeleporterData;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Class;
import rafradek.TF2weapons.util.TF2Util;

public class ItemWrench extends ItemMeleeWeapon implements IItemSlotNumber {

	/*
	 * @Override public void addInformation(ItemStack par1ItemStack, EntityPlayer
	 * par2EntityPlayer, List<String> par2List, boolean par4) {
	 * super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4);
	 * par2List.add("Metal: " + Integer.toString(200 -
	 * par1ItemStack.getItemDamage()) + "/200"); }
	 */

	@Override
	public boolean onHit(ItemStack stack, EntityLivingBase attacker, Entity target, float damage, int critical,
			boolean simulate) {
		// attacker.swingArm(EnumHand.MAIN_HAND);
		if (target instanceof EntityBuilding && TF2Util.isOnSameTeam(target, attacker)) {
			EntityBuilding building = (EntityBuilding) target;

			if (building.isSapped())
				building.removeSapper();
			else if (building.isConstructing()) {
				building.wrenchBonusTime = 25;
				building.wrenchBonusMult = TF2Attribute.getModifier("Construction Rate", stack, 1, attacker);
				building.playSound(ItemFromData.getSound(stack, PropertyType.BUILD_HIT_SUCCESS_SOUND), 1.7f, 1f);
			} else {
				boolean useIgnot = false;
				int metalLeft = attacker.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal();
				float metalMult = TF2Attribute.getModifier("Metal Used", stack, 1f, attacker);
				if ((attacker instanceof EntityPlayer && ((EntityPlayer) attacker).capabilities.isCreativeMode))
					metalMult = 10;

				ItemStack ingot = new ItemStack(Items.IRON_INGOT);
				if (metalLeft == 0 && attacker instanceof EntityPlayer
						&& ((EntityPlayer) attacker).inventory.hasItemStack(ingot)) {
					metalLeft = 50;
					attacker.getCapability(TF2weapons.WEAPONS_CAP, null).setMetal(50);
					useIgnot = true;
				}
				int metalUse = 0;

				if (building.getHealth() < building.getMaxHealth()) {
					metalUse = (int) Math
							.min((Math.min((building.getMaxHealth() - building.getHealth()) * 3.333333f, 33 * metalMult)
									+ 1), metalLeft);
					building.heal(metalUse * 0.3f);
					metalLeft -= metalUse;
				}

				if (building instanceof EntitySentry) {
					metalUse = Math
							.min(Math.min(((EntitySentry) building).getMaxAmmo() - ((EntitySentry) building).getAmmo(),
									(int) (40 * metalMult)), metalLeft);
					((EntitySentry) building).setAmmo(Math.min(((EntitySentry) building).getMaxAmmo(),
							((EntitySentry) building).getAmmo() + metalUse));
					metalLeft -= metalUse;
					if (building.getLevel() == 3) {
						metalUse = Math.min(
								Math.min(20 - ((EntitySentry) building).getRocketAmmo(), (int) (8 * metalMult)),
								metalLeft / 2);
						((EntitySentry) building)
								.setRocketAmmo(Math.min(20, ((EntitySentry) building).getRocketAmmo() + metalUse));
						metalLeft -= metalUse * 2;
					}
				}
				if (building.getLevel() < building.getMaxLevel()) {
					metalUse = Math.min(
							Math.min(200 - building.getProgress(),
									(int) TF2Attribute.getModifier("Upgrade Rate", stack, 25 * metalMult, attacker)),
							metalLeft);
					float teleUpgradeRate = building instanceof EntityTeleporter
							? TF2Attribute.getModifier("Teleporter Cost", stack, 1f, attacker)
							: 1;
					building.setProgress(Math.min(building.getProgress() + (int) (metalUse * teleUpgradeRate), 200));
					metalLeft -= metalUse;
					if (building.getProgress() >= 200)
						building.upgrade();
				}

				building.playSound(ItemFromData.getSound(stack,
						metalLeft != attacker.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal()
								? PropertyType.BUILD_HIT_SUCCESS_SOUND
								: PropertyType.BUILD_HIT_FAIL_SOUND),
						1.7f, 1f);
				// System.out.println("metal: "+TF2weapons.getMetal(attacker)+"
				// used: "+metalLeft);
				if (useIgnot && metalLeft != 50)
					((EntityPlayer) attacker).inventory.clearMatchingItems(Items.IRON_INGOT, 0, 1, null);

				if (!(attacker instanceof EntityPlayer && ((EntityPlayer) attacker).capabilities.isCreativeMode))
					attacker.getCapability(TF2weapons.WEAPONS_CAP, null).setMetal(metalLeft);
			}

			return false;
		}
		if (target instanceof EntityTF2Character && TF2Util.isOnSameTeam(target, attacker)
				&& ((EntityTF2Character) target).isRobot()) {
			EntityTF2Character robot = (EntityTF2Character) target;
			if (robot.getActivePotionEffect(TF2weapons.sapped) != null)
				robot.removeActivePotionEffect(TF2weapons.sapped);
			else {
				boolean useIgnot = false;
				int metalLeft = attacker.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal();
				float metalMult = TF2Attribute.getModifier("Metal Used", stack, 1f, attacker);
				if ((attacker instanceof EntityPlayer && ((EntityPlayer) attacker).capabilities.isCreativeMode))
					metalMult = 10;

				ItemStack ingot = new ItemStack(Items.IRON_INGOT);
				if (metalLeft == 0 && attacker instanceof EntityPlayer
						&& ((EntityPlayer) attacker).inventory.hasItemStack(ingot)) {
					metalLeft = 50;
					attacker.getCapability(TF2weapons.WEAPONS_CAP, null).setMetal(50);
					useIgnot = true;
				}
				int metalUse = 0;

				if (robot.getHealth() < robot.getMaxHealth()) {
					metalUse = (int) Math.min(
							(Math.min((robot.getMaxHealth() - robot.getHealth()) * 3.333333f, 33 * metalMult) + 1),
							metalLeft);
					robot.heal(metalUse * 0.3f);
					metalLeft -= metalUse;
				}

				if (useIgnot && metalLeft != 50)
					((EntityPlayer) attacker).inventory.clearMatchingItems(Items.IRON_INGOT, 0, 1, null);

				robot.playSound(ItemFromData.getSound(stack,
						metalLeft != attacker.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal()
								? PropertyType.BUILD_HIT_SUCCESS_SOUND
								: PropertyType.BUILD_HIT_FAIL_SOUND),
						1.7f, 1f);

				if (!(attacker instanceof EntityPlayer && ((EntityPlayer) attacker).capabilities.isCreativeMode))
					attacker.getCapability(TF2weapons.WEAPONS_CAP, null).setMetal(metalLeft);
			}
			return false;
		}
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer living, EnumHand hand) {
		ItemStack stack = living.getHeldItem(hand);
		if (living.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal() >= 20
				&& TF2Attribute.getModifier("Weapon Mode", stack, 0, living) == 1) {
			living.setActiveHand(hand);
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}
		return super.onItemRightClick(world, living, hand);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 800;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	/*
	 * public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack
	 * newStack, boolean slotChanged) { return
	 * super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged); }
	 */
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
	public boolean catchSlotHotkey(ItemStack stack, EntityPlayer player) {
		return ItemToken.allowUse(player, TF2Class.ENGINEER) && !player.getActiveItemStack().isEmpty()
				&& player.getItemInUseCount() < 770;
	}

	@Override
	public void onSlotSelection(ItemStack stack, EntityPlayer player, int slot) {
		if (!player.world.isRemote && player.getActiveItemStack().getItem() instanceof ItemWrench) {
			int dimension = 0;
			BlockPos pos = null;
			if (slot == 8) {
				dimension = player.dimension;
				pos = player.getBedLocation(player.dimension);
				if (pos == null) {
					pos = player.getBedLocation(0);
					dimension = 0;
				}
				if (pos != null)
					pos = EntityPlayer.getBedSpawnLocation(TF2weapons.server.getWorld(dimension), pos,
							player.isSpawnForced(dimension));
				else
					pos = TF2weapons.server.getWorld(0).provider.getRandomizedSpawnPoint();
			} else if (EntityTeleporter.teleporters.containsKey(player.getUniqueID())) {
				TeleporterData[] data = EntityTeleporter.teleporters.get(player.getUniqueID());
				if (data[slot] != null) {
					dimension = data[slot].dimension;
					pos = data[slot];
				}
			}
			if (pos != null) {
				if (dimension != player.dimension)
					player.world.getMinecraftServer().getPlayerList().transferPlayerToDimension((EntityPlayerMP) player,
							dimension, new EntityTeleporter.TeleporterDim((WorldServer) player.world, pos));
				player.setPositionAndUpdate(pos.getX() + 0.5, pos.getY() + 0.23, pos.getZ() + 0.5);
				player.getCooldownTracker().setCooldown(MapList.weaponClasses.get("wrench"), 200);
				TF2Util.playSound(player, TF2Sounds.MOB_TELEPORTER_SEND, 1.0F, 1.0F);
				player.resetActiveHand();
				player.getCapability(TF2weapons.WEAPONS_CAP, null).consumeMetal(20, false);
			}
		}
	}

	@Override
	public void drawOverlay(ItemStack stack, EntityPlayer player, Tessellator tessellator, BufferBuilder renderer,
			ScaledResolution resolution) {
		if (player.getActiveItemStack().getItem() instanceof ItemWrench && player.getItemInUseCount() < 770) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.buildingTexture);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
			GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
			Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 80,
					resolution.getScaledHeight() / 2 - 32, 64, 192, 64, 64);
			Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(resolution.getScaledWidth() / 2 + 16,
					resolution.getScaledHeight() / 2 - 32, 0, 192, 64, 64);

			gui.drawCenteredString(gui.getFontRenderer(), "(1-8)", resolution.getScaledWidth() / 2 - 48,
					resolution.getScaledHeight() / 2 + 40, 0xFFFFFFFF);
			gui.drawCenteredString(gui.getFontRenderer(), I18n.format("gui.selectlocation"),
					resolution.getScaledWidth() / 2, resolution.getScaledHeight() / 2 - 50, 0xFFFFFFFF);
			gui.drawCenteredString(gui.getFontRenderer(), "(9)", resolution.getScaledWidth() / 2 + 48,
					resolution.getScaledHeight() / 2 + 40, 0xFFFFFFFF);

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
