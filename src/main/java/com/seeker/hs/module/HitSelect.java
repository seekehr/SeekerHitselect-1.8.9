package com.seeker.hs.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * HitSelect: a combo-timing assist for 1.8 PvP.
 *
 * It does NOT attack for the player. When the player is hit by a nearby enemy
 * and knocked airborne, the mod force-stops the player's own attacks for the
 * duration of the air arc (blocking the premature "first hit"), then releases
 * the moment the player lands on the ground so the player's own click lands the
 * grounded, combo-starting hit ("selecting" the right hit).
 *
 * Engaging is gated by a jittered random chance band plus smart detection,
 * so it does not happen on every exchange.
 */
@SideOnly(Side.CLIENT)
public class HitSelect {

    private final Minecraft mc  = Minecraft.getMinecraft();
    private final Random    rng = new Random();

    // --- Settings (persisted) ---
    private boolean enabled       = true;
    private boolean debug         = false;
    // Engage chance is a jittered band: each exchange picks a random % between
    // chanceMin and chanceMax, then rolls against it. Keeps the rate irregular.
    private double  chanceMin     = 0.40;  // 0.0 - 1.0
    private double  chanceMax     = 0.70;  // 0.0 - 1.0
    private double  detectRange   = 6.0;   // blocks: how close an enemy must be
    private long    timeoutMs     = 1500;  // safety release if we never land

    // --- Runtime state ---
    private boolean      selecting     = false;
    private boolean      airbornePhase = false;
    private long         selectStart   = 0;
    private long         lastReleaseAt = 0;
    private EntityPlayer target        = null;
    private int          lastHurtTime  = 0;

    private static final long   COOLDOWN_MS    = 400;  // gap after a release before re-arming
    private static final double KNOCKBACK_LIFT = 0.10; // min upward motion proving a knockback launch

    // Minecraft.leftClickCounter is private; clickMouse() no-ops while it is > 0,
    // which blocks both the attack and the held-click swing animation. Accessed by
    // dev name (leftClickCounter) and obfuscated SRG name (field_71429_W).
    private static final Field LEFT_CLICK_COUNTER =
            ReflectionHelper.findField(Minecraft.class, "leftClickCounter", "field_71429_W");

    // --- Tick / state machine ---

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        EntityPlayer player = mc.thePlayer;
        if (player == null || mc.theWorld == null) {
            reset();
            return;
        }

        int hurt        = player.hurtTime;
        boolean justHurt = lastHurtTime == 0 && hurt > 0;
        lastHurtTime    = hurt;

        if (!enabled) {
            if (selecting) stopSelecting(null);
            return;
        }

        long now = System.currentTimeMillis();

        if (selecting) {
            if (!player.onGround) airbornePhase = true;

            boolean landed     = airbornePhase && player.onGround;
            boolean expired    = now - selectStart > timeoutMs;
            boolean lostTarget = target == null || target.isDead
                                 || player.getDistanceToEntity(target) > detectRange + 2.0;

            if (landed) {
                // Released the instant we touch ground — let this click through.
                stopSelecting("landed");
            } else if (expired) {
                stopSelecting("timeout");
            } else if (lostTarget) {
                stopSelecting("target lost");
            } else {
                // Still in the air arc: force-stop any attack the player throws.
                suppressAttack();
            }
            return;
        }

        // --- Idle: decide whether to engage ---
        if (now - lastReleaseAt < COOLDOWN_MS) return;
        if (!justHurt) return;
        if (!isBeneficial(player)) return;

        EntityPlayer enemy = nearestEnemy(player);
        if (enemy == null) return;

        // Smart-detection gate passed -> pick a jittered chance in the band, then roll.
        double effective = chanceMin + rng.nextDouble() * (chanceMax - chanceMin);
        if (rng.nextDouble() < effective) {
            selecting     = true;
            airbornePhase = false;
            selectStart   = now;
            target        = enemy;
            if (debug) sendDebug(EnumChatFormatting.GREEN + "Hit-selecting"
                    + EnumChatFormatting.GRAY + " - holding your hit until you land");
        }
    }

    /**
     * Smart detection: hit selecting only helps when the incoming hit actually
     * launched you into a knockback arc. Landing your own hit from the ground
     * (after the arc) gives better, sprint-resettable knockback and throws off
     * the opponent's timing — the basis of a combo. If the hit left you grounded
     * (chip damage, projectile, fall), there is no arc to exploit, so skip it.
     */
    private boolean isBeneficial(EntityPlayer player) {
        return player.motionY > KNOCKBACK_LIFT;
    }

    /**
     * Backstop: if a click slips through input draining on an off-ordered tick,
     * cancel the attack so it deals no local damage/knockback.
     */
    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (!enabled || !selecting) return;
        if (event.entityPlayer == mc.thePlayer) {
            event.setCanceled(true);
        }
    }

    // --- Internal ---

    /**
     * Fully blocks the player's attack while airborne — including the swing
     * animation when left-click is held down.
     *
     * Draining keyBindAttack clears queued taps so vanilla never calls
     * clickMouse() for them. But a *held* button is handled separately by
     * Minecraft.runTick(), which still swings and attacks every tick. Keeping
     * leftClickCounter positive makes runTick()/clickMouse() skip the attack
     * branch entirely, so neither the hit nor the arm-swing animation happens.
     */
    private void suppressAttack() {
        KeyBinding attack = mc.gameSettings.keyBindAttack;
        while (attack.isPressed()) {
            // isPressed() decrements the internal press counter on each call
        }
        setLeftClickCounter(2); // > 0 for the next couple of ticks; topped up each tick
    }

    private void setLeftClickCounter(int value) {
        try {
            LEFT_CLICK_COUNTER.setInt(mc, value);
        } catch (Exception ignored) {}
    }

    private void stopSelecting(String reason) {
        selecting     = false;
        airbornePhase = false;
        target        = null;
        lastReleaseAt = System.currentTimeMillis();
        setLeftClickCounter(0); // re-enable clicks immediately so your ground hit lands
        if (reason != null && debug) {
            sendDebug(EnumChatFormatting.YELLOW + "Released"
                    + EnumChatFormatting.GRAY + " (" + reason + ") - your hit is live");
        }
    }

    private void reset() {
        selecting     = false;
        airbornePhase = false;
        target        = null;
        lastHurtTime  = 0;
    }

    private EntityPlayer nearestEnemy(EntityPlayer player) {
        EntityPlayer best = null;
        double bestDist   = Double.MAX_VALUE;
        for (Object o : mc.theWorld.playerEntities) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) o;
            if (p == player || p.isDead) continue;
            double d = player.getDistanceToEntity(p);
            if (d <= detectRange && d < bestDist) {
                bestDist = d;
                best     = p;
            }
        }
        return best;
    }

    private void sendDebug(String msg) {
        if (mc.ingameGUI == null) return;
        String line = EnumChatFormatting.AQUA + "[HS] " + EnumChatFormatting.GRAY + msg;
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(line));
    }

    // --- Public API (used by command, config, HUD) ---

    public boolean isEnabled()   { return enabled; }
    public boolean isDebug()     { return debug; }
    public boolean isSelecting() { return selecting; }

    public double  getChanceMin()     { return chanceMin; }
    public double  getChanceMax()     { return chanceMax; }
    public double  getDetectRange()   { return detectRange; }
    public long    getTimeoutMs()     { return timeoutMs; }

    public void setEnabled(boolean v) {
        this.enabled = v;
        if (!v) reset();
    }

    public void setDebug(boolean v)        { this.debug = v; }

    /** Sets the engage-chance band; order-insensitive, each end clamped to 0-1. */
    public void setChanceRange(double min, double max) {
        double lo = clamp(Math.min(min, max), 0.0, 1.0);
        double hi = clamp(Math.max(min, max), 0.0, 1.0);
        this.chanceMin = lo;
        this.chanceMax = hi;
    }

    public void setDetectRange(double v)   { this.detectRange = clamp(v, 1.0, 8.0); }
    public void setTimeoutMs(long v)       { this.timeoutMs = (long) clamp(v, 200, 5000); }

    public String getStateLabel() {
        if (!enabled) return "OFF";
        return selecting ? "SELECTING" : "ON";
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
