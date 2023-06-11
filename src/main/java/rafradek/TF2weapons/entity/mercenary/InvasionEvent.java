package rafradek.TF2weapons.entity.mercenary;

import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2EventsCommon.TF2WorldStorage;
import rafradek.TF2weapons.TF2PlayerCapability;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.item.ItemFromData;
import rafradek.TF2weapons.util.PropertyType;
import rafradek.TF2weapons.util.TF2Util;

import java.util.*;
import java.util.Map.Entry;

public class InvasionEvent implements INBTSerializable<NBTTagCompound> {

	protected final BossInfoServer bossInfo = (new BossInfoServer(
			new TextComponentTranslation("gui.robotinvasion", 1, 1), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS));

	public static final float[] DIFFICULTY = { 1f, 1.5f, 2f, 2.75f, 4f };

	public static Multimap<Squad.Type, Squad> squads;
	public World world;
	public BlockPos target;
	public long startTime;
	public float difficulty;
	public int diffTour;
	public int wave;
	public int progress;
	public List<UUID> playersTotal = new ArrayList<>();
	public Set<EntityPlayer> playersArea = new HashSet<>();
	public Set<BlockPos> notSpawnArea = new HashSet<>();
	public int robotKilledWave;
	public int robotKilledTotal;
	public int robotsWave;
	public int pauseTicks;
	public long endTime;
	public Map<UUID, Float> sentryDamage = new HashMap<>();
	private Set<ChunkPos> eligibleChunksForSpawning = new HashSet<>();
	private HashMap<EntityTF2Character, Integer> entityList = new HashMap<>();
	public float direction = 0f;
	private int robotKilledEnv;
	private int robotCountWeighted;
	private int lowestInvasionBeaten = 100;
	public static List<SpawnListEntry> spawnList = new ArrayList<>();

	public boolean finished;
	public int waves;

	public InvasionEvent(World world, NBTTagCompound tag) {
		this.world = world;
		this.deserializeNBT(tag);
	}

	public InvasionEvent(World world, BlockPos targetPos, int diff) {
		this.world = world;
		this.startTime = world.getWorldTime();
		this.target = targetPos;
		this.direction = (float) (world.rand.nextFloat() * Math.PI * 2 - Math.PI);
		List<EntityPlayerMP> players = this.world.getPlayers(EntityPlayerMP.class,
				player -> this.isInRange(player.getPosition()));
		for (EntityPlayerMP player : players) {
			float killed = player.getStatFile().readStat(TF2weapons.robotsKilled);
			this.difficulty += 1f + Math.min(0.5f + diff * 0.1, killed / (500f * (diff + 1)));
			this.onPlayerEnter(player);
			this.lowestInvasionBeaten = Math.min(lowestInvasionBeaten,
					TF2PlayerCapability.get(player).maxInvasionBeaten);
			player.sendMessage(new TextComponentTranslation("gui.robotinvasion.message"));
		}
		this.diffTour = diff;
		this.difficulty *= DIFFICULTY[diff] * (3f / (players.size() + 2));
		this.waves = TF2ConfigVars.invasionMaxWaves - TF2ConfigVars.invasionMaxWaves / 3
				+ world.rand.nextInt(TF2ConfigVars.invasionMaxWaves / 3 + 1);
		this.calculateWave();
	}

	public void onPlayerEnter(EntityPlayer player) {
		if (player instanceof EntityPlayerMP)
			this.bossInfo.addPlayer((EntityPlayerMP) player);
		this.playersTotal.add(player.getUniqueID());
		this.playersArea.add(player);

	}

	public void onPlayerLeave(EntityPlayer player) {
		if (player instanceof EntityPlayerMP)
			this.bossInfo.removePlayer((EntityPlayerMP) player);
		TF2PlayerCapability.get(player).setInvasionDir(Float.MIN_VALUE);
		TF2PlayerCapability.get(player).lastDayInvasion = (int) (this.world.getWorldTime() / 24000);
	}

	public void addRobotToList(EntityTF2Character entity) {
		if (this.entityList.put(entity, -1) == null)
			this.robotCountWeighted += entity.isGiant() ? 3 : 1;
	}

	public void removeRobotFromList(EntityTF2Character entity) {
		if (this.entityList.remove(entity) != null)
			this.robotCountWeighted -= entity.isGiant() ? 3 : 1;
	}

	public void removeRobotCount(EntityTF2Character entity) {
		this.robotCountWeighted -= entity.isGiant() ? 3 : 1;
	}

	public float getWaveDifficulty() {
		return this.difficulty * (1f + (this.wave - 1) / 4f);
	}

	public int getMaxActiveRobots() {
		return (int) (this.getWaveDifficulty() * 4.2f);
	}

	@SuppressWarnings("deprecation")
	public void onUpdate() {
		// this.playersArea.removeIf(player -> !isInRange(player.getPosition()));
		Iterator<EntityPlayer> players = this.playersArea.iterator();
		while (players.hasNext()) {
			EntityPlayer player = players.next();
			if (!this.isInRange(player.getPosition())) {
				this.onPlayerLeave(player);
				players.remove();
			}
		}
		for (EntityPlayer player : this.world.getPlayers(EntityPlayer.class,
				player -> !playersArea.contains(player) && isInRange(player.getPosition()))) {
			this.onPlayerEnter(player);
		}
		if ((this.world.getTotalWorldTime() & 111) == 0)
			for (EntityTF2Character ent : this.world.getEntitiesWithinAABB(EntityTF2Character.class,
					new AxisAlignedBB(target).grow(256),
					entity -> entity.isEntityAlive() && entity.getOwnerId() == null && entity.isRobot())) {
				this.addRobotToList(ent);
			}

		boolean giantslook = false;

		boolean reduce = this.robotCountWeighted > this.getMaxActiveRobots();
		Iterator<Entry<EntityTF2Character, Integer>> it = this.entityList.entrySet().iterator();

		while (it.hasNext()) {
			Entry<EntityTF2Character, Integer> entry = it.next();
			EntityTF2Character ent = entry.getKey();
			if (ent.isDead) {
				if (ent.getAttackingEntity() != null
						&& TF2Util.getOwnerIfOwnable(ent.getAttackingEntity()) instanceof EntityPlayer
						&& (ent.getAttackingEntity().getTeam() != null || !TF2ConfigVars.canJoin))
					this.onKill(TF2Util.getOwnerIfOwnable(ent.getAttackingEntity()), ent.getLastDamageSource(), ent);
				it.remove();
				this.removeRobotCount(ent);
				continue;
			}
			if (ent.getAttackTarget() instanceof EntityPlayer && ent.isGiant()) {
				giantslook = true;
			}
			if (ent.ticksExisted == entry.getValue()) {
				it.remove();
				this.removeRobotCount(ent);
				continue;
			}

			if (ent.getIdleTime() > (reduce ? 400 : 750)) {
				it.remove();
				ent.setDead();
				this.removeRobotCount(ent);
				continue;
			}
			entry.setValue(ent.ticksExisted);
		}

		if (this.robotKilledWave >= this.robotsWave && !giantslook) {
			if (this.wave < this.waves)
				this.calculateWave();
			else
				this.finish();
		}

		if (this.pauseTicks <= 0 && this.robotKilledWave < this.robotsWave) {
			this.eligibleChunksForSpawning.clear();
			if (this.robotCountWeighted < this.getMaxActiveRobots()) {
				for (EntityPlayer entityplayer : this.playersArea) {
					if (!entityplayer.isSpectator()) {
						int j = MathHelper.floor(entityplayer.posX / 16.0D);
						int k = MathHelper.floor(entityplayer.posZ / 16.0D);

						for (int i1 = -7; i1 <= 7; ++i1) {
							for (int j1 = -7; j1 <= 7; ++j1) {
								boolean flag = i1 == -7 || i1 == 7 || j1 == -7 || j1 == 7;
								ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);
								float distance = i1 * i1 + j1 * j1;
								float angle = (float) MathHelper.atan2(i1, j1);

								if (!this.eligibleChunksForSpawning.contains(chunkpos)
										&& (Math.PI - Math.abs(Math.abs(angle - direction) - Math.PI)) < Math.PI * 0.4
										&& distance / 60f < world.rand.nextFloat() + 0.1f) {
									if (!flag && world.getWorldBorder().contains(chunkpos)) {
										PlayerChunkMapEntry playerchunkmapentry = ((WorldServer) world)
												.getPlayerChunkMap().getEntry(chunkpos.x, chunkpos.z);

										if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers()) {
											this.eligibleChunksForSpawning.add(chunkpos);
										}
									}
								}
							}
						}
					}
				}

				BlockPos spawnPoint = world.getSpawnPoint();

				java.util.ArrayList<ChunkPos> shuffled = com.google.common.collect.Lists
						.newArrayList(this.eligibleChunksForSpawning);
				java.util.Collections.shuffle(shuffled);
				BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
				label134:

				for (ChunkPos chunkpos1 : shuffled) {
					BlockPos blockpos = getRandomChunkPosition(world, chunkpos1.x, chunkpos1.z);
					int k1 = blockpos.getX();
					int l1 = blockpos.getY();
					int i2 = blockpos.getZ();
					IBlockState iblockstate = world.getBlockState(blockpos);

					if (!iblockstate.isNormalCube()) {

						for (int k2 = 0; k2 < 3; ++k2) {
							int l2 = k1;
							int i3 = l1;
							int j3 = i2;
							SpawnListEntry spawnEntry = WeightedRandom.getRandomItem(world.rand, spawnList);
							TF2CharacterAdditionalData livingdata = new TF2CharacterAdditionalData();
							livingdata.natural = true;
							livingdata.team = 2;

							int l3 = MathHelper.ceil(Math.random() * 4.0D);

							for (int i4 = 0; i4 < l3; ++i4) {
								l2 += world.rand.nextInt(6) - world.rand.nextInt(6);
								i3 += world.rand.nextInt(1) - world.rand.nextInt(1);
								j3 += world.rand.nextInt(6) - world.rand.nextInt(6);
								blockpos$mutableblockpos.setPos(l2, i3, j3);
								float f = l2 + 0.5F;
								float f1 = j3 + 0.5F;

								if (!world.isAnyPlayerWithinRangeAt(f, i3, f1, 24.0D)
										&& this.checkInArea((int) f, i3, (int) f1)
										&& spawnPoint.distanceSq(f, i3, f1) >= 576.0D) {

									if (true && WorldEntitySpawner
											.canCreatureTypeSpawnAtLocation(
													EntitySpawnPlacementRegistry
															.getPlacementForEntity(EntityTF2Character.class),
													world, blockpos$mutableblockpos)) {
										EntityTF2Character entityliving = null;

										try {
											entityliving = (EntityTF2Character) spawnEntry.newInstance(world);
										} catch (Exception exception) {
											exception.printStackTrace();
										}

										entityliving.setLocationAndAngles(f, i3, f1, world.rand.nextFloat() * 360.0F,
												0.0F);
										entityliving.setRobot(1);
										Event.Result canSpawn = net.minecraftforge.event.ForgeEventFactory
												.canEntitySpawn(entityliving, world, f, i3, f1, false);
										if (canSpawn == Event.Result.ALLOW || (canSpawn == Event.Result.DEFAULT
												&& (entityliving.getCanSpawnHere() && entityliving.isNotColliding()))) {
											livingdata.isGiant = entityliving.canBecomeGiant()
													&& world.rand.nextFloat() < 0.025 * this.getWaveDifficulty();
											entityliving.robotStrength = this.difficulty;

											if (!ForgeEventFactory.doSpecialSpawn(entityliving, world, f, i3, f1))
												livingdata = (TF2CharacterAdditionalData) entityliving.onInitialSpawn(
														world.getDifficultyForLocation(new BlockPos(entityliving)),
														livingdata);

											if (entityliving.isNotColliding()) {
												world.spawnEntity(entityliving);
												this.addRobotToList(entityliving);
											} else {
												entityliving.setDead();
											}

											// if (j2 >=
											// net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(entityliving))
											// {
											continue label134;
											// }
										}
									}
								}
							}
						}
					}
				}
			}
		} else {
			--this.pauseTicks;
			if (pauseTicks == 120) {
				for (EntityPlayer player : this.playersArea) {
					TF2Util.playSoundToPlayer(player, TF2Sounds.MVM_WAVE_START, SoundCategory.VOICE, player.posX,
							player.posY, player.posZ, 6f, 1f);
				}
			} else if (pauseTicks == 0) {
				for (EntityPlayer player : this.playersArea) {
					TF2PlayerCapability.get(player).setInvasionDir(Float.MIN_VALUE);
				}
			}
		}

		if (this.world.getWorldTime() >= this.endTime)
			this.finish();
	}

	public void calculateWave() {
		if (wave != 0) {
			giveRobotAwards();
			this.direction = (float) (world.rand.nextFloat() * Math.PI * 2 - Math.PI);
			for (EntityPlayer player : this.playersArea) {
				TF2Util.playSoundToPlayer(player, TF2Sounds.MVM_WAVE_END, SoundCategory.VOICE, player.posX, player.posY,
						player.posZ, 6f, 1f);
			}
		}
		this.wave++;
		for (EntityPlayer player : this.playersArea) {
			TF2PlayerCapability.get(player).setInvasionDir(direction);
		}
		this.pauseTicks = 300;
		this.robotsWave = (int) (11.2f * this.getWaveDifficulty());
		this.robotKilledWave = 0;
		this.endTime = this.world.getWorldTime() + (this.wave + 1) * 12000;
		this.bossInfo.setName(new TextComponentTranslation("gui.robotinvasion", this.wave, this.waves));
		this.bossInfo.setPercent(1f - (float) this.robotKilledWave / this.robotsWave);
		this.entityList.keySet().removeIf(ent -> {
			ent.attackEntityFrom(DamageSource.GENERIC, 99999);
			robotCountWeighted -= ent.isGiant() ? 3 : 1;
			return true;
		});
	}

	public void onKill(Entity player, DamageSource source, EntityTF2Character robot) {
		if (player instanceof EntityPlayer)
			this.playersTotal.add(player.getUniqueID());
		if (robot.damagedByEnv) {
			this.robotKilledEnv += 1;
			this.addNotSpawnArea(robot.spawnPos);
			this.addNotSpawnArea(robot.getPosition());
		}
		int robotSize = robot.getRobotSize();
		this.robotKilledTotal += robotSize * robotSize * robotSize;
		this.robotKilledWave += robotSize * robotSize * robotSize;

		this.bossInfo.setPercent(1f - (float) this.robotKilledWave / this.robotsWave);
		// this.players.put(player,
		// this.players.get(player)+robot.getRobotSize()*robot.getRobotSize());

	}

	public void addNotSpawnArea(BlockPos pos) {
		for (BlockPos pos2 : this.notSpawnArea) {
			if (pos.distanceSq(pos2) < 400)
				return;
		}
		this.notSpawnArea.add(pos);
	}

	public boolean checkInArea(int x, int y, int z) {
		for (BlockPos pos : this.notSpawnArea) {
			if (pos.distanceSq(x, y, z) < 400)
				return false;
		}
		return true;
	}

	public void finish() {
		if (this.finished)
			return;
		this.finished = true;

		giveRobotAwards();
		for (EntityPlayer player : this.playersArea) {
			TF2Util.playSoundToPlayer(player, TF2Sounds.MVM_WAVE_END, SoundCategory.VOICE, player.posX, player.posY,
					player.posZ, 6f, 1f);
			if (player instanceof EntityPlayerMP)
				this.onPlayerLeave(player);
			if (this.wave == this.waves)
				TF2PlayerCapability.get(player).maxInvasionBeaten = Math
						.max(TF2PlayerCapability.get(player).maxInvasionBeaten, this.diffTour + 1);
		}

		for (EntityTF2Character ent : this.entityList.keySet())
			ent.attackEntityFrom(DamageSource.OUT_OF_WORLD, 9999f);
	}

	public void giveRobotAwards() {
		if (this.playersTotal.isEmpty())
			return;
		TF2WorldStorage cap = this.world.getCapability(TF2weapons.WORLD_CAP, null);

		List<ItemStack> items = new ArrayList<>();
		float chance = this.difficulty;
		chance = Math.min(46f, (float) Math.pow(this.robotKilledWave, 0.7))
				* (this.world.rand.nextFloat() * 0.8f + 1.05f);
		if (this.wave == this.waves) {
			if (this.lowestInvasionBeaten <= this.diffTour || this.diffTour == DIFFICULTY.length - 1)
				items.add(new ItemStack(TF2weapons.blockRobotDeploy, this.diffTour - this.lowestInvasionBeaten + 1));
			chance *= 2;
		}

		// for (int i = 0; i < this.playersTotal.size(); i++) {
		// cap.getPlayerStorage(this.playersTotal.get(i)).itemsToGive.add(new
		// ItemStack(TF2weapons.itemMoney,(int)
		// (chance/this.playersTotal.size()*1.75f),1));
		// }
		int itemtype = 2;
		while (chance > 0f) {

			float cost = 0f;
			ItemStack item = ItemStack.EMPTY;
			if (itemtype == 0 && chance >= 4.5f) {
				boolean australium = chance > 18 && world.rand.nextInt(6) == 0;
				float chl = chance;
				item = ItemFromData.getRandomWeapon(world.rand,
						Predicates.and(data -> (chl > (australium ? 2f : 0.5f) * data.getInt(PropertyType.COST)),
								ItemFromData.VISIBLE_WEAPON));
				if (!item.isEmpty()) {
					cost = 0.5f * ItemFromData.getData(item).getInt(PropertyType.COST);
					if (australium) {
						cost *= 4f;
						item.getTagCompound().setBoolean("Australium", true);
						item.getTagCompound().setBoolean("Strange", true);
					}
					float upgradecost = (chance - cost) * world.rand.nextFloat() * 0.5f;
					TF2Attribute.upgradeItemStack(item, (int) upgradecost * 20, world.rand);
					cost += upgradecost;
				}
			}
			if (cost == 0 && itemtype == 1 && chance >= 3) {
				ArrayList<TF2Attribute> list = new ArrayList<>(Arrays.asList(TF2Attribute.attributes));
				list.removeIf(attr -> attr == null || attr.perKill == 0);
				int level = 0;
				cost = 3f;
				float rand = world.rand.nextFloat();
				if (rand < 0.2f && chance >= 27f) {
					level = 2;
					cost = 27f;
				} else if (rand < 0.4f && chance >= 9f) {
					level = 1;
					cost = 9f;
				}
				item = new ItemStack(TF2weapons.itemKillstreakFabricator, 1,
						list.get(world.rand.nextInt(list.size())).id + (level << 9));
			}
			if (itemtype == 2) {
				cost = Math.min(chance, 2 + world.rand.nextFloat() * 2);
				item = new ItemStack(TF2weapons.itemMoney, Math.round(cost * 3.2f), 1);
			}
			if (!item.isEmpty()) {
				chance -= cost;
				items.add(item);
			} else
				break;
			itemtype = world.rand.nextInt(3);
		}

		for (int i = 0; i < items.size(); i++) {
			cap.getPlayerStorage(this.playersTotal.get(i % this.playersTotal.size())).itemsToGive.add(items.get(i));
		}
		// ((EntityPlayerMP)this.owner).sendMessage(new TextComponentString("You were
		// awarded"));
	}

	public boolean isInRange(BlockPos pos) {
		return pos.distanceSq(target) <= 65536D;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("start", this.startTime);
		tag.setLong("end", this.endTime);
		tag.setFloat("diff", this.difficulty);
		tag.setIntArray("pos", new int[] { target.getX(), target.getZ() });
		tag.setByte("wave", (byte) this.wave);
		tag.setShort("rkwave", (short) this.robotKilledWave);
		tag.setShort("rktotal", (short) this.robotKilledTotal);
		tag.setShort("rwave", (short) this.robotsWave);
		tag.setShort("rkenv", (short) this.robotKilledEnv);
		NBTTagList list = new NBTTagList();
		for (UUID uuid : this.playersTotal) {
			list.appendTag(NBTUtil.createUUIDTag(uuid));
		}
		tag.setByte("difftour", (byte) this.diffTour);
		tag.setTag("players", list);
		tag.setByte("waves", (byte) this.waves);
		tag.setFloat("direction", this.direction);
		tag.setByte("lowinvasion", (byte) this.lowestInvasionBeaten);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.startTime = nbt.getLong("start");
		this.difficulty = nbt.getFloat("diff");
		int[] coord = nbt.getIntArray("pos");
		this.target = new BlockPos(coord[0], 0, coord[1]);
		this.wave = nbt.getByte("wave");
		this.waves = nbt.getByte("waves");
		this.endTime = nbt.getLong("end");
		this.bossInfo.setName(new TextComponentTranslation("gui.robotinvasion", this.wave, this.waves));
		this.robotKilledTotal = nbt.getShort("rkwave");
		this.robotKilledWave = nbt.getShort("rktotal");
		this.robotsWave = nbt.getShort("rwave");
		this.robotKilledEnv = nbt.getShort("rkenv");
		this.diffTour = nbt.getByte("difftour");
		this.bossInfo.setPercent(1f - (float) this.robotKilledWave / this.robotsWave);
		this.direction = nbt.getFloat("direction");
		NBTTagList list = nbt.getTagList("players", 11);
		for (int i = 0; i < list.tagCount(); i++) {
			this.playersTotal.add(NBTUtil.getUUIDFromTag(list.getCompoundTagAt(i)));
		}
		this.lowestInvasionBeaten = nbt.getByte("lowinvasion");
	}

	private static BlockPos getRandomChunkPosition(World worldIn, int x, int z) {
		Chunk chunk = worldIn.getChunkFromChunkCoords(x, z);
		int i = x * 16 + worldIn.rand.nextInt(16);
		int j = z * 16 + worldIn.rand.nextInt(16);
		int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
		int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
		return new BlockPos(i, l, j);
	}

	static {
		spawnList.add(new SpawnListEntry(EntitySoldier.class, 4, 2, 4));
		spawnList.add(new SpawnListEntry(EntityScout.class, 3, 2, 4));
		spawnList.add(new SpawnListEntry(EntityPyro.class, 3, 2, 4));
		spawnList.add(new SpawnListEntry(EntityDemoman.class, 3, 2, 4));
		spawnList.add(new SpawnListEntry(EntityHeavy.class, 3, 2, 4));
		spawnList.add(new SpawnListEntry(EntityEngineer.class, 1, 1, 1));
		spawnList.add(new SpawnListEntry(EntitySniper.class, 1, 1, 1));
		spawnList.add(new SpawnListEntry(EntitySpy.class, 1, 1, 1));
	}

	public void onDamageSentry(EntityTF2Character entity, EntitySentry sentry, DamageSource source, float amount) {
		this.sentryDamage.compute(sentry.getUniqueID(), (uuid, dmg) -> dmg == null ? 0 : dmg + amount);
	}

	public void onDamageEnv(EntityTF2Character entity, DamageSource source, float amount) {

	}
}
