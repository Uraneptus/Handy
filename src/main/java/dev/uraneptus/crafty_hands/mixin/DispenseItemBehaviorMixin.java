package dev.uraneptus.crafty_hands.mixin;

import dev.uraneptus.crafty_hands.CraftyHands;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultDispenseItemBehavior.class)
public class DispenseItemBehaviorMixin {

    // Fixes a crash when glove activates an action that destroys the dispenser eg. bed in nether, respawn anchor in overworld
    @Inject(method = "dispense(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/core/dispenser/DefaultDispenseItemBehavior;execute(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true
    )
    public void crafty_hands$preventWhenDispenserDestroyed(BlockSource source, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (source.getBlockState().isAir()) {
            cir.setReturnValue(itemStack);
        }
    }


    //Shouldn't play animation when item is glove
    @Inject(method = "dispense(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/DefaultDispenseItemBehavior;playAnimation(Lnet/minecraft/core/BlockSource;Lnet/minecraft/core/Direction;)V"),
            cancellable = true
    )
    public void crafty_hands$dispense(BlockSource source, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (itemStack.is(CraftyHands.GLOVES)) {
            cir.setReturnValue(itemStack);
        }
    }
}
