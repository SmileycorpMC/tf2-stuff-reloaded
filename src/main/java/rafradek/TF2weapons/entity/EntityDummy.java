package rafradek.TF2weapons.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.common.WeaponsCapability;

import javax.annotation.Nullable;

public class EntityDummy extends EntityLiving {

	public WeaponsCapability cap;

	public EntityDummy(World world) {
		super(world);
		cap = new WeaponsCapability(this);
		this.setSize(0, 0);
		this.setDead();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
			@Nullable net.minecraft.util.EnumFacing facing) {
		if (capability == TF2weapons.WEAPONS_CAP)
			return (T) cap;
		else
			return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability,
			@Nullable net.minecraft.util.EnumFacing facing) {
		return capability == TF2weapons.WEAPONS_CAP || super.hasCapability(capability, facing);
	}
}
