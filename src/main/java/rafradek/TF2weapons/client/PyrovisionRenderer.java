package rafradek.TF2weapons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemPyrovision;

public class PyrovisionRenderer {

    private static boolean pyrovison_enabled;

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (shouldRenderPyrovision()) {
            if (!isShaderLoaded()) {
                enableShader();
                pyrovison_enabled = false;
            }
        } else if (isShaderLoaded()) {
            disableShader();
            pyrovison_enabled = false;
        }
    }

    private boolean shouldRenderPyrovision() {
        Minecraft mc = Minecraft.getMinecraft();
        Entity entity = mc.getRenderViewEntity();
        if (entity != null) return entity instanceof EntityLivingBase ? shouldRenderPyrovision((EntityLivingBase) entity) : false;
        return shouldRenderPyrovision(mc.player);
    }

    private boolean shouldRenderPyrovision(EntityLivingBase entity) {
        if (entity == null) return false;
        ItemStack hat = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (shouldRenderPyrovision(entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD))) return true;
        if (!entity.hasCapability(TF2weapons.INVENTORY_CAP, null)) return false;
        for (int i = 0; i < 2; i++) {
            if (shouldRenderPyrovision(entity.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(i))) return true; ;
        }
        return false;
    }

    private boolean shouldRenderPyrovision(ItemStack stack) {
        if (stack == null) return false;
        return stack.getItem() instanceof ItemPyrovision;
    }

    private boolean isShaderLoaded() {
        return Minecraft.getMinecraft().getRenderViewEntity() == null && pyrovison_enabled;
    }

    private void enableShader() {
        Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation("rafradek_tf2_weapons", "shaders/post/pyrovision.json"));
        pyrovison_enabled = true;
    }

    private void disableShader() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
        pyrovison_enabled = false;
    }


}
