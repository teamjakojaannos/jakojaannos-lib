package jakojaannos.api.lib;

import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Interface for instanced APIs. By registering your API-level managers to IApiInstance-registry, you can use
 * {@literal @ObjectHolder}-annotations to inject your instances to static final INSTANCE -fields. This effectively
 * prevents anyone from tampering with your instances, while still providing convenient access to the instanced
 * methods. You get to hide actual implementation from the API, no need to create static wrapper methods to
 * delegate calls to instance and instance is effectively final. win-win-win! Only downside is that your instance
 * implementation needs to extend from {@literal IForgeRegistryEntry.Impl<IApiInstance>}
 * <p>
 * So, how do you access this registry, you ask? Well, it's a IForgeRegistry, so just like you (at least should)
 * do with any other stuff you register: just @SubscribeEvent to {@literal RegistryEvent.Register<IApiInstance>}
 * and use <c>event.getRegistry().register(...);</c>
 */
public interface IApiInstance extends IForgeRegistryEntry<IApiInstance> {
}
