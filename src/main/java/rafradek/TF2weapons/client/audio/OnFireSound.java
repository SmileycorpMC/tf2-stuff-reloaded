package rafradek.TF2weapons.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;

public class OnFireSound extends MovingSound {

	public Entity target;

	public OnFireSound(SoundEvent soundIn, Entity target) {
		super(soundIn, target.getSoundCategory());
		xPosF = (float) target.posX;
		yPosF = (float) target.posY;
		zPosF = (float) target.posZ;
		volume = 0.6f;
		pitch = 1f;
		this.target = target;
		repeat = true;
	}

	@Override
	public void update() {
		xPosF = (float) target.posX;
		yPosF = (float) target.posY;
		zPosF = (float) target.posZ;
		if (target.ticksExisted - target.getEntityData().getInteger("LastHitBurn") > 5) donePlaying = true;
	}

}
