package jakojaannos.lib.init;

import com.google.common.base.Preconditions;
import jakojaannos.lib.block.IBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;


/**
 * Provides helpers for block registration.
 * <p>
 * Blocks registered via methods provided are automatically added to forge registry, without need to worry about
 * {@link RegistryEvent RegistryEvents}. Supports using {@link IBlock}-interface metadata for further simplifying the
 * registration process.
 */
public abstract class BlocksBase extends ContentBase {
    private static final Logger LOGGER = LogManager.getLogger("jakojaannos-lib");

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Registration
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Register blocks here using {@link #register} -method overrides
     */
    public abstract void initBlocks();


    /**
     * Queues the block for registration and generates its properties using {@link IBlock}-metadata
     *
     * @param name  Registry name
     * @param block Block to register
     */
    protected final <TBlock extends Block & IBlock> void register(String name, TBlock block) {
        register(name, block, block.hasItemBlock() ? getItemBlockFor(block) : null);
    }


    /**
     * Queues the block for registration using given properties
     *
     * @param name              Registry name
     * @param block             Block to register
     * @param generateItemBlock Should an ItemBlock be automatically generated for this block
     */
    protected final void register(String name, Block block, boolean generateItemBlock) {
        register(name, block, generateItemBlock ? generateItemBlockFor(block) : null);
    }


    /**
     * Queues the block for registration
     *
     * @param name      Registry name
     * @param block     Block to register
     * @param itemBlock The ItemBlock for this Block. use null for no item block.
     */
    protected final void register(String name, Block block, @Nullable Item itemBlock) {
        blocks.put(name, new BlockEntry(block, itemBlock));
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Private Implementation
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Map<String, BlockEntry> blocks = new HashMap<>();

    private <TBlock extends Block & IBlock> Item getItemBlockFor(TBlock block) {
        Item itemBlock = block.getCustomItemBlock();
        return itemBlock != null ? itemBlock : generateItemBlockFor(block);
    }

    private Item generateItemBlockFor(Block block) {
        return new ItemBlock(block);
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Registry events
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        blocks.forEach((name, entry) -> doRegisterBlock(event.getRegistry(), name, entry));
    }

    private void doRegisterBlock(IForgeRegistry<Block> registry, String name, BlockEntry entry) {
        // Set registry name
        entry.block.setUnlocalizedName(name);
        entry.block.setRegistryName(new ResourceLocation(getModId(), name));

        // Register
        registry.register(entry.block);
    }


    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        // Register all available item blocks
        blocks.entrySet().stream()
                .filter(entry -> entry.getValue().hasItemBlock())
                .forEach(entry -> doRegisterItem(event.getRegistry(), entry.getValue()));
    }

    private void doRegisterItem(IForgeRegistry<Item> registry, BlockEntry entry) {
        Preconditions.checkState(entry.hasItemBlock(), "Trying to register item block that does not exist!");
        Preconditions.checkNotNull(entry.itemBlock); // Always passes, BlockEntry::hasItemBlock performs null-check
        Preconditions.checkNotNull(entry.block.getRegistryName(), "Registry name not set!");

        // Set name
        entry.itemBlock.setRegistryName(entry.block.getRegistryName());

        // Register
        registry.register(entry.itemBlock);
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent event) {
        blocks.values().stream()
                .filter(BlockEntry::hasItemBlock)
                .forEach(this::registerItemBlockModel);
    }

    @SideOnly(Side.CLIENT)
    private void registerItemBlockModel(BlockEntry entry) {
        Preconditions.checkNotNull(entry.itemBlock);
        Preconditions.checkNotNull(entry.itemBlock.getRegistryName());

        ModelLoader.setCustomModelResourceLocation(
                entry.itemBlock,
                0,
                new ModelResourceLocation(entry.itemBlock.getRegistryName(), "inventory"));
    }


    private class BlockEntry {
        final Block block;
        final Item itemBlock;

        BlockEntry(Block block, @Nullable Item itemBlock) {
            this.block = block;
            this.itemBlock = itemBlock;
        }

        boolean hasItemBlock() {
            return itemBlock != null;
        }
    }
}
