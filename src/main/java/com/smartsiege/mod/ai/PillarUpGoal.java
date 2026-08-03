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

public class PillarUpGoal extends Goal {

    private final Mob mob;
    private int placeCooldown;
    private int blocksPlaced;

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

        return heightDiff > 2.5 && heightDiff < SiegeConfig.PILLAR_MAX_HEIGHT.get() + 1
            && horizontalDistSqr < 4.0
            && mob.getNavigation().isDone();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive()
            && target.getY() - mob.getY() > 1.0
            && blocksPlaced < SiegeConfig.PILLAR_MAX_HEIGHT.get();
    }

    @Override
    public void start() {
        blocksPlaced = 0;
        placeCooldown = 0;
    }

    @Override
    public void tick() {
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
            placeCooldown = SiegeConfig.PILLAR_COOLDOWN_TICKS.get();
        }
    }

    @Override
    public void stop() {
        blocksPlaced = 0;
    }
}
