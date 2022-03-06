package net.torocraft.toroquest.entities.ai;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAIRaid extends EntityAIBase
{
	public final EntityCreature entity;
	private final int minDistanceFromCenter = 16;
	private final int moveDistance = 8;
	private final double movementSpeed;
	
	private int centerX;
	private int centerZ;
	
	private boolean enabled = false;
		
	public EntityAIRaid(EntityCreature entity, int x, int z, double speedIn )
	{
		this.entity = entity;
		
		if ( !( x == 0 && z == 0 ) )
		{
			this.enabled = true;
			this.entity.enablePersistence();
		}
		
		this.movementSpeed = speedIn;
		this.centerX = x;
		this.centerZ = z;
		this.setMutexBits(1);
	}
	
//	public EntityAIRaid(EntityCreature entity, double speedIn, int md, int mdfc )
//	{
//		this.entity = entity;
//		this.movementSpeed = speedIn;
//		this.moveDistance = md;
//		this.minDistanceFromCenter = mdfc;
//		this.setMutexBits(1);
//	}

//	public void setCenter( Integer x, Integer z )
//	{
//		if ( x != null && z != null && !( x == 0 && z == 0 ) )
//		{
//			this.centerX = x;
//			this.centerZ = z;
//		}
//	}
//	
//	public void setCenter( BlockPos pos )
//	{
//		if ( pos != null && pos.getY() != 0 ) // pos != BlockPos.ORIGIN )
//		{
//			this.centerX = pos.getX();
//			this.centerZ = pos.getZ();
//		}
//	}
	
	private boolean move( World world, BlockPos start )
	{
		// System.out.println( this.entity + "mooooove");
		
		double x = this.centerX - start.getX();
		double z = this.centerZ - start.getZ();
		
		double xz = Math.abs(x) + Math.abs(z);
				
		if ( xz < this.minDistanceFromCenter )
		{
			return false;
		}
		
		x = x/xz * (world.rand.nextInt(this.moveDistance)+this.moveDistance) + start.getX();
		z = z/xz * (world.rand.nextInt(this.moveDistance)+this.moveDistance) + start.getZ();
				
		BlockPos moveTo = findValidSurface(world, new BlockPos(x, start.getY(), z), 8);
		
		if ( moveTo != null )
		{
			if ( this.entity.getNavigator().tryMoveToXYZ(moveTo.getX(), moveTo.getY(), moveTo.getZ(), this.movementSpeed) )
			{
				return true;
			}
		}
		
		Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 16, 8, new Vec3d(x,this.entity.posY,z));
		
		if ( vec3d == null || !this.entity.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, this.movementSpeed) )
        {
			vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 8, 8, new Vec3d(x,this.entity.posY,z));
			
			if ( vec3d == null || !this.entity.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, this.movementSpeed ) )
			{
				vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 12, 8, new Vec3d(x,this.entity.posY,z));
				
				if ( vec3d == null || !this.entity.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, this.movementSpeed ) )
				{
					// move away, no path!
					vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 12, 8, new Vec3d(x,this.entity.posY,z));
					
					if ( vec3d == null || !this.entity.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, this.movementSpeed ) )
					{
						return false;
					}
				}
			}
        }
		
		return true;
	}
	
	public static BlockPos findValidSurface( World world, BlockPos startPos, int yOffset )
	{
		IBlockState blockState;
		
		// =-=-=-=-=-= SEARCH UP =-=-=-=-=-=
		BlockPos pos = startPos.down();
		boolean airspace = false;
		boolean floor =  false;
		int y = 0;
		
		while ( yOffset > y )
		{
			blockState = world.getBlockState(pos);
			if ( blockState.getBlock() instanceof BlockLiquid && blockState.getBlock().getDefaultState() != Blocks.WATER )
			{
				return null;
			}
			
			if ( !blockState.getBlock().getDefaultState().isFullCube() )
			{
				if ( floor )
				{
					if ( airspace )
					{
						return pos.down();
					}
					else
					{
						airspace = true;
					}
				}
			}
			else
			{
				floor = true;
				airspace = false;
			}
			pos = pos.up();
			y++;
		}
		// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		
		// =-=-=-=-= SEARCH DOWN =-=-=-=-=
		pos = startPos.up();
		airspace = false;
		floor = false;
		y = 0;
		
		while ( yOffset > y )
		{
			blockState = world.getBlockState(pos);
			if ( blockState.getBlock() instanceof BlockLiquid && blockState.getBlock().getDefaultState() != Blocks.WATER )
			{
				return null;
			}
			
			if ( !blockState.getBlock().getDefaultState().isFullCube() )
			{
				if ( airspace )
				{
					floor = true;
				}
				else
				{
					airspace = true;
				}
			}
			else if ( airspace && floor )
			{
				return pos;
			}
			else
			{
				airspace = false;
				floor = false;
			}
			
			pos = pos.down();
			y++;
		}
		return null;
	}

	public boolean shouldExecute()
	{
		if ( !this.enabled )
		{
			return false;
		}
		
		if ( this.entity.getAttackTarget() != null )
		{
			return false;
		}
		
		if ( this.inCorrectPosition() )
		{
			return false;
		}
		
		return true;
	}
	
	public void updateTask()
    {
		if ( ( this.entity.world.rand.nextBoolean() && this.entity.getNavigator().noPath() ) || this.entity.world.rand.nextInt(32) == 0 )
		{
			this.move(this.entity.world, this.entity.getPosition());
		}
    }
	
	public boolean inCorrectPosition()
	{
		return this.minDistanceFromCenter > this.entity.getDistance(this.entity.posX - centerX, this.entity.posY, this.entity.posX - centerX);
	}
	
	public void startExecuting()
	{
		this.move(this.entity.world, this.entity.getPosition());
	}
}