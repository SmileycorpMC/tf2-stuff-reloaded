package rafradek.TF2weapons.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityCritEffect extends Particle {

	public EntityCritEffect(World world, double p_i46352_2_, double p_i46352_4_, double p_i46352_6_, int color) {
		super(world, p_i46352_2_, p_i46352_4_, p_i46352_6_);
		this.setParticleTextureIndex(65);
		this.setPosition(this.posX + this.rand.nextFloat() * 0.15f - 0.075f,
				this.posY + this.rand.nextFloat() * 0.15f - 0.075f, this.posZ + this.rand.nextFloat() * 0.15f - 0.075f);
		this.particleScale *= this.rand.nextFloat() * 0.5F + 0.5F;
		this.setRBGColorF((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
		this.motionY += 0.03;
		// this.particleAlpha = Math.min(1 / this.particleScale * 3, 1);
		this.particleMaxAge = 20;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.particleScale -= 0.05F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public float getBrightness(float p_70013_1_) {
		return 1.0F;
	}
}
