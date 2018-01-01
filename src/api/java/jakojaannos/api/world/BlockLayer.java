package jakojaannos.api.world;

import jakojaannos.api.helpers.BlockHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public BlockLayer(String string) {
        List<String> strings = Arrays.stream(string.toLowerCase().trim().split("\\s*,\\s*")) // Split by commas surrounded by whitespace
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        int d = 1;
        if (strings.size() > 0) {
            try {
                d = Integer.parseInt(strings.get(0));
            } catch (NumberFormatException ignored) {
            }
        }
        this.depth = d;

        if (strings.size() > 1) {
            this.block = BlockHelper.stringToBlockstateWithFallback(Blocks.STONE.getDefaultState(), strings.get(1));
        } else {
            this.block = Blocks.STONE.getDefaultState();
        }
    }
}
