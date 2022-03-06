package net.torocraft.toroquest.entities;

import java.util.Random;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AIAttackWithSword extends EntityAIBase
{
	protected World world;
    protected EntityCreature attacker;
    protected int attackTick = 0;
    protected double speedTowardsTarget;
    protected Path path;
    protected boolean offhandAttack = false;
	protected double range = 3.275D;
	protected Random rand = new Random();

    public AIAttackWithSword(EntityCreature creature, double speedIn)
    {
        this.attacker = creature;
        this.world = creature.world;
        this.speedTowardsTarget = speedIn;
        this.setMutexBits(3);
    }
    
    public boolean shouldExecute()
    {
        if ( !this.shouldContinueExecuting() )
        {
        	return false;
        }
        
        if ( this.attacker instanceof EntitySentry )
        {
        	
        }
        else
        {
	        this.path = this.attacker.getNavigator().getPathToEntityLiving(this.attacker.getAttackTarget());
	
	        if ( this.path != null )
	        {
	            return true;
	        }
        }
        
        return this.getAttackReachSqr(this.attacker.getAttackTarget()) >= this.attacker.getDistanceSq(this.attacker.getAttackTarget().posX, this.attacker.getAttackTarget().getEntityBoundingBox().minY, this.attacker.getAttackTarget().posZ);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {    	
		if ( this.attacker.getHeldItemMainhand().getItem() instanceof ItemBow )
		{
    		return false;
    	}
		
        if ( this.attacker.getAttackTarget() == null )
        {
            return false;
        }
        
        if ( !this.attacker.getAttackTarget().isEntityAlive() )
        {
            return false;
        }

        return true;
    }
    
    public void startExecuting()
    {
    	ItemStack iStack = this.attacker.getHeldItemMainhand();

        if ( iStack != null && !iStack.isEmpty() )
    	{
        	String s = iStack.getItem().getRegistryName().toString();
        	
        		 if ( s.contains("pike_") ) 		{this.range = 4.525D;}
        	else if ( s.contains("spear_") )  	 	{this.range = 3.85D;}
        	else if ( s.contains("glaive_") ) 		{this.range = 3.85D;}
        	else if ( s.contains("halberd_") ) 		{this.range = 3.85D;}
        	else if ( s.contains("greatsword_") ) 	{this.range = 3.85D;}
        	else if ( s.contains("lance_") ) 		{this.range = 3.85D;}
        	else if ( s.contains("staff") ) 		{this.range = 3.85D;}
    	}

		this.attacker.getNavigator().setPath( this.path, this.speedTowardsTarget );
    }

    @Override
    public void resetTask()
    {
    	this.attacker.setSprinting(false);
    }

    public void updateTask()
    {
    	if ( !shouldContinueExecuting() )
    	{
    		return;
    	}
        
        this.attacker.faceEntity(this.attacker.getAttackTarget(), 30.0F, 30.0F);
        this.attacker.getLookHelper().setLookPositionWithEntity(this.attacker.getAttackTarget(), 30.0F, 30.0F);
        
        double d0 = this.attacker.getDistanceSq(this.attacker.getAttackTarget().posX, this.attacker.getAttackTarget().getEntityBoundingBox().minY, this.attacker.getAttackTarget().posZ);
        //this.attackTick = Math.max(this.attackTick - 1, 0);
        this.attackTick--;
        this.checkAndPerformAttack(this.attacker.getAttackTarget(), d0);
    }

    protected void checkAndPerformAttack(EntityLivingBase victim, double distanceSq)
    {        
        boolean backPeddaling = false;
        
        if ( this.attacker instanceof EntitySentry )
        {
        	EntitySentry e = (EntitySentry)this.attacker;
        	if ( e.isDrinkingPotion() )
			{
            	this.attackTick = 10;
        		backPeddaling = true;
				return;
			}
        	if ( e.stance < 5 )
        	{
        		backPeddaling = true;
        	}
        	if ( e.flanking )
        	{
        		backPeddaling = true;
        	}
        }
        else if ( this.attacker instanceof EntityGuard )
        {
    		EntityGuard e = (EntityGuard)this.attacker;
    		if ( e.stance < 5 )
         	{
         		backPeddaling = true;
         	}
        }
        
    	this.attacker.setSprinting(false);
    	
        if ( !this.attacker.collidedHorizontally && !backPeddaling && !this.attacker.isHandActive() ) // this.attacker.getNavigator().getPathToEntityLiving(victim) != null
        {
        	int tt = Math.abs(this.attackTick-5) % 40;
        	
        	if ( tt < 3 )
        	{
            	this.attacker.setSprinting(true);
            	if ( tt == 0 && distanceSq <= 12 )
            	{
            		if ( distanceSq >= 3.5D && this.attacker.onGround && this.rand.nextInt(5) == 0 )
            		{
            			if ( !this.world.isRemote ) 
        		        {
            		        Vec3d velocityVector = new Vec3d(victim.posX - this.attacker.posX, 0, victim.posZ - this.attacker.posZ);
        		        	this.attacker.addVelocity((velocityVector.x)/12.0,0.3D,(velocityVector.z)/12.0);
        		        	this.attacker.velocityChanged = true;
        		        }
            		}
            		else
            		{
            			if ( !this.world.isRemote ) 
        		        {
            		        Vec3d velocityVector = new Vec3d(victim.posX - this.attacker.posX, 0, victim.posZ - this.attacker.posZ);
        		        	this.attacker.addVelocity((velocityVector.x)/10.0,0.02D,(velocityVector.z)/10.0);
        		        	this.attacker.velocityChanged = true;
        		        }
            		}
            	}
        	}
        }
        
        double attackDistance = this.getAttackReachSqr(victim);

        if ( this.attacker.getActiveHand().equals(EnumHand.OFF_HAND) )
        {
        	this.attackTick = 7; // for adding an attack delay after blocking
        }
        else if ( distanceSq <= attackDistance )
        {
        	if ( this.attackTick <= 0 )
        	{
	            if ( !offhandAttack )
	            {
	            	this.attacker.swingArm(EnumHand.MAIN_HAND);
	            	this.attackTick = 20+rand.nextInt(11);
	            	this.attacker.attackEntityAsMob(victim);
	            }
	            else
	            {
	            	this.attacker.swingArm(EnumHand.OFF_HAND);
	            	this.attackTick = 12+rand.nextInt(9);
	            	offhandAttack = false;
	            	this.attacker.attackEntityAsMob(victim);
	            	return;
	            }
	        	
	            ItemStack iStack = this.attacker.getHeldItem(EnumHand.OFF_HAND);
	            
	            if ( iStack != null && !( iStack.isEmpty() ) && !( iStack.getItem() instanceof ItemBow ) && !( iStack.getItem() instanceof ItemPotion ) && !( iStack.getItem() instanceof ItemShield ) )
	            {
	            	offhandAttack = true;
	            	this.attackTick = 12+rand.nextInt(9);
	            }
        	}
        }
    }
    
    protected double getAttackReachSqr(EntityLivingBase attackTarget)
    {
        return (this.attacker.width * this.range * this.attacker.width * this.range + attackTarget.width + (this.rand.nextDouble()/8.0D));
    }

	public static boolean canReach(EntityCreature creature)
	{
		return !creature.getNavigator().noPath();
	}
}