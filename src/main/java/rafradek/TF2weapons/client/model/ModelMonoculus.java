package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * ModelMonoculus - rafradek Created using Tabula 5.1.0
 */
public class ModelMonoculus extends ModelBase {
	public ModelRenderer shape1;
	public ModelRenderer shape2;
	public ModelRenderer shape3;

	public ModelMonoculus() {
		textureWidth = 128;
		textureHeight = 128;
		shape2 = new ModelRenderer(this, 0, 0);
		shape2.setRotationPoint(0.0F, 8f, 0.0F);
		shape2.addBox(-13.0F, -16.0F, -13.0F, 26, 32, 26, 0.0F);
		shape3 = new ModelRenderer(this, 0, 64);
		shape3.setRotationPoint(0.0F, 8f, 0.0F);
		shape3.addBox(-13.0F, -13.0F, -16.0F, 26, 26, 32, 0.0F);
		shape1 = new ModelRenderer(this, 0, 0);
		shape1.setRotationPoint(0.0F, 8f, 0.0F);
		shape1.addBox(-16.0F, -13.0F, -13.0F, 32, 26, 26, 0.0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		shape2.render(f5);
		shape3.render(f5);
		shape1.render(f5);
	}

	/**
	 * This is a helper function from Tabula to set the rotation of model parts
	 */
	public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_,
			float p_78087_5_, float p_78087_6_, Entity entityIn) {
		// System.out.println("rotY: "+p_78087_5_);

		shape1.rotateAngleX = p_78087_5_ / (180F / (float) Math.PI);
		shape2.rotateAngleX = p_78087_5_ / (180F / (float) Math.PI);
		shape3.rotateAngleX = p_78087_5_ / (180F / (float) Math.PI);
	}
}
