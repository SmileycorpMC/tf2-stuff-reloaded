package rafradek.TF2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.message.TF2Message.ParticleSpawnMessage;

public class TF2ParticleSpawnHandler implements IMessageHandler<ParticleSpawnMessage, IMessage> {

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(final ParticleSpawnMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			ClientHandler.processParticleSpawnMessage(message);
		});
		return null;
	}

}
