package rafradek.TF2weapons.item;

import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.mercenary.*;

import java.util.List;

@SuppressWarnings("deprecation")
public class ItemMonsterPlacerPlus extends Item {

	public ItemMonsterPlacerPlus() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabspawnertf2);
	}

	/**
	 * Callback for item usage. If the item does something special on right
	 * clicking, he will have one of those. Return True if something happen and
	 * false if it don't. This is for ITEMS, not BLOCKS
	 */
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = playerIn.getHeldItem(hand);
		if (worldIn.isRemote)
			return EnumActionResult.SUCCESS;
		else if (!playerIn.canPlayerEdit(pos.offset(facing), facing, stack))
			return EnumActionResult.FAIL;
		else {
			IBlockState iblockstate = worldIn.getBlockState(pos);

			pos = pos.offset(facing);
			double d0 = 0.0D;

			if (facing == EnumFacing.UP && iblockstate.getBlock() instanceof BlockFence)
				d0 = 0.5D;

			boolean hastag = stack.getTagCompound() != null && stack.getTagCompound().hasKey("SavedEntity");

			EntityLivingBase entity = spawnCreature(playerIn, worldIn, stack.getItemDamage(), pos.getX() + 0.5D,
					pos.getY() + d0, pos.getZ() + 0.5D,
					hastag ? stack.getTagCompound().getCompoundTag("SavedEntity") : null);

			if (entity != null) {
				if (entity instanceof EntityLivingBase && stack.hasDisplayName())
					entity.setCustomNameTag(stack.getDisplayName());

				if (!playerIn.capabilities.isCreativeMode)
					stack.shrink(1);
				if (entity instanceof EntityBuilding) {
					if (entity instanceof EntityTeleporter || !(playerIn.isCreative() && playerIn.isSneaking()))
						((EntityBuilding) entity).setOwner(playerIn);
					if (hastag) {
						((EntityBuilding) entity).setConstructing(true);
						((EntityBuilding) entity).redeploy = true;
					}
					entity.rotationYaw = playerIn.rotationYawHead;
					entity.renderYawOffset = playerIn.rotationYawHead;
					entity.rotationYawHead = playerIn.rotationYawHead;
					if (entity instanceof EntityTeleporter)
						((EntityTeleporter) entity).setExit(stack.getItemDamage() > 23);
				}
			}

			return EnumActionResult.SUCCESS;
		}
	}

	/**
	 * Applies the data in the EntityTag tag of the given ItemStack to the given
	 * Entity.
	 */

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (worldIn.isRemote)
			return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
		else {
			RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);

			if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
				BlockPos blockpos = raytraceresult.getBlockPos();

				if (!(worldIn.getBlockState(blockpos).getBlock() instanceof BlockLiquid))
					return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
				else if (worldIn.isBlockModifiable(playerIn, blockpos)
						&& playerIn.canPlayerEdit(blockpos, raytraceresult.sideHit, itemStackIn)) {

					boolean hastag = itemStackIn.getTagCompound() != null
							&& itemStackIn.getTagCompound().hasKey("SavedEntity");

					EntityLivingBase entity = spawnCreature(playerIn, worldIn, itemStackIn.getItemDamage(),
							blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D,
							hastag ? itemStackIn.getTagCompound().getCompoundTag("SavedEntity") : null);

					if (entity == null)
						return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
					else {
						if (entity instanceof EntityLivingBase && itemStackIn.hasDisplayName())
							entity.setCustomNameTag(itemStackIn.getDisplayName());

						if (!playerIn.capabilities.isCreativeMode)
							itemStackIn.shrink(1);
						if (entity instanceof EntityBuilding) {
							if (entity instanceof EntityTeleporter || !(playerIn.isCreative() && playerIn.isSneaking()))
								((EntityBuilding) entity).setOwner(playerIn);
							if (hastag) {
								((EntityBuilding) entity).setConstructing(true);
								((EntityBuilding) entity).redeploy = true;
							}
							if (entity instanceof EntitySentry && itemStackIn.hasTagCompound()
									&& itemStackIn.getTagCompound().getBoolean("Mini"))
								((EntitySentry) entity).setMini(true);

							entity.rotationYaw = playerIn.rotationYawHead;
							entity.renderYawOffset = playerIn.rotationYawHead;
							entity.rotationYawHead = playerIn.rotationYawHead;

							/*
							 * if(entity instanceof EntityTeleporter){ ((EntityTeleporter)
							 * entity).setExit(itemStackIn.getItemDamage()>23); }
							 */
						}
						playerIn.addStat(StatList.getObjectUseStats(this));
						return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
					}
				} else
					return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
			} else
				return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
		}
	}

	public static EntityLiving spawnCreature(Entity spawner, World par0World, int par1, double par2, double par4,
			double par6, NBTTagCompound nbtdata) {
		EntityLiving entity = null;

		for (int j = 0; j < 1; ++j) {
			int team = par1 % 2;
			if (par1 < 18 || (par1 >= 36 && par1 < 45)) {
				switch (par1 % 9) {
				case 0:
					entity = new EntityScout(par0World);
					break;
				case 1:
					entity = new EntitySoldier(par0World);
					break;
				case 2:
					entity = new EntityPyro(par0World);
					break;
				case 3:
					entity = new EntityDemoman(par0World);
					break;
				case 4:
					entity = new EntityHeavy(par0World);
					break;
				case 5:
					entity = new EntityEngineer(par0World);
					break;
				case 6:
					entity = new EntityMedic(par0World);
					break;
				case 7:
					entity = new EntitySniper(par0World);
					break;
				case 8:
					entity = new EntitySpy(par0World);
					break;
				}
				team = par1 / 9;
				if (par1 >= 36)
					team = 2;
			} else if (par1 / 2 == 9)
				entity = new EntitySentry(par0World);
			else if (par1 / 2 == 10)
				entity = new EntityDispenser(par0World);
			else if (par1 / 2 == 11)
				entity = new EntityTeleporter(par0World);
			else if (par1 / 2 == 13)
				entity = new EntitySaxtonHale(par0World);
			else if (par1 == 28)
				entity = new EntityMonoculus(par0World);
			else if (par1 == 29)
				entity = new EntityHHH(par0World);
			else if (par1 == 30)
				entity = new EntityMerasmus(par0World);
			if (entity != null) {
				EntityLiving entityliving = entity;
				if (nbtdata != null)
					entityliving.readFromNBT(nbtdata);
				// System.out.println("read");
				entity.setLocationAndAngles(par2, par4, par6,
						MathHelper.wrapDegrees(par0World.rand.nextFloat() * 360.0F), 0.0F);
				entityliving.rotationYawHead = entityliving.rotationYaw;
				entityliving.renderYawOffset = entityliving.rotationYaw;
				entityliving.enablePersistence();
				TF2CharacterAdditionalData data = new TF2CharacterAdditionalData();
				data.team = team;
				data.noEquipment = team < 2 && spawner != null && spawner.isSneaking();
				data.isGiant = team == 2 && spawner != null && spawner.isSneaking();
				if (nbtdata == null)
					entityliving.onInitialSpawn(par0World.getDifficultyForLocation(new BlockPos(entityliving)), data);
				entityliving.playLivingSound();
				if (entity instanceof EntityBuilding)
					((EntityBuilding) entity).setEntTeam(team);
				if (entity instanceof EntitySaxtonHale && par1 % 2 == 1)
					((EntitySaxtonHale) entity).setHostile();
				if (!par0World.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty())
					return null;
				par0World.spawnEntity(entity);

			}

		}

		return entity;
	}

	@Override
	@SideOnly(Side.CLIENT)

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns
	 * 16 items)
	 */
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 18; i++)
			par3List.add(new ItemStack(this, 1, i));
		for (int i = 36; i < 45; i++)
			par3List.add(new ItemStack(this, 1, i));
		par3List.add(new ItemStack(this, 1, 26));
		par3List.add(new ItemStack(this, 1, 27));
		par3List.add(new ItemStack(this, 1, 28));
		par3List.add(new ItemStack(this, 1, 29));
		par3List.add(new ItemStack(this, 1, 30));

	}

	@Override
	public String getItemStackDisplayName(ItemStack p_77653_1_) {
		String s = ("" + I18n.translateToLocal(this.getUnlocalizedName() + ".name")).trim();
		int i = p_77653_1_.getItemDamage();
		String s1 = "hale";
		if (i < 18 || (i >= 36 && i < 45))
			s1 = ItemToken.CLASS_NAMES[i % 9];
		if (p_77653_1_.getItemDamage() == 28)
			s1 = "monoculus";
		if (p_77653_1_.getItemDamage() == 29)
			s1 = "hhh";
		if (p_77653_1_.getItemDamage() == 30)
			s1 = "merasmus";
		s1 = I18n.translateToLocal("entity." + s1 + ".name");
		if (p_77653_1_.getItemDamage() == 27)
			s1 = s1.concat(" " + I18n.translateToLocal("item." + TF2weapons.MOD_ID + ".placer.hostile"));
		return s.concat(" " + s1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		if (stack.getMetadata() < 18)
			tooltip.add("Hold " + KeyBinding.getDisplayString("key.sneak").get() + " to spawn with default equipment");
		if (stack.getMetadata() >= 36 && stack.getMetadata() < 45)
			tooltip.add("Hold " + KeyBinding.getDisplayString("key.sneak").get() + " to spawn a giant");
	}
}
