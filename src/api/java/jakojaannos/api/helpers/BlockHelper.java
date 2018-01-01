package jakojaannos.api.helpers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class BlockHelper {
    private BlockHelper() {
    }

    public static IBlockState stringToBlockstateWithFallback(IBlockState fallback, String block) {
        Block b = Block.getBlockFromName(block);
        return (b == null || b == Blocks.AIR) ? fallback : b.getDefaultState();
    }
}
