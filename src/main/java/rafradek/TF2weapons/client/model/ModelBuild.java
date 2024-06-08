package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * ModelBuild - rafradek Created using Tabula 5.1.0
 */
public class ModelBuild extends ModelBase {
	public double[] modelScale = new double[] { 1.2D, 1.2D, 1.2D };
	public ModelRenderer shape1;
	public ModelRenderer shape1_1;
	public ModelRenderer shape1_2;
	public ModelRenderer shape1_3;
	public ModelRenderer shape1_4;
	public ModelRenderer shape1_5;
	public ModelRenderer shape1_6;
	public ModelRenderer shape1_7;
	public ModelRenderer shape1_8;
	public ModelRenderer shape1_9;
	public ModelRenderer shape1_10;
	public ModelRenderer shape1_11;

	public ModelBuild() {
		textureWidth = 64;
		textureHeight = 32;
		shape1_2 = new ModelRenderer(this, 0, 18);
		shape1_2.setRotationPoint(-6.0F, 28.800000000000054F, 3.0F);
		shape1_2.addBox(0.0F, -3.0F, 0.0F, 12, 3, 6, 0.0F);
		setRotateAngle(shape1_2, 1.3658946726107624F, 0.0F, 0.0F);
		shape1_7 = new ModelRenderer(this, 32, 0);
		shape1_7.setRotationPoint(-3.5F, 23.100000000000055F, -1.5F);
		shape1_7.addBox(0.0F, -3.0F, 0.0F, 1, 1, 3, 0.0F);
		shape1_11 = new ModelRenderer(this, 32, 0);
		shape1_11.setRotationPoint(-2.5F, 18.600000000000016F, 0.0F);
		shape1_11.addBox(0.0F, 0.0F, -0.5F, 5, 1, 1, 0.0F);
		shape1 = new ModelRenderer(this, 0, 0);
		shape1.setRotationPoint(-6.0F, 25.800000000000054F, -3.0F);
		shape1.addBox(0.0F, 0.0F, 0.0F, 12, 3, 6, 0.0F);
		shape1_10 = new ModelRenderer(this, 32, 0);
		shape1_10.setRotationPoint(2.0F, 18.600000000000016F, 0.0F);
		shape1_10.addBox(0.0F, 0.0F, -0.5F, 1, 2, 1, 0.0F);
		shape1_1 = new ModelRenderer(this, 0, 9);
		shape1_1.setRotationPoint(-6.0F, 28.800000000000054F, -3.0F);
		shape1_1.addBox(0.0F, 0.0F, 0.0F, 12, 3, 6, 0.0F);
		setRotateAngle(shape1_1, 1.7756979809790308F, 0.0F, 0.0F);
		shape1_8 = new ModelRenderer(this, 0, 0);
		shape1_8.setRotationPoint(-6.0F, 23.800000000000054F, -2.1F);
		shape1_8.addBox(0.0F, -3.0F, 0.0F, 12, 3, 3, 0.0F);
		shape1_9 = new ModelRenderer(this, 32, 0);
		shape1_9.setRotationPoint(-2.5F, 18.600000000000016F, 0.0F);
		shape1_9.addBox(0.0F, 0.0F, -0.5F, 1, 2, 1, 0.0F);
		shape1_5 = new ModelRenderer(this, 0, 0);
		shape1_5.setRotationPoint(-6.0F, 23.800000000000054F, -2.0F);
		shape1_5.addBox(0.0F, -0.1F, 0.0F, 12, 3, 3, 0.0F);
		shape1_6 = new ModelRenderer(this, 32, 0);
		shape1_6.setRotationPoint(2.5F, 23.100000000000055F, -1.5F);
		shape1_6.addBox(0.0F, -3.0F, 0.0F, 1, 1, 3, 0.0F);
		shape1_3 = new ModelRenderer(this, 0, 0);
		shape1_3.setRotationPoint(-6.0F, 22.900000000000055F, 0.0F);
		shape1_3.addBox(0.0F, -3.0F, 0.0F, 12, 3, 3, 0.0F);
		setRotateAngle(shape1_3, 2.356194490192345F, 0.0F, 0.0F);
		shape1_4 = new ModelRenderer(this, 0, 0);
		shape1_4.setRotationPoint(-6.0F, 22.900000000000055F, 4.2F);
		shape1_4.addBox(0.0F, -3.0F, 0.0F, 12, 3, 3, 0.0F);
		setRotateAngle(shape1_4, 2.356194490192345F, 0.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(1D / modelScale[0], 1D / modelScale[1], 1D / modelScale[2]);
		shape1_2.render(f5);
		shape1_7.render(f5);
		GlStateManager.pushMatrix();
		GlStateManager.translate(shape1_11.offsetX, shape1_11.offsetY, shape1_11.offsetZ);
		GlStateManager.translate(shape1_11.rotationPointX * f5, shape1_11.rotationPointY * f5,
				shape1_11.rotationPointZ * f5);
		GlStateManager.scale(1.0D, 0.5D, 0.5D);
		GlStateManager.translate(-shape1_11.offsetX, -shape1_11.offsetY, -shape1_11.offsetZ);
		GlStateManager.translate(-shape1_11.rotationPointX * f5, -shape1_11.rotationPointY * f5,
				-shape1_11.rotationPointZ * f5);
		shape1_11.render(f5);
		GlStateManager.popMatrix();
		shape1.render(f5);
		GlStateManager.pushMatrix();
		GlStateManager.translate(shape1_10.offsetX, shape1_10.offsetY, shape1_10.offsetZ);
		GlStateManager.translate(shape1_10.rotationPointX * f5, shape1_10.rotationPointY * f5,
				shape1_10.rotationPointZ * f5);
		GlStateManager.scale(0.5D, 1.0D, 0.5D);
		GlStateManager.translate(-shape1_10.offsetX, -shape1_10.offsetY, -shape1_10.offsetZ);
		GlStateManager.translate(-shape1_10.rotationPointX * f5, -shape1_10.rotationPointY * f5,
				-shape1_10.rotationPointZ * f5);
		shape1_10.render(f5);
		GlStateManager.popMatrix();
		shape1_1.render(f5);
		GlStateManager.pushMatrix();
		GlStateManager.translate(shape1_8.offsetX, shape1_8.offsetY, shape1_8.offsetZ);
		GlStateManager.translate(shape1_8.rotationPointX * f5, shape1_8.rotationPointY * f5,
				shape1_8.rotationPointZ * f5);
		GlStateManager.scale(1.0D, 1.0D, 1.4D);
		GlStateManager.translate(-shape1_8.offsetX, -shape1_8.offsetY, -shape1_8.offsetZ);
		GlStateManager.translate(-shape1_8.rotationPointX * f5, -shape1_8.rotationPointY * f5,
				-shape1_8.rotationPointZ * f5);
		shape1_8.render(f5);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.translate(shape1_9.offsetX, shape1_9.offsetY, shape1_9.offsetZ);
		GlStateManager.translate(shape1_9.rotationPointX * f5, shape1_9.rotationPointY * f5,
				shape1_9.rotationPointZ * f5);
		GlStateManager.scale(0.5D, 1.0D, 0.5D);
		GlStateManager.translate(-shape1_9.offsetX, -shape1_9.offsetY, -shape1_9.offsetZ);
		GlStateManager.translate(-shape1_9.rotationPointX * f5, -shape1_9.rotationPointY * f5,
				-shape1_9.rotationPointZ * f5);
		shape1_9.render(f5);
		GlStateManager.popMatrix();
		shape1_5.render(f5);
		shape1_6.render(f5);
		shape1_3.render(f5);
		shape1_4.render(f5);
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
