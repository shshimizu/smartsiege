package com.smartsiege.mod.ai;

import com.smartsiege.mod.config.SiegeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class DigTowardTargetGoal extends Goal {

    private final Mob mob;
    private BlockPos digTarget;
    private int digTicks;

    public DigTowardTargetGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (SiegeConfig.REQUIRE_MOB_GRIEFING.get()
            && !mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (!mob.getNavigation().isDone()) return false;
        double dist = mob.distanceToSqr(target);
        if (dist > 36.0 || dist < 4.0) return false;
        if (Math.abs(mob.getY() - target.getY()) > 2.0) return false;

        BlockPos candidate = findDiggableBlockToward(target);
        if (candidate == null) return false;

        this.digTarget = candidate;
        return true;
    }

    private BlockPos findDiggableBlockToward(LivingEntity target) {
        Direction dir = Direction.getNearest(
            target.getX() - mob.getX(), 0, target.getZ() - mob.getZ());
        BlockPos mobPos = mob.blockPosition();

        for (int i = 1; i <= 2; i++) {
            BlockPos check = mobPos.relative(dir, i);
            BlockState state = mob.level().getBlockState(check);
            if (isDiggable(state)) {
                return check;
            }
        }
        return null;
    }

    private boolean isDiggable(BlockState state) {
        if (state.isAir()) return false;
        float hardness = state.getDestroySpeed(mob.level(), BlockPos.ZERO);
        if (hardness < 0) return false;
        return hardness * 10 <= SiegeConfig.DIG_HARDNESS_LIMIT.get();
    }

    @Override
    public boolean canContinueToUse() {
        if (digTarget == null) return false;
        BlockState state = mob.level().getBlockState(digTarget);
        return !state.isAir() && digTicks < SiegeConfig.DIG_TICKS.get();
    }

    @Override
    public void start() {
        digTicks = 0;
    }

    @Override
    public void tick() {
        if (digTarget == null) return;
        digTicks++;
        mob.getLookControl().setLookAt(
            digTarget.getX() + 0.5, digTarget.getY() + 0.5, digTarget.getZ() + 0.5);

        if (mob.level() instanceof ServerLevel serverLevel) {
            int maxTicks = SiegeConfig.DIG_TICKS.get();
            int progress = Math.min(9, (digTicks * 10) / maxTicks);
            serverLevel.destroyBlockProgress(mob.getId(), digTarget, progress);

            if (digTicks >= maxTicks) {
                serverLevel.destroyBlockProgress(mob.getId(), digTarget, -1);
                serverLevel.destroyBlock(digTarget, false, mob);
            }
        }
    }

    @Override
    public void stop() {
        if (mob.level() instanceof ServerLevel serverLevel && digTarget != null) {
            serverLevel.destroyBlockProgress(mob.getId(), digTarget, -1);
        }
        digTarget = null;
    }
}
