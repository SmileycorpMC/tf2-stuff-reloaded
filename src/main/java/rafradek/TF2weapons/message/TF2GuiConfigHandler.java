package rafradek.TF2weapons.message;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.message.TF2Message.GuiConfigMessage;
import rafradek.TF2weapons.tileentity.IEntityConfigurable;

public class TF2GuiConfigHandler implements IMessageHandler<TF2Message.GuiConfigMessage, IMessage> {

	public static HashMap<Entity, float[]> shotInfo = new HashMap<>();

	@Override
	public IMessage onMessage(final GuiConfigMessage message, final MessageContext ctx) {

		if (ctx.side == Side.SERVER) {
			final EntityPlayerMP player = ctx.getServerHandler().player;
			((WorldServer) player.world).addScheduledTask(() -> {
				TileEntity ent = player.world.getTileEntity(message.getPos());
				if (ent instanceof IEntityConfigurable) {
					((IEntityConfigurable) ent).readConfig(message.getTag());
				}
				/*
				 * if (player.openContainer instanceof ContainerConfigurable) {
				 * ((ContainerConfigurable)player.openContainer).config.readConfig(message.tag);
				 * }
				 */
			});
		} else ClientHandler.createConfigGui(message);

		return null;
	}

}
