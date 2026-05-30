package com.seeker.ab;

import com.seeker.ab.command.AutoBlockCommand;
import com.seeker.ab.module.AutoBlock;
import com.seeker.ab.render.HudRenderer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Main.MODID, version = Main.VERSION, name = Main.NAME, clientSideOnly = true)
public class Main {

    public static final String MODID   = "togglesprint";
    public static final String VERSION = "1.0";
    public static final String NAME    = "ToggleSprint";

    public static AutoBlock autoBlock;
    public static HudRenderer hud;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        autoBlock = new AutoBlock();
        hud = new HudRenderer();
        ConfigManager.load(autoBlock, hud);
        MinecraftForge.EVENT_BUS.register(autoBlock);
        MinecraftForge.EVENT_BUS.register(hud);
        ClientCommandHandler.instance.registerCommand(new AutoBlockCommand());
    }
}
