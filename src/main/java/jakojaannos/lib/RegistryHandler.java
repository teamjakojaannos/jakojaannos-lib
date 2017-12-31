package jakojaannos.lib;

import jakojaannos.api.lib.IApiInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * Registers registries to registry-registry
 */
@Mod.EventBusSubscriber
public class RegistryHandler {
    @SubscribeEvent
    public static void onNewRegistry(RegistryEvent.NewRegistry event) {
        new RegistryBuilder<IApiInstance>()
                .disableOverrides()
                .disableSaving()
                .setIDRange(0, 255)
                .setType(IApiInstance.class)
                .setName(new ResourceLocation(ModInfo.MODID, "managerapi"))
                .create();
    }
}
