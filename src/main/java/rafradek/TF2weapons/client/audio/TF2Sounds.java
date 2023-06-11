package rafradek.TF2weapons.client.audio;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import rafradek.TF2weapons.TF2weapons;

import java.util.HashMap;

public class TF2Sounds {

	public static final HashMap<ResourceLocation, SoundEvent> SOUND_EVENTS = new HashMap<>();
	public static final SoundEvent MISC_PAIN = register(new ResourceLocation(TF2weapons.MOD_ID, "misc.pain"));
	public static final SoundEvent MISC_CRIT = register(new ResourceLocation(TF2weapons.MOD_ID, "misc.crit"));
	public static final SoundEvent MISC_MINI_CRIT = register(new ResourceLocation(TF2weapons.MOD_ID, "misc.crit.mini"));
	public static final SoundEvent RAZORBACK_BREAK = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.razorback"));
	public static final SoundEvent MOB_ENGINEER_HURT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.engineer.hurt"));
	public static final SoundEvent MOB_ENGINEER_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.engineer.death"));
	public static final SoundEvent MOB_ENGINEER_SAY = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.engineer.say"));
	public static final SoundEvent MOB_DEMOMAN_HURT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.demoman.hurt"));
	public static final SoundEvent MOB_DEMOMAN_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.demoman.death"));
	public static final SoundEvent MOB_DEMOMAN_SAY = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.demoman.say"));
	public static final SoundEvent MOB_HEAVY_HURT = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.heavy.hurt"));
	public static final SoundEvent MOB_HEAVY_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.heavy.death"));
	public static final SoundEvent MOB_HEAVY_SAY = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.heavy.say"));
	public static final SoundEvent MOB_MEDIC_HURT = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.medic.hurt"));
	public static final SoundEvent MOB_MEDIC_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.medic.death"));
	public static final SoundEvent MOB_MEDIC_SAY = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.medic.say"));
	public static final SoundEvent MOB_PYRO_HURT = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.pyro.hurt"));
	public static final SoundEvent MOB_PYRO_DEATH = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.pyro.death"));
	public static final SoundEvent MOB_PYRO_SAY = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.pyro.say"));
	public static final SoundEvent MOB_SCOUT_HURT = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.scout.hurt"));
	public static final SoundEvent MOB_SCOUT_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.scout.death"));
	public static final SoundEvent MOB_SCOUT_SAY = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.scout.say"));
	public static final SoundEvent MOB_SNIPER_HURT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sniper.hurt"));
	public static final SoundEvent MOB_SNIPER_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sniper.death"));
	public static final SoundEvent MOB_SNIPER_SAY = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.sniper.say"));
	public static final SoundEvent MOB_SOLDIER_HURT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.soldier.hurt"));
	public static final SoundEvent MOB_SOLDIER_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.soldier.death"));
	public static final SoundEvent MOB_SOLDIER_SAY = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.soldier.say"));
	public static final SoundEvent MOB_SPY_HURT = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.spy.hurt"));
	public static final SoundEvent MOB_SPY_DEATH = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.spy.death"));
	public static final SoundEvent MOB_SPY_SAY = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.spy.say"));
	public static final SoundEvent MOB_SAXTON_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.saxton.death"));
	public static final SoundEvent MOB_SAXTON_START = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.saxton.start"));
	public static final SoundEvent MOB_SAXTON_KILL = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.saxton.kill"));
	public static final SoundEvent MOB_SAXTON_DESTROY = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.saxton.destroy"));
	public static final SoundEvent MOB_SAXTON_STAB = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.saxton.stab"));
	public static final SoundEvent MOB_SAXTON_RAGE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.saxton.rage"));
	public static final SoundEvent MOB_SAXTON_JUMP = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.saxton.jump"));
	public static final SoundEvent MOB_SENTRY_HURT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.hurt"));
	public static final SoundEvent MOB_SENTRY_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.death"));
	public static final SoundEvent MOB_SENTRY_SHOOT_1 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.shoot.1"));
	public static final SoundEvent MOB_SENTRY_SHOOT_2 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.shoot.2"));
	public static final SoundEvent MOB_SENTRY_SHOOT_3 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.shoot.3"));
	public static final SoundEvent MOB_SENTRY_SCAN_1 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.scan.1"));
	public static final SoundEvent MOB_SENTRY_SCAN_2 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.scan.2"));
	public static final SoundEvent MOB_SENTRY_SCAN_3 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.scan.3"));
	public static final SoundEvent MOB_SENTRY_ROCKET = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.rocket"));
	public static final SoundEvent MOB_SENTRY_SPOT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.spot"));
	public static final SoundEvent MOB_SENTRY_EMPTY = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sentry.empty"));
	public static final SoundEvent MOB_DISPENSER_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.dispenser.death"));
	public static final SoundEvent MOB_DISPENSER_IDLE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.dispenser.idle"));
	public static final SoundEvent MOB_DISPENSER_HEAL = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.dispenser.heal"));
	public static final SoundEvent MOB_DISPENSER_GENERATE_METAL = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.dispenser.generatemetal"));
	public static final SoundEvent MOB_TELEPORTER_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.teleporter.death"));
	public static final SoundEvent MOB_TELEPORTER_SPIN_1 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.teleporter.spin.1"));
	public static final SoundEvent MOB_TELEPORTER_SPIN_2 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.teleporter.spin.2"));
	public static final SoundEvent MOB_TELEPORTER_SPIN_3 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.teleporter.spin.3"));
	public static final SoundEvent MOB_TELEPORTER_READY = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.teleporter.ready"));
	public static final SoundEvent MOB_TELEPORTER_RECEIVE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.teleporter.receive"));
	public static final SoundEvent MOB_TELEPORTER_SEND = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.teleporter.send"));
	public static final SoundEvent MOB_SAPPER_DEATH = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sapper.death"));
	public static final SoundEvent MOB_SAPPER_IDLE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sapper.idle"));
	public static final SoundEvent MOB_SAPPER_PLANT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.sapper.plant"));
	public static final SoundEvent WEAPON_TO_GOLD = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.turntogold"));
	public static final SoundEvent WEAPON_STUN = register(new ResourceLocation(TF2weapons.MOD_ID, "weapon.stun.ball"));
	public static final SoundEvent WEAPON_STUN_MAX = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.stun.max.ball"));
	public static final SoundEvent WEAPON_MANTREADS = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.mantreads"));
	public static final SoundEvent WEAPON_FLAMETHROWER_HIT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.hit.flamethrower"));
	public static final SoundEvent JAR_EXPLODE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.jar.explode"));
	public static final SoundEvent MOB_HHH_START = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.hhh.start"));
	public static final SoundEvent MOB_MONOCULUS_START = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.monoculus.start"));
	public static final SoundEvent MOB_MERASMUS_START = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.starttalk"));
	public static final SoundEvent MOB_HHH_DEFEAT = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.hhh.defeat"));
	public static final SoundEvent MOB_MONOCULUS_DEFEAT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.monoculus.defeat"));
	public static final SoundEvent MOB_MERASMUS_DEFEAT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.defeat"));
	public static final SoundEvent MOB_BOSS_ESCAPE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.boss.escape"));
	public static final SoundEvent MOB_BOSS_ESCAPE_60 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.boss.escape60"));
	public static final SoundEvent MOB_BOSS_ESCAPE_10 = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.boss.escape10"));
	public static final SoundEvent MOB_MERASMUS_LEAVE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.leave"));
	public static final SoundEvent MOB_MONOCULUS_SHOOT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.monoculus.shoot"));
	public static final SoundEvent MOB_MONOCULUS_SHOOT_MAD = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.monoculus.shoot.mad"));
	public static final SoundEvent MOB_HHH_ALERT = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.hhh.alert"));
	public static final SoundEvent MOB_HHH_HIT = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.hhh.hit"));
	public static final SoundEvent MOB_HHH_MISS = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.hhh.miss"));
	public static final SoundEvent MOB_HHH_ATTACK = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.hhh.attack"));
	public static final SoundEvent MOB_HHH_SAY = register(new ResourceLocation(TF2weapons.MOD_ID, "mob.hhh.say"));
	public static final SoundEvent MOB_MERASMUS_APPEAR = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.appear"));
	public static final SoundEvent MOB_MERASMUS_DISAPPEAR = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.disappear"));
	public static final SoundEvent MOB_MERASMUS_STUN = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.stun"));
	public static final SoundEvent MOB_MERASMUS_BOMBINOMICON = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.bombinomicon"));
	public static final SoundEvent MOB_MERASMUS_HEADBOMB = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.headbomb"));
	public static final SoundEvent MOB_MERASMUS_SPELL = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.spell"));
	public static final SoundEvent MOB_MERASMUS_HIDE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mob.merasmus.hideheal"));
	public static final SoundEvent WEAPON_SHIELD_CHARGE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.shield.charge"));
	public static final SoundEvent WEAPON_SHIELD_HIT = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.shield.hit"));
	public static final SoundEvent WEAPON_MACHINA = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.fire.machina"));
	public static final SoundEvent WEAPON_SHIELD_HIT_RANGE = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.shield.hit.range"));
	public static final SoundEvent DISCIPLINARY_SPEED = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.powerup.disciplinary"));
	public static final SoundEvent DOUBLE_DONK = register(new ResourceLocation(TF2weapons.MOD_ID, "misc.doubledonk"));
	public static final SoundEvent GRENADE_EXPLODESPECIAL = register(
			new ResourceLocation(TF2weapons.MOD_ID, "weapon.explosion.airstrike"));
	public static final SoundEvent MVM_WAVE_START = register(new ResourceLocation(TF2weapons.MOD_ID, "mvm.wave.start"));
	public static final SoundEvent MVM_WAVE_END = register(new ResourceLocation(TF2weapons.MOD_ID, "mvm.wave.end"));
	public static final SoundEvent MVM_SENTRY_BUSTER = register(
			new ResourceLocation(TF2weapons.MOD_ID, "mvm.sentrybuster"));

	public static void registerSounds() {
		/*
		 * IForgeRegistry<SoundEvent>
		 * registry=GameRegistry.findRegistry(SoundEvent.class); GameRegistry.register(,
		 * name) registry.register(MISC_CRIT); registry.register(MISC_PAIN);
		 * registry.register(MOB_ENGINEER_HURT); registry.register(MOB_ENGINEER_DEATH);
		 * registry.register(MOB_ENGINEER_SAY); registry.register(MOB_SNIPER_HURT);
		 * registry.register(MOB_SNIPER_DEATH); registry.register(MOB_SNIPER_SAY);
		 * registry.register(MOB_DEMOMAN_HURT); registry.register(MOB_DEMOMAN_DEATH);
		 * registry.register(MOB_DEMOMAN_SAY); registry.register(MOB_HEAVY_HURT);
		 * registry.register(MOB_HEAVY_DEATH); registry.register(MOB_HEAVY_SAY);
		 * registry.register(MOB_MEDIC_HURT); registry.register(MOB_MEDIC_DEATH);
		 * registry.register(MOB_MEDIC_SAY); registry.register(MOB_PYRO_HURT);
		 * registry.register(MOB_PYRO_DEATH); registry.register(MOB_PYRO_SAY);
		 * registry.register(MOB_SPY_HURT); registry.register(MOB_SPY_DEATH);
		 * registry.register(MOB_SPY_SAY); registry.register(MOB_SOLDIER_HURT);
		 * registry.register(MOB_SOLDIER_DEATH); registry.register(MOB_SOLDIER_SAY);
		 * registry.register(MOB_SCOUT_HURT); registry.register(MOB_SCOUT_DEATH);
		 * registry.register(MOB_SCOUT_SAY); registry.register(MOB_SENTRY_HURT);
		 * registry.register(MOB_SENTRY_DEATH); registry.register(MOB_SENTRY_SHOOT_1);
		 * registry.register(MOB_SENTRY_SHOOT_2); registry.register(MOB_SENTRY_SHOOT_3);
		 * registry.register(MOB_SENTRY_SCAN_1); registry.register(MOB_SENTRY_SCAN_2);
		 * registry.register(MOB_SENTRY_SCAN_3); registry.register(MOB_SENTRY_ROCKET);
		 * registry.register(MOB_SENTRY_SPOT); registry.register(MOB_SENTRY_EMPTY);
		 * registry.register(MOB_DISPENSER_IDLE);
		 * registry.register(MOB_DISPENSER_DEATH);
		 * registry.register(MOB_DISPENSER_HEAL);
		 * registry.register(MOB_DISPENSER_GENERATE_METAL);
		 * registry.register(MOB_TELEPORTER_DEATH);
		 * registry.register(MOB_TELEPORTER_SPIN_1);
		 * registry.register(MOB_TELEPORTER_SPIN_2);
		 * registry.register(MOB_TELEPORTER_SPIN_3);
		 * registry.register(MOB_TELEPORTER_RECEIVE);
		 * registry.register(MOB_TELEPORTER_SEND);
		 * registry.register(MOB_TELEPORTER_READY);
		 */
	}

	public static SoundEvent register(ResourceLocation location) {
		if (SOUND_EVENTS.containsKey(location))
			return SOUND_EVENTS.get(location);
		SoundEvent event = new SoundEvent(location).setRegistryName(location);
		SOUND_EVENTS.put(location, event);
		return event;
	}
}
