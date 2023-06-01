package rafradek.TF2weapons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.EntityLivingBase;
import rafradek.TF2weapons.client.particle.EnumTF2Particles;
import rafradek.TF2weapons.message.TF2Message.ParticleSpawnMessage;

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

}
