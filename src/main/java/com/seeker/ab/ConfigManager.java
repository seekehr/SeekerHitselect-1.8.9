package com.seeker.ab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seeker.ab.module.AutoBlock;
import com.seeker.ab.render.HudRenderer;
import net.minecraft.client.Minecraft;

import java.io.*;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File configFile() {
        return new File(Minecraft.getMinecraft().mcDataDir, "config/autoblock.json");
    }

    public static void load(AutoBlock ab, HudRenderer hud) {
        File f = configFile();
        if (!f.exists()) return;
        try (Reader r = new FileReader(f)) {
            Config cfg = GSON.fromJson(r, Config.class);
            if (cfg == null) return;
            ab.setEnabled(cfg.enabled);
            ab.setDebugMode(cfg.debugMode);
            ab.setMinDelay(cfg.minDelay);
            ab.setMaxDelay(cfg.maxDelay);
            ab.setMinDuration(cfg.minDuration);
            ab.setMaxDuration(cfg.maxDuration);
            ab.setMissChance(cfg.missChance);
            ab.setHpThreshold(cfg.hpThreshold);
            hud.setVisible(cfg.hudVisible);
        } catch (Exception ignored) {}
    }

    public static void save(AutoBlock ab, HudRenderer hud) {
        File f = configFile();
        f.getParentFile().mkdirs();
        Config cfg       = new Config();
        cfg.enabled      = ab.isEnabled();
        cfg.debugMode    = ab.isDebugMode();
        cfg.minDelay     = ab.getMinDelay();
        cfg.maxDelay     = ab.getMaxDelay();
        cfg.minDuration  = ab.getMinDuration();
        cfg.maxDuration  = ab.getMaxDuration();
        cfg.missChance   = ab.getMissChance();
        cfg.hpThreshold  = ab.getHpThreshold();
        cfg.hudVisible   = hud.isVisible();
        try (Writer w = new FileWriter(f)) {
            GSON.toJson(cfg, w);
        } catch (Exception ignored) {}
    }

    private static class Config {
        boolean enabled     = true;
        boolean debugMode   = false;
        int     minDelay    = 0;
        int     maxDelay    = 80;
        int     minDuration = 120;
        int     maxDuration = 220;
        double  missChance  = 0.04;
        float   hpThreshold = 10f;
        boolean hudVisible  = true;
    }
}
