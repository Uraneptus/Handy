package dev.uraneptus.handy;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HandyFakePlayer extends FakePlayer {
    public static final UUID ID = UUID.fromString("69c0f2e3-482d-4b7a-aadd-d0c6187bbe4c");

    public HandyFakePlayer(ServerLevel level) {
        super(level, new GameProfile(ID, "HandyClicker"));
        System.out.println("Player created");
    }

    @Override
    public float getCurrentItemAttackStrengthDelay() {
        return 1 / 64f;
    }

    @Override
    public boolean canEat(boolean ignoreHunger) {
        return false;
    }

    @Override
    public OptionalInt openMenu(MenuProvider menuProvider) {
        return OptionalInt.empty();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Faker");
    }

    @SubscribeEvent
    public static void killsDontSpawnXP(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof HandyFakePlayer) {
            event.setCanceled(true);
        }
    }
}
