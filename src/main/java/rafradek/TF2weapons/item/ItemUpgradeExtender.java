package rafradek.TF2weapons.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import rafradek.TF2weapons.TF2weapons;

import java.util.Collections;

public class ItemUpgradeExtender extends Item {

	public ItemUpgradeExtender() {
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
	}

	public static int getUpgradeId(NonNullList<ItemStack> stacks, int... count) {
		int total = 0;
		Collections.sort(stacks, (stack1, stack2) -> (stack1.getItemDamage() > stack2.getItemDamage() ? 1
				: (stack1.getItemDamage() == stack2.getItemDamage() ? 0 : -1)));
		int[] countused = new int[3];
		for (ItemStack stack : stacks) {
			if (stack.getItem() instanceof ItemRobotPart) {
				int level = ItemRobotPart.getLevel(stack);
				int variant = ItemRobotPart.getVariant(stack);

				variant = variant + (countused[level] * 3);
				total += variant;
				;
				countused[level]++;
			}
		}
		return 0;
	}
}
