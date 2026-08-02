package com.smartsiege.mod.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Server-side config: lets players/server owners toggle each smart-AI feature
 * on or off, and tune how aggressive it is, without recompiling the mod.
 */
public class SiegeConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_DIGGING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DOOR_BREAKING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PILLARING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_AVOID_EXPLOSIONS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DODGE_ARROWS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PACK_TACTICS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_AMBUSH;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXPANDED_SENSING;

    public static final ForgeConfigSpec.DoubleValue ALERT_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SENSING_RANGE_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue DIG_HARDNESS_LIMIT;
    public static final ForgeConfigSpec.BooleanValue ONLY_ON_HARD_DIFFICULTY;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_MOB_GRIEFING;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("features");
        ENABLE_DIGGING = builder
            .comment("Hostile mobs will mine through soft blocks to reach the player.")
            .define("enableDigging", true);
        ENABLE_DOOR_BREAKING = builder
            .comment("Hostile mobs will break down closed doors blocking their path.")
            .define("enableDoorBreaking", true);
        ENABLE_PILLARING = builder
            .comment("Hostile mobs will stack blocks to climb up to an elevated target.")
            .define("enablePillaring", true);
        ENABLE_AVOID_EXPLOSIONS = builder
            .comment("Hostile mobs will flee from primed TNT and other explosives.")
            .define("enableAvoidExplosions", true);
        ENABLE_DODGE_ARROWS = builder
            .comment("Hostile mobs will sidestep incoming arrows.")
            .define("enableDodgeArrows", true);
        ENABLE_PACK_TACTICS = builder
            .comment("Hostile mobs will alert nearby allies of the same type to join the fight.")
            .define("enablePackTactics", true);
        ENABLE_AMBUSH = builder
            .comment("Hostile mobs will hide/wait in the dark and rush the player once close.")
            .define("enableAmbush", true);
        ENABLE_EXPANDED_SENSING = builder
            .comment("Hostile mobs get a larger detection/follow range.")
            .define("enableExpandedSensing", true);
        builder.pop();

        builder.push("tuning");
        ALERT_RADIUS = builder
            .comment("Radius (blocks) in which pack tactics alerts allies.")
            .defineInRange("alertRadius", 16.0, 4.0, 64.0);
        SENSING_RANGE_MULTIPLIER = builder
            .comment("Multiplier applied to a mob's follow range when expanded sensing is enabled.")
            .defineInRange("sensingRangeMultiplier", 1.75, 1.0, 4.0);
        DIG_HARDNESS_LIMIT = builder
            .comment("Only blocks with hardness at or below this value (x10, integer) can be dug through. "
                + "Stone is 15, dirt is 5. Prevents mobs from digging through stone/obsidian etc.")
            .defineInRange("digHardnessLimit", 10, 0, 100);
        ONLY_ON_HARD_DIFFICULTY = builder
            .comment("If true, all smart-AI behaviors only apply when world difficulty is Hard.")
            .define("onlyOnHardDifficulty", false);
        REQUIRE_MOB_GRIEFING = builder
            .comment("If true, digging/door-breaking/pillaring respect the mobGriefing gamerule.")
            .define("requireMobGriefing", true);
        builder.pop();

        SPEC = builder.build();
    }
}
