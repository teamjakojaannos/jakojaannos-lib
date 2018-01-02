package jakojaannos.api.world;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Random;

/**
 * Adds some convenience features for allowing a bit more customized biomes.
 */
public abstract class AdvancedBiomeBase extends Biome {
    private static final IBlockState[] LOOKUP = new IBlockState[256];

    private int seaLevelOverride;
    private int bedrockDepth;

    private float seaLevelFuzzScale;
    private float seaLevelFuzzOffset;

    private IBlockState oceanBlock;
    private IBlockState stoneBlock;

    private final LayerSupplier layerSupplier;


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


    protected AdvancedBiomeBase(BiomeProperties properties, LayerSupplier layerSupplier) {
        super(properties);
        this.layerSupplier = layerSupplier;

        this.oceanBlock = WATER;
        this.stoneBlock = STONE;
        this.bedrockDepth = 5;
        this.seaLevelOverride = -1;

        this.seaLevelFuzzOffset = 0.0f;
        this.seaLevelFuzzScale = 0.0f;
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
        int solidDepth = 0;

        // Replace blocks
        for (int y = 255; y > 0; y--) {

            final IBlockState state = primer.getBlockState(x, y, z);

            // Keep replacing water blocks with air or water override block until we hit solid
            if (!hitSolid) {
                if (state.getMaterial() == Material.WATER) {
                    final IBlockState newState = y > seaLevel ? Blocks.AIR.getDefaultState() : oceanBlock;
                    primer.setBlockState(x, y, z, newState);
                } else if (state.getMaterial() != Material.AIR) {
                    hitSolid = true;

                    // Generate temporary lookup table to speed up the block selection
                    generateLookup(rand, y, fuzzySeaLevel, globalX, globalZ, noiseVal, LOOKUP);
                }
            }

            // Replace first solid with top block and the rest with filler blocks.
            if (hitSolid) {
                primer.setBlockState(x, y, z, LOOKUP[solidDepth++]);
            }
        }
    }


    /**
     * Generates lookup table for blockstates in a single column. DO NOT CREATE NEW ARRAY ON EACH CALL, use the array
     * provided in "lookup" parameter as it is recycled.
     *
     * @param solidY        y-coordinate of the first solid layer
     * @param fuzzySeaLevel sea level with fuzz applied
     * @param x             global x-coordinate of the column
     * @param z             global z-coordinate of the column
     */
    protected void generateLookup(Random random, int solidY, int fuzzySeaLevel, int x, int z, double noiseVal, IBlockState[] lookup) {
        final boolean underwater = solidY <= fuzzySeaLevel;
        final BlockLayer[] layers = getLayers(underwater);
        final int fuzzyBedrockDepth = bedrockDepth; // TODO: apply fuzz

        int y = 0;
        for (BlockLayer layer : layers) {
            int i = 0;
            while (y < 256 && i < layer.getDepth()) {
                if (y == 0) {
                    lookup[y] = layer.getBlock();
                } else if (y > random.nextInt(fuzzyBedrockDepth - solidY)) {
                    lookup[y] = Blocks.BEDROCK.getDefaultState();
                } else {
                    lookup[y] = layer.getBlock();
                }

                i++;
                y++;
            }
        }

        // Fill the rest of blocks with stone blocks
        while (y < 256) {
            lookup[y] = stoneBlock;
            y++;
        }
    }

    protected BlockLayer[] getLayers(boolean underwater) {
        return layerSupplier.supply(underwater);
    }

    public interface LayerSupplier {
        BlockLayer[] supply(boolean underwater);
    }
}
