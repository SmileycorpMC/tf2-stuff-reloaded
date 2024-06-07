package rafradek.TF2weapons.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import rafradek.TF2weapons.entity.building.EntityBuilding;

public class BuildingSound extends MovingSound {

	public EntityBuilding sentry;
	private int state;

	public BuildingSound(EntityBuilding sentry, SoundEvent location, int state) {
		super(location, SoundCategory.NEUTRAL);
		this.sentry = sentry;
		volume = 0.65f;
		repeat = true;
		this.state = state;
	}

	@Override
	public void update() {
		xPosF = (float) sentry.posX;
		yPosF = (float) sentry.posY;
		zPosF = (float) sentry.posZ;
		if (sentry.getHealth() <= 0 || sentry.isDead) stopPlaying();
	}

	public void stopPlaying() {
		donePlaying = true;
	}

}
