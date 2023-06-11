package rafradek.TF2weapons.arena;

import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import rafradek.TF2weapons.NBTLiterals;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.item.ItemToken;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.tileentity.TileEntityGameConfigure;
import rafradek.TF2weapons.util.TF2Util;

import java.util.*;
import java.util.Map.Entry;

public class GameArena implements INBTSerializable<NBTTagCompound> {

	private static int ID_INCREMENT;
	private boolean active;
	private AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	private World world;
	private String name;
	private BlockPos confPos;
	private boolean first = true;
	public boolean markDelete;
	public Map<UUID, EntityPlayer> playerUUID = new HashMap<>();
	public Map<UUID, EntityDataManager> playerInfoUUID = new HashMap<>();
	private int defaultClass = -1;
	private Team defaultTeam = null;
	private boolean showClassSelection = false;
	private boolean showTeamSelection = false;
	private boolean disableHunger = true;
	private boolean forceClassTexture = false;
	public boolean clearInventory = false;
	private List<Team> allowedTeams;
	private int unbalanceLimit = 0;
	private int classLimit = 0;

	public static final DataParameter<String> PLAYER_NAME = new DataParameter<>(0, DataSerializers.STRING);
	public static final DataParameter<Optional<UUID>> PLAYER_UUID = new DataParameter<>(1,
			DataSerializers.OPTIONAL_UNIQUE_ID);
	public static final DataParameter<Integer> TEAM = new DataParameter<>(2, DataSerializers.VARINT);
	public static final DataParameter<Integer> POINTS = new DataParameter<>(3, DataSerializers.VARINT);

	private NBTTagCompound joinTeamDataCache;
	private NBTTagCompound joinClassDataCache;
	public int networkId;

	public GameArena(World world, String name, BlockPos confPos) {
		this.world = world;
		this.name = name;
		this.confPos = confPos;
	}

	public GameArena(World world, NBTTagCompound tag) {
		this.world = world;
		this.deserializeNBT(tag);

	}

	public GameArena(World world, int id) {
		this.world = world;
		this.networkId = id;
	}

	public void addToWorld() {
		this.networkId = ID_INCREMENT++;
	}

	public void tick() {
		if (first) {
			this.allowedTeams = new ArrayList<>();
			this.allowedTeams.add(this.world.getScoreboard().getTeam("RED"));
			this.allowedTeams.add(this.world.getScoreboard().getTeam("BLU"));
			this.first = false;
			if (!(confPos != null && this.world.getTileEntity(confPos) instanceof TileEntityGameConfigure
					&& ((TileEntityGameConfigure) this.world.getTileEntity(confPos)).getName().equals(this.name))) {
				this.markDelete = true;
			}
		}
		if (active) {
			this.joinTeamDataCache = null;
			boolean checkAll = this.bounds == null || (this.bounds.maxX == this.bounds.minX
					&& this.bounds.maxY == this.bounds.minY && this.bounds.maxZ == this.bounds.minZ);
			List<EntityPlayer> players = checkAll ? world.playerEntities
					: world.getEntitiesWithinAABB(EntityPlayer.class, bounds);
			for (EntityPlayer player : players) {
				if (!playerUUID.containsKey(player.getUniqueID())
						&& (TF2PlayerCapability.get(player).getGameArena() == null
								|| TF2PlayerCapability.get(player).getGameArena() == this)) {
					playerUUID.put(player.getUniqueID(), player);
					this.onPlayerJoinMatch(player);
				}
			}

			boolean naturalRegen = this.world.getGameRules().getBoolean("naturalRegeneration");

			Iterator<Entry<UUID, EntityPlayer>> itePly = playerUUID.entrySet().iterator();
			while (itePly.hasNext()) {
				Entry<UUID, EntityPlayer> entry = itePly.next();
				EntityPlayer player = entry.getValue();

				if (this.world.getPlayerEntityByUUID(entry.getKey()) != player
						&& this.world.getPlayerEntityByUUID(entry.getKey()) != null) {
					entry.setValue(player = this.world.getPlayerEntityByUUID(entry.getKey()));

				}
				TF2PlayerCapability playercap = TF2PlayerCapability.get(player);
				WeaponsCapability cap = WeaponsCapability.get(player);
				if (this.world.getPlayerEntityByUUID(entry.getKey()) == null
						|| (!checkAll && !player.getEntityBoundingBox().intersects(this.bounds))) {
					itePly.remove();
					removePlayer(entry.getKey(), player, true);
					continue;
				}
				if (player.getTeam() == null && this.showTeamSelection && !player.isSpectator()) {
					player.setGameType(GameType.SPECTATOR);

					this.showJoinTeamDialog(player);
				}
				if (!player.isSpectator() && this.defaultTeam != null && player.getTeam() == null) {
					world.getScoreboard().addPlayerToTeam(player.getName(), this.defaultTeam.getName());
				}

				if (playercap.getRespawnTime() > 0 && this.world.getTotalWorldTime() % 20 == 0) {
					playercap.setRespawnTime(playercap.getRespawnTime() - 1);
				}

				if (player.isSpectator() && player.getTeam() != null && playercap.getRespawnTime() <= 0
						&& (cap.getUsedToken() != -1 || (this.defaultClass == -1 && !this.showClassSelection))) {
					player.setGameType(GameType.ADVENTURE);
					this.respawnPlayer(player);
				}
				/*
				 * if (this.allowedTeams.contains(player.getTeam())) {
				 * this.playerTeam.put(player, player.getTeam()); } else {
				 * this.playerTeam.remove(player); }
				 */

				if (this.defaultClass != -1 && !cap.forcedClass) {
					((ItemToken) TF2weapons.itemToken)
							.updateAttributes(new ItemStack(TF2weapons.itemToken, 1, this.defaultClass), player);
					WeaponsCapability.get(player).forcedClass = true;
				}

				if (this.disableHunger) {
					player.getFoodStats().setFoodLevel(naturalRegen ? 17 : 20);
				}
			}
		}
	}

	public void placePlayerAtSpawn(EntityPlayer player) {
		BlockPos blockpos = player.getBedLocation(player.dimension);
		if (blockpos != null) {
			BlockPos blockpos1 = EntityPlayer.getBedSpawnLocation(this.world, blockpos, true);

			if (blockpos1 != null) {
				player.setLocationAndAngles(blockpos1.getX() + 0.5F, blockpos1.getY() + 0.1F, blockpos1.getZ() + 0.5F,
						0.0F, 0.0F);
			}
		}

		this.world.getChunkProvider().provideChunk((int) player.posX >> 4, (int) player.posZ >> 4);

		while (!this.world.getCollisionBoxes(player, player.getEntityBoundingBox()).isEmpty() && player.posY < 256.0D) {
			player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
		}
	}

	public void respawnPlayer(EntityPlayer player) {
		// player.setDead();
		// EntityPlayer newplayer =
		// this.world.getMinecraftServer().getPlayerList().recreatePlayerEntity((EntityPlayerMP)player,
		// player.dimension, false);
		this.placePlayerAtSpawn(player);
		this.resupplyPlayer(player);
	}

	public void resupplyPlayer(EntityPlayer player) {
		if (this.clearInventory) {
			player.inventory.clear();
			player.getCapability(TF2weapons.INVENTORY_CAP, null).clear();
		} else {
			TF2Util.removeItemsMatching(new InvWrapper(player.getCapability(TF2weapons.INVENTORY_CAP, null)), 64,
					stackl -> stackl.hasTagCompound()
							&& stackl.getTagCompound().getBoolean(NBTLiterals.STACK_ARENA_ASSIGNED));
			TF2Util.removeItemsMatching(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), 64,
					stackl -> stackl.hasTagCompound()
							&& stackl.getTagCompound().getBoolean(NBTLiterals.STACK_ARENA_ASSIGNED));
		}
		if (WeaponsCapability.get(player).getUsedToken() != -1) {
			for (int i = 0; i < 5; i++) {
				ItemStack weapon = ItemFromData.getRandomWeaponOfSlotMob(
						ItemToken.CLASS_NAMES[WeaponsCapability.get(player).getUsedToken()], i, player.getRNG(), false,
						0xFFFFFFFF, false);
				if (!weapon.isEmpty()) {
					weapon.getTagCompound().setBoolean(NBTLiterals.STACK_ARENA_ASSIGNED, true);
					ItemHandlerHelper.giveItemToPlayer(player, weapon, i);
				}
			}
		}
		TF2Util.restoreAmmoToWeapons(player, 1f);
	}

	public void onPlayerJoinMatch(EntityPlayer player) {
		TF2PlayerCapability.get(player).setGameArena(this);
		TF2PlayerCapability.get(player).setForceClassTexture(this.forceClassTexture);
		this.world.getScoreboard().removePlayerFromTeams(player.getName());
		player.setGameType(GameType.SPECTATOR);
		if (this.defaultTeam != null) {
			world.getScoreboard().addPlayerToTeam(player.getName(), this.defaultTeam.getName());
		}
		if (this.showTeamSelection) {
			this.showJoinTeamDialog(player);
		}
	}

	public void tryPlayerJoinTeam(EntityPlayer player, int number) {
		Team team = this.allowedTeams.get(number % this.allowedTeams.size());
		this.world.getScoreboard().addPlayerToTeam(player.getName(), team.getName());
	}

	public void showJoinTeamDialog(EntityPlayer player) {
		NBTTagCompound data = this.joinTeamDataCache;
		if (data == null) {
			data = new NBTTagCompound();
			NBTTagList listteamnbt = new NBTTagList();
			int[] count = new int[allowedTeams.size()];
			byte[] allowed = new byte[allowedTeams.size()];
			int lowestPlayerCount = Integer.MAX_VALUE;

			for (int i = 0; i < this.allowedTeams.size(); i++) {
				Team team = this.allowedTeams.get(i);
				listteamnbt.appendTag(new NBTTagString(team.getName()));
				for (EntityPlayer playerl : this.playerUUID.values()) {
					if (playerl.getTeam() == team) {
						count[i] += 1;
					}
				}
				if (count[i] <= lowestPlayerCount) {
					lowestPlayerCount = count[i];
				}
			}

			for (int i = 0; i < this.allowedTeams.size(); i++) {
				if (count[i] <= lowestPlayerCount + this.unbalanceLimit)
					allowed[i] = 1;
			}
			data.setTag("Teams", listteamnbt);
			data.setByteArray("Allowed", allowed);
			data.setIntArray("Count", count);
			this.joinTeamDataCache = data;
		}
		TF2weapons.network.sendTo(new TF2Message.ShowGuiMessage(100, data), (EntityPlayerMP) player);
	}

	public void showJoinClassDialog(EntityPlayer player) {
		NBTTagCompound data = this.joinClassDataCache;
		if (data == null) {
			data = new NBTTagCompound();
			NBTTagList listteamnbt = new NBTTagList();
			int[] count = new int[allowedTeams.size()];
			byte[] allowed = new byte[allowedTeams.size()];
			for (int i = 0; i < this.allowedTeams.size(); i++) {
				Team team = this.allowedTeams.get(i);
				listteamnbt.appendTag(new NBTTagString(team.getName()));
				for (EntityPlayer playerl : this.playerUUID.values()) {
					if (playerl.getTeam() == team) {
						count[i] += 1;
					}
				}
				allowed[i] = 1;
			}
			data.setTag("Teams", listteamnbt);
			data.setByteArray("Allowed", allowed);
			data.setIntArray("Count", count);
			this.joinClassDataCache = data;
		}
		TF2weapons.network.sendTo(new TF2Message.ShowGuiMessage(100, data), (EntityPlayerMP) player);
	}

	public void removePlayer(UUID uuid, EntityPlayer player, boolean alreadyRemoved) {
		if (!alreadyRemoved) {
			player = playerUUID.remove(uuid);
		}
		playerInfoUUID.remove(uuid);
		if (player != null) {
			this.world.getScoreboard().removePlayerFromTeams(player.getName());
			// playerTeam.remove(player);
			player.setGameType(this.world.getWorldInfo().getGameType());
			TF2PlayerCapability.get(player).setGameArena(null);
			TF2PlayerCapability.get(player).setForceClassTexture(false);
			WeaponsCapability cap = WeaponsCapability.get(player);
			if (cap.forcedClass) {
				((ItemToken) TF2weapons.itemToken).updateAttributes(ItemStack.EMPTY, player);
				WeaponsCapability.get(player).forcedClass = false;
			}
		}
	}

	public void setActive(boolean active) {
		this.active = active;
		if (!active) {
			this.playerUUID.entrySet().removeIf((entry) -> {
				removePlayer(entry.getKey(), entry.getValue(), true);
				return true;
			});
		}
	}

	public void onPlayerKill(EntityPlayer player) {
		player.setGameType(GameType.SPECTATOR);
		TF2PlayerCapability.get(player).setRespawnTime(10);
	}

	public void changePlayerTeam(EntityPlayer player, Team team) {
		if (this.playerUUID.containsKey(player.getUniqueID()) && this.allowedTeams.contains(team)
				&& player.getTeam() != team) {

		}
	}

	public void readConfig(NBTTagCompound tag) {
		if (tag.hasKey("Min Bounds")) {
			int[] boundsMin = tag.getIntArray("Min Bounds");
			int[] boundsMax = tag.getIntArray("Max Bounds");
			this.bounds = new AxisAlignedBB(boundsMin[0], boundsMin[1], boundsMin[2], boundsMax[0], boundsMax[1],
					boundsMax[2]);
		}
		this.defaultClass = ItemToken.getClassID(tag.getString("C:Default Class"));
		this.showClassSelection = tag.getBoolean("Show Class Selection");
		this.showTeamSelection = tag.getBoolean("Show Team Selection");
		if (tag.hasKey("T:Default Team"))
			this.defaultTeam = this.world.getScoreboard().getTeam(tag.getString("T:Default Team"));

		this.disableHunger = tag.getBoolean("Disable Hunger");
		this.forceClassTexture = tag.getBoolean("Force Class Texture");
	}

	public NBTTagCompound writeConfig(NBTTagCompound tag) {
		if (bounds != null) {
			tag.setIntArray("Min Bounds", new int[] { (int) bounds.minX, (int) bounds.minY, (int) bounds.minZ });
			tag.setIntArray("Max Bounds", new int[] { (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ });
		}
		tag.setString("C:Default Class", this.defaultClass == -1 ? "none" : ItemToken.CLASS_NAMES[this.defaultClass]);
		tag.setBoolean("Show Class Selection", this.showClassSelection);
		if (this.defaultTeam != null)
			tag.setString("T:Default Team", this.defaultTeam.getName());
		else
			tag.setString("T:Default Team", "");
		tag.setBoolean("Show Team Selection", this.showTeamSelection);
		tag.setBoolean("Disable Hunger", this.disableHunger);
		tag.setBoolean("Force Class Texture", this.forceClassTexture);
		return tag;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Name", this.name);
		tag.setLong("ConfPos", this.confPos.toLong());
		tag.setBoolean("Active", this.active);
		this.writeConfig(tag);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.name = nbt.getString("Name");
		this.active = nbt.getBoolean("Active");
		this.readConfig(nbt);
		this.confPos = BlockPos.fromLong(nbt.getLong("ConfPos"));
	}

	public String getName() {
		return name;
	}

	public static class PlayerMatchInfo {

	}
}
