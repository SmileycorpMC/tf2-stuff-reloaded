package rafradek.TF2weapons.message;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.message.TF2Message.WeaponDataMessage;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.WeaponData;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class TF2WeaponDataHandler implements IMessageHandler<TF2Message.WeaponDataMessage, IMessage> {

	public static int size;

	@Override
	public IMessage onMessage(final WeaponDataMessage message, MessageContext ctx) {
		DataInputStream input;
		try {
			input = new DataInputStream(new BufferedInputStream(
					new GZIPInputStream(new ByteArrayInputStream(message.bytes, 0, message.bytes.length))));

			MapList.nameToData.clear();
			MapList.buildInAttributes.clear();

			while (input.available() > 0) {
				WeaponData weapon = new WeaponData(input.readUTF());
				int propertyCount = input.readByte();
				for (int i = 0; i < propertyCount; i++) {
					int propId = input.readByte();
					PropertyType<?> prop = WeaponData.propertyTypes[propId];
					weapon.properties.put(prop, prop.deserialize(input, weapon));
				}

				TF2weapons.loadWeapon(weapon.getName(), weapon);
				ClientProxy.RegisterWeaponData(weapon);

			}
			input.close();
		} catch (IOException e) {}
		return null;
	}

}
