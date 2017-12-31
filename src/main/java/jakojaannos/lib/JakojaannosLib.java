package jakojaannos.lib;

import jakojaannos.api.mod.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModInfo.MODID, name = ModInfo.NAME, version = ModInfo.VERSION)
public class JakojaannosLib extends ModMainBase<BlocksBase, ItemsBase, BiomesBase, CommandsBase> {
    private static final Logger LOGGER = LogManager.getLogger("jakojaannos-lib");

    @Override
    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        super.onServerStarting(event);
    }

    @Override
    @Mod.EventHandler
    public void onInit(FMLPreInitializationEvent event) {
        super.onInit(event);
    }

    @Override
    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {

    }

    @Override
    @Mod.EventHandler
    public void onInit(FMLPostInitializationEvent event) {

    }
}
