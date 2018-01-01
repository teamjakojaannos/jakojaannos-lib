package jakojaannos.api.mod;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;

public abstract class LootTablesBase {
    protected static ResourceLocation register(String modid, String name) {
        ResourceLocation resourceLocation = new ResourceLocation(modid, name);
        LootTableList.register(resourceLocation);
        return resourceLocation;
    }
}
