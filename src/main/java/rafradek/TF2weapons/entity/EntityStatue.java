package rafradek.TF2weapons.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemStatue;

import javax.annotation.Nullable;

public class EntityStatue extends Entity implements IEntityAdditionalSpawnData {

	public EntityLivingBase entity;
	public NBTTagCompound data;
	public boolean isFeign;
	public float renderYawOffset;
	public boolean first;
	public boolean clientOnly;
	public int ticksLeft;
	public boolean player;
	public boolean useHand;
	public GameProfile profile;
	public float armSwing;

	public EntityStatue(World worldIn) {
		super(worldIn);
	}

	public EntityStatue(World worldIn, EntityLivingBase toCopy, boolean isFeign) {
		super(worldIn);
		this.setPosition(toCopy.posX, toCopy.posY, toCopy.posZ);
		this.width = toCopy.width;
		this.height = toCopy.height;
		this.entity = toCopy;
		if (!this.world.isRemote) {
			if (toCopy instanceof EntityPlayer) {
				this.data = toCopy.writeToNBT(new NBTTagCompound());
				this.player = true;
				this.profile = ((EntityPlayer) toCopy).getGameProfile();
			} else {
				this.data = toCopy.serializeNBT();
			}
			// this.first = true;
			this.ticksLeft = toCopy instanceof EntityPlayer || !toCopy.isNonBoss() ? -1 : 1200;
			this.useHand = toCopy.hasCapability(TF2weapons.WEAPONS_CAP, null)
					&& ((WeaponsCapability.get(toCopy).state & 3) != 0 || WeaponsCapability.get(toCopy).isCharging());
		} else
			this.clientOnly = true;
		// this.data = this.entity.serializeNBT();
		this.isFeign = isFeign;
		if (!isFeign) {
			this.entity.deathTime = 0;
			this.entity.hurtTime = 0;
			this.renderYawOffset = toCopy.renderYawOffset;
			this.prevRotationYaw = this.entity.prevRotationYaw;
			this.rotationYaw = this.entity.rotationYaw;
		} else {
			this.motionX = entity.motionX;
			this.motionY = entity.motionY;
			this.motionZ = entity.motionZ;
		}
		/*
		 * this.renderYawOffset=toCopy.renderYawOffset;
		 * this.rotationYawHead=toCopy.rotationYawHead;
		 * this.rotationYaw=toCopy.rotationYaw; this.rotationPitch=toCopy.rotationPitch;
		 * this.limbSwingAmount=toCopy.limbSwingAmount; this.limbSwing=toCopy.limbSwing;
		 * this.entityClass=toCopy.getClass().getName();
		 * this.data=toCopy.getDataManager();
		 */
	}

	@Override
	protected void entityInit() {}

	/*
	 * public AxisAlignedBB getEntityBoundingBox() { return this.entity != null ?
	 * this.entity.getEntityBoundingBox() : super.getEntityBoundingBox(); }
	 */

	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead && !this.clientOnly;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		}
		if (source.damageType.equals("player")) {
			if (this.data != null)
				this.entityDropItem(ItemStatue.getStatue(this), 0);
			this.setDead();
			return true;
		}
		return false;
	}

	@Override
	public void onUpdate() {
		if (this.onGround) {
			this.motionX *= 0.1;
			this.motionZ *= 0.1;
		}
		this.motionX *= 0.98;
		this.motionY *= 0.98;
		this.motionZ *= 0.98;
		this.motionY -= 0.08;
		/*
		 * if(!this.isFeign && this.clientOnly && this.ticksExisted > 15 &&
		 * this.world.getEntitiesWithinAABB(EntityStatue.class,
		 * getEntityBoundingBox().expand(1, 0, 1), stat -> { return stat != this;
		 * }).isEmpty()) this.setDead();
		 */
		if (!this.world.isRemote && this.ticksLeft >= 0) {
			if (--this.ticksLeft <= 0)
				this.setDead();
		}
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		if (this.isFeign && this.ticksExisted >= 20) {
			/*
			 * int i = 20; while (i > 0) { int j = EntityXPOrb.getXPSplit(i); i -= j;
			 * EntityXPOrb orb=new EntityXPOrb(this.world, this.posX, this.posY, this.posZ,
			 * j); orb.xpOrbAge=5900; this.world.spawnEntity(orb); }
			 */

			this.setDead();

			for (int k = 0; k < 20; ++k) {
				double d2 = this.rand.nextGaussian() * 0.02D;
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
						this.posX + this.rand.nextFloat() * this.width * 2.0F - this.width,
						this.posY + this.rand.nextFloat() * this.height,
						this.posZ + this.rand.nextFloat() * this.width * 2.0F - this.width, d2, d0, d1);
			}
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		if (!TF2ConfigVars.australiumStatue) {
			this.setDead();
			return;
		}

		this.data = compound.getCompoundTag("Entity");
		this.ticksLeft = compound.getShort("TicksLeft");
		this.player = compound.getBoolean("Player");
		if (player) {
			this.profile = NBTUtil.readGameProfileFromNBT(compound.getCompoundTag("Profile"));
		}
		this.useHand = compound.getBoolean("UseArm");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		try {
			NBTTagCompound tag = this.data;
			if (tag != null)
				compound.setTag("Entity", tag);
		} catch (Exception e) {

		}
		compound.setShort("TicksLeft", (short) this.ticksLeft);
		if (this.profile != null) {
			compound.setTag("Profile", NBTUtil.writeGameProfile(new NBTTagCompound(), this.profile));
		}
		compound.setBoolean("Player", this.player);
		compound.setBoolean("UseArm", this.useHand);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {

		if (!this.first) {
			PacketBuffer buff = new PacketBuffer(buffer);
			buffer.writeBoolean(this.player);
			if (this.profile != null) {
				buff.writeUniqueId(this.profile.getId());
				buff.writeString(this.profile.getName());
				buff.writeVarInt(this.profile.getProperties().size());

				for (Property property : this.profile.getProperties().values()) {
					buff.writeString(property.getName());
					buff.writeString(property.getValue());

					if (property.hasSignature()) {
						buff.writeBoolean(true);
						buff.writeString(property.getSignature());
					} else {
						buff.writeBoolean(false);
					}
				}
			}
			int pos = buff.writerIndex();
			buff.writeCompoundTag(data);
			if (buff.writerIndex() - pos >= 2097152)
				buff.clear();
			buff.writeBoolean(useHand);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void readSpawnData(ByteBuf additionalData) {
		if (additionalData.readableBytes() == 0)
			return;
		try {
			PacketBuffer buff = new PacketBuffer(additionalData);
			if (buff.readBoolean()) {
				profile = new GameProfile(buff.readUniqueId(), buff.readString(16));
				int l = buff.readVarInt();
				int i1 = 0;

				for (; i1 < l; ++i1) {
					String s = buff.readString(32767);
					String s1 = buff.readString(32767);

					if (buff.readBoolean()) {
						profile.getProperties().put(s, new Property(s, s1, buff.readString(32767)));
					} else {
						profile.getProperties().put(s, new Property(s, s1));
					}
				}
				final NetworkPlayerInfo info = new NetworkPlayerInfo(profile);
				this.entity = new EntityOtherPlayerMP(world, profile) {
					@Override
					@Nullable
					protected NetworkPlayerInfo getPlayerInfo() {
						return info;
					}
				};
				this.entity.readFromNBT(buff.readCompoundTag());
				/*
				 * TF2EventsCommon.THREAD_POOL.submit(()->{ if (profile.getId() != null)
				 * cap.skinType = DefaultPlayerSkin.getSkinType(profile.getId());
				 * Minecraft.getMinecraft().getSkinManager().loadProfileTextures(profile, new
				 * SkinManager.SkinAvailableCallback() {
				 *
				 * @Override public void skinAvailable(Type typeIn, ResourceLocation location,
				 * MinecraftProfileTexture profileTexture) { if (typeIn == Type.SKIN) { if
				 * (typeIn == Type.SKIN) cap.skinDisguise = location; cap.skinType =
				 * profileTexture.getMetadata("model");
				 *
				 * if (cap.skinType == null) cap.skinType = "default"; } } }, false); });
				 */
			} else
				this.entity = (EntityLivingBase) EntityList.createEntityFromNBT(buff.readCompoundTag(), this.world);
			if (this.entity == null) {
				this.setDead();
				return;
			}
			this.entity.deathTime = 0;
			this.entity.hurtTime = 0;
			this.entity.limbSwingAmount = 0.5f;
			this.entity.ticksExisted = 15;
			this.entity.limbSwing += this.rand.nextFloat() * 10;
			this.setSize(this.entity.width, this.entity.height);
			this.entity.setPosition(this.posX, this.posY, this.posZ);
			// this.prevRotationYaw = this.entity.prevRotationYaw;
			// this.rotationYaw = this.entity.rotationYaw;
			this.entity.rotationYawHead = this.rotationYaw;
			this.entity.renderYawOffset = this.rotationYaw;
			this.entity.prevRenderYawOffset = this.rotationYaw;

			if (buff.readBoolean()) {
				WeaponsCapability.get(this.entity).state = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
