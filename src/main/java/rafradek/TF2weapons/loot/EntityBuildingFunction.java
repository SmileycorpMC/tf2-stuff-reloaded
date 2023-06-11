package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;

import java.util.Random;

public class EntityBuildingFunction extends LootFunction {

	// public int[] possibleValues;
	// public int[] withClass;
	public EntityBuildingFunction(LootCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		Entity entity = context.getEntity(EntityTarget.THIS);
		if (entity != null && entity instanceof EntityTF2Character)
			stack.setItemDamage(18 + rand.nextInt(3) * 2 + ((EntityTF2Character) entity).getEntTeam());
		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<EntityBuildingFunction> {
		public Serializer() {
			super(new ResourceLocation("random_meta"), EntityBuildingFunction.class);
		}

		@Override
		public void serialize(JsonObject object, EntityBuildingFunction functionClazz,
				JsonSerializationContext serializationContext) {
			// object.add("possibleValues",
			// serializationContext.serialize(functionClazz.possibleValues));
			// object.add("data",
			// serializationContext.serialize(functionClazz.metaRange));
		}

		@Override
		public EntityBuildingFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootCondition[] conditionsIn) {
			return new EntityBuildingFunction(conditionsIn);
		}
	}
}
