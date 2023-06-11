package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemWeapon;
import rafradek.TF2weapons.util.PropertyType;

import java.util.Random;

public class RandomWeaponFunction extends LootFunction {

	// public int[] possibleValues;
	// public int[] withClass;
	public RandomValueRange upgradeRange;
	public float valveWepChance;

	public RandomWeaponFunction(LootCondition[] conditionsIn, RandomValueRange range, float valve) {
		super(conditionsIn);
		this.upgradeRange = range;
		this.valveWepChance = valve;
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		if (this.valveWepChance * 0.35f < rand.nextFloat()) {
			stack = ItemFromData.getRandomWeapon(rand,
					input -> (!input.getBoolean(PropertyType.HIDDEN) && input.getInt(PropertyType.ROLL_HIDDEN) == 0
							&& !input.getString(PropertyType.CLASS).equals("cosmetic")
							&& !input.getString(PropertyType.CLASS).equals("crate")));
		} else {
			stack = ItemFromData.getRandomWeapon(rand, input -> (input.getInt(PropertyType.ROLL_HIDDEN) == 2));
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon && this.valveWepChance >= rand.nextFloat()) {
			stack.getTagCompound().setBoolean("Valve", true);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[0], 1.15f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[2], 2.5f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[6], 0.85f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[61], 1.5f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[37], 1f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[45], 5f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[48], 5);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[49], 0.85f);
			TF2Attribute.setAttribute(stack, TF2Attribute.attributes[46], 1.15f);
		}
		if (upgradeRange.getMax() > 0)
			TF2Attribute.upgradeItemStack(stack, this.upgradeRange.generateInt(rand), rand);
		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<RandomWeaponFunction> {
		public Serializer() {
			super(new ResourceLocation("set_weapon_upgraded"), RandomWeaponFunction.class);
		}

		@Override
		public void serialize(JsonObject object, RandomWeaponFunction functionClazz,
				JsonSerializationContext serializationContext) {
			object.add("upgrade_range", serializationContext.serialize(functionClazz.upgradeRange));
			object.add("valve_chance", serializationContext.serialize(functionClazz.valveWepChance));
			// object.add("possibleValues",
			// serializationContext.serialize(functionClazz.possibleValues));
			// object.add("data",
			// serializationContext.serialize(functionClazz.metaRange));
		}

		@Override
		public RandomWeaponFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootCondition[] conditionsIn) {
			return new RandomWeaponFunction(conditionsIn,
					JsonUtils.deserializeClass(object, "upgrade_range", deserializationContext, RandomValueRange.class),
					object.get("valve_chance").getAsFloat());
		}
	}
}
