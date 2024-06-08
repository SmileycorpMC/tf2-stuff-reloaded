package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * ModelJar - Undefined Created using Tabula 5.1.0
 */
public class ModelJar extends ModelBase {
	public ModelRenderer shape1;
	public ModelRenderer shape2;

	public ModelJar() {
		textureWidth = 32;
		textureHeight = 16;
		shape1 = new ModelRenderer(this, 0, 0);
		shape1.setRotationPoint(-1.5F, -2.5F, -1.5F);
		shape1.addBox(0.0F, 0.0F, 0.0F, 3, 5, 3, 0.0F);
		shape2 = new ModelRenderer(this, 0, 8);
		shape2.setRotationPoint(-1.0F, -3.0F, -1.0F);
		shape2.addBox(0.0F, 0.0F, 0.0F, 2, 1, 2, 0.0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		shape1.render(f5);
		shape2.render(f5);
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
