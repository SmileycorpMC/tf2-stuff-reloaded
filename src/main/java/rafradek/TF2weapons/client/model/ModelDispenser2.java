package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * ModelDispenser2 - Undefined Created using Tabula 5.1.0
 */
public class ModelDispenser2 extends ModelBase {
	public ModelRenderer shapemed2;
	public ModelRenderer shape1;
	public ModelRenderer shape2;
	public ModelRenderer shape3;
	public ModelRenderer shape4;
	public ModelRenderer shape5;
	public ModelRenderer shape1_1;
	public ModelRenderer shape4_1;
	public ModelRenderer shape3_1;
	public ModelRenderer shape2_1;
	public ModelRenderer shape5_1;
	public ModelRenderer shapemain;
	public ModelRenderer shapemed;
	public ModelRenderer shapemed_1;
	public ModelRenderer shapeammo;
	public ModelRenderer shapelvl22;
	public ModelRenderer shapelvl21;
	public ModelRenderer shape5_2;
	public ModelRenderer shape5_3;

	public ModelDispenser2() {
		textureWidth = 64;
		textureHeight = 32;
		shapemed = new ModelRenderer(this, 32, 18);
		shapemed.setRotationPoint(-6.5F, 19.3F, 0.0F);
		shapemed.addBox(-1.0F, -5.0F, -1.5F, 3, 5, 3, -0.2F);
		shapeammo = new ModelRenderer(this, 56, 0);
		shapeammo.setRotationPoint(4.2F, 10.0F, -1.5F);
		shapeammo.addBox(0.0F, 0.0F, 0.0F, 1, 9, 3, 0.0F);
		shapelvl21 = new ModelRenderer(this, 8, 20);
		shapelvl21.setRotationPoint(0.5F, 8.2F, -3.0F);
		shapelvl21.addBox(-4.5F, 7.0F, -1.2F, 8, 3, 2, 0.2F);
		shapelvl22 = new ModelRenderer(this, 8, 20);
		shapelvl22.setRotationPoint(0.5F, 8.2F, 3.5F);
		shapelvl22.addBox(-4.5F, 7.0F, -1.2F, 8, 3, 2, 0.2F);
		shapemain = new ModelRenderer(this, 0, 0);
		shapemain.setRotationPoint(0.0F, 16.0F, 0.0F);
		shapemain.addBox(-4.5F, -8.0F, -2.0F, 9, 16, 4, 0.0F);
		shape2 = new ModelRenderer(this, 32, 3);
		shape2.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape2.addBox(-4.5F, 7.0F, -2.7F, 9, 9, 1, -0.1F);
		shape4_1 = new ModelRenderer(this, 52, 0);
		shape4_1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape4_1.addBox(3.5F, 1.9F, 1.5F, 1, 5, 1, -0.1F);
		shape1_1 = new ModelRenderer(this, 32, 0);
		shape1_1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape1_1.addBox(-4.5F, 0.0F, -3.0F, 9, 2, 1, 0.0F);
		shape2_1 = new ModelRenderer(this, 32, 3);
		shape2_1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape2_1.addBox(-4.5F, 7.0F, 1.7F, 9, 9, 1, -0.1F);
		shapemed2 = new ModelRenderer(this, 44, 18);
		shapemed2.setRotationPoint(-6.5F, 13.0F, -0.5F);
		shapemed2.addBox(0.0F, 0.0F, 0.0F, 1, 9, 1, 0.0F);
		shape5_1 = new ModelRenderer(this, 32, 13);
		shape5_1.setRotationPoint(0.0F, 21.9F, 3.0F);
		shape5_1.addBox(-4.5F, -2.0F, -0.5F, 9, 4, 1, 0.0F);
		setRotateAngle(shape5_1, -0.47123889803846897F, 0.0F, 0.0F);
		shape4 = new ModelRenderer(this, 52, 0);
		shape4.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape4.addBox(3.5F, 1.9F, -2.5F, 1, 5, 1, -0.1F);
		shape1 = new ModelRenderer(this, 32, 0);
		shape1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape1.addBox(-4.5F, 0.0F, 2.0F, 9, 2, 1, 0.0F);
		shape3 = new ModelRenderer(this, 52, 0);
		shape3.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape3.addBox(-4.5F, 1.9F, -2.5F, 1, 5, 1, -0.1F);
		shape5_2 = new ModelRenderer(this, 32, 13);
		shape5_2.setRotationPoint(0.5F, 19.3F, 2.4F);
		shape5_2.addBox(-4.5F, -2.0F, -0.5F, 8, 4, 1, 0.3F);
		setRotateAngle(shape5_2, -0.9162978572970231F, 0.0F, 0.0F);
		shape5_3 = new ModelRenderer(this, 32, 13);
		shape5_3.setRotationPoint(0.5F, 19.3F, -2.4F);
		shape5_3.addBox(-4.5F, -2.0F, -0.5F, 8, 4, 1, 0.3F);
		setRotateAngle(shape5_3, 0.9162978572970231F, 0.0F, 0.0F);
		shape5 = new ModelRenderer(this, 32, 13);
		shape5.setRotationPoint(0.0F, 21.9F, -3.0F);
		shape5.addBox(-4.5F, -2.0F, -0.5F, 9, 4, 1, 0.0F);
		setRotateAngle(shape5, 0.47123889803846897F, 0.0F, 0.0F);
		shape3_1 = new ModelRenderer(this, 52, 0);
		shape3_1.setRotationPoint(0.0F, 8.0F, 0.0F);
		shape3_1.addBox(-4.5F, 1.9F, 1.5F, 1, 5, 1, -0.1F);
		shapemed_1 = new ModelRenderer(this, 0, 20);
		shapemed_1.setRotationPoint(-6.5F, 16.0F, 1.0F);
		shapemed_1.addBox(-1.0F, -5.7F, -1.5F, 3, 3, 1, 0.1F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		shapemed.render(f5);
		shapeammo.render(f5);
		shapelvl21.render(f5);
		shapelvl22.render(f5);
		shapemain.render(f5);
		shape2.render(f5);
		shape4_1.render(f5);
		shape1_1.render(f5);
		shape2_1.render(f5);
		GlStateManager.pushMatrix();
		GlStateManager.translate(shapemed2.offsetX, shapemed2.offsetY, shapemed2.offsetZ);
		GlStateManager.translate(shapemed2.rotationPointX * f5, shapemed2.rotationPointY * f5,
				shapemed2.rotationPointZ * f5);
		GlStateManager.scale(1.0D, 1.0D, 0.9D);
		GlStateManager.translate(-shapemed2.offsetX, -shapemed2.offsetY, -shapemed2.offsetZ);
		GlStateManager.translate(-shapemed2.rotationPointX * f5, -shapemed2.rotationPointY * f5,
				-shapemed2.rotationPointZ * f5);
		shapemed2.render(f5);
		GlStateManager.popMatrix();
		shape5_1.render(f5);
		shape4.render(f5);
		shape1.render(f5);
		shape3.render(f5);
		shape5_2.render(f5);
		shape5_3.render(f5);
		shape5.render(f5);
		shape3_1.render(f5);
		shapemed_1.render(f5);
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
