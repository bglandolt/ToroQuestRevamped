package net.torocraft.toroquest;


import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.torocraft.toroquest.util.Timer;

import java.util.List;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.OrderedLoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ServerProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent e)
	{
		super.preInit(e);
		MinecraftForge.EVENT_BUS.register(Timer.INSTANCE);
	}

	@Override
	public void init(FMLInitializationEvent e)
	{
		super.init(e);
	}

	@Override
	public void postInit(FMLPostInitializationEvent e)
	{
		super.postInit(e);
	}
	
//	@EventHandler
//    public void postInit(FMLInitializationEvent event)
//    {
//    	ForgeChunkManager.setForcedChunkLoadingCallback(ToroQuest.INSTANCE, new MainframeChunkloadCallback());
//    }
//	
//	
//	public class MainframeChunkloadCallback implements OrderedLoadingCallback
//	{
//
//		@Override
//		public void ticketsLoaded(List<Ticket> tickets, World world)
//		{
//			for (Ticket ticket : tickets)
//			{
//				ForgeChunkManager.forceChunk(ticket, new ChunkPos(ticket.getEntity().chunkCoordX, ticket.getEntity().chunkCoordZ));
//			}			
//		}
//
//		@Override
//		public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount)
//		{
//			for (Ticket ticket : tickets)
//			{
//				ForgeChunkManager.forceChunk(ticket, new ChunkPos(ticket.getEntity().chunkCoordX, ticket.getEntity().chunkCoordZ));
//			}
//			return tickets;
//		}
//	}

}