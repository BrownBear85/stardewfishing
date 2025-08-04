package com.bonker.stardewfishing.common.items;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (tooltip == null) {
            tooltip = makeTooltip();
        }
        tooltipComponents.addAll(tooltip);
    }
}
