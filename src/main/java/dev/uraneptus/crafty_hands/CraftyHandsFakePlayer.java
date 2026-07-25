package dev.uraneptus.crafty_hands;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

import java.util.OptionalInt;
import java.util.UUID;

@EventBusSubscriber
public class CraftyHandsFakePlayer extends FakePlayer {
    public static final UUID ID = UUID.fromString("69c0f2e3-482d-4b7a-aadd-d0c6187bbe4c");

    public CraftyHandsFakePlayer(ServerLevel level) {
        super(level, new GameProfile(ID, "CraftyHandsClicker"));
    }

    @Override
    public float getCurrentItemAttackStrengthDelay() {
        return 1 / 64f;
    }

    @Override
    public boolean canEat(boolean ignoreHunger) {
        return true;
    }

    @Override
    public OptionalInt openMenu(MenuProvider menuProvider) {
        return OptionalInt.empty();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Crafty Hands Faker");
    }

    //TODO maybe we want to disable mob loot that only drops for the player too
    @SubscribeEvent
    public static void killsDontSpawnXP(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof CraftyHandsFakePlayer) {
            event.setCanceled(true);
        }
    }
}
