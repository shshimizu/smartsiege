package com.smartsiege.mod.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Mobs actively run away from primed TNT and swelling creepers instead of
 * standing in the blast like vanilla mobs do. Self-preservation, not suicide.
 */
public class AvoidExplosionGoal extends Goal {

    private final PathfinderMob mob;
    private final double speedModifier;
    private Vec3 fleeTarget;
    private static final double DANGER_RADIUS = 6.0;

    public AvoidExplosionGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private Entity findDanger() {
        List<Entity> nearby = mob.level().getEntities(mob, mob.getBoundingBox().inflate(DANGER_RADIUS));
        for (Entity e : nearby) {
            if (e instanceof PrimedTnt tnt && tnt.getFuse() < 40) {
                return tnt;
            }
            if (e instanceof Creeper creeper && creeper.getSwellDir() > 0) {
                return creeper;
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        Entity danger = findDanger();
        if (danger == null) return false;

        Vec3 away = mob.position().subtract(danger.position()).normalize();
        Vec3 fleePos = mob.position().add(away.scale(8.0));
        this.fleeTarget = DefaultRandomPos.getPosAway(mob, 8, 6,
            new Vec3(fleePos.x, mob.getY(), fleePos.z));
        return this.fleeTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone() && findDanger() != null;
    }

    @Override
    public void start() {
        if (fleeTarget != null) {
            mob.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, speedModifier);
        }
    }

    @Override
    public void stop() {
        fleeTarget = null;
    }
}
