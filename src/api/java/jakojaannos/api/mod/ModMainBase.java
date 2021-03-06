package jakojaannos.api.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.*;

/**
 * Base for creating the mod main class.
 * <p>
 * Implementation uses concept of "Content" classes which are classes that facilitate registration and storage of static
 * references (or {@link ObjectHolder ObjectHolders}) of content instances. For example, Block content -class handles
 * block registration and is the place where mod should have @ObjectHolders for all its blocks. There is no need to
 * create unneeded placeholder classes neither. For example, if mod doesn't add any new biomes, it's valid to just pass
 * plain {@link BiomesBase} to {@link TBiomes} in class definition and you are set.
 * <p>
 * Implementations must override event handlers for {@link #onInit(FMLPreInitializationEvent)},
 * {@link #onInit(FMLPreInitializationEvent)}, {@link #onInit(FMLInitializationEvent)} and
 * {@link #onInit(FMLPostInitializationEvent)}, annotate them with {@link Mod.EventHandler} and call base implementation
 * (if the method base is not abstract, ofc). Always declare subclasses final. Content class resolving from generic type
 * arguments breaks if trying to inherit further.
 * <p>
 * Note: I'm terribly sorry to everyone for all the black magic used. Necessary evil for eliminating unnecessary
 * boilerplate from mod mains/content classes in a convenient way.
 *
 * TODO: Change discovery so that it can locate stuff higher from hierarchy and create subclasses with less generic params
 */
public abstract class ModMainBase<
        TBlocks extends BlocksBase,
        TItems extends ItemsBase,
        TBiomes extends BiomesBase,
        TCommands extends CommandsBase,
        TLoot extends LootTablesBase> {

    private static final Logger LOGGER = LogManager.getLogger("jakojaannos-lib");

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// FML Events
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onServerStarting(FMLServerStartingEvent event) {
        if (commands != null) {
            commands.doInitCommands(event);
        }
    }

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

        if (biomes != null) {
            MinecraftForge.EVENT_BUS.register(biomes);
            biomes.initBiomes();
        }
    }

    public abstract void onInit(FMLInitializationEvent event);

    public abstract void onInit(FMLPostInitializationEvent event);


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Private Implementation (you might not want to mess with this monstrous spaghetti code)
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final TBlocks blocks;
    private final TItems items;
    private final TBiomes biomes;
    private final TCommands commands;
    private final TLoot loot;

    protected ModMainBase() {
        blocks = createContentInstance(getBlocksClass());
        items = createContentInstance(getItemsClass());
        biomes = createContentInstance(getBiomesClass());
        commands = createContentInstance(getCommandsClass());
        loot = createContentInstance(getLootTablesClass());
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// WARNING: Black Magic
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    private <TContent> TContent createContentInstance(Class<TContent> clazz) {
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
        if (instance instanceof ContentBase) {
            ((ContentBase) instance).setModId(findModId());
        }

        // Return the instance. I'm amazed if this line actually executes with all the reflection involved
        return instance;
    }

    private String findModId() {
        Mod modMetadata = getClass().getAnnotation(Mod.class);
        return modMetadata.modid();
    }


    private static final int BLOCKS_INDEX = 0;
    private static final int ITEMS_INDEX = 1;
    private static final int BIOMES_INDEX = 2;
    private static final int COMMANDS_INDEX = 3;
    private static final int LOOT_INDEX = 4;

    private Class<TBlocks> getBlocksClass() {
        // noinspection unchecked (Shh, just let it happen)
        return (Class<TBlocks>) getActualTypeArguments()[BLOCKS_INDEX];
    }

    private Class<TItems> getItemsClass() {
        // noinspection unchecked (Shh, just let it happen)
        return (Class<TItems>) getActualTypeArguments()[ITEMS_INDEX];
    }

    private Class<TBiomes> getBiomesClass() {
        // noinspection unchecked (Shh, just let it happen)
        return (Class<TBiomes>) getActualTypeArguments()[BIOMES_INDEX];
    }

    private Class<TCommands> getCommandsClass() {
        // noinspection unchecked (Shh, just let it happen)
        return (Class<TCommands>) getActualTypeArguments()[COMMANDS_INDEX];
    }

    private Class<TLoot> getLootTablesClass() {
        // noinspection unchecked (Shh, just let it happen)
        return (Class<TLoot>) getActualTypeArguments()[LOOT_INDEX];
    }

    private Type[] getActualTypeArguments() {
        // Umm... yeah. It might be better you just don't even ask.
        // ...but if you did, all this line does is basically just getting an array of types specified in generic type
        // parameters.
        //
        // 1. getClass() returns the Class of the calling subclass
        // 2. getGenericSupertype() gets type token of this abstract base with generic type parameters filled in (This
        //    works because subclass has those types explicitly defined, thus overcoming the type erasure). And
        //    as we know for sure that generics are involved, we just cast the Type to ParameterizedType to get access
        //    to that data.
        // 3. Now we can use getActualTypeArguments to get type tokens describing what is filled to the generic type
        //    parameters.
        //
        // Array we get contains type tokens to generic parameters in order they are defined. First at index zero being
        // TModMain, first content class being TBlocks at index one, second TItems at two and so forth.
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    }
}
