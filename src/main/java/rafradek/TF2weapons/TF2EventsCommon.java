package rafradek.TF2weapons;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules.ValueType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.TF2weapons.arena.GameArena;
import rafradek.TF2weapons.client.ClientProxy;
import rafradek.TF2weapons.client.audio.TF2Sounds;
import rafradek.TF2weapons.common.MapList;
import rafradek.TF2weapons.common.TF2Attribute;
import rafradek.TF2weapons.common.WeaponsCapability;
import rafradek.TF2weapons.common.WeaponsCapability.RageType;
import rafradek.TF2weapons.entity.EntityDummy;
import rafradek.TF2weapons.entity.EntityStatue;
import rafradek.TF2weapons.entity.IEntityTF2;
import rafradek.TF2weapons.entity.boss.EntityHHH;
import rafradek.TF2weapons.entity.boss.EntityMerasmus;
import rafradek.TF2weapons.entity.boss.EntityMonoculus;
import rafradek.TF2weapons.entity.boss.EntityTF2Boss;
import rafradek.TF2weapons.entity.building.EntityBuilding;
import rafradek.TF2weapons.entity.building.EntityDispenser;
import rafradek.TF2weapons.entity.building.EntitySentry;
import rafradek.TF2weapons.entity.building.EntityTeleporter;
import rafradek.TF2weapons.entity.mercenary.*;
import rafradek.TF2weapons.entity.projectile.EntityProjectileBase;
import rafradek.TF2weapons.inventory.ContainerMercenary;
import rafradek.TF2weapons.inventory.InventoryWearables;
import rafradek.TF2weapons.item.*;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.udp.TF2UdpClient;
import rafradek.TF2weapons.message.udp.TF2UdpServer;
import rafradek.TF2weapons.util.*;
import rafradek.TF2weapons.util.Contract.Objective;
import rafradek.TF2weapons.world.gen.structure.MannCoBuilding;
import rafradek.TF2weapons.world.gen.structure.ScatteredFeatureTF2Base;
import rafradek.TF2weapons.world.gen.structure.ScatteredFeatureTF2Base.MapGen;

import java.util.*;
import java.util.Map.Entry;

public class TF2EventsCommon {
	public int tickleft;

	public static EntityBuilding teleporterBView;

	public static EntityBuilding teleporterAView;

	public static EntityBuilding dispenserView;

	public static EntityBuilding sentryView;

	// public ModelSkeleton skeletonModel=new ModelSkeleton();
	// private HashMap eligibleChunksForSpawning = new HashMap();
	public static final String[] STRANGE_TITLES = new String[] { "Strange", "Unremarkable", "Scarely lethal",
			"Mildly Menacing", "Somewhat threatening", "Uncharitable", "Notably dangerous", "Sufficiently lethal",
			"Truly feared", "Spectacularly lethal", "Gore-spatterer", "Wicked nasty", "Positively inhumane",
			"Totally ordinary", "Face-melting", "Rage-inducing", "Server-clearing", "Epic", "Legendary", "Australian",
			"Hale's own" };
	public static final int[] STRANGE_KILLS = new int[] { 0, 10, 25, 45, 70, 100, 135, 175, 225, 275, 350, 500, 750,
			999, 1000, 1500, 2500, 5000, 7500, 7616, 8500 };
	public static HashMap<EntityLivingBase, EntityLivingBase> fakeEntities = new HashMap<>();
	public static ArrayList<EntityLivingBase> pathsToDefine = new ArrayList<>();

	// public static final DataParameter<Boolean> ENTITY_UBER = new
	// DataParameter<Boolean>(169, DataSerializers.BOOLEAN);
	public static final DataParameter<Float> ENTITY_OVERHEAL = new DataParameter<>(170, DataSerializers.FLOAT);

	public static final UUID REMOVE_ARMOR = UUID.fromString("5a0959c5-90e8-486b-ae51-26f69f19a248");

	// public static AttributeModifier NO_KNOCKBACK = new
	// AttributeModifier(UUID.fromString("c174c6c6-57cd-43b1-bada-43da0e3a88e1"),
	// "No knockba", tickleft, tickleft);
	public static long[] tickTimeLiving = new long[20];
	public static long[] tickTimeMercUpdate = new long[20];
	public static long[] tickTimeOther = new long[20];

	/*
	 * @SubscribeEvent public void spawn(WorldEvent.PotentialSpawns event){ int
	 * time=(int) (event.getWorld().getWorldInfo().getWorldTotalTime()/24000);
	 * if(MapList.scoutSpawn.containsKey(event.list)){
	 * MapList.scoutSpawn.get(event.list).itemWeight=time; } else{
	 * System.out.println("add"); SpawnListEntry entry=new
	 * SpawnListEntry(EntityScout.class, time, 1, 3); event.list.add(entry);
	 * MapList.scoutSpawn.put(event.list,entry); } }
	 */

	@SubscribeEvent
	public void untargetable(LivingSetAttackTargetEvent event) {
		boolean notarget = event.getTarget() == null;
		if (!(event.getEntityLiving() instanceof EntityLiving))
			return;
		if (notarget && event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
				.getModifier(TF2Util.FOLLOW_MODIFIER) != null) {
			event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
					.removeModifier(TF2Util.FOLLOW_MODIFIER);
			event.getEntityLiving().getEntityData().removeTag("TF2AM");
		}
		if (!notarget && ((event.getTarget().hasCapability(TF2weapons.WEAPONS_CAP, null)
				&& event.getTarget().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks >= 45))) {
			// event.getEntityLiving().setRevengeTarget(null);
			((EntityLiving) event.getEntity()).setAttackTarget(null);
		}
		if (event.getTarget() instanceof EntityTF2Character && event.getEntity() instanceof EntityGolem
				&& ((IEntityOwnable) event.getTarget()).getOwner() != null) {
			// event.getEntityLiving().setRevengeTarget(null);
			((EntityLiving) event.getEntity()).setAttackTarget(null);
		}
		if (!notarget && event.getEntityLiving().isNonBoss()
				&& (event.getTarget().hasCapability(TF2weapons.WEAPONS_CAP, null)
						&& ItemDisguiseKit.isDisguised(event.getTarget(), event.getEntityLiving())
						&& event.getEntityLiving().getAttackingEntity() != event.getTarget()))
			if (event.getEntityLiving() instanceof EntityLiving) {
				((EntityLiving) event.getEntity()).setAttackTarget(null);
			}
	}

	@SubscribeEvent
	public void serverTickEnd(TickEvent.ServerTickEvent event) {

	}

	public static double avg(long[] values) {
		long totalticktime = 0L;
		for (long val : values) {
			totalticktime += val;
		}
		totalticktime /= values.length;
		return totalticktime * 1.0E-6D;
	}

	@SubscribeEvent
	public void worldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.side == Side.SERVER) {
			/*
			 * long biggest = 0; int biggesttick = 0; if
			 * (event.world.getMinecraftServer().getTickCounter()%20 == 0) { for (int i = 0;
			 * i < 100; i++) { if (event.world.getMinecraftServer().tickTimeArray[i] >
			 * biggest) { biggest = event.world.getMinecraftServer().tickTimeArray[i]; } if
			 * (event.world.getMinecraftServer().tickTimeArray[i] > 50000000) biggesttick
			 * +=1; } System.out.println(biggest/1000000+ " "+biggesttick); }
			 */

			/*
			 * if(TF2weapons.server.getTickCounter()%20 == 0) {
			 * System.out.println("TickTimeLiving: "+avg(tickTimeLiving));
			 * System.out.println("TickTimeTF2Mob: "+avg(tickTimeMercUpdate));
			 * System.out.println("TickTimeOther: "+avg(tickTimeOther));
			 * System.out.println("TickTimeTotal: "+avg(TF2weapons.server.tickTimeArray)); }
			 */
			long worldTime = event.world.getWorldTime();

			TF2WorldStorage events = event.world.getCapability(TF2weapons.WORLD_CAP, null);

			Iterator<Entry<Entity, InboundDamage>> it = events.damage.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Entity, InboundDamage> entry = it.next();
				if (entry.getKey().isDead) {
					it.remove();
					continue;
				}

				if (entry.getKey().hurtResistantTime <= (entry.getKey() instanceof EntityLivingBase
						? ((EntityLivingBase) entry.getKey()).maxHurtResistantTime / 2f
						: 10f)) {
					TF2Util.dealDamageActual(entry.getKey(), event.world, entry.getValue().living,
							entry.getValue().stack, entry.getValue().critical, entry.getValue().damage,
							entry.getValue().source);
					it.remove();
				}
			}

			if (worldTime % 4 == 0) {
				for (int i = events.destroyProgress.size() - 1; i >= 0; i--) {
					DestroyBlockEntry entry = events.destroyProgress.get(i);
					int count = 0;
					if (entry != null && entry.world == event.world) {
						count++;
						entry.curDamage -= 0.05f;
						if (entry.curDamage <= 0 || entry.world.isAirBlock(entry.pos) || count >= 100) {
							events.destroyProgress.set(i, null);
							event.world.sendBlockBreakProgress(Math.min(Integer.MAX_VALUE, 0xFFFF + i), entry.pos, -1);
							continue;
						} else if (worldTime % 20 == 0) {
							int val = (int) ((entry.curDamage
									/ TF2Util.getHardness(entry.world.getBlockState(entry.pos), entry.world, entry.pos))
									* 10);
							event.world.sendBlockBreakProgress(Math.min(Integer.MAX_VALUE, 0xFFFF + i), entry.pos, val);
						}
					}
				}
			}
			Iterator<Entry<UUID, InvasionEvent>> ite = events.invasions.entrySet().iterator();
			while (ite.hasNext()) {
				Entry<UUID, InvasionEvent> entry = ite.next();
				entry.getValue().onUpdate();
				if (entry.getValue().finished)
					ite.remove();
			}
			Iterator<Entry<String, GameArena>> iteGame = events.gameArenas.entrySet().iterator();
			while (iteGame.hasNext()) {
				Entry<String, GameArena> entry = iteGame.next();
				entry.getValue().tick();
				if (entry.getValue().markDelete)
					iteGame.remove();
			}
			if (event.world.provider.getDimensionType() == DimensionType.OVERWORLD) {
				int dayTime = (int) (worldTime % 24000);
				int dayNum = (int) (worldTime / 24000);
				if (dayTime == 1) {
					if (events != null
							&& new Random(event.world.getSeed() + worldTime * worldTime * 4987142 + worldTime * 5947611)
									.nextFloat() <= TF2ConfigVars.invasionChance) {
						for (EntityPlayer player : event.world.playerEntities) {
							if (player.getTeam() != null && player.getRNG().nextFloat() < (dayNum
									- TF2PlayerCapability.get(player).lastDayInvasion - TF2ConfigVars.invasionMinDay)
									* 0.055f)
								events.startInvasion(player, Math.min(TF2PlayerCapability.get(player).maxInvasionBeaten,
										MathHelper.clamp(MathHelper.ceil(worldTime / 960000f), 0, 2)), false);
						}
					}
				}
				if (!TF2ConfigVars.disableBossSpawn && dayTime >= 13000 && dayTime <= 20000 && dayTime % 1000 == 0
						&& event.world.getCurrentMoonPhaseFactor() == 1 && worldTime > 24000) {
					for (EntityPlayer player : event.world.playerEntities)
						if (player.getCapability(TF2weapons.PLAYER_CAP, null).nextBossTicks <= worldTime
								&& event.world.getEntitiesWithinAABB(EntityTF2Boss.class,
										player.getEntityBoundingBox().grow(200, 200, 200)).isEmpty()) {
							player.getCapability(TF2weapons.PLAYER_CAP,
									null).nextBossTicks = (int) (worldTime + Math.min(40000, TF2ConfigVars.bossReappear)
											+ player.getRNG().nextInt(Math.max(1, TF2ConfigVars.bossReappear - 40000)));
							EntityTF2Boss boss;
							int bosslowest = 31;
							int bossid = 0;
							if (TF2PlayerCapability.get(player).highestBossLevel.get(EntityHHH.class) < bosslowest) {
								bosslowest = TF2PlayerCapability.get(player).highestBossLevel.get(EntityHHH.class);
								bossid = 0;
							}
							if (TF2PlayerCapability.get(player).highestBossLevel
									.get(EntityMonoculus.class) < bosslowest) {
								bosslowest = TF2PlayerCapability.get(player).highestBossLevel
										.get(EntityMonoculus.class);
								bossid = 1;
							}
							if (TF2PlayerCapability.get(player).highestBossLevel
									.get(EntityMerasmus.class) < bosslowest) {
								bosslowest = TF2PlayerCapability.get(player).highestBossLevel.get(EntityMerasmus.class);
								bossid = 2;
							}
							switch (bossid) {
							case 0:
								boss = new EntityHHH(event.world);
								break;
							case 1:
								boss = new EntityMonoculus(event.world);
								break;
							default:
								boss = new EntityMerasmus(event.world);
								break;
							}
							player.sendMessage(new TextComponentTranslation("boss.message", boss.getName()));
							TF2PlayerCapability.get(player).bossSpawnTicks = worldTime + 1200;
							TF2PlayerCapability.get(player).bossToSpawn = boss;
						}
				}
			}

		}
	}

	public static boolean isSpawnEvent(World world) {
		return world.hasCapability(TF2weapons.WORLD_CAP, null)
				&& world.getCapability(TF2weapons.WORLD_CAP, null).eventFlag == 1;
	}

	/*
	 * @SubscribeEvent public void spawnCharacters(TickEvent.WorldTickEvent event){
	 * if(!event.getWorld().isRemote && event.phase==TickEvent.Phase.END){
	 *
	 * //if(time!=0&&event.getWorld().rand.nextInt(2500/time)!=0) return;
	 * this.eligibleChunksForSpawning.clear(); int i; int k;
	 *
	 * for (i = 0; i < event.getWorld().playerEntities.size(); ++i) { EntityPlayer
	 * entityplayer = (EntityPlayer)event.getWorld().playerEntities.get(i); int j =
	 * MathHelper.floor(entityplayer.posX / 16.0D); k =
	 * MathHelper.floor(entityplayer.posZ / 16.0D); byte b0 = 8;
	 *
	 * for (int l = -b0; l <= b0; ++l) { for (int i1 = -b0; i1 <= b0; ++i1) {
	 * boolean flag3 = l == -b0 || l == b0 || i1 == -b0 || i1 == b0;
	 * ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(l + j, i1 + k);
	 *
	 * if (!flag3) { this.eligibleChunksForSpawning.put(chunkcoordintpair,
	 * Boolean.valueOf(false)); } else if
	 * (!this.eligibleChunksForSpawning.containsKey(chunkcoordintpair)) {
	 * this.eligibleChunksForSpawning.put(chunkcoordintpair, Boolean.valueOf(true));
	 * } } } }
	 *
	 * i = 0; ChunkCoordinates chunkcoordinates = event.getWorld().getSpawnPoint();
	 * Iterator iterator = this.eligibleChunksForSpawning.keySet().iterator();
	 * ArrayList<ChunkCoordIntPair> tmp = new
	 * ArrayList(eligibleChunksForSpawning.keySet()); Collections.shuffle(tmp);
	 * iterator = tmp.iterator(); label110:
	 *
	 * while (iterator.hasNext()) { ChunkCoordIntPair chunkcoordintpair1 =
	 * (ChunkCoordIntPair)iterator.next();
	 *
	 * if (!((Boolean)this.eligibleChunksForSpawning.get(chunkcoordintpair1)).
	 * booleanValue()) { Chunk chunk =
	 * event.getWorld().getChunkFromChunkCoords(chunkcoordintpair1.chunkXPos,
	 * chunkcoordintpair1.chunkZPos); int x = chunkcoordintpair1.chunkXPos * 16 +
	 * event.getWorld().rand.nextInt(16); int z = chunkcoordintpair1.chunkZPos * 16
	 * + event.getWorld().rand.nextInt(16); int y =
	 * event.getWorld().rand.nextInt(chunk == null ?
	 * event.getWorld().getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
	 * System.out.println("tick2"); if (!event.getWorld().getBlock(x, y,
	 * z).isNormalCube() && event.getWorld().getBlock(x, y, z).getMaterial() ==
	 * Material.air) { int i2 = 0; int j2 = 0; int
	 * team=event.getWorld().rand.nextInt(2); System.out.println("tick3"); while (j2
	 * < 1) { int k2 = x; int l2 = y; int i3 = z; byte b1 = 6; IEntityLivingData
	 * ientitylivingdata = null; int j3 = 0; System.out.println("tick4"); while
	 * (true) { System.out.println("tick5"); if (j3 < 4) { label103: { k2 +=
	 * event.getWorld().rand.nextInt(b1) - event.getWorld().rand.nextInt(b1); l2 +=
	 * event.getWorld().rand.nextInt(1) - event.getWorld().rand.nextInt(1); i3 +=
	 * event.getWorld().rand.nextInt(b1) - event.getWorld().rand.nextInt(b1);
	 *
	 * if (canCreatureTypeSpawnAtLocation(EnumCreatureType.monster,
	 * event.getWorld(), k2, l2, i3)) {System.out.println("tick6"); float f =
	 * (float)k2 + 0.5F; float f1 = (float)l2; float f2 = (float)i3 + 0.5F;
	 *
	 * if (event.getWorld().getClosestPlayer((double)f, (double)f1, (double)f2,
	 * 24.0D) == null) { System.out.println("tick7"); float f3 = f -
	 * (float)chunkcoordinates.posX; float f4 = f1 - (float)chunkcoordinates.posY;
	 * float f5 = f2 - (float)chunkcoordinates.posZ; float f6 = f3 * f3 + f4 * f4 +
	 * f5 * f5;
	 *
	 * if (f6 >= 576.0F) { EntityTF2Character entityliving=null;
	 *
	 * System.out.println("tick8"); try { switch (event.getWorld().rand.nextInt(2)){
	 * case 1:entityliving = new EntityScout(event.getWorld()); default:entityliving
	 * = new EntityHeavy(event.getWorld()); } entityliving.setEntTeam(team); } catch
	 * (Exception exception) { exception.printStackTrace(); }
	 *
	 * entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2,
	 * event.getWorld().rand.nextFloat() * 360.0F, 0.0F);
	 *
	 * Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving,
	 * event.getWorld(), f, f1, f2); if (canSpawn == Result.ALLOW || (canSpawn ==
	 * Result.DEFAULT && entityliving.getCanSpawnHere())) {
	 * System.out.println("tick9"); ++i2;
	 * event.getWorld().spawnEntity(entityliving); if
	 * (!ForgeEventFactory.doSpecialSpawn(entityliving, event.getWorld(), f, f1,
	 * f2)) { ientitylivingdata = entityliving.onSpawnWithEgg(ientitylivingdata); }
	 *
	 * if (j2 >= ForgeEventFactory.getMaxSpawnPackSize(entityliving)) { continue
	 * label110; } }
	 *
	 * i += i2; } } }
	 *
	 * ++j3; continue; } } j2++; break; } } } } } } } public static boolean
	 * canCreatureTypeSpawnAtLocation(EnumCreatureType p_77190_0_, World p_77190_1_,
	 * int p_77190_2_, int p_77190_3_, int p_77190_4_) { if
	 * (!World.doesBlockHaveSolidTopSurface(p_77190_1_, p_77190_2_, p_77190_3_ - 1,
	 * p_77190_4_)) { return false; } else { Block block =
	 * p_77190_1_.getBlock(p_77190_2_, p_77190_3_ - 1, p_77190_4_); boolean
	 * spawnBlock = block.canCreatureSpawn(p_77190_0_, p_77190_1_, p_77190_2_,
	 * p_77190_3_ - 1, p_77190_4_); return spawnBlock && block != Blocks.bedrock &&
	 * !p_77190_1_.getBlock(p_77190_2_, p_77190_3_, p_77190_4_).isNormalCube() &&
	 * !p_77190_1_.getBlock(p_77190_2_, p_77190_3_,
	 * p_77190_4_).getMaterial().isLiquid() && !p_77190_1_.getBlock(p_77190_2_,
	 * p_77190_3_ + 1, p_77190_4_).isNormalCube(); } }
	 */
	@SubscribeEvent
	public void stopHurt(LivingAttackEvent event) {
		if (event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof EntityLivingBase
				&& (event.getSource().damageType.equals("mob") || event.getSource().damageType.equals("player"))) {
			EntityLivingBase damageSource = (EntityLivingBase) event.getSource().getTrueSource();
			if (!TF2Util.canInteract(damageSource)) {
				event.setCanceled(true);
			}
			if (damageSource.hasCapability(TF2weapons.WEAPONS_CAP, null)) {
				WeaponsCapability.get(damageSource).setDisguised(false);
			}
		}
		if (event.getSource().getTrueSource() instanceof EntityLivingBase
				&& event.getSource() instanceof TF2DamageSource
				&& ((TF2DamageSource) event.getSource()).hasAttackFlag(TF2DamageSource.BACKSTAB)) {
			ItemStack backpack = ItemBackpack.getBackpack(event.getEntityLiving());
			if (!backpack.isEmpty() && backpack.getTagCompound().getShort("Cooldown") <= 0
					&& TF2Attribute.getModifier("No Backstab", backpack, 0, event.getEntityLiving()) != 0) {
				((EntityLivingBase) event.getSource().getTrueSource())
						.addPotionEffect(new PotionEffect(TF2weapons.stun, 40));
				event.setCanceled(true);
				TF2Util.playSound(event.getEntityLiving(), TF2Sounds.RAZORBACK_BREAK, 1f, 1f);
				backpack.getTagCompound().setShort("Cooldown",
						(short) ((ItemBackpack) backpack.getItem()).getCooldown(backpack));
				return;
			}
		}
		if (!event.getSource().isDamageAbsolute() && !event.getSource().canHarmInCreative()) {
			if ((event.getEntityLiving().getActivePotionEffect(TF2weapons.bonk) != null
					|| event.getEntityLiving().getActivePotionEffect(TF2weapons.uber) != null)
					&& !event.getSource().canHarmInCreative()) {
				event.setCanceled(true);

				if (event.getEntityLiving() instanceof EntityPlayer
						&& event.getEntityLiving().getActivePotionEffect(TF2weapons.bonk) != null) {
					event.getEntityLiving().getCapability(TF2weapons.PLAYER_CAP, null).dodgedDmg += event.getAmount();
					/*
					 * if(event.getEntityLiving().getCapability(TF2weapons.PLAYER_CAP,
					 * null).dodgedDmg>100){
					 * ((EntityPlayer)event.getEntityLiving()).addStat(TF2Achievements.DODGE_DAMAGE)
					 * ; }
					 */
				}
				return;
			}
		}

		if (!event.isCanceled() && event.getAmount() > 0) {
			/*
			 * if(event.getEntity().getEntityData().getByte("IsCloaked")!=0){
			 * event.getEntity().getEntityData().setInteger("VisTicks",
			 * Math.min(10,event.getEntity().getEntityData().getInteger( "VisTicks")));
			 * event.getEntity().setInvisible(false); //System.out.println("notInvisible");
			 * }
			 */
			event.getEntityLiving().getEntityData().setInteger("lasthit", event.getEntityLiving().ticksExisted);
		}

	}

	@SubscribeEvent
	public void clonePlayer(final PlayerEvent.Clone event) {
		InventoryWearables oldInv = event.getOriginal().getCapability(TF2weapons.INVENTORY_CAP, null);
		InventoryWearables newInv = event.getEntityPlayer().getCapability(TF2weapons.INVENTORY_CAP, null);
		if (event.getEntityPlayer().world.getGameRules().getBoolean("keepInventory")) {
			for (int i = 0; i < oldInv.getSizeInventory(); i++) {
				newInv.setInventorySlotContents(i, oldInv.getStackInSlot(i));
				if (!oldInv.getStackInSlot(i).isEmpty()) {
					Multimap<String, AttributeModifier> modifiers = oldInv.getStackInSlot(i)
							.getAttributeModifiers(EntityEquipmentSlot.CHEST);
					if (i == 2) {
						modifiers.removeAll(SharedMonsterAttributes.ARMOR.getName());
						modifiers.removeAll(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName());
					}
					event.getEntityPlayer().getAttributeMap().applyAttributeModifiers(modifiers);
					if (i < TF2PlayerCapability.get(event.getEntityPlayer()).wearablesAttrib.length)
						TF2PlayerCapability.get(event.getEntityPlayer()).wearablesAttrib[i] = modifiers;
				}
			}
		} else {
			for (int i = 0; i < 2; i++) {
				newInv.setInventorySlotContents(i, oldInv.getStackInSlot(i));
			}
			newInv.setInventorySlotContents(4, oldInv.getStackInSlot(4));
			for (int i = 5; i < 8; i++)
				newInv.setInventorySlotContents(i, oldInv.getStackInSlot(i));
		}
		WeaponsCapability cap = WeaponsCapability.get(event.getEntityPlayer());
		cap.forcedClass = WeaponsCapability.get(event.getOriginal()).forcedClass;
		((ItemToken) TF2weapons.itemToken).updateAttributes(cap.forcedClass
				? new ItemStack(TF2weapons.itemToken, 1, WeaponsCapability.get(event.getOriginal()).getUsedToken())
				: newInv.getStackInSlot(4), event.getEntityPlayer());
		cap.ticksTotal = WeaponsCapability.get(event.getOriginal()).ticksTotal += 1000000L;

		TF2PlayerCapability.get(event.getEntityPlayer()).clone(TF2PlayerCapability.get(event.getOriginal()));

	}

	@SubscribeEvent
	public void respawnPlayer(final PlayerRespawnEvent event) {}

	@SubscribeEvent
	public void uber(LivingHurtEvent event) {
		/*
		 * if(event.getEntity().getEntityData().getByte("IsCloaked")!=0){
		 * event.getEntity().getEntityData().setInteger("VisTicks",
		 * Math.min(10,event.getEntity().getEntityData().getInteger("VisTicks")) );
		 * event.getEntity().setInvisible(false); //System.out.println("notInvisible");
		 * }
		 */
		// System.out.println("damage "+event.getAmount());
		if (event.isCanceled() || event.getAmount() <= 0)
			return;
		EntityLivingBase attacker = null;
		EntityLivingBase target = event.getEntityLiving();

		if (event.getSource().getTrueSource() != null
				&& event.getSource().getTrueSource() instanceof EntityLivingBase) {
			attacker = (EntityLivingBase) event.getSource().getTrueSource();
		}
		if (target.getActivePotionEffect(TF2weapons.crit) != null) {
			event.setAmount(event.getAmount() * 1.1f);
		}

		if (!event.getSource().isDamageAbsolute() && !event.getSource().canHarmInCreative()) {
			if (target.hasCapability(TF2weapons.WEAPONS_CAP, null)) {
				if (attacker != null)
					WeaponsCapability.get(target).lastAttacked = attacker;
				if (WeaponsCapability.get(target).getUsedToken() == 2 && event.getSource() == DamageSource.ON_FIRE) {
					event.setCanceled(true);
					target.extinguish();
				}
				if (WeaponsCapability.get(target).isInvisible()) {
					if (TF2Attribute.getModifier("Weapon Mode", ItemCloak.searchForWatches(target).getSecond(), 0,
							target) == 1)
						event.setAmount(event.getAmount() * 0.5f);
					else
						event.setAmount(event.getAmount() * 0.8f);
				}
				if (WeaponsCapability.get(target).isFeign()) {
					ItemStack watch = ItemCloak.getFeignDeathWatch(target);
					if (!watch.isEmpty()) {
						InventoryPlayer inv = null;
						if (target instanceof EntityPlayer) {
							inv = new InventoryPlayer((EntityPlayer) target);
							inv.copyInventory(((EntityPlayer) target).inventory);
						}
						event.setAmount(event.getAmount() * 0.15f);
						target.getCombatTracker().trackDamage(event.getSource(), target.getHealth(), event.getAmount());
						if (TF2ConfigVars.deadRingerTrigger)
							target.onDeath(event.getSource());
						TF2Util.sendTracking(new TF2Message.ActionMessage(24, target), target);
						target.extinguish();
						for (EntityLiving living : target.world.getEntitiesWithinAABB(EntityLiving.class,
								target.getEntityBoundingBox().grow(80), living -> {
									return living.getAttackTarget() == living;
								})) {
							living.setAttackTarget(null);
						}
						/*
						 * EntityPlayerMP feign=FakePlayerFactory.get((WorldServer)
						 * target.getEntityWorld(), ((EntityPlayer)target).getGameProfile());
						 * feign.setPositionAndRotation(target.posX, target.posY, target.posZ,
						 * target.rotationYaw, target.rotationPitch); feign.setHealth(0f);
						 * target.getEntityWorld().spawnEntity(feign);
						 */
						if (target instanceof EntityPlayer)
							((EntityPlayer) target).inventory.copyInventory(inv);
						((ItemCloak) watch.getItem()).setCloak(true, watch, target, target.getEntityWorld());
						WeaponsCapability.get(target).invisTicks = 20;
						target.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60, 1, false, false));
						WeaponsCapability.get(target).setFeign(false);
						target.setSilent(true);
					}
				}
				if (target.getCapability(TF2weapons.WEAPONS_CAP, null).isExpJump()) {
					if (event.getAmount() > 1 && event.getSource() == DamageSource.FALL) {
						event.setAmount((float) Math.sqrt(event.getAmount()));
						WeaponsCapability.get(event.getEntity()).setExpJump(false);
					}
					target.velocityChanged = false;
					target.addPotionEffect(new PotionEffect(TF2weapons.noKnockback, 1));
				}
			}

			ItemStack backpack = ItemBackpack.getBackpack(target);
			if (!backpack.isEmpty())
				event.setAmount(getDamageReductionFromItem(backpack, event.getSource(), target, event.getAmount()));

			for (ItemStack stack : target.getEquipmentAndArmor())
				if (!stack.isEmpty() && !(stack.getItem() instanceof ItemBackpack)) {
					// System.out.println("Damaged");
					event.setAmount(getDamageReductionFromItem(stack, event.getSource(), target, event.getAmount()));
				}
			if (target.getActivePotionEffect(TF2weapons.shieldBullet) != null && event.getSource().isProjectile()
					&& !event.getSource().isExplosion() && !event.getSource().isFireDamage()
					&& !event.getSource().isMagicDamage()) {
				if (target.getActivePotionEffect(TF2weapons.shieldBullet).getAmplifier() == 1)
					event.setAmount(event.getAmount() * 0.25f);
				else
					event.setAmount(event.getAmount() * 0.9f);
			} else if (target.getActivePotionEffect(TF2weapons.shieldExplosive) != null
					&& event.getSource().isExplosion()) {
				if (target.getActivePotionEffect(TF2weapons.shieldExplosive).getAmplifier() == 1)
					event.setAmount(event.getAmount() * 0.25f);
				else
					event.setAmount(event.getAmount() * 0.9f);
			} else if (target.getActivePotionEffect(TF2weapons.shieldFire) != null
					&& event.getSource().isFireDamage()) {
				if (target.getActivePotionEffect(TF2weapons.shieldFire).getAmplifier() == 1)
					event.setAmount(event.getAmount() * 0.25f);
				else
					event.setAmount(event.getAmount() * 0.9f);
			}
			if (target.getActivePotionEffect(TF2weapons.backup) != null) {
				if (event.getSource().getImmediateSource() instanceof EntityArrow) {
					event.setAmount(Math.min(event.getAmount(), 8f));
				}
				if ((event.getSource().damageType.equals("mob") || event.getSource().damageType.equals("player"))
						&& (attacker != null
								&& attacker.getAttributeMap()
										.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE) != null
								&& attacker.getAttributeMap()
										.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).getModifier(
												UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF")) != null)) {
					event.setAmount((float) Math.min(event.getAmount(),
							1 + attacker.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE)
									.getModifier(UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"))
									.getAmount()))/*
													 * ((EntityLivingBase)event. getSource().getEntity()).
													 * getHeldItemMainhand(). getAttributeModifiers(
													 * EntityEquipmentSlot.MAINHAND) .get(SharedMonsterAttributes.
													 * ATTACK_DAMAGE. getAttributeUnlocalizedName() ).toArray(new
													 * AttributeModifier[2])[0].))
													 */;
				}
				event.setAmount(event.getAmount() * 0.65f);
			}
			if (target instanceof EntityTF2Character && event.getSource().getTrueSource() != null
					&& event.getSource().getTrueSource() == event.getEntity()) {
				event.setAmount(event.getAmount() * 0.35f);
			}
		}

		int crit = 0;
		if (attacker != null && attacker.hasCapability(TF2weapons.WEAPONS_CAP, null)) {
			WeaponsCapability.get(attacker).lastAttacked = target;
			if (attacker.getActivePotionEffect(TF2weapons.it) != null
					&& (event.getSource().damageType.equals("mob") || event.getSource().damageType.equals("player"))) {
				attacker.removePotionEffect(TF2weapons.it);
				attacker.getCapability(TF2weapons.WEAPONS_CAP, null).itProtection = 15;
				target.addPotionEffect(new PotionEffect(TF2weapons.it, 600));
			}
			if (attacker.getActivePotionEffect(TF2weapons.bonk) != null)
				event.setCanceled(true);

			ItemStack stack = attacker == event.getSource().getImmediateSource() ? attacker.getHeldItemMainhand()
					: ItemStack.EMPTY;
			crit = TF2Util.calculateCritPre(stack, attacker);

			ItemStack backpack = ItemBackpack.getBackpack(attacker);
			if (backpack.getItem() instanceof ItemSoldierBackpack
					&& !WeaponsCapability.get(attacker).isRageActive(RageType.BANNER)) {
				((ItemSoldierBackpack) backpack.getItem()).addRage(backpack, event.getAmount(), target, attacker);
			}
			if (attacker.getActivePotionEffect(TF2weapons.conch) != null) {
				attacker.heal(0.35f * TF2Util.getDamageReduction(event.getSource(), target, event.getAmount()));
			}
			if (target.getActivePotionEffect(TF2weapons.madmilk) != null) {
				attacker.heal(0.6f * TF2Util.getDamageReduction(event.getSource(), target, event.getAmount()));
			}
		}
		if (!(event.getSource() instanceof TF2DamageSource)) {
			if (event.getSource().getImmediateSource() != null
					&& event.getSource().getImmediateSource().getEntityData().getBoolean("CritHeadshot"))
				crit = 2;
			crit = TF2Util.calculateCritPost(target, attacker, crit, ItemStack.EMPTY, event.getSource());
			if (crit == 1) {
				event.setAmount(event.getAmount() * 1.35f);
				TF2Util.playSound(target, TF2Sounds.MISC_MINI_CRIT, 1.5F,
						1.2F / (target.getRNG().nextFloat() * 0.2F + 0.9F));
			} else if (crit == 2) {
				event.setAmount(event.getAmount() * 2.2f);
				TF2Util.playSound(target, TF2Sounds.MISC_CRIT, 1.5F,
						1.2F / (target.getRNG().nextFloat() * 0.2F + 0.9F));
			}
		}
		if (attacker != null && target.getActivePotionEffect(TF2weapons.gas) != null) {
			target.removePotionEffect(TF2weapons.gas);

			TF2Util.explosion(target.world, attacker, ItemFromData.getNewStack("explodeonignite"), attacker, target,
					target.posX, target.posY, target.posZ, 2, 1f, 0, 0f);
			TF2Util.igniteAndAchievement(target, attacker, 8, 1f);
			// event.setAmount(event.getAmount() + 35f);
		}
		if (!target.world.isRemote && target.getDataManager().get(ENTITY_OVERHEAL) > 0) {
			target.getDataManager().set(ENTITY_OVERHEAL, target.getAbsorptionAmount());
			if (target.getDataManager().get(ENTITY_OVERHEAL) <= 0) {
				target.getDataManager().set(ENTITY_OVERHEAL, -1f);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void reduceDamageArmor(LivingHurtEvent event) {
		if (event.getSource() instanceof TF2DamageSource && event.getAmount() > 0f) {
			EntityLivingBase living = event.getEntityLiving();

			float orig = CombatRules.getDamageAfterAbsorb(event.getAmount(), living.getTotalArmorValue(),
					(float) living.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue())
					/ event.getAmount();
			float protect = orig;
			ItemStack weapon = ((TF2DamageSource) event.getSource()).getWeapon();
			if (!weapon.isEmpty() && weapon.getItem() instanceof ItemWeapon) {
				float damage = ((ItemWeapon) weapon.getItem()).getDamageForArmor(weapon,
						(EntityLivingBase) event.getSource().getTrueSource(), living);
				protect = CombatRules.getDamageAfterAbsorb(damage, living.getTotalArmorValue(),
						(float) living.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue())
						/ damage;
			}
			if (event.getSource().getTrueSource() != null
					&& event.getSource().getTrueSource().hasCapability(TF2weapons.WEAPONS_CAP, null)
					&& event.getSource().getTrueSource().getCapability(TF2weapons.WEAPONS_CAP,
							null).focusShotRemaining > 0) {

				float focus = TF2Attribute.getModifier("Focus", ((TF2DamageSource) event.getSource()).getWeapon(), 0,
						null);
				// float pierce = TF2Util.lerp(protect, 1f, 0.12f * focus);

				float hpDamage = event.getAmount() + Math.min(35 * focus, (event.getAmount()
						* (living.getMaxHealth() - living.getHealth()) / living.getMaxHealth() * focus * 0.6f));
				// if (hpDamage > event.getAmount() / pierce)
				event.setAmount(hpDamage);
				// else
				// protect = pierce;
			}

			TF2Util.addModifierSafe(living, SharedMonsterAttributes.ARMOR,
					new AttributeModifier(REMOVE_ARMOR, "remove_arm", -event.getEntityLiving().getTotalArmorValue(), 0),
					false);
			TF2Util.addModifierSafe(living, SharedMonsterAttributes.ARMOR_TOUGHNESS,
					new AttributeModifier(
							REMOVE_ARMOR, "remove_armt", -event.getEntityLiving()
									.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue(),
							0),
					false);
			// vent.getSource().setDamageBypassesArmor();
			event.setAmount(event.getAmount() * (protect));
			if (WeaponsCapability.get(event.getEntityLiving()) != null) {
				WeaponsCapability cap = WeaponsCapability.get(event.getEntityLiving());
				float armorDamage = event.getAmount();
				armorDamage += cap.damageArmorMin;
				cap.damageArmorMin = armorDamage % 4f;
				armorDamage /= 4f;

				if (armorDamage >= 1f) {
					for (ItemStack armor : event.getEntityLiving().getArmorInventoryList()) {
						if (armor.getItem() instanceof ItemArmor) {
							armor.damageItem((int) armorDamage, event.getEntityLiving());
						}
					}
				}
			}
			// System.out.println("Health2: "+event.getAmount() + " "+protect);

			// System.out.println("Health3: "+event.getAmount());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void reduceDamageArmor(LivingDamageEvent event) {
		if (event.getSource() instanceof TF2DamageSource) {
			event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(REMOVE_ARMOR);
			event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS)
					.removeModifier(REMOVE_ARMOR);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void healFromAttack(LivingDamageEvent event) {
		EntityLivingBase attacker = null;
		if (event.getSource().getTrueSource() instanceof EntityLivingBase)
			attacker = (EntityLivingBase) event.getSource().getTrueSource();
		EntityLivingBase target = event.getEntityLiving();
		if (attacker != null) {
			if (attacker.getActivePotionEffect(TF2weapons.conch) != null) {
				attacker.heal(TF2Util.getReducedHealing(attacker, target, 0.35f * event.getAmount()));
			}
			if (target.getActivePotionEffect(TF2weapons.madmilk) != null) {
				attacker.heal(TF2Util.getReducedHealing(attacker, target, 0.6f * event.getAmount()));
			}

		}
	}

	public static float getDamageReductionFromItem(ItemStack stack, DamageSource source, EntityLivingBase target,
			float damage) {
		float initialDamage = damage;
		if (TF2Attribute.getModifier("Breakable", stack, 0, target) != 0
				&& stack.getItemDamage() == stack.getMaxDamage())
			return damage;
		damage = TF2Attribute.getModifier("Damage Resist", stack, damage, target);
		if (source.isExplosion()) {
			damage = TF2Attribute.getModifier("Explosion Resist", stack, damage, target);
		}
		// System.out.println("Absorbed:
		// "+TF2Attribute.getModifier("Explosion Resist", stack, 1,
		// target));
		if (source.isFireDamage()) {
			damage = TF2Attribute.getModifier("Fire Resist", stack, damage, target);
		}
		if (target.getHeldItemMainhand() == stack) {
			if (source.isProjectile() || source.isExplosion() || source instanceof EntityDamageSourceIndirect)
				damage = TF2Attribute.getModifier("Ranged Resist", stack, damage, target);
			else if (!source.isProjectile() && !source.isExplosion() && !source.isMagicDamage()
					&& source instanceof EntityDamageSource)
				damage = TF2Attribute.getModifier("Melee Resist", stack, damage, target);
		}
		if (source == DamageSource.FALL && stack.getItem() instanceof ItemJetpack
				&& stack.getTagCompound().getBoolean("Active")) {
			damage = damage * 0.4f;
		}
		if (initialDamage != damage) {
			float mult = TF2Attribute.getModifier("Breakable", stack, 0, target);
			if (mult != 0) {
				stack.damageItem(MathHelper.ceil((initialDamage - damage) * mult), target);
				if (stack.isEmpty()) {
					stack.setCount(1);
					stack.setItemDamage(stack.getMaxDamage());
				}
			}
		}
		return damage;
	}

	@SubscribeEvent
	public void stopBreak(BlockEvent.BreakEvent event) {

		if (event.getPlayer().getHeldItem(EnumHand.MAIN_HAND) != null
				&& event.getPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemUsable
				&& !TF2PlayerCapability.get(event.getPlayer()).breakBlocks) {
			event.setCanceled(true);
		}
		if (event.getPlayer().getActivePotionEffect(TF2weapons.bonk) != null) {
			event.setCanceled(true);
		}
		if (event.getPlayer().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks > 0) {
			event.setCanceled(true);
		}
		if (event.getPlayer().getActivePotionEffect(TF2weapons.stun) != null) {
			event.setCanceled(true);
		}
		WeaponsCapability.get(event.getPlayer()).setDisguised(false);
	}

	/*
	 * @SubscribeEvent public void stopInteract(PlayerInteractEvent event){
	 * if(!((event.==PlayerInteractEvent.Action.RIGHT_CLICK_AIR||event.action==
	 * PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)&&event.getEntity()Player.
	 * getHeldItem(EnumHand.MAIN_HAND)!=null&&(event.getEntity()Player.
	 * getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemCloak ||
	 * event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof
	 * ItemDisguiseKit))){
	 * if(event.getEntity()Player.getEntityData().getByte("Disguised")!=0){
	 * disguise(event.getEntity()Player,false); }
	 * if((event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND) !=
	 * null&&event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND).getItem()
	 * instanceof ItemUsable &&
	 * event.action==PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)||event.
	 * getEntity()Player.getEntityData().getInteger("VisTicks")!=0){
	 * event.setCanceled(true); } } }
	 */

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityItem
				&& ((EntityItem) event.getEntity()).getItem().getItem() instanceof IBackpackItem) {
			event.getEntity().setDead();
			event.setCanceled(true);
		}
		if (event.getEntity() instanceof EntityCreature && TF2Util.isHostile((EntityLivingBase) event.getEntity())
				&& !(event.getEntity() instanceof IEntityTF2) && event.getEntity().isNonBoss()) {
			if (TF2ConfigVars.targetSentries) {
				((EntityCreature) event.getEntity()).targetTasks.addTask(535,
						new EntityAINearestAttackableTarget<EntitySentry>((EntityCreature) event.getEntity(),
								EntitySentry.class, 10, true, false, sentry -> {
									return sentry.getOwnerId() != null;
								}) {
							@Override
							protected double getTargetDistance() {
								return super.getTargetDistance() * 0.45f;
							}
						});
			}
			if (TF2ConfigVars.attackMobs) {
				((EntityCreature) event.getEntity()).targetTasks.addTask(536, new EntityAINearestAttackableTarget<>(
						(EntityCreature) event.getEntity(), EntityTF2Character.class, 10, true, false, (target) -> {
							return !target.isRobot();
						}));
			}
		}
		if (event.getEntity() instanceof EntityPlayer) {
			if (event.getEntity().world.isRemote) {
				if (event.getEntity() == ClientProxy.getLocalPlayer())
					TF2weapons.network.sendToServer(new TF2Message.InitClientMessage(TF2weapons.conf));
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(99, (EntityLivingBase) event.getEntity()));
			}
			if (event.getEntity().world != null && !event.getEntity().world.isRemote
					&& event.getEntity() instanceof EntityPlayerMP) {
				EntityPlayerMP player = ((EntityPlayerMP) event.getEntity());
				player.inventoryContainer.addListener(new TF2ContainerListener(player));
			}
		}
	}

	@SubscribeEvent
	public void entityConstructing(final EntityEvent.EntityConstructing event) {

		if (event.getEntity() instanceof EntityLivingBase) {
			// event.getEntity().getDataManager().register(ENTITY_UBER, false);
			event.getEntity().getDataManager().register(ENTITY_OVERHEAL, 0f);
		}

		if (event.getEntity() instanceof EntityPlayer && TF2UdpClient.instance != null) {
			TF2UdpClient.instance.shutdown();
			TF2UdpClient.instance = null;
		}
	}

	/*
	 * @SubscribeEvent public void ChunkLoad(ChunkEvent.Load event){
	 * if(!event.getWorld().isRemote){ List<EntityTeleporter>
	 * teleporter=event.getWorld().getEntitiesWithinAABB(EntityTeleporter.class, new
	 * AxisAligned())); } }
	 */
	@SubscribeEvent
	public void cleanPlayer(PlayerLoggedOutEvent event) {
		if (TF2weapons.udpServer != null)
			TF2weapons.udpServer.playerList.remove(event.player.getCapability(TF2weapons.PLAYER_CAP, null).udpServerId);
	}

	@SubscribeEvent
	public void loadPlayer(PlayerLoggedInEvent event) {
		// System.out.println("LoggedIn");
		if (TF2weapons.server.isDedicatedServer() || Minecraft.getMinecraft().getIntegratedServer().getPublic()) {
			TF2weapons.network.sendTo(new TF2Message.WeaponDataMessage(TF2weapons.itemDataCompressed),
					(EntityPlayerMP) event.player);
		}
		int i = 0;
		for (Contract contract : event.player.getCapability(TF2weapons.PLAYER_CAP, null).contracts) {
			TF2weapons.network.sendTo(new TF2Message.ContractMessage(i, contract), (EntityPlayerMP) event.player);
			i++;
		}
		int udpport = -1;
		if (TF2weapons.udpServer != null) {
			event.player.getCapability(TF2weapons.PLAYER_CAP, null).udpServerId = TF2UdpServer.nextPlayerId;
			TF2weapons.udpServer.playerList.put(TF2UdpServer.nextPlayerId, (EntityPlayerMP) event.player);
			udpport = TF2weapons.udpServer.port;
			TF2UdpServer.nextPlayerId++;
		}
		TF2weapons.network.sendTo(
				new TF2Message.InitMessage(udpport, TF2UdpServer.nextPlayerId, TF2ConfigVars.buildingsUseEnergy),
				(EntityPlayerMP) event.player);
	}

	/*
	 * @SubscribeEvent public void onConnect(ServerConnectionFromClientEvent event){
	 * new NetHandlerPlayServer(MinecraftServer.getServer(), event.manager,
	 * ((NetHandlerPlayServer)event.handler).playerEntity); }
	 */
	/*
	 * public static class PacketReceiveHack extends
	 * SimpleChannelInboundHandler<Packet>{
	 *
	 * @Override protected void channelRead0(ChannelHandlerContext ctx, Packet msg)
	 * throws Exception { System.out.println(msg); }
	 *
	 * }
	 */
	/*
	 * public static class MovePacketHack extends NetHandlerPlayServer{
	 *
	 * public MovePacketHack(MinecraftServer server, NetworkManager
	 * networkManagerIn, EntityPlayerMP playerIn) { super(server, networkManagerIn,
	 * playerIn); } public void processPlayer(C03PacketPlayer packetIn) {
	 * System.out.println("send"); super.processPlayer(packetIn); } }
	 */
	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.RightClickBlock event) {
		if (!TF2Util.canInteract(event.getEntityLiving())) {
			event.setCanceled(true);
		}
		event.getEntityPlayer().removePotionEffect(TF2weapons.charging);
	}

	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.RightClickItem event) {
		ItemStack item = event.getEntityPlayer().getHeldItem(event.getHand());
		if (!TF2Util.canInteract(event.getEntityLiving()) && !(item.getItem() instanceof ItemDisguiseKit)
				&& !(item.getItem() instanceof ItemCloak && (item.getTagCompound().getBoolean("Active")
						|| (WeaponsCapability.get(event.getEntity()).isFeign()
								&& TF2Attribute.getModifier("Weapon Mode", item, 0, event.getEntityLiving()) == 1)))) {
			event.setCanceled(true);
			return;
		}
		if (!(item.getItem() instanceof ItemFood || item.getItem() instanceof ItemCloak
				|| item.getItem() instanceof ItemDisguiseKit)) {
			WeaponsCapability.get(event.getEntity()).setDisguised(false);
		}
		event.getEntityPlayer().removePotionEffect(TF2weapons.charging);
	}

	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.EntityInteract event) {

		if (!TF2Util.canInteract(event.getEntityLiving())) {
			event.setCanceled(true);
			return;
		}
		if (!(event.getEntityPlayer().getHeldItem(event.getHand()).getItem() instanceof ItemFood)) {
			WeaponsCapability.get(event.getEntity()).setDisguised(false);
		}
		event.getEntityPlayer().removePotionEffect(TF2weapons.charging);
	}

	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.LeftClickBlock event) {
		if (!TF2Util.canInteract(event.getEntityLiving())) {
			event.setCanceled(true);
		}
		event.getEntityPlayer().removePotionEffect(TF2weapons.charging);
	}

	@SubscribeEvent
	public void stopJump(LivingEvent.LivingJumpEvent event) {

		EntityLivingBase living = event.getEntityLiving();

		if (WeaponsCapability.get(living) != null && WeaponsCapability.get(living).isGrappled()) {
			WeaponsCapability.get(living).setGrappled(false);
		}

		if ((living.getActivePotionEffect(TF2weapons.stun) != null
				&& living.getActivePotionEffect(TF2weapons.bombmrs) == null)
				|| living.getActivePotionEffect(TF2weapons.charging) != null
				|| (living.getHeldItemMainhand() != null
						&& (living.getHeldItemMainhand().getItem() instanceof ItemMinigun
								|| living.getHeldItemMainhand().getItem() instanceof ItemHuntsman)
						&& living.hasCapability(TF2weapons.WEAPONS_CAP, null)
						&& living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks > 0)) {
			living.isAirBorne = false;
			living.motionY -= 0.5f;
			if (living.isSprinting()) {
				float f = living.rotationYaw * 0.017453292F;
				living.motionX += MathHelper.sin(f) * 0.2F;
				living.motionZ -= MathHelper.cos(f) * 0.2F;
			}

		}
		if (living.getHeldItemMainhand().getItem() instanceof ItemSniperRifle
				&& living.hasCapability(TF2weapons.WEAPONS_CAP, null))
			((ItemSniperRifle) living.getHeldItemMainhand().getItem()).disableZoom(living.getHeldItemMainhand(),
					living);

	}

	@SubscribeEvent
	public void livingUpdate(final LivingEvent.LivingUpdateEvent event) {
		event.getEntity().getEntityWorld().profiler.startSection("TF2TickEvent");
		final EntityLivingBase living = event.getEntityLiving();
		if (living.hasCapability(TF2weapons.INVENTORY_CAP, null)) {
			for (int i = 0; i < 3; i++) {
				ItemStack stack = living.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof ItemWearable)
					((ItemWearable) stack.getItem()).onUpdateWearing(stack, living.world, living);
				else if (!stack.isEmpty() && stack.getItem() instanceof ItemBackpack)
					((ItemBackpack) stack.getItem()).onArmorTickAny(living.world, living, stack);
			}
			if (!living.world.isRemote && living.ticksExisted % 2 == 0)
				living.getCapability(TF2weapons.INVENTORY_CAP, null).updateSlots();
		}
		if (living.hasCapability(TF2weapons.PLAYER_CAP, null))
			living.getCapability(TF2weapons.PLAYER_CAP, null).tick();
		if (living.isEntityAlive() && (living.hasCapability(TF2weapons.WEAPONS_CAP, null))) {

			// long nanoTickStart=System.nanoTime();
			final WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
			cap.tick();

			// if(!living.world.isRemote)
			// tickTimeLiving[TF2weapons.server.getTickCounter()%20]+=System.nanoTime()-nanoTickStart;
		}

		if (living.getRevengeTarget() != null && living.getRevengeTarget().hasCapability(TF2weapons.WEAPONS_CAP, null)
				&& living.getRevengeTarget().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks >= 45) {
			living.setRevengeTarget(null);
		}

		if (living instanceof EntityLiving && ((EntityLiving) living).getAttackTarget() != null) {
			if (living.getEntityData().hasKey("TF2AM")
					&& living.getEntityWorld().getTotalWorldTime() - living.getEntityData().getInteger("TF2AM") > 360) {
				living.getEntityData().removeTag("TF2AM");
				living.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(TF2Util.FOLLOW_MODIFIER);
			} else if (((EntityLiving) living).getAttackTarget().hasCapability(TF2weapons.WEAPONS_CAP, null)
					&& ((EntityLiving) living).getAttackTarget().getCapability(TF2weapons.WEAPONS_CAP,
							null).invisTicks >= 45) {
				((EntityLiving) living).setAttackTarget(null);
			}
		}

		if (living.getDataManager().get(ENTITY_OVERHEAL) == -1) {
			living.getDataManager().set(ENTITY_OVERHEAL, 0F);
			living.setAbsorptionAmount(0);
		}
		if (living.getDataManager().get(ENTITY_OVERHEAL) > 0) {
			if (living.world.isRemote) {
				living.setAbsorptionAmount(living.getDataManager().get(ENTITY_OVERHEAL));
			}
			living.setAbsorptionAmount(living.getAbsorptionAmount() - living.getMaxHealth() * 0.001666f);
			if (!living.world.isRemote && living.ticksExisted % 4 == 0)
				if (living.getAbsorptionAmount() <= 0) {
					living.getDataManager().set(ENTITY_OVERHEAL, -1f);
					// living.getEntityData().setFloat("Overheal", -1f);
				} else {
					living.getDataManager().set(ENTITY_OVERHEAL, living.getAbsorptionAmount());
				}
		}
		living.world.profiler.endSection();
		/*
		 * if (living.getActivePotionEffect(TF2weapons.uber)!=null &&
		 * !(living.getHeldItem(EnumHand.MAIN_HAND) != null &&
		 * living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemMedigun &&
		 * living.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean(
		 * "Activated"))) { List<EntityLivingBase> list =
		 * living.world.getEntitiesWithinAABB(EntityLivingBase.class, new
		 * AxisAlignedBB(living.posX - 8, living.posY - 8, living.posZ - 8, living.posX
		 * + 8, living.posY + 8, living.posZ + 8), new Predicate<EntityLivingBase>() {
		 *
		 * @Override public boolean apply(EntityLivingBase input) { return
		 * input.world.getEntityByID( input.getCapability(TF2weapons.WEAPONS_CAP, null)
		 * != null ? input.getCapability(TF2weapons.WEAPONS_CAP, null).healTarget : -1)
		 * == living &&
		 * input.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean("Activated"
		 * ); }
		 *
		 * }); boolean isOK = !list.isEmpty(); if (!isOK) {
		 * living.getEntityData().setBoolean("Ubercharge", false); } }
		 */
	}

	@SubscribeEvent
	public void loadWorld(WorldEvent.Load event) {
		/*
		 * if (!event.getWorld().isRemote &&
		 * event.getWorld().getPerWorldStorage().getOrLoadData(TF2WorldStorage.class,
		 * TF2weapons.MOD_ID)==null){
		 * event.getWorld().getPerWorldStorage().setData(TF2weapons.MOD_ID, new
		 * TF2WorldStorage()); dummyEnt = new EntityCreeper(null); }
		 */
		if (event.getWorld().isRemote) {
			TF2EventsCommon.sentryView = new EntitySentry(event.getWorld());
			TF2EventsCommon.sentryView.onDeath(DamageSource.GENERIC);
			TF2EventsCommon.dispenserView = new EntityDispenser(event.getWorld());
			TF2EventsCommon.dispenserView.onDeath(DamageSource.GENERIC);
			TF2EventsCommon.teleporterAView = new EntityTeleporter(event.getWorld());
			TF2EventsCommon.teleporterAView.onDeath(DamageSource.GENERIC);
			TF2EventsCommon.teleporterBView = new EntityTeleporter(event.getWorld());
			TF2EventsCommon.teleporterBView.onDeath(DamageSource.GENERIC);
		}
		if (TF2weapons.dummyEnt == null)
			TF2weapons.dummyEnt = new EntityDummy(event.getWorld());
		if (!event.getWorld().getGameRules().hasRule("doTF2AI"))
			event.getWorld().getGameRules().addGameRule("doTF2AI", "true", ValueType.BOOLEAN_VALUE);
		if (!event.getWorld().isRemote && event.getWorld().getScoreboard().getTeam("RED") == null) {
			ScorePlayerTeam teamRed = event.getWorld().getScoreboard().createTeam("RED");
			ScorePlayerTeam teamBlu = event.getWorld().getScoreboard().createTeam("BLU");

			teamRed.setSeeFriendlyInvisiblesEnabled(true);
			teamRed.setAllowFriendlyFire(false);
			teamBlu.setSeeFriendlyInvisiblesEnabled(true);
			teamBlu.setAllowFriendlyFire(false);
			teamRed.setPrefix(TextFormatting.RED.toString());
			teamBlu.setPrefix(TextFormatting.BLUE.toString());
			teamRed.setColor(TextFormatting.RED);
			teamBlu.setColor(TextFormatting.BLUE);
			event.getWorld().getScoreboard().broadcastTeamInfoUpdate(teamRed);
			event.getWorld().getScoreboard().broadcastTeamInfoUpdate(teamBlu);

		}
		if (!event.getWorld().isRemote && event.getWorld().getScoreboard().getTeam("TF2Bosses") == null) {
			ScorePlayerTeam teamBosses = event.getWorld().getScoreboard().createTeam("TF2Bosses");
			teamBosses.setSeeFriendlyInvisiblesEnabled(true);
			teamBosses.setAllowFriendlyFire(false);
			teamBosses.setPrefix(TextFormatting.DARK_PURPLE.toString());
			teamBosses.setColor(TextFormatting.DARK_PURPLE);
			event.getWorld().getScoreboard().broadcastTeamInfoUpdate(teamBosses);
		}
		if (!event.getWorld().isRemote && event.getWorld().getScoreboard().getTeam("Robots") == null) {
			ScorePlayerTeam teamRobots = event.getWorld().getScoreboard().createTeam("Robots");
			teamRobots.setSeeFriendlyInvisiblesEnabled(true);
			teamRobots.setAllowFriendlyFire(false);
			teamRobots.setPrefix(TextFormatting.AQUA.toString());
			teamRobots.setColor(TextFormatting.AQUA);
			event.getWorld().getScoreboard().broadcastTeamInfoUpdate(teamRobots);
		}
		if (!event.getWorld().isRemote
				&& event.getWorld().getScoreboard().getTeam("RED").getColor() == TextFormatting.RESET) {
			event.getWorld().getScoreboard().getTeam("RED").setColor(TextFormatting.RED);
			event.getWorld().getScoreboard().getTeam("BLU").setColor(TextFormatting.BLUE);
		}
	}

	@SubscribeEvent
	public void unloadWorld(WorldEvent.Unload event) {
		if (TF2weapons.dummyEnt != null && TF2weapons.dummyEnt.world == event.getWorld())
			TF2weapons.dummyEnt = null;
		if (event.getWorld().isRemote && sentryView.world == event.getWorld()) {
			TF2EventsCommon.sentryView = null;
			TF2EventsCommon.dispenserView = null;
			TF2EventsCommon.teleporterAView = null;
			TF2EventsCommon.teleporterBView = null;
		}
	}

	@SubscribeEvent
	public void medicSpawn(LivingSpawnEvent.SpecialSpawn event) {
		float chance = 0f;

		if (event.getEntity() instanceof EntityHeavy) {
			chance = 0.16f;
		} else if (event.getEntity() instanceof EntitySoldier) {
			chance = 0.08f;
		} else if (event.getEntity() instanceof EntityDemoman) {
			chance = 0.07f;
		} else if (event.getEntity() instanceof EntityPyro) {
			chance = 0.06f;
		} else if (event.getEntity() instanceof EntityScout) {
			chance = 0.03f;
		} else
			return;
		chance *= TF2ConfigVars.medicChance;
		if (event.getWorld().rand.nextFloat() < event.getWorld().getDifficulty().getDifficultyId() * chance) {
			((EntityTF2Character) event.getEntity()).spawnMedic = true;
			/*
			 * EntityMedic medic = new EntityMedic(event.getWorld());
			 * medic.setLocationAndAngles(event.getEntity().posX +
			 * event.getWorld().rand.nextDouble() * 0.5 - 0.25, event.getEntity().posY,
			 * event.getEntity().posZ + event.getWorld().rand.nextDouble() * 0.5 - 0.25,
			 * event.getWorld().rand.nextFloat() * 360.0F, 0.0F); medic.natural = true; //
			 * medic.setEntTeam(event.getWorld().rand.nextInt(2));
			 * medic.onInitialSpawn(event.getWorld().getDifficultyForLocation(new
			 * BlockPos(event.getX(), event.getY(), event.getZ())), null);
			 * EntityTF2Character.nextEntTeam = medic.getEntTeam();
			 *
			 * event.getWorld().spawnEntity(medic);
			 */
		}
	}

	@SubscribeEvent
	public void attachCapabilityEnt(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(new ResourceLocation(TF2weapons.MOD_ID, "weaponcapability"),
					new WeaponsCapability((EntityLivingBase) event.getObject()));
			if (!event.getObject().hasCapability(TF2weapons.INVENTORY_CAP, null)) {
				final InventoryWearables inv = new InventoryWearables((EntityPlayer) event.getObject());
				event.addCapability(new ResourceLocation(TF2weapons.MOD_ID, "wearablescapability"), inv);
			}
			event.addCapability(new ResourceLocation(TF2weapons.MOD_ID, "playercapability"),
					new TF2PlayerCapability((EntityPlayer) event.getObject()));
		}
	}

	@SubscribeEvent
	public void attachCapabilityWorld(AttachCapabilitiesEvent<World> event) {
		event.addCapability(new ResourceLocation(TF2weapons.MOD_ID, "tf2worldcapability"),
				new TF2WorldStorage(event.getObject()));
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public void placeBanner(BlockEvent.PlaceEvent event) {
		TileEntity banner = event.getWorld().getTileEntity(event.getBlockSnapshot().getPos());
		if (banner != null && banner instanceof TileEntityBanner) {
			List<BannerPattern> patterns = getPatterns((TileEntityBanner) banner);
			if (patterns.contains(TF2weapons.redPattern) || patterns.contains(TF2weapons.bluPattern)
					|| patterns.contains(TF2weapons.neutralPattern)) {
				// System.out.println("Banner is");
				event.getWorld().getCapability(TF2weapons.WORLD_CAP, null).banners.add(event.getPos());
			}
		}

	}

	@SubscribeEvent
	public void removeBanner(BlockEvent.BreakEvent event) {
		TileEntity banner = event.getWorld().getTileEntity(event.getPos());
		if (banner != null && banner instanceof TileEntityBanner) {
			if (getPatterns((TileEntityBanner) banner).contains(TF2weapons.redPattern)) {
				event.getWorld().getCapability(TF2weapons.WORLD_CAP, null).banners.remove(event.getPos());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void craftItem(net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent event) {
		ItemStack withPattern = ItemStack.EMPTY;
		ItemStack noPattern = ItemStack.EMPTY;
		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
			ItemStack stack = event.craftMatrix.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ItemBanner) {
				if (TileEntityBanner.getPatterns(stack) > 0) {
					withPattern = stack;
				} else {
					noPattern = stack;
				}
				if (!withPattern.isEmpty() && !noPattern.isEmpty()) {
					withPattern.shrink(1);
				}
			}
		}
	}

	/*
	 * @SubscribeEvent public void attachCapabilityItem(AttachCapabilitiesEvent.Item
	 * event) { if (event.getItem() instanceof ItemFromData) {
	 * System.out.println("Adding cap"); event.addCapability(new
	 * ResourceLocation(TF2weapons.MOD_ID, "weapondatacapability"), new
	 * WeaponData.WeaponDataCapability()); } }
	 */
	@SubscribeEvent
	public void livingDeath(LivingDeathEvent event) {
		if (event.isCanceled())
			return;
		if (!event.getEntity().world.isRemote && event.getSource() != null && event.getSource().getTrueSource() != null
				&& event.getSource().getTrueSource() instanceof EntityLivingBase) {
			ItemStack stack = ItemStack.EMPTY;
			final EntityLivingBase living = (EntityLivingBase) event.getSource().getTrueSource();
			boolean isEnemy = TF2Util.isEnemy(living, event.getEntityLiving());
			if (event.getSource() instanceof TF2DamageSource) {

				stack = ((TF2DamageSource) event.getSource()).getWeaponOrig();

			} else {
				stack = living.getHeldItemMainhand();
			}
			if (living instanceof EntityPlayerMP && isEnemy) {
				EntityPlayerMP player = (EntityPlayerMP) living;
				TF2PlayerCapability plcap = player.getCapability(TF2weapons.PLAYER_CAP, null);
				if (event.getEntity() instanceof EntityTF2Character
						&& !((EntityTF2Character) event.getEntity()).isRobot()) {
					if ((plcap.fastKillTimer += 120) > 360) {
						plcap.fastKillTimer = 0;
						plcap.completeObjective(Objective.KILLS, stack);
					}
					plcap.completeObjective(Objective.KILL, stack);
					if (event.getSource() instanceof TF2DamageSource
							&& ((TF2DamageSource) event.getSource()).hasAttackFlag(TF2DamageSource.HEADSHOT)) {
						plcap.completeObjective(Objective.HEADSHOT, stack);
					}
					if (event.getSource() instanceof TF2DamageSource
							&& ((TF2DamageSource) event.getSource()).hasAttackFlag(TF2DamageSource.BACKSTAB)) {
						plcap.completeObjective(Objective.BACKSTAB, stack);
					}
					if (event.getSource().getImmediateSource() instanceof EntityProjectileBase
							&& ((EntityProjectileBase) event.getSource().getImmediateSource()).reflected) {
						plcap.completeObjective(Objective.KILL_REFLECTED, stack);
					}
					if (stack.getItem() instanceof ItemStickyLauncher) {
						plcap.completeObjective(Objective.STICKY_KILL, stack);
					}
					if (stack.getItem() instanceof ItemMeleeWeapon) {
						plcap.completeObjective(Objective.KILL_MELEE, stack);
					}
					if (player.getCapability(TF2weapons.WEAPONS_CAP, null).isExpJump()) {
						plcap.completeObjective(Objective.KILL_BLAST, stack);
					}
					if (player.getCapability(TF2weapons.WEAPONS_CAP, null).airJumps > 0) {
						plcap.completeObjective(Objective.KILL_DOUBLE, stack);
					}
					if (event.getEntity() instanceof EntityScout)
						plcap.completeObjective(Objective.KILL_SCOUT, stack);
					else if (event.getEntity() instanceof EntityPyro)
						plcap.completeObjective(Objective.KILL_PYRO, stack);
					else if (event.getEntity() instanceof EntitySoldier)
						plcap.completeObjective(Objective.KILL_SOLDIER, stack);
					else if (event.getEntity() instanceof EntityHeavy)
						plcap.completeObjective(Objective.KILL_HEAVY, stack);
					else if (event.getEntity() instanceof EntityDemoman)
						plcap.completeObjective(Objective.KILL_DEMOMAN, stack);
					else if (event.getEntity() instanceof EntityEngineer)
						plcap.completeObjective(Objective.KILL_ENGINEER, stack);
					else if (event.getEntity() instanceof EntityMedic)
						plcap.completeObjective(Objective.KILL_MEDIC, stack);
					else if (event.getEntity() instanceof EntitySpy)
						plcap.completeObjective(Objective.KILL_SPY, stack);
					else if (event.getEntity() instanceof EntitySniper)
						plcap.completeObjective(Objective.KILL_SNIPER, stack);
				}

			} else if (living instanceof EntityPlayerMP && event.getEntity() instanceof EntityBuilding
					&& !TF2Util.isOnSameTeam(living, event.getEntity())) {
				living.getCapability(TF2weapons.PLAYER_CAP, null).completeObjective(Objective.DESTROY_BUILDING, stack);
			}
			if (event.getEntity() instanceof EntitySentry) {
				for (EntityPlayerMP player : living.world.getEntitiesWithinAABB(EntityPlayerMP.class,
						living.getEntityBoundingBox().grow(8, 8, 8), new Predicate<EntityPlayerMP>() {

							@Override
							public boolean apply(EntityPlayerMP input) {
								return input.getCapability(TF2weapons.WEAPONS_CAP, null).getHealTarget() == living
										.getEntityId()
										&& input.getHeldItemMainhand().getTagCompound().getBoolean("Activated");
							}

						})) {
					player.getCapability(TF2weapons.PLAYER_CAP, null).completeObjective(Objective.DESTROY_SENTRY_UBER,
							stack);
				}

			}
			if (!stack.isEmpty() && stack.getItem() instanceof ItemWeapon) {

				if (!(event.getEntity() instanceof EntityPlayer)
						&& TF2Attribute.getModifier("Silent Kill", stack, 0, null) != 0) {
					event.getEntity().setSilent(true);
				}

				if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Strange")) {

					if (!(event.getEntityLiving() instanceof EntityPlayer)) {
						stack.getTagCompound().setInteger("Kills", stack.getTagCompound().getInteger("Kills") + 1);
					} else {
						stack.getTagCompound().setInteger("PlayerKills",
								stack.getTagCompound().getInteger("PlayerKills") + 1);
					}
					onStrangeUpdate(stack, living);

				}
				if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBTLiterals.STREAK_ATTRIB) && isEnemy) {
					stack.getTagCompound().setInteger(NBTLiterals.STREAK_KILLS,
							stack.getTagCompound().getInteger(NBTLiterals.STREAK_KILLS) + 1);
					stack.getTagCompound().setLong(NBTLiterals.STREAK_COOL,
							WeaponsCapability.get(living).ticksTotal + ItemKillstreakKit.getCooldown(
									stack.getTagCompound().getByte(NBTLiterals.STREAK_LEVEL),
									stack.getTagCompound().getInteger(NBTLiterals.STREAK_KILLS)));
					/*
					 * stack.getTagCompound().setLong(NBTLiterals.STREAK_COOL,
					 * WeaponsCapability.get(living).ticksTotal +
					 * Math.max(100,ItemKillstreakKit.getCooldown(stack.getTagCompound().getByte(
					 * NBTLiterals.STREAK_LEVEL)) -
					 * MathHelper.log2(stack.getTagCompound().getInteger(NBTLiterals.STREAK_KILLS))*
					 * 250));
					 */
					stack.getTagCompound().setLong(NBTLiterals.STREAK_LAST, WeaponsCapability.get(living).ticksTotal);
					stack.getTagCompound().setShort(NBTLiterals.STREAK_REDUCTION, (short) 1);

					stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).cached = false;
				}
				if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Australium")
						&& TF2ConfigVars.australiumStatue) {
					event.getEntity().world
							.spawnEntity(new EntityStatue(event.getEntity().world, event.getEntityLiving(), false));
					TF2Util.sendTracking(new TF2Message.ActionMessage(19, event.getEntityLiving()), event.getEntity());
					event.getEntity().playSound(TF2Sounds.WEAPON_TO_GOLD, 1.5f, 2f);
					// event.getEntityLiving().deathTime = 20;
					// event.getEntityLiving().onEntityUpdate();
					if (!(event.getEntity() instanceof EntityPlayer)) {
						event.getEntity().setSilent(true);
					}
				}
				if (isEnemy && living.hasCapability(TF2weapons.WEAPONS_CAP, null)
						&& TF2Attribute.getModifier("Kill Count", stack, 0, living) != 0) {
					living.getCapability(TF2weapons.WEAPONS_CAP, null).addHead(stack);
				}
				float toHeal = TF2Attribute.getModifier("Health Kill", stack, 0, living);
				if (toHeal != 0) {
					living.heal(TF2Util.getReducedHealing(living, event.getEntityLiving(),
							isEnemy ? toHeal : toHeal * 0.35f));
				}
				if (TF2Attribute.getModifier("Crit Kill", stack, 0, living) > 0) {
					living.addPotionEffect(new PotionEffect(TF2weapons.critBoost,
							(int) TF2Attribute.getModifier("Crit Kill", stack, -1, living) * 20, 1));
				}
				if (event.getEntityLiving() instanceof EntityPlayer && !event.getEntity().world.isRemote) {
					ItemStack held = event.getEntityLiving().getHeldItemMainhand();
					if (!held.isEmpty() && held.getItem() instanceof ItemUsable) {
						((ItemUsable) held.getItem()).holster(
								event.getEntityLiving().getCapability(TF2weapons.WEAPONS_CAP, null), held,
								event.getEntityLiving(), event.getEntityLiving().world);
					}
				}
			}
		}
		if (event.getEntityLiving().getEntityData().hasKey("Cleavers")) {
			NBTTagList list = event.getEntity().getEntityData().getTagList("Cleavers", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				event.getEntityLiving().entityDropItem(new ItemStack(list.getCompoundTagAt(i)), 0f);
			}
			event.getEntityLiving().getEntityData().removeTag("Cleavers");
		}
		if (event.getEntityLiving() instanceof EntityPlayerMP) {
			((WorldServer) event.getEntityLiving().world).addScheduledTask(() -> {
				// EntityPlayerMP player =
				// ((WorldServer)event.getEntityLiving().world).getMinecraftServer().getPlayerList().recreatePlayerEntity((EntityPlayerMP)
				// event.getEntityLiving(), event.getEntityLiving().dimension, false);
				// player.setGameType(GameType.SPECTATOR);
			});

		}
	}

	@SubscribeEvent
	public void generateStructures(PopulateChunkEvent.Pre event) {
		if (event.getWorld().provider.getDimension() == 0 && !TF2ConfigVars.disableGeneration
				&& event.getWorld().getWorldInfo().isMapFeaturesEnabled()) {
			event.getWorld().getCapability(TF2weapons.WORLD_CAP, null).mannCoGenerator.generate(event.getWorld(),
					event.getChunkX(), event.getChunkZ(), new ChunkPrimer());
			event.getWorld().getCapability(TF2weapons.WORLD_CAP, null).mannCoGenerator.generateStructure(
					event.getWorld(), event.getRand(), new ChunkPos(event.getChunkX(), event.getChunkZ()));
			event.getWorld().getCapability(TF2weapons.WORLD_CAP, null).tf2BaseGenerator.generate(event.getWorld(),
					event.getChunkX(), event.getChunkZ(), new ChunkPrimer());
			event.getWorld().getCapability(TF2weapons.WORLD_CAP, null).tf2BaseGenerator.generateStructure(
					event.getWorld(), event.getRand(), new ChunkPos(event.getChunkX(), event.getChunkZ()));
		}
	}

	@SubscribeEvent
	public void generateOres(OreGenEvent.Post event) {
		if (event.getWorld().provider.getDimension() == 0) {
			if (TF2weapons.generateCopper) {
				generateOre(TF2weapons.blockCopperOre.getDefaultState(), 7, 9, 32, 80, event.getWorld(),
						event.getRand(), event.getPos());
				generateOre(TF2weapons.blockCopperOre.getDefaultState(), 7, 1, 0, 32, event.getWorld(), event.getRand(),
						event.getPos());
			}
			if (TF2weapons.generateLead) {
				generateOre(TF2weapons.blockLeadOre.getDefaultState(), 5, 7, 24, 74, event.getWorld(), event.getRand(),
						event.getPos());
				generateOre(TF2weapons.blockLeadOre.getDefaultState(), 5, 1, 0, 24, event.getWorld(), event.getRand(),
						event.getPos());
			}
			if (TF2weapons.generateAustralium) {
				generateOre(TF2weapons.blockAustraliumOre.getDefaultState(), 3, 4, 0, 24, event.getWorld(),
						event.getRand(), event.getPos());
			}
		}
	}

	public void generateOre(IBlockState state, int size, int count, int minY, int maxY, World world, Random random,
			BlockPos chunkPos) {
		for (int i = 0; i < count; i++) {
			BlockPos pos = chunkPos.add(random.nextInt(16), minY + random.nextInt(maxY - minY), random.nextInt(16));
			new WorldGenMinable(state, size).generate(world, random, pos);
		}
	}

	@SubscribeEvent
	public void pickAmmo(EntityItemPickupEvent event) {
		ItemStack stack = event.getItem().getItem();

		if (event.getItem().getEntityData().getBoolean("Fake")) {
			event.setResult(Result.ALLOW);
			return;
		}

		if ((stack.getItem() instanceof ItemFoodThrowable) && stack.hasTagCompound()
				&& stack.getTagCompound().getBoolean("IsHealing")) {
			if (event.getEntityPlayer().getHealth() < event.getEntityPlayer().getMaxHealth()) {
				event.getEntityPlayer().heal(event.getEntityPlayer().getMaxHealth()
						* ((ItemFood) TF2weapons.itemSandvich).getHealAmount(stack) / 28f);
				stack.shrink(1);
				if (stack.isEmpty())
					event.setResult(Result.ALLOW);
			} else {
				event.getEntityPlayer().getCooldownTracker().setCooldown(stack.getItem(), 0);
			}
			stack.setTagCompound(null);
			return;
		}

		if (WeaponsCapability.get(event.getEntity()).isInvisible()) {
			event.setResult(Result.DENY);
			event.setCanceled(true);
			return;
		}

		if (stack.getItem() instanceof ItemAmmoPackage) {
			ItemStack weapon = event.getEntityPlayer().getHeldItemMainhand();
			int ammoType = stack.getMetadata() % 16;
			if (!weapon.isEmpty() && weapon.getItem() instanceof ItemUsable
					&& ((ItemUsable) weapon.getItem()).getAmmoType(weapon) != 0
					&& ((ItemUsable) weapon.getItem()).getAmmoType(weapon) < ItemAmmo.AMMO_TYPES.length)
				ammoType = ((ItemUsable) weapon.getItem()).getAmmoType(weapon);
			else {
				for (ItemStack invstack : event.getEntityPlayer().inventory.mainInventory) {
					if (!invstack.isEmpty() && invstack.getItem() instanceof ItemUsable
							&& ((ItemUsable) invstack.getItem()).getAmmoType(invstack) != 0
							&& ((ItemUsable) invstack.getItem()).getAmmoType(invstack) < ItemAmmo.AMMO_TYPES.length) {
						ammoType = ((ItemUsable) invstack.getItem()).getAmmoType(invstack);
						break;
					}
				}
			}
			stack = ItemAmmoPackage.convertPackage(stack, event.getEntityPlayer(), ammoType);
			event.getItem().setItem(stack);
			event.setResult(Result.DENY);
			event.setCanceled(true);
			return;
		}

		if (stack.getItem() instanceof ItemMoney) {
			ItemMoney.collect(stack, event.getEntityPlayer());
			event.setResult(Result.ALLOW);
		}
		/*
		 * if (!(stack.getItem() instanceof ItemCrate) && stack.hasTagCompound() &&
		 * stack.getTagCompound().getBoolean("DropFrom")) {
		 * event.getEntityPlayer().addStat(TF2Achievements.SPOILS_WAR); }
		 */
		ItemStack stackam = TF2Util.pickAmmo(stack, event.getEntityPlayer(), false);
		stack.setCount(stackam.getCount());

		if (stack.isEmpty()) {
			event.setResult(Result.ALLOW);
			return;
		}
		event.setResult(Result.DEFAULT);
	}

	public static void onStrangeUpdate(ItemStack stack, EntityLivingBase player) {
		int points = 0;
		if (stack.getItem() instanceof ItemMedigun) {
			points = stack.getTagCompound().getInteger("Ubercharges");
		} else if (stack.getItem() instanceof ItemCloak) {
			points = stack.getTagCompound().getInteger("CloakTicks") / 400;
		} else {
			points = stack.getTagCompound().getInteger("Kills");
			points += stack.getTagCompound().getInteger("PlayerKills") * 5;
		}
		int calculatedLevel = 0;

		if (points >= STRANGE_KILLS[STRANGE_KILLS.length - 1]) {
			calculatedLevel = STRANGE_KILLS.length - 1;
		} else {
			for (int i = 1; i < STRANGE_KILLS.length; i++)
				if (points < STRANGE_KILLS[i]) {
					calculatedLevel = i - 1;
					break;
				}
		}

		if (calculatedLevel > stack.getTagCompound().getInteger("StrangeLevel")) {
			stack.getTagCompound().setInteger("StrangeLevel", calculatedLevel);
			if (player instanceof EntityPlayer) {
				int rewardLevel = calculatedLevel < 10 ? calculatedLevel : calculatedLevel * 2 - 10;
				TF2Util.setExperiencePoints((EntityPlayer) player,
						TF2Util.getExperiencePoints((EntityPlayer) player) + 35 * rewardLevel);
				ItemHandlerHelper.giveItemToPlayer((EntityPlayer) player,
						new ItemStack(TF2weapons.itemTF2, MathHelper.ceil(rewardLevel / 2f), 6));
				if (calculatedLevel == 20)
					ItemHandlerHelper.giveItemToPlayer((EntityPlayer) player, new ItemStack(TF2weapons.itemTF2, 2, 7));
			}
			/*
			 * final int level = calculatedLevel; if (player instanceof EntityPlayerMP) {
			 * ((EntityPlayerMP) player).addStat(new
			 * Achievement(Integer.toString(player.getRNG().nextInt()), "strangeUp", 0, 0,
			 * stack, null) {
			 *
			 * @Override public ITextComponent getStatName() { return
			 * super.getStatName().appendText(STRANGE_TITLES[level]); } }); }
			 */
		}
	}

	@SubscribeEvent
	public void addTable(LootTableLoadEvent event) {
		if (!TF2ConfigVars.disableLoot && (event.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON)
				|| event.getName().equals(LootTableList.CHESTS_END_CITY_TREASURE)
				|| event.getName().equals(LootTableList.CHESTS_NETHER_BRIDGE)
				|| event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT)
				|| event.getName().equals(LootTableList.CHESTS_STRONGHOLD_CORRIDOR))) {
			// System.out.println("loaded: "+new
			// ResourceLocation(TF2weapons.MOD_ID,event.getName().getResourcePath()));
			event.getTable()
					.addPool(getLootPool(new ResourceLocation(TF2weapons.MOD_ID, event.getName().getResourcePath())));
		}
	}

	@SubscribeEvent
	public void craftEvent(net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent event) {
		/*
		 * if (event.crafting != null &&
		 * Block.getBlockFromItem(event.crafting.getItem()) == TF2weapons.blockCabinet)
		 * { event.player.addStat(TF2Achievements.WEAPON_CRAFTING); }
		 */
	}

	@SubscribeEvent
	public void itemToss(ItemTossEvent event) {
		ItemStack stack = (event.getEntityItem().getItem());
		if (!event.getPlayer().world.isRemote && stack.getItem() instanceof ItemUsable
				&& stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).active > 0) {

			// event.getPlayer().getCapability(TF2weapons.WEAPONS_CAP, null).state = 0;
			WeaponsCapability.get(event.getPlayer()).setInactiveHand(EnumHand.MAIN_HAND, stack);
		}

		if (stack.getItem() instanceof ItemFoodThrowable
				&& !event.getPlayer().getCooldownTracker().hasCooldown(stack.getItem())) {
			event.getPlayer().getCooldownTracker().setCooldown(stack.getItem(),
					((ItemFoodThrowable) stack.getItem()).waitTime / (TF2ConfigVars.fastItemCooldown ? 2 : 1));
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setBoolean("IsHealing", true);
		}

		if (stack.getItem() instanceof ItemFromData && stack.hasTagCompound()
				&& (stack.getTagCompound().getBoolean("Bought") || stack.getTagCompound().getBoolean("Valve"))) {
			event.getEntity().setEntityInvulnerable(true);
			event.getEntityItem().setThrower(event.getPlayer().getName());
		}
	}

	@SubscribeEvent
	public void itemExpire(ItemExpireEvent event) {
		ItemStack stack = event.getEntityItem().getItem();
		String thrower = event.getEntityItem().getThrower();
		if (stack.getItem() instanceof ItemFromData && thrower != null) {
			// System.out.println("put");
			HashMap<String, MerchantRecipeList> map = event.getEntityItem().world.getCapability(TF2weapons.WORLD_CAP,
					null).lostItems;
			if (!map.containsKey(thrower))
				map.put(thrower, new MerchantRecipeList());
			int cost = ItemFromData.getData(stack).getInt(PropertyType.COST) / 3;
			ItemStack ingot = new ItemStack(TF2weapons.itemTF2, cost / 9, 2);
			ItemStack nugget = new ItemStack(TF2weapons.itemTF2, cost % 9, 6);
			map.get(thrower).add(new MerchantRecipe(ingot.getCount() > 0 ? ingot : nugget,
					nugget.getCount() > 0 && ingot.getCount() > 0 ? nugget : ItemStack.EMPTY, stack, 0, 1));

			// ItemHandlerHelper.insertItemStacked(map.get(thrower), stack, false);
		}
	}

	@SubscribeEvent
	public void startTrack(PlayerEvent.StartTracking event) {
		if (event.getTarget().hasCapability(TF2weapons.WEAPONS_CAP, null)) {
			TF2weapons.network.sendTo(new TF2Message.ActionMessage(WeaponsCapability.get(event.getTarget()).state,
					(EntityLivingBase) event.getTarget()), (EntityPlayerMP) event.getEntityPlayer());
			TF2weapons.network.sendTo(new TF2Message.CapabilityMessage(event.getTarget(), true),
					(EntityPlayerMP) event.getEntityPlayer());
		}
		if (event.getTarget().hasCapability(TF2weapons.PLAYER_CAP, null)) {
			if (event.getTarget() == event.getEntityPlayer())
				TF2weapons.network.sendTo(new TF2Message.PlayerCapabilityMessage(event.getTarget(), true, false),
						(EntityPlayerMP) event.getEntityPlayer());
			TF2weapons.network.sendTo(new TF2Message.PlayerCapabilityMessage(event.getTarget(), true, true),
					(EntityPlayerMP) event.getEntityPlayer());
		}

		if (event.getTarget().hasCapability(TF2weapons.INVENTORY_CAP, null) && !event.getTarget().world.isRemote) {
			// System.out.println("Tracking");
			InventoryWearables inv = event.getTarget().getCapability(TF2weapons.INVENTORY_CAP, null);
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(event.getTarget(), 0, inv.getStackInSlot(0)),
					(EntityPlayerMP) event.getEntityPlayer());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(event.getTarget(), 1, inv.getStackInSlot(1)),
					(EntityPlayerMP) event.getEntityPlayer());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(event.getTarget(), 2, inv.getStackInSlot(2)),
					(EntityPlayerMP) event.getEntityPlayer());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage(event.getTarget(), 3, inv.getStackInSlot(3)),
					(EntityPlayerMP) event.getEntityPlayer());
		}
		if (event.getTarget() instanceof EntityTF2Character) {
			ItemStackHandler loadout = ((EntityTF2Character) event.getTarget()).loadout;
			for (int i = 0; i < loadout.getSlots(); i++) {
				// if (loadout.getStackInSlot(i).getItem() instanceof ItemFromData &&
				// loadout.getStackInSlot(i).getItem())
				TF2weapons.network.sendTo(
						new TF2Message.WearableChangeMessage(event.getTarget(), 20 + i, loadout.getStackInSlot(i)),
						(EntityPlayerMP) event.getEntityPlayer());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void dropFakeItems(PlayerDropsEvent event) {
		if (WeaponsCapability.get(event.getEntity()).isFeign())
			for (EntityItem item : event.getDrops()) {
				item.getEntityData().setBoolean("Fake", true);
				item.setAgeToCreativeDespawnTime();
			}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void dropItems(PlayerDropsEvent event) {
		InventoryWearables inv = event.getEntityLiving().getCapability(TF2weapons.INVENTORY_CAP, null);
		for (int i = 2; i < 4; i++)
			if (!inv.getStackInSlot(i).isEmpty()) {
				event.getDrops().add(event.getEntityPlayer().dropItem(inv.getStackInSlot(i), true, false));
			}
		for (int i = 5; i < 8; i++)
			if (!inv.getStackInSlot(i).isEmpty() && inv.getStackInSlot(i).getCount() >= 4)
				event.getDrops().add(event.getEntityPlayer().dropItem(
						inv.getStackInSlot(i).splitStack(MathHelper.floor(inv.getStackInSlot(i).getCount() * 0.25f)),
						true, false));
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void register(RegistryEvent.Register<SoundEvent> event) {
		// System.out.println("SoundEvents:"+TF2Sounds.SOUND_EVENTS.size());
		TF2Sounds.registerSounds();
		// System.out.println("Registering sounds: "+ MapList.nameToData.values());
		for (WeaponData weapon : MapList.nameToData.values())
			for (PropertyType<?> propType : weapon.properties.keySet())
				if (propType.name.contains("sound")) {
					ResourceLocation soundLocation = new ResourceLocation(
							weapon.getString((PropertyType<String>) propType));
					if (!"".equals(soundLocation.getResourcePath())) {
						TF2Sounds.register(soundLocation);
						if (propType == PropertyType.FIRE_SOUND || propType == PropertyType.FIRE_LOOP_SOUND
								|| propType == PropertyType.CHARGED_FIRE_SOUND)
							TF2Sounds.register(
									new ResourceLocation(weapon.getString((PropertyType<String>) propType) + ".crit"));
					}
				}
		for (SoundEvent sevent : TF2Sounds.SOUND_EVENTS.values()) {
			event.getRegistry().register(sevent);
		}
	}

	@SubscribeEvent
	public void looting(LootingLevelEvent event) {
		if (event.getDamageSource() instanceof TF2DamageSource) {
			event.setLootingLevel(event.getLootingLevel()
					+ (int) TF2Attribute.getModifier("Looting", ((TF2DamageSource) event.getDamageSource()).getWeapon(),
							0, (EntityLivingBase) event.getDamageSource().getTrueSource()));
		}
	}

	@SubscribeEvent
	public void containerOpen(PlayerContainerEvent.Open event) {
		if (!event.getEntityPlayer().world.isRemote && event.getContainer() instanceof ContainerMercenary) {
			((ContainerMerchant) event.getContainer()).getMerchantInventory();
			((ContainerMercenary) event.getContainer()).mercenary.getDisplayName();
			MerchantRecipeList merchantrecipelist = ((ContainerMercenary) event.getContainer()).mercenary
					.getRecipes(event.getEntityPlayer());

			if (merchantrecipelist != null) {
				PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
				packetbuffer.writeInt(event.getContainer().windowId);
				merchantrecipelist.writeToBuf(packetbuffer);
				((EntityPlayerMP) event.getEntityPlayer()).connection
						.sendPacket(new SPacketCustomPayload("MC|TrList", packetbuffer));
			}
		}
	}

	@SubscribeEvent
	public void equipItem(LivingEquipmentChangeEvent event) {
		if (event.getEntityLiving().hasCapability(TF2weapons.WEAPONS_CAP, null)
				&& WeaponsCapability.get(event.getEntityLiving()).getUsedToken() >= 0
				&& event.getTo().hasCapability(TF2weapons.WEAPONS_DATA_CAP, null)) {
			event.getTo().getCapability(TF2weapons.WEAPONS_DATA_CAP, null).usedClass = WeaponsCapability
					.get(event.getEntityLiving()).getUsedToken();
		}
	}

	@SubscribeEvent
	public void drops(LivingDropsEvent event) {
		if (event.getSource().getTrueSource() instanceof EntityPlayer
				&& TF2Util.isEnemy((EntityLivingBase) event.getSource().getTrueSource(), event.getEntityLiving())
				&& !(event.getEntityLiving() instanceof IEntityTF2) && event.getSource() instanceof TF2DamageSource
				&& event.getEntityLiving().getRNG()
						.nextFloat() < (TF2ConfigVars.dropAmmo * Math.sqrt(event.getEntityLiving().getMaxHealth() / 20f)
								* (1 + event.getEntityLiving().getTotalArmorValue() / 13f))) {
			event.getDrops()
					.add(event.getEntityLiving().entityDropItem(new ItemStack(TF2weapons.itemAmmoPackage, 1,
							1 + event.getEntityLiving().getRNG().nextInt(ItemAmmoPackage.AMMO_PACKAGE_MIN.length - 1)),
							0));
		}
	}

	@SubscribeEvent
	public void arrowHeadshot(ProjectileImpactEvent.Arrow event) {
		if (event.getRayTraceResult().entityHit instanceof EntityLivingBase
				&& event.getArrow().getEntityData().getBoolean("TF2Arrow")) {
			RayTraceResult trace = Iterables.getFirst(TF2Util.pierce(event.getEntity().world,
					event.getArrow().shootingEntity, event.getArrow().posX, event.getArrow().posY,
					event.getArrow().posZ, event.getArrow().posX + event.getArrow().motionX,
					event.getArrow().posY + event.getArrow().motionY, event.getArrow().posZ + event.getArrow().motionZ,
					true, 0.08f, false), null);
			float armor = (float) ((EntityLivingBase) event.getRayTraceResult().entityHit)
					.getEntityAttribute(SharedMonsterAttributes.ARMOR).getAttributeValue();
			if (trace.hitInfo instanceof Boolean && ((Boolean) trace.hitInfo)) {
				event.getArrow().getEntityData().setBoolean("CritHeadshot", true);
				if (((EntityLivingBase) event.getRayTraceResult().entityHit)
						.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
					event.getArrow().setDamage(event.getArrow().getDamage() * 1.25f);
				event.getArrow().setDamage(event.getArrow().getDamage() * (1 - armor * 0.0115));
			}
			event.getArrow().setDamage(event.getArrow().getDamage() * (1 - armor * 0.0152));
		}

	}

	public static LootPool getLootPool(ResourceLocation res) {
		return new LootPool(new LootEntry[] { new LootEntryTable(res, 1, 0, new LootCondition[0], "combined") },
				new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0), "combined");
	}

	public static List<BannerPattern> getPatterns(TileEntityBanner banner) {
		List<BannerPattern> patterns = new ArrayList<>();
		patterns.add(BannerPattern.BASE);
		NBTTagList patternsnbt = banner.writeToNBT(new NBTTagCompound()).getTagList("Patterns", 10);
		for (int i = 0; i < patternsnbt.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = patternsnbt.getCompoundTagAt(i);
			for (BannerPattern pattern : BannerPattern.values()) {
				if (pattern.getHashname().equals(nbttagcompound.getString("Pattern"))) {
					patterns.add(pattern);
					break;
				}
			}
		}
		return patterns;
	}

	public static class DestroyBlockEntry {
		public BlockPos pos;
		public float curDamage;
		public World world;

		public DestroyBlockEntry(BlockPos pos, World world) {
			this.world = world;
			this.pos = pos;
		}
	}

	public static class InboundDamage {
		public DamageSource source;
		public float damage;
		public int critical;
		public EntityLivingBase living;
		public ItemStack stack;

		public InboundDamage(DamageSource source, float damage, int critical, EntityLivingBase living,
				ItemStack stack) {
			this.source = source;
			this.damage = damage;
			this.critical = critical;
			this.living = living;
			this.stack = stack;
		}
	}

	public static class TF2WorldStorage implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

		public int eventFlag;

		public World world;
		public HashMap<Entity, InboundDamage> damage = new HashMap<>();
		public ArrayList<BlockPos> banners = new ArrayList<>();
		public HashMap<String, MerchantRecipeList> lostItems = new HashMap<>();
		private HashMap<UUID, PlayerPersistStorage> playerStorage = new HashMap<>();
		public Map<UUID, InvasionEvent> invasions = new HashMap<>();
		public ArrayList<DestroyBlockEntry> destroyProgress = new ArrayList<>();
		public HashMap<String, GameArena> gameArenas = new HashMap<>();
		public MannCoBuilding.MapGen mannCoGenerator = new MannCoBuilding.MapGen();
		public MapGen tf2BaseGenerator = new ScatteredFeatureTF2Base.MapGen(null);
		public boolean silent;

		/*
		 * @Override public void readFromNBT(NBTTagCompound nbt) {
		 *
		 * System.out.println("Load world nbt"); }
		 *
		 * @Override public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		 *
		 * System.out.println("SAVE world nbt"); return compound; }
		 */
		public TF2WorldStorage(World world) {
			this.world = world;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("Event", eventFlag);
			NBTTagCompound items = new NBTTagCompound();
			nbt.setTag("Items", items);
			NBTTagList bannersS = new NBTTagList();
			for (BlockPos pos : banners) {

				NBTTagList coords = new NBTTagList();
				coords.appendTag(new NBTTagInt(pos.getX()));
				coords.appendTag(new NBTTagInt(pos.getY()));
				coords.appendTag(new NBTTagInt(pos.getZ()));
				bannersS.appendTag(coords);
			}
			nbt.setTag("Banners", bannersS);
			for (Entry<String, MerchantRecipeList> entry : lostItems.entrySet()) {

				items.setTag(entry.getKey(), entry.getValue().getRecipiesAsTags());
			}
			NBTTagCompound tagPlSt = new NBTTagCompound();
			nbt.setTag("PlayerStorage", tagPlSt);

			for (Entry<UUID, PlayerPersistStorage> entry : playerStorage.entrySet()) {
				if (entry.getValue().save)
					tagPlSt.setTag(entry.getKey().toString(), entry.getValue().serializeNBT());
			}

			NBTTagCompound invTag = new NBTTagCompound();
			for (Entry<UUID, InvasionEvent> entry : invasions.entrySet()) {
				invTag.setTag(entry.getKey().toString(), entry.getValue().serializeNBT());
			}
			nbt.setTag("Invasions", invTag);
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			eventFlag = nbt.getInteger("Event");
			NBTTagCompound items = nbt.getCompoundTag("Items");
			NBTTagCompound mercslost = nbt.getCompoundTag("MercsLost");
			NBTTagCompound tagPlSt = nbt.getCompoundTag("PlayerStorage");
			for (String key : items.getKeySet()) {
				MerchantRecipeList handler = new MerchantRecipeList();
				handler.readRecipiesFromTags(items.getCompoundTag(key));
				lostItems.put(key, handler);
			}
			for (String key : tagPlSt.getKeySet()) {
				PlayerPersistStorage storage = new PlayerPersistStorage(UUID.fromString(key));
				storage.deserializeNBT(tagPlSt.getCompoundTag(key));
				this.playerStorage.put(UUID.fromString(key), storage);
			}
			for (String key : mercslost.getKeySet()) {
				if (this.playerStorage.containsKey(UUID.fromString(key))) {
					NBTTagList list = nbt.getTagList(key, 11);
					for (int i = 0; i < list.tagCount(); i++) {
						int[] pos = list.getIntArrayAt(i);
						this.playerStorage.get(UUID.fromString(key)).lostMercPos
								.add(new BlockPos(pos[0], pos[1], pos[2]));
					}
				}

			}
			NBTTagList bannersS = nbt.getTagList("Banners", 9);
			for (int i = 0; i < bannersS.tagCount(); i++) {
				NBTTagList coords = (NBTTagList) bannersS.get(i);
				banners.add(new BlockPos(coords.getIntAt(0), coords.getIntAt(1), coords.getIntAt(2)));
			}
			NBTTagCompound invTag = nbt.getCompoundTag("Invasions");
			for (String key : invTag.getKeySet()) {
				invasions.put(UUID.fromString(key), new InvasionEvent(world, invTag.getCompoundTag(key)));
			}
			NBTTagCompound gameTag = nbt.getCompoundTag("Invasions");
			for (String key : gameTag.getKeySet()) {
				gameArenas.put(key, new GameArena(world, gameTag.getCompoundTag(key)));
			}
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return TF2weapons.WORLD_CAP != null && capability == TF2weapons.WORLD_CAP;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (TF2weapons.WORLD_CAP != null && capability == TF2weapons.WORLD_CAP)
				return TF2weapons.WORLD_CAP.cast(this);
			return null;
		}

		public PlayerPersistStorage getPlayerStorage(EntityPlayer player) {
			return this.getPlayerStorage(player.getUniqueID());
		}

		public PlayerPersistStorage getPlayerStorage(UUID player) {
			if (!this.playerStorage.containsKey(player))
				playerStorage.put(player, new PlayerPersistStorage(player));
			return this.playerStorage.get(player);
		}

		public boolean startInvasion(EntityPlayer player, int difficulty, boolean force) {
			for (Entry<UUID, InvasionEvent> entry : invasions.entrySet()) {
				if (entry.getKey().equals(player.getUniqueID()) || entry.getValue().isInRange(player.getPosition())) {
					if (force)
						entry.getValue().finish();
					else
						return false;
				}
			}

			InvasionEvent event = new InvasionEvent(world, player.getPosition(), difficulty);
			this.invasions.put(player.getUniqueID(), event);
			return true;
		}
	}

	public static class TF2ContainerListener implements IContainerListener {

		public EntityPlayerMP player;

		public TF2ContainerListener(EntityPlayerMP player) {
			this.player = player;
		}

		@Override
		public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
			if (player.getHeldItemMainhand().getItem() instanceof ItemWeapon)
				TF2weapons.network.sendTo(new TF2Message.UseMessage(
						((ItemWeapon) player.getHeldItemMainhand().getItem()).getClip(player.getHeldItemMainhand()),
						false, ((ItemUsable) player.getHeldItemMainhand().getItem()).getAmmoAmount(player,
								player.getHeldItemMainhand()),
						EnumHand.MAIN_HAND), player);
		}

		@Override
		public void sendAllWindowProperties(Container containerIn, IInventory inventory) {}

		@Override
		public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {}

		@Override
		public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {}

	}
}
