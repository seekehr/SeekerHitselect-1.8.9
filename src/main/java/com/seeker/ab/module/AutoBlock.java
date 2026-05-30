package com.seeker.ab.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class AutoBlock {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random rng = new Random();

    private boolean enabled   = true;
    private boolean blocking  = false;
    private boolean debugMode = false;

    // Configurable timing (ms)
    private int minDelay    = 0;
    private int maxDelay    = 80;
    private int minDuration = 120;
    private int maxDuration = 220;

    // Miss chance (0.0–1.0)
    private double missChance = 0.04;

    // HP threshold (1–20)
    private float hpThreshold = 10f;

    private long blockAt   = -1;
    private long unblockAt = -1;

    // Hit detection state
    private int   lastHurtTime = 0;
    private float lastHealth   = 20f;

    // --- Tick handler ---

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        float currentHealth = mc.thePlayer.getHealth();
        int   hurtTime      = mc.thePlayer.hurtTime;
        boolean justHit     = lastHurtTime == 0 && hurtTime > 0;
        boolean wasBlocking = blocking;

        if (justHit && debugMode && currentHealth <= hpThreshold) {
            float dmg = lastHealth - currentHealth;
            if (dmg > 0) {
                String label = wasBlocking ? "Damage reduced by " : "Damage reduced from blocking: ";
                sendDebug(label + String.format("%.1f", dmg) + " HP");
            }
        }

        lastHurtTime = hurtTime;
        lastHealth   = currentHealth;

        if (!enabled) return;

        // New hit — try to schedule a block for the next one
        if (justHit && !blocking) {
            tryScheduleBlock();
        }

        long now = System.currentTimeMillis();

        if (!blocking && blockAt != -1 && now >= blockAt) {
            startBlocking();
            blockAt = -1;
        }

        if (blocking && unblockAt != -1 && now >= unblockAt) {
            stopBlocking();
            unblockAt = -1;
        }
    }

    // --- Internal ---

    private void tryScheduleBlock() {
        if (!isHoldingSword()) return;
        if (mc.thePlayer.getHealth() > hpThreshold) return;
        if (rng.nextDouble() < missChance) return;

        long now   = System.currentTimeMillis();
        long delay = minDelay + (long)(rng.nextDouble() * (maxDelay - minDelay));
        long dur   = minDuration + (long)(rng.nextDouble() * (maxDuration - minDuration));

        blockAt   = now + delay;
        unblockAt = blockAt + dur;
    }

    private void startBlocking() {
        if (!isHoldingSword()) {
            cancelPending();
            return;
        }
        int key = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(key, true);
        KeyBinding.onTick(key);
        blocking = true;
    }

    private void stopBlocking() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        blocking = false;
    }

    private void cancelPending() {
        blockAt   = -1;
        unblockAt = -1;
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    private void sendDebug(String msg) {
        if (mc.ingameGUI == null) return;
        String line = EnumChatFormatting.YELLOW + "[AB DEBUG] "
                    + EnumChatFormatting.WHITE  + msg;
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(line));
    }

    // --- Public API ---

    public boolean isEnabled()   { return enabled; }
    public boolean isBlocking()  { return blocking; }
    public boolean isDebugMode() { return debugMode; }

    public void setEnabled(boolean v) {
        this.enabled = v;
        if (!v && blocking) {
            stopBlocking();
            cancelPending();
        }
    }

    public void setDebugMode(boolean v) { debugMode = v; }

    public int    getMinDelay()    { return minDelay; }
    public int    getMaxDelay()    { return maxDelay; }
    public int    getMinDuration() { return minDuration; }
    public int    getMaxDuration() { return maxDuration; }
    public double getMissChance()  { return missChance; }
    public float  getHpThreshold() { return hpThreshold; }

    public void setMinDelay(int v)     { minDelay    = Math.max(0, v); }
    public void setMaxDelay(int v)     { maxDelay    = Math.max(minDelay, v); }
    public void setMinDuration(int v)  { minDuration = Math.max(0, v); }
    public void setMaxDuration(int v)  { maxDuration = Math.max(minDuration, v); }
    public void setMissChance(double v){ missChance  = Math.max(0.0, Math.min(1.0, v)); }
    public void setHpThreshold(float v){ hpThreshold = Math.max(1f, Math.min(20f, v)); }
}
