package rafradek.TF2weapons.contract;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message;

public class PlayerContractsProxy extends NetHandlerPlayServer {

	private NetHandlerPlayServer origin;

	public PlayerContractsProxy(NetHandlerPlayServer origin, MinecraftServer server, EntityPlayerMP player) {
		super(server, new NetworkManager(EnumPacketDirection.SERVERBOUND), player);
		this.origin = origin;
	}

	@Override
	public void sendPacket(Packet<?> packet) {
		TF2weapons.network.sendTo(new TF2Message.ContractNewMessage(packet), player);
		origin.sendPacket(packet);
	}
}
