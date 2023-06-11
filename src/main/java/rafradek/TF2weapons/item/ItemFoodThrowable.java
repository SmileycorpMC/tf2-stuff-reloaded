package rafradek.TF2weapons.item;

import com.google.common.collect.Iterables;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2ConfigVars;

public class ItemFoodThrowable extends ItemFood {

	public int waitTime;

	public ItemFoodThrowable(int amount, float saturation, boolean isWolfFood, int waitTime) {
		super(amount, saturation, isWolfFood);
		this.waitTime = waitTime;
		this.setAlwaysEdible();
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if (entityItem.getItem().hasTagCompound() && entityItem.getItem().getTagCompound().getBoolean("IsHealing")) {
			EntityLivingBase living = Iterables.getFirst(entityItem.world.getEntitiesWithinAABB(EntityLivingBase.class,
					entityItem.getEntityBoundingBox(), test -> (!(test instanceof EntityPlayer)
							&& test.getHealth() < test.getMaxHealth() && test.isNonBoss() && test.isEntityAlive())),
					null);

			if (living != null) {
				living.heal(living.getMaxHealth() * this.getHealAmount(entityItem.getItem()) / 28f);
				entityItem.getItem().shrink(1);
				if (entityItem.getItem().getTagCompound().getBoolean("IsHealing"))
					entityItem.getItem().setTagCompound(null);
			}
		}
		return false;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {

		if (!TF2ConfigVars.fastItemCooldown) {
			/*
			 * if (!player.getCooldownTracker().hasCooldown(this)) {
			 * player.getCooldownTracker().setCooldown(this, waitTime); if
			 * (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
			 * item.getTagCompound().setBoolean("IsHealing", true); }
			 */
		}
		return true;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		super.onFoodEaten(stack, worldIn, player);
		if (!worldIn.isRemote) {
			player.getCooldownTracker().setCooldown(this, TF2ConfigVars.fastItemCooldown ? waitTime / 2 : waitTime);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack previous = playerIn.getHeldItem(handIn);
		ActionResult<ItemStack> result = super.onItemRightClick(worldIn, playerIn, handIn);
		if (TF2ConfigVars.freeUseItems)
			result.getResult().setCount(previous.getCount());
		return result;
	}
}
