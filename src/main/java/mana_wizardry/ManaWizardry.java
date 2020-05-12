package mana_wizardry;

import mana_wizardry.module.ModuleWizardry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ManaWizardry.MODID, acceptedMinecraftVersions = "[1.12, 1.13)", guiFactory = "mana_wizardry.gui.GuiFactory")
@Mod.EventBusSubscriber
public class ManaWizardry
{
    public static final String MODID = "mana_wizardry";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigManager.init(event.getSuggestedConfigurationFile());

        if(Loader.isModLoaded("ebwizardry"))
            MinecraftForge.EVENT_BUS.register(new ModuleWizardry());
        MinecraftForge.EVENT_BUS.register(new HandlerBase());
    }
}
