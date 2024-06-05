package rafradek.TF2weapons.item;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

import javax.annotation.Nullable;

public class ItemParachute extends ItemBackpack implements ISpecialArmor {

	public ItemParachute() {
		this.setMaxDamage(1000);
		this.addPropertyOverride(new ResourceLocation("active"), (ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) -> {
			if (entityIn != null && stack.getTagCompound().getBoolean("Deployed"))
				return 1;
			return 0;
		});
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage,
			int slot) {
		return new ArmorProperties(0, 0, Integer.MAX_VALUE);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		return 0;
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		stack.damageItem(damage * (stack.getTagCompound().getBoolean("Deployed") ? 8 : 1), entity);
	}

	@Override
	public void onArmorTickAny(World world, final EntityLivingBase player, ItemStack itemStack) {
		if (itemStack.getTagCompound().getBoolean("Deployed")) {
			player.motionY = Math.max(-0.1f, player.motionY);
			player.fallDistance = 0f;
			/*
			 * if (player.ticksExisted % 30 == 0) { itemStack.damageItem(1, player); }
			 */
			if (player.onGround || player.isInsideOfMaterial(Material.WATER))
				itemStack.getTagCompound().setBoolean("Deployed", false);
		}
	}
}
