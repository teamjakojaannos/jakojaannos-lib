package jakojaannos.lib.world.biome;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Random;

/**
 * Adds some convenience features for allowing a bit more customized biomes.
 */
public class BiomeBase extends Biome {
    private int seaLevelOverride;
    private int bedrockDepth;
    private int fillerDepth;

    private IBlockState oceanBlock;
    private IBlockState stoneBlock;


    /**
     * Gets the sea level override. Negative value means that world default will be used instead.
     */
    public final int getSeaLevelOverride() {
        return seaLevelOverride;
    }

    /**
     * Sets the sea level override. Set to negative value to use world default.
     */
    public final void setSeaLevelOverride(int seaLevel) {
        this.seaLevelOverride = seaLevel;
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
    public void setOceanBlock(IBlockState oceanBlock) {
        this.oceanBlock = oceanBlock;
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
    public void setBedrockDepth(int bedrockDepth) {
        this.bedrockDepth = bedrockDepth;
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
    public void setStoneBlock(IBlockState stoneBlock) {
        this.stoneBlock = stoneBlock;
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
    public void setFillerBlock(IBlockState fillerBlock) {
        this.fillerBlock = fillerBlock;
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
    public void setTopBlock(IBlockState topBlock) {
        this.topBlock = topBlock;
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
    public void setFillerDepth(int fillerDepth) {
        this.fillerDepth = fillerDepth;
    }

    /**
     * Gets the sea level for this biome. If no override is set, world default will be used.
     */
    public final int getSeaLevel(World world) {
        return seaLevelOverride < 0 ? world.getSeaLevel() : seaLevelOverride;
    }


    protected BiomeBase(BiomeProperties properties) {
        super(properties);
        this.oceanBlock = WATER;
        this.stoneBlock = STONE;
        this.fillerDepth = 5;
        this.bedrockDepth = 5;
        this.seaLevelOverride = -1;
    }

    // TODO: Apply noiseVal to fillerDepth etc.

    @Override
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int x, int z, double noiseVal) {
        // Set bottom layer to bedrock
        primer.setBlockState(x, 0, z, BEDROCK);

        final int seaLevel = getSeaLevel(world);
        boolean hitSolid = false;
        int solidDepth = 0;

        // Replace blocks
        for (int y = world.getSeaLevel(); y > 0; y--) {

            final IBlockState state = world.getBlockState(new BlockPos(x, y, z));

            // Keep replacing water blocks with air or water override block until we hit solid
            if (!hitSolid) {
                if (state.getBlock() == Blocks.WATER) {
                    replaceWater(world, rand, primer, seaLevel, x, y, z, noiseVal);
                } else if (state.getMaterial().isSolid()) {
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
        final IBlockState newState = y > seaLevel ? AIR : oceanBlock;

        primer.setBlockState(x, y, z, newState);
    }

    protected void replaceSolid(World world, Random random, ChunkPrimer primer, int seaLevel, int x, int y, int z, double noiseVal, int solidDepth) {
        // Randomly replace blocks with bedrock starting at bedrock depth with frequency of (1 / bedrockDepth) and
        // increasing to frequency of 100% at bottommost layer. Topmost solid layer can never be bedrock.
        if (solidDepth == 0) {
            primer.setBlockState(x, y, z, topBlock);
        } else if (y <= random.nextInt(bedrockDepth)) {
            primer.setBlockState(x, y, z, BEDROCK);
        } else {
            primer.setBlockState(x, y, z, solidDepth >= fillerDepth ? stoneBlock : fillerBlock);
        }
    }
}
