package com.smartsiege.mod.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Mobs watch for incoming arrows and sidestep at the last moment,
 * instead of tanking every shot like a vanilla mob would.
 */
public class DodgeArrowGoal extends Goal {

    private final PathfinderMob mob;
    private final double speedModifier;
    private static final double DETECTION_RADIUS = 8.0;
    private int dodgeCooldown = 0;

    public DodgeArrowGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private AbstractArrow findIncomingArrow() {
        List<AbstractArrow> arrows = mob.level().getEntitiesOfClass(
            AbstractArrow.class, mob.getBoundingBox().inflate(DETECTION_RADIUS));
        for (AbstractArrow arrow : arrows) {
            if (arrow.getDeltaMovement().lengthSqr() < 0.01) continue;

            Vec3 toMob = mob.position().subtract(arrow.position());
            double distance = toMob.length();
            if (distance > DETECTION_RADIUS) continue;

            Vec3 arrowDir = arrow.getDeltaMovement().normalize();
            Vec3 towardMobDir = toMob.normalize();
            double alignment = arrowDir.dot(towardMobDir);
            if (alignment > 0.85 && distance < 6.0) {
                return arrow;
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        if (dodgeCooldown > 0) {
            dodgeCooldown--;
            return false;
        }
        return findIncomingArrow() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        AbstractArrow arrow = findIncomingArrow();
        if (arrow == null) return;

        Vec3 arrowDir = arrow.getDeltaMovement().normalize();
        Vec3 perpendicular = new Vec3(-arrowDir.z, 0, arrowDir.x).normalize();
        if (mob.getRandom().nextBoolean()) {
            perpendicular = perpendicular.scale(-1);
        }

        Vec3 dodgePos = mob.position().add(perpendicular.scale(2.5));
        mob.getNavigation().moveTo(dodgePos.x, dodgePos.y, dodgePos.z, speedModifier);
        dodgeCooldown = 15;
    }
}
