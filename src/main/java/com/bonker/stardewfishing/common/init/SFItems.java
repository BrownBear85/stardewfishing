package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.items.SFTooltipItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class SFItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StardewFishing.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StardewFishing.MODID);

    public static final int LEGENDARY_FISH_COLOR = 0xFFFFBE;
    public static final Component LEGENDARY_FISH_TOOLTIP = Component.translatable("tooltip.stardew_fishing.legendary_fish")
            .withStyle(StardewFishing.LEGENDARY);

    public static final DeferredItem<SFTooltipItem> TRAP_BOBBER = ITEMS.registerItem("trap_bobber",
            prop -> new SFTooltipItem(prop.durability(64)));

    public static final DeferredItem<SFTooltipItem> CORK_BOBBER = ITEMS.registerItem("cork_bobber",
            prop -> new SFTooltipItem(prop.durability(64)));

    public static final DeferredItem<SFTooltipItem> SONAR_BOBBER = ITEMS.registerItem("sonar_bobber",
            prop -> new SFTooltipItem(prop.durability(64)));

    public static final DeferredItem<SFTooltipItem> TREASURE_BOBBER = ITEMS.registerItem("treasure_bobber",
            prop -> new SFTooltipItem(prop.durability(64)));

    public static final DeferredItem<SFTooltipItem> QUALITY_BOBBER = ITEMS.registerItem("quality_bobber",
            prop -> new SFTooltipItem(prop.durability(64)) {
        @Override
        protected List<Component> makeTooltip() {
            List<Component> tooltip = super.makeTooltip();
            if (StardewFishing.QUALITY_FOOD_INSTALLED) {
                tooltip.add(1, Component.translatable(getDescriptionId() + ".quality_food_tooltip").withStyle(StardewFishing.LIGHTER_COLOR));
            }
            return tooltip;
        }
    });

    public static final DeferredItem<SFTooltipItem> GOLIATH_GROUPER = ITEMS.registerItem("goliath_grouper",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> VAMPIRE_PAYARA = ITEMS.registerItem("vampire_payara",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> GOLDEN_SNOOK = ITEMS.registerItem("golden_snook",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> SABRETOOTHED_TIGERFISH = ITEMS.registerItem("sabretoothed_tigerfish",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> CHROMATIC_ARAPAIMA = ITEMS.registerItem("chromatic_arapaima",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> CYCLOPS_MAHIMAHI = ITEMS.registerItem("cyclops_mahimahi",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> STORM_TARPON = ITEMS.registerItem("storm_tarpon",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> BLAZING_OARFISH = ITEMS.registerItem("blazing_oarfish",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> CRYSTALLINE_SNAKEHEAD = ITEMS.registerItem("crystalline_snakehead",
            SFTooltipItem::new);

    public static final DeferredItem<SFTooltipItem> DEMON_GAR = ITEMS.registerItem("demon_gar",
            SFTooltipItem::new);

    public static final Supplier<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.stardewFishing"))
            .icon(() -> new ItemStack(SABRETOOTHED_TIGERFISH.get()))
            .displayItems((pParameters, pOutput) -> {
                pOutput.accept(SFBlocks.FISH_DISPLAY.get());
                pOutput.accept(TRAP_BOBBER.get());
                pOutput.accept(CORK_BOBBER.get());
                pOutput.accept(SONAR_BOBBER.get());
                pOutput.accept(TREASURE_BOBBER.get());
                pOutput.accept(QUALITY_BOBBER.get());
                pOutput.accept(GOLIATH_GROUPER.get());
                pOutput.accept(VAMPIRE_PAYARA.get());
                pOutput.accept(GOLDEN_SNOOK.get());
                pOutput.accept(SABRETOOTHED_TIGERFISH.get());
                pOutput.accept(CHROMATIC_ARAPAIMA.get());
                pOutput.accept(CYCLOPS_MAHIMAHI.get());
                pOutput.accept(STORM_TARPON.get());
                pOutput.accept(BLAZING_OARFISH.get());
                pOutput.accept(CRYSTALLINE_SNAKEHEAD.get());
                pOutput.accept(DEMON_GAR.get());
            })
            .build());
}
