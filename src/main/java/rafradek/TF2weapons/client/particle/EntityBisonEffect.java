package rafradek.TF2weapons.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.client.TF2EventsClient;

public class EntityBisonEffect extends Particle {

	public EntityBisonEffect(World world, double p_i46352_2_, double p_i46352_4_, double p_i46352_6_, int color) {
		super(world, p_i46352_2_, p_i46352_4_, p_i46352_6_);
		this.setParticleTexture(TF2EventsClient.bisonIcon);
		this.setPosition(this.posX,
				this.posY, this.posZ);
		this.particleScale *= this.rand.nextFloat() * 0.1F + 0.3F;
		this.setRBGColorF((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		//this.particleAlpha = Math.min(1 / this.particleScale * 3, 1);
		this.particleMaxAge = 15;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.particleAge < 5)
			this.particleScale += 0.2F;
		else
			this.particleScale -=0.12F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public float getBrightness(float p_70013_1_) {
		return 1.0F;
	}
	
	public int getFXLayer()
    {
        return 1;
    }
}
