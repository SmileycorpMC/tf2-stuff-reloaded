package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.TF2Class;

import java.util.Random;

public class EntityOfClassFunction extends LootFunction {

	// public int[] possibleValues;
	// public int[] withClass;
	public TF2Class weaponClass;

	public EntityOfClassFunction(LootCondition[] conditionsIn, String weaponClass) {
		super(conditionsIn);
		this.weaponClass = TF2Class.getClass(weaponClass);
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		stack = ItemFromData.getRandomWeaponOfClass(weaponClass, rand, true);
		if (! stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setBoolean("DropFrom", true);
		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<EntityOfClassFunction> {
		public Serializer() {
			super(new ResourceLocation("set_weapon_class"), EntityOfClassFunction.class);
		}

		@Override
		public void serialize(JsonObject object, EntityOfClassFunction functionClazz,
				JsonSerializationContext serializationContext) {
			object.addProperty("weaponClass", functionClazz.weaponClass.getName());
			// object.add("possibleValues",
			// serializationContext.serialize(functionClazz.possibleValues));
			// object.add("data",
			// serializationContext.serialize(functionClazz.metaRange));
		}

		@Override
		public EntityOfClassFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootCondition[] conditionsIn) {
			return new EntityOfClassFunction(conditionsIn, object.get("weaponClass").getAsString());
		}
	}
}
