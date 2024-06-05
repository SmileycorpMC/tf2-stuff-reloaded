package rafradek.TF2weapons.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ParticleBulletHole extends Particle {

	RayTraceResult block;

	public ParticleBulletHole(World world, RayTraceResult result) {
		super(world, result.hitVec.x + result.sideHit.getFrontOffsetX() * 0.012,
				result.hitVec.y + result.sideHit.getFrontOffsetY() * 0.012,
				result.hitVec.z + result.sideHit.getFrontOffsetZ() * 0.012);
		this.block = result;
		this.particleRed = 0.05F;
		this.particleGreen = 0.05F;
		this.particleBlue = 0.05F;
		this.particleAlpha = 1f;
		this.particleMaxAge = 280;
		this.particleScale *= 0.9F;
		this.setSize(0.1F, 0.1F);
		this.setParticleTextureIndex(0);
	}

	@Override
	public void onUpdate() {
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		// this.move(this.motionX, this.motionY, this.motionZ);

		if (this.world.getBlockState(block.getBlockPos()).getBlock()
				.isAir(this.world.getBlockState(block.getBlockPos()), world, block.getBlockPos())) {
			this.setExpired();
		}

		if (this.particleMaxAge-- <= 0) {
			this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		// super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ,
		// rotationYZ, rotationXY, rotationXZ);
		// System.out.println("rot: "+rotationX+" "+rotationZ+" "+rotationYZ+"
		// "+rotationXY+" "+rotationXZ);
		EnumFacing face = this.block.sideHit;
		if (face == EnumFacing.UP)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 0, 0, 0, 1);
		else if (face == EnumFacing.DOWN)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 0, 0, 0, -1);
		else if (face == EnumFacing.NORTH)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 1, 0, 0, 0);
		else if (face == EnumFacing.SOUTH)
			super.renderParticle(buffer, entityIn, partialTicks, -1, 1, 0, 0, 0);
		else if (face == EnumFacing.EAST)
			super.renderParticle(buffer, entityIn, partialTicks, 0, 1, 1, 0, 0);
		else if (face == EnumFacing.WEST)
			super.renderParticle(buffer, entityIn, partialTicks, 0, 1, -1, 0, 0);
	}
}
