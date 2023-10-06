package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.item.ItemWearable;
import rafradek.TF2weapons.util.PropertyType;

import java.util.Random;

public class RandomWeaponItemFunction extends LootFunction {

	public RandomWeaponItemFunction() {
		super(new LootCondition[]{});
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		//loottweaker support
		if (rand == null && context == null) return stack;
		if (!(stack.getMetadata() == 9 || stack.getMetadata() == 10)) return stack;
		return ItemFromData.getRandomWeapon(stack, rand);
	}

}
