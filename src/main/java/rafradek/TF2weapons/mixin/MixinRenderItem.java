package rafradek.TF2weapons.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.PyrolandRenderer;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.WeaponData;

import javax.annotation.Nullable;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    private static boolean silhouette;

    @Shadow protected abstract void renderItemModel(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transforms, boolean leftHanded);

    @Shadow public abstract IBakedModel getItemModelWithOverrides(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity);

    @Shadow public abstract void renderItem(ItemStack stack, EntityLivingBase entitylivingbaseIn, ItemCameraTransforms.TransformType transform, boolean leftHanded);

    @Shadow public abstract void renderItem(ItemStack stack, ItemCameraTransforms.TransformType cameraTransformType);

    @Shadow public abstract void renderItemIntoGUI(ItemStack stack, int x, int y);

    @Shadow protected abstract void renderModel(IBakedModel model, int color, ItemStack stack);

    @Shadow public float zLevel;

    @Inject(at=@At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V", cancellable = true)
    private void renderItem(ItemStack stack, EntityLivingBase entity, ItemCameraTransforms.TransformType transforms, boolean leftHanded, CallbackInfo callback) {
        if (transforms == ItemCameraTransforms.TransformType.GUI && stack.getItem() == TF2weapons.itemTF2 && (stack.getMetadata() == 9 || stack.getMetadata() == 10)) {
            ItemStack weapon = ItemFromData.getDisplayWeapon(stack, Minecraft.getMinecraft().world.getWorldTime());
            if (!weapon.isEmpty()) {
                silhouette = true;
                zLevel -= 50;
                renderItem(weapon, entity, transforms, leftHanded);
            }
        }
        renderItem(stack, entity, entity.world, transforms, leftHanded, callback);
    }

    @Inject(at=@At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V", cancellable = true)
    private void renderItem(ItemStack stack, ItemCameraTransforms.TransformType transforms, CallbackInfo callback) {
        if (transforms == ItemCameraTransforms.TransformType.GUI && stack.getItem() == TF2weapons.itemTF2 && (stack.getMetadata() == 9 || stack.getMetadata() == 10)) {
            ItemStack weapon = ItemFromData.getDisplayWeapon(stack, Minecraft.getMinecraft().world.getWorldTime());
            if (!weapon.isEmpty()) {
                silhouette = true;
                zLevel -= 50;
                renderItem(weapon, transforms);
            }
        }
        renderItem(stack, null, null, transforms, false, callback);
    }

    @Inject(at=@At("HEAD"), method = "renderItemModelIntoGUI", cancellable = true)
    private void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel, CallbackInfo callback) {
        if (stack.getItem() == TF2weapons.itemTF2 && (stack.getMetadata() == 9 || stack.getMetadata() == 10)) {
            ItemStack weapon = ItemFromData.getDisplayWeapon(stack, Minecraft.getMinecraft().world.getWorldTime());
            if (!weapon.isEmpty()) {
                silhouette = true;
                zLevel -= 50;
                renderItemIntoGUI(weapon, x, y);
            }
        }
    }

    @Inject(at=@At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", cancellable = true)
    private void renderItem(ItemStack stack, IBakedModel model, CallbackInfo callback) {
        if (silhouette) {
            callback.cancel();
            if (!stack.isEmpty())
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                renderModel(model,0xFF000000, stack);
                GlStateManager.popMatrix();
            }
            silhouette = false;
            zLevel += 50;
        }
    }

    private void renderItem(ItemStack stack, EntityLivingBase entity, World world, ItemCameraTransforms.TransformType transforms, boolean leftHanded, CallbackInfo callback) {
        if (!(stack.getItem() instanceof ItemFromData && stack.hasCapability(TF2weapons.WEAPONS_DATA_CAP, null))) return;
        if (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).getAttributeValue(stack, "Visible in Pyroland", 0) <= 0 ||
                PyrolandRenderer.INSTANCE.shouldRenderPyrovision() |! isThirdPerson(transforms)) return;
        callback.cancel();
        WeaponData data = ItemFromData.getData(stack);
        if (data == null |! data.hasProperty(PropertyType.BASED_ON)) return;
        String base = data.get(PropertyType.BASED_ON);
        if (base == null) return;
        ItemStack basestack = ItemFromData.getNewStack(base);
        if (basestack == null || basestack.isEmpty()) return;
        renderItemModel(basestack, getItemModelWithOverrides(basestack, world, entity), transforms, leftHanded);
    }

    private boolean isThirdPerson(ItemCameraTransforms.TransformType transforms) {
        return transforms == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND ||
                transforms == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND ||
                transforms == ItemCameraTransforms.TransformType.HEAD ||
                transforms == ItemCameraTransforms.TransformType.GROUND;
    }

}
