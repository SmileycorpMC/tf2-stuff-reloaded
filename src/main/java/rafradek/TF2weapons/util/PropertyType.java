package rafradek.TF2weapons.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.item.ItemCrate;
import rafradek.TF2weapons.item.ItemFromData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PropertyType<T> implements JsonDeserializer<T> {

	public static final PropertyType<String> FIRE_SOUND = new PropertyType<>(3, "Fire sound", String.class);
	public static final PropertyType<String> RENDER = new PropertyType<>(2, "Render", String.class);
	public static final PropertyType<Map<String, Integer>> SLOT = new PropertyTypeMap<>(1, "Slot", Integer.class);
	public static final PropertyType<String> NAME = new PropertyType<>(0, "Name", String.class);
	public static final PropertyType<String> RELOAD_SOUND = new PropertyType<>(4, "Reload sound", String.class);
	public static final PropertyType<Integer> FIRE_SPEED = new PropertyType<>(5, "Firing speed", Integer.class);
	public static final PropertyType<Integer> PELLETS = new PropertyType<>(6, "Pellets", Integer.class);
	public static final PropertyType<Float> DAMAGE = new PropertyType<>(7, "Damage", Float.class);
	public static final PropertyType<Float> MAX_DAMAGE = new PropertyType<>(8, "Max damage", Float.class);
	public static final PropertyType<Float> MIN_DAMAGE = new PropertyType<>(9, "Min damage", Float.class);
	public static final PropertyType<Boolean> RANDOM_CRITS = new PropertyType<>(10, "Random crits", Boolean.class);
	public static final PropertyType<Boolean> RAPIDFIRE_CRITS = new PropertyType<>(11, "Rapidfire crits",
			Boolean.class);
	public static final PropertyType<Float> DAMAGE_FALOFF = new PropertyType<>(12, "Damage falloff", Float.class);
	public static final PropertyType<Boolean> RELOADS_CLIP = new PropertyType<>(13, "Reloads clip", Boolean.class);
	public static final PropertyType<Boolean> RELOADS_FULL_CLIP = new PropertyType<>(14, "Reloads full clip",
			Boolean.class);
	public static final PropertyType<Integer> RELOAD_TIME_FIRST = new PropertyType<>(15, "Reload time first",
			Integer.class);
	public static final PropertyType<Integer> RELOAD_TIME = new PropertyType<>(16, "Reload time", Integer.class);
	public static final PropertyType<Integer> CLIP_SIZE = new PropertyType<>(17, "Clip size", Integer.class);
	public static final PropertyType<Integer> KNOCKBACK = new PropertyType<>(18, "Knockback", Integer.class);
	public static final PropertyType<Boolean> SPREAD_RECOVERY = new PropertyType<>(19, "Spread recovery",
			Boolean.class);
	public static final PropertyType<Float> DUAL_WIELD_SPEED = new PropertyType<>(20, "Dual wield speed", Float.class);
	public static final PropertyType<String> FIRE_LOOP_SOUND = new PropertyType<>(21, "Fire loop sound", String.class);
	public static final PropertyType<String> SPIN_SOUND = new PropertyType<>(22, "Spin sound", String.class);
	public static final PropertyType<String> WIND_UP_SOUND = new PropertyType<>(23, "Wind up sound", String.class);
	public static final PropertyType<String> WIND_DOWN_SOUND = new PropertyType<>(24, "Wind down sound", String.class);
	public static final PropertyType<String> FIRE_START_SOUND = new PropertyType<>(25, "Fire start sound",
			String.class);
	public static final PropertyType<String> FIRE_STOP_SOUND = new PropertyType<>(26, "Fire stop sound", String.class);
	public static final PropertyType<String> AIRBLAST_SOUND = new PropertyType<>(27, "Airblast sound", String.class);
	public static final PropertyType<String> AIRBLAST_ROCKET_SOUND = new PropertyType<>(37, "Airblast rocket sound",
			String.class);
	public static final PropertyType<Boolean> HIDDEN = new PropertyType<>(38, "Hidden", Boolean.class);
	public static final PropertyType<String> CLOAK_SOUND = new PropertyType<>(28, "Cloak sound", String.class);
	public static final PropertyType<String> DECLOAK_SOUND = new PropertyType<>(29, "Decloak sound", String.class);
	public static final PropertyType<String> BUILD_HIT_SUCCESS_SOUND = new PropertyType<>(30, "Build hit success sound",
			String.class);
	public static final PropertyType<String> BUILD_HIT_FAIL_SOUND = new PropertyType<>(31, "Build hit fail sound",
			String.class);
	public static final PropertyType<String> EFFECT_TYPE = new PropertyType<>(32, "Effect type", String.class);
	public static final PropertyType<Integer> DURATION = new PropertyType<>(33, "Duration", Integer.class);
	public static final PropertyType<Integer> COOLDOWN = new PropertyType<>(34, "Cooldown", Integer.class);
	public static final PropertyType<String> HEAL_START_SOUND = new PropertyType<>(35, "Heal start sound",
			String.class);
	public static final PropertyType<String> NO_TARGET_SOUND = new PropertyType<>(36, "No target sound", String.class);
	public static final PropertyType<String> CHARGED_SOUND = new PropertyType<>(39, "Charged sound", String.class);
	public static final PropertyType<String> UBER_START_SOUND = new PropertyType<>(40, "Uber start sound",
			String.class);
	public static final PropertyType<String> UBER_STOP_SOUND = new PropertyType<>(41, "Uber stop sound", String.class);
	public static final PropertyType<Float> HEAL = new PropertyType<>(42, "Heal", Float.class);
	public static final PropertyType<Float> MAX_OVERHEAL = new PropertyType<>(43, "Max overheal", Float.class);
	public static final PropertyType<Float> RANGE = new PropertyType<>(44, "Range", Float.class);
	public static final PropertyType<Float> PROJECTILE_SPEED = new PropertyType<>(45, "Projectile speed", Float.class);
	public static final PropertyType<String> PROJECTILE = new PropertyType<>(46, "Projectile", String.class);
	public static final PropertyType<String> BASED_ON = new PropertyType<>(47, "Based on", String.class);
	public static final PropertyType<String> RENDER_BACKSTAB = new PropertyType<>(48, "Render backstab", String.class);
	public static final PropertyType<String> CHARGE_SOUND = new PropertyType<>(49, "Charge sound", String.class);
	public static final PropertyType<String> DETONATE_SOUND = new PropertyType<>(50, "Detonate sound", String.class);
	public static final PropertyType<String> CLASS = new PropertyType<>(51, "Class", String.class);
	public static final PropertyType<Float> SPREAD = new PropertyType<>(52, "Spread", Float.class);
	public static final PropertyType<String> HIT_SOUND = new PropertyType<>(53, "Hit sound", String.class);
	public static final PropertyType<Integer> AMMO_TYPE = new PropertyType<>(54, "Ammo type", Integer.class);
	public static final PropertyType<Integer> ROLL_HIDDEN = new PropertyType<>(55, "Roll hidden", Integer.class);
	public static final PropertyType<String> MOB_TYPE = new PropertyType<>(56, "Mobs", String.class);
	public static final PropertyType<Float> RECOIL = new PropertyType<>(57, "Recoil", Float.class);
	public static final PropertyType<Float> DROP_CHANCE = new PropertyType<>(58, "Drop chance", Float.class);
	public static final PropertyType<Integer> COST = new PropertyType<>(59, "Cost", Integer.class);
	public static final PropertyType<Integer> WEAR = new PropertyType<>(60, "Wear flags", Integer.class);
	public static final PropertyType<String> ARMOR_IMAGE = new PropertyType<>(61, "Overlay", String.class);
	public static final PropertyType<String> HEAD_MODEL = new PropertyType<>(62, "Head model", String.class);
	public static final PropertyType<String> BODY_MODEL = new PropertyType<>(63, "Body model", String.class);
	public static final PropertyType<Float> ARMOR = new PropertyType<>(64, "Armor", Float.class);
	public static final PropertyType<Float> ARMOR_TOUGHNESS = new PropertyType<>(65, "Armor toughness", Float.class);
	public static final PropertyType<String> HORN_RED_SOUND = new PropertyType<>(66, "Horn red sound", String.class);
	public static final PropertyType<String> HORN_BLU_SOUND = new PropertyType<>(67, "Horn blu sound", String.class);
	public static final PropertyType<String> DESC = new PropertyType<>(68, "Description", String.class);
	public static final PropertyType<String> HIT_WORLD_SOUND = new PropertyType<>(69, "Hit world sound", String.class);
	public static final PropertyType<String> HIT_LOOP_SOUND = new PropertyType<>(70, "Hit loop sound", String.class);
	public static final PropertyType<Float> EFFICIENT_RANGE = new PropertyType<>(71, "Efficient range", Float.class);
	public static final PropertyType<Boolean> STOCK = new PropertyType<>(72, "Stock", Boolean.class);
	public static final PropertyType<String> NO_FIRE_SOUND = new PropertyType<>(73, "No fire sound", String.class);
	public static final PropertyType<String> CHARGED_FIRE_SOUND = new PropertyType<>(73, "Charged fire sound",
			String.class);
	public static final PropertyType<Boolean> PENETRATE = new PropertyType<>(74, "Penetrate", Boolean.class);
	public static final PropertyType<String> OVERRIDE = new PropertyType<>(75, "Weapon override", String.class);
	public static final PropertyType<Integer> MAX_AMMO = new PropertyType<>(76, "Max ammo", Integer.class);
	public static final PropertyType<String> EXPLOSION_SOUND = new PropertyType<>(77, "Explosion sound", String.class);
	public static final PropertyType<Float> ARMOR_PEN_SCALE = new PropertyType<>(78, "Armor penetration scale",
			Float.class);
	public static final PropertyType<String> SPECIAL_1_SOUND = new PropertyType<>(79, "Special 1 sound", String.class);
	public static final PropertyType<String> SPECIAL_2_SOUND = new PropertyType<>(80, "Special 2 sound", String.class);
	public static final PropertyType<ItemFromData.AttributeProvider> ATTRIBUTES = new ItemFromData.PropertyAttribute(81,
			"Attributes", ItemFromData.AttributeProvider.class);
	public static final PropertyType<ItemCrate.CrateContent> CONTENT = new ItemCrate.PropertyContent(82, "Content",
			ItemCrate.CrateContent.class);
	public static final PropertyType<Integer> COLOR = new PropertyType<>(83, "Color", Integer.class);
	public static final PropertyType<Boolean> F2P = new PropertyType<>(84, "F2P", Boolean.class);
	public static final PropertyType<Float> KNOCKBACK_Z = new PropertyType<>(85, "Knockback Z", Float.class);
	public static final PropertyType<Float> MINIMAL_RANGE = new PropertyType<>(86, "Minimal range", Float.class);
	public static final PropertyType<Float> COOLDOWN_LONG = new PropertyType<>(87, "Long cooldown", Float.class);
	public static final PropertyType<Boolean> TEMPLATE = new PropertyType<>(88, "Template", Boolean.class);
	public static final PropertyType<Float> UBER_TIME = new PropertyType<>(89, "Uber time", Float.class);
	public Class<T> type;
	public int id;
	public String name;

	public PropertyType(int id, String name, Class<T> type) {
		this.name = name;
		this.id = id;
		this.type = type;
		WeaponData.propertyTypes[id] = this;
		MapList.propertyTypes.put(name, this);
	}

	public static void init() {

	}

	public void serialize(DataOutput buf, WeaponData data, T value) throws IOException {

		if (this.type == Boolean.class)
			buf.writeBoolean((Boolean) value);
		else if (this.type == Integer.class)
			buf.writeInt((Integer) value);
		else if (this.type == Float.class)
			buf.writeFloat((Float) value);
		else if (this.type == String.class)
			buf.writeUTF((String) value);
	}

	public T deserialize(DataInput buf, WeaponData data) throws IOException {
		T prop = null;
		if (this.type == Boolean.class)
			prop = type.cast(buf.readBoolean());
		else if (this.type == Integer.class)
			prop = type.cast(buf.readInt());
		else if (this.type == Float.class)
			prop = type.cast(buf.readFloat());
		else if (this.type == String.class)
			prop = type.cast(buf.readUTF());
		return prop;
		// data.properties.put(this, prop);
	}

	public T getDefaultValue() {
		if (this.type == Boolean.class)
			return type.cast(false);
		else if (this.type == Integer.class)
			return type.cast(0);
		else if (this.type == Float.class)
			return type.cast(0f);
		else if (this.type == String.class)
			return type.cast("");
		return null;
		// data.properties.put(this, prop);
	}

	public boolean hasKey(WeaponData data) {
		return data.properties.get(this) != null;
	}

	@Override
	public T deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		if (type == Boolean.class)
			return type.cast(json.getAsBoolean());
		else if (type == Integer.class)
			return type.cast(json.getAsInt());
		else if (type == String.class)
			return type.cast(json.getAsString());
		else if (type == Float.class)
			return type.cast(json.getAsFloat());
		return null;
	}

	public static class PropertyTypeMap<T> extends PropertyType<Map<String, T>> {

		public Class<T> mapType;
		public static Map<?, ?> defaultValue = new HashMap<>();

		public PropertyTypeMap(int id, String name, Class<T> type) {
			super(id, name, null);
			this.mapType = type;
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, Map<String, T> value) throws IOException {

			buf.writeByte(value.size());
			for (Entry<String, T> entry : value.entrySet()) {
				buf.writeUTF(entry.getKey());
				if (this.mapType == Boolean.class)
					buf.writeBoolean((Boolean) entry.getValue());
				else if (this.mapType == Integer.class)
					buf.writeInt((Integer) entry.getValue());
				else if (this.mapType == Float.class)
					buf.writeFloat((Float) entry.getValue());
				else if (this.mapType == String.class)
					buf.writeUTF((String) entry.getValue());
			}
		}

		@Override
		public Map<String, T> deserialize(DataInput buf, WeaponData data) throws IOException {
			Map<String, T> prop = new HashMap<>();
			int count = buf.readByte();
			for (int i = 0; i < count; i++) {
				String name = buf.readUTF();
				T value = null;
				if (this.mapType == Boolean.class)
					value = mapType.cast(buf.readBoolean());
				else if (this.mapType == Integer.class)
					value = mapType.cast(buf.readInt());
				else if (this.mapType == Float.class)
					value = mapType.cast(buf.readFloat());
				else if (this.mapType == String.class)
					value = mapType.cast(buf.readUTF());
				prop.put(name, value);
			}
			return prop;
			// data.properties.put(this, prop);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Map<String, T> getDefaultValue() {
			return (Map<String, T>) defaultValue;
		}

		@Override
		public Map<String, T> deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			Map<String, T> prop = new HashMap<>();
			for (Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
				if (mapType == Boolean.class)
					prop.put(entry.getKey(), mapType.cast(entry.getValue().getAsBoolean()));
				else if (mapType == Integer.class)
					prop.put(entry.getKey(), mapType.cast(entry.getValue().getAsInt()));
				else if (mapType == String.class)
					prop.put(entry.getKey(), mapType.cast(entry.getValue().getAsString()));
				else if (mapType == Float.class)
					prop.put(entry.getKey(), mapType.cast(entry.getValue().getAsFloat()));
			return prop;
		}
	}
}