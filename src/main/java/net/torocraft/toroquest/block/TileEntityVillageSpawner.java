package net.torocraft.toroquest.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.entities.EntityVillageLord;

public class TileEntityVillageSpawner extends TileEntity implements ITickable
{

	protected int triggerDistance = 60;

	public TileEntityVillageSpawner()
	{
		// this.setPos(p);
	}

	public void update()
	{
		
		if ( this.getPos() != null && this.getPos() != BlockPos.ORIGIN && this.withinRange() )
		{
			this.triggerSpawner();
		}
	}

	protected void triggerSpawner()
	{
		if ( this.triggerDistance < 0 )
		{
			this.world.removeTileEntity(this.getPos());
			this.world.setBlockToAir(this.getPos());
			// this.markDirty();
			return;
		}
		
		this.triggerDistance = -1;
		this.spawnCreature();
		this.world.removeTileEntity(this.getPos());
		this.world.setBlockToAir(this.getPos());
		// this.markDirty();
	}

	public void spawnCreature()
	{
		if ( this.world.isRemote )
		{
			return;
		}
		
		EntityVillageLord entity = new EntityVillageLord(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
		this.spawnEntityLiving((EntityLiving) entity, this.getPos());
	}

//	public BlockPos findSuitableSpawnLocation()
//	{
//		Random rand = world.rand;
//
//		if ( spawnRadius < 1 )
//		{
//			return getPos();
//		}
//
//		int degrees, distance, x, z;
//
//		BlockPos pos;
//
//		for (int i = 0; i < 16; i++)
//		{
//			distance = rand.nextInt(spawnRadius);
//			degrees = rand.nextInt(360);
//			x = distance * (int) Math.round(Math.cos(Math.toRadians(degrees)));
//			z = distance * (int) Math.round(Math.sin(Math.toRadians(degrees)));
//			pos = findSurface(x, z);
//			if (pos != null)
//			{
//				return pos;
//			}
//		}
//		return getPos();
//	}

//	private BlockPos findSurface(int x, int z)
//	{
//		BlockPos pos = getPos().add(x, -3, z);
//		IBlockState blockState;
//		int yOffset = 0;
//		boolean groundFound = false;
//		boolean[] airSpace = { false, false };
//
//		while (yOffset <= 16)
//		{
//			blockState = world.getBlockState(pos);
//			if (isGroundBlock(blockState))
//			{
//				groundFound = true;
//				airSpace[0] = false;
//				airSpace[1] = false;
//
//			}
//			else if (airSpace[0] && airSpace[1] && groundFound)
//			{
//				return pos.down();
//
//			}
//			else if (Blocks.AIR.equals(blockState.getBlock()))
//			{
//				if (airSpace[0])
//				{
//					airSpace[1] = true;
//				}
//				else
//				{
//					airSpace[0] = true;
//				}
//			}
//			pos = pos.up();
//			yOffset++;
//		}
//		return null;
//	}

	protected boolean isGroundBlock(IBlockState blockState)
	{
		return blockState.isOpaqueCube();
	}
	
	protected boolean spawnEntityLiving(EntityLiving entity, BlockPos pos)
	{
		double x = pos.getX() + 0.5D;
		double y = pos.getY();
		double z = pos.getZ() + 0.5D;

		entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
		entity.rotationYawHead = entity.rotationYaw;
		entity.renderYawOffset = entity.rotationYaw;
		entity.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);

		entity.enablePersistence();
		world.spawnEntity(entity);
		entity.playLivingSound();
		
		return true;
	}

	protected int ticksExisted = 0;
	
	protected boolean withinRange()
	{
		return this.ticksExisted++ % 60 == 0 && this.playerNear();
	}

    public boolean playerNear()
    {    			
        for ( EntityPlayer player : this.world.playerEntities )
        {
            if ( EntitySelectors.NOT_SPECTATING.apply(player) && !player.isCreative() && player.dimension == 0 && player.getDistanceSq(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()) <= this.triggerDistance*this.triggerDistance );
            {
            	return true;
            }
        }

        return false;
    }

//	public boolean isAnyPlayerWithinRangeAt( World world, double x, double y, double z, double range)
//    {
//        for (int j2 = 0; j2 < world.playerEntities.size(); ++j2)
//        {
//            EntityPlayer entityplayer = world.playerEntities.get(j2);
//
//            if ( entityplayer.isCreative() )
//            {
//            	continue;
//            }
//            
//            if (EntitySelectors.NOT_SPECTATING.apply(entityplayer))
//            {
//                double d0 = entityplayer.getDistanceSq(x, y, z);
//
//                if (range < 0.0D || d0 < range * range)
//                {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//	
//	protected boolean isRunTick()
//	{
//		return world.getWorldTime() % 75 == 0;
//	}

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