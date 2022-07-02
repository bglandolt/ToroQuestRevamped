package net.torocraft.toroquest;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.datafix.DataFixer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.torocraft.toroquest.block.BlockVillageSpawner;
import net.torocraft.toroquest.civilization.CivilizationGeneratorHandlers;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.civilization.quests.util.Quests;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.configuration.ConfigurationHandler;
import net.torocraft.toroquest.entities.EntityVillageLord;
import net.torocraft.toroquest.entities.ToroQuestEntities;
import net.torocraft.toroquest.generation.WorldGenPlacer;
import net.torocraft.toroquest.generation.village.VillageHandlerBarracks;
import net.torocraft.toroquest.generation.village.VillageHandlerGuardTower;
import net.torocraft.toroquest.generation.village.VillageHandlerKeep;
import net.torocraft.toroquest.generation.village.VillageHandlerShop;
import net.torocraft.toroquest.generation.village.VillageHandlerWall;
import net.torocraft.toroquest.gui.VillageLordGuiHandler;
import net.torocraft.toroquest.network.ToroQuestPacketHandler;

@EventBusSubscriber
public class CommonProxy
{

	@SubscribeEvent
	public static void registerBlocks( RegistryEvent.Register<Block> event )
	{
		event.getRegistry().register(new BlockVillageSpawner());
	}

	@SubscribeEvent
	public static void registerItems( RegistryEvent.Register<Item> event )
	{
		event.getRegistry().register(new ItemBlock(BlockVillageSpawner.INSTANCE).setRegistryName(BlockVillageSpawner.REGISTRY_NAME));
	}

	@Mod.EventHandler
	public void preInit( FMLPreInitializationEvent e )
	{
		ConfigurationHandler.init(e.getSuggestedConfigurationFile());
		initConfig(e.getSuggestedConfigurationFile());
		MinecraftForge.EVENT_BUS.register(new EventHandlers());
		MinecraftForge.EVENT_BUS.register(new CivilizationGeneratorHandlers());
		NetworkRegistry.INSTANCE.registerGuiHandler(ToroQuest.INSTANCE, new VillageLordGuiHandler());
		EntityVillageLord.registerFixesVillageLord(new DataFixer(922));
		ToroQuestEntities.init();
		VillageHandlerWall.init();
		VillageHandlerKeep.init();
		VillageHandlerShop.init();
		VillageHandlerGuardTower.init();
		VillageHandlerBarracks.init();
		ToroQuestPacketHandler.init();
		ToroQuestTriggers.register();
		PlayerCivilizationCapabilityImpl.register();
		WorldGenPlacer.init();
		Quests.init();
		SoundHandler.registerSounds();
	}

	private void initConfig( File configFile )
	{
		ToroQuestConfiguration.init(configFile);
		MinecraftForge.EVENT_BUS.register(new ToroQuestConfiguration());
	}

	@Mod.EventHandler
	public void init( FMLInitializationEvent e )
	{
	}

	@Mod.EventHandler
	public void postInit( FMLPostInitializationEvent e )
	{

	}

}
