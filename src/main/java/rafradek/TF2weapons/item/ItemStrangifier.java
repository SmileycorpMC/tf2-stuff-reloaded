package rafradek.TF2weapons.item;

import com.google.common.base.Predicates;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.util.PropertyType;

import java.util.List;

public class ItemStrangifier extends ItemApplicableEffect {

	public ItemStrangifier() {
		this.setMaxStackSize(1);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.hasTagCompound() && MapList.nameToData.containsKey(stack.getTagCompound().getString("Weapon"))) {
			return I18n.format("weapon." + stack.getTagCompound().getString("Weapon")) + " "
					+ super.getItemStackDisplayName(stack);
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		if (!stack.hasTagCompound())
			tooltip.add("Applicable to any weapon");
		tooltip.add("Combine with the matching weapon in a crafting table");
	}

	@Override
	public void onUpdate(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("class")) {
			ItemStack data = ItemFromData.getRandomWeapon(par2World.rand,
					Predicates.and(ItemFromData.VISIBLE_WEAPON, weapon -> (weapon.getInt(PropertyType.COST) >= 9
							&& weapon.get(PropertyType.SLOT).containsKey(stack.getTagCompound().getString("class")))));
			if (!data.isEmpty())
				stack.getTagCompound().setString("Weapon", ItemFromData.getData(data).getName());
			stack.getTagCompound().removeTag("class");
		}
	}

	@Override
	public boolean isApplicable(ItemStack stack, ItemStack weapon) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("Weapon") ? super.isApplicable(stack, weapon)
				: weapon.getItem() instanceof ItemUsable || weapon.getItem() instanceof ItemCloak
						|| weapon.getItem() instanceof ItemPDA || weapon.getItem() instanceof ItemBackpack;
	}

	@Override
	public void apply(ItemStack stack, ItemStack weapon) {
		weapon.getTagCompound().setBoolean("Strange", true);
	}
}
