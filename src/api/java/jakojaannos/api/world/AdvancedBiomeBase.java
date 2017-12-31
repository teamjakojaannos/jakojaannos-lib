package jakojaannos.api.world;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Random;

// TODO: Allow specifying top/filler separately for underwater blocks
// TODO: Apply noiseVal to fillerDepth etc.

/**
 * Adds some convenience features for allowing a bit more customized biomes.
 */
public class AdvancedBiomeBase extends Biome {
    private int seaLevelOverride;
    private int bedrockDepth;
    private int fillerDepth;

    private IBlockState oceanBlock;
    private IBlockState stoneBlock;


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
     * Gets the sea level for this biome. If no override is set, world default will be used.
     */
    public final int getSeaLevel(World world) {
        return seaLevelOverride < 0 ? world.getSeaLevel() : seaLevelOverride;
    }


    protected AdvancedBiomeBase(BiomeProperties properties) {
        super(properties);
        this.oceanBlock = WATER;
        this.stoneBlock = STONE;
        this.fillerDepth = 5;
        this.bedrockDepth = 5;
        this.seaLevelOverride = -1;
    }

    @Override
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int globalX, int globalZ, double noiseVal) {
        // HACK: Vanilla has x<->z swapped, so do we
        int x = globalZ & 15;
        int z = globalX & 15;

        // Set bottom layer to bedrock
        primer.setBlockState(x, 0, z, BEDROCK);

        final int seaLevel = getSeaLevel(world);
        boolean hitSolid = false;
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
                }
            }

            // Replace first solid with top block and the rest with filler blocks.
            if (hitSolid) {
                replaceSolid(world, rand, primer, seaLevel, x, y, z, noiseVal, solidDepth++);
            }
        }
    }

    protected void replaceWater(World world, Random random, ChunkPrimer primer, int seaLevel, int x, int y, int z, double noiseVal) {
        final IBlockState newState = y > seaLevel ? Blocks.AIR.getDefaultState() : oceanBlock;

        primer.setBlockState(x, y, z, newState);
    }

    protected void replaceSolid(World world, Random random, ChunkPrimer primer, int seaLevel, int x, int y, int z, double noiseVal, int solidDepth) {
        // Randomly replace blocks with bedrock starting at bedrock depth with frequency of (1 / bedrockDepth) and
        // increasing to frequency of 100% at bottommost layer. Topmost solid layer can never be bedrock.
        if (solidDepth == 0) {
            primer.setBlockState(x, y, z, topBlock);
        } else if (y <= random.nextInt(bedrockDepth)) {
            primer.setBlockState(x, y, z, Blocks.BEDROCK.getDefaultState());
        } else {
            primer.setBlockState(x, y, z, solidDepth >= fillerDepth ? stoneBlock : fillerBlock);
        }
    }
}
