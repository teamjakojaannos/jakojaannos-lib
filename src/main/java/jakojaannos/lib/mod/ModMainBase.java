package jakojaannos.lib.mod;

import jakojaannos.lib.init.BlocksBase;
import jakojaannos.lib.init.ContentBase;
import jakojaannos.lib.init.ItemsBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.*;

/**
 * Base for creating the mod main class.
 * <p>
 * Implementations must override event handlers for {@link #onInit(FMLPreInitializationEvent)},
 * {@link #onInit(FMLInitializationEvent)} and {@link #onInit(FMLPostInitializationEvent)},
 * annotate them with {@link Mod.EventHandler} and call base implementation (if the method base is not abstract).
 * Always declare subclasses final. Content class resolving from generic type arguments breaks if trying to inherit
 * further.
 * <p>
 * Implementation uses concept of "Content" classes which are classes that facilitate registration and storage of static
 * references (or {@link ObjectHolder ObjectHolders}) of content instances. For example, Block content -class handles
 * block registration and is the place where mod should have @ObjectHolders for all its blocks.
 * <p>
 * Note: I'm terribly sorry to everyone for all the black magic used. Necessary evil for eliminating unnecessary
 * boilerplate from mod mains/content classes in a convenient way.
 */
public abstract class ModMainBase<
        TModMain extends ModMainBase,
        TBlocks extends BlocksBase,
        TItems extends ItemsBase> {

    private static final Logger LOGGER = LogManager.getLogger("jakojaannos-lib");

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// FML Events
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onInit(FMLPreInitializationEvent event) {
        // Register content instances and initialize content
        if (blocks != null) {
            MinecraftForge.EVENT_BUS.register(blocks);
            blocks.initBlocks();
        }

        if (items != null) {
            MinecraftForge.EVENT_BUS.register(items);
            items.initItems();
        }


    }

    public abstract void onInit(FMLInitializationEvent event);

    public abstract void onInit(FMLPostInitializationEvent event);


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Private Implementation (you might not want to mess with this monstrous spaghetti code)
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final TBlocks blocks;
    private final TItems items;

    protected ModMainBase() {
        blocks = createContentInstance(getBlocksClass());
        items = createContentInstance(getItemsClass());
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// WARNING: Black Magic
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    private <TContent extends ContentBase> TContent createContentInstance(Class<TContent> clazz) {
        // Do not proceed if abstract class was specified
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }

        // Try to create instance
        TContent instance;
        try {
            Constructor<TContent> ctor = clazz.getConstructor();

            try {
                instance = ctor.newInstance();
            } catch (InstantiationException e) {
                throw LOGGER.throwing(new IllegalStateException("Cannot instantiate abstract class!", e));
            } catch (IllegalAccessException e) {
                throw LOGGER.throwing(new IllegalStateException("Content class constructor should be public", e));
            } catch (InvocationTargetException e) {
                throw LOGGER.throwing(new IllegalStateException("Target constructor threw an exception", e));
            }
        } catch (NoSuchMethodException e) {
            throw LOGGER.throwing(new IllegalStateException("Content class should define a parameterless constructor", e));
        }

        // Set instance modid
        instance.setModId(findModId());

        // Return the instance. I'm amazed if this line actually executes with all the reflection involved
        return instance;
    }

    private String findModId() {
        Mod modMetadata = getClass().getAnnotation(Mod.class);
        return modMetadata.modid();
    }


    private static final int BLOCKS_INDEX = 1;
    private static final int ITEMS_INDEX = 2;

    private Class<TBlocks> getBlocksClass() {
        // Shh, just let it happen
        // noinspection unchecked
        return (Class<TBlocks>) getActualTypeArguments()[BLOCKS_INDEX];
    }

    private Class<TItems> getItemsClass() {
        // Shh, just let it happen
        // noinspection unchecked
        return (Class<TItems>) getActualTypeArguments()[ITEMS_INDEX];
    }

    private Type[] getActualTypeArguments() {
        // Umm... yeah. It might be better you just don't even ask.
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    }
}
