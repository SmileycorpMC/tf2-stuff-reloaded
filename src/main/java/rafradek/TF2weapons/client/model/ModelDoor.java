package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * ModelDoor - rafradek Created using Tabula 5.1.0
 */
public class ModelDoor extends ModelBase {
	public ModelRenderer shape1;

	public ModelDoor() {
		textureWidth = 64;
		textureHeight = 32;
		shape1 = new ModelRenderer(this, 0, 0);
		shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
		shape1.addBox(0.0F, 0.0F, 0.0F, 16, 16, 1, 0.0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(shape1.offsetX, shape1.offsetY, shape1.offsetZ);
		GlStateManager.translate(shape1.rotationPointX * f5, shape1.rotationPointY * f5,
				shape1.rotationPointZ * f5);
		GlStateManager.scale(1.0D, 1.0D, 0.5D);
		GlStateManager.translate(-shape1.offsetX, -shape1.offsetY, -shape1.offsetZ);
		GlStateManager.translate(-shape1.rotationPointX * f5, -shape1.rotationPointY * f5,
				-shape1.rotationPointZ * f5);
		shape1.render(f5);
		GlStateManager.popMatrix();
	}

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
