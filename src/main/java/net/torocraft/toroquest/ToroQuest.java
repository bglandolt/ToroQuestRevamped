package net.torocraft.toroquest;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod( modid = ToroQuest.MODID, name = ToroQuest.MODNAME, version = ToroQuest.VERSION, guiFactory = "net.torocraft." + ToroQuest.MODID + ".gui.GuiFactoryToroQuest" )
public class ToroQuest
{
	public static final String MODID = "toroquest";
	public static final String VERSION = "1.12.2-5.3";
	public static final String MODNAME = "ToroQuest";

	@SidedProxy( clientSide = "net.torocraft.toroquest.ClientProxy", serverSide = "net.torocraft.toroquest.ServerProxy" )
	public static CommonProxy proxy;

	@Instance( value = ToroQuest.MODID )
	public static ToroQuest INSTANCE;

	@Mod.EventHandler
	public void preInit( FMLPreInitializationEvent e )
	{
		proxy.preInit(e);
	}

	@Mod.EventHandler
	public void init( FMLInitializationEvent e )
	{
		proxy.init(e);
	}

	@Mod.EventHandler
	public void postInit( FMLPostInitializationEvent e )
	{
		proxy.postInit(e);
	}

	@Mod.EventHandler
	public void serverLoad( FMLServerStartingEvent e )
	{
		e.registerServerCommand(new ToroQuestCommand());
	}
}
