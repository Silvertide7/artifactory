package net.silvertide.artifactory.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class Config {
    public static ForgeConfigSpec SERVER_CONFIG;
    
    static {
        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

        setupServer(SERVER_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    private static void setupServer(ForgeConfigSpec.Builder builder) {
        buildBasics(builder);
    }


    public static ForgeConfigSpec.ConfigValue<Integer> XP_LEVELS_TO_ATTUNE_THRESHOLD;
    public static ForgeConfigSpec.ConfigValue<Integer> XP_LEVELS_TO_ATTUNE_CONSUMED;
    public static ForgeConfigSpec.ConfigValue<String> WEAR_EFFECTS_WHEN_USE_RESTRICTED;
    public static ForgeConfigSpec.ConfigValue<String> EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM;
    public static ForgeConfigSpec.ConfigValue<Integer> NUMBER_UNIQUE_ATTUNEMENTS_PER_PLAYER;
    public static ForgeConfigSpec.ConfigValue<String> ATTUNEMENT_INFORMATION_EXTENT;
    public static ForgeConfigSpec.ConfigValue<Boolean> CAN_USE_KEYBIND_TO_OPEN_MANAGE_SCREEN;

    private static void buildBasics(ForgeConfigSpec.Builder builder) {
        builder.push("Artifactory Configs");
        builder.comment("General");
        builder.comment("");
        builder.comment("How many levels by default you need to have to start the attunement process.");
        XP_LEVELS_TO_ATTUNE_THRESHOLD = builder.defineInRange("Attune XP Levels Threshold", 35, 0, Integer.MAX_VALUE);

        builder.comment("");
        builder.comment("How many levels by default are consumed to attune an item.");
        XP_LEVELS_TO_ATTUNE_CONSUMED = builder.defineInRange("Attune XP Levels Consumed", 20, 0, Integer.MAX_VALUE);

        builder.comment("");
        builder.comment("These effects are applied to a player who is wearing a restricted item in one of the armor slots.");
        builder.comment("The format is \"effect/level;effect/level;etc\" so if you wanted a player to be slowed at");
        builder.comment("level 3 and poisoned at level 1 you would put \"minecraft:slowness/3;minecraft:poison/1\".");
        WEAR_EFFECTS_WHEN_USE_RESTRICTED = builder.define("Wear Use Restricted Effects", "minecraft:slowness/4");

        builder.comment("");
        builder.comment("These effects are applied to a player who is wearing a restricted item in one of the armor slots.");
        builder.comment("The format is \"effect/level;effect/level;etc\" so if you wanted a player to be slowed and poisoned");
        builder.comment("you would put \"minecraft:slowness/3;minecraft:poison/1\".");
        EFFECTS_WHEN_HOLDING_OTHER_PLAYER_ITEM = builder.define("Effect When Holding Other Player Item", "minecraft:slowness/4;minecraft:poison/2");

        builder.comment("");
        builder.comment("A unique item can only be attuned by one player at a time. This controls how many unique attunements");
        builder.comment("each player can have at a time. This makes it so if they attune a number of unique items as specified here");
        builder.comment("then they will have to first break the attunement to an existing unique item before attuning a new one.");
        NUMBER_UNIQUE_ATTUNEMENTS_PER_PLAYER = builder.defineInRange("Number Unique Attunements Allowed Per Player", 0, 0, Integer.MAX_VALUE);

        builder.comment("");
        builder.comment("How much information is shown on the possible levels of attunement for an item after it has been bonded.");
        builder.comment("This information can be found in the manage screen of the Attunement Nexus by hovering on the (i).");
        builder.comment("These are the allowed values: all, next, current.");
        builder.comment("all: show all possible attunement level information for all existing levels.");
        builder.comment("next: show all levels up to the currently attuned level and one more if it exists, but no further.");
        builder.comment("current: show all levels up to the currently attuned levels information, but no further.");
        List<String> attunementInformationExtentAllowedValues = Arrays.asList("all", "next", "current");
        ATTUNEMENT_INFORMATION_EXTENT = builder.define("Extent of Attunement Information", "all", attunementInformationExtentAllowedValues::contains);

        builder.comment("");
        builder.comment("Keybinds");

        builder.comment("");
        builder.comment("Controls if a player can open the manage attunements screen from a keybind.");
        builder.comment("If true the player can assign a keybind to the manage attunements screen and open it anywhere.");
        builder.comment("If false it will not ope when the keybind is assigned and pressed.");
        CAN_USE_KEYBIND_TO_OPEN_MANAGE_SCREEN = builder.define("Can Open Manage Attunements Screen From Keybind", true);

        builder.pop();
    }
}
