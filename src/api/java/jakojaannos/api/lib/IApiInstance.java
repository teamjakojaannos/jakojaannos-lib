package jakojaannos.api.lib;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Interface for API-level managers and systems which functionality can be exposed through interface. Provides
 * convenient way to setup a singleton instance which can be accessed via {@link ObjectHolder @ObjectHolder}, while
 * minimizing boilerplate required.
 * <p>
 * First of all, ask yourself: Does my system really need a specialized manager? If yes, could it be implemented
 * as a {@link IForgeRegistry}? Or perhaps via {@link Capability}-system? If answer is "no" or "not quite" or something
 * else that would mean ending up with shitload of spaghetti code, continue reading. This approach is in no way superior
 * to any other approach, it just helps keeping your APIs clean and decoupled from the actual implementation.
 * <p>
 * Let's start from the background first: Convenient way to allow other mods interact with your mod's systems is to
 * expose some sort of manager class which can be used to interact with the said system. However, to improve
 * readability and/or in order to hide the implementation behind layer of abstraction, it is useful to do this via
 * exposing an interface or abstract class with stub implementations. The actual instance can then be provided in many
 * ways, via singleton pattern, setter which can only set the value once, etc.
 * <p>
 * Very convenient way to provide the instance is to have a public static field which is set to the implementation
 * during initialization. However, protecting those fields so that people don't accidentally and/or due to enormous
 * brain-farts tamper with them (most likely breaking everything in the process) can be a problem, resulting in some
 * ugly boilerplate. The whole point in IApiInstance boils down to minimizing that boilerplate.
 * <p>
 * IApiInstance provides a registry which you can register your instanced managers to. This allows using
 * {@link ObjectHolder @ObjectHolder} annotations to get references to APIs. Basic setup is something like
 * following:<br><ul>
 * <li> a public interface with exposed methods in your API
 * <pre><c>   public interface ISystemInterface extends IApiInstance {
 *     void foo();
 *     void bar();
 * }</c></pre>
 * <li> private implementation in your mod's main source set. note the supertypes here, they are required for registries
 * to work properly
 * <pre><c>   public class SystemImplementation extends{@literal IForgeRegistryEntry.Impl<IApiInstance>}
 *                                   implements ISystemInterface {
 *     public void foo() { ... }
 *     public void bar() { ... }
 * }</c></pre>
 * <li> Event handler for registering the manager/system
 * <pre><c>   @SubscribeEvent
 * public static void register(RegistryEvent.Register<IApiInstance> event) {
 *     event.getRegistry().register(new SystemImplementation().setRegistryName("modid:managername"));
 * }</c></pre>
 * <li> If other components are set-up correctly, mods that require reference to that instance can then just declare
 * {@link ObjectHolder @ObjectHolder} to get a valid instance with all the exposed methods.
 * <pre><c>   @ObjectHolder("modid:managername")
 * public static final ISystemInterface instance = null;</c></pre>
 * </ul>
 */
public interface IApiInstance extends IForgeRegistryEntry<IApiInstance> {
}
