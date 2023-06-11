package rafradek.TF2weapons.message.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.net.InetSocketAddress;

public class TF2UdpClient {

	public static TF2UdpClient instance;
	public static String addressToUse = "127.0.0.1";
	public static int playerId;

	public InetSocketAddress address;
	public Channel channel;
	private EventLoopGroup group;

	public TF2UdpClient(InetSocketAddress address) throws Exception {
		group = new NioEventLoopGroup();
		this.address = address;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		Bootstrap boot = new Bootstrap();
		boot.group(group).channel(NioDatagramChannel.class).handler(new UdpChannelHandlerClient());

		channel = boot.bind(0).sync().channel();

		channel.connect(address);

		/*
		 * PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		 * buffer.writeShort(playerId); buffer.writeShort(0); buffer.writeByte(0);
		 * buffer.writeLong(System.currentTimeMillis());
		 * 
		 * channel.writeAndFlush(new DatagramPacket(buffer, address));
		 */
	}

	public void shutdown() {
		channel.close();
		group.shutdownGracefully();
	}
}
