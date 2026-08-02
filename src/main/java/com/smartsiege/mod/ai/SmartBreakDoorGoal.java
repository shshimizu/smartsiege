package com.smartsiege.mod.ai;

import com.smartsiege.mod.config.SiegeConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generalized version of vanilla's zombie-only door-breaking goal so that
 * any hostile mob can smash through a closed door standing between it and
 * its target, instead of just pacing back and forth uselessly.
 */
public class SmartBreakDoorGoal extends DoorInteractGoal {

    private int breakTicks;
    private final int breakSpeedDivisor; // lower = faster breaking
    private int lastDamageSent = -1;

    public SmartBreakDoorGoal(Mob mob, int breakSpeedDivisor) {
        super(mob);
        this.breakSpeedDivisor = breakSpeedDivisor;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) return false;
        if (SiegeConfig.REQUIRE_MOB_GRIEFING.get()
            && !mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        return isSmashable();
    }

    private boolean isSmashable() {
        BlockState state = mob.level().getBlockState(doorPos);
        return state.getBlock() instanceof DoorBlock;
    }

    @Override
    public void start() {
        super.start();
        breakTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(mob.level() instanceof ServerLevel serverLevel)) return;

        breakTicks++;
        int progress = Math.min(9, (breakTicks * 10) / breakSpeedDivisor);
        if (progress != lastDamageSent) {
            serverLevel.destroyBlockProgress(mob.getId(), doorPos, progress);
            lastDamageSent = progress;
        }
        if (breakTicks >= breakSpeedDivisor) {
            serverLevel.destroyBlockProgress(mob.getId(), doorPos, -1);
            serverLevel.destroyBlock(doorPos, false, mob);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.destroyBlockProgress(mob.getId(), doorPos, -1);
        }
    }
}
