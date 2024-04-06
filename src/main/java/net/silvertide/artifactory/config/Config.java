package net.silvertide.artifactory.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER;
    public static final ForgeConfigSpec COMMON_CONFIG;

    public static final ForgeConfigSpec.ConfigValue<Integer> XP_LEVELS_TO_ATTUNE_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Integer> XP_LEVELS_TO_ATTUNE_CONSUMED;
    public static final ForgeConfigSpec.ConfigValue<String> WEAR_EFFECTS_WHEN_USE_RESTRICTED;
    public static final ForgeConfigSpec.ConfigValue<String> EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM;

    static {
        BUILDER = new ForgeConfigSpec.Builder();

        BUILDER.push("Homebound Configs");

        BUILDER.comment("General");
        BUILDER.comment("How many levels you need to have to start the attunement process.");
        XP_LEVELS_TO_ATTUNE_THRESHOLD = BUILDER.defineInRange("Attune XP Levels Threshold", 35, 0, Integer.MAX_VALUE);
        BUILDER.comment("How many levels are consumed when you attune an item.");
        XP_LEVELS_TO_ATTUNE_CONSUMED = BUILDER.defineInRange("Attune XP Levels Consumed", 20, 0, Integer.MAX_VALUE);

        BUILDER.comment("These effects are applied to a player who is wearing a restricted item in one of the armor slots.");
        BUILDER.comment("The format is \"effect/level;effect/level;etc\" so if you wanted a player to be slowed at");
        BUILDER.comment("level 3 and poisoned at level 1 you would put \"minecraft:slowness/3;minecraft:poison/1\".");
        WEAR_EFFECTS_WHEN_USE_RESTRICTED = BUILDER.define("Wear Use Restricted Effects", "minecraft:slowness/3");

        BUILDER.comment("These effects are applied to a player who is wearing a restricted item in one of the armor slots.");
        BUILDER.comment("The format is \"effect/level;effect/level;etc\" so if you wanted a player to be slowed and poisoned");
        BUILDER.comment("you would put \"minecraft:slowness/3;minecraft:poison/1\".");
        EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM = BUILDER.define("Effect When Holding Other Player Item", "minecraft:slowness/3;minecraft:poison/1");

        BUILDER.pop();

        COMMON_CONFIG = BUILDER.build();
    }
}
