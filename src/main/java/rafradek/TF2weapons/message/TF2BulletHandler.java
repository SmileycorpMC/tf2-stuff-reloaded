package rafradek.TF2weapons.message;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.message.TF2Message.BulletMessage;

import java.util.HashMap;

public class TF2BulletHandler implements IMessageHandler<TF2Message.BulletMessage, IMessage> {

	public static HashMap<Entity, float[]> shotInfo = new HashMap<>();

	@Override
	public IMessage onMessage(final BulletMessage message, MessageContext ctx) {
		/*
		 * final EntityPlayer shooter=ctx.getServerHandler().player;
		 * ((WorldServer)shooter.world).addScheduledTask(new Runnable(){
		 *
		 * @Override public void run() { ItemStack
		 * stack=message.hand==EnumHand.MAIN_HAND?shooter.inventory.
		 * getStackInSlot(message.slot):shooter.getHeldItemOffhand(); if(stack == null
		 * || !(stack.getItem() instanceof ItemBulletWeapon)) return; ItemBulletWeapon
		 * item=(ItemBulletWeapon) stack.getItem(); int totalCrit=0;
		 * if((!item.rapidFireCrits(stack)&&item.hasRandomCrits(stack,shooter) &&
		 * shooter.getRNG().nextFloat()<item.critChance(stack,
		 * shooter))||stack.getTagCompound().getShort("crittime")>0){ totalCrit=2; }
		 * for(Object[] obj:message.readData){ Entity
		 * target=shooter.world.getEntityByID((Integer) obj[0]); if(target==null)
		 * continue;
		 *
		 * if(!shotInfo.containsKey(target)||shotInfo.get(target)==null){
		 * shotInfo.put(target, new float[3]); } int critical=totalCrit;
		 * //System.out.println(var4.hitInfo); if((Boolean)obj[1]){ critical=2; }
		 * critical=item.setCritical(stack, shooter, target, critical); if(critical>0){
		 * totalCrit=critical; } //ItemRangedWeapon.critical=critical; float[]
		 * values=shotInfo.get(target); //System.out.println(obj[2]+" "+critical);
		 * values[0]++; values[1]+=TF2weapons.calculateDamage(target,shooter.world,
		 * shooter, stack, critical, (Float) obj[2]); }
		 * ItemBulletWeapon.handleShoot(shooter, stack, shooter.world,
		 * shotInfo,totalCrit); ItemBulletWeapon.processShotServer=true;
		 * ((ItemBulletWeapon)stack.getItem()).use(stack, shooter, shooter.world,
		 * message.hand, message); ItemBulletWeapon.processShotServer=false;
		 * shotInfo.clear(); }
		 *
		 * });
		 */
		return null;
	}

}
