package rafradek.TF2weapons.client.audio;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class ReloadSound extends PositionedSound implements ITickableSound {

	public boolean done;

	public ReloadSound(SoundEvent soundResource, Entity entity) {
		super(soundResource, SoundCategory.NEUTRAL);
		xPosF = (float) entity.posX;
		yPosF = (float) entity.posY;
		zPosF = (float) entity.posZ;
		volume = 0.6f;
	}

	@Override
	public void update() {}

	@Override
	public boolean isDonePlaying() {
		return done;
	}

}
