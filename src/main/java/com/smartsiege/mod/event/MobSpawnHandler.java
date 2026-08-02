package com.smartsiege.mod.event;

import com.smartsiege.mod.ai.AmbushGoal;
import com.smartsiege.mod.ai.AvoidExplosionGoal;
import com.smartsiege.mod.ai.DigTowardTargetGoal;
import com.smartsiege.mod.ai.DodgeArrowGoal;
import com.smartsiege.mod.ai.PackTacticsGoal;
import com.smartsiege.mod.ai.PillarUpGoal;
import com.smartsiege.mod.ai.SmartBreakDoorGoal;
import com.smartsiege.mod.config.SiegeConfig;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "smartsiege")
public class MobSpawnHandler {

    private static final Set<java.util.UUID> UPGRADED = new HashSet<>();

    private static Field goalSelectorField;
    private static Field targetSelectorField;

    private static void initFields() {
        if (goalSelectorField != null) return;
        java.util.List<Field> matches = new java.util.ArrayList<>();
        for (Field f : net.minecraft.world.entity.Mob.class.getDeclaredFields()) {
            if (f.getType() == GoalSelector.class) {
                f.setAccessible(true);
                matches.add(f);
            }
        }
        if (matches.size() < 2) {
            throw new RuntimeException(
                "SmartSiege: expected 2 GoalSelector fields on Mob, found " + matches.size());
        }
        goalSelectorField = matches.get(0);
        targetSelectorField = matches.get(1);
    }

    private static GoalSelector getGoalSelector(PathfinderMob mob) {
        initFields();
        try {
            return (GoalSelector) goalSelectorField.get(mob);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static GoalSelector getTargetSelector(PathfinderMob mob) {
        initFields();
        try {
            return (GoalSelector) targetSelectorField.get(mob);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getEntity() instanceof Monster monster)) return;
        PathfinderMob mob = monster;

        if (SiegeConfig.ONLY_ON_HARD_DIFFICULTY.get()
            && level.getDifficulty() != Difficulty.HARD) {
            return;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;

        if (!UPGRADED.add(monster.getUUID())) return;
        applySmartAi(mob);
    }

    private static void applySmartAi(PathfinderMob mob) {
        GoalSelector goals = getGoalSelector(mob);
        GoalSelector targets = getTargetSelector(mob);

        if (SiegeConfig.ENABLE_AVOID_EXPLOSIONS.get()) {
            goals.addGoal(1, new AvoidExplosionGoal(mob, 1.3));
        }
        if (SiegeConfig.ENABLE_DODGE_ARROWS.get()) {
            goals.addGoal(2, new DodgeArrowGoal(mob, 1.2));
        }
        if (SiegeConfig.ENABLE_AMBUSH.get()) {
            goals.addGoal(3, new AmbushGoal(mob));
        }
        if (SiegeConfig.ENABLE_DOOR_BREAKING.get()) {
            goals.addGoal(4, new SmartBreakDoorGoal(mob, 240));
        }
        if (SiegeConfig.ENABLE_DIGGING.get()) {
            goals.addGoal(6, new DigTowardTargetGoal(mob));
        }
        if (SiegeConfig.ENABLE_PILLARING.get()) {
            goals.addGoal(7, new PillarUpGoal(mob));
        }
        if (SiegeConfig.ENABLE_PACK_TACTICS.get()) {
            targets.addGoal(1, new PackTacticsGoal(mob));
        }
        if (SiegeConfig.ENABLE_EXPANDED_SENSING.get()) {
            AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
            if (followRange != null) {
                double multiplier = SiegeConfig.SENSING_RANGE_MULTIPLIER.get();
                followRange.setBaseValue(followRange.getBaseValue() * multiplier);
            }
        }
    }
}
