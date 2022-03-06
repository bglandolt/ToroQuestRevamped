package net.torocraft.toroquest.entities.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import net.torocraft.toroquest.entities.EntityGuard;

public class EntityAIPatrolVillage extends EntityAIBase
{
    private final EntityGuard entity;
    private final double movementSpeed;
    /** The PathNavigate of our entity. */
    private Path path;
    private VillageDoorInfo doorInfo;
    private final List<VillageDoorInfo> doorList = Lists.<VillageDoorInfo>newArrayList();
    private Random rand = new Random();

    private Village village = null;
    
    public EntityAIPatrolVillage(EntityGuard entityIn, double movementSpeedIn)
    {
        this.entity = entityIn;
        this.movementSpeed = movementSpeedIn;
        this.setMutexBits(1);

//        if (!(entityIn.getNavigator() instanceof PathNavigateGround))
//        {
//            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
//        }
    }

    private boolean hasPath = false;
    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
    	this.hasPath = this.entity.hasPath();
    	
    	if ( this.entity.isInWater() && !this.hasPath )
    	{
    		Vec3d vec3d = RandomPositionGenerator.getLandPos(this.entity, 12, 6);
            if ( vec3d != null )
            {
            	this.path = this.entity.getNavigator().getPathToXYZ(vec3d.x, vec3d.y, vec3d.z);
            	if ( path == null )
            	{
            		return false;
            	}
            	else
            	{
                	AIHelper.faceEntitySmart(this.entity, (int)vec3d.x, (int)vec3d.z);
                	return true;
            	}
            }
    	}
    	
    	if ( this.rand.nextInt((this.hasPath?32:128)) != 0 )
    	{
    		return false;
    	}

    	if ( this.entity.actionTimer > 3 || this.entity.isAnnoyed() )
    	{
    		return false;
    	}
    	
    	if ( this.entity.getRaidLocationX() != 0 && this.entity.getRaidLocationZ() != 0 )
    	{
        	int d = (int) this.entity.getDistance(this.entity.getRaidLocationX(), this.entity.posY, this.entity.getRaidLocationZ());
        	
        	if ( this.entity.returningToPost || this.rand.nextInt(32)+4 < d )
        	{
        		if ( this.entity.returningToPost = this.entity.returnToPost() )
        		{
        			return false;
        		}
        		else
        		{
        			return true;
        		}
        	}
    	}

    	if ( rand.nextInt(3) == 0 )
    	{
    		Vec3d vec3d = RandomPositionGenerator.getLandPos(this.entity, 12, 6);
    		
            if ( vec3d != null )
            {
            	if ( path == null )
            	{
            		return false;
            	}
            	else
            	{
                	AIHelper.faceEntitySmart(this.entity, (int)vec3d.x, (int)vec3d.z);
                	return true;
            	}
            }
    	}
    	else
    	{
            if ( this.village == null )
            {
                this.village = this.entity.world.getVillageCollection().getNearestVillage(new BlockPos(this.entity), 16);
                
                if ( this.village == null )
                {
                	return false;
                }
            }
            
            int i = this.rand.nextInt(this.village.getVillageDoorInfoList().size());
            
            if ( i > 0 )
            {
            	this.doorInfo = this.village.getVillageDoorInfoList().get(i);
            }
            
            if ( this.doorInfo == null )
            {
            	return false;
            }
            
            PathNavigateGround pathnavigateground = (PathNavigateGround)this.entity.getNavigator();
            boolean flag = pathnavigateground.getEnterDoors();
            pathnavigateground.setBreakDoors(false);
            this.path = pathnavigateground.getPathToPos(this.doorInfo.getDoorBlockPos());
            pathnavigateground.setBreakDoors(flag);

            if (this.path != null)
            {
                return true;
            }
            else
            {
                Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 16, 8, new Vec3d((double)this.doorInfo.getDoorBlockPos().getX(), (double)this.doorInfo.getDoorBlockPos().getY(), (double)this.doorInfo.getDoorBlockPos().getZ()));

                if (vec3d == null)
                {
                    return false;
                }
                else
                {
                    pathnavigateground.setBreakDoors(false);
                    this.path = this.entity.getNavigator().getPathToXYZ(vec3d.x, vec3d.y, vec3d.z);
                    pathnavigateground.setBreakDoors(flag);
                    return this.path != null;
                }
            }
    	}
		return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
//    public boolean shouldContinueExecuting()
//    {
//        if (this.entity.getNavigator().noPath())
//        {
//            return false;
//        }
//        else
//        {
//			float f = this.entity.width + 4.0F;
//            return this.entity.getDistanceSq(this.doorInfo.getDoorBlockPos()) > (double)(f * f);
//        }
//    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.entity.getNavigator().setPath(this.path, this.movementSpeed);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
//        if (this.entity.getNavigator().noPath() || this.entity.getDistanceSq(this.doorInfo.getDoorBlockPos()) < 16.0D)
//        {
//            this.doorList.add(this.doorInfo);
//        }
    }

//    private List<VillageDoorInfo> findNearestDoor(Village villageIn)
//    {
//    	ArrayList<VillageDoorInfo> villagedoorinfo = new ArrayList<VillageDoorInfo>();
//        
//        int i = Integer.MAX_VALUE;
//
//        for ( VillageDoorInfo v : villageIn.getVillageDoorInfoList() )
//        {
//            int j = v.getDistanceSquared(MathHelper.floor(this.entity.posX), MathHelper.floor(this.entity.posY), MathHelper.floor(this.entity.posZ));
//
//            if ( j < i && !this.doesDoorListContain(v) )
//            {
//                villagedoorinfo.add(v);
//                i = j;
//            }
//        }
//
//        return villagedoorinfo;
//    }

//    private boolean doesDoorListContain(VillageDoorInfo doorInfoIn)
//    {
//        for (VillageDoorInfo villagedoorinfo : this.doorList)
//        {
//            if (doorInfoIn.getDoorBlockPos().equals(villagedoorinfo.getDoorBlockPos()))
//            {
//                return true;
//            }
//        }
//
//        return false;
//    }

//    private void resizeDoorList()
//    {
//        if (this.doorList.size() > 15)
//        {
//            this.doorList.remove(0);
//        }
//    }
}