package jakojaannos.api.world;

import com.google.common.collect.ImmutableList;
import jakojaannos.api.helpers.BlockHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.config.Config.Comment;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// TODO: Apply noiseVal to fillerDepth etc.

/**
 * Adds some convenience features for allowing a bit more customized biomes.
 */
public class AdvancedBiomeBase extends Biome {
    private int seaLevelOverride;
    private int bedrockDepth;
    private int fillerDepth;

    private float seaLevelFuzzScale;
    private float seaLevelFuzzOffset;

    private IBlockState oceanBlock;
    private IBlockState stoneBlock;

    private final @Nullable List<BlockLayer> overwaterLayers;
    private final @Nullable List<BlockLayer> underwaterLayers;

    private IBlockState[] blockStateLookup;
    private IBlockState[] underwaterBlockStateLookup;


    /**
     * Gets the sea level override. Negative value means that world default will be used instead.
     */
    public int getSeaLevelOverride() {
        return seaLevelOverride;
    }

    /**
     * Sets the sea level override. Set to negative value to use world default.
     */
    public AdvancedBiomeBase setSeaLevelOverride(int seaLevel) {
        this.seaLevelOverride = seaLevel;
        return this;
    }

    /**
     * Gets the block used as water substitute for blocks below sea level
     */
    public IBlockState getOceanBlock() {
        return oceanBlock;
    }

    /**
     * Sets the block used as water substitute for blocks below sea level
     */
    public AdvancedBiomeBase setOceanBlock(IBlockState oceanBlock) {
        this.oceanBlock = oceanBlock;
        return this;
    }

    /**
     * Gets the number of bedrock layers generated
     */
    public int getBedrockDepth() {
        return bedrockDepth;
    }

    /**
     * Sets the number of bedrock layers generated
     */
    public AdvancedBiomeBase setBedrockDepth(int bedrockDepth) {
        this.bedrockDepth = bedrockDepth;
        return this;
    }

    /**
     * Gets the block to use as stone substitute
     */
    public IBlockState getStoneBlock() {
        return stoneBlock;
    }

    /**
     * Sets the block to use as stone substitute
     */
    public AdvancedBiomeBase setStoneBlock(IBlockState stoneBlock) {
        this.stoneBlock = stoneBlock;
        return this;
    }

    /**
     * Gets the filler block
     */
    public IBlockState getFillerBlock() {
        return fillerBlock;
    }

    /**
     * Sets the filler block
     */
    public AdvancedBiomeBase setFillerBlock(IBlockState fillerBlock) {
        this.fillerBlock = fillerBlock;
        return this;
    }

    /**
     * Gets the top block
     */
    public IBlockState getTopBlock() {
        return topBlock;
    }

    /**
     * Sets the top block
     */
    public AdvancedBiomeBase setTopBlock(IBlockState topBlock) {
        this.topBlock = topBlock;
        return this;
    }

    /**
     * Gets the number of filler block layers to generate
     */
    public int getFillerDepth() {
        return fillerDepth;
    }

    /**
     * Sets the number of filler block layers to generate
     */
    public AdvancedBiomeBase setFillerDepth(int fillerDepth) {
        this.fillerDepth = fillerDepth;
        return this;
    }

    /**
     * Gets the fuzz-scale when transitioning from overwater layers to underwater ones
     */
    public float getSeaLevelFuzzScale() {
        return seaLevelFuzzScale;
    }

    /**
     * Gets the fuzz-offset when transitioning from overwater layers to underwater ones
     */
    public float getSeaLevelFuzzOffset() {
        return seaLevelFuzzOffset;
    }

    /**
     * Sets the underwater border fuzz properties
     */
    public void setSeaLevelFuzz(float scale, float offset) {
        this.seaLevelFuzzScale = scale;
        this.seaLevelFuzzOffset = offset;
    }


    /**
     * Gets the sea level for this biome. If no override is set, world default will be used.
     */
    public final int getSeaLevel(World world) {
        return seaLevelOverride < 0 ? world.getSeaLevel() : seaLevelOverride;
    }

    protected AdvancedBiomeBase(BiomeProperties properties, Config config) {
        super(properties);

        this.overwaterLayers = Arrays.stream(config.layers).map(BlockLayer::new).collect(Collectors.toList());
        this.underwaterLayers = Arrays.stream(config.underwaterLayers).map(BlockLayer::new).collect(Collectors.toList());

        this.fillerDepth = config.fallBackFillerDepth;
        this.topBlock = BlockHelper.stringToBlockstateWithFallback(Blocks.GRASS.getDefaultState(), config.fallbackTopBlock);
        this.fillerBlock = BlockHelper.stringToBlockstateWithFallback(Blocks.DIRT.getDefaultState(), config.fallbackFillerBlock);

        this.stoneBlock = BlockHelper.stringToBlockstateWithFallback(Blocks.STONE.getDefaultState(), config.stoneBlock);
        this.oceanBlock = BlockHelper.stringToBlockstateWithFallback(Blocks.WATER.getDefaultState(), config.oceanBlock);

        this.bedrockDepth = config.bedrockDepth;
        this.seaLevelFuzzScale = config.seaLevelFuzzScale;
        this.seaLevelFuzzOffset = config.seaLevelFuzzOffset;
        this.seaLevelOverride = config.seaLevelOverride;

        blockStateLookup = null;
    }

    protected AdvancedBiomeBase(BiomeProperties properties, @Nullable List<BlockLayer> layers, @Nullable List<BlockLayer> underwaterLayers) {
        super(properties);
        this.overwaterLayers = layers == null ? null : ImmutableList.copyOf(layers);
        this.underwaterLayers = underwaterLayers == null ? null : ImmutableList.copyOf(underwaterLayers);

        this.oceanBlock = WATER;
        this.stoneBlock = STONE;
        this.fillerDepth = 5;
        this.bedrockDepth = 5;
        this.seaLevelOverride = -1;

        this.seaLevelFuzzOffset = 3.0f;
        this.seaLevelFuzzScale = 3.0f;

        blockStateLookup = null;
    }

    @Override
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int globalX, int globalZ, double noiseVal) {
        // HACK: Vanilla has x<->z swapped, so do we
        int x = globalZ & 15;
        int z = globalX & 15;

        // Set bottom layer to bedrock
        primer.setBlockState(x, 0, z, BEDROCK);

        final int seaLevel = getSeaLevel(world);
        final int fuzzySeaLevel = MathHelper.floor((seaLevel + getSeaLevelFuzzOffset()) + (noiseVal * getSeaLevelFuzzScale()));
        boolean hitSolid = false;
        boolean underwater = false;
        int solidDepth = 0;

        // Replace blocks
        for (int y = 255; y > 0; y--) {

            final IBlockState state = primer.getBlockState(x, y, z);

            // Keep replacing water blocks with air or water override block until we hit solid
            if (!hitSolid) {
                if (state.getMaterial() == Material.WATER) {
                    replaceWater(world, rand, primer, seaLevel, x, y, z, noiseVal);
                } else if (state.getMaterial() != Material.AIR) {
                    hitSolid = true;
                } else if (y <= fuzzySeaLevel) {
                    underwater = true;
                }
            }

            // Replace first solid with top block and the rest with filler blocks.
            if (hitSolid) {
                replaceSolid(world, rand, primer, seaLevel, x, y, z, noiseVal, solidDepth++, underwater);
            }
        }
    }

    /**
     * Returns true if placed block was ocean block
     */
    protected void replaceWater(World world, Random random, ChunkPrimer primer, int seaLevel, int x, int y, int z, double noiseVal) {
        final IBlockState newState = y > seaLevel ? Blocks.AIR.getDefaultState() : oceanBlock;
        primer.setBlockState(x, y, z, newState);
    }

    protected void replaceSolid(World world, Random random, ChunkPrimer primer, int seaLevel, int x, int y, int z, double noiseVal, int solidDepth, boolean underwater) {
        final int fuzzyBedrockDepth = bedrockDepth; // TODO: apply fuzz

        final IBlockState[] lookup = getBlockStateLookup(underwater);

        // Randomly replace blocks with bedrock starting at bedrock depth with frequency of (1 / bedrockDepth) and
        // increasing to frequency of 100% at bottommost layer. Topmost solid layer can never be bedrock.
        if (solidDepth == 0) {
            primer.setBlockState(x, y, z, lookup[solidDepth]);
        } else if (y <= random.nextInt(fuzzyBedrockDepth)) {
            primer.setBlockState(x, y, z, Blocks.BEDROCK.getDefaultState());
        } else {
            primer.setBlockState(x, y, z, lookup[solidDepth]);
        }
    }

    protected IBlockState[] getBlockStateLookup(boolean underwater) {
        if (underwater) {
            if (underwaterBlockStateLookup == null) {
                underwaterBlockStateLookup = generateLookup(underwaterLayers, GRAVEL, STONE);
            }

            return underwaterBlockStateLookup;
        } else {
            if (blockStateLookup == null) {
                blockStateLookup = generateLookup(overwaterLayers, topBlock, fillerBlock);
            }

            return blockStateLookup;
        }
    }

    private IBlockState[] generateLookup(@Nullable List<BlockLayer> layers, IBlockState top, IBlockState filler) {
        IBlockState[] lookup = new IBlockState[256];

        if (layers == null) {
            for (int y = 0; y < 256; y++) {
                if (y == 0) {
                    lookup[y] = top;
                } else if (y < fillerDepth) {
                    lookup[y] = filler;
                } else {
                    lookup[y] = stoneBlock;
                }
            }
        } else {
            int y = 0;
            for (BlockLayer layer : layers) {
                int i = 0;
                while (y < 256 && i < layer.getDepth()) {
                    lookup[y] = layer.getBlock();
                    y++;
                }
            }

            while (y < 256) {
                lookup[y] = stoneBlock;
                y++;
            }
        }

        return lookup;
    }


    /**
     * Allows constructing advanced biomes from config files
     */
    public static class Config {
        @Comment("Overrides the world sea y-level for this biome. Set to negative value for no override")
        public int seaLevelOverride = -1;

        @Comment("Y-level offset for switching between underwater and normal layers when near sea level")
        public float seaLevelFuzzOffset = 3.0f;

        @Comment("Y-level fuzz scale for switching between underwater and normal layers when near sea level")
        public float seaLevelFuzzScale = 3.0f;

        @Comment("Number of bedrock layers to generate.")
        public int bedrockDepth = 5;


        @Comment("After layers are generated, all remaining solid blocks below them are set to this")
        public String stoneBlock = "minecraft:stone";

        @Comment("Replaces ocean water with this block")
        public String oceanBlock = "minecraft:water";


        @Comment("If layers definition is invalid, generate filler layer with this depth")
        public int fallBackFillerDepth = 5;

        @Comment("If layers definition is invalid, use this block as fallback top layer")
        public String fallbackTopBlock = "minecraft:grass";

        @Comment("If layers definition is invalid, use this block as fallback filler layer")
        public String fallbackFillerBlock = "minecraft:dirt";


        @Comment("Layers to generate. These are generated in order they are defined, relative to the first solid block counting from sky towards bedrock")
        public BlockLayer.Config[] layers = {};

        @Comment("Layers to use when below sea level")
        public BlockLayer.Config[] underwaterLayers = {};
    }
}
