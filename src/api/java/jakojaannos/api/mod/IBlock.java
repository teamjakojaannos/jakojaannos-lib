package jakojaannos.api.mod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nullable;

public interface IBlock {
    /**
     * If true, an {@link ItemBlock} is automatically generated for this block
     */
    default boolean hasItemBlock() {
        return true;
    }

    /**
     * If non-null, item returned is registered as {@link ItemBlock} for this block
     *
     * @see #hasItemBlock()
     */
    @Nullable
    default Item getCustomItemBlock() {
        return null;
    }
}
