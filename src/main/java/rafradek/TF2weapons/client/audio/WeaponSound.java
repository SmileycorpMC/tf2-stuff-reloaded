package rafradek.TF2weapons.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.WeaponData;

public class WeaponSound extends MovingSound {
	public EntityLivingBase entity;
	public int type;
	public WeaponData conf;
	public boolean endsnextTick;
	public WeaponSound playOnEnd;

	public WeaponSound(SoundEvent sound, EntityLivingBase entity, int type, WeaponData conf) {
		super(sound, SoundCategory.NEUTRAL);
		this.type = type;
		this.entity = entity;
		this.conf = conf;
		volume = entity instanceof EntityPlayer ? TF2ConfigVars.gunVolume : TF2ConfigVars.mercenaryVolume;
	}

	@Override
	public void update() {
		if (endsnextTick) setDone();
		xPosF = (float) entity.posX;
		yPosF = (float) entity.posY;
		zPosF = (float) entity.posZ;
		if (/*
			 * !(entity instanceof EntityPlayer &&
			 * ((EntityPlayer)entity).inventory.currentItem != slot)
			 */ItemFromData.getData(entity.getHeldItem(EnumHand.MAIN_HAND)) != conf || entity.isDead) setDone();
	}

	public void setDone() {
		ClientProxy.fireSounds.remove(entity);
		if (playOnEnd != null) {
			Minecraft.getMinecraft().getSoundHandler().playSound(playOnEnd);
			ClientProxy.fireSounds.put(entity, playOnEnd);
		}
		donePlaying = true;
	}
}
