package rafradek.TF2weapons.client.renderer.tileentity;

import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.model.ModelTF2Character;
import rafradek.TF2weapons.client.renderer.entity.RenderTF2Character;
import rafradek.TF2weapons.tileentity.TileEntityRobotDeploy;

public class RenderRobotDeploy extends TileEntitySpecialRenderer<TileEntityRobotDeploy> {

	private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation("textures/entity/sign.png");
	/** The ModelSign instance for use in this renderer */
	private final ModelSign model = new ModelSign();

	ModelTF2Character robotModel = new ModelTF2Character();

	@Override
	public void render(TileEntityRobotDeploy te, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		BlockPos orig = te.getPos();
		int progress = te.progressClient;
		if (progress > 0) {
			GlStateManager.pushMatrix();
			Tessellator tessellator = Tessellator.getInstance();
			bindTexture(new ResourceLocation(RenderTF2Character.TEXTURE_PATH_BASE + "robot/" +
					te.classType.getName() + ".png"));
			boolean giant = te.produceGiant();
			float f1 = (te.getBlockMetadata() & 3) * 360 / 4F;
			GlStateManager.translate((float) x + 0.5f, (float) y + 1.2 + (giant ? 0.6f : 0), (float) z + 0.5f);
			GlStateManager.rotate(f1 + 180, 0.0F, 1.0F, 0.0F);
			if (giant)
				GlStateManager.translate(-0.5f, 0f, 0.3f);
			robotModel.isChild = false;
			robotModel.setVisible(false);
			switch (progress) {
			case 6:
				robotModel.bipedHead.showModel = true;
			case 5:
				robotModel.bipedLeftArm.showModel = true;
			case 4:
				robotModel.bipedRightArm.showModel = true;
			case 3:
				robotModel.bipedBody.showModel = true;
			case 2:
				robotModel.bipedLeftLeg.showModel = true;
			case 1:
				robotModel.bipedRightLeg.showModel = true;
			}
			float scale = giant ? 1.5f : 1f;
			GlStateManager.enableRescaleNormal();
			GlStateManager.scale(scale, -scale, -scale);
			robotModel.render(TF2weapons.dummyEnt, 0, 0, 0, 0, 0, 0.05f);
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		}
	}
}
