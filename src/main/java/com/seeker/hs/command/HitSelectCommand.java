package com.seeker.hs.command;

import com.seeker.hs.ConfigManager;
import com.seeker.hs.Main;
import com.seeker.hs.module.HitSelect;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class HitSelectCommand extends CommandBase {

    private static final String G  = EnumChatFormatting.GREEN.toString();
    private static final String R  = EnumChatFormatting.RED.toString();
    private static final String W  = EnumChatFormatting.WHITE.toString();
    private static final String GR = EnumChatFormatting.GRAY.toString();
    private static final String A  = EnumChatFormatting.AQUA.toString();
    private static final String B  = EnumChatFormatting.BOLD.toString();

    @Override
    public String getCommandName() { return "hs"; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/hs [on|off|chance <min> <max>|range <blocks>|timeout <ms>|debug|hud|status|help]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("hitselect");
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        HitSelect hs = Main.hitSelect;

        if (args.length == 0) {
            boolean next = !hs.isEnabled();
            hs.setEnabled(next);
            send(sender, next ? G + "HitSelect " + B + "enabled" : R + "HitSelect " + B + "disabled");
            save();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "on":
                hs.setEnabled(true);
                send(sender, G + "HitSelect " + B + "enabled");
                save();
                break;

            case "off":
                hs.setEnabled(false);
                send(sender, R + "HitSelect " + B + "disabled");
                save();
                break;

            case "chance":
                if (args.length < 2) {
                    send(sender, GR + "Chance band: " + W + chanceBand(hs));
                    break;
                }
                // /hs chance <min> <max>, or /hs chance <value> for a fixed point.
                double lo = parseNum(args[1]);
                double hi = args.length >= 3 ? parseNum(args[2]) : lo;
                hs.setChanceRange(lo / 100.0, hi / 100.0);
                send(sender, GR + "Chance band set to " + W + chanceBand(hs));
                save();
                break;

            case "range":
                if (args.length < 2) {
                    send(sender, GR + "Detection range: " + W + hs.getDetectRange() + " blocks");
                    break;
                }
                hs.setDetectRange(parseNum(args[1]));
                send(sender, GR + "Detection range: " + W + hs.getDetectRange() + " blocks");
                save();
                break;

            case "timeout":
                if (args.length < 2) {
                    send(sender, GR + "Timeout: " + W + hs.getTimeoutMs() + " ms");
                    break;
                }
                hs.setTimeoutMs((long) parseNum(args[1]));
                send(sender, GR + "Timeout: " + W + hs.getTimeoutMs() + " ms");
                save();
                break;

            case "debug":
                boolean dbg = !hs.isDebug();
                hs.setDebug(dbg);
                send(sender, GR + "Debug: " + (dbg ? G + "on" : R + "off")
                        + GR + " (announces triggers/releases)");
                save();
                break;

            case "hud":
                boolean hudOn = !Main.hud.isVisible();
                Main.hud.setVisible(hudOn);
                send(sender, GR + "HUD: " + (hudOn ? G + "visible" : R + "hidden"));
                save();
                break;

            case "status":
                send(sender, A + B + "--- HitSelect status ---");
                send(sender, GR + "Enabled:  " + (hs.isEnabled() ? G + "yes" : R + "no"));
                send(sender, GR + "Chance:   " + W + chanceBand(hs));
                send(sender, GR + "Range:    " + W + hs.getDetectRange() + " blocks");
                send(sender, GR + "Timeout:  " + W + hs.getTimeoutMs() + " ms");
                send(sender, GR + "HUD:      " + (Main.hud.isVisible() ? G + "visible" : R + "hidden"));
                send(sender, GR + "Debug:    " + (hs.isDebug() ? G + "on" : R + "off"));
                break;

            case "help":
                send(sender, A + B + "--- HitSelect commands ---");
                send(sender, W + "/hs" + GR + " - toggle on/off");
                send(sender, W + "/hs on|off" + GR + " - enable or disable");
                send(sender, W + "/hs chance <min> <max>" + GR + " - engage-chance band (0-100); rolled per hit");
                send(sender, W + "/hs range <blocks>" + GR + " - how close an enemy must be (1-8)");
                send(sender, W + "/hs timeout <ms>" + GR + " - safety release if you never land");
                send(sender, W + "/hs debug" + GR + " - announce when hitselect triggers/releases");
                send(sender, W + "/hs hud" + GR + " - toggle the HUD overlay");
                send(sender, W + "/hs status" + GR + " - show current settings");
                send(sender, W + "/hs help" + GR + " - show this message");
                break;

            default:
                send(sender, R + "Unknown subcommand. Use " + W + "/hs help" + R + " for a list.");
        }
    }

    private static void save() {
        ConfigManager.save(Main.hitSelect, Main.hud);
    }

    private static int pct(double chance01) {
        return (int) Math.round(chance01 * 100);
    }

    private static String chanceBand(HitSelect hs) {
        int lo = pct(hs.getChanceMin());
        int hi = pct(hs.getChanceMax());
        return lo == hi ? lo + "%" : lo + "-" + hi + "%";
    }

    private static void send(ICommandSender sender, String msg) {
        sender.addChatMessage(new ChatComponentText(msg));
    }

    private static double parseNum(String s) throws CommandException {
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { throw new CommandException("'" + s + "' is not a number."); }
    }
}
