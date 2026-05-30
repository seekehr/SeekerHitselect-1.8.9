package com.seeker.ab.render;

import com.seeker.ab.Main;
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

        FontRenderer fr = mc.fontRendererObj;
        boolean on      = Main.autoBlock.isEnabled();
        boolean blocking = Main.autoBlock.isBlocking();

        String color = blocking ? EnumChatFormatting.YELLOW.toString()
                     : on      ? EnumChatFormatting.GREEN.toString()
                                : EnumChatFormatting.RED.toString();
        String state = blocking ? "[BLOCK]" : on ? "ON" : "OFF";

        fr.drawStringWithShadow("AB: " + color + state, 2, 2, 0xFFFFFF);
    }
}
