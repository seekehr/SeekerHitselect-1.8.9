package com.seeker.hs.render;

import com.seeker.hs.Main;
import com.seeker.hs.module.HitSelect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HudRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean visible = true;

    public boolean isVisible()        { return visible; }
    public void setVisible(boolean v) { visible = v; }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!visible) return;
        if (mc.thePlayer == null || mc.gameSettings.showDebugInfo) return;

        HitSelect hs = Main.hitSelect;
        if (hs == null) return;

        FontRenderer fr = mc.fontRendererObj;

        String color = !hs.isEnabled()  ? EnumChatFormatting.RED.toString()
                     : hs.isSelecting() ? EnumChatFormatting.YELLOW.toString()
                                        : EnumChatFormatting.GREEN.toString();

        int lo = (int) Math.round(hs.getChanceMin() * 100);
        int hi = (int) Math.round(hs.getChanceMax() * 100);
        String band = lo == hi ? lo + "%" : lo + "-" + hi + "%";

        String title  = "HitSelect " + color + "[" + hs.getStateLabel() + "]";
        String chance = EnumChatFormatting.GRAY + "Chance: " + EnumChatFormatting.WHITE + band;

        fr.drawStringWithShadow(title, 2, 2, 0xFFFFFF);
        fr.drawStringWithShadow(chance, 2, 12, 0xFFFFFF);
    }
}
