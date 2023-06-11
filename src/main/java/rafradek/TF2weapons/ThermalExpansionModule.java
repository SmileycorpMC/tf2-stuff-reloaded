package rafradek.TF2weapons;

import cofh.api.util.ThermalExpansionHelper;
import com.google.common.collect.Lists;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ThermalExpansionModule {

	public static void init() {
		ThermalExpansionHelper.addCentrifugeRecipe(8000, new ItemStack(TF2weapons.itemAmmoMinigun),
				Lists.newArrayList(new ItemStack(TF2weapons.itemTF2, 2, 1), new ItemStack(TF2weapons.itemTF2, 2, 0),
						new ItemStack(Items.GUNPOWDER, 3)));
		ThermalExpansionHelper.addCentrifugeRecipe(4000, new ItemStack(TF2weapons.itemAmmo, 13, 1),
				Lists.newArrayList(new ItemStack(TF2weapons.itemTF2, 1, 1), new ItemStack(TF2weapons.itemTF2, 1, 0),
						new ItemStack(Items.GUNPOWDER, 1)));
		ThermalExpansionHelper.addCentrifugeRecipe(4000, new ItemStack(TF2weapons.itemAmmoPistol, 4, 0),
				Lists.newArrayList(new ItemStack(TF2weapons.itemTF2, 1, 1), new ItemStack(TF2weapons.itemTF2, 1, 0),
						new ItemStack(Items.GUNPOWDER, 1)));
		ThermalExpansionHelper.addCentrifugeRecipe(4000, new ItemStack(TF2weapons.itemAmmo, 16, 4),
				Lists.newArrayList(new ItemStack(TF2weapons.itemTF2, 1, 1), new ItemStack(TF2weapons.itemTF2, 1, 0),
						new ItemStack(Items.GUNPOWDER, 1)));
		ThermalExpansionHelper.addCentrifugeRecipe(4000, new ItemStack(TF2weapons.itemAmmo, 5, 6),
				Lists.newArrayList(new ItemStack(TF2weapons.itemTF2, 1, 1), new ItemStack(TF2weapons.itemTF2, 1, 0),
						new ItemStack(Items.GUNPOWDER, 1)));
		ThermalExpansionHelper.addCentrifugeRecipe(6000, new ItemStack(TF2weapons.itemAmmo, 20, 7),
				Lists.newArrayList(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Blocks.TNT, 1)));
		ThermalExpansionHelper.addCentrifugeRecipe(6000, new ItemStack(TF2weapons.itemAmmo, 20, 8),
				Lists.newArrayList(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Blocks.TNT, 1)));
		ThermalExpansionHelper.addCentrifugeRecipe(6000, new ItemStack(TF2weapons.itemAmmo, 16, 11),
				Lists.newArrayList(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Blocks.TNT, 1)));
		ThermalExpansionHelper.addCentrifugeRecipe(4000, new ItemStack(TF2weapons.itemAmmo, 10, 13),
				Lists.newArrayList(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.REDSTONE, 1)));
		ThermalExpansionHelper.addCentrifugeRecipe(6000, new ItemStack(TF2weapons.itemAmmoFire),
				Lists.newArrayList(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.MAGMA_CREAM, 1)));
		ThermalExpansionHelper.addSmelterRecipe(4000, new ItemStack(TF2weapons.itemAmmoMinigun),
				new ItemStack(Blocks.SAND), new ItemStack(TF2weapons.itemTF2, 2, 1),
				new ItemStack(TF2weapons.itemTF2, 2, 0), 100);
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmo, 13, 1),
				new ItemStack(Blocks.SAND), new ItemStack(TF2weapons.itemTF2, 1, 1),
				new ItemStack(TF2weapons.itemTF2, 1, 0), 100);
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmoPistol, 4, 0),
				new ItemStack(Blocks.SAND), new ItemStack(TF2weapons.itemTF2, 1, 1),
				new ItemStack(TF2weapons.itemTF2, 1, 0), 100);
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmo, 16, 4),
				new ItemStack(Blocks.SAND), new ItemStack(TF2weapons.itemTF2, 1, 1),
				new ItemStack(TF2weapons.itemTF2, 1, 0), 100);
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmo, 5, 6),
				new ItemStack(Blocks.SAND), new ItemStack(TF2weapons.itemTF2, 1, 1),
				new ItemStack(TF2weapons.itemTF2, 1, 0), 100);
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmo, 20, 7),
				new ItemStack(Blocks.SAND), new ItemStack(Items.IRON_INGOT, 2));
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmo, 20, 8),
				new ItemStack(Blocks.SAND), new ItemStack(Items.IRON_INGOT, 2));
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmo, 16, 11),
				new ItemStack(Blocks.SAND), new ItemStack(Items.IRON_INGOT, 2));
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmo, 10, 13),
				new ItemStack(Blocks.SAND), new ItemStack(Items.IRON_INGOT, 1));
		ThermalExpansionHelper.addSmelterRecipe(2000, new ItemStack(TF2weapons.itemAmmoFire),
				new ItemStack(Blocks.SAND), new ItemStack(Items.IRON_INGOT, 2));
		TF2weapons.LOGGER.info("Added thermal expansion recipes");
	}
}
