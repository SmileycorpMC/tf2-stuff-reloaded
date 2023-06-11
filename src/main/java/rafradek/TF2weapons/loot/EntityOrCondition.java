package rafradek.TF2weapons.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import rafradek.TF2weapons.TF2weapons;

import java.util.Random;

public class EntityOrCondition implements LootCondition {

	LootCondition[] conditions;
	boolean negate;

	public EntityOrCondition(LootCondition[] conditions, boolean negate) {
		this.negate = negate;
		this.conditions = conditions;
	}

	@Override
	public boolean testCondition(Random rand, LootContext context) {

		for (LootCondition condition : conditions) {
			if (condition.testCondition(rand, context))
				return !this.negate;
		}
		return this.negate;
	}

	public static class Serializer extends LootCondition.Serializer<EntityOrCondition> {
		public Serializer() {
			super(new ResourceLocation(TF2weapons.MOD_ID, "or"), EntityOrCondition.class);
		}

		@Override
		public void serialize(JsonObject json, EntityOrCondition value, JsonSerializationContext context) {
			json.addProperty("negate", value.negate);
		}

		@Override
		public EntityOrCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			JsonArray arr = JsonUtils.getJsonArray(json, "conditions");
			LootCondition[] conditions = new LootCondition[arr.size()];
			for (int i = 0; i < conditions.length; i++) {
				conditions[i] = LootConditionManager
						.getSerializerForName(new ResourceLocation(arr.get(i).getAsString()))
						.deserialize(json, context);
			}
			return new EntityOrCondition(conditions, JsonUtils.getBoolean(json, "negate"));
		}
	}
}
