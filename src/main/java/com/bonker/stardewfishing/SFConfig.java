package com.bonker.stardewfishing;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SFConfig {
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    // server
    private static final ModConfigSpec.DoubleValue QUALITY_1_THRESHOLD;
    private static final ModConfigSpec.DoubleValue QUALITY_2_THRESHOLD;
    private static final ModConfigSpec.DoubleValue QUALITY_3_THRESHOLD;
    private static final ModConfigSpec.DoubleValue QUALITY_1_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue QUALITY_2_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue QUALITY_3_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue BITE_TIME_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue TREASURE_CHEST_CHANCE;
    private static final ModConfigSpec.DoubleValue GOLDEN_CHEST_CHANCE;
    private static final ModConfigSpec.BooleanValue INVENTORY_BOBBER_EQUIPPING;
    private static final ModConfigSpec.BooleanValue LEGENDARY_FISH_FLASHING;
    private static final ModConfigSpec.DoubleValue LEGENDARY_FISH_CHANCE;

    // client
    private static final ModConfigSpec.BooleanValue ISOLATE_AUDIO_CUES;

    static {
        QUALITY_1_THRESHOLD = SERVER_BUILDER
                .comment("The minimum accuracy that grants an item of quality 1.")
                .defineInRange("quality1Threshold", 0.75, 0, 1);

        QUALITY_2_THRESHOLD = SERVER_BUILDER
                .comment("The minimum accuracy that grants an item of quality 2.")
                .defineInRange("quality2Threshold", 0.9, 0, 1);

        QUALITY_3_THRESHOLD = SERVER_BUILDER
                .comment("The minimum accuracy that grants an item of quality 3.")
                .defineInRange("quality3Threshold", 1.0, 0, 1);

        QUALITY_1_MULTIPLIER = SERVER_BUILDER
                .comment("The multiplier that is applied to experience gained from fishing a quality 1 reward.")
                .defineInRange("quality1Multiplier", 1.5, 1, 10);

        QUALITY_2_MULTIPLIER = SERVER_BUILDER
                .comment("The multiplier that is applied to experience gained from fishing a quality 2 reward.")
                .defineInRange("quality2Multiplier", 2.5, 1, 10);

        QUALITY_3_MULTIPLIER = SERVER_BUILDER
                .comment("The multiplier that is applied to experience gained from fishing a quality 3 reward.")
                .defineInRange("quality3Multiplier", 4.0, 1, 10);

        BITE_TIME_MULTIPLIER = SERVER_BUILDER
                .comment("The multiplier that is applied to the time it takes for a fish to bite after casting your rod.")
                .defineInRange("biteTimeMultiplier", 0.8, 0, 1);

        TREASURE_CHEST_CHANCE = SERVER_BUILDER
                .comment("The chance for finding a treasure chest each time you play the fishing minigame.")
                .defineInRange("treasureChestChance", 0.15, 0, 1);

        GOLDEN_CHEST_CHANCE = SERVER_BUILDER
                .comment("The chance that a treasure chest found in the fishing minigame is a golden chest.")
                .defineInRange("goldenChestChance", 0.1, 0, 1);

        INVENTORY_BOBBER_EQUIPPING = SERVER_BUILDER
                .comment("Whether it be possible to attach bobber items by hovering over a fishing rod in an inventory and right clicking.")
                .define("inventoryBobberEquipping", true);

        LEGENDARY_FISH_FLASHING = SERVER_BUILDER
                .comment("Whether legendary fish will have a strobe effect when moving in the minigame.")
                .define("legendaryFishFlashing", true);

        LEGENDARY_FISH_CHANCE = SERVER_BUILDER
                .comment("The chance that any fish that bites is a legendary fish.")
                .defineInRange("legendaryFishChance", 0.01, 0, 1);

        SERVER_SPEC = SERVER_BUILDER.build();

        ISOLATE_AUDIO_CUES = CLIENT_BUILDER
                .comment("When this setting is enabled, audio cues in the minigame will be louder and other sounds will be muted.")
                .define("isolateAudioCues", false);

        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    public static int getQuality(double accuracy) {
        if (accuracy >= SFConfig.QUALITY_3_THRESHOLD.get()) {
            return 3;
        } else if (accuracy >= SFConfig.QUALITY_2_THRESHOLD.get()) {
            return 2;
        } else if (accuracy >= SFConfig.QUALITY_1_THRESHOLD.get()) {
            return 1;
        }
        return 0;
    }

    public static double getMultiplier(double accuracy, double expMultiplierStat) {
        double multiplier = switch (getQuality(accuracy)) {
            case 3 -> QUALITY_3_MULTIPLIER.get();
            case 2 -> QUALITY_2_MULTIPLIER.get();
            case 1 -> QUALITY_1_MULTIPLIER.get();
            default -> 1;
        };

        multiplier *= expMultiplierStat;

        return multiplier;
    }

    public static double getBiteTimeMultiplier() {
        return BITE_TIME_MULTIPLIER.get();
    }

    public static double getTreasureChestChance() {
        return TREASURE_CHEST_CHANCE.get();
    }

    public static double getGoldenChestChance() {
        return GOLDEN_CHEST_CHANCE.get();
    }

    public static boolean isInventoryEquippingEnabled() {
        return SERVER_SPEC.isLoaded() ? INVENTORY_BOBBER_EQUIPPING.get() : false;
    }

    public static boolean isLegendaryFlashingEnabled() {
        return LEGENDARY_FISH_FLASHING.get();
    }

    public static float getLegendaryFishChance(float luck) {
        return (float) (LEGENDARY_FISH_CHANCE.get() + luck * 0.01F);
    }

    public static boolean isolateAudioCues() {
        return ISOLATE_AUDIO_CUES.get();
    }
}
