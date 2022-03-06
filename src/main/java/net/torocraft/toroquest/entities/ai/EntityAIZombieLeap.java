package net.torocraft.toroquest.entities.ai;


import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

public class EntityAIZombieLeap extends EntityAIBase
{
    /** The entity that is leaping. */
    EntityLiving leaper;
    /** The entity that the leaper is leaping towards. */
    EntityLivingBase leapTarget;
    /** The entity's motionY after leaping. */
    double leapMotionY;
    boolean immuneWhileLeaping = false;
    Random rand = new Random();

    public EntityAIZombieLeap(EntityLiving leapingEntity, double d, boolean immuneWhileLeaping)
    {
        this.leaper = leapingEntity;
        this.leapMotionY = d;
        this.immuneWhileLeaping = immuneWhileLeaping;
        this.setMutexBits(0);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        this.leapTarget = this.leaper.getAttackTarget();

        if ( this.leapTarget == null )
        {
            return false;
        }
        else
        {
            double d0 = this.leaper.getDistance(this.leapTarget);

            if ( d0 >= 2.0D && d0 <= 6.0D )
            {
                if ( !this.leaper.onGround )
                {
                    return false;
                }
                else
                {
                    return ( this.leaper.getNavigator().getPathToEntityLiving(this.leapTarget) == null || ( d0 <= 3.0D && rand.nextInt(32) == 0 ) );
                }
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return this.leaper.onGround;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        double d0 = this.leapTarget.posX - this.leaper.posX;
        double d1 = this.leapTarget.posZ - this.leaper.posZ;
        double f = MathHelper.sqrt(d0 * d0 + d1 * d1);

        if ((double)f >= 1.0E-4D)
        {
            this.leaper.motionX += d0 / f * 0.5D + this.leaper.motionX * 0.2D;
            this.leaper.motionZ += d1 / f * 0.5D + this.leaper.motionZ * 0.2D;
        }

        if ( this.leapTarget.posY - this.leaper.posY >= 0.5 )
        {
        	this.leaper.motionY = this.leapMotionY * 1.2D;
        }
        else
        {
        	this.leaper.motionY = this.leapMotionY;
        }
    	if ( immuneWhileLeaping && this.leaper.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue() < 2.22D ) this.leaper.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(2.22D);
    }
}