package rafradek.TF2weapons.message.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import rafradek.TF2weapons.TF2weapons;

import java.lang.reflect.Constructor;

public class UdpChannelHandlerClient extends SimpleChannelInboundHandler<DatagramPacket> {

	private Constructor<MessageContext> constr;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		ByteBuf buffer = msg.content();

		int seq = buffer.readUnsignedShort();
		int msgid = buffer.readByte();

		IMessage message = TF2weapons.network.messages[msgid].newInstance();
		// buffer.discardReadBytes();
		message.fromBytes(buffer);
		IMessageHandler<IMessage, IMessage> handler = TF2weapons.network.handlerList.get(message.getClass());
		if (constr == null) {
			constr = MessageContext.class.getDeclaredConstructor(INetHandler.class, Side.class);
			constr.setAccessible(true);
		}
		MessageContext context = constr.newInstance(Minecraft.getMinecraft().player.connection, Side.CLIENT);
		handler.onMessage(message, context);
		// System.out.println("PacketFrom: "+msg.sender().getAddress()+ "
		// "+msg.sender().getPort()+" ");

	}

}
