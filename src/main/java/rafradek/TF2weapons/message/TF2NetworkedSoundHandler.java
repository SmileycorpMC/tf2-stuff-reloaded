package rafradek.TF2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.client.audio.NetworkedSound;
import rafradek.TF2weapons.message.TF2Message.NetworkedSoundMessage;

import java.util.HashMap;

public class TF2NetworkedSoundHandler implements IMessageHandler<TF2Message.NetworkedSoundMessage, IMessage> {

	HashMap<Integer, ISound> sounds = new HashMap<>();

	@Override
	public IMessage onMessage(final NetworkedSoundMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			if (sounds.containsKey(message.id))
				sounds.remove(message.id);
			NetworkedSound sound;
			if (message.pos != null)
				sound = new NetworkedSound(message.pos, message.event, message.category, message.volume, message.pitch,
						message.id, message.repeat);
			else {
				Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.target);
				sound = new NetworkedSound(entity, message.event, message.category, message.volume, message.pitch,
						message.id, message.repeat);
			}
			sounds.put(message.id, sound);
			Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound);
		});
		return null;
	}

}
