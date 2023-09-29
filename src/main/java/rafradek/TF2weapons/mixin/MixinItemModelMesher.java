package rafradek.TF2weapons.mixin;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rafradek.TF2weapons.client.PyrolandRenderer;

import java.util.Map;

@Mixin(ItemModelMesher.class)
public abstract class MixinItemModelMesher {

    @Shadow @Final private ModelManager modelManager;

    @Shadow @Final protected Map<Item, ItemMeshDefinition> shapers;

    @Inject(at=@At("TAIL"), method = "getItemModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/renderer/block/model/IBakedModel;", cancellable = true)
    private void getItemModel(ItemStack stack, CallbackInfoReturnable<IBakedModel> callback) {
        if (!PyrolandRenderer.INSTANCE.shouldRenderPyrovision()) return;
        ModelResourceLocation loc = shapers.get(stack.getItem()).getModelLocation(stack);
        if (PyrolandRenderer.INSTANCE.hasReplacementModel(loc)) {
            callback.setReturnValue(modelManager.getModel(PyrolandRenderer.INSTANCE.getReplacementModel(loc)));
        }
    }

}
