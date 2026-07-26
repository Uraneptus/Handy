package dev.uraneptus.crafty_hands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class RightDispenseBehavior extends OptionalDispenseItemBehavior {
    private final boolean isPhantom;

    public RightDispenseBehavior(boolean isPhantom) {
        this.isPhantom = isPhantom;
    }

    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel serverlevel = blockSource.level();
        this.setSuccess(false);
        if (!serverlevel.isClientSide()) {
            BlockPos blockposFacing = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING), isPhantom ? 2 : 1);
            setSuccess(entityInteract(serverlevel, blockposFacing) || blockInteract(serverlevel, blockSource.pos(), blockposFacing));
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
            CraftyHandsFakePlayer player = new CraftyHandsFakePlayer(serverlevel);
            InteractionHand hand = InteractionHand.MAIN_HAND;
            InteractionResult cancelResult = CommonHooks.onInteractEntity(player, randomEntityAtPos, hand);
            if (cancelResult == null) {
                if (randomEntityAtPos.interact(player, hand).consumesAction()) {
                    if (randomEntityAtPos instanceof AbstractVillager villager) {
                        if (villager.getTradingPlayer() instanceof CraftyHandsFakePlayer) {
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
            CraftyHandsFakePlayer clickerPlayer = new CraftyHandsFakePlayer(serverlevel);
            InteractionHand hand = InteractionHand.MAIN_HAND;
            BlockHitResult hitResult = serverlevel.clip(new ClipContext(blockPosOrigin.getCenter(), blockposFacing.getCenter(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, clickerPlayer));
            if (hitResult.getBlockPos() != blockposFacing) {
                hitResult = new BlockHitResult(hitResult.getLocation(), hitResult.getDirection(), blockposFacing, hitResult.isInside());
            }
            PlayerInteractEvent.RightClickBlock event = CommonHooks.onRightClickBlock(clickerPlayer, hand, blockposFacing, hitResult);
            TriState useBlock = event.getUseBlock();
            if (useBlock != TriState.FALSE) {
                InteractionResult result = stateAtPos.useWithoutItem(serverlevel, clickerPlayer, hitResult);
                if (result.consumesAction()) {
                    if (stateAtPos.is(Blocks.CAKE)) {
                        serverlevel.playSound(null, blockPosOrigin, SoundEvents.PLAYER_BURP, SoundSource.BLOCKS, 0.4F, 1.0F);
                    }
                    if (causesExplosion(stateAtPos, serverlevel)) {
                        Player nearestPlayer = serverlevel.getNearestPlayer(TargetingConditions.forNonCombat().ignoreLineOfSight().range(2).selector(e -> e.isAlive() && !e.isSpectator()), blockPosOrigin.getX(), blockPosOrigin.getY(), blockPosOrigin.getZ());
                        if (nearestPlayer instanceof ServerPlayer sp) {
                            CraftyHands.CAUSE_EXPLOSION_TRIGGER.get().trigger(sp);
                        }
                    }
                    clickerPlayer.discard();
                    return true;
                }
            }
            clickerPlayer.discard();
        }
        return false;
    }

    private boolean causesExplosion(BlockState state, Level level) {
        return (state.is(BlockTags.BEDS) && !level.dimensionType().bedWorks()) || (state.is(Blocks.RESPAWN_ANCHOR) && !level.dimensionType().respawnAnchorWorks());
    }
}
