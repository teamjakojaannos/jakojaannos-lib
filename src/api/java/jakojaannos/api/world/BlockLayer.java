package jakojaannos.api.world;

import jakojaannos.api.helpers.BlockHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

/**
 * Defines a generated block layer when generating {@link AdvancedBiomeBase}-based biomes
 */
public class BlockLayer {
    private final int depth;
    private final IBlockState block;

    public int getDepth() {
        return depth;
    }

    public IBlockState getBlock() {
        return block;
    }

    public BlockLayer(int depth, IBlockState block) {
        this.depth = depth;
        this.block = block;
    }

    public BlockLayer(Config config) {
        this.depth = config.depth;
        this.block = BlockHelper.stringToBlockstateWithFallback(Blocks.STONE.getDefaultState(), config.block);
    }

    /**
     * Config-friendly version which can be used for constructing actual layers
     */
    public static class Config {
        public int depth;
        public String block;

        public Config(int depth, String block) {
            this.depth = depth;
            this.block = block;
        }
    }
}
