package rafradek.TF2weapons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.gui.GuiConfigurable2;
import rafradek.TF2weapons.client.gui.GuiTeamSelect;
import rafradek.TF2weapons.client.particle.EnumTF2Particles;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.message.TF2Message.GuiConfigMessage;
import rafradek.TF2weapons.message.TF2Message.ParticleSpawnMessage;
import rafradek.TF2weapons.message.TF2Message.ShowGuiMessage;
import rafradek.TF2weapons.util.TF2Util;

import java.util.List;

public class ClientHandler {

	public static void processParticleSpawnMessage(ParticleSpawnMessage message) {
		IParticleFactory factory = ClientProxy.particleFactories.get(EnumTF2Particles.values()[message.id]);
		for (int i = 0; i < message.count; i++) {
			ParticleSpawnMessage message2 = message;
			Particle particle = factory.createParticle(message.id, Minecraft.getMinecraft().world, message.x,
					message.y, message.z, message.offsetX, message.offsetY, message.offsetZ, message.params);
			ClientProxy.spawnParticle(Minecraft.getMinecraft().world, particle);
		}
	}

	public static EntityLivingBase getClientEntity(int id) {
		return (EntityLivingBase) Minecraft.getMinecraft().world.getEntityByID(id);
	}

	public static void playerDropWeapon(String name) {
		ItemStack stack = ItemFromData.getNewStack(name);
		((ItemUsable) stack.getItem()).holster(
				Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null), stack,
				Minecraft.getMinecraft().player, Minecraft.getMinecraft().world);
	}

	public static void createConfigGui(GuiConfigMessage message) {
		Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(GuiConfigurable2.create(message.getTag(), message.getPos())));
	}

	public static void createGui(ShowGuiMessage message) {
		NBTTagList listTeams = message.data.getTagList("Teams", 8);
		List<String> teams = TF2Util.NBTTagListToList(listTeams, String.class);
		int[] counts = message.data.getIntArray("Count");
		boolean[] allowed = new boolean[counts.length];
		byte[] allowedbyte = message.data.getByteArray("Allowed");
		for (int i = 0; i < allowed.length; i++) {
			allowed[i] = allowedbyte[i] == 1;
		}
		Minecraft.getMinecraft().addScheduledTask(
				() -> Minecraft.getMinecraft().displayGuiScreen(new GuiTeamSelect(teams, counts, allowed)));
	}

}
