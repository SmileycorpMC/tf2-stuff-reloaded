package rafradek.TF2weapons.message.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.SocketUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import rafradek.TF2weapons.TF2weapons;

import java.lang.reflect.Constructor;

public class UdpChannelHandlerServer extends SimpleChannelInboundHandler<DatagramPacket> {

	public TF2UdpServer server;

	private Constructor<MessageContext> constr;

	public UdpChannelHandlerServer(TF2UdpServer server) {
		this.server = server;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		ByteBuf buffer = msg.content();

		int plid = buffer.readShort();
		int seq = buffer.readUnsignedShort();
		int msgid = buffer.readByte();

		if (msgid == (byte) -128) {
			server.outboundTargets.put((short) plid,
					SocketUtils.socketAddress(msg.sender().getAddress().getHostAddress(), msg.sender().getPort()));
		} else {
			EntityPlayerMP player = server.playerList.get((short) plid);
			IMessage message = TF2weapons.network.messages[msgid].newInstance();
			// buffer.discardReadBytes();
			message.fromBytes(buffer);
			IMessageHandler<IMessage, IMessage> handler = TF2weapons.network.handlerList.get(message.getClass());
			if (constr == null) {
				constr = MessageContext.class.getDeclaredConstructor(INetHandler.class, Side.class);
				constr.setAccessible(true);
			}
			MessageContext context = constr.newInstance(player.connection, Side.SERVER);
			handler.onMessage(message, context);
		}
		// System.out.println("PacketFrom: "+msg.sender().getAddress()+ "
		// "+msg.sender().getPort()+" ");

	}

}
