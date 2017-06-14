package rafradek.TF2weapons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.decoration.ItemWearable;

public class ItemCrate extends ItemFromData {
	public ItemCrate() {
		this.setMaxStackSize(64);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn,
			EnumHand hand) {
		ItemStack itemStackIn=playerIn.getHeldItem(hand); 
		if (itemStackIn.getTagCompound().getBoolean("Open")) {
			//ArrayList<String> list = new ArrayList<String>();
			
			
			ItemStack stack=ItemStack.EMPTY;
			if(playerIn.getRNG().nextInt(32)==0){
				stack= ItemFromData.getRandomWeaponOfClass("cosmetic", playerIn.getRNG(), false);
				((ItemWearable)stack.getItem()).applyRandomEffect(stack, playerIn.getRNG());
			}
			else{
				int choosen=playerIn.getRNG().nextInt(getData(itemStackIn).maxCrateValue);
				int currVal=0;
				for (Entry<String, Integer> entry : getData(itemStackIn).crateContent.entrySet()){
					currVal+=entry.getValue();
					if(choosen<currVal){
						 stack=ItemFromData.getNewStack(entry.getKey());
						 break;
					}
					/*for (int i = 0; i < entry.getValue(); i++)
						list.add(entry.getKey());*/
				}
			}
			//
			if (!(stack.getItem() instanceof ItemWearable))
				stack.getTagCompound().setBoolean("Strange", true);
			
			if (!playerIn.inventory.addItemStackToInventory(stack))
				playerIn.dropItem(stack, true);
			playerIn.addStat(TF2Achievements.LOOT_CRATE);
			playerIn.addStat(TF2weapons.cratesOpened);
			if(!worldIn.isRemote && ((EntityPlayerMP)playerIn).getStatFile().readStat(TF2weapons.cratesOpened)>=9){
				playerIn.addStat(TF2Achievements.CRATES_10);
			}
			itemStackIn.shrink(1);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStackIn);
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par2List,
			boolean par4) {
		/*
		 * if (!par1ItemStack.hasTagCompound()) {
		 * par1ItemStack.getTagCompound()=new NBTTagCompound();
		 * par1ItemStack.getTagCompound().setTag("Attributes", (NBTTagCompound)
		 * ((ItemUsable)par1ItemStack.getItem()).buildInAttributes.copy()); }
		 */
		if (par1ItemStack.hasTagCompound()) {
			super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4);

			if (par1ItemStack.getTagCompound().getBoolean("Open")) {
				par2List.add("The crate is opened now");
				par2List.add("Right click to get the item");
			}

			par2List.add("Possible content:");
			for (String name : getData(par1ItemStack).crateContent.keySet()) {
				WeaponData data = MapList.nameToData.get(name);
				if (data != null)
					par2List.add(data.getString(PropertyType.NAME));
			}
		}
	}
}
