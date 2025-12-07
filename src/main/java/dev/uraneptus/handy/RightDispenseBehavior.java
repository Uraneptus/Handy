package dev.uraneptus.handy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class RightDispenseBehavior extends OptionalDispenseItemBehavior {

    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel serverlevel = blockSource.getLevel();
        this.setSuccess(false);
        if (!serverlevel.isClientSide()) {
            BlockPos blockposFacing = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
            setSuccess(entityInteract(serverlevel, blockposFacing) || blockInteract(serverlevel, blockSource.getPos(), blockposFacing));
        }
        return itemStack;
    }

    private boolean entityInteract(ServerLevel serverlevel, BlockPos blockposFacing) {
        List<Entity> entitiesAtPos = serverlevel.getEntitiesOfClass(Entity.class, new AABB(blockposFacing))
                .stream()
                .filter(e -> e instanceof LivingEntity || e instanceof HangingEntity)
                .toList();
        if (!entitiesAtPos.isEmpty()) {
            Entity randomEntityAtPos = entitiesAtPos.get(serverlevel.random.nextInt(entitiesAtPos.size()));
            HandyFakePlayer player = new HandyFakePlayer(serverlevel);
            InteractionHand hand = InteractionHand.MAIN_HAND;
            InteractionResult cancelResult = ForgeHooks.onInteractEntity(player, randomEntityAtPos, hand);
            if (cancelResult == null) {
                if (randomEntityAtPos.interact(player, hand).consumesAction()) {
                    if (randomEntityAtPos instanceof AbstractVillager villager) {
                        if (villager.getTradingPlayer() instanceof HandyFakePlayer) {
                            villager.setTradingPlayer(null);
                        }
                    }
                    player.discard();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean blockInteract(ServerLevel serverlevel, BlockPos blockPosOrigin, BlockPos blockposFacing) {
        BlockState stateAtPos = serverlevel.getBlockState(blockposFacing);
        if (!stateAtPos.getShape(serverlevel, blockposFacing).isEmpty()) {
            HandyFakePlayer player = new HandyFakePlayer(serverlevel);
            InteractionHand hand = InteractionHand.MAIN_HAND;
            BlockHitResult result = serverlevel.clip(new ClipContext(blockPosOrigin.getCenter(), blockposFacing.getCenter(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, blockposFacing, result);
            Event.Result useBlock = event.getUseBlock();
            System.out.println(useBlock);
            if (useBlock != Event.Result.DENY) {
                System.out.println("ello");
                stateAtPos.use(serverlevel, player, hand, result);
                player.discard();
                return true;
            }
        }
        return false;
    }
}
