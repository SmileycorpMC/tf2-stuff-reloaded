package rafradek.TF2weapons.util;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.logging.log4j.Level;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemKillstreakKit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WeaponData implements ICapabilityProvider {

	private static final Gson GSON = (new GsonBuilder())
			.registerTypeAdapter(WeaponData.class, new WeaponData.Serializer()).create();
	private static String nameItemLoaded;
	public static PropertyType<?>[] propertyTypes = new PropertyType[256];
	public static Map<String, JsonDeserializer<ICapabilityProvider>> propertyDeserializers;

	public static class Serializer implements JsonDeserializer<WeaponData> {

		@Override
		public WeaponData deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			WeaponData data = new WeaponData();

			for (Entry<String, JsonElement> property : json.getAsJsonObject().entrySet())
				data.addProperty(property.getKey(), property.getValue(), context);
			return data;
		}

	}

	public static abstract class SpecialProperty implements ICapabilityProvider {

		public abstract void serialize(DataOutput buf, WeaponData data) throws IOException;

		public abstract void deserialize(DataInput buf, WeaponData data) throws IOException;
	}

	public HashMap<PropertyType<?>, Object> properties;

	public CapabilityDispatcher capabilities;

	public int maxCrateValue;
	private String name;

	public WeaponData() {
		this.properties = new HashMap<>();

	}

	public WeaponData(String name) {
		this();
		this.name = name;
	}

	public void addCapabilities(Map<ResourceLocation, ICapabilityProvider> map) {
		this.capabilities = new CapabilityDispatcher(map);
	}

	public int getInt(PropertyType<Integer> propType) {
		Integer property = (Integer) this.properties.get(propType);
		if (property != null)
			return property;
		return 0;
	}

	public String getString(PropertyType<String> propType) {
		String property = (String) this.properties.get(propType);
		if (property != null)
			return property;
		return "";
	}

	public boolean getBoolean(PropertyType<Boolean> propType) {
		Boolean property = (Boolean) this.properties.get(propType);
		if (property != null)
			return property;
		return false;
	}

	public float getFloat(PropertyType<Float> propType) {
		Float property = (Float) this.properties.get(propType);
		if (property != null)
			return property;
		return 0f;
	}

	@SuppressWarnings("unchecked")
	public <A> A get(PropertyType<A> propType) {
		A property = (A) (this.properties.get(propType));
		if (property != null)
			return property;
		return propType.getDefaultValue();
	}

	@SuppressWarnings("unchecked")
	public <A> A get(PropertyType<A> propType, A def) {
		A property = (A) (this.properties.get(propType));
		if (property != null)
			return property;
		return def;
	}

	public boolean hasProperty(PropertyType<?> property) {
		return this.properties.containsKey(property);
	}

	public void addProperty(String name, JsonElement element, JsonDeserializationContext context) {

		PropertyType<?> propType = MapList.propertyTypes.get(name);
		try {
			this.properties.put(propType, propType.deserialize(element, propType.type, context));
		} catch (Exception e) {
			TF2weapons.LOGGER.error("Error reading property {} for {}, value is {}", name, nameItemLoaded,
					element.toString());
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static ArrayList<WeaponData> parseFile(String fileData, String filename) {
		ArrayList<WeaponData> list = new ArrayList<>();
		try {
			String s = fileData;
			JsonObject tree = new JsonParser().parse(s).getAsJsonObject();
			for (Entry<String, JsonElement> entry : tree.getAsJsonObject().entrySet()) {
				nameItemLoaded = entry.getKey();
				WeaponData data = GSON.fromJson(entry.getValue(), WeaponData.class);
				data.name = entry.getKey();
				list.add(data);
			}
		} catch (Exception e) {
			TF2weapons.LOGGER.error("Skipped reading weapon data from file: {}", filename);
			TF2weapons.LOGGER.catching(Level.ERROR, e);
		}
		return list;
	}

	public static class WeaponDataCapability implements ICapabilityProvider {

		public WeaponData inst = ItemFromData.BLANK_DATA;
		public HashMap<String, Float> cachedAttrMult = new HashMap<>();
		public HashMap<String, Float> cachedAttrAdd = new HashMap<>();
		public boolean cached = false;
		public int active;
		public int usedClass = -1;
		public int fire1Cool = 0;
		public int fire2Cool = 0;
		public int clip = 0;

		public float getAttributeValue(ItemStack stack, String nameattr, float initial) {
			if (!cached) {
				NBTTagCompound attributelist;
				cachedAttrMult.clear();
				cachedAttrAdd.clear();
				attributelist = MapList.buildInAttributes.get(ItemFromData.getData(stack).getName());
				if (attributelist != null)
					for (String name : attributelist.getKeySet()) {
						NBTBase tag = attributelist.getTag(name);
						if (tag instanceof NBTTagFloat) {
							TF2Attribute attribute = TF2Attribute.attributes[Integer.parseInt(name)];

							if (attribute.typeOfValue == TF2Attribute.Type.ADDITIVE) {
								if (!cachedAttrAdd.containsKey(attribute.effect))
									cachedAttrAdd.put(attribute.effect, 0f);
								cachedAttrAdd.put(attribute.effect,
										cachedAttrAdd.get(attribute.effect) + ((NBTTagFloat) tag).getFloat());
							} else {
								if (!cachedAttrMult.containsKey(attribute.effect))
									cachedAttrMult.put(attribute.effect, 1f);
								cachedAttrMult.put(attribute.effect,
										cachedAttrMult.get(attribute.effect) * ((NBTTagFloat) tag).getFloat());
							}
						}
					}
				if (stack.hasTagCompound()) {
					attributelist = stack.getTagCompound().getCompoundTag("Attributes");
					for (String name : attributelist.getKeySet()) {
						NBTBase tag = attributelist.getTag(name);
						if (tag instanceof NBTTagFloat) {
							TF2Attribute attribute = TF2Attribute.attributes[Integer.parseInt(name)];

							if (attribute.typeOfValue == TF2Attribute.Type.ADDITIVE) {
								if (!cachedAttrAdd.containsKey(attribute.effect))
									cachedAttrAdd.put(attribute.effect, 0f);
								cachedAttrAdd.put(attribute.effect,
										cachedAttrAdd.get(attribute.effect) + ((NBTTagFloat) tag).getFloat());
							} else {
								if (!cachedAttrMult.containsKey(attribute.effect))
									cachedAttrMult.put(attribute.effect, 1f);
								cachedAttrMult.put(attribute.effect,
										cachedAttrMult.get(attribute.effect) * ((NBTTagFloat) tag).getFloat());
							}

						}
					}
					if (TF2ConfigVars.killstreakDrop && stack.getTagCompound().hasKey(NBTLiterals.STREAK_ATTRIB)) {
						TF2Attribute attribute = TF2Attribute.attributes[stack.getTagCompound()
								.getShort(NBTLiterals.STREAK_ATTRIB)];
						float value = ItemKillstreakKit.getKillstreakBonus(attribute,
								stack.getTagCompound().getByte(NBTLiterals.STREAK_LEVEL),
								stack.getTagCompound().getInteger(NBTLiterals.STREAK_KILLS), this.inst);
						if (attribute.typeOfValue == TF2Attribute.Type.ADDITIVE) {
							if (!cachedAttrAdd.containsKey(attribute.effect))
								cachedAttrAdd.put(attribute.effect, 0f);
							cachedAttrAdd.put(attribute.effect, cachedAttrAdd.get(attribute.effect) + value);
						} else {
							if (!cachedAttrMult.containsKey(attribute.effect))
								cachedAttrMult.put(attribute.effect, 1f);
							if (value > attribute.defaultValue)
								cachedAttrMult.put(attribute.effect,
										cachedAttrMult.get(attribute.effect) + value - attribute.defaultValue);
							else
								cachedAttrMult.put(attribute.effect, cachedAttrMult.get(attribute.effect) * value);
						}
					}
				}
				this.cached = true;
			}
			Float valueadd = cachedAttrAdd.get(nameattr);
			Float valuemult = cachedAttrMult.get(nameattr);
			if (valueadd == null)
				valueadd = Float.valueOf(0f);
			if (valuemult == null)
				valuemult = Float.valueOf(1f);
			return (initial + valueadd) * valuemult;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			// System.out.println("capinit: "+TF2weapons.WEAPONS_DATA_CAP);
			return TF2weapons.WEAPONS_DATA_CAP != null && capability == TF2weapons.WEAPONS_DATA_CAP;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (TF2weapons.WEAPONS_DATA_CAP != null && capability == TF2weapons.WEAPONS_DATA_CAP)
				return TF2weapons.WEAPONS_DATA_CAP.cast(this);
			return null;
		}

	}

	public static WeaponDataCapability getCapability(ItemStack stack) {
		return stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capabilities.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capabilities.getCapability(capability, facing);
	}
}
