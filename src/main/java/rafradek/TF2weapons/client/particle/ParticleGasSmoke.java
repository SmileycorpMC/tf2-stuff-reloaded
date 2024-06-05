package rafradek.TF2weapons.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class ParticleGasSmoke extends Particle {

	public ParticleGasSmoke(World world, double p_i46352_2_, double p_i46352_4_, double p_i46352_6_, int color) {
		super(world, p_i46352_2_, p_i46352_4_, p_i46352_6_);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.particleScale *= this.rand.nextFloat() * 0.1F + 0.3F;
		this.setRBGColorF((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		this.particleScale = this.rand.nextFloat() * this.rand.nextFloat() * 6.0F + 1.0F;
		this.particleMaxAge = (int) (16.0D / (this.rand.nextFloat() * 0.8D + 0.2D)) + 2;
		// this.particleAlpha = Math.min(1 / this.particleScale * 3, 1);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}
	}

}
