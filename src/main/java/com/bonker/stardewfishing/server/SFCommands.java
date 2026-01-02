package com.bonker.stardewfishing.server;

import com.bonker.stardewfishing.common.FishingHookLogic;
import com.bonker.stardewfishing.proxy.ItemUtils;
import com.bonker.stardewfishing.server.data.FishBehaviorReloadListener;
import com.bonker.stardewfishing.server.data.MinigameDisabledPlayers;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.CompletableFuture;

public class SFCommands {
    private static final DynamicCommandExceptionType NO_BEHAVIOR = new DynamicCommandExceptionType(obj -> Component.translatable("commands.stardew_fishing.no_behavior", obj));
    private static final SimpleCommandExceptionType NO_ROD = new SimpleCommandExceptionType(Component.translatable("commands.stardew_fishing.no_rod"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        ArgumentTypeInfos.registerByClass(FishBehaviorArgument.class, SingletonArgumentInfo.contextAware(FishBehaviorArgument::new));

        dispatcher.register(Commands.literal("stardew_fishing")
                .then(Commands.literal("start_minigame")
                        .requires(stack -> stack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("item", new FishBehaviorArgument(buildContext))
                                .executes(SFCommands::startMinigame)))
                .then(Commands.literal("toggle_minigame")
                        .executes(SFCommands::toggleMinigame)));
    }

    private static int startMinigame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        InteractionHand rodHand = ItemUtils.getRodHand(player);
        if (rodHand == null) {
            throw NO_ROD.create();
        }

        ItemStack stack = context.getArgument("item", ItemInput.class).createItemStack(1, false);

        FishingHook hook = ItemUtils.spawnHook(player, player.getItemInHand(rodHand), player.position().add(0, 1, 0));
        player.fishing = hook;

        FishingHookLogic.getStoredRewards(hook).ifPresent(rewards -> {
            rewards.clear();
            rewards.add(stack);
        });

        FishingHookLogic.startStardewMinigame(context.getSource().getPlayerOrException());
        return 0;
    }

    private static int toggleMinigame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        MinigameDisabledPlayers data = MinigameDisabledPlayers.get(source.getServer());
        boolean disabled = data.isMinigameDisabled(player);
        data.setMinigameDisabled(player, !disabled);

        source.sendSuccess(() -> Component.translatable("commands.stardew_fishing." + (disabled ? "enabled" : "disabled") + "_minigame"), true);
        return 0;
    }

    public static class FishBehaviorArgument extends ItemArgument {
        public FishBehaviorArgument(CommandBuildContext pContext) {
            super(pContext);
        }

        @Override
        public ItemInput parse(StringReader pReader) throws CommandSyntaxException {
            ItemInput item = super.parse(pReader);
            if (!FishBehaviorReloadListener.getKeys().contains(ForgeRegistries.ITEMS.getKey(item.getItem()))) {
                throw NO_BEHAVIOR.createWithContext(pReader, item.getItem());
            }
            return item;
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
            return SharedSuggestionProvider.suggestResource(FishBehaviorReloadListener.getKeys(), pBuilder);
        }
    }
}
