package rafradek.TF2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.mercenary.EntitySniper;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2DamageSource;
import rafradek.TF2weapons.util.TF2Util;

import java.util.UUID;

public class ItemSniperRifle extends ItemBulletWeapon {
	public static UUID slowdownUUID = UUID.fromString("12843092-A5D6-BBCD-3D4F-A3DD4D8C65A9");
	public static AttributeModifier slowdown = new AttributeModifier(slowdownUUID, "sniper slowdown", -0.73D, 2);

	@Override
	public boolean canAltFire(World worldObj, EntityLivingBase player, ItemStack item) {
		return super.canAltFire(worldObj, player, item)
				&& player.getCapability(TF2weapons.WEAPONS_CAP, null).getPrimaryCooldown() <= 0;
	}

	@Override
	public boolean use(ItemStack stack, EntityLivingBase living, World world, EnumHand hand,
			PredictionMessage message) {
		if (!(living instanceof EntityTF2Character) || stack.getTagCompound().getBoolean("WaitProper")) {
			super.use(stack, living, world, hand, message);
			if (TF2Attribute.getModifier("Weapon Mode", stack, 0, living) != 2)
				this.disableZoom(stack, living);
			else
				living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks -= getChargeTime(stack, living) * 0.08f;

			stack.getTagCompound().setBoolean("WaitProper", false);

			if (message != null && (message.readData == null || message.readData.isEmpty()))
				living.getCapability(TF2weapons.PLAYER_CAP, null).headshotsRow = 0;

			return true;
		} else {
			stack.getTagCompound().setBoolean("WaitProper", true);
			this.altUse(stack, living, world);

			living.getCapability(TF2weapons.WEAPONS_CAP, null).setPrimaryCooldown(2500);
		}
		return false;
	}

	@Override
	public void altUse(ItemStack stack, EntityLivingBase living, World world) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (!cap.isCharging()) {
			cap.setCharging(true);
			if (living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(slowdownUUID) == null)
				living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(slowdown);
		} else
			this.disableZoom(stack, living);
		cap.setPrimaryCooldown(400);

	}

	@Override
	public boolean fireTick(ItemStack stack, EntityLivingBase living, World world) {
		if (!world.isRemote && living instanceof EntityPlayer && living.ticksExisted % 20 == 0
				&& TF2Attribute.getModifier("Weapon Mode", stack, 0, living) == 1
				&& !living.getCapability(TF2weapons.WEAPONS_CAP, null).isCharging()) {
			TF2Util.playSound(living, getSound(stack, PropertyType.NO_FIRE_SOUND), 0.7f, 1);
		}
		return super.fireTick(stack, living, world);
	}

	public void disableZoom(ItemStack stack, EntityLivingBase living) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
		cap.setCharging(false);
		living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);
	}

	@Override
	public boolean canHeadshot(EntityLivingBase living, ItemStack stack) {
		return (living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks > 4
				|| TF2Attribute.getModifier("Weapon Mode", stack, 0, living) == 2)
				&& (TF2Attribute.getModifier("No Headshot", stack, 0, living) == 0
						|| TF2Attribute.getModifier("Jarate Hit", stack, 0, living) != 0);
	}

	@Override
	public int getHeadshotCrit(EntityLivingBase living, ItemStack stack) {
		return TF2Attribute.getModifier("Jarate Hit", stack, 0, living) != 0 ? 1 : 2;
	}

	@Override
	public boolean showTracer(ItemStack stack) {
		return TF2Attribute.getModifier("Weapon Mode", stack, 0, null) >= 1;
	}

	@Override
	public boolean showSpecialTracer(ItemStack stack) {
		return TF2Attribute.getModifier("Weapon Mode", stack, 0, null) == 1;
	}

	@Override
	public float getWeaponDamage(ItemStack stack, EntityLivingBase living, Entity target) {
		return super.getWeaponDamage(stack, living, target) * (living != null ? this.getZoomBonus(stack, living)
				* (living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks >= getChargeTime(stack, living)
						? TF2Attribute.getModifier("Damage Charged", stack, 1, living)
						: 1)
				: 1);
	}

	@Override
	public float getWeaponMaxDamage(ItemStack stack, EntityLivingBase living) {
		return super.getWeaponMaxDamage(stack, living);
	}

	@Override
	public float getWeaponMinDamage(ItemStack stack, EntityLivingBase living) {
		return super.getWeaponMinDamage(stack, living);
	}

	public float getZoomBonus(ItemStack stack, EntityLivingBase living) {
		return 1 + Math.max(0,
				(living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks - getChargeStartTime(stack, living))
						/ ((getChargeTime(stack, living) - getChargeStartTime(stack, living)) / 2));
	}

	public static float getChargeTime(ItemStack stack, EntityLivingBase living) {
		return 66 / TF2Attribute.getModifier("Charge", stack, 1, living);
	}

	public static float getChargeStartTime(ItemStack stack, EntityLivingBase living) {
		return 26 / (TF2Attribute.getModifier("Charge", stack, 0.5f, living) + 0.5f);
	}

	@Override
	public short getAltFiringSpeed(ItemStack item, EntityLivingBase player) {
		return 400;
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);

		if (cap.reloadCool > 0 && par5 && cap.isCharging())
			this.disableZoom(par1ItemStack, (EntityLivingBase) par3Entity);

		if (cap.isCharging() && par5
				&& !(TF2Attribute.getModifier("Weapon Mode", par1ItemStack, 0, (EntityLivingBase) par3Entity) == 2
						&& cap.getPrimaryCooldown() > 0))
			if (cap.chargeTicks < getChargeTime(par1ItemStack, (EntityLivingBase) par3Entity))
				cap.chargeTicks += 1;
		// System.out.println("Charging: "+cap.chargeTicks);

		if (par3Entity instanceof EntitySniper && ((EntitySniper) par3Entity).getAttackTarget() != null
				&& par1ItemStack.getTagCompound().getBoolean("WaitProper"))
			if (((EntitySniper) par3Entity).getHealth() < 8 && cap.getPrimaryCooldown() > 250)
				par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null).setPrimaryCooldown(250);
	}

	/*
	 * public double getDiff(EntityTF2Character mob){
	 * if(mob.getAttackTarget()!=null){
	 * mob.attack.lookingAt(mob.getAttackTarget(),2) double
	 * mX=mob.getAttackTarget().posX-mob.getAttackTarget().lastTickPosX; double
	 * mY=mob.getAttackTarget().posY-mob.getAttackTarget().lastTickPosY; double
	 * mZ=mob.getAttackTarget().posZ-mob.getAttackTarget().lastTickPosZ; double
	 * totalMotion=Math.sqrt(mX*mX+mY*mY+mZ*mZ);
	 * System.out.println("Odskok: "+totalMotion); return totalMotion; } return 0; }
	 */
	@Override
	public void holster(WeaponsCapability cap, ItemStack stack, EntityLivingBase living, World world) {
		this.disableZoom(stack, living);
		super.holster(cap, stack, living, world);
	}

	@Override
	public boolean canFire(World worldObj, EntityLivingBase player, ItemStack item) {
		if (super.canFire(worldObj, player, item)) {
			if (player instanceof EntityPlayer && TF2Attribute.getModifier("Weapon Mode", item, 0, player) == 1
					&& !player.getCapability(TF2weapons.WEAPONS_CAP, null).isCharging()) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void onDealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, DamageSource source,
			float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);

		boolean headshot = ((TF2DamageSource) source).hasAttackFlag(TF2DamageSource.HEADSHOT);
		if (target instanceof EntityLivingBase && TF2Attribute.getModifier("Jarate Hit", stack, 0f, attacker) != 0f
				&& (headshot || WeaponsCapability.get(attacker).chargeTicks > 12)) {
			int time = (int) (TF2Attribute.getModifier("Jarate Hit", stack, 0f, attacker)
					* this.getZoomBonus(stack, attacker));
			if (headshot) {
				time -= 1;
			}
			((EntityLivingBase) target).addPotionEffect(new PotionEffect(TF2weapons.jarate, time * 20));
		}
		/*
		 * if(attacker instanceof EntityPlayerMP && target instanceof EntityLivingBase
		 * && !target.isEntityAlive() && TF2weapons.isEnemy(attacker, (EntityLivingBase)
		 * target)){ if(!attacker.getCapability(TF2weapons.WEAPONS_CAP, null).charging){
		 * ((EntityPlayerMP) attacker).addStat(TF2Achievements.KILLED_NOSCOPE);
		 * if(((EntityPlayerMP)
		 * attacker).getStatFile().readStat(TF2Achievements.KILLED_NOSCOPE)>=10)
		 * ((EntityPlayerMP) attacker).addStat(TF2Achievements.NO_SCOPE); }
		 * if(((TF2DamageSource)source).getCritical()==2){
		 * if(++attacker.getCapability(TF2weapons.PLAYER_CAP, null).headshotsRow>=3)
		 * ((EntityPlayerMP) attacker).addStat(TF2Achievements.EFFICIENT_SNIPER); } else
		 * attacker.getCapability(TF2weapons.PLAYER_CAP, null).headshotsRow=0; }
		 */
	}

	@Override
	public void doFireSound(ItemStack stack, EntityLivingBase living, World world, int critical) {
		if (ItemFromData.getData(stack).hasProperty(PropertyType.CHARGED_FIRE_SOUND)
				&& living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks >= getChargeTime(stack, living)) {
			SoundEvent soundToPlay = SoundEvent.REGISTRY.getObject(
					new ResourceLocation(ItemFromData.getData(stack).getString(PropertyType.CHARGED_FIRE_SOUND)
							+ (critical == 2 ? ".crit" : "")));
			living.playSound(soundToPlay, 4f, 1f);
			if (world.isRemote)
				ClientProxy.removeReloadSound(living);
		} else {
			super.doFireSound(stack, living, world, critical);
		}
	}

	@Override
	public void drawOverlay(ItemStack stack, EntityPlayer player, Tessellator tessellator, BufferBuilder renderer,
			ScaledResolution resolution) {
		// System.out.println("drawing");

		WeaponsCapability cap = WeaponsCapability.get(player);
		if (cap.isCharging()) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			// gui.drawTexturedModalRect(x,
			// y, textureSprite, widthIn, heightIn);
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.scopeTexture);
			double widthDrawStart = (double) (resolution.getScaledWidth() - resolution.getScaledHeight()) / 2;
			double widthDrawEnd = widthDrawStart + resolution.getScaledHeight();
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(widthDrawStart, resolution.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
			renderer.pos(widthDrawEnd, resolution.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
			renderer.pos(widthDrawEnd, 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
			renderer.pos(widthDrawStart, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
			tessellator.draw();
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.blackTexture);
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(0, resolution.getScaledHeight(), -90.0D).tex(0d, 1d).endVertex();
			renderer.pos(widthDrawStart, resolution.getScaledHeight(), -90.0D).tex(1d, 1d).endVertex();
			renderer.pos(widthDrawStart, 0.0D, -90.0D).tex(1d, 0d).endVertex();
			renderer.pos(0, 0.0D, -90.0D).tex(0d, 0d).endVertex();
			tessellator.draw();
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(widthDrawEnd, resolution.getScaledHeight(), -90.0D).tex(0d, 1d).endVertex();
			renderer.pos(resolution.getScaledWidth(), resolution.getScaledHeight(), -90.0D).tex(1d, 1d).endVertex();
			renderer.pos(resolution.getScaledWidth(), 0.0D, -90.0D).tex(1d, 0d).endVertex();
			renderer.pos(widthDrawEnd, 0.0D, -90.0D).tex(0d, 0d).endVertex();
			tessellator.draw();
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.chargeTexture);
			GlStateManager.color(0.5F, 0.5F, 0.5F, 0.7F);
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos((double) resolution.getScaledWidth() / 2 + 50, (double) resolution.getScaledHeight() / 2 + 15,
					-90.0D).tex(0d, 0.25d).endVertex();
			renderer.pos((double) resolution.getScaledWidth() / 2 + 100, (double) resolution.getScaledHeight() / 2 + 15,
					-90.0D).tex(0.508d, 0.25d).endVertex();
			renderer.pos((double) resolution.getScaledWidth() / 2 + 100, (double) resolution.getScaledHeight() / 2,
					-90.0D).tex(0.508d, 0d).endVertex();
			renderer.pos((double) resolution.getScaledWidth() / 2 + 50, (double) resolution.getScaledHeight() / 2,
					-90.0D).tex(0d, 0d).endVertex();
			tessellator.draw();
			if (cap.chargeTicks >= ItemSniperRifle.getChargeStartTime(stack, player)) {
				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos((double) resolution.getScaledWidth() / 2 + 110,
						(double) resolution.getScaledHeight() / 2 + 18, -90.0D).tex(0d, 0.57d).endVertex();
				renderer.pos((double) resolution.getScaledWidth() / 2 + 121,
						(double) resolution.getScaledHeight() / 2 + 18, -90.0D).tex(0.125d, 0.57d).endVertex();
				renderer.pos((double) resolution.getScaledWidth() / 2 + 121,
						(double) resolution.getScaledHeight() / 2 - 3, -90.0D).tex(0.125d, 0.25d).endVertex();
				renderer.pos((double) resolution.getScaledWidth() / 2 + 110,
						(double) resolution.getScaledHeight() / 2 - 3, -90.0D).tex(0d, 0.25d).endVertex();
				tessellator.draw();
			}
			double progress = cap.chargeTicks / ItemSniperRifle.getChargeTime(stack, player);
			GlStateManager.color(1F, 1F, 1F, 1F);
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos((double) resolution.getScaledWidth() / 2 + 50, (double) resolution.getScaledHeight() / 2 + 15,
					-90.0D).tex(0d, 0.25d).endVertex();
			renderer.pos((double) resolution.getScaledWidth() / 2 + 50 + progress * 50,
					(double) resolution.getScaledHeight() / 2 + 15, -90.0D).tex(progress * 0.508d, 0.25d).endVertex();
			renderer.pos((double) resolution.getScaledWidth() / 2 + 50 + progress * 50,
					(double) resolution.getScaledHeight() / 2, -90.0D).tex(progress * 0.508d, 0d).endVertex();
			renderer.pos((double) resolution.getScaledWidth() / 2 + 50, (double) resolution.getScaledHeight() / 2,
					-90.0D).tex(0d, 0d).endVertex();
			tessellator.draw();
			if (progress >= 1d) {
				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos((double) resolution.getScaledWidth() / 2 + 110,
						(double) resolution.getScaledHeight() / 2 + 18, -90.0D).tex(0d, 0.57d).endVertex();
				renderer.pos((double) resolution.getScaledWidth() / 2 + 121,
						(double) resolution.getScaledHeight() / 2 + 18, -90.0D).tex(0.125d, 0.57d).endVertex();
				renderer.pos((double) resolution.getScaledWidth() / 2 + 121,
						(double) resolution.getScaledHeight() / 2 - 3, -90.0D).tex(0.125d, 0.25d).endVertex();
				renderer.pos((double) resolution.getScaledWidth() / 2 + 110,
						(double) resolution.getScaledHeight() / 2 - 3, -90.0D).tex(0d, 0.25d).endVertex();
				tessellator.draw();
			}
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
		super.drawOverlay(stack, player, tessellator, renderer, resolution);
	}

	static {
		slowdown.setSaved(false);
	}
}
