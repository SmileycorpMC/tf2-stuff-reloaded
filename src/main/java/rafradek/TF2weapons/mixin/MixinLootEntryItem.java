package rafradek.TF2weapons.mixin;

import net.minecraft.item.Item;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.loot.RandomWeaponItemFunction;

@Mixin(LootEntryItem.class)
public class MixinLootEntryItem {

    @Mutable
    @Shadow @Final protected LootFunction[] functions;

    @Inject(at=@At("TAIL"), method = "<init>")
    public void LootEntryItem(Item item, int weightIn, int qualityIn, LootFunction[] functionsIn, LootCondition[] conditionsIn, String entryName, CallbackInfo callback) {
        if (item == TF2weapons.itemTF2) functions = ArrayUtils.add(functions, new RandomWeaponItemFunction());
    }


}
