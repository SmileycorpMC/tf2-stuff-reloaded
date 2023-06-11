package rafradek.TF2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.ContractMessage;
import rafradek.TF2weapons.util.Contract;

import java.util.ArrayList;

public class TF2ContractHandler implements IMessageHandler<TF2Message.ContractMessage, IMessage> {

	public static int size;

	@Override
	public IMessage onMessage(final ContractMessage message, MessageContext ctx) {

		if (Minecraft.getMinecraft().player == null)
			return null;
		ArrayList<Contract> contracts = Minecraft.getMinecraft().player.getCapability(TF2weapons.PLAYER_CAP,
				null).contracts;
		if (message.id == -1) {
			contracts.add(message.contract);
			Minecraft.getMinecraft().player.getCapability(TF2weapons.PLAYER_CAP, null).newContracts = true;
		} else if (contracts.size() <= message.id) {
			contracts.add(message.id, message.contract);
			if (message.contract.rewards > 0) {
				Minecraft.getMinecraft().player.getCapability(TF2weapons.PLAYER_CAP, null).newRewards = true;
			}
		} else {
			Contract prev = contracts.set(message.id, message.contract);
			if (prev.rewards == 0 && message.contract.rewards > 0) {
				Minecraft.getMinecraft().player.getCapability(TF2weapons.PLAYER_CAP, null).newRewards = true;
			}
		}
		/*
		 * Minecraft.getMinecraft().addScheduledTask(new Runnable(){
		 * 
		 * @Override public void run() { //System.out.println("Wep drop "+message.name);
		 * ItemStack stack=ItemFromData.getNewStack(message);
		 * ((ItemUsable)stack.getItem()).holster(Minecraft.getMinecraft().player.
		 * getCapability(TF2weapons.WEAPONS_CAP, null), stack,
		 * Minecraft.getMinecraft().player, Minecraft.getMinecraft().world);
		 * 
		 * }
		 * 
		 * });
		 */

		return null;
	}

}
