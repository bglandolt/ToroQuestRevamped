package net.torocraft.toroquest.civilization;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.World;

public class CivilizationUtil
{
	public static Province getProvinceAt( World world, int chunkX, int chunkZ )
	{
		return CivilizationsWorldSaveData.get(world).atLocation(chunkX, chunkZ);
	}

	public static Province getProvinceFromUUID( World world, @Nullable UUID id )
	{
		if ( id == null )
		{
			return null;
		}

		for ( Province p : CivilizationsWorldSaveData.get(world).getProvinces() )
		{
			if ( p.getUUID().equals(id) )
			{
				return p;
			}
		}

		return null;
	}

	public static UUID enumUUID( String s )
	{
		try
		{
			UUID uuid = UUID.fromString(s);
			return uuid;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	// CivilizationsWorldSaveData.resgister
	public static Province registerNewCivilization( World world, int chunkX, int chunkZ )
	{
		return CivilizationsWorldSaveData.get(world).register(chunkX, chunkZ, true);
	}
}