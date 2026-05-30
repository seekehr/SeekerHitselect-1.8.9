package com.seeker.hs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seeker.hs.module.HitSelect;
import com.seeker.hs.render.HudRenderer;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File configFile() {
        return new File(Minecraft.getMinecraft().mcDataDir, "config/hitselect.json");
    }

    public static void load(HitSelect hs, HudRenderer hud) {
        File f = configFile();
        if (!f.exists()) return;
        try (Reader r = new FileReader(f)) {
            Config cfg = GSON.fromJson(r, Config.class);
            if (cfg == null) return;
            hs.setEnabled(cfg.enabled);
            hs.setDebug(cfg.debug);
            hs.setChanceRange(cfg.chanceMin, cfg.chanceMax);
            hs.setDetectRange(cfg.detectRange);
            hs.setTimeoutMs(cfg.timeoutMs);
            hud.setVisible(cfg.hudVisible);
        } catch (Exception ignored) {}
    }

    public static void save(HitSelect hs, HudRenderer hud) {
        File f = configFile();
        f.getParentFile().mkdirs();
        Config cfg        = new Config();
        cfg.enabled       = hs.isEnabled();
        cfg.debug         = hs.isDebug();
        cfg.chanceMin     = hs.getChanceMin();
        cfg.chanceMax     = hs.getChanceMax();
        cfg.detectRange   = hs.getDetectRange();
        cfg.timeoutMs     = hs.getTimeoutMs();
        cfg.hudVisible    = hud.isVisible();
        try (Writer w = new FileWriter(f)) {
            GSON.toJson(cfg, w);
        } catch (Exception ignored) {}
    }

    private static class Config {
        boolean enabled       = true;
        boolean debug         = false;
        double  chanceMin     = 0.40;
        double  chanceMax     = 0.70;
        double  detectRange   = 6.0;
        long    timeoutMs     = 1500;
        boolean hudVisible    = true;
    }
}
