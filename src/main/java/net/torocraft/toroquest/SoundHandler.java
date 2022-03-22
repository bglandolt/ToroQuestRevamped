package net.torocraft.toroquest;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SoundHandler
{
	public static SoundEvent STEAM_0;
	public static SoundEvent STEAM_1;
	public static SoundEvent STEAM_2;
	public static SoundEvent STEAM_3;
	public static SoundEvent STEAM_STEP_0;
	public static SoundEvent STEAM_STEP_1;
	public static SoundEvent STEAM_AMBIENT;
	
	public static SoundEvent TRADEC_0;
	public static SoundEvent TRADEC_1;
	public static SoundEvent TRADEC_2;
	
//	public static SoundEvent GUARD_CHAT;
//	public static SoundEvent GUARD_ANGRY;
	
	public static void registerSounds()
	{
		TRADEC_0 = registerSound("player.tradec_0");
		TRADEC_1 = registerSound("player.tradec_1");
		TRADEC_2 = registerSound("player.tradec_2");
		STEAM_0 = registerSound("entity.steam_0");
		STEAM_1 = registerSound("entity.steam_1");
		STEAM_2 = registerSound("entity.steam_2");
		STEAM_3 = registerSound("entity.steam_3");
		STEAM_STEP_0 = registerSound("entity.steam_step_0");
		STEAM_STEP_1 = registerSound("entity.steam_step_1");
		STEAM_AMBIENT = registerSound("entity.steam_ambient");
	}
	
	private static SoundEvent registerSound(String name)
	{
		ResourceLocation location = new ResourceLocation(ToroQuest.MODID, name);
		SoundEvent event = new SoundEvent(location);
		event.setRegistryName(name);
		ForgeRegistries.SOUND_EVENTS.register(event);
		return event;
	}
}
