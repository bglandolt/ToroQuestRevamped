package net.torocraft.toroquest.entities.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAIRaidDespawn extends EntityAIRaid
{
	public EntityAIRaidDespawn( EntityCreature entity, Integer x, Integer z, double speedIn )
	{
		super(entity, x, z, speedIn);
	}

	@Override
	public void updateTask()
	{
		try
		{
			if ( this.entity.ticksExisted % 100 == 0 )
			{
				int dt = this.entity.getEntityData().getInteger("despawnTimer");

				if ( dt < 0 )
				{
					if ( this.entity.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.entity.getPosition()).grow(25, 15, 25)).isEmpty() || (this.entity.world.getWorldTime() == 22000 || dt < -50) || dt < -100 )
					{
						this.entity.setHealth(0);
						this.entity.setDead();
						return;
					}
				}

				this.entity.getEntityData().setInteger("despawnTimer", --dt);

				// System.out.println(this.entity.getEntityData().getInteger("despawnTimer"));
			}
		}
		catch (Exception e)
		{
			this.entity.setHealth(0);
			this.entity.setDead();
			return;
		}
		super.updateTask();
	}
}