package dev.uraneptus.crafty_hands;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(CraftyHands.MODID)
public class CraftyHands {

    public static final String MODID = "crafty_hands";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MODID);

    public static final DeferredItem<Item> GLOVE_LEFT = ITEMS.register("glove_left", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PHANTOM_GLOVE_LEFT = ITEMS.register("phantom_glove_left", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> GLOVE_RIGHT = ITEMS.register("glove_right", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PHANTOM_GLOVE_RIGHT = ITEMS.register("phantom_glove_right", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final TagKey<Item> GLOVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "gloves"));

    public static final Supplier<PlayerTrigger> CAUSE_EXPLOSION_TRIGGER = TRIGGERS.register("cause_explosion", PlayerTrigger::new);

    public CraftyHands(IEventBus bus, ModContainer modContainer) {
        ITEMS.register(bus);
        TRIGGERS.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(this::addCreative);
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
            event.acceptAll(ITEMS.getEntries().stream().map(Supplier::get).map(Item::getDefaultInstance).toList());
        }
    }

}
