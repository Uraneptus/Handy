package dev.uraneptus.crafty_hands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class RightDispenseBehavior extends OptionalDispenseItemBehavior {

    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel serverlevel = blockSource.getLevel();
        this.setSuccess(false);
        if (!serverlevel.isClientSide()) {
            BlockPos blockposFacing = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING), 1);
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
            CraftyHandsFakePlayer player = new CraftyHandsFakePlayer(serverlevel);
            InteractionHand hand = InteractionHand.MAIN_HAND;
            InteractionResult cancelResult = ForgeHooks.onInteractEntity(player, randomEntityAtPos, hand);
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
            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(clickerPlayer, hand, blockposFacing, hitResult);
            Event.Result useBlock = event.getUseBlock();
            if (useBlock != Event.Result.DENY) {
                InteractionResult result = stateAtPos.use(serverlevel, clickerPlayer, hand, hitResult);
                if (result.consumesAction()) {
                    if (stateAtPos.is(Blocks.CAKE)) {
                        serverlevel.playSound(null, blockPosOrigin, SoundEvents.PLAYER_BURP, SoundSource.BLOCKS, 0.4F, 1.0F);
                    }
                    if (causesExplosion(stateAtPos, serverlevel)) {
                        Player nearestPlayer = serverlevel.getNearestPlayer(TargetingConditions.forNonCombat().ignoreLineOfSight().range(2).selector(e -> e.isAlive() && !e.isSpectator()), blockPosOrigin.getX(), blockPosOrigin.getY(), blockPosOrigin.getZ());
                        System.out.println(nearestPlayer);
                        if (nearestPlayer instanceof ServerPlayer sp) {
                            System.out.println(nearestPlayer.getName().getString());
                            CraftyHands.CAUSE_EXPLOSION_TRIGGER.trigger(sp);
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
