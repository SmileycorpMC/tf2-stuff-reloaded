package rafradek.TF2weapons.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.model.ModelBomb;
import rafradek.TF2weapons.client.model.ModelGrenade;
import rafradek.TF2weapons.client.model.ModelStickybomb;
import rafradek.TF2weapons.entity.projectile.EntityGrenade;
import rafradek.TF2weapons.util.TF2Util;

public class RenderGrenade extends Render<EntityGrenade> {

	// if you want a model, be sure to add it here:
	private ModelBase model;
	private ModelBase modelBomb;
	private ModelStickybomb modelBurst;
	private static final ResourceLocation TEXTURE_RED = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/projectile/grenadered.png");
	private static final ResourceLocation TEXTURE_BLU = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/projectile/grenadeblu.png");
	public static final ResourceLocation TEXTURE_BOMB = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/projectile/bomb.png");

	public RenderGrenade(RenderManager manager) {
		super(manager);
		// we could have initialized it above, but here is fine as well:
		model = new ModelGrenade();
		modelBomb = new ModelBomb();
		modelBurst = new ModelStickybomb();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGrenade entity) {

		return entity.getBomb() > 0 || entity.isBurst() ? TEXTURE_BOMB
				: TF2Util.getTeamForDisplay(entity.shootingEntity) == 0 ? TEXTURE_RED : TEXTURE_BLU;
	}

	@Override
	public void doRender(EntityGrenade entity, double x, double y, double z, float yaw, float partialTick) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y + entity.height / 2, (float) z);
		GlStateManager.color(0.7F, 0.7F, 0.7F, 1F);
		GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick, 0.0F, 1.0F,
				0.0F);
		GL11.glRotatef((entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick)
				* (entity.getBomb() > 0 ? 1 : -1), 1.0F, 0.0F, 0.0F);
		if (entity.getBomb() == 1)
			GL11.glScalef(2, 2, 2);
		if (entity.isBurst())
			GL11.glScalef(0.6f, 0.6f, 0.6f);
		bindEntityTexture(entity);

		ModelBase model = this.model;
		if (entity.isBurst()) {
			model = this.modelBurst;
		} else if (entity.getBomb() > 0) {
			model = this.modelBomb;
		}
		// GL11.glTranslatef((float)entity.posX, (float)entity.posY,
		// entity.posZ);
		if (entity.getCritical() == 2) {
			// GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			// GlStateManager.color(0.0F, 0.0F, 1.0F, 1F);
			model.render(entity, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			// GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
			// GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
		} else
			model.render(entity, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);

		/*
		 * GL11.glScalef(1.5f, 1.5f, 1.5f); GL11.glEnable(GL11.GL_BLEND);
		 * //GL11.glDisable(GL11.GL_ALPHA_TEST); OpenGlHelper.glBlendFunc(770, 771, 1,
		 * 0);
		 *
		 * char c0 = 61680; int j = c0 % 65536; int k = c0 / 65536;
		 * OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j
		 * / 1.0F, (float)k / 1.0F);
		 *
		 * model.render(entity, 0F, 0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		 * GL11.glDisable(GL11.GL_BLEND); // GL11.glEnable(GL11.GL_ALPHA_TEST);
		 */
		GlStateManager.color(1F, 1F, 1F, 1F);
		GL11.glPopMatrix();
		/*
		 * IIcon iicon = TF2EventBusListener.pelletIcon;
		 *
		 * if (iicon != null) { GL11.glPushMatrix(); GL11.glTranslatef((float)x,(float)
		 * y,(float) z); GL11.glEnable(GL12.GL_RESCALE_NORMAL); GL11.glScalef(0.5F,
		 * 0.5F, 0.5F); this.bindTexture(TextureMap.locationItemsTexture); Tessellator
		 * tessellator = Tessellator.instance;
		 *
		 * float f = iicon.getMinU(); float f1 = iicon.getMaxU(); float f2 =
		 * iicon.getMinV(); float f3 = iicon.getMaxV(); float f4 = 1.0F; float f5 =
		 * 0.5F; float f6 = 0.25F; GL11.glRotatef(180.0F -
		 * this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		 * GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		 * tessellator.startDrawingQuads(); tessellator.setNormal(0.0F, 1.0F, 0.0F);
		 * tessellator.addVertexWithUV((double)(0.0F - f5), (double)(0.0F - f6), 0.0D,
		 * (double)f, (double)f3); tessellator.addVertexWithUV((double)(f4 - f5),
		 * (double)(0.0F - f6), 0.0D, (double)f1, (double)f3);
		 * tessellator.addVertexWithUV((double)(f4 - f5), (double)(f4 - f6), 0.0D,
		 * (double)f1, (double)f2); tessellator.addVertexWithUV((double)(0.0F - f5),
		 * (double)(f4 - f6), 0.0D, (double)f, (double)f2); tessellator.draw();
		 * GL11.glDisable(GL12.GL_RESCALE_NORMAL); GL11.glPopMatrix();
		 *
		 * }
		 */

	}

}
