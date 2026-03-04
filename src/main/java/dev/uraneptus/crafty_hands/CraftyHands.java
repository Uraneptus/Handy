package dev.uraneptus.crafty_hands;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(CraftyHands.MODID)
public class CraftyHands {

    public static final String MODID = "crafty_hands";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);

    public static final RegistryObject<Item> GLOVE_LEFT = ITEMS.register("glove_left", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PHANTOM_GLOVE_LEFT = ITEMS.register("phantom_glove_left", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GLOVE_RIGHT = ITEMS.register("glove_right", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PHANTOM_GLOVE_RIGHT = ITEMS.register("phantom_glove_right", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final TagKey<Item> GLOVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "gloves"));

    public static final PlayerTrigger CAUSE_EXPLOSION_TRIGGER = CriteriaTriggers.register(new PlayerTrigger(ResourceLocation.fromNamespaceAndPath(MODID, "cause_explosion")));

    public CraftyHands() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(GLOVE_LEFT.get(), new LeftDispenseBehavior(false));
            DispenserBlock.registerBehavior(GLOVE_RIGHT.get(), new RightDispenseBehavior(false));
            DispenserBlock.registerBehavior(PHANTOM_GLOVE_LEFT.get(), new LeftDispenseBehavior(true));
            DispenserBlock.registerBehavior(PHANTOM_GLOVE_RIGHT.get(), new RightDispenseBehavior(true));
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.acceptAll(ITEMS.getEntries().stream().map(RegistryObject::get).map(Item::getDefaultInstance).toList());
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
