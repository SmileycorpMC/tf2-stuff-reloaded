package rafradek.TF2weapons.jei;

import com.google.common.collect.Lists;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.TF2Class;

import javax.annotation.Nullable;
import java.util.List;

public class RandomWeaponCategory implements IRecipeCategory<RandomWeaponCategory.Wrapper> {

    public static final String ID = new ResourceLocation(TF2weapons.MOD_ID, "random_weapons").toString();
    public static final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
            "textures/gui/jei/random_weapons.png");
    private final IDrawable background;
    private final IDrawable icon;

    public RandomWeaponCategory(IGuiHelper guiHelper) {
        background = guiHelper.createDrawable(TEXTURE, 0, 0, 168, 114);
        icon = guiHelper.createDrawableIngredient(new ItemStack(TF2weapons.itemTF2, 1, 9));
    }

    @Nullable
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public String getModName() {
        return TF2weapons.MOD_ID;
    }

    @Override
    public String getTitle() {
        return new TextComponentTranslation("jei.category.rafradek_tf2_weapons.RandomWeapons").getFormattedText();
    }

    @Override
    public String getUid() {
        return ID;
    }

    public static List<Wrapper> getRecipes() {
        List<Wrapper> recipes = Lists.newArrayList();
        recipes.add(new Wrapper(new ItemStack(TF2weapons.itemTF2, 1, 10)));
        ItemStack stack = new ItemStack(TF2weapons.itemTF2, 1, 9);
        recipes.add(new Wrapper(stack));
        for (TF2Class clazz : TF2Class.getClasses()) {
            ItemStack newStack = stack.copy();
            newStack.setTagCompound(new NBTTagCompound());
            newStack.getTagCompound().setByte("Token", (byte)clazz.getIndex());
            recipes.add(new Wrapper(newStack));
        }
        return recipes;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, Wrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup items = layout.getItemStacks();
        List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);
        items.init(0, false, 75, 6);
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 9; ++x) {
                int index = 1 + x + (y * 9);
                items.init(index, true, 3+ x * 18, 39 + y * 18);
                items.set(index, outputs.get(y).get(x));
            }
        }
        items.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
    }

    public static class Wrapper implements IRecipeWrapper {

        private final ItemStack input;
        private final List<List<ItemStack>> outputs = Lists.newArrayList();

        public Wrapper(ItemStack input) {
            this.input = input;
            List<ItemStack> stacks = ItemFromData.getRandomWeapons(input);
            for (int y = 0; y < 4; y++) {
                List<ItemStack> list = Lists.newArrayList();
                for (int x = 0; x < 9; x++) {
                    int i = y*9 + x;
                    list.add(i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
                }
                outputs.add(list);
            }
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            List<List<ItemStack>> inputs = Lists.newArrayList();
            inputs.add(Lists.newArrayList(input));
            ingredients.setInputLists(VanillaTypes.ITEM, inputs);
            ingredients.setOutputLists(VanillaTypes.ITEM, outputs);
        }

    }
}
