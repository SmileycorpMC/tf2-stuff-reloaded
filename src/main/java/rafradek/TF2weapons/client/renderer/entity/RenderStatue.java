package rafradek.TF2weapons.client.renderer.entity;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.entity.EntityStatue;

import java.nio.FloatBuffer;

public class RenderStatue extends Render<EntityStatue> {

	private static final DynamicTexture TEXTURE_BRIGHTNESS = new DynamicTexture(16, 16);
	private FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);

	public RenderStatue(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityStatue entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// float health=entity.entity.getHealth();

		if (entity.entity == null)
			return;
		if (!entity.isFeign) {
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			this.brightnessBuffer.position(0);
			this.brightnessBuffer.put(1.0F);
			this.brightnessBuffer.put(0.8F);
			this.brightnessBuffer.put(0.0F);
			this.brightnessBuffer.put(0.3F);

			this.brightnessBuffer.flip();
			GlStateManager.glTexEnv(8960, 8705, this.brightnessBuffer);
			GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
			GlStateManager.enableTexture2D();
			GlStateManager.bindTexture(TEXTURE_BRIGHTNESS.getGlTextureId());
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

			GlStateManager.color(1.0f, 0.8f, 0f);
		} else {
			entity.entity.setInvisible(false);
			WeaponsCapability.get(entity.entity).setInvisible(false);
			// entity.entity.setHealth(0f);
			entity.entity.limbSwingAmount = 0;
			entity.entity.renderYawOffset = entity.renderYawOffset;
			entity.entity.deathTime = entity.ticksExisted;
		}
		// GlStateManager.rotate(23f, 1, 0.5f, 0);
		// entity.entity.renderYawOffset=entity.entity.getRNG().nextFloat()*180f;
		renderManager.getEntityRenderObject(entity.entity).doRender(entity.entity, x, y, z, entityYaw,
				entity.isFeign ? partialTicks : 1);
		if (!entity.isFeign) {
			GlStateManager.color(1.0f, 1f, 1f);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			GlStateManager.enableTexture2D();
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_ALPHA, 770);
			GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
			GlStateManager.disableTexture2D();
			GlStateManager.bindTexture(0);
			GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
			GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
			GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		} else {
			entity.entity.setInvisible(true);
			// entity.entity.setHealth(health);
			WeaponsCapability.get(entity.entity).setInvisible(true);
			entity.entity.deathTime = 0;
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityStatue entity) {
		return null;
	}

}
