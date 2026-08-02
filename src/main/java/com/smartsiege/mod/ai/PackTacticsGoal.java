package com.smartsiege.mod.ai;

import com.smartsiege.mod.config.SiegeConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

/**
 * When a mob acquires a target, it "shouts" to nearby mobs of the same class
 * so the whole group converges on the player at once instead of trickling in
 * one at a time.
 */
public class PackTacticsGoal extends Goal {

    private final Mob mob;
    private LivingEntity lastAlertedTarget;

    public PackTacticsGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && target != lastAlertedTarget;
    }

    @Override
    public boolean canContinueToUse() {
        return false; // fires once per new target, see start()
    }

    @Override
    public void start() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        lastAlertedTarget = target;

        double radius = SiegeConfig.ALERT_RADIUS.get();
        List<? extends Mob> allies = mob.level().getEntitiesOfClass(
            mob.getClass(), mob.getBoundingBox().inflate(radius));

        for (Mob ally : allies) {
            if (ally == mob) continue;
            if (ally.getTarget() == null) {
                ally.setTarget(target);
            }
        }
    }
}
