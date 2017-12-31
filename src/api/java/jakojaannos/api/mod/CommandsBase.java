package jakojaannos.api.mod;

import net.minecraft.command.ICommand;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public abstract class CommandsBase {

    private FMLServerStartingEvent event;

    protected abstract void initCommands();

    protected void register(ICommand command) {
        event.registerServerCommand(command);
    }

    void doInitCommands(FMLServerStartingEvent event) {
        this.event = event;
        initCommands();
    }
}
