package rafradek.TF2weapons.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.model.ModelBuild;
import rafradek.TF2weapons.client.model.ModelTeleporter;
import rafradek.TF2weapons.entity.building.EntityTeleporter;

public class RenderTeleporter extends RenderLiving<EntityTeleporter> {

	private static final ResourceLocation TELEPORTER_RED = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/red/Teleporter.png");
	private static final ResourceLocation TELEPORTER_BLU = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/blu/Teleporter.png");
	private static final ResourceLocation TELEPORTER_ROBOT = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/robot/Teleporter.png");
	private static final ResourceLocation BOX_RED = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/red/box.png");
	private static final ResourceLocation BOX_BLU = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/blu/box.png");
	public ModelBase box;
	public ModelBase main;

	public RenderTeleporter(RenderManager renderManager) {
		super(renderManager, new ModelTeleporter(), 0.6f);
		main = this.mainModel;
		box = new ModelBuild();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTeleporter par1EntityLiving) {
		// System.out.println("class: "+clazz);
		boolean constr = par1EntityLiving.isConstructing();
		switch (par1EntityLiving.getEntTeam()) {
		case 0:
			return constr ? BOX_RED : TELEPORTER_RED;
		case 1:
			return constr ? BOX_BLU : TELEPORTER_BLU;
		case 2:
			return constr ? BOX_BLU : TELEPORTER_ROBOT;
		default:
			return constr ? BOX_BLU : TELEPORTER_BLU;
		}
	}

	@Override
	public void doRender(EntityTeleporter living, double x, double y, double z, float rot, float partial) {
		if (living.isConstructing())
			this.mainModel = this.box;
		else
			this.mainModel = this.main;

		if (living.getSoundState() == 1)
			living.spinRender = living.spin
					+ ((float) Math.PI * (living.getLevel() == 1 ? 0.25f : (living.getLevel() == 2 ? 0.325f : 0.4f)))
							* partial;
		else
			living.spinRender = 0;

		super.doRender(living, x, y, z, rot, partial);

		if (living.getSoundState() == 1) {

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder renderer = tessellator.getBuffer();
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) x, (float) y + 0.23D, (float) z);
			// GL11.glRotatef((living.prevRotationYawHead +
			// (living.rotationYawHead - living.prevRotationYawHead) *
			// p_76986_9_)*-1, 0.0F, 1.0F, 0.0F);
			// GL11.glRotatef((living.prevRotationPitch + (living.rotationPitch
			// - living.prevRotationPitch) * p_76986_9_), 1.0F, 0.0F, 0.0F);
			// GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
			/*
			 * if(TF2weapons.getTeamForDisplay(living)==0){ GlStateManager.color(1.0F, 0.0F,
			 * 0.0F, 0.28F); } else{ GlStateManager.color(0.0F, 0.0F, 1.0F, 0.28F); }
			 */
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(-0.5, 0.0D, 0.5).tex(0.75D, 0.5D).endVertex();
			renderer.pos(0.5, 0.0D, 0.5).tex(1D, 0.5D).endVertex();
			renderer.pos(0.5, 0.0D, -0.5).tex(1D, 0.0D).endVertex();
			renderer.pos(-0.5, 0.0D, -0.5).tex(0.75D, 0.0D).endVertex();
			tessellator.draw();

			if (living.getColor() != -1) {
				float[] color = EntitySheep.getDyeRgb(EnumDyeColor.byDyeDamage(living.getColor()));
				GlStateManager.color(color[0], color[1], color[2], 0.85F);

				renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(-0.5, 0.0D, 0.5).tex(0.75D, 1D).endVertex();
				renderer.pos(0.5, 0.0D, 0.5).tex(1D, 1D).endVertex();
				renderer.pos(0.5, 0.0D, -0.5).tex(1D, 0.5D).endVertex();
				renderer.pos(-0.5, 0.0D, -0.5).tex(0.75D, 0.5D).endVertex();
				tessellator.draw();
			}

			GlStateManager.color(1F, 1F, 1F, 1.0F);
			GL11.glDisable(GL11.GL_BLEND);
			// GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
}
