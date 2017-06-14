package rafradek.TF2weapons.characters;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.characters.ai.EntityAIStickybomb;
import rafradek.TF2weapons.projectiles.EntityStickybomb;
import rafradek.TF2weapons.weapons.ItemChargingTarge;
import rafradek.TF2weapons.weapons.ItemStickyLauncher;
import rafradek.TF2weapons.weapons.ItemWeapon;
import rafradek.TF2weapons.weapons.WeaponsCapability;

public class EntityDemoman extends EntityTF2Character {

	public ItemStack stickyBombLauncher = ItemFromData.getNewStack("stickybomblauncher");

	public int chargeCool=0;
	public EntityDemoman(World par1World) {
		super(par1World);
		this.tasks.addTask(5, new EntityAIStickybomb(this,1,10f));
		if (this.attack != null) {
			this.attack.setRange(20F);
			this.attack.projSpeed = 1.16205f;
			this.attack.gravity = 0.0381f;
			this.attack.setDodge(true, false);
		}
		this.rotation = 10;
		this.ammoLeft = 20;
		this.experienceValue = 15;

		// this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
		// ItemUsable.getNewStack("Minigun"));

	}
	protected void addWeapons() {
		super.addWeapons();
		if(this.rand.nextFloat() > 0.18f && !this.loadout.get(1).isEmpty() && this.loadout.get(1).getItem() instanceof ItemChargingTarge){
			ItemStack sword=ItemFromData.getRandomWeapon(this.rand, Predicates.and(ItemFromData.VISIBLE_WEAPON,new Predicate<WeaponData>(){

				@Override
				public boolean apply(WeaponData input) {
					// TODO Auto-generated method stub
					return !input.getBoolean(PropertyType.STOCK) && input.getInt(PropertyType.SLOT)==2 && input.getString(PropertyType.MOB_TYPE).contains("demoman");
				}
				
			}));
			if(!sword.isEmpty())
				this.loadout.set(2,sword); 
		}
	}
	/*
	 * protected ResourceLocation getLootTable() { return
	 * TF2weapons.lootDemoman; }
	 */
	/*
	 * protected void addWeapons() {
	 * this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
	 * ItemFromData.getNewStack("grenadelauncher")); }
	 */
	public float[] getDropChance() {
		return new float[] { 0.065f, 0.065f, 0.11f };
	}
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18.5D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.295D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance p_180482_1_, IEntityLivingData data) {
		data=super.onInitialSpawn(p_180482_1_, data);
		if(!this.loadout.get(1).isEmpty() && this.loadout.get(1).getItem() instanceof ItemChargingTarge){
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("ShieldHP", 3, 0));
			this.heal(3);
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier("ShieldMove", 0.02, 2));
		}
		return data;
	}
	public int getDefaultSlot(){
		return !this.loadout.get(1).isEmpty() && this.loadout.get(1).getItem() instanceof ItemChargingTarge ? 1:0;
	}
	
	public void onLivingUpdate(){
		super.onLivingUpdate();
		this.chargeCool--;
		if(!this.world.isRemote){
			if(!this.getWepCapability().activeBomb.isEmpty()){
				EntityStickybomb bomb=this.getWepCapability().activeBomb.get(this.rand.nextInt(this.getWepCapability().activeBomb.size()));
				if(this.getAttackTarget() != null && this.getAttackTarget().getDistanceSqToEntity(bomb)<7&&this.getEntitySenses().canSee(this.getAttackTarget())&&this.getAttackTarget().canEntityBeSeen(bomb))
					((ItemWeapon)this.loadout.get(1).getItem()).altFireTick(this.loadout.get(1), this, world);
			}
			if(this.ticksExisted%4==0&&this.loadout.get(1) != null && this.loadout.get(1).getItem() instanceof ItemChargingTarge){
				this.setHeldItem(EnumHand.OFF_HAND, this.loadout.get(1));
				this.switchSlot(2);
				if(this.getAttackTarget() != null && this.getEntitySenses().canSee(this.getAttackTarget())){
					if(this.chargeCool<0){
						this.attack.setDodge(false, false);
						this.chargeCool=300;
						this.playSound(TF2Sounds.WEAPON_SHIELD_CHARGE, 2F, 1F);
						this.addPotionEffect(new PotionEffect(TF2weapons.charging, 40));
					}
				}
			}
		}
	}
	
	protected void onFinishedPotionEffect(PotionEffect effect)
    {
		super.onFinishedPotionEffect(effect);
		if(effect.getPotion()==TF2weapons.charging)
			this.attack.setDodge(true, false);
    }
	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_DEMOMAN_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound() {
		return TF2Sounds.MOB_DEMOMAN_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_DEMOMAN_DEATH;
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.rand.nextFloat() < 0.075f + p_70628_2_ * 0.0375f)
			this.entityDropItem(ItemFromData.getNewStack("grenadelauncher"), 0);
		if (this.rand.nextFloat() < 0.06f + p_70628_2_ * 0.03f)
			this.entityDropItem(ItemFromData.getNewStack("stickybomblauncher"), 0);
	}
	
	public float getAttributeModifier(String attribute) {
		if(attribute.equals("Spread") && this.getHeldItemMainhand() != null && this.getHeldItemMainhand().getItem() instanceof ItemStickyLauncher)
			return 3f;
		return super.getAttributeModifier(attribute);
	}

	/*
	 * @Override public float getAttributeModifier(String attribute) {
	 * if(attribute.equals("Minigun Spinup")){ return
	 * super.getAttributeModifier(attribute)*1.5f; } return
	 * super.getAttributeModifier(attribute); }
	 */
}
