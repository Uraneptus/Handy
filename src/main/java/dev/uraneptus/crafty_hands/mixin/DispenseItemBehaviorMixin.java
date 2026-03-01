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
