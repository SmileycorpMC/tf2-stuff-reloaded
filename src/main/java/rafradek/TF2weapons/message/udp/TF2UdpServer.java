package rafradek.TF2weapons.message.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import net.minecraft.entity.player.EntityPlayerMP;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class TF2UdpServer extends Thread {

	public HashMap<Short, EntityPlayerMP> playerList;
	public HashMap<Short, InetSocketAddress> outboundTargets;
	public int port;
	public static short nextPlayerId;
	public DatagramChannel channel;
	public final EventLoopGroup group = new NioEventLoopGroup();

	public TF2UdpServer(int port) {
		this.port = port;
		this.playerList = new HashMap<>();
		this.outboundTargets = new HashMap<>();
	}

	@Override
	public void run() {
		try {
			Bootstrap boot = new Bootstrap();
			boot.group(group).channel(NioDatagramChannel.class).handler(new ChannelInitializer<DatagramChannel>() {

				@Override
				protected void initChannel(DatagramChannel ch) throws Exception {
					channel = ch;
					ch.pipeline().addLast(new UdpChannelHandlerServer(TF2UdpServer.this));
				}

			});
			boot.bind(port).sync().channel().closeFuture();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void stopServer() {
		this.channel.disconnect();
		this.group.shutdownGracefully();
	}
}
