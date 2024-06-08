package rafradek.TF2weapons.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;

public class ModelTF2Character extends ModelBiped {

	public ModelTF2Character() {
		super(0.0f, 0.0f, 64, 64);
	}
	
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        GlStateManager.pushMatrix();

        if (isChild)
        {
            float f = 2.0F;
            GlStateManager.scale(0.75F, 0.75F, 0.75F);
            GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            bipedHead.render(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            bipedBody.render(scale);
            bipedRightArm.render(scale);
            bipedLeftArm.render(scale);
            bipedRightLeg.render(scale);
            bipedLeftLeg.render(scale);
            bipedHeadwear.render(scale);
        }
        else
        {
            if (entityIn.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            if (entityIn instanceof EntityTF2Character && ((EntityTF2Character) entityIn).isGiant()) {
            	GlStateManager.scale(0.75F, 0.75F, 0.75F);
                GlStateManager.translate(0.0F, 0, 0.0F);
                bipedHead.render(scale);
            	bipedHeadwear.render(scale);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
            	
            	
            }
            else {
            	bipedHead.render(scale);
            	bipedHeadwear.render(scale);
            }
            bipedBody.render(scale);
            bipedRightArm.render(scale);
            bipedLeftArm.render(scale);
            bipedRightLeg.render(scale);
            bipedLeftLeg.render(scale);
            
        }

        GlStateManager.popMatrix();
    }
	
}
