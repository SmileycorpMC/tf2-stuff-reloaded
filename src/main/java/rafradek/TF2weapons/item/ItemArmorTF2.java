package rafradek.TF2weapons.item;

import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

public class ItemArmorTF2 extends ItemArmor {

	public String description;
	public UUID knockbackUUID = UUID.fromString("7941f9c1-13ac-4ae0-b54b-cbd8d5eec6df");
	public float knockbackReduction;

	public ItemArmorTF2(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn,
			String description, float kresistance) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.description = description;
		this.knockbackReduction = kresistance;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(description);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

		if (equipmentSlot == this.armorType && this.knockbackReduction != 0) {
			multimap.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
					new AttributeModifier(knockbackUUID, "Knockback modifier", this.knockbackReduction, 0));
		}

		return multimap;
	}
}
