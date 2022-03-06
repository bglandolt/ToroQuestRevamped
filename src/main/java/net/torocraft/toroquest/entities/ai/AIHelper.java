package net.torocraft.toroquest.entities.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;

public interface AIHelper
{
	
	public static void faceEntitySmart(EntityLivingBase in, EntityLivingBase p)
    {
    	try
    	{
	        double d0 = (p.getPositionVector().x - in.getPositionVector().x) * 2;
	        double d2 = (p.getPositionVector().z - in.getPositionVector().z) * 2;
	        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
	        in.rotationYaw = f;
	        in.prevRotationYaw = f;
    	}
    	catch ( Exception e ) {}
    }
	
	public static void faceEntitySmart(EntityLivingBase in, double x, double z)
    {
    	try
    	{
	        double d0 = (x - in.getPositionVector().x) * 2;
	        double d2 = (z - in.getPositionVector().z) * 2;
	        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
	        in.rotationYaw = f;
	        in.prevRotationYaw = f;
	        in.rotationYawHead = f;
	        in.prevRotationYawHead = f;
    	}
    	catch ( Exception e ) {}
    }
	
	public static void spawnSweepHit( Entity e, Entity target )
    {
        double d0 = (double)(-MathHelper.sin(e.rotationYaw * 0.017453292F));
        double d1 = (double)MathHelper.cos(e.rotationYaw * 0.017453292F);

        if (e.world instanceof WorldServer)
        {
            ((WorldServer)e.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX + d0, e.posY + (double)e.height * 0.5D, target.posZ + d1, 0, d0, 0.0D, d1, 0.0D);
        }
    }
	
//	public static void spawnSweepHit( Entity e, Entity target )
//    {
//        double d0 = (double)(-MathHelper.sin(e.rotationYaw * 0.017453292F));
//        double d1 = (double)MathHelper.cos(e.rotationYaw * 0.017453292F);
//
//        if (e.world instanceof WorldServer)
//        {
//            ((WorldServer)e.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX + d0, target.posY + (double)target.height * 0.5D, target.posZ + d1, 0, d0, 0.0D, d1, 0.0D);
//        }
//    }
	
//	public static void faceEntitySmart(EntityLivingBase in, EntityLivingBase p, float turnSpeed)
//    {
//    	try
//    	{
//    		double prevRotationYaw = in.rotationYaw;
//    		
//	        double d0 = (p.getPositionVector().x - in.getPositionVector().x) * 2;
//	        double d2 = (p.getPositionVector().z - in.getPositionVector().z) * 2;
//	        float f = (float)(MathHelper.atan2(d2+prevRotationYaw*turnSpeed, d0) * (180D / Math.PI)) - 90.0F;
//	        in.rotationYaw = f;
//	        in.prevRotationYaw = f;
//    	}
//    	catch ( Exception e ) {}
//    }
	
//							    public static boolean faceMovingDirection(EntityLiving in)
//							    {
//							    	if ( !in.getNavigator().noPath() )
//							        {
//								    	try
//								    	{
//									    	PathPoint p = in.getNavigator().getPath().getFinalPathPoint();
//									    	PathPoint p2 = in.getNavigator().getPath().getPathPointFromIndex(in.getNavigator().getPath().getCurrentPathIndex());
//							
//									        double d0 = (p.x - in.posX) + (p2.x - in.posX);
//									        double d2 = (p.z - in.posZ) + (p2.z - in.posZ);
//									        //double d1 = p.y - in.posY;
//									
//									        //double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
//									        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
//									        //float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
//									        //in.rotationPitch = f1;
//									        in.rotationYaw = f;
//									        in.prevRotationYaw = f;
//							
//									        return true;
//								    	}
//								    	catch ( Exception e )
//								    	{
//								    		in.prevRotationYaw = in.rotationYaw;
//								    	}
//							        }
//							    	else
//							    	{
//							    		in.prevRotationYaw = in.rotationYaw;
//							    	}
//							    	return false;
//							    }
	
	public static float getMyMovementSpeed(Entity entity)
    {
        return MathHelper.sqrt((entity.motionX * entity.motionX) + (entity.motionZ * entity.motionZ));
    }
	
    public static boolean sfaceMovingDirection(EntityLiving in)
    {
    	if ( in.getNavigator().getPath() != null && AIHelper.getMyMovementSpeed(in) > 0.1F )
        {
	    	try
	    	{
		    	PathPoint p = in.getNavigator().getPath().getFinalPathPoint();
		    	PathPoint p2 = in.getNavigator().getPath().getPathPointFromIndex(in.getNavigator().getPath().getCurrentPathIndex());

		        double d0 = (p.x - in.posX) + (p2.x - in.posX);
		        double d2 = (p.z - in.posZ) + (p2.z - in.posZ);
		        
		        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
		        
		        in.rotationYaw = f;
		        in.prevRotationYaw = f;

		        return true;
	    	}
	    	catch ( Exception e )
	    	{
	    		in.prevRotationYaw = in.rotationYaw;
	    	}
        }
    	else
    	{
    		in.prevRotationYaw = in.rotationYaw;
    	}
    	return false;
    }
    
    
    
//	public static void faceEntitySmart(EntityLivingBase in, EntityLivingBase p)
//    {
//    	try
//    	{
//	        double d0 = (p.getPositionVector().x - in.getPositionVector().x) * 2;
//	        double d2 = (p.getPositionVector().z - in.getPositionVector().z) * 2;
//	        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
//	        in.rotationYaw = f;
//	        in.prevRotationYaw = f;
//	    }
//    	catch ( Exception e ) {}
//    }
//	
//    public static boolean faceMovingDirection(EntityLiving in)
//    {
//    	if ( !in.getNavigator().noPath() )
//        {
//	    	try
//	    	{
//		    	PathPoint p = in.getNavigator().getPath().getFinalPathPoint();
//
//		        double d0 = (p.x - in.posX) * 2;
//		        double d2 = (p.z - in.posZ) * 2;
//		        //double d1 = p.y - in.posY;
//		
//		        //double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
//		        float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
//		        //float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
//		        //in.rotationPitch = f1;
//		        in.rotationYaw = f;
//		        return true;
//	    	}
//	    	catch ( Exception e ) {}
//        }
//    	return false;
//    }
}
