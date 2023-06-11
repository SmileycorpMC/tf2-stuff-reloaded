package rafradek.TF2weapons.entity.mercenary;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.util.WeaponData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Squad {

	public int minCount;
	public String name;
	public ArrayList<Unit> units = new ArrayList<>();
	public int cost;

	public static enum Type {
		NORMAL, GIANT, FINAL, SUPPORT
	}

	public Squad(Type type) {}

	public static Multimap<Type, Squad> parseFile(File file) {
		Multimap<Type, Squad> map = HashMultimap.create();
		try {
			String s = Files.toString(file, Charsets.UTF_8);
			JsonObject tree = new JsonParser().parse(s).getAsJsonObject();
			for (Entry<String, JsonElement> entry : tree.entrySet()) {
				Type type;
				switch (entry.getKey()) {
				case "giant":
					type = Type.GIANT;
					break;
				case "final":
					type = Type.FINAL;
					break;
				case "support":
					type = Type.SUPPORT;
					break;
				default:
					type = Type.NORMAL;
				}
				for (JsonElement squadEl : entry.getValue().getAsJsonArray()) {
					Squad squad = new Squad(type);
					JsonObject squadObj = squadEl.getAsJsonObject();
					squad.name = JsonUtils.getString(squadObj, "Name", "");
					squad.minCount = JsonUtils.getInt(squadObj, "Min count", 1);
					squad.cost = JsonUtils.getInt(squadObj, "Cost", 1);
					for (JsonElement unitEl : squadObj.getAsJsonArray("Unit")) {
						JsonObject unitObj = unitEl.getAsJsonObject();
						Unit unit = new Unit();
						if (unitObj.has("Health"))
							unit.health = unitObj.get("Health").getAsInt();
						unit.entity = new ResourceLocation(TF2weapons.MOD_ID, unitObj.get("Class").getAsString());
						if (unitObj.has("Weapon"))
							unit.weapon = MapList.nameToData.get(unitObj.get("Weapon").getAsString());
						if (unitObj.has("Attributes"))
							for (Entry<String, JsonElement> attribute : unitObj.getAsJsonObject("Attributes")
									.entrySet()) {
								String attributeName = attribute.getKey();
								float attributeValue = attribute.getValue().getAsFloat();
								Iterator<String> iterator2 = MapList.nameToAttribute.keySet().iterator();
								// System.out.println("to je"+attributeName+"
								// "+attributeValue);
								boolean has = false;

								while (iterator2.hasNext())
									if (iterator2.next().equals(attributeName)) {
										unit.attributes.put(MapList.nameToAttribute.get(attributeName), attributeValue);
										has = true;
									}
								if (has == false)
									unit.attributes.put(TF2Attribute.attributes[Integer.parseInt(attributeName)],
											attributeValue);
							}
					}

					map.put(type, squad);
				}
			}
		} catch (Exception e) {
			TF2weapons.LOGGER.error("Skipped reading squad data from file: %0", file.getName());
			e.printStackTrace();
		}
		return map;
	}

	public static class Unit {
		HashMap<TF2Attribute, Float> attributes = new HashMap<>();
		ResourceLocation entity;
		WeaponData weapon;
		int health;
	}
}
