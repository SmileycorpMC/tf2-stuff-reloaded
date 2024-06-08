package rafradek.TF2weapons.client;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.item.ItemWearable;
import rafradek.TF2weapons.util.TF2Class;

import java.util.Map;

public class PyrolandRenderer {

    private static final ResourceLocation PYROLAND_SHADER = new ResourceLocation("rafradek_tf2_weapons", "shaders/post/pyrovision.json");

    public static PyrolandRenderer INSTANCE = new PyrolandRenderer();

    private final Map<ResourceLocation, ResourceLocation> SOUND_MAP = Maps.newHashMap();
    private final Map<ModelResourceLocation, ModelResourceLocation> MODEL_MAP = Maps.newHashMap();

    private PyrolandRenderer() {
        for (TF2Class clazz : TF2Class.getClasses()) {
            String name = clazz.getName();
            registerSoundReplacement(new ResourceLocation(TF2weapons.MOD_ID, "mob."+ name +".hurt"),
                    new ResourceLocation(TF2weapons.MOD_ID, "mob."+ name +".laughshort"));
            registerSoundReplacement(new ResourceLocation(TF2weapons.MOD_ID, "mob."+ name +".death"),
                    new ResourceLocation(TF2weapons.MOD_ID, "mob."+ name +".laughhappy"));
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (shouldRenderPyrovision()) if (!isShaderLoaded()) enableShader();
        else if (isShaderLoaded()) disableShader();
    }

    @SubscribeEvent
    public void playSound(PlaySoundEvent event) {
        ISound sound = event.getSound();
        SoundCategory cat = sound.getCategory();
        if (!(cat == SoundCategory.HOSTILE || cat == SoundCategory.PLAYERS || cat == SoundCategory.NEUTRAL)) return;
        if (sound instanceof PositionedSound && shouldRenderPyrovision()) {
            PositionedSound positionedsound = (PositionedSound) sound;
            positionedsound.pitch = ((PositionedSound)sound).pitch * 1.5f;
            if (SOUND_MAP.containsKey(sound.getSoundLocation()))
                positionedsound.positionedSoundLocation = SOUND_MAP.get(sound.getSoundLocation());
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
        for (EnumHand hand : EnumHand.values()) if (shouldRenderPyrovision(entity.getHeldItem(hand), entity, false)) return true;
        if (shouldRenderPyrovision(entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD), entity, true)) return true;
        if (!entity.hasCapability(TF2weapons.INVENTORY_CAP, null)) return false;
        for (int i = 0; i < 2; i++) if (shouldRenderPyrovision(entity.getCapability(TF2weapons.INVENTORY_CAP, null)
                .getStackInSlot(i), entity, true)) return true;
        return false;
    }

    private boolean shouldRenderPyrovision(ItemStack stack, EntityLivingBase entity, boolean isWorn) {
        if (stack == null) return false;
        return (!(stack.getItem() instanceof ItemWearable) || isWorn)
                && TF2Attribute.getModifier("Pyrovision", stack, 0, entity) > 0;
    }

    private boolean isShaderLoaded() {
        ShaderGroup shader = Minecraft.getMinecraft().entityRenderer.getShaderGroup();
        return shader != null && PYROLAND_SHADER.toString().equals(shader.getShaderGroupName());
    }

    private void enableShader() {
        Minecraft.getMinecraft().entityRenderer.loadShader(PYROLAND_SHADER);
    }

    private void disableShader() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
    }

    public void registerSoundReplacement(ResourceLocation sound, ResourceLocation replacement) {
        SOUND_MAP.put(sound, replacement);
    }

    public void registerModelReplacement(ModelResourceLocation model, ModelResourceLocation replacement) {
        MODEL_MAP.put(model, replacement);
    }

    public boolean hasReplacementModel(ModelResourceLocation loc) {
        return MODEL_MAP.containsKey(loc);
    }

    public ModelResourceLocation getReplacementModel(ModelResourceLocation loc) {
        return MODEL_MAP.get(loc);
    }

}
