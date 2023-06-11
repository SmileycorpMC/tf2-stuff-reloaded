package rafradek.TF2weapons.item;

import com.google.common.base.Predicate;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public abstract class ItemFabricator extends Item {

	public static class PropertyItemList extends PropertyType<ItemList> {

		public PropertyItemList(int id, String name, Class<ItemList> type) {
			super(id, name, type);
		}

		@Override
		public ItemList deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			ItemList content = new ItemList();
			for (Entry<String, JsonElement> attribute : json.getAsJsonObject().entrySet()) {
				String itemName = attribute.getKey();
				int chance = attribute.getValue().getAsInt();
				content.items.put(itemName, chance);
			}
			return content;
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, ItemList value) throws IOException {
			buf.writeByte(value.items.size());
			for (Entry<String, Integer> entry : value.items.entrySet()) {
				buf.writeUTF(entry.getKey());
				buf.writeShort(entry.getValue());
			}
		}

		@Override
		public ItemList deserialize(DataInput buf, WeaponData data) throws IOException {
			int attributeCount = buf.readByte();
			ItemList content = new ItemList();
			for (int i = 0; i < attributeCount; i++) {
				String entry = buf.readUTF();
				int value = buf.readShort();
				content.items.put(entry, value);
			}
			return content;
		}
	}

	public static class ItemList {
		public HashMap<String, Integer> items = new HashMap<>();
	}

	public static abstract class TF2Ingredient implements Predicate<ItemStack> {

		public abstract String getName();

		public abstract int getCount();
	}

	public static class IngredientItemStack extends TF2Ingredient {

		public ItemStack test;

		public IngredientItemStack(ItemStack test) {
			super();
			this.test = test;
		}

		@Override
		public boolean apply(ItemStack input) {
			return ItemHandlerHelper.canItemStacksStack(input, test);
		}

		@Override
		public String getName() {
			return I18n.format(test.getUnlocalizedName() + ".name");
		}

		@Override
		public int getCount() {
			return test.getCount();
		}

	}

	public static class IngredientPredicate extends TF2Ingredient {

		public Predicate<ItemStack> test;
		public int count;
		public String name;

		public IngredientPredicate(Predicate<ItemStack> test, int count, String name) {
			super();
			this.test = test;
			this.name = name;
			this.count = count;
		}

		@Override
		public boolean apply(ItemStack input) {
			return test.apply(input);
		}

		@Override
		public String getName() {
			return I18n.format(name);
		}

		@Override
		public int getCount() {
			return count;
		}

	}

	public abstract NonNullList<TF2Ingredient> getInput(ItemStack stack, EntityPlayer player);

	public abstract NonNullList<ItemStack> getOutput(ItemStack stack, EntityPlayer player);

	@SideOnly(Side.CLIENT)
	public List<String> getOutputNames(ItemStack stack) {
		ArrayList<String> list = new ArrayList<>();
		for (ItemStack output : getOutput(stack, null)) {
			list.add(output.getCount() + "x " + I18n.format(output.getUnlocalizedName() + ".name"));
		}
		return list;
	}

	@SideOnly(Side.CLIENT)
	public List<String> getInputNames(ItemStack stack) {
		ArrayList<String> list = new ArrayList<>();
		for (TF2Ingredient input : getInput(stack, null)) {
			list.add(input.getCount() + "x " + input.getName());
		}
		return list;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("When used, this item will produce");
		tooltip.addAll(this.getOutputNames(stack));
		tooltip.add("");
		tooltip.add("After consuming those items from inventory");
		tooltip.addAll(this.getInputNames(stack));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer living, EnumHand hand) {
		if (!world.isRemote) {
			ItemStack stack = living.getHeldItem(hand);
			IItemHandler handler = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for (TF2Ingredient input : getInput(stack, living)) {
				if (!TF2Util.hasEnoughItem(handler, input, input.getCount()))
					return new ActionResult<>(EnumActionResult.FAIL, living.getHeldItem(hand));
			}
			for (TF2Ingredient input : getInput(stack, living)) {
				TF2Util.removeItemsMatching(handler, input.getCount(), input);
			}
			for (ItemStack out : getOutput(stack, living))
				ItemHandlerHelper.giveItemToPlayer(living, out);

			stack.shrink(1);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, living.getHeldItem(hand));
	}
}
