package com.seeker.ab.command;

import com.seeker.ab.ConfigManager;
import com.seeker.ab.Main;
import com.seeker.ab.module.AutoBlock;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class AutoBlockCommand extends CommandBase {

    private static final String G  = EnumChatFormatting.GREEN.toString();
    private static final String R  = EnumChatFormatting.RED.toString();
    private static final String W  = EnumChatFormatting.WHITE.toString();
    private static final String GR = EnumChatFormatting.GRAY.toString();
    private static final String B  = EnumChatFormatting.BOLD.toString();

    @Override
    public String getCommandName() { return "ab"; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ab [on|off|delay <min> <max>|duration <min> <max>|miss <pct>|hp <1-20>|hud|status]";
    }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        AutoBlock ab = Main.autoBlock;

        if (args.length == 0) {
            boolean next = !ab.isEnabled();
            ab.setEnabled(next);
            send(sender, next ? G + "Autoblock " + B + "enabled" : R + "Autoblock " + B + "disabled");
            ConfigManager.save(ab, Main.hud);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "on":
                ab.setEnabled(true);
                send(sender, G + "Autoblock " + B + "enabled");
                ConfigManager.save(ab, Main.hud);
                break;

            case "off":
                ab.setEnabled(false);
                send(sender, R + "Autoblock " + B + "disabled");
                ConfigManager.save(ab, Main.hud);
                break;

            case "delay":
                requireArgs(args, 3);
                ab.setMinDelay(CommandBase.parseInt(args[1]));
                ab.setMaxDelay(CommandBase.parseInt(args[2]));
                send(sender, GR + "Reaction delay: " + W + ab.getMinDelay() + "-" + ab.getMaxDelay() + " ms");
                ConfigManager.save(ab, Main.hud);
                break;

            case "duration":
                requireArgs(args, 3);
                ab.setMinDuration(CommandBase.parseInt(args[1]));
                ab.setMaxDuration(CommandBase.parseInt(args[2]));
                send(sender, GR + "Block duration: " + W + ab.getMinDuration() + "-" + ab.getMaxDuration() + " ms");
                ConfigManager.save(ab, Main.hud);
                break;

            case "miss":
                requireArgs(args, 2);
                double pct = Double.parseDouble(args[1]);
                ab.setMissChance(pct / 100.0);
                send(sender, GR + "Miss chance: " + W + pct + "%");
                ConfigManager.save(ab, Main.hud);
                break;

            case "debug":
                boolean dbg = !ab.isDebugMode();
                ab.setDebugMode(dbg);
                send(sender, GR + "Debug: " + (dbg ? G + "on" : R + "off"));
                ConfigManager.save(ab, Main.hud);
                break;

            case "hud":
                boolean hudOn = !Main.hud.isVisible();
                Main.hud.setVisible(hudOn);
                send(sender, GR + "HUD: " + (hudOn ? G + "visible" : R + "hidden"));
                ConfigManager.save(ab, Main.hud);
                break;

            case "hp":
                requireArgs(args, 2);
                float hp = parseFloat(args[1]);
                ab.setHpThreshold(hp);
                send(sender, GR + "HP threshold: " + W + ab.getHpThreshold() + GR + " (activates at <=" + ab.getHpThreshold() + " HP)");
                ConfigManager.save(ab, Main.hud);
                break;

            case "status":
                send(sender, GR + "--- Autoblock status ---");
                send(sender, GR + "Enabled:   " + (ab.isEnabled() ? G + "yes" : R + "no"));
                send(sender, GR + "Delay:     " + W + ab.getMinDelay() + "-" + ab.getMaxDelay() + " ms");
                send(sender, GR + "Duration:  " + W + ab.getMinDuration() + "-" + ab.getMaxDuration() + " ms");
                send(sender, GR + "Miss:      " + W + String.format("%.1f", ab.getMissChance() * 100) + "%");
                send(sender, GR + "HP gate:   " + W + "<=" + ab.getHpThreshold() + " HP");
                send(sender, GR + "HUD:       " + (Main.hud.isVisible() ? G + "visible" : R + "hidden"));
                send(sender, GR + "Debug:     " + (ab.isDebugMode() ? G + "on" : R + "off"));
                break;

            case "help":
                send(sender, GR + "--- Autoblock commands ---");
                send(sender, W + "/ab" + GR + " - toggle on/off");
                send(sender, W + "/ab on|off" + GR + " - enable or disable");
                send(sender, W + "/ab delay <min> <max>" + GR + " - reaction delay in ms");
                send(sender, W + "/ab duration <min> <max>" + GR + " - block hold time in ms");
                send(sender, W + "/ab miss <pct>" + GR + " - miss chance 0-100");
                send(sender, W + "/ab hp <1-20>" + GR + " - only activate at or below this HP");
                send(sender, W + "/ab hud" + GR + " - toggle HUD overlay");
                send(sender, W + "/ab debug" + GR + " - toggle damage reduction debug messages");
                send(sender, W + "/ab status" + GR + " - show current settings");
                send(sender, W + "/ab help" + GR + " - show this message");
                break;

            default:
                send(sender, R + "Unknown subcommand. Use " + W + "/ab help" + R + " for a list.");
        }
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("autoblock");
    }

    private static void send(ICommandSender sender, String msg) {
        sender.addChatMessage(new ChatComponentText(msg));
    }

    private static void requireArgs(String[] args, int needed) throws CommandException {
        if (args.length < needed)
            throw new CommandException("Not enough arguments.");
    }

    private static float parseFloat(String s) throws CommandException {
        try { return Float.parseFloat(s); }
        catch (NumberFormatException e) { throw new CommandException("'" + s + "' is not a number."); }
    }
}
