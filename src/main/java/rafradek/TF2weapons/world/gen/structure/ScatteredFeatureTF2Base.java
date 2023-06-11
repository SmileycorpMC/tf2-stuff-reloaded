package rafradek.TF2weapons.world.gen.structure;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.*;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2weapons;

import java.util.Map.Entry;
import java.util.Random;

public class ScatteredFeatureTF2Base extends StructureComponentTemplate {

	public static String[] templateNames = { "red", "blu" };
	private String templateName;
	private Rotation rotation;
	private Mirror mirror;

	private int groundLevel;

	public ScatteredFeatureTF2Base() {}

	public ScatteredFeatureTF2Base(TemplateManager manager, String name, BlockPos pos, Rotation rot, World world) {
		this(manager, name, pos, rot, Mirror.NONE, world);
	}

	public ScatteredFeatureTF2Base(TemplateManager p_i47356_1_, String p_i47356_2_, BlockPos p_i47356_3_,
			Rotation p_i47356_4_, Mirror p_i47356_5_, World world) {
		super(0);
		this.templateName = p_i47356_2_;
		this.templatePosition = p_i47356_3_;
		this.rotation = p_i47356_4_;
		this.mirror = p_i47356_5_;
		this.loadTemplate(p_i47356_1_);
		// this.offsetToAverageGroundLevel(world);
	}

	private void loadTemplate(TemplateManager p_191081_1_) {
		Template template = p_191081_1_.getTemplate((MinecraftServer) null,
				new ResourceLocation(TF2weapons.MOD_ID, "tf2base/" + this.templateName));
		PlacementSettings placementsettings = (new PlacementSettings()).setIgnoreEntities(false)
				.setRotation(this.rotation).setMirror(this.mirror).setBoundingBox(boundingBox);
		this.setup(template, this.templatePosition, placementsettings);
		for (Entry<BlockPos, String> entr : template.getDataBlocks(this.templatePosition, placementsettings)
				.entrySet()) {
			if (entr.getValue().equals("GLevel"))
				this.groundLevel = entr.getKey().getY() - this.boundingBox.minY;
		}

	}

	/**
	 * (abstract) Helper method to write subclass data to NBT
	 */
	@Override
	protected void writeStructureToNBT(NBTTagCompound tagCompound) {
		super.writeStructureToNBT(tagCompound);
		tagCompound.setString("Template", this.templateName);
		tagCompound.setString("Rot", this.placeSettings.getRotation().name());
		tagCompound.setString("Mi", this.placeSettings.getMirror().name());
	}

	/**
	 * (abstract) Helper method to read subclass data from NBT
	 */
	@Override
	protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
		super.readStructureFromNBT(tagCompound, p_143011_2_);
		this.templateName = tagCompound.getString("Template");
		this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
		this.mirror = Mirror.valueOf(tagCompound.getString("Mi"));
		this.loadTemplate(p_143011_2_);
	}

	@Override
	public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
		if (this.templatePosition.getY() == 64) {
			int averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);

			if (averageGroundLvl < 0)
				return true;

			this.offset(0, averageGroundLvl - this.boundingBox.minY - this.groundLevel, 0);
		}

		boolean done = super.addComponentParts(worldIn, randomIn, structureBoundingBoxIn);
		if (done) {
			for (int i = 0; i < this.boundingBox.getXSize(); i++)
				for (int j = 0; j < this.boundingBox.getZSize(); j++)
					this.replaceAirAndLiquidDownwards(worldIn, Blocks.DIRT.getDefaultState(), i, -1 + groundLevel, j,
							structureBoundingBoxIn);
		}
		return done;
	}

	protected int getAverageGroundLevel(World worldIn, StructureBoundingBox structurebb) {
		int i = 0;
		int j = 0;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int k = this.boundingBox.minZ; k <= this.boundingBox.maxZ; ++k) {
			for (int l = this.boundingBox.minX; l <= this.boundingBox.maxX; ++l) {
				blockpos$mutableblockpos.setPos(l, 64, k);

				if (structurebb.isVecInside(blockpos$mutableblockpos)) {
					i += Math.max(worldIn.getTopSolidOrLiquidBlock(blockpos$mutableblockpos).getY(),
							worldIn.provider.getAverageGroundLevel() - 1);
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

	@Override
	protected void handleDataMarker(String function, BlockPos pos, World worldIn, Random rand,
			StructureBoundingBox sbb) {
		if (function.startsWith("ChestB")) {
			BlockPos off = pos.down();
			TileEntity ent = worldIn.getTileEntity(off);
			((TileEntityChest) ent).setLootTable(TF2weapons.lootTF2Base, rand.nextLong());
		}
	}

	public static class Start extends StructureStart {
		public Start() {}

		public Start(World worldIn, Random random, IChunkGenerator provider, int chunkX, int chunkZ) {
			this(worldIn, random, provider, chunkX, chunkZ,
					worldIn.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8)));
		}

		public Start(World worldIn, Random random, IChunkGenerator provider, int chunkX, int chunkZ, Biome biomeIn) {
			super(chunkX, chunkZ);
			Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
			BlockPos pos = new BlockPos(chunkX * 16 + 8, 64, chunkZ * 16 + 8);
			this.components.add(new ScatteredFeatureTF2Base(worldIn.getSaveHandler().getStructureTemplateManager(),
					templateNames[random.nextInt(templateNames.length)], pos, rotation, worldIn));

			this.updateBoundingBox();
		}
	}

	public static class MapGen extends MapGenStructure {
		private int distance;

		private IChunkGenerator provider;

		public MapGen(IChunkGenerator provider) {
			this.provider = provider;
			this.distance = TF2ConfigVars.baseChance;
		}

		@Override
		public String getStructureName() {
			return "TF2BaseScatt";
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
			Random random = this.world.setRandomSeed(k, l, 1456463);
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
		public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
			this.world = worldIn;
			return findNearestStructurePosBySpacing(worldIn, this, pos, this.distance, 8, 10387312, false, 100,
					findUnexplored);
		}

		@Override
		protected StructureStart getStructureStart(int chunkX, int chunkZ) {
			return new Start(this.world, this.rand, this.provider, chunkX, chunkZ);
		}
	}
}
