package rafradek.TF2weapons.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemKillstreakFabricator extends ItemFabricator {

	public static Map<TF2Attribute, NonNullList<TF2Ingredient>> standardInput = new HashMap<>();
	public static Map<TF2Attribute, NonNullList<TF2Ingredient>> specializedInput = new HashMap<>();
	public static Map<TF2Attribute, NonNullList<TF2Ingredient>> proInput = new HashMap<>();

	public ItemKillstreakFabricator() {
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setMaxStackSize(1);
	}

	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (TF2Attribute attribute : TF2Attribute.attributes) {
			if (attribute != null && attribute.perKill != 0) {
				for (int level = 0; level < 3; level++) {
					par3List.add(new ItemStack(this, 1, attribute.id + (level << 9)));
				}
			}
		}
	}

	public int getLevel(ItemStack stack) {
		return (stack.getMetadata() >> 9) + 1;
	}

	@Override
	public NonNullList<TF2Ingredient> getInput(ItemStack stack, EntityPlayer player) {
		TF2Attribute attr = this.getAttribute(stack);
		switch (this.getLevel(stack)) {
		case 1:
			return standardInput.get(attr);
		case 2:
			return specializedInput.get(attr);
		case 3:
			return proInput.get(attr);
		default:
			return null;
		}
	}

	@Override
	public NonNullList<ItemStack> getOutput(ItemStack stack, EntityPlayer player) {
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(new ItemStack(TF2weapons.itemKillstreak, 1, stack.getMetadata()));
		return list;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + TF2weapons.MOD_ID + ".killstreakfabricator."
				+ ItemKillstreakKit.NAMES[MathHelper.clamp(this.getLevel(stack) - 1, 0, 2)];
	}

	public TF2Attribute getAttribute(ItemStack stack) {
		return TF2Attribute.attributes[MathHelper.clamp(stack.getMetadata() & 511, 0, TF2Attribute.attributes.length)];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {

		TF2Attribute attrib = this.getAttribute(stack);

		if (attrib != null) {
			tooltip.add("For each kill gain up to:");
			tooltip.add(attrib.getTranslatedString(
					attrib.defaultValue
							+ attrib.perKill * ItemKillstreakKit.getBonusMult(this.getLevel(stack), attrib, null),
					true));
		}
		tooltip.add("");
		super.addInformation(stack, world, tooltip, advanced);
	}

	public static void initKillstreaks() {
		int i = 0;
		for (TF2Attribute attribute : TF2Attribute.attributes) {
			if (attribute != null && attribute.perKill != 0) {
				int meta1 = ItemRobotPart.LEVEL1[i % ItemRobotPart.LEVEL1.length];
				int meta2 = ItemRobotPart.LEVEL2[i % ItemRobotPart.LEVEL2.length];
				int meta3 = ItemRobotPart.LEVEL3[i % ItemRobotPart.LEVEL3.length];
				NonNullList<TF2Ingredient> list = NonNullList.create();
				list.add(new IngredientItemStack(new ItemStack(TF2weapons.itemRobotPart, 12, meta1)));
				standardInput.put(attribute, list);
				list = NonNullList.create();
				list.add(new IngredientItemStack(new ItemStack(TF2weapons.itemRobotPart, 12, meta2)));
				list.add(new IngredientItemStack(new ItemStack(TF2weapons.itemRobotPart, 8, meta1)));
				list.add(new IngredientPredicate(
						stack -> ((stack.getItem() instanceof ItemKillstreakKit
								&& ((ItemKillstreakKit) stack.getItem()).getLevel(stack) == 0)
								|| (stack.getItem() instanceof ItemFromData
										&& stack.getTagCompound().getByte(NBTLiterals.STREAK_LEVEL) > 0)),
						1, "Killstreak kit or weapon"));
				specializedInput.put(attribute, list);
				list = NonNullList.create();
				list.add(new IngredientItemStack(new ItemStack(TF2weapons.itemRobotPart, 9, meta3)));
				list.add(new IngredientItemStack(new ItemStack(TF2weapons.itemRobotPart, 8, meta2)));
				list.add(new IngredientPredicate(
						stack -> ((stack.getItem() instanceof ItemKillstreakKit
								&& ((ItemKillstreakKit) stack.getItem()).getLevel(stack) == 1)
								|| (stack.getItem() instanceof ItemFromData
										&& stack.getTagCompound().getByte(NBTLiterals.STREAK_LEVEL) > 1)),
						1, "Specialized killstreak kit or weapon"));
				proInput.put(attribute, list);
				i++;
			}
		}
	}
}
