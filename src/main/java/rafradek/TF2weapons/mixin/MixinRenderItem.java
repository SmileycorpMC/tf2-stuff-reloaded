package rafradek.TF2weapons.mixin;

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

    @Shadow protected abstract void renderItemModel(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transforms, boolean leftHanded);

    @Shadow public abstract IBakedModel getItemModelWithOverrides(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity);


    @Inject(at=@At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V", cancellable = true)
    private void renderItem(ItemStack stack, EntityLivingBase entity, ItemCameraTransforms.TransformType transforms, boolean leftHanded, CallbackInfo callback) {
        renderItem(stack, entity, entity.world, transforms, leftHanded, callback);
    }

    @Inject(at=@At("HEAD"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V", cancellable = true)
    private void renderItem(ItemStack stack, ItemCameraTransforms.TransformType transforms, CallbackInfo callback) {
        renderItem(stack, null, null, transforms, false, callback);
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
