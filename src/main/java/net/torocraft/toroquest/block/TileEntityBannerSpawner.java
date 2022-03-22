package net.torocraft.toroquest.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.generation.village.util.VillagePieceBlockMap;

public class TileEntityBannerSpawner extends TileEntity implements ITickable
{
	public TileEntityBannerSpawner()
	{
		// this.setPos(p);
	}

	public void update()
	{
		this.triggerSpawner();
	}
	
	protected void triggerSpawner()
	{
		try
		{
			if ( this.getPos() != null && this.getPos() != BlockPos.ORIGIN )
			{
				Block banner = this.world.getBlockState(this.getPos()).getBlock();
				this.world.removeTileEntity(this.getPos());
				if ( banner instanceof BlockSmartBanner )
				{
					VillagePieceBlockMap.setBannerRotation(this.getWorld(), this.getPos(), ((BlockSmartBanner)banner).getFacing() );
				}
			}
		}
		catch (Exception e)
		{
			
		}
	}
	
	protected boolean isGroundBlock(IBlockState blockState)
	{
		return blockState.isOpaqueCube();
	}
	
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		return new SPacketUpdateTileEntity(this.getPos(), 1, getUpdateTag());
	}
	
	public NBTTagCompound getUpdateTag()
	{
		NBTTagCompound nbttagcompound = this.writeToNBT(new NBTTagCompound());
		// nbttagcompound.removeTag("SpawnPotentials");
		return nbttagcompound;
	}
		
	@SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 0.0D;
    }
	
	@Override
	public boolean hasFastRenderer()
    {
        return true;
    }
	
	@Override
	public Block getBlockType()
    {
		return Blocks.AIR.getDefaultState().getBlock();
    }

}