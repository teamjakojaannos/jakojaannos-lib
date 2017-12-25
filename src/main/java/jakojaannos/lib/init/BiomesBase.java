package jakojaannos.lib.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;

public abstract class BiomesBase extends ContentBase {
    private final Map<String, Entry> biomes = new HashMap<>();

    protected abstract void doRegister();

    protected void register(String key, int weight, BiomeManager.BiomeType type, Biome biome, BiomeDictionary.Type... types) {
        biomes.put(key, new Entry(biome, weight, type, types));
    }


    @SubscribeEvent
    public void onRegisterBiomes(RegistryEvent.Register<Biome> event) {
        biomes.forEach((s, entry) -> doRegisterBiome(event.getRegistry(), s, entry));
    }

    private void doRegisterBiome(IForgeRegistry<Biome> registry, String key, Entry entry) {
        entry.biome.setRegistryName(new ResourceLocation(getModId(), key));
        registry.register(entry.biome);

        BiomeManager.addBiome(entry.type, new BiomeManager.BiomeEntry(entry.biome, entry.weight));
        BiomeDictionary.addTypes(entry.biome, entry.dictTypes);
    }


    private class Entry {
        Biome biome;
        int weight;
        BiomeManager.BiomeType type;
        BiomeDictionary.Type[] dictTypes;

        public Entry(Biome biome, int weight, BiomeManager.BiomeType type, BiomeDictionary.Type... dictTypes) {
            this.biome = biome;
            this.weight = weight;
            this.type = type;
            this.dictTypes = dictTypes;
        }
    }
}
