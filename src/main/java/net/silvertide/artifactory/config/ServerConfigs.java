package net.silvertide.artifactory.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Boolean> UPDATE_ATTUNEMENT_LEVEL_ATTRIBUTE;
    public static final ModConfigSpec.ConfigValue<Integer> BASE_ATTUNEMENT_LEVEL_ATTRIBUTE;
    public static final ModConfigSpec.ConfigValue<Integer> XP_LEVELS_TO_ATTUNE_THRESHOLD;
    public static final ModConfigSpec.ConfigValue<Integer> XP_LEVELS_TO_ATTUNE_CONSUMED;
    public static final ModConfigSpec.ConfigValue<String> WEAR_EFFECTS_WHEN_USE_RESTRICTED;
    public static final ModConfigSpec.ConfigValue<Boolean> PLAYERS_CAN_PICKUP_ATTUNED_ITEMS;
    public static final ModConfigSpec.ConfigValue<String> EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM;
    public static final ModConfigSpec.ConfigValue<Boolean> SHOW_UNIDENTIFIED_PERCENTAGE;
    public static final ModConfigSpec.ConfigValue<Boolean> CAN_USE_KEYBIND_TO_OPEN_MANAGE_SCREEN;

    static {
        BUILDER.push("Artifactory Configs");

        BUILDER.comment("If true it will check and update the attunement level attribute of a player when they log in, if their base value is different than below.");
        BUILDER.comment("Once you have all players updated you can turn this back off, just in case another mod is supposed to adjust the base value of this attribute. Although that seems unlikely.");
        UPDATE_ATTUNEMENT_LEVEL_ATTRIBUTE = BUILDER.worldRestart().define("updateAttunementLevelAttribute", false);

        BUILDER.comment("The base value of the Attunement Level attribute. Default: 15");
        BUILDER.comment("If you change this and you want it to update for players, make sure to turn on the updateAttunementLevelAttribute config or it won't apply.");
        BASE_ATTUNEMENT_LEVEL_ATTRIBUTE = BUILDER.worldRestart().defineInRange("baseAttunementLevelAttribute", 15, 1, 1000);

        BUILDER.comment("How many levels by default you need to have to start the attunement process. Default: 35");
        XP_LEVELS_TO_ATTUNE_THRESHOLD = BUILDER.worldRestart().defineInRange("xpLevelThreshold", 30, 0, Integer.MAX_VALUE);

        BUILDER.comment("How many levels by default are consumed to attune an item.");
        XP_LEVELS_TO_ATTUNE_CONSUMED = BUILDER.worldRestart().defineInRange("xpLevelsConsumed", 20, 0, Integer.MAX_VALUE);

        BUILDER.comment("These effects are applied to a player who is wearing a restricted item in one of the armor slots.");
        BUILDER.comment("The format is \"effect/level;effect/level;etc\" so if you wanted a player to be slowed at");
        BUILDER.comment("level 3 and poisoned at level 1 you would put \"minecraft:slowness/3;minecraft:poison/1\".");
        WEAR_EFFECTS_WHEN_USE_RESTRICTED = BUILDER.worldRestart().define("wearRestrictedEffects", "minecraft:slowness/4");

        BUILDER.comment("Controls if players can pick up items attuned to other players.");
        BUILDER.comment("If true then players can pickup items that are attuned to other players");
        BUILDER.comment("If false then players can only pickup items that are unattuned or that they are attuned to.");
        PLAYERS_CAN_PICKUP_ATTUNED_ITEMS = BUILDER.worldRestart().define("canPlayersPickupAttunedItems", false);

        BUILDER.comment("These effects are applied to a player who is wearing a restricted item in one of the armor slots.");
        BUILDER.comment("The format is \"effect/level;effect/level;etc\" so if you wanted a player to be slowed and poisoned");
        BUILDER.comment("you would put \"minecraft:slowness/3;minecraft:poison/1\".");
        EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM = BUILDER.worldRestart().define("holdingOtherPlayersItemEffects", "minecraft:slowness/4;minecraft:poison/2");

        BUILDER.comment("If true then when an attunement has not yet been identified it will show the perfect chance that the item has an attunement if it is less than 100%.");
        BUILDER.comment("If false then it won't, it will just show a question mark instead.");
        SHOW_UNIDENTIFIED_PERCENTAGE = BUILDER.worldRestart().define("showUnidentifiedPercentage", true);

        BUILDER.pop();

        BUILDER.push("Keybinds");

        BUILDER.comment("Controls if a player can open the manage attunements screen from a keybind.");
        BUILDER.comment("If true the player can assign a keybind to the manage attunements screen and open it anywhere.");
        BUILDER.comment("If false it will not ope when the keybind is assigned and pressed.");
        CAN_USE_KEYBIND_TO_OPEN_MANAGE_SCREEN = BUILDER.worldRestart().define("Can Open Manage Attunements Screen From Keybind", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
