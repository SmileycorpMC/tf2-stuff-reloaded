package rafradek.TF2weapons.lightsource;

import atomicstryker.dynamiclights.client.IDynamicLightSource;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Optional;

;

@Optional.Interface(iface = "atomicstryker.dynamiclights.client.IDynamicLightSource", modid = "dynamiclights", striprefs = true)
public class MuzzleFlashLightSource implements IDynamicLightSource {

	public int ticksLeft = 4;
	public boolean over;
	public Entity shooter;

	public MuzzleFlashLightSource(Entity entity) {
		shooter = entity;
	}

	@Override
	public Entity getAttachmentEntity() {
		return shooter;
	}

	@Override
	public int getLightLevel() {
		return 13;
	}

	public void update() {
		if (--ticksLeft <= 0)
			over = true;
	}
}
