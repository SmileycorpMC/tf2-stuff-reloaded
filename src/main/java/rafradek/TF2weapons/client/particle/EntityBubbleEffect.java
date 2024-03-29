package rafradek.TF2weapons.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBubbleEffect extends Particle {

	protected EntityBubbleEffect(World worldIn, double p_i1209_2_, double p_i1209_4_, double p_i1209_6_, double motionX,
                                 double motionY, double motionZ, int time) {
		super(worldIn, p_i1209_2_, p_i1209_4_, p_i1209_6_);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		// this.noClip=false;
		this.particleMaxAge = time;
		this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
		this.particleScale = 0.5f;
		this.particleMaxAge = 20;
		this.setParticleTextureIndex(32);
	}

	@Override
	public void renderParticle(BufferBuilder p_180434_1_, Entity p_180434_2_, float p_180434_3_, float p_180434_4_,
			float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
		super.renderParticle(p_180434_1_, p_180434_2_, p_180434_3_, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_,
				p_180434_8_);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.particleAlpha *= 0.9f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public static EntityBubbleEffect createNewEffect(World world, Vec3d pos, Vec3d motion) {
		return new EntityBubbleEffect(world, pos.x, pos.y, pos.z,
				motion.x, motion.y, motion.z, 5);
	}
}
