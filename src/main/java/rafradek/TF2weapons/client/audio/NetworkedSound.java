package rafradek.TF2weapons.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class NetworkedSound extends MovingSound {

	private Entity parent;
	private boolean isStatic;
	private int id;

	public NetworkedSound(Entity parent, SoundEvent soundIn, SoundCategory categoryIn, float volume, float pitch,
			int id, boolean repeat) {
		super(soundIn, categoryIn);
		this.parent = parent;
		this.volume = volume;
		this.pitch = pitch;
		this.id = id;
		this.repeat = repeat;
	}

	public NetworkedSound(Vec3d pos, SoundEvent soundIn, SoundCategory categoryIn, float volume, float pitch, int id,
			boolean repeat) {
		super(soundIn, categoryIn);
		isStatic = true;
		this.volume = volume;
		this.pitch = pitch;
		this.id = id;
		this.repeat = repeat;
		xPosF = (float) pos.x;
		yPosF = (float) pos.y;
		zPosF = (float) pos.z;
	}

	@Override
	public void update() {
		if (isStatic) return;
		if (parent != null && !parent.isDead) {
			xPosF = (float) parent.posX;
			yPosF = (float) parent.posY;
			zPosF = (float) parent.posZ;
		} else donePlaying = true;
	}

}
