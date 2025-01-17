package net.torocraft.toroquest.generation.village.util;

import java.io.IOException;
import java.util.Map;

import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public abstract class BlockMapBuilder extends BlockMapBase
{

	private Map<String, IBlockState> palette;

	protected abstract void setBlockState( IBlockState block, int x, int y, int z );

	protected abstract void replaceAirAndLiquidDownwards( IBlockState block, int x, int y, int z );

	public BlockMapBuilder( String name )
	{
		super(name);
	}

	public void build( Map<String, IBlockState> palette )
	{
		this.palette = palette;
		load();

		x = 0;
		y = 0;
		z = 0;

		if ( reader == null )
		{
			return;
		}

		try
		{
			while ((line = reader.readLine()) != null)
			{
				handleLine();
			}
		}
		catch (IOException e)
		{
			System.out.println("Failed to build village piece NAME[" + name + "]: " + e.getMessage());
		}
	}

	private void handleLine()
	{
		if ( line == null )
		{
			return;
		}

		if ( line.matches("#{4,}") )
		{
			y++;
			z = 0;
			return;
		}

		x = 0;

		char[] a = line.toCharArray();

		for ( int i = 0; i < a.length - 1; i += 2 )
		{
			placeBlock(String.valueOf(a[i]) + String.valueOf(a[i + 1]));
			x++;
		}

		z++;
	}

	protected abstract boolean specialBlockHandling( String c, int x, int y, int z );

	public String colorChange()
	{
		return "CC";
	}

	public String carpetChange()
	{
		return "cc";
	}

	public void bannerChange( String s )
	{

	}

	public String bedChange( String s )
	{
		return s;
	}

	public String woolChange()
	{
		return "WW";
	}

	public String flowerChange()
	{
		return "FL";
	}

	private void placeBlock( String c )
	{
		if ( c.equals("  ") )
		{
			return;
		}

		if ( this.specialBlockHandling(c, x, y, z) )
		{
			return;
		}

		IBlockState block = palette.get(c);

		// color specific structures ==============================
		if ( c.equals("*C") )
		{
			block = palette.get(colorChange());
		}
		else if ( c.equals("*c") )
		{
			block = palette.get(carpetChange());
		}
		else if ( c.equals("wc") )
		{
			block = palette.get(woolChange());
		}
		else if ( c.equals("FL") )
		{
			block = palette.get(flowerChange());
		}
		// else if ( c.contains("@") ) {block = palette.get(bannerChange(c));}
		// //{setBlockState();bannerChange(c,x,y,z);return;}
		// else if ( c.contains("$") ) {block = palette.get(bedChange(c));}
		// ========================================================

		if ( block == null )
		{
			return;
		}

		if ( y == 0 && blockIsRepeatable(block) )
		{
			replaceAirAndLiquidDownwards(block, x, y, z);
		}
		else
		{
			setBlockState(block, x, y, z);
			handleExtraBedBlock(block, c);
			handleExtraDoorBlock(block, c);
		}
	}

	// wooden planks getting flipped, purifying

	private boolean blockIsRepeatable( IBlockState block )
	{
		return !(block instanceof BlockStairs || block instanceof BlockDoor);
	}

	private void handleExtraDoorBlock( IBlockState block, String c )
	{
		if ( !isDoor(c) )
		{
			return;
		}
		block = block.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER);
		setBlockState(block, x, y + 1, z);
	}

	private boolean isDoor( String c )
	{
		return c.startsWith("d") && (c.equals("d^") || c.equals("dv") || c.equals("d<") || c.equals("d>"));
	}

	protected void handleExtraBedBlock( IBlockState block, String c )
	{
		if ( !isBed(c) )
		{
			return;
		}
		block = block.withProperty(BlockBed.PART, EnumPartType.HEAD);
		switch( block.getValue(BlockBed.FACING) )
		{
		case EAST:
			setBlockState(block, x + 1, y, z);
			break;
		case NORTH:
			setBlockState(block, x, y, z + 1);
			break;
		case SOUTH:
			setBlockState(block, x, y, z - 1);
			break;
		case WEST:
			setBlockState(block, x - 1, y, z);
			break;
		default:
			break;
		}
	}

	protected boolean isBed( String c )
	{
		return c.startsWith("b") && (c.equals("b^") || c.equals("bv") || c.equals("b<") || c.equals("b>"));
	}

}