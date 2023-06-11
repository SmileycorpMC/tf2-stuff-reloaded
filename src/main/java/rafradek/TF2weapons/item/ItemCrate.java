package rafradek.TF2weapons.item;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.WeaponData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ItemCrate extends ItemFromData {

	public static class PropertyContent extends PropertyType<CrateContent> {

		public PropertyContent(int id, String name, Class<CrateContent> type) {
			super(id, name, type);
		}

		@Override
		public CrateContent deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			CrateContent content = new CrateContent();
			HashMap<String, Integer> map = new HashMap<>();
			for (Entry<String, JsonElement> attribute : json.getAsJsonObject().entrySet()) {
				String itemName = attribute.getKey();
				int chance = attribute.getValue().getAsInt();
				content.content.put(itemName, chance);
				content.maxCrateValue += chance;
			}
			return content;
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, CrateContent value) throws IOException {
			buf.writeByte(value.content.size());
			for (Entry<String, Integer> entry : value.content.entrySet()) {
				buf.writeUTF(entry.getKey());
				buf.writeShort(entry.getValue());
			}
		}

		@Override
		public CrateContent deserialize(DataInput buf, WeaponData data) throws IOException {
			int attributeCount = buf.readByte();
			CrateContent content = new CrateContent();
			for (int i = 0; i < attributeCount; i++) {
				String entry = buf.readUTF();
				int value = buf.readShort();
				content.content.put(entry, value);
				content.maxCrateValue += value;
			}
			return content;
		}
	}

	public static class CrateContent {
		public HashMap<String, Integer> content = new HashMap<>();
		public int maxCrateValue;
	}

	public ItemCrate() {
		this.setMaxStackSize(64);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (!worldIn.isRemote && !itemStackIn.getTagCompound().getBoolean("Open")) {
			if (playerIn.inventory.hasItemStack(new ItemStack(TF2weapons.itemTF2, 1, 7))) {
				itemStackIn.getTagCompound().setBoolean("Open", true);
				playerIn.inventory.clearMatchingItems(TF2weapons.itemTF2, 7, 1, null);
			}
		}
		if (!worldIn.isRemote && itemStackIn.getTagCompound().getBoolean("Open")) {
			// ArrayList<String> list = new ArrayList<String>();

			ItemStack stack = ItemStack.EMPTY;
			if (playerIn.getRNG().nextInt(32) == 0) {
				stack = ItemFromData.getRandomWeaponOfClass("cosmetic", playerIn.getRNG(), false);
				((ItemWearable) stack.getItem()).applyRandomEffect(stack, playerIn.getRNG());
			} else {
				int choosen = playerIn.getRNG().nextInt(getData(itemStackIn).get(PropertyType.CONTENT).maxCrateValue);
				int currVal = 0;
				for (Entry<String, Integer> entry : getData(itemStackIn).get(PropertyType.CONTENT).content.entrySet()) {
					currVal += entry.getValue();
					if (choosen < currVal) {
						stack = ItemFromData.getNewStack(entry.getKey());
						break;
					}
					/*
					 * for (int i = 0; i < entry.getValue(); i++) list.add(entry.getKey());
					 */
				}
			}
			//
			if (!(stack.getItem() instanceof ItemWearable))
				stack.getTagCompound().setBoolean("Strange", true);

			if (!playerIn.inventory.addItemStackToInventory(stack))
				playerIn.dropItem(stack, true);
			// playerIn.addStat(TF2Achievements.LOOT_CRATE);
			playerIn.addStat(TF2weapons.cratesOpened);
			/*
			 * if(!worldIn.isRemote &&
			 * ((EntityPlayerMP)playerIn).getStatFile().readStat(TF2weapons.cratesOpened)>=9
			 * ){ playerIn.addStat(TF2Achievements.CRATES_10); }
			 */
			itemStackIn.shrink(1);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
		} else
			return new ActionResult<>(EnumActionResult.FAIL, itemStackIn);
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
			super.addInformation(stack, world, tooltip, advanced);

			if (stack.getTagCompound().getBoolean("Open")) {
				tooltip.add("The crate is opened now");
				tooltip.add("Right click to get the item");
			}

			tooltip.add("Possible content:");
			for (String name : getData(stack).get(PropertyType.CONTENT).content.keySet()) {
				WeaponData data = MapList.nameToData.get(name);
				if (data != null)
					tooltip.add(I18n.format("weapon." + data.getName()));
			}
		}
	}

	@Override
	public int getItemBurnTime(ItemStack itemStack) {
		return 2400;
	}
}
