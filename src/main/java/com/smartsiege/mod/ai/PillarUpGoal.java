package com.smartsiege.mod.ai;

import com.smartsiege.mod.config.SiegeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * When the target is well above the mob and unreachable by normal pathing
 * (e.g. player towered up on a 1x1 pillar), the mob stacks blocks beneath
 * itself and jumps to climb up, mimicking the classic player "tower defense"
 * counter-strategy.
 */
public class PillarUpGoal extends Goal {

    private final Mob mob;
    private int placeCooldown;
    private int blocksPlaced;
    private static final int MAX_HEIGHT = 6;

    public PillarUpGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (SiegeConfig.REQUIRE_MOB_GRIEFING.get()
            && !mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        double heightDiff = target.getY() - mob.getY();
        double horizontalDistSqr =
            (target.getX() - mob.getX()) * (target.getX() - mob.getX())
                + (target.getZ() - mob.getZ()) * (target.getZ() - mob.getZ());

        // Target is significantly higher and close horizontally (i.e. towered up).
        return heightDiff > 2.5 && heightDiff < 12.0 && horizontalDistSqr < 4.0
            && mob.getNavigation().isDone();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive()
            && target.getY() - mob.getY() > 1.0
            && blocksPlaced < MAX_HEIGHT;
    }

    @Override
    public void start() {
        blocksPlaced = 0;
        placeCooldown = 0;
    }

    @Override
    public void tick() {
        // Always jump while this goal is active; combined with placing a
        // block underfoot mid-air, this lets the mob climb straight up.
        mob.getJumpControl().jump();

        if (placeCooldown > 0) {
            placeCooldown--;
            return;
        }

        BlockPos belowPos = mob.blockPosition().below();
        BlockState below = mob.level().getBlockState(belowPos);
        if (below.isAir() && mob.getDeltaMovement().y > 0.05 && !mob.onGround()) {
            mob.level().setBlockAndUpdate(belowPos, Blocks.COBBLESTONE.defaultBlockState());
            blocksPlaced++;
            placeCooldown = 10;
        }
    }

    @Override
    public void stop() {
        blocksPlaced = 0;
    }
}
