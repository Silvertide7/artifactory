package net.silvertide.artifactory.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_APPLIES_WHEN_HINT = BUILDER
            .comment("Show a hint on items that only become artifacts once a condition is met, e.g. \"Becomes attunable when: Must be fully awakened\".")
            .define("showAppliesWhenHint", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfigs() {}
}
