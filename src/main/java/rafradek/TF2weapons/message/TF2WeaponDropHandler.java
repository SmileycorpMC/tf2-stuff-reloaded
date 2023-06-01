package rafradek.TF2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.client.ClientHandler;
import rafradek.TF2weapons.message.TF2Message.WeaponDroppedMessage;

public class TF2WeaponDropHandler implements IMessageHandler<TF2Message.WeaponDroppedMessage, IMessage> {

	@Override
	public IMessage onMessage(final WeaponDroppedMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> ClientHandler.playerDropWeapon(message.name));
		return null;
	}

}
