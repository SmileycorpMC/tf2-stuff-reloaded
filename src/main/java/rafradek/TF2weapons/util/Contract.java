package rafradek.TF2weapons.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.item.ItemFromData;

import java.util.ArrayList;
import java.util.Random;

public class Contract {

	public String className;
	public int expireDay;
	public int progress;
	public Objective[] objectives;
	public boolean active;
	public int rewards;

	public final static int REWARD_LOW = 40;
	public final static int REWARD_HIGH = 135;

	public Contract(String className, int expireDay, Random rand) {
		this.className = className;
		this.expireDay = expireDay;
		this.objectives = new Objective[3];
		ArrayList<Objective> applicableNormal = new ArrayList<>();
		ArrayList<Objective> applicableAdvanced = new ArrayList<>();
		for (Objective obj : Objective.values()) {
			if (obj.classApplicable != null) {
				for (String str : obj.classApplicable) {
					if (str.equalsIgnoreCase(className)) {
						if (!obj.advanced)
							applicableNormal.add(obj);
						else
							applicableAdvanced.add(obj);
						break;
					}
				}
			} else if (obj != Objective.KILL)
				if (!obj.advanced)
					applicableNormal.add(obj);
				else
					applicableAdvanced.add(obj);
		}
		// for(int i=0;i<2;i++) {
		// int toAdd=rand.nextInt(applicableNormal.size());
		this.objectives[0] = Objective.KILL;
		this.objectives[1] = applicableNormal.remove(rand.nextInt(applicableNormal.size()));
		// }

		this.objectives[2] = applicableAdvanced.remove(rand.nextInt(applicableAdvanced.size()));
	}

	public Contract(String className, int expireDay, Objective[] objectives) {
		this.className = className;
		this.expireDay = expireDay;
		this.objectives = objectives;
	}

	public void completeContract(EntityPlayer player) {
		if ((this.rewards & 1) == 1) {
			ItemStack reward = player.getRNG().nextBoolean() ? new ItemStack(TF2weapons.itemTF2, 1, 2)
					: ItemFromData.getRandomWeapon(player.getRNG(), ItemFromData.VISIBLE_WEAPON);
			if (!player.inventory.addItemStackToInventory(reward))
				player.dropItem(reward, true);
			player.addExperience(120);
			TF2Util.setExperiencePoints(player, TF2Util.getExperiencePoints(player) + 240);
		}
		if ((this.rewards & 2) == 2) {
			ItemStack reward = player.getRNG().nextBoolean() ? new ItemStack(TF2weapons.itemTF2, 4, 2)
					: new ItemStack(TF2weapons.itemTF2, 1, 7);
			if (!player.inventory.addItemStackToInventory(reward))
				player.dropItem(reward, true);
			player.addExperience(600);
			TF2Util.setExperiencePoints(player, TF2Util.getExperiencePoints(player) + 1200);
		}
		this.rewards = 0;
	}

	public enum Objective {
		KILL(false, 1, (String[]) null), HEAL_20(false, 1, "medic"), KILL_W_SENTRY(false, 3, "engineer"),
		BACKSTAB(true, 6, "spy"), KILL_SCOUT(false, 2, "kill", "heavy"), KILL_SOLDIER(false, 2, "kill"),
		KILL_PYRO(false, 2, "kill", "soldier"), KILL_DEMOMAN(false, 2, "kill", "scout"),
		KILL_HEAVY(false, 2, "kill", "heavy", "sniper"), KILL_ENGINEER(false, 2, "kill", "spy"),
		KILL_MEDIC(false, 2, "kill", "scout", "sniper"), KILL_SNIPER(false, 2, "kill", "sniper", "spy"),
		KILL_SPY(false, 2, "kill", "pyro"), HEADSHOT(true, 4, "sniper"), KILL_DOUBLE(true, 3, "scout"),
		KILLS_SENTRY(true, 10, "engineer"), STICKY_KILL(false, 2, "demoman"), KILL_REFLECTED(true, 15, "pyro"),
		KILL_BLAST(true, 8, "soldier"), KILL_MELEE(true, 3, "demoman"), DESTROY_SENTRY_UBER(true, 20, "medic"),
		DESTROY_BUILDING(true, 6, "heavy", "spy"), KILLS(true, 10, "kill");

		public String[] classApplicable;
		public boolean advanced;
		private int points;

		Objective(boolean advanced, int points, String... className) {
			this.points = points;
			this.advanced = advanced;
			this.classApplicable = className;
		}

		public int getPoints() {
			return this.points + (this == KILL ? 1 : 0);
		}
	}
}
