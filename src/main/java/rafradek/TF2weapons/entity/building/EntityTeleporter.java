package rafradek.TF2weapons.entity.building;

import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.lwjgl.opengl.GL11;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Achievements;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.util.TF2Util;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EntityTeleporter extends EntityBuilding {
	// public static ArrayList<BlockPosDimension> teleportersData=new
	// ArrayList<BlockPosDimension>();
	public static final int TP_PER_PLAYER = 128;

	public static int tpCount = 0;
	public static HashMap<UUID, TeleporterData[]> teleporters = new HashMap<>();

	public int tpID = -1;
	public int ticksToTeleport;

	public EntityTeleporter linkedTp;

	public float spin;
	public float spinRender;

	public long timestamp;

	private static final DataParameter<Integer> TELEPORTS = EntityDataManager.createKey(EntityTeleporter.class,
			DataSerializers.VARINT);
	private static final DataParameter<Integer> TPPROGRESS = EntityDataManager.createKey(EntityTeleporter.class,
			DataSerializers.VARINT);
	private static final DataParameter<Byte> CHANNEL = EntityDataManager.createKey(EntityTeleporter.class,
			DataSerializers.BYTE);
	private static final DataParameter<Boolean> EXIT = EntityDataManager.createKey(EntityTeleporter.class,
			DataSerializers.BOOLEAN);
	private static final DataParameter<Byte> COLOR = EntityDataManager.createKey(EntityTeleporter.class,
			DataSerializers.BYTE);

	public EntityTeleporter(World world) {
		super(world);
		this.setSize(1f, 0.2f);
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		if (!this.world.isRemote && !this.isExit() && this.getTPprogress() <= 0 && entityIn != null
				&& entityIn instanceof EntityLivingBase && !(entityIn instanceof EntityBuilding)
				&& ((this.getOwner() != null && ((WeaponsCapability.get(this.getOwner()).teleporterEntity
						&& !(entityIn instanceof EntityPlayer))
						|| (WeaponsCapability.get(this.getOwner()).teleporterPlayer && entityIn instanceof EntityPlayer
								&& entityIn.getTeam() == null)))
						|| TF2Util.isOnSameTeam(EntityTeleporter.this, entityIn))
				&& entityIn.getEntityBoundingBox()
						.intersects(this.getEntityBoundingBox().grow(0, 0.5, 0).offset(0, 0.5D, 0)))
			if (ticksToTeleport <= 0)
				if (ticksToTeleport < 0)
					ticksToTeleport = 10;
				else {
					TeleporterData exit = this.getTeleportExit();
					if (exit != null && this.consumeEnergy(this.getMinEnergy())) {
						if (exit.dimension != this.dimension) {
							if (entityIn instanceof EntityPlayerMP
									&& net.minecraftforge.common.ForgeHooks.onTravelToDimension(this, exit.dimension)) {
								this.world.getMinecraftServer().getPlayerList().transferPlayerToDimension(
										(EntityPlayerMP) entityIn, exit.dimension,
										new TeleporterDim((WorldServer) this.world, exit));

							} else {
								World destworld = this.world.getMinecraftServer().getWorld(exit.dimension);
								Entity newent = EntityList.newEntity(entityIn.getClass(), destworld);
								if (newent != null) {
									NBTTagCompound data = entityIn.writeToNBT(new NBTTagCompound());
									data.removeTag("Dimension");
									newent.readFromNBT(data);
									entityIn.setDead();
									newent.forceSpawn = true;
									entityIn.moveToBlockPosAndAngles(exit, entityIn.rotationYaw,
											entityIn.rotationPitch);
									destworld.spawnEntity(newent);
									entityIn = newent;
								}
							}
						}
						entityIn.setPositionAndUpdate(exit.getX() + 0.5, exit.getY() + 0.23, exit.getZ() + 0.5);
						this.setTeleports(this.getTeleports() + 1);
						this.setTPprogress(this.getLevel() == 1 ? 200 : (this.getLevel() == 2 ? 100 : 60));
						this.playSound(TF2Sounds.MOB_TELEPORTER_SEND, 1.5f, 1f);
						entityIn.playSound(TF2Sounds.MOB_TELEPORTER_RECEIVE, 0.75f, 1f);
						if (this.getOwner() instanceof EntityPlayerMP) {
							((EntityPlayer) this.getOwner()).addStat(TF2Achievements.TELEPORTED);
							/*
							 * if(((EntityPlayerMP)
							 * this.getOwner()).getStatFile().readStat(TF2Achievements.TELEPORTED)>=100)
							 * ((EntityPlayer) this.getOwner()).addStat(TF2Achievements.TELEPORTS);
							 */
						}
					}
				}
		return super.getCollisionBox(entityIn);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			if (this.tpID == -1) {
				tpCount++;
				this.tpID = tpCount;
			}
			if (!this.isExit()) {
				ticksToTeleport--;
				if (this.getTPprogress() > 0)
					this.setTPprogress(this.getTPprogress() - 1);
				if (this.getSoundState() == 1 && (this.getTPprogress() > 0 || this.getTeleportExit() == null)) {
					this.setSoundState(0);
					if (this.linkedTp != null)
						this.linkedTp.setSoundState(0);

				}
				if (this.getSoundState() == 0 && this.getTPprogress() <= 0 && this.getTeleportExit() != null) {
					this.setSoundState(1);
					if (this.linkedTp != null)
						this.linkedTp.setSoundState(1);
				}
				if (this.linkedTp != null && this.getColor() != this.linkedTp.getColor()) {
					this.getDataManager().set(COLOR, (byte) this.linkedTp.getColor());
				}
				/*
				 * if(ticksToTeleport<=0){ List<EntityLivingBase>
				 * targetList=this.world.getEntitiesWithinAABB( EntityLivingBase.class,
				 * this.getEntityBoundingBox().grow(0, 0.5, 0).offset(0, 0.5D, 0), new
				 * Predicate<EntityLivingBase>(){
				 *
				 * @Override public boolean apply(EntityLivingBase input) {
				 *
				 * return !(input instanceof
				 * EntityBuilding)&&EntityTeleporter.this!=input&&((TF2weapons.
				 * dispenserHeal&&input instanceof EntityPlayer && getTeam()==null &&
				 * input.getTeam() ==
				 * null)||TF2weapons.isOnSameTeam(EntityTeleporter.this,input)); }
				 *
				 * });
				 *
				 * if(!targetList.isEmpty()){ if(ticksToTeleport<0){ ticksToTeleport=10; } else{
				 * BlockPosDimension exit=this.getTeleportExit(); if(exit !=null){
				 * if(exit.dimension!=this.dimension)
				 * targetList.get(0).travelToDimension(exit.dimension);
				 * targetList.get(0).setPositionAndUpdate(exit.getX()+0.5, exit.getY()+0.23,
				 * exit.getZ()+0.5); } } } }
				 */
			} else if (teleporters.get(this.getOwnerId())[this.getID()] == null
					|| teleporters.get(this.getOwnerId())[this.getID()].id != this.tpID) {
				this.setExit(false);
				this.updateTeleportersData(true);
			} else if (teleporters.get(this.getOwnerId())[this.getID()].id == this.tpID
					&& !this.getPosition().equals(teleporters.get(this.getOwnerId())[this.getID()]))
				this.updateTeleportersData(false);
		} else if (this.getSoundState() == 1)
			this.spin += (float) Math.PI * (this.getLevel() == 1 ? 0.25f : (this.getLevel() == 2 ? 0.325f : 0.4f));
		else
			this.spin = 0;
	}

	public TeleporterData getTeleportExit() {
		UUID uuid = this.getOwnerId();
		if (this.getOwnerId() != null && teleporters.get(uuid) != null) {
			final TeleporterData data = teleporters.get(uuid)[this.getID()];
			List<EntityTeleporter> list = world.getEntities(EntityTeleporter.class, input -> data != null && data.id == input.tpID);
			if (!list.isEmpty())
				this.linkedTp = list.get(0);
			// System.out.println("linkedtpset");
			return data;
		}
		return null;
	}

	@Override
	public SoundEvent getSoundNameForState(int state) {
		switch (state) {
		case 0:
			return null;
		case 1:
			return this.getLevel() == 1 ? TF2Sounds.MOB_TELEPORTER_SPIN_1
					: (this.getLevel() == 2 ? TF2Sounds.MOB_TELEPORTER_SPIN_2 : TF2Sounds.MOB_TELEPORTER_SPIN_3);
		default:
			return super.getSoundNameForState(state);
		}
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {

		if (player == this.getOwner() && player.getHeldItem(hand).getItem() instanceof ItemDye) {
			if (!world.isRemote) {
				this.setColor(player.getHeldItem(hand).getMetadata());
			}
			return true;
		}
		if (player == this.getOwner() && hand == EnumHand.MAIN_HAND) {
			if (!this.world.isRemote)
				if (TF2ConfigVars.disableBuildingGui)
					this.grab();
				else
					FMLNetworkHandler.openGui(player, TF2weapons.instance, 5, world, this.getEntityId(), 0, 0);
			return true;
		}
		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(TELEPORTS, 0);
		this.dataManager.register(TPPROGRESS, 0);
		this.dataManager.register(EXIT, false);
		this.dataManager.register(CHANNEL, (byte) 0);
		this.dataManager.register(COLOR, (byte) -1);
	}

	public boolean isExit() {
		return this.dataManager.get(EXIT);
	}

	public void setExit(boolean exit) {
		// if(this.getOwner()!=null&&exit&&teleporters.get(UUID.fromString(this.getOwnerId()))[this.getID()]!=null)
		// return;
		if (this.tpID == -1) {
			tpCount++;
			this.tpID = tpCount;
		}
		this.dataManager.set(EXIT, exit);
		if (this.getOwnerId() != null)
			this.updateTeleportersData(false);
	}

	public int getID() {
		return this.dataManager.get(CHANNEL);
	}

	public void setID(int id) {
		if ((id >= TP_PER_PLAYER || id < 0)/*
											 * ||teleporters.get(UUID.fromString( this.getOwnerId()))[id]!=null
											 */)
			return;
		if (this.getOwnerId() != null)
			this.updateTeleportersData(true);
		this.dataManager.set(CHANNEL, (byte) id);
		;
		if (this.getOwnerId() != null)
			this.updateTeleportersData(false);
	}

	public int getTPprogress() {
		return this.isDisabled() ? 20 : this.dataManager.get(TPPROGRESS);
	}

	public void setTPprogress(int progress) {
		this.dataManager.set(TPPROGRESS, progress);
	}

	public void setTeleports(int amount) {
		this.dataManager.set(TELEPORTS, amount);
	}

	public int getTeleports() {
		return this.dataManager.get(TELEPORTS);
	}

	public void setColor(int color) {
		this.dataManager.set(COLOR, (byte) color);
		if (this.linkedTp != null && !this.isExit()) {
			this.linkedTp.setColor(color);
		}
	}

	public int getColor() {
		return this.dataManager.get(COLOR);
	}

	/*
	 * public void setOwner(EntityLivingBase owner) { super.setOwner(owner);
	 * if(owner instanceof EntityPlayer){ this.dataManager.set(key, value);14,
	 * owner.getUniqueID().toString()); } }
	 */
	@Override
	public void upgrade() {
		super.upgrade();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	public void setDead() {
		// Chunk
		// chunk=this.world.getChunkFromBlockCoords(this.getPosition());
		// if(chunk.isLoaded()){
		if (!this.world.isRemote)
			this.updateTeleportersData(true);
		// }
		// System.out.println("teleporter removed: "+chunk.isLoaded()+"
		// "+chunk.isEmpty()+" "+chunk.isPopulated());
		super.setDead();
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if (!this.world.isRemote)
			if (OWNER_UUID.equals(key)) {
				UUID id = this.getOwnerId();
				if (!teleporters.containsKey(id))
					teleporters.put(id, new TeleporterData[TP_PER_PLAYER]);
			}
	}

	public void updateTeleportersData(boolean forceremove) {
		if (this.world.isRemote)
			return;

		UUID id = this.getOwnerId();
		if (!forceremove && this.isExit()/* &&teleporters.get(id)[this.getID()]==null */)
			teleporters.get(id)[this.getID()] = new TeleporterData(this.getPosition(), this.tpID, this.dimension);
		else if (teleporters.get(id)[this.getID()] != null && teleporters.get(id)[this.getID()].id == this.tpID) {
			// new Exception().printStackTrace();
			teleporters.get(id)[this.getID()] = null;
		}
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_TELEPORTER_DEATH;
	}

	@Override
	public float getCollHeight() {
		return 0.2f;
	}

	@Override
	public float getCollWidth() {
		return 1f;
	}

	@Override
	public int getIronDrop() {
		return 1 + this.getLevel() / 2;
	}

	@Override
	public boolean shouldUseBlocks() {
		return TF2ConfigVars.teleporterUseEnergy >= 0 && super.shouldUseBlocks();
	}

	@Override
	public int getMinEnergy() {
		return this.getOwnerId() != null ? TF2ConfigVars.teleporterUseEnergy : 0;
	}

	@Override
	public int getBuildingID() {
		return 2 + (this.isExit() ? 1 : 0);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);

		par1NBTTagCompound.setByte("TeleExitID", (byte) this.getID());
		par1NBTTagCompound.setBoolean("TeleExit", this.isExit());
		par1NBTTagCompound.setInteger("TeleID", this.tpID);
		par1NBTTagCompound.setShort("Teleports", (short) this.getTeleports());
		par1NBTTagCompound.setByte("Color", (byte) this.getColor());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {

		super.readEntityFromNBT(par1NBTTagCompound);
		this.tpID = par1NBTTagCompound.getInteger("TeleID");
		this.setTeleports(par1NBTTagCompound.getShort("Teleports"));
		this.getDataManager().set(CHANNEL, par1NBTTagCompound.getByte("TeleExitID"));
		if (!this.world.isRemote && ((teleporters.get(this.getOwnerId())[this.getID()] == null
				|| teleporters.get(this.getOwnerId())[this.getID()].id == this.tpID)))
			// this.setID(par1NBTTagCompound.getByte("TeleExitID"));
			this.setExit(par1NBTTagCompound.getBoolean("TeleExit"));
		if (this.world.isRemote)
			this.getDataManager().set(EXIT, par1NBTTagCompound.getBoolean("TeleExit"));
		this.getDataManager().set(COLOR, par1NBTTagCompound.getByte("Color"));

	}

	@Override
	public void renderGUI(BufferBuilder renderer, Tessellator tessellator, EntityPlayer player, int width, int height,
			GuiIngame gui) {
		// GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		// gui.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64,
		// event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);
		ClientProxy.setColor(TF2Util.getTeamColor(this), 0.7f, 0, 0.25f, 0.8f);
		gui.drawTexturedModalRect(20, 2, 0, 112, 124, 44);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		gui.drawTexturedModalRect(0, 0, 0, 0, 144, 48);
		/*
		 * renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 72,
		 * event.getResolution().getScaledHeight() / 2 + 76, 0.0D).tex(0.0D,
		 * 0.1875D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 72, event.getResolution().getScaledHeight() / 2 + 76, 0.0D).tex(0.5625D,
		 * 0.1875D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 72, event.getResolution().getScaledHeight() / 2 + 28, 0.0D).tex(0.5625D,
		 * 0D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2 -
		 * 72, event.getResolution().getScaledHeight() / 2 + 28, 0.0D).tex(0.0D,
		 * 0D).endVertex(); tessellator.draw();
		 */

		double imagePos = this.isExit() ? 0.1875D : 0;
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(19, 48, 0.0D).tex(0.5625D + imagePos, 0.9375D).endVertex();
		renderer.pos(69, 48, 0.0D).tex(0.75D + imagePos, 0.9375D).endVertex();
		renderer.pos(69, 0, 0.0D).tex(0.75D + imagePos, 0.75D).endVertex();
		renderer.pos(19, 0, 0.0D).tex(0.5625D + imagePos, 0.75D).endVertex();
		tessellator.draw();

		if (!this.isEntityAlive())
			return;

		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(67, 22, 0.0D).tex(0.9375D, 0.1875D).endVertex();
		renderer.pos(83, 22, 0.0D).tex(1D, 0.1875D).endVertex();
		renderer.pos(83, 6, 0.0D).tex(1D, 0.125D).endVertex();
		renderer.pos(67, 6, 0.0D).tex(0.9375D, 0.125D).endVertex();
		tessellator.draw();

		imagePos = this.getLevel() == 1 ? 0.3125D : this.getLevel() == 2 ? 0.375D : 0.4375D;
		renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(50, 18, 0.0D).tex(0.9375D, 0.0625D + imagePos).endVertex();
		renderer.pos(66, 18, 0.0D).tex(1D, 0.0625D + imagePos).endVertex();
		renderer.pos(66, 2, 0.0D).tex(1D, imagePos).endVertex();
		renderer.pos(50, 2, 0.0D).tex(0.9375D, imagePos).endVertex();
		tessellator.draw();

		if (this.getLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(67, 42, 0.0D).tex(0.9375D, 0.125D).endVertex();
			renderer.pos(83, 42, 0.0D).tex(1D, 0.125D).endVertex();
			renderer.pos(83, 26, 0.0D).tex(1D, 0.0625).endVertex();
			renderer.pos(67, 26, 0.0D).tex(0.9375D, 0.0625).endVertex();
			tessellator.draw();
		}
		if (this.getTPprogress() <= 0) {
			gui.drawString(gui.getFontRenderer(), this.getTeleports() + " (ID: " + (this.getID() + 1) + ")", 85, 10,
					16777215);
		}

		float health = this.getHealth() / this.getMaxHealth();
		if (health > 0.33f) {
			GlStateManager.color(0.9F, 0.9F, 0.9F, 1F);
		} else {
			GlStateManager.color(0.85F, 0.0F, 0.0F, 1F);
		}
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		for (int i = 0; i < health * 8; i++) {

			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(19, 39 - i * 5, 0.0D).endVertex();
			renderer.pos(9, 39 - i * 5, 0.0D).endVertex();
			renderer.pos(9, 43 - i * 5, 0.0D).endVertex();
			renderer.pos(19, 43 - i * 5, 0.0D).endVertex();
			tessellator.draw();
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.33F);
		if (this.getTPprogress() > 0) {

			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 21, 0.0D).endVertex();
			renderer.pos(140, 21, 0.0D).endVertex();
			renderer.pos(140, 7, 0.0D).endVertex();
			renderer.pos(85, 7, 0.0D).endVertex();
			tessellator.draw();
		}

		if (this.getLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 41, 0.0D).endVertex();
			renderer.pos(140, 41, 0.0D).endVertex();
			renderer.pos(140, 27, 0.0D).endVertex();
			renderer.pos(85, 27, 0.0D).endVertex();
			tessellator.draw();
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F);
		if (this.getTPprogress() > 0) {
			double tpProgress = (1 - ((double) this.getTPprogress()
					/ (this.getLevel() == 1 ? 200 : (this.getLevel() == 2 ? 100 : 60)))) * 55;

			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 21, 0.0D).endVertex();
			renderer.pos(85 + tpProgress, 21, 0.0D).endVertex();
			renderer.pos(85 + tpProgress, 7, 0.0D).endVertex();
			renderer.pos(85, 7, 0.0D).endVertex();
			tessellator.draw();
		}
		if (this.getLevel() < 3) {
			renderer.begin(7, DefaultVertexFormats.POSITION);
			renderer.pos(85, 41, 0.0D).endVertex();
			renderer.pos(85 + this.getProgress() * 0.275D, 41, 0.0D).endVertex();
			renderer.pos(85 + this.getProgress() * 0.275D, 27, 0.0D).endVertex();
			renderer.pos(85, 27, 0.0D).endVertex();
			tessellator.draw();
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		// GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		// gui.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64,
		// event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);

		/*
		 * renderer.begin(7, DefaultVertexFormats.POSITION_TEX); renderer.pos(69,
		 * event.getResolution().getScaledHeight() / 2 + 50, 0.0D).tex(0.9375D,
		 * 0.3125D).endVertex(); renderer.pos(83,
		 * event.getResolution().getScaledHeight() / 2 + 50, 0.0D).tex(1D,
		 * 0.3125D).endVertex(); renderer.pos(83,
		 * event.getResolution().getScaledHeight() / 2 + 34, 0.0D).tex(1D,
		 * 0.25D).endVertex(); renderer.pos(69, event.getResolution().getScaledHeight()
		 * / 2 + 34, 0.0D).tex(0.9375D, 0.25D).endVertex(); tessellator.draw();
		 * 
		 * imagePos = teleporter.getLevel() == 1 ? 0.3125D : teleporter.getLevel() == 2
		 * ? 0.375D : 0.4375D; renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 22,
		 * event.getResolution().getScaledHeight() / 2 + 46, 0.0D).tex(0.9375D, 0.0625D
		 * + imagePos).endVertex(); renderer.pos(event.getResolution().getScaledWidth()
		 * / 2 - 6, event.getResolution().getScaledHeight() / 2 + 46, 0.0D).tex(1D,
		 * 0.0625D + imagePos).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 6,
		 * event.getResolution().getScaledHeight() / 2 + 30, 0.0D).tex(1D,
		 * imagePos).endVertex(); renderer.pos(event.getResolution().getScaledWidth() /
		 * 2 - 22, event.getResolution().getScaledHeight() / 2 + 30, 0.0D).tex(0.9375D,
		 * imagePos).endVertex(); tessellator.draw();
		 * 
		 * if (teleporter.getLevel() < 3) { renderer.begin(7,
		 * DefaultVertexFormats.POSITION_TEX);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 5,
		 * event.getResolution().getScaledHeight() / 2 + 70, 0.0D).tex(0.9375D,
		 * 0.125D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 11, event.getResolution().getScaledHeight() / 2 + 70, 0.0D).tex(1D,
		 * 0.125D).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * + 11, event.getResolution().getScaledHeight() / 2 + 54, 0.0D).tex(1D,
		 * 0.0625).endVertex(); renderer.pos(event.getResolution().getScaledWidth() / 2
		 * - 5, event.getResolution().getScaledHeight() / 2 + 54, 0.0D).tex(0.9375D,
		 * 0.0625).endVertex(); tessellator.draw(); } if (teleporter.getTPprogress() <=
		 * 0) { gui.drawString(gui.getFontRenderer(), teleporter.getTeleports() +
		 * " (ID: " + (teleporter.getID() + 1) + ")",
		 * event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 38, 16777215); } float health =
		 * teleporter.getHealth() / teleporter.getMaxHealth(); if (health > 0.33f) {
		 * GlStateManager.color(0.9F, 0.9F, 0.9F, 1F); } else {
		 * GlStateManager.color(0.85F, 0.0F, 0.0F, 1F); }
		 * GL11.glDisable(GL11.GL_TEXTURE_2D); for (int i = 0; i < health * 8; i++) {
		 * 
		 * renderer.begin(7, DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 53,
		 * event.getResolution().getScaledHeight() / 2 + 67 - i * 5, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 63,
		 * event.getResolution().getScaledHeight() / 2 + 67 - i * 5, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 63,
		 * event.getResolution().getScaledHeight() / 2 + 71 - i * 5, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 - 53,
		 * event.getResolution().getScaledHeight() / 2 + 71 - i * 5, 0.0D).endVertex();
		 * tessellator.draw(); } GlStateManager.color(1.0F, 1.0F, 1.0F, 0.33F); if
		 * (teleporter.getTPprogress() > 0) { renderer.begin(7,
		 * DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 49, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 68,
		 * event.getResolution().getScaledHeight() / 2 + 49, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 68,
		 * event.getResolution().getScaledHeight() / 2 + 35, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 35, 0.0D).endVertex();
		 * tessellator.draw(); } if (teleporter.getLevel() < 3) { renderer.begin(7,
		 * DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 69, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 68,
		 * event.getResolution().getScaledHeight() / 2 + 69, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 68,
		 * event.getResolution().getScaledHeight() / 2 + 55, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 55, 0.0D).endVertex();
		 * tessellator.draw(); } GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F); if
		 * (teleporter.getTPprogress() > 0) { double tpProgress = (1 - ((double)
		 * teleporter.getTPprogress() / (teleporter.getLevel() == 1 ? 200 :
		 * (teleporter.getLevel() == 2 ? 100 : 60)))) * 55; renderer.begin(7,
		 * DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 49, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13 + tpProgress,
		 * event.getResolution().getScaledHeight() / 2 + 49, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13 + tpProgress,
		 * event.getResolution().getScaledHeight() / 2 + 35, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 35, 0.0D).endVertex();
		 * tessellator.draw(); } if (teleporter.getLevel() < 3) { renderer.begin(7,
		 * DefaultVertexFormats.POSITION);
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 69, 0.0D).endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13 +
		 * teleporter.getProgress() * 0.275D, event.getResolution().getScaledHeight() /
		 * 2 + 69, 0.0D) .endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13 +
		 * teleporter.getProgress() * 0.275D, event.getResolution().getScaledHeight() /
		 * 2 + 55, 0.0D) .endVertex();
		 * renderer.pos(event.getResolution().getScaledWidth() / 2 + 13,
		 * event.getResolution().getScaledHeight() / 2 + 55, 0.0D).endVertex();
		 * tessellator.draw(); }
		 */
	}

	public static class TeleporterData extends BlockPos {

		public final int dimension;
		public final int id;

		public TeleporterData(BlockPos blockPos, int id, int dimension) {
			super(blockPos);
			this.id = id;
			this.dimension = dimension;
		}
	}

	public static class TeleporterDim extends Teleporter {

		public BlockPos target;

		public TeleporterDim(WorldServer world, BlockPos targetPos) {
			super(world);
			target = targetPos;
		}

		@Override
		public boolean makePortal(Entity entityIn) {
			return true;
		}

		@Override
		public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
			if (entityIn instanceof EntityPlayerMP) {
				((EntityPlayerMP) entityIn).connection.setPlayerLocation(target.getX(), target.getY(), target.getZ(),
						entityIn.rotationYaw, entityIn.rotationPitch);
			} else {
				entityIn.setLocationAndAngles(target.getX(), target.getY(), target.getZ(), entityIn.rotationYaw,
						entityIn.rotationPitch);
			}
			return true;
		}
	}
}
