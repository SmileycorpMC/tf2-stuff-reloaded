package rafradek.TF2weapons.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.udp.TF2UdpClient;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;

public class TF2NetworkWrapper extends SimpleNetworkWrapper {

	public boolean useUdp;

	public HashMap<Class<? extends IMessage>, IMessageHandler<IMessage, IMessage>> handlerList;

	public HashSet<IMessageHandler<IMessage, IMessage>> udpEnabled;

	public HashMap<Class<? extends IMessage>, Byte> discriminators;

	@SuppressWarnings("unchecked")
	public Class<? extends IMessage>[] messages = new Class[256];

	public TF2NetworkWrapper(String channelName) {
		super(channelName);
		udpEnabled = new HashSet<>();
		handlerList = new HashMap<>();
		discriminators = new HashMap<>();
	}

	public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
			Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType,
			int discriminator, Side side, boolean useUdp) {
		try {
			registerMessage(messageHandler.newInstance(), requestMessageType, discriminator, side, useUdp);
		} catch (Exception e) {

		}
	}

	@SuppressWarnings("unchecked")
	public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
			IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestMessageType,
			int discriminator, Side side, boolean useUdp) {
		super.registerMessage(messageHandler, requestMessageType, discriminator, side);
		if (useUdp) {
			handlerList.put(requestMessageType, (IMessageHandler<IMessage, IMessage>) messageHandler);
			messages[discriminator] = requestMessageType;
			discriminators.put(requestMessageType, (byte) discriminator);

		}
	}

	@Override
	public void sendToAll(IMessage message) {

		if (useUdp && discriminators.containsKey(message.getClass())) {
			for (EntityPlayer player : TF2weapons.server.getPlayerList().getPlayers()) {
				InetSocketAddress address = TF2weapons.udpServer.outboundTargets
						.get(player.getCapability(TF2weapons.PLAYER_CAP, null).udpServerId);
				if (address != null) {
					ByteBuf buffer = Unpooled.buffer();
					buffer.writeShort(0);
					buffer.writeByte(discriminators.get(message.getClass()));
					message.toBytes(buffer);
					DatagramPacket packet = new DatagramPacket(buffer, address);
					TF2weapons.udpServer.channel.writeAndFlush(packet);
				} else {
					super.sendTo(message, (EntityPlayerMP) player);
				}
			}
		} else {
			super.sendToAll(message);
		}
	}

	@Override
	public void sendTo(IMessage message, EntityPlayerMP player) {
		if (useUdp && discriminators.containsKey(message.getClass())) {
			InetSocketAddress address = TF2weapons.udpServer.outboundTargets
					.get(player.getCapability(TF2weapons.PLAYER_CAP, null).udpServerId);
			if (address != null) {
				ByteBuf buffer = Unpooled.buffer();
				buffer.writeShort(0);
				buffer.writeByte(discriminators.get(message.getClass()));
				message.toBytes(buffer);
				DatagramPacket packet = new DatagramPacket(buffer, address);
				TF2weapons.udpServer.channel.writeAndFlush(packet);
			}
		} else {
			super.sendTo(message, player);
		}
	}

	@Override
	public void sendToServer(IMessage message) {
		if (useUdp && TF2UdpClient.instance != null && discriminators.containsKey(message.getClass())) {
			InetSocketAddress address = TF2UdpClient.instance.address;
			// System.out.println("Addr: "+address);
			if (address != null) {
				ByteBuf buffer = Unpooled.buffer();
				buffer.writeShort(TF2UdpClient.playerId);
				buffer.writeShort(0);
				buffer.writeByte(discriminators.get(message.getClass()));
				message.toBytes(buffer);
				DatagramPacket packet = new DatagramPacket(buffer, address);
				TF2UdpClient.instance.channel.writeAndFlush(packet);
			}
		} else {
			super.sendToServer(message);
		}
	}
}
