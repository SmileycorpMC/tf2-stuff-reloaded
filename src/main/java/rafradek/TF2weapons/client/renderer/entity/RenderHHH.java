package rafradek.TF2weapons.client.renderer.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.boss.EntityHHH;

public class RenderHHH extends RenderBiped<EntityHHH> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/hhh.png");

	public RenderHHH(RenderManager renderManagerIn) {
		super(renderManagerIn, new ModelBiped(), 0.8F);
		this.addLayer(new LayerHeldItem(this));

	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHHH par1EntityLiving) {
		return TEXTURE;
	}

	@Override
	protected void preRenderCallback(EntityHHH entitylivingbaseIn, float partialTickTime) {
		float f = 1.45F;
		if (entitylivingbaseIn.begin > 0)
			GlStateManager.translate(0, entitylivingbaseIn.begin * 0.1f, 0);
		GlStateManager.scale(f, f, f);
	}
}
