package dev.uraneptus.handy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LeftDispenseBehavior extends OptionalDispenseItemBehavior {

    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel serverlevel = blockSource.getLevel();
        this.setSuccess(false);
        if (!serverlevel.isClientSide()) {
            BlockPos blockposFacing = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
            setSuccess(shouldAttack(serverlevel, blockposFacing) || shouldBreakBlock(serverlevel, blockposFacing));
        }
        return itemStack;
    }

    private boolean shouldAttack(ServerLevel serverlevel, BlockPos blockposFacing) {
        List<Entity> entitiesAtPos = serverlevel.getEntitiesOfClass(Entity.class, new AABB(blockposFacing))
                .stream()
                .filter(e -> e instanceof LivingEntity || e instanceof HangingEntity)
                .toList();
        if (!entitiesAtPos.isEmpty()) {
            Entity randomEntityAtPos = entitiesAtPos.get(serverlevel.random.nextInt(entitiesAtPos.size()));
            if (!(randomEntityAtPos instanceof Player player && player.isCreative())) {
                HandyFakePlayer player = new HandyFakePlayer(serverlevel);
                player.resetAttackStrengthTicker();
                player.attack(randomEntityAtPos);
                player.discard();
                return true;
            }
        }
        return false;
    }

    private boolean shouldBreakBlock(ServerLevel serverlevel, BlockPos blockposFacing) {
        BlockState stateAtPos = serverlevel.getBlockState(blockposFacing);
        if (!stateAtPos.isAir() && stateAtPos.getDestroySpeed(serverlevel, blockposFacing) <= 0.1F) {
            return removeBlock(serverlevel, new HandyFakePlayer(serverlevel), blockposFacing, stateAtPos);
        }
        return false;
    }

    private boolean removeBlock(ServerLevel level, Player player, BlockPos blockPos, BlockState state) {
        boolean removed = state.onDestroyedByPlayer(level, blockPos, player, true, level.getFluidState(blockPos));
        if (removed) {
            state.getBlock().destroy(level, blockPos, state);
            Block.dropResources(state, level, blockPos);
            player.discard();
        }
        return removed;
    }
}
