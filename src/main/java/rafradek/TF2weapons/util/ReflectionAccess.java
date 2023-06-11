package rafradek.TF2weapons.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.reflect.Field;

public class ReflectionAccess {

	public static Field entityFire;
	public static Field entityRecentlyHit;
	public static Field entityAttackingPlayer;
	public static Field entityLookHelper;
	public static Field entityHandInv;
	public static Field entityArmorInv;
	public static Field playerAdvancements;

	static {
		for (Field field : EntityLivingBase.class.getDeclaredFields()) {
			if (field.getName().equals("recentlyHit") || field.getName().equals("field_70718_bc")) {
				field.setAccessible(true);
				entityRecentlyHit = field;
			} else if ((field.getName().equals("attackingPlayer") || field.getName().equals("field_70717_bb"))) {
				field.setAccessible(true);
				entityAttackingPlayer = field;
			}
		}

		for (Field field : Entity.class.getDeclaredFields()) {
			if (field.getName().equals("fire") || field.getName().equals("field_190534_ay")) {
				field.setAccessible(true);
				entityFire = field;
			}
		}

		for (Field field : EntityLiving.class.getDeclaredFields()) {
			if (field.getName().equals("lookHelper") || field.getName().equals("field_70749_g")) {
				field.setAccessible(true);
				entityLookHelper = field;
			}
		}

		for (Field field : EntityLivingBase.class.getDeclaredFields()) {
			if (field.getName().equals("handInventory") || field.getName().equals("field_184630_bs")) {
				field.setAccessible(true);
				entityHandInv = field;
			}
		}

		for (Field field : EntityLivingBase.class.getDeclaredFields()) {
			if (field.getName().equals("armorArray") || field.getName().equals("field_184631_bt")) {
				field.setAccessible(true);
				entityArmorInv = field;
			}
		}

		for (Field field : EntityPlayerMP.class.getDeclaredFields()) {
			if (field.getName().equals("advancements") || field.getName().equals("field_18463f1_bt")) {
				field.setAccessible(true);
				playerAdvancements = field;
			}
		}
	}
}
