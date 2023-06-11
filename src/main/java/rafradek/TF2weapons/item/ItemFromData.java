package rafradek.TF2weapons.item;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.TF2Attribute.State;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.mercenary.EntityTF2Character;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("deprecation")
public class ItemFromData extends Item implements IItemOverlay {

	public static class PropertyAttribute extends PropertyType<AttributeProvider> {

		public PropertyAttribute(int id, String name, Class<AttributeProvider> type) {
			super(id, name, type);
		}

		@Override
		public AttributeProvider deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			HashMap<TF2Attribute, Float> attributes = new HashMap<>();
			for (Entry<String, JsonElement> attribute : json.getAsJsonObject().entrySet()) {
				String attributeName = attribute.getKey();
				float attributeValue = attribute.getValue().getAsFloat();
				Iterator<String> iterator2 = MapList.nameToAttribute.keySet().iterator();
				// System.out.println("to je"+attributeName+"
				// "+attributeValue);
				boolean has = false;

				while (iterator2.hasNext())
					if (iterator2.next().equals(attributeName)) {
						attributes.put(MapList.nameToAttribute.get(attributeName), attributeValue);
						has = true;
					}
				if (has == false)
					attributes.put(TF2Attribute.attributes[Integer.parseInt(attributeName)], attributeValue);
			}
			return new AttributeProvider(attributes);
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, AttributeProvider value) throws IOException {
			buf.writeByte(value.attributes.size());
			for (Entry<TF2Attribute, Float> attr : value.attributes.entrySet()) {
				buf.writeByte(attr.getKey().id);
				buf.writeFloat(attr.getValue());
			}
		}

		@Override
		public AttributeProvider deserialize(DataInput buf, WeaponData data) throws IOException {
			HashMap<TF2Attribute, Float> map = new HashMap<>();
			int attributeCount = buf.readByte();
			for (int i = 0; i < attributeCount; i++) {
				map.put(TF2Attribute.attributes[buf.readUnsignedByte()], buf.readFloat());
			}
			return new AttributeProvider(map);
		}
	}

	public static class AttributeProvider {
		public Map<TF2Attribute, Float> attributes;

		public AttributeProvider(Map<TF2Attribute, Float> attributes) {
			this.attributes = attributes;
		}
	}

	public static final WeaponData BLANK_DATA = new WeaponData("toloadfiles");
	public static final Predicate<WeaponData> VISIBLE_WEAPON = input -> !input.getBoolean(PropertyType.HIDDEN)
			&& input.getInt(PropertyType.ROLL_HIDDEN) == 0 && !input.getString(PropertyType.CLASS).equals("cosmetic")
			&& !input.getString(PropertyType.CLASS).equals("crate");

	public ItemFromData() {
		this.setCreativeTab(TF2weapons.tabutilitytf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2usable");
		this.setMaxStackSize(1);
		this.setNoRepair();
		this.addPropertyOverride(new ResourceLocation("team"),
				(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) -> {
					if (entityIn != null) {
						if (TF2Util.getTeamColorNumber(entityIn) == 12)
							return 0;
						else if (TF2Util.getTeamColorNumber(entityIn) == 9
								|| TF2Util.getTeamColorNumber(entityIn) == 11)
							return 1f;
						return 0.5f;
					}
					return 0;
				});
	}

	@Override
	public void onUpdate(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
		if (getData(stack) == BLANK_DATA && par3Entity instanceof EntityPlayer) {
			((EntityPlayer) par3Entity).inventory.setInventorySlotContents(par4, ItemStack.EMPTY);
			stack.setCount(0);
			return;
		}
	}

	public static WeaponData getData(ItemStack stack) {
		WeaponData value = BLANK_DATA;
		if (stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null)) {
			value = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).inst;
			if (value == BLANK_DATA && stack.hasTagCompound()
					&& MapList.nameToData.containsKey(stack.getTagCompound().getString("Type"))) {
				value = stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).inst = MapList.nameToData
						.get(stack.getTagCompound().getString("Type"));
			}
		}
		return value;
	}

	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs) && par2CreativeTabs != TF2weapons.tabrareweapontf2)
			return;
		Iterator<Entry<String, WeaponData>> iterator = MapList.nameToData.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, WeaponData> entry = iterator.next();
			// System.out.println("Hidden:
			// "+entry.getValue().hasProperty(PropertyType.HIDDEN));
			boolean isRare = entry.getValue().getInt(PropertyType.ROLL_HIDDEN) == 2;
			if (entry.getValue().hasProperty(PropertyType.HIDDEN) && entry.getValue().getBoolean(PropertyType.HIDDEN))
				continue;
			if ((isRare && par2CreativeTabs == TF2weapons.tabrareweapontf2)
					|| (!isRare && par2CreativeTabs != TF2weapons.tabrareweapontf2)) {
				Item item = MapList.weaponClasses.get(entry.getValue().getString(PropertyType.CLASS));
				if (item == this)
					par3List.add(ItemFromData.getNewStack(entry.getKey()));
			}
		}
	}

	public static ItemStack getNewStack(String type) {
		// "+MapList.weaponClasses.get(MapList.nameToCC.get(type).get("Class").getString())+"
		// "+Thread.currentThread().getName());
		if (!MapList.nameToData.containsKey(type))
			return ItemStack.EMPTY;

		return getNewStack(MapList.nameToData.get(type));
	}

	public static ItemStack getNewStack(WeaponData type) {
		ItemStack stack = new ItemStack(MapList.weaponClasses.get(type.getString(PropertyType.CLASS)));
		// System.out.println(stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null));
		stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).inst = type;
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Type", type.getName());
		tag.setTag("Attributes", new NBTTagCompound());
		stack.setTagCompound(tag);
		// System.out.println(stack.toString());
		return stack;
	}

	@Override
	public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack,
			NBTTagCompound nbt) {
		return new WeaponData.WeaponDataCapability();
	}

	public static List<ItemStack> getRandomWeapons(Random random, Predicate<WeaponData> predicate, int count) {

		ArrayList<WeaponData> weapons = new ArrayList<>();
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet())
			if (predicate.apply(entry.getValue())) {
				weapons.add(entry.getValue());
			}
		ArrayList<ItemStack> ret = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			if (weapons.isEmpty())
				break;
			ret.add(getNewStack(weapons.remove(random.nextInt(weapons.size()))));
		}
		return ret;
	}

	public static ItemStack getRandomWeapon(Random random, Predicate<WeaponData> predicate) {
		return Iterables.getFirst(getRandomWeapons(random, predicate, 1), ItemStack.EMPTY);
	}

	public static ItemStack getRandomWeaponOfType(String type, float chanceOfParent, Random random) {
		// WeaponData parent=MapList.nameToData.get(type);
		if (chanceOfParent >= 0 && random.nextFloat() <= chanceOfParent)
			return getNewStack(type);
		else {
			ArrayList<String> weapons = new ArrayList<>();
			if (chanceOfParent < 0)
				weapons.add(type);
			for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet())
				if (!entry.getValue().getBoolean(PropertyType.HIDDEN)
						&& entry.getValue().getInt(PropertyType.ROLL_HIDDEN) == 0
						&& entry.getValue().getString(PropertyType.BASED_ON).equals(type))
					weapons.add(entry.getKey());
			if (weapons.size() > 0)
				return getNewStack(weapons.get(random.nextInt(weapons.size())));
			else
				return getNewStack(type);
		}

	}

	public static ItemStack getRandomWeaponOfClass(String clazz, Random random, boolean showHidden) {
		ArrayList<WeaponData> weapons = new ArrayList<>();
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet())
			if (!entry.getValue().getBoolean(PropertyType.HIDDEN)
					&& (showHidden || entry.getValue().getInt(PropertyType.ROLL_HIDDEN) == 0)
					&& entry.getValue().getString(PropertyType.CLASS).equals(clazz))
				weapons.add(entry.getValue());
		if (weapons.isEmpty())
			return ItemStack.EMPTY;
		return getNewStack(weapons.get(random.nextInt(weapons.size())));
	}

	public static ItemStack getRandomWeaponOfSlotMob(final String mob, final int slot, Random random,
			final boolean showHidden, float stockWeight, boolean stockOnly) {
		Predicate<WeaponData> base = input -> !input.getBoolean(PropertyType.HIDDEN)
				&& !(input.getInt(PropertyType.ROLL_HIDDEN) > 0 && !showHidden)
				&& ItemFromData.isItemOfClassSlot(input, slot, mob);

		if (stockWeight == 1f && !stockOnly)
			return getRandomWeapon(random, base);

		ItemStack stock = getRandomWeapon(random,
				Predicates.and(base, (Predicate<WeaponData>) input -> input.getBoolean(PropertyType.STOCK)));

		if (stockOnly)
			return stock;
		Predicate<WeaponData> unipredicate = Predicates.and(base,
				(Predicate<WeaponData>) input -> !input.getBoolean(PropertyType.STOCK));
		float unicount = getWeaponCount(unipredicate);
		ItemStack uni = getRandomWeapon(random, unipredicate);
		if (uni.isEmpty()) {
			return stock;
		} else if (stock.isEmpty()) {
			return uni;
		} else if (random.nextFloat() < unicount / (unicount + stockWeight)) {
			return uni;
		} else {
			return stock;
		}
	}

	public static List<ItemStack> getRandomWeaponsOfSlotMob(final String mob, final int slot, Random random,
			final boolean showHidden, int count) {
		return getRandomWeapons(random,
				input -> !input.getBoolean(PropertyType.HIDDEN)
						&& !(input.getInt(PropertyType.ROLL_HIDDEN) > 0 && !showHidden)
						&& ItemFromData.isItemOfClassSlot(input, slot, mob),
				count);
	}

	public static int getWeaponCount(Predicate<WeaponData> predicate) {
		int count = 0;
		for (Entry<String, WeaponData> entry : MapList.nameToData.entrySet()) {
			if (predicate.apply(entry.getValue()))
				count++;
		}
		return count;
	}

	public static boolean isSameType(ItemStack stack, String name) {
		return !stack.isEmpty() && getData(stack) != BLANK_DATA && (getData(stack).getName().equals(name)
				|| getData(stack).getString(PropertyType.BASED_ON).equals(name));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return getData(oldStack) != getData(newStack) || (slotChanged);

	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (ItemFromData.getData(stack) == BLANK_DATA)
			return "Weapon";
		String name = getTranslatedName(stack);
		if (stack.getTagCompound().getBoolean("Strange"))
			name = TextFormatting.GOLD
					+ TF2EventsCommon.STRANGE_TITLES[stack.getTagCompound().getInteger("StrangeLevel")] + " " + name;
		if (stack.getTagCompound().getBoolean("Australium"))
			name = TextFormatting.GOLD + "Australium " + name;
		if (stack.getTagCompound().getBoolean("Valve"))
			name = TextFormatting.DARK_PURPLE + "Valve " + name;
		return name;
	}

	public static SoundEvent getSound(ItemStack stack, PropertyType<String> name) {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(getData(stack).getString(name)));
	}

	public static int getSlotForClass(WeaponData data, String name) {
		return data.hasProperty(PropertyType.SLOT) && data.get(PropertyType.SLOT).containsKey(name)
				? data.get(PropertyType.SLOT).get(name)
				: -1;
	}

	public static int getSlotForClass(WeaponData data, EntityTF2Character name) {
		return getSlotForClass(data, ItemToken.CLASS_NAMES[name.getClassIndex()]);
	}

	public static boolean isItemOfClassSlot(WeaponData data, int slot, String name) {
		return data.hasProperty(PropertyType.SLOT) && data.get(PropertyType.SLOT).containsKey(name)
				&& data.get(PropertyType.SLOT).get(name) == slot;
	}

	public static boolean isItemOfClass(WeaponData data, String name) {
		return data.hasProperty(PropertyType.SLOT) && data.get(PropertyType.SLOT).containsKey(name);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		/*
		 * if (!par1ItemStack.hasTagCompound()) { par1ItemStack.getTagCompound()=new
		 * NBTTagCompound(); par1ItemStack.getTagCompound().setTag("Attributes",
		 * (NBTTagCompound)
		 * ((ItemUsable)par1ItemStack.getItem()).buildInAttributes.copy()); }
		 */
		if (stack.hasTagCompound()) {
			NBTTagCompound attributeList = stack.getTagCompound().getCompoundTag("Attributes");
			// attributeList.merge(MapList.buildInAttributes.get(getData(par1ItemStack).getName()));
			Iterator<String> iterator = attributeList.getKeySet().iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				NBTBase tag = attributeList.getTag(name);
				if (tag instanceof NBTTagFloat) {
					NBTTagFloat tagFloat = (NBTTagFloat) tag;
					TF2Attribute attribute = TF2Attribute.attributes[Integer.parseInt(name)];
					// System.out.println("Attribute id: "+name);
					if (attribute != null && attribute.state != State.HIDDEN)
						tooltip.add(attribute.getTranslatedString(tagFloat.getFloat(), true));
				}
			}
			attributeList = MapList.buildInAttributes.get(getData(stack).getName());
			// attributeList.merge(MapList.buildInAttributes.get(getData(par1ItemStack).getName()));
			iterator = attributeList.getKeySet().iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				NBTBase tag = attributeList.getTag(name);
				if (tag instanceof NBTTagFloat) {
					NBTTagFloat tagFloat = (NBTTagFloat) tag;
					TF2Attribute attribute = TF2Attribute.attributes[Integer.parseInt(name)];
					// System.out.println("Attribute id: "+name);
					if (attribute != null && attribute.state != State.HIDDEN)
						tooltip.add(attribute.getTranslatedString(tagFloat.getFloat(), true));
				}
			}

			if (stack.getTagCompound().hasKey(NBTLiterals.STREAK_ATTRIB)) {
				tooltip.add("Killstreak Active");
				TF2Attribute attribute = TF2Attribute.attributes[stack.getTagCompound()
						.getShort(NBTLiterals.STREAK_ATTRIB)];
				if (attribute != null && attribute.state != State.HIDDEN)
					tooltip.add(attribute.getTranslatedString(
							ItemKillstreakKit.getKillstreakBonus(attribute,
									stack.getTagCompound().getByte(NBTLiterals.STREAK_LEVEL),
									stack.getTagCompound().getInteger(NBTLiterals.STREAK_KILLS), getData(stack)),
							true));
			}

			if (getData(stack).hasProperty(PropertyType.DESC)) {
				tooltip.add("");
				for (String line : getData(stack).getString(PropertyType.DESC).split("\n"))
					tooltip.add(line);
			}
			if (stack.getTagCompound().getBoolean("Bought")) {
				tooltip.add("");
				tooltip.add("This item cannot be destroyed");
			}
		}
	}

	public boolean hasKillstreak(ItemStack stack, int minLevel) {
		return stack.getTagCompound().getByte(NBTLiterals.STREAK_LEVEL) >= minLevel;
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return 12000;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, EntityPlayer player) {
		return false;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, EntityPlayer player) {
		return null;
	}

	@Override
	public void drawOverlay(ItemStack stack, EntityPlayer player, Tessellator tesselator, BufferBuilder buffer,
			ScaledResolution resolution) {}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return this.getMaxRage(stack, null) > 0f ? (this.getRage(stack, Minecraft.getMinecraft().player) < this
				.getMaxRage(stack, Minecraft.getMinecraft().player)) : super.showDurabilityBar(stack);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return this.getMaxRage(stack, null) > 0f
				? (1 - this.getRage(stack, Minecraft.getMinecraft().player)
						/ this.getMaxRage(stack, Minecraft.getMinecraft().player))
				: super.getDurabilityForDisplay(stack);
	}

	public RageType getRageType(ItemStack stack, EntityLivingBase living) {
		return null;
	}

	public float getMaxRage(ItemStack stack, EntityLivingBase living) {
		return 0f;
	}

	public float getRage(ItemStack stack, EntityLivingBase living) {
		return WeaponsCapability.get(living).getRage(this.getRageType(stack, living));
	}

	public void setRage(ItemStack stack, EntityLivingBase living, float value) {
		WeaponsCapability.get(living).setRage(this.getRageType(stack, living),
				MathHelper.clamp(value, 0f, this.getMaxRage(stack, living)));
	}

	public void addRage(ItemStack stack, EntityLivingBase living, float value) {
		this.setRage(stack, living, this.getRage(stack, living) + value);
	}

	public boolean isAmmoSufficient(ItemStack stack, EntityLivingBase living, boolean all) {
		return !this.searchForAmmo(living, stack).isEmpty();
	}

	public void consumeAmmoGlobal(EntityLivingBase living, ItemStack stack, int amount) {
		if (EntityDispenser.isNearDispenser(living.world, living))
			return;
		amount = this.getActualAmmoUse(stack, living, amount);
		if (living instanceof EntityTF2Character)
			((EntityTF2Character) living).useAmmo(amount);
		if (!(living instanceof EntityPlayer) || ((EntityPlayer) living).isCreative())
			return;
		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, living) != 0) {
			living.getCapability(TF2weapons.WEAPONS_CAP, null).consumeMetal(amount, false);
		}
		if (amount > 0) {
			// int
			// type=ItemFromData.getData(stack).getInt(PropertyType.AMMO_TYPE);

			// stack.getCount()-=amount;
			ItemStack stackAmmo;
			while (amount > 0 && !(stackAmmo = searchForAmmo(living, stack)).isEmpty()) {
				if (stackAmmo.getItem() instanceof ItemAmmo) {
					amount = ((ItemAmmo) stackAmmo.getItem()).consumeAmmo(living, stackAmmo, amount);
				} else {
					if (stackAmmo.getItem() instanceof ItemArrow)
						stack.getTagCompound().setTag("LastLoaded", stackAmmo.serializeNBT());
					int ammo = amount;
					amount -= stackAmmo.getCount();
					stackAmmo.shrink(ammo);

				}

			}
		}
	}

	public ItemStack searchForAmmo(EntityLivingBase owner, ItemStack stack) {
		if (EntityDispenser.isNearDispenser(owner.world, owner)
				|| (owner instanceof EntityPlayer && ((EntityPlayer) owner).capabilities.isCreativeMode))
			return ItemAmmo.STACK_FILL;

		int type = ((ItemFromData) stack.getItem()).getAmmoType(stack);

		if (type == 0 || (type == 14 && TF2ConfigVars.freeUseItems))
			return ItemAmmo.STACK_FILL;

		if (owner instanceof EntityTF2Character) {
			return ((EntityTF2Character) owner)
					.getAmmo(ItemFromData.getSlotForClass(ItemFromData.getData(stack), (EntityTF2Character) owner)) > 0
							? ItemAmmo.STACK_FILL
							: ItemStack.EMPTY;
		} else if (!(owner instanceof EntityPlayer))
			return ItemAmmo.STACK_FILL;
		int metalammo = (int) TF2Attribute.getModifier("Metal Ammo", stack, 0, owner);
		if (metalammo != 0) {
			return owner.getCapability(TF2weapons.WEAPONS_CAP, null).hasMetal(metalammo) ? ItemAmmo.STACK_FILL
					: ItemStack.EMPTY;
		}

		if (owner.world.isRemote && (type >= owner.getCapability(TF2weapons.PLAYER_CAP, null).cachedAmmoCount.length
				|| owner.getCapability(TF2weapons.PLAYER_CAP, null).cachedAmmoCount[type] > 0))
			return ItemAmmo.STACK_FILL;

		if (!owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3).isEmpty()) {
			IItemHandler inv = owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3)
					.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			// System.out.println("Ammo Search: "+inv.getSlots());
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stackCap = inv.getStackInSlot(i);
				// System.out.println("Stack: "+stackCap);
				if (!stackCap.isEmpty() && ((stackCap.getItem() instanceof ItemAmmo
						&& ((ItemAmmo) stackCap.getItem()).getTypeInt(stackCap) == type)
						|| (type == 1000 && stackCap.getItem() instanceof ItemArrow))) {
					// System.out.println("Found: "+i);
					return stackCap;
				}
			}
		}
		for (int i = 0; i < ((EntityPlayer) owner).inventory.mainInventory.size(); i++) {
			ItemStack stackInv = ((EntityPlayer) owner).inventory.mainInventory.get(i);
			if (stackInv != null && ((stackInv.getItem() instanceof ItemAmmo
					&& ((ItemAmmo) stackInv.getItem()).getTypeInt(stackInv) == type)
					|| (type == 1000 && stackInv.getItem() instanceof ItemArrow)))
				return stackInv;
		}
		return ItemStack.EMPTY;
	}

	public int getAmmoType(ItemStack stack) {
		return TF2Attribute.getModifier("No Ammo", stack, 0, null) != 0 ? 0
				: getData(stack).getInt(PropertyType.AMMO_TYPE);
	}

	public static int getAmmoAmountType(EntityPlayer owner, int type) {
		int ammoCount = 0;
		if (!owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3).isEmpty()) {
			IItemHandler inv = owner.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3)
					.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stackCap = inv.getStackInSlot(i);
				if (!stackCap.isEmpty() && stackCap.getItem() instanceof ItemAmmo
						&& ((ItemAmmo) stackCap.getItem()).getTypeInt(stackCap) == type) {
					// System.out.println("Found: "+i);
					ammoCount += ((ItemAmmo) stackCap.getItem()).getAmount(stackCap);
				} else if (type == 1000 && !stackCap.isEmpty() && stackCap.getItem() instanceof ItemArrow)
					ammoCount += stackCap.getCount();
			}
		}
		for (int i = 0; i < owner.inventory.mainInventory.size(); i++) {
			ItemStack stackInv = owner.inventory.mainInventory.get(i);
			if (!stackInv.isEmpty() && stackInv.getItem() instanceof ItemAmmo
					&& ((ItemAmmo) stackInv.getItem()).getTypeInt(stackInv) == type)
				ammoCount += ((ItemAmmo) stackInv.getItem()).getAmount(stackInv);
			else if (type == 1000 && !stackInv.isEmpty() && stackInv.getItem() instanceof ItemArrow)
				ammoCount += stackInv.getCount();
		}
		return ammoCount;
	}

	public int getAmmoAmount(EntityLivingBase owner, ItemStack stack) {

		int type = this.getAmmoType(stack);

		if (type == 0)
			return 999;

		if (type == 14 && owner instanceof EntityPlayer && TF2ConfigVars.freeUseItems) {
			return ((EntityPlayer) owner).getCooldownTracker().hasCooldown(this) ? 0 : 1;
		}
		if (EntityDispenser.isNearDispenser(owner.world, owner)
				|| (owner instanceof EntityPlayer && ((EntityPlayer) owner).capabilities.isCreativeMode))
			return 999;

		if (owner instanceof EntityTF2Character)
			return (int) (((EntityTF2Character) owner).getAmmo()
					/ TF2Attribute.getModifier("Ammo Eff", stack, 1, owner));

		if (TF2Attribute.getModifier("Ball Release", stack, 0, owner) > 0)
			stack = ItemFromData.getNewStack("sandmanball");

		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, owner) != 0) {
			return owner.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal();
		}

		int ammoCount = getAmmoAmountType((EntityPlayer) owner, type);

		return (int) (ammoCount / TF2Attribute.getModifier("Ammo Eff", stack, 1, owner));
	}

	public int getActualAmmoUse(ItemStack stack, EntityLivingBase living, int amount) {
		if (this.getAmmoType(stack) == 0 || amount == 0)
			return 0;

		if (TF2Attribute.getModifier("Metal Ammo", stack, 0, living) != 0) {
			amount = (int) TF2Attribute.getModifier("Metal Ammo", stack, 0, living);
		}
		stack.getTagCompound().setFloat("UsedAmmo", stack.getTagCompound().getFloat("UsedAmmo")
				+ amount * TF2Attribute.getModifier("Ammo Eff", stack, 1, living));
		amount = 0;
		while (stack.getTagCompound().getFloat("UsedAmmo") >= 1) {
			stack.getTagCompound().setFloat("UsedAmmo", stack.getTagCompound().getFloat("UsedAmmo") - 1);
			amount++;
		}
		return amount;
	}

	public int getVisibilityFlags(ItemStack stack, EntityLivingBase living) {
		return ItemFromData.getData(stack).getInt(PropertyType.WEAR);
	}

	public String getTranslatedName(ItemStack stack) {
		WeaponData data = getData(stack);
		String key = "weapon." + data.getName();
		return I18n.canTranslate(key) ? I18n.translateToLocal(key)
				: getData(stack).hasProperty(PropertyType.NAME) ? getData(stack).getString(PropertyType.NAME)
						: getData(stack).getName();
	}

	public boolean canSwitchTo(ItemStack stack) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public ItemMeshDefinition getMeshDefinition() {
		return stack -> {
			if (stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null)) {
				if (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).inst != ItemFromData.BLANK_DATA)
					return ClientProxy.nameToModel
							.get(stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).inst.getName());
				else if (stack.hasTagCompound())
					return ClientProxy.nameToModel.get(stack.getTagCompound().getString("Type"));
			}
			return ClientProxy.nameToModel.get("minigun");
		};
	}

	@SideOnly(Side.CLIENT)
	public void registerModels(WeaponData weapon) {
		String modelName = weapon.getString(PropertyType.RENDER);

		if (modelName == null || modelName.isEmpty())
			return;

		ModelResourceLocation model = new ModelResourceLocation(modelName, "inventory");
		ModelLoader.registerItemVariants(MapList.weaponClasses.get(weapon.getString(PropertyType.CLASS)), model);
		ClientProxy.nameToModel.put(weapon.getName(), model);
		if (weapon.hasProperty(PropertyType.RENDER_BACKSTAB)) {
			modelName = weapon.getString(PropertyType.RENDER_BACKSTAB);
			model = new ModelResourceLocation(modelName, "inventory");
			ModelLoader.registerItemVariants(MapList.weaponClasses.get(weapon.getString(PropertyType.CLASS)), model);
			ClientProxy.nameToModel.put(weapon.getName() + "/b", model);
		}
	}
}
