package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import rafradek.TF2weapons.entity.building.EntityTeleporter;

/**
 * ModelTeleporter - rafradek Created using Tabula 5.1.0
 */
public class ModelTeleporter extends ModelBase {
	public ModelRenderer rotating;
	public ModelRenderer base1;
	public ModelRenderer base2;
	public ModelRenderer base3;
	public ModelRenderer rotating2;
	public ModelRenderer rotating3;

	public ModelTeleporter() {
		textureWidth = 64;
		textureHeight = 32;
		rotating3 = new ModelRenderer(this, 0, 0);
		rotating3.setRotationPoint(0.0F, 0.0F, 0.0F);
		rotating3.addBox(4.0F, -0.7F, -1F, 2, 1, 2, 0.0F);
		base1 = new ModelRenderer(this, 0, 15);
		base1.setRotationPoint(-3.5F, 22.4F, -2.5F);
		base1.addBox(0.0F, 0.0F, 0.0F, 7, 1, 5, 0.0F);
		base3 = new ModelRenderer(this, 0, 18);
		base3.setRotationPoint(2.1F, 23.0F, -4F);
		base3.addBox(0.0F, 0.0F, 0.0F, 1, 1, 8, 0.0F);
		rotating = new ModelRenderer(this, 0, 10);
		rotating.setRotationPoint(0.0F, 21.4F, 0.0F);
		rotating.addBox(-7.0F, 0.0F, -2F, 14, 1, 4, 0.0F);
		base2 = new ModelRenderer(this, 0, 18);
		base2.setRotationPoint(-3.1F, 23.0F, -4F);
		base2.addBox(0.0F, 0.0F, 0.0F, 1, 1, 8, 0.0F);
		rotating2 = new ModelRenderer(this, 0, 0);
		rotating2.setRotationPoint(0.0F, 0.0F, 0.0F);
		rotating2.addBox(-6.0F, -0.7F, -1.0F, 2, 1, 2, 0.0F);
		rotating.addChild(rotating3);
		rotating.addChild(rotating2);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		base1.render(f5);
		base3.render(f5);
		rotating.render(f5);
		base2.render(f5);
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
		rotating.rotateAngleY = ((EntityTeleporter) entityIn).spinRender;
	}
}
