package rafradek.TF2weapons.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.client.TF2EventsClient;
import rafradek.TF2weapons.util.TF2Util;

public class EntityBulletTracer extends Particle {

	private float duration;
	private boolean nextDead;
	private float length;

	private boolean motionless;

	public EntityBulletTracer(World par1World, double startX, double startY, double startZ, double x, double y,
			double z, int speed, int color, float length) {
		super(par1World, startX, startY, startZ);
		this.particleScale = 0.2f;
		// this.special = special;
		double dist = new Vec3d(startX, startY, startZ).distanceTo(new Vec3d(x, y, z));
		if (speed > 0) {
			this.duration = (float) dist / speed;
			this.motionX = (x - startX) / duration;
			this.motionY = (y - startY) / duration;
			this.motionZ = (z - startZ) / duration;
			this.length = length;

		} else {
			this.motionless = true;
			this.duration = length;
			this.length = (float) dist * 1000;
			this.motionX = (x - startX) / dist * 0.001;
			this.motionY = (y - startY) / dist * 0.001;
			this.motionZ = (z - startZ) / dist * 0.001;
		}
		/*
		 * if (type == 1) { crits=2; this.motionX *= 0.001; this.motionY *= 0.001;
		 * this.motionZ *= 0.001; }
		 */
		this.particleMaxAge = 200;
		this.setSize(0.025f, 0.025f);
		// this.setParticleIcon(Item.itemsList[2498+256].getIconFromDamage(0));
		this.setParticleTexture(TF2EventsClient.pelletIcon[0]);
		// this.setParticleTextureIndex(81);
		this.multipleParticleScaleBy(2);

		if (color == 0)
			this.setRBGColorF(0.97f, 0.76f, 0.51f);
		else
			this.setRBGColorF((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
	}

	public EntityBulletTracer(World par1World, double startX, double startY, double startZ, double x, double y,
			double z, int duration, int crits, EntityLivingBase shooter, int type, float length) {
		this(par1World, startX, startY, startZ, x, y, z, duration, 0, length);
		if (crits != 2)
			this.setRBGColorF(0.97f, 0.76f, 0.51f);
		else {
			int color = TF2Util.getTeamColor(shooter);
			this.setRBGColorF(MathHelper.clamp((color >> 16) / 255f, 0.2f, 1f),
					MathHelper.clamp((color >> 8 & 255) / 255f, 0.2f, 1f),
					MathHelper.clamp((color & 255) / 255f, 0.2f, 1f));
		}
	}

	@Override
	public void onUpdate() {
		if (nextDead)
			this.setExpired();
		nextDead = true;
		// this.setVelocity(0, 0, 0);
		super.onUpdate();
		this.motionX *= 1.025D;
		this.motionY *= 1.025D;
		this.motionZ *= 1.025D;
		if (duration > 0) {
			duration--;
			if (duration <= 0)
				this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
		float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
		float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
		Vec3d rightVec = new Vec3d(this.motionX, this.motionY, this.motionZ)
				.crossProduct(Minecraft.getMinecraft().getRenderViewEntity().getLook(1)).normalize();
		// System.out.println(rightVec);
		float f4 = 0.1F * this.particleScale;

		int i = this.getBrightnessForRender(partialTicks);
		int j = i >> 16 & 65535;
		int k = i & 65535;

		float xNext;
		float yNext;
		float zNext;

		if (motionless) {
			xNext = (float) (x + this.motionX * length);
			yNext = (float) (y + this.motionY * length);
			zNext = (float) (z + this.motionZ * length);
		} else {
			float length = 2 * this.length;
			if (this.duration < 1)
				length *= this.duration - (int) this.duration;
			xNext = (float) (x + this.motionX * length);
			yNext = (float) (y + this.motionY * length);
			zNext = (float) (z + this.motionZ * length);
		}

		float xMin = this.particleTexture.getMinU();
		float xMax = this.particleTexture.getMaxU();
		float yMin = this.particleTexture.getMinV();
		float yMax = this.particleTexture.getMaxV();

		worldRendererIn.pos(x - rightVec.x * f4, y - rightVec.y * f4, z - rightVec.z * f4).tex(xMax, yMax)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
				.endVertex();
		;
		worldRendererIn.pos(x + rightVec.x * f4, y + rightVec.y * f4, z + rightVec.z * f4).tex(xMax, yMin)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
				.endVertex();
		;
		worldRendererIn.pos(xNext + rightVec.x * f4, yNext + rightVec.y * f4, zNext + rightVec.z * f4).tex(xMin, yMin)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
				.endVertex();
		;
		worldRendererIn.pos(xNext - rightVec.x * f4, yNext - rightVec.y * f4, zNext - rightVec.z * f4).tex(xMin, yMax)
				.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k)
				.endVertex();
		;

		/*
		 * worldRendererIn.pos((double)(x + rightVec.x * f4), (double)(y+rightVec.y*f4),
		 * (double)(z+rightVec.z*f4)).tex((double)xMax,
		 * (double)yMax).color(this.particleRed, this.particleGreen, this.particleBlue,
		 * this.particleAlpha).lightmap(j, k).endVertex();;
		 * worldRendererIn.pos((double)(x - rightVec.x * f4), (double)(y-rightVec.y*f4),
		 * (double)(z-rightVec.z*f4)).tex((double)xMax,
		 * (double)yMin).color(this.particleRed, this.particleGreen, this.particleBlue,
		 * this.particleAlpha).lightmap(j, k).endVertex();;
		 * worldRendererIn.pos((double)(xNext - rightVec.x * f4),
		 * (double)(yNext-rightVec.y*f4),
		 * (double)(zNext-rightVec.z*f4)).tex((double)xMin,
		 * (double)yMin).color(this.particleRed, this.particleGreen, this.particleBlue,
		 * this.particleAlpha).lightmap(j, k).endVertex();;
		 * worldRendererIn.pos((double)(xNext + rightVec.x * f4 ),
		 * (double)(yNext+rightVec.y*f4),
		 * (double)(zNext+rightVec.z*f4)).tex((double)xMin,
		 * (double)yMin).color(this.particleRed, this.particleGreen, this.particleBlue,
		 * this.particleAlpha).lightmap(j, k).endVertex();;
		 */
		// System.out.println("Rotation X: "+rotationX+" Rotation Z:
		// "+rotationZ+" Rotation YZ: "+rotationYZ+" Rotation XY: "+rotationXY+"
		// rotation XZ: "+rotationXZ);
		// super.renderParticle(worldRendererIn, entityIn, partialTicks,
		// rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
		this.resetPositionToBB();
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		return 15728880;
	}

	public float getBrightness(float p_70013_1_) {
		return 1.0F;
	}

	public static class Factory implements IParticleFactory {

		@Override
		public Particle createParticle(int particleID, World world, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new EntityBulletTracer(world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn,
					p_178902_15_[0], p_178902_15_[1], (p_178902_15_[2] / 64f));
		}

	}
}
