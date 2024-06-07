package rafradek.TF2weapons.client.audio;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemUsable;
import rafradek.TF2weapons.util.TF2Util;
import rafradek.TF2weapons.util.WeaponData;

public class WeaponLoopSound extends WeaponSound {

	private boolean firing;
	private boolean crit;

	public WeaponLoopSound(SoundEvent sound, EntityLivingBase entity, boolean firing, WeaponData conf, boolean crit, int type) {
		super(sound, entity, type, conf);
		repeat = true;
		this.firing = firing;
		this.crit = crit;
	}

	@Override
	public void update() {
		super.update();
		if (endsnextTick || donePlaying) return;
		ItemStack stack = entity.getHeldItem(EnumHand.MAIN_HAND);
		boolean boost = TF2Util.calculateCritPre(stack, entity) == 2;
		boolean playThis = (boost && crit) || (!boost && !crit);
		if (((ItemUsable) stack.getItem()).canFireInternal(entity.world, entity, stack,
				EnumHand.MAIN_HAND)/*
									 * stack.getTagCompound().getShort("minigunticks")>=17*
									 * TF2Attribute.getModifier("Minigun Spinup", stack, 1,entity)
									 */) {
			int action = entity.getCapability(TF2weapons.WEAPONS_CAP, null).state;
			if (((action & 1) != 0 && firing && playThis) || (!firing && (action & 3) == 2)) {
				// this.volume=1.0f;
			} else setDone();
		} else
			this.setDone();
	}

}
