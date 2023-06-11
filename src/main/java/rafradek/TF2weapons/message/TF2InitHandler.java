package rafradek.TF2weapons.message;

import io.netty.util.internal.SocketUtils;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.message.udp.TF2UdpClient;

import java.util.Map.Entry;

public class TF2InitHandler implements IMessageHandler<TF2Message.InitMessage, IMessage> {

	@Override
	public IMessage onMessage(TF2Message.InitMessage message, MessageContext ctx) {
		try {
			if (message.port != -1) {
				TF2UdpClient.instance = new TF2UdpClient(
						SocketUtils.socketAddress(TF2UdpClient.addressToUse, message.port));
				TF2UdpClient.playerId = message.id;
				TF2weapons.network.useUdp = false;
			}
			for (Entry<String, Property> entry : message.property.entries()) {
				TF2weapons.conf.getCategory(entry.getKey()).get(entry.getValue().getName())
						.set(entry.getValue().getString());
				TF2ConfigVars.createConfig(false);
			}
			ClientProxy.buildingsUseEnergy = message.energyUse;

		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("setting "+message.value);
		// TF2weapons.proxy.playReloadSound(player,stack);
		return null;
	}
}
