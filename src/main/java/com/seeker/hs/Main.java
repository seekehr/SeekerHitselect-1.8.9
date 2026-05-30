package com.seeker.hs;

import com.seeker.hs.command.HitSelectCommand;
import com.seeker.hs.module.HitSelect;
import com.seeker.hs.render.HudRenderer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Main.MODID, version = Main.VERSION, name = Main.NAME, clientSideOnly = true)
public class Main {

    public static final String MODID   = "hitselect";
    public static final String VERSION = "1.0";
    public static final String NAME    = "HitSelect";

    public static HitSelect   hitSelect;
    public static HudRenderer hud;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        hitSelect = new HitSelect();
        hud       = new HudRenderer();

        ConfigManager.load(hitSelect, hud);

        MinecraftForge.EVENT_BUS.register(hitSelect);
        MinecraftForge.EVENT_BUS.register(hud);
        ClientCommandHandler.instance.registerCommand(new HitSelectCommand());
    }
}
