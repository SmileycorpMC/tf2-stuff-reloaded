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

	public EntityStatue(World world) {
		super(world);
	}

	public EntityStatue(World world, EntityLivingBase toCopy, boolean isFeign) {
		super(world);
		this.setPosition(toCopy.posX, toCopy.posY, toCopy.posZ);
		width = toCopy.width;
		height = toCopy.height;
		entity = toCopy;
		if (!world.isRemote) {
			if (toCopy instanceof EntityPlayer) {
				data = toCopy.writeToNBT(new NBTTagCompound());
				player = true;
				profile = ((EntityPlayer) toCopy).getGameProfile();
			} else data = toCopy.serializeNBT();
			ticksLeft = toCopy instanceof EntityPlayer || !toCopy.isNonBoss() ? -1 : 1200;
			useHand = toCopy.hasCapability(TF2weapons.WEAPONS_CAP, null)
					&& ((WeaponsCapability.get(toCopy).state & 3) != 0 || WeaponsCapability.get(toCopy).isCharging());
		} else clientOnly = true;
		// this.data = this.entity.serializeNBT();
		this.isFeign = isFeign;
		if (isFeign) {
			motionX = entity.motionX;
			motionY = entity.motionY;
			motionZ = entity.motionZ;
		} else {
			entity.deathTime = 0;
			entity.hurtTime = 0;
			renderYawOffset = toCopy.renderYawOffset;
			prevRotationYaw = entity.prevRotationYaw;
			rotationYaw = entity.rotationYaw;
		}
	}

	@Override
	protected void entityInit() {}

	@Override
	public boolean canBeCollidedWith() {
		return !isDead && !clientOnly;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!isEntityInvulnerable(source) && source.getImmediateSource() instanceof EntityPlayer) {
			if (data != null) entityDropItem(ItemStatue.getStatue(this), 0);
			setDead();
			return true;
		}
		return false;
	}

	@Override
	public void onUpdate() {
		if (onGround) {
			motionX *= 0.1;
			motionZ *= 0.1;
		}
		motionX *= 0.98;
		motionY *= 0.98;
		motionZ *= 0.98;
		motionY -= 0.08;
		if (!world.isRemote && ticksLeft >= 0 && ticksLeft-- == 0) setDead();
		move(MoverType.SELF, motionX, motionY, motionZ);
		if (isFeign && ticksExisted >= 20) {
			setDead();
			for (int k = 0; k < 20; ++k) {
				double d2 = rand.nextGaussian() * 0.02D;
				double d0 = rand.nextGaussian() * 0.02D;
				double d1 = rand.nextGaussian() * 0.02D;
				world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, posX + rand.nextFloat() * width * 2.0F - width,
						posY + rand.nextFloat() * height, posZ + rand.nextFloat() * width * 2.0F - width, d2, d0, d1);
			}
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		if (!TF2ConfigVars.australiumStatue) {
			setDead();
			return;
		}
		data = compound.getCompoundTag("Entity");
		ticksLeft = compound.getShort("TicksLeft");
		player = compound.getBoolean("Player");
		if (player) profile = NBTUtil.readGameProfileFromNBT(compound.getCompoundTag("Profile"));
		useHand = compound.getBoolean("UseArm");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		try {
			if (data != null) compound.setTag("Entity", data);
		} catch (Exception e) {}
		compound.setShort("TicksLeft", (short) ticksLeft);
		if (profile != null) compound.setTag("Profile", NBTUtil.writeGameProfile(new NBTTagCompound(), profile));
		compound.setBoolean("Player", player);
		compound.setBoolean("UseArm", useHand);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		if (first) return;
		PacketBuffer buff = new PacketBuffer(buffer);
		buffer.writeBoolean(player);
		if (profile != null) {
			buff.writeUniqueId(profile.getId());
			buff.writeString(profile.getName());
			buff.writeVarInt(profile.getProperties().size());
			for (Property property : profile.getProperties().values()) {
				buff.writeString(property.getName());
				buff.writeString(property.getValue());
				if (property.hasSignature()) {
					buff.writeBoolean(true);
					buff.writeString(property.getSignature());
				} else buff.writeBoolean(false);
			}
		}
		int pos = buff.writerIndex();
		buff.writeCompoundTag(data);
		if (buff.writerIndex() - pos >= 2097152) buff.clear();
		buff.writeBoolean(useHand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void readSpawnData(ByteBuf additionalData) {
		if (additionalData.readableBytes() == 0) return;
		try {
			PacketBuffer buff = new PacketBuffer(additionalData);
			if (buff.readBoolean()) {
				profile = new GameProfile(buff.readUniqueId(), buff.readString(16));
				int l = buff.readVarInt();
				int i1 = 0;
				for (; i1 < l; ++i1) {
					String s = buff.readString(32767);
					String s1 = buff.readString(32767);
					if (buff.readBoolean()) profile.getProperties().put(s, new Property(s, s1, buff.readString(32767)));
					else profile.getProperties().put(s, new Property(s, s1));
				}
				final NetworkPlayerInfo info = new NetworkPlayerInfo(profile);
				entity = new EntityOtherPlayerMP(world, profile) {
					@Override
					@Nullable
					protected NetworkPlayerInfo getPlayerInfo() {
						return info;
					}
				};
				entity.readFromNBT(buff.readCompoundTag());
			} else entity = (EntityLivingBase) EntityList.createEntityFromNBT(buff.readCompoundTag(), world);
			if (entity == null) {
				setDead();
				return;
			}
			entity.deathTime = 0;
			entity.hurtTime = 0;
			entity.limbSwingAmount = 0.5f;
			entity.ticksExisted = 15;
			entity.limbSwing += rand.nextFloat() * 10;
			setSize(entity.width, entity.height);
			entity.setPosition(posX, posY, posZ);
			entity.rotationYawHead = rotationYaw;
			entity.renderYawOffset = rotationYaw;
			entity.prevRenderYawOffset = rotationYaw;
			if (buff.readBoolean()) WeaponsCapability.get(entity).state = 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
