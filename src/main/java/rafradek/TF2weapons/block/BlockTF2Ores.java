package rafradek.TF2weapons.block;

import net.minecraft.block.BlockOre;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTF2Ores extends BlockOre {

	public enum EnumOreType implements IStringSerializable {

		COPPER("copper"), LEAD("lead"), AUSTRALIUM("australium");

		private String name;

		EnumOreType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	public static final PropertyEnum<EnumOreType> TYPE = PropertyEnum.<EnumOreType>create("oreType", EnumOreType.class);

	public BlockTF2Ores() {
		super();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		for (EnumOreType enumoretype : EnumOreType.values())
			list.add(new ItemStack(this, 1, enumoretype.ordinal()));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE, EnumOreType.values()[meta]);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { TYPE });
	}
}
