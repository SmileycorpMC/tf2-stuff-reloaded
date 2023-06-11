package rafradek.TF2weapons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemPyrovision;

public class PyrovisionRenderer {

    private static final ResourceLocation PYROVISION_SHADER = new ResourceLocation("rafradek_tf2_weapons", "shaders/post/pyrovision.json");

    public static PyrovisionRenderer INSTANCE = new PyrovisionRenderer();

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (shouldRenderPyrovision()) {
            if (!isShaderLoaded()) {
                enableShader();
            }
        } else if (isShaderLoaded()) {
            disableShader();
        }
    }

    @SubscribeEvent
    public void playSound(PlaySoundEvent event) {
        ISound sound = event.getSound();
        if (sound instanceof PositionedSound && shouldRenderPyrovision()) {
            ((PositionedSound)sound).pitch = ((PositionedSound)sound).pitch * 1.5f;
        }
    }

    public boolean shouldRenderPyrovision() {
        Minecraft mc = Minecraft.getMinecraft();
        Entity entity = mc.getRenderViewEntity();
        if (entity instanceof EntityLivingBase) return shouldRenderPyrovision((EntityLivingBase) entity);
        return shouldRenderPyrovision(mc.player);
    }

    private boolean shouldRenderPyrovision(EntityLivingBase entity) {
        if (entity == null) return false;
        if (shouldRenderPyrovision(entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD))) return true;
        if (!entity.hasCapability(TF2weapons.INVENTORY_CAP, null)) return false;
        for (int i = 0; i < 2; i++) {
            if (shouldRenderPyrovision(entity.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(i))) return true;
        }
        return false;
    }

    private boolean shouldRenderPyrovision(ItemStack stack) {
        if (stack == null) return false;
        return stack.getItem() instanceof ItemPyrovision;
    }

    private boolean isShaderLoaded() {
        ShaderGroup shader = Minecraft.getMinecraft().entityRenderer.getShaderGroup();
        return shader != null && PYROVISION_SHADER.toString().equals(shader.getShaderGroupName());
    }

    private void enableShader() {
        Minecraft.getMinecraft().entityRenderer.loadShader(PYROVISION_SHADER);
    }

    private void disableShader() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
    }


}
