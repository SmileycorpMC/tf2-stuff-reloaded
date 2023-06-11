package rafradek.TF2weapons.jei;

import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import rafradek.TF2weapons.TF2weapons;

import javax.annotation.Nonnull;

@JEIPlugin
public class TF2Plugin implements IModPlugin {

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		registry.addRecipeCategories(new TF2CrafterCategory(guiHelper));
		registry.addRecipeCategories(new AmmoFurnaceCategory(guiHelper));
	}

	@Override
	public void register(@Nonnull IModRegistry registry) {
		// tf2 crafter
		registry.handleRecipes(TF2CrafterRecipeWrapper.class, r -> r, TF2CrafterCategory.ID);
		registry.addRecipes(TF2CrafterCategory.getRecipes(), TF2CrafterCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(TF2weapons.blockCabinet), TF2CrafterCategory.ID);
		// ammo furnace fuel
		registry.addRecipeCatalyst(new ItemStack(TF2weapons.blockAmmoFurnace), VanillaRecipeCategoryUid.FUEL);
		// ammo furnace
		registry.handleRecipes(AmmoFurnaceCategory.Wrapper.class, r -> r, AmmoFurnaceCategory.ID);
		registry.addRecipes(AmmoFurnaceCategory.getRecipes(), AmmoFurnaceCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(TF2weapons.blockAmmoFurnace), AmmoFurnaceCategory.ID);
	}

}
