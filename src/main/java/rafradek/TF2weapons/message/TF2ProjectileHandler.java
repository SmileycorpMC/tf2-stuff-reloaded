package rafradek.TF2weapons.message;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;

import java.util.ArrayList;
import java.util.Deque;

public class TF2ProjectileHandler implements IMessageHandler<TF2Message.PredictionMessage, IMessage> {

	// public static HashMap<Entity, ArrayList<PredictionMessage>> nextShotPos=
	// new HashMap<Entity, ArrayList<PredictionMessage>>();

	@Override
	public IMessage onMessage(final PredictionMessage message, MessageContext ctx) {
		final EntityPlayer shooter = ctx.getServerHandler().player;
		// ItemStack stack=shooter.getHeldItem(EnumHand.MAIN_HAND);
		((WorldServer) shooter.world).addScheduledTask(() -> {
			// shooter.getCapability(TF2weapons.WEAPONS_CAP, null).predictionList.poll();
			message.target = new ArrayList<>();
			if (message.readData != null)
				for (Object[] obj : message.readData) {
					RayTraceResult result;

					if (obj[0] != null) {
						Entity entity = shooter.world.getEntityByID((int) obj[0]);
						Vec3d hit1 = new Vec3d((Byte) obj[6] / 16D + entity.posX, (Byte) obj[7] / 16D + entity.posY,
								(Byte) obj[8] / 16D + entity.posZ);
						result = new RayTraceResult(entity, hit1);
						result.hitInfo = new float[] { (Boolean) obj[1] ? 1f : 0f, (Float) obj[2] };
					} else {
						BlockPos pos = new BlockPos((Integer) obj[3], (Integer) obj[4], (Integer) obj[5]);
						Vec3d hit2 = new Vec3d((Byte) obj[6] / 16D + pos.getX(), (Byte) obj[7] / 16D + pos.getY(),
								(Byte) obj[8] / 16D + pos.getZ());
						result = new RayTraceResult(hit2, EnumFacing.getFront((Byte) obj[1]), pos);
						result.hitInfo = new float[] { (Float) obj[2] };
					}
					message.target.add(result);
				}
			Deque<PredictionMessage> deque = shooter.getCapability(TF2weapons.WEAPONS_CAP,
					null).predictionList[(message.state == 1 ? 0 : 2) + message.hand.ordinal()];
			deque.addLast(message);
			message.time = shooter.world.getTotalWorldTime();
		});
		return null;
	}

}
