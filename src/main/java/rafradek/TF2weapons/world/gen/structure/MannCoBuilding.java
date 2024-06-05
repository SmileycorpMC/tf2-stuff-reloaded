package rafradek.TF2weapons.world.gen.structure;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.*;
import net.minecraft.world.gen.structure.template.TemplateManager;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.block.BlockUpgradeStation;
import rafradek.TF2weapons.entity.mercenary.EntitySaxtonHale;

import java.util.Random;

public class MannCoBuilding extends StructureComponent {

	private boolean haleSpawned;
	protected int averageGroundLvl = -1;

	public MannCoBuilding() {

	}

	public MannCoBuilding(int p5, Random random, StructureBoundingBox structureboundingbox, EnumFacing facing) {
		super(p5);
		this.setCoordBaseMode(facing);
		this.boundingBox = structureboundingbox;
	}

	@Override
	protected void writeStructureToNBT(NBTTagCompound tagCompound) {
		tagCompound.setInteger("HPos", this.averageGroundLvl);
		tagCompound.setBoolean("HS", this.haleSpawned);

	}

	/**
	 * (abstract) Helper method to read subclass data from NBT
	 */
	@Override
	protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
		this.averageGroundLvl = tagCompound.getInteger("HPos");
		this.haleSpawned = tagCompound.getBoolean("HS");
	}

	/**
	 * second Part of Structure generating, this for example places Spiderwebs, Mob
	 * Spawners, it closes Mineshafts at the end, it adds Fences...
	 */
	@Override
	public boolean addComponentParts(World world, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
		if (this.averageGroundLvl < 0) {
			this.averageGroundLvl = this.getAverageGroundLevel(world, structureBoundingBoxIn);

			if (this.averageGroundLvl < 0)
				return true;

			this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 15, 0);
		}

		IBlockState stone = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT,
				BlockStone.EnumType.ANDESITE_SMOOTH);
		IBlockState iblockstate1 = this.getBiomeSpecificBlockState(
				Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
		IBlockState iblockstate2 = this.getBiomeSpecificBlockState(
				Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
		IBlockState wood = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
		IBlockState upgradeStation = this.getBiomeSpecificBlockState(
				TF2weapons.blockUpgradeStation.getDefaultState().withProperty(BlockUpgradeStation.HOLDER, false));
		IBlockState upgradeStationHolder = this.getBiomeSpecificBlockState(
				TF2weapons.blockUpgradeStation.getDefaultState().withProperty(BlockUpgradeStation.HOLDER, true));
		IBlockState iblockstate5 = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
		IBlockState iblockstate6 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
		this.fillWithBlocks(world, structureBoundingBoxIn, 0, 1, 0, 13, 16, 10, Blocks.AIR.getDefaultState(),
				Blocks.AIR.getDefaultState(), false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 0, 0, 2, 13, 15, 10, stone, Blocks.AIR.getDefaultState(),
				false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 1, 0, 3, 12, 0, 9, wood, wood, false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 1, 4, 3, 12, 4, 9, stone, stone, false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 0, 2, 3, 0, 3, 9, Blocks.GLASS_PANE.getDefaultState(),
				Blocks.GLASS_PANE.getDefaultState(), false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 13, 2, 3, 13, 3, 9, Blocks.GLASS_PANE.getDefaultState(),
				Blocks.GLASS_PANE.getDefaultState(), false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 1, 2, 2, 12, 3, 2, Blocks.GLASS_PANE.getDefaultState(),
				Blocks.GLASS_PANE.getDefaultState(), false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 1, 2, 10, 12, 3, 10, Blocks.GLASS_PANE.getDefaultState(),
				Blocks.GLASS_PANE.getDefaultState(), false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 5, 0, 2, 8, 3, 2, wood, wood, false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 6, 1, 2, 7, 2, 2, Blocks.AIR.getDefaultState(),
				Blocks.AIR.getDefaultState(), false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 6, 0, 1, 7, 0, 1, iblockstate2, iblockstate2, false);
		this.setBlockState(world, Blocks.GLOWSTONE.getDefaultState(), 7, 4, 6, structureBoundingBoxIn);
		this.setBlockState(world, Blocks.GLOWSTONE.getDefaultState(), 4, 4, 6, structureBoundingBoxIn);
		this.setBlockState(world, Blocks.GLOWSTONE.getDefaultState(), 10, 4, 6, structureBoundingBoxIn);
		this.fillWithBlocks(world, structureBoundingBoxIn, 1, 1, 5, 1, 2, 7,
				upgradeStation.withProperty(BlockUpgradeStation.FACING, EnumFacing.EAST),
				upgradeStation.withProperty(BlockUpgradeStation.FACING, EnumFacing.WEST), false);
		this.fillWithBlocks(world, structureBoundingBoxIn, 12, 1, 5, 12, 2, 7,
				upgradeStation.withProperty(BlockUpgradeStation.FACING, EnumFacing.WEST),
				upgradeStation.withProperty(BlockUpgradeStation.FACING, EnumFacing.EAST), false);
		this.setBlockState(world, upgradeStationHolder.withProperty(BlockUpgradeStation.FACING, EnumFacing.EAST), 1,
				1, 6, structureBoundingBoxIn);
		this.setBlockState(world, upgradeStationHolder.withProperty(BlockUpgradeStation.FACING, EnumFacing.WEST), 12,
				1, 6, structureBoundingBoxIn);
		for (int k = 0; k < 10; ++k)
			for (int j = 0; j < 13; ++j) {
				this.clearCurrentPositionBlocksUpwards(world, j, 17, k, structureBoundingBoxIn);
				this.replaceAirAndLiquidDownwards(world, stone, j, -1, k, structureBoundingBoxIn);
			}

		if (!this.haleSpawned) {
			EntitySaxtonHale shopkeeper = new EntitySaxtonHale(world);
			shopkeeper.setLocationAndAngles(this.getXWithOffset(7, 5), this.getYWithOffset(1),
					this.getZWithOffset(7, 5), 0.0F, 0.0F);
			shopkeeper.enablePersistence();
			this.haleSpawned = true;
			world.spawnEntity(shopkeeper);
		}
		return true;
	}

	protected IBlockState getBiomeSpecificBlockState(IBlockState blockstateIn) {

		return blockstateIn;
	}

	protected int getAverageGroundLevel(World world, StructureBoundingBox structurebb) {
		int i = 0;
		int j = 0;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int k = this.boundingBox.minZ; k <= this.boundingBox.maxZ; ++k) {
			for (int l = this.boundingBox.minX; l <= this.boundingBox.maxX; ++l) {
				blockpos$mutableblockpos.setPos(l, 64, k);

				if (structurebb.isVecInside(blockpos$mutableblockpos)) {
					i += Math.max(world.getTopSolidOrLiquidBlock(blockpos$mutableblockpos).getY(),
							world.provider.getAverageGroundLevel() - 1);
					++j;
				}
			}
		}

		if (j == 0) {
			return -1;
		} else {
			return i / j;
		}
	}

	public static class Start extends StructureStart {
		public Start() {}

		public Start(World world, Random random, int chunkX, int chunkZ) {
			this(world, random, chunkX, chunkZ, world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)));
		}

		public Start(World world, Random random, int chunkX, int chunkZ, Biome biomeIn) {
			super(chunkX, chunkZ);
			EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(random);
			StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(chunkX * 16,
					64, chunkZ * 16, 0, 0, 0, 14, 16, 11, facing);
			this.components.add(new MannCoBuilding(0, random, structureboundingbox, facing));
			/*
			 * if (biomeIn != Biomes.JUNGLE && biomeIn != Biomes.JUNGLE_HILLS) { if (biomeIn
			 * == Biomes.SWAMPLAND) { ComponentScatteredFeaturePieces.SwampHut
			 * componentscatteredfeaturepieces$swamphut = new
			 * ComponentScatteredFeaturePieces.SwampHut(random, chunkX * 16, chunkZ * 16);
			 * this.components.add(componentscatteredfeaturepieces$swamphut); } else if
			 * (biomeIn != Biomes.DESERT && biomeIn != Biomes.DESERT_HILLS) { if (biomeIn ==
			 * Biomes.ICE_PLAINS || biomeIn == Biomes.COLD_TAIGA) {
			 * ComponentScatteredFeaturePieces.Igloo componentscatteredfeaturepieces$igloo =
			 * new ComponentScatteredFeaturePieces.Igloo(random, chunkX * 16, chunkZ * 16);
			 * this.components.add(componentscatteredfeaturepieces$igloo); } } else {
			 * ComponentScatteredFeaturePieces.DesertPyramid
			 * componentscatteredfeaturepieces$desertpyramid = new
			 * ComponentScatteredFeaturePieces.DesertPyramid(random, chunkX * 16, chunkZ *
			 * 16); this.components.add(componentscatteredfeaturepieces$desertpyramid); } }
			 * else { ComponentScatteredFeaturePieces.JunglePyramid
			 * componentscatteredfeaturepieces$junglepyramid = new
			 * ComponentScatteredFeaturePieces.JunglePyramid(random, chunkX * 16, chunkZ *
			 * 16); this.components.add(componentscatteredfeaturepieces$junglepyramid); }
			 */

			this.updateBoundingBox();
		}
	}

	public static class MapGen extends MapGenStructure {
		private int distance;

		public MapGen() {
			this.distance = TF2ConfigVars.mannCoChance;
		}

		@Override
		public String getStructureName() {
			return "MannCoBuild";
		}

		@Override
		protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
			int i = chunkX;
			int j = chunkZ;

			if (chunkX < 0) {
				chunkX -= this.distance - 1;
			}

			if (chunkZ < 0) {
				chunkZ -= this.distance - 1;
			}

			int k = chunkX / this.distance;
			int l = chunkZ / this.distance;
			Random random = this.world.setRandomSeed(k, l, 4234124);
			k = k * this.distance;
			l = l * this.distance;
			k = k + random.nextInt(Math.max(1, this.distance - 8));
			l = l + random.nextInt(Math.max(1, this.distance - 8));

			if (i == k && j == l) {
				boolean flag = this.world.getBiomeProvider().areBiomesViable(i * 16 + 8, j * 16 + 8, 0,
						MapGenVillage.VILLAGE_SPAWN_BIOMES);

				if (flag) {
					return true;
				}
			}

			return false;
		}

		@Override
		public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored) {
			this.world = world;
			return findNearestStructurePosBySpacing(world, this, pos, this.distance, 8, 10387312, false, 100,
					findUnexplored);
		}

		@Override
		protected StructureStart getStructureStart(int chunkX, int chunkZ) {
			return new Start(this.world, this.rand, chunkX, chunkZ);
		}
	}
}
