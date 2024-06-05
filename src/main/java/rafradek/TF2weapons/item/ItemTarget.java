package rafradek.TF2weapons.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.EntityTarget;

import java.util.List;
import java.util.Random;

public class ItemTarget extends Item {

	public ItemTarget() {
		this.setCreativeTab(TF2weapons.tabutilitytf2);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (facing == EnumFacing.DOWN) {
			return EnumActionResult.FAIL;
		} else {
			boolean flag = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
			BlockPos blockpos = flag ? pos : pos.offset(facing);
			ItemStack itemstack = player.getHeldItem(hand);

			if (!player.canPlayerEdit(blockpos, facing, itemstack)) {
				return EnumActionResult.FAIL;
			} else {
				BlockPos blockpos1 = blockpos.up();
				boolean flag1 = !world.isAirBlock(blockpos)
						&& !world.getBlockState(blockpos).getBlock().isReplaceable(world, blockpos);
				flag1 = flag1 | (!world.isAirBlock(blockpos1)
						&& !world.getBlockState(blockpos1).getBlock().isReplaceable(world, blockpos1));

				if (flag1) {
					return EnumActionResult.FAIL;
				} else {
					double d0 = blockpos.getX();
					double d1 = blockpos.getY();
					double d2 = blockpos.getZ();
					List<Entity> list = world.getEntitiesWithinAABBExcludingEntity((Entity) null,
							new AxisAlignedBB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));

					if (!list.isEmpty()) {
						return EnumActionResult.FAIL;
					} else {
						if (!world.isRemote) {
							world.setBlockToAir(blockpos);
							world.setBlockToAir(blockpos1);
							EntityTarget entityarmorstand = new EntityTarget(world, d0 + 0.5D, d1, d2 + 0.5D,
									player.capabilities.isCreativeMode);
							float f = MathHelper.floor(
									(MathHelper.wrapDegrees(player.rotationYaw - 180.0F) + 22.5F) / 45.0F) * 45.0F;
							entityarmorstand.setLocationAndAngles(d0 + 0.5D, d1, d2 + 0.5D, f, 0.0F);
							this.applyRandomRotations(entityarmorstand, world.rand);
							ItemMonsterPlacer.applyItemEntityDataToEntity(world, player, itemstack, entityarmorstand);
							world.spawnEntity(entityarmorstand);
							world.playSound((EntityPlayer) null, entityarmorstand.posX, entityarmorstand.posY,
									entityarmorstand.posZ, SoundEvents.ENTITY_ARMORSTAND_PLACE, SoundCategory.BLOCKS,
									0.75F, 0.8F);
						}

						itemstack.shrink(1);
						return EnumActionResult.SUCCESS;
					}
				}
			}
		}
	}

	private void applyRandomRotations(EntityArmorStand armorStand, Random rand) {
		Rotations rotations = armorStand.getHeadRotation();
		float f = rand.nextFloat() * 5.0F;
		float f1 = rand.nextFloat() * 20.0F - 10.0F;
		Rotations rotations1 = new Rotations(rotations.getX() + f, rotations.getY() + f1, rotations.getZ());
		armorStand.setHeadRotation(rotations1);
		rotations = armorStand.getBodyRotation();
		f = rand.nextFloat() * 10.0F - 5.0F;
		rotations1 = new Rotations(rotations.getX(), rotations.getY() + f, rotations.getZ());
		armorStand.setBodyRotation(rotations1);
	}

}
