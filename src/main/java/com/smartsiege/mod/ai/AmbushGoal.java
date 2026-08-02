package com.smartsiege.mod.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Instead of wandering around in the open (making noise/footsteps and
 * revealing its position), a mob with no current target that is standing
 * in low light will hold still and wait. As soon as a player gets close
 * enough, this goal backs off and lets the mob's normal targeting/attack
 * goals take over, resulting in a "jump-scare" ambush rather than the
 * player spotting it wandering from a distance.
 */
public class AmbushGoal extends Goal {

    private final Mob mob;
    private static final int HIDDEN_LIGHT_LEVEL = 7;
    private static final double TRIGGER_RADIUS = 6.0;

    public AmbushGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() != null) return false;

        BlockPos pos = mob.blockPosition();
        int light = mob.level().getMaxLocalRawBrightness(pos);
        if (light > HIDDEN_LIGHT_LEVEL) return false;

        Player nearest = mob.level().getNearestPlayer(mob, 32.0);
        if (nearest == null) return false;

        double dist = mob.distanceTo(nearest);
        // Stay still only while the player is still far enough away;
        // once they close in, stand down so attack goals engage.
        return dist > TRIGGER_RADIUS;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        // Deliberately do nothing: holding position is the point.
        mob.getNavigation().stop();
    }
}
