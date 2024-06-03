package rafradek.TF2weapons.loot;

import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import rafradek.TF2weapons.item.ItemFromData;

import java.util.Random;

public class RandomWeaponItemFunction extends LootFunction {

	public RandomWeaponItemFunction() {
		super(new LootCondition[]{});
	}

	@Override
	public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
		//loottweaker support
		if (rand == null && context == null) return stack;
		if (!(stack.getMetadata() == 9 || stack.getMetadata() == 10)) return stack;
		return ItemFromData.getRandomWeapon(stack, rand);
	}

}
