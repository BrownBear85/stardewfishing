package com.bonker.stardewfishing.common.items;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SFTooltipItem extends Item {
    private List<Component> tooltip;

    public SFTooltipItem(Properties pProperties) {
        super(pProperties);
    }

    protected List<Component> makeTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(StardewFishing.LIGHTER_COLOR));
        return tooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        if (tooltip == null) {
            tooltip = makeTooltip();
        }
        for (Component component : tooltip) {
            tooltipAdder.accept(component);
        }
    }
}
