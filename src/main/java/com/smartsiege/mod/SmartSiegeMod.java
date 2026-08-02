package com.smartsiege.mod;

import com.smartsiege.mod.config.SiegeConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Smart Siege
 *
 * Gives hostile mobs meaningfully smarter, more dangerous AI so that
 * Survival stops being trivial once you have basic gear:
 *   - they avoid explosions and dodge arrows instead of tanking them
 *   - they break down doors, dig through soft blocks, and pillar up
 *     to reach you instead of giving up
 *   - they call nearby allies of their own kind into the fight
 *   - they lurk quietly in the dark and ambush rather than beeline at you
 *     from render distance
 *
 * Everything is toggleable per-feature via config (see SiegeConfig).
 */
@Mod("smartsiege")
public class SmartSiegeMod {

    public static final Logger LOGGER = LogUtils.getLogger();

    public SmartSiegeMod() {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
            ModConfig.Type.SERVER, SiegeConfig.SPEC);

        LOGGER.info("Smart Siege loaded: hostile mobs are about to get a lot smarter.");
    }
}
