package rafradek.TF2weapons.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import rafradek.TF2weapons.TF2weapons;

import java.util.Random;

public class EntityNBTCondition implements LootCondition {

	NBTTagCompound tagValue;
	boolean negate;

	public EntityNBTCondition(NBTTagCompound test, boolean negate) {
		this.negate = negate;
		this.tagValue = test;
	}

	public EntityNBTCondition(String nbttag, Number test) {

	}

	@Override
	public boolean testCondition(Random rand, LootContext context) {

		Entity test = context.getEntity(EntityTarget.THIS);
		NBTTagCompound tag = test.writeToNBT(new NBTTagCompound());
		NBTTagCompound tagmerge = new NBTTagCompound();
		for (String key : tagValue.getKeySet())
			tagmerge.setTag(key, tag.getTag(key));

		// tagmerge.merge(tagValue);
		return (!negate && tagmerge.equals(tagValue)) || (negate && !tagmerge.equals(tagValue));
	}

	public static class Serializer extends LootCondition.Serializer<EntityNBTCondition> {
		public Serializer() {
			super(new ResourceLocation(TF2weapons.MOD_ID, "nbt_test"), EntityNBTCondition.class);
		}

		@Override
		public void serialize(JsonObject json, EntityNBTCondition value, JsonSerializationContext context) {
			json.addProperty("test", value.tagValue.toString());
			json.addProperty("negate", value.negate);
		}

		@Override
		public EntityNBTCondition deserialize(JsonObject json, JsonDeserializationContext context) {
			try {
				return new EntityNBTCondition(JsonToNBT.getTagFromJson(JsonUtils.getString(json, "test", "{}")),
						JsonUtils.getBoolean(json, "negate", false));
			} catch (NBTException e) {
				e.printStackTrace();
			}
			return new EntityNBTCondition(new NBTTagCompound(), false);
		}
	}
}
