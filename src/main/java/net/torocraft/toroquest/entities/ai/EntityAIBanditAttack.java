package net.torocraft.toroquest.entities.ai;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.server.command.TextComponentHelper;
import net.torocraft.toroquest.civilization.CivilizationType;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.EntityGuard;
import net.torocraft.toroquest.entities.EntityOrc;
import net.torocraft.toroquest.entities.EntitySentry;
import net.torocraft.toroquest.entities.EntityToroMob;
import net.torocraft.toroquest.entities.EntityToroNpc;
import net.torocraft.toroquest.item.armor.ItemBanditArmor;
import net.torocraft.toroquest.item.armor.ItemLegendaryBanditArmor;

public class EntityAIBanditAttack extends EntityAITarget
{

	protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
	protected final Predicate<EntityLivingBase> targetEntitySelector;
	protected EntityLivingBase targetEntity;

	protected EntityToroMob taskOwner;

	public EntityAIBanditAttack(EntityToroMob npc)
	{
		// checkSight, onlyNearby
		super(npc, false, false);
		this.taskOwner = npc;
		this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(taskOwner);
		this.setMutexBits(1);
		
		this.targetEntitySelector = new Predicate<EntityLivingBase>()
		{
			public boolean apply(@Nullable EntityLivingBase target)
			{
				if (!isSuitableTarget(taskOwner, target, false, false))
				{
					return false;
				}
				
				if ( target instanceof EntityGuard )
				{
					if ( taskOwner instanceof EntityOrc )
					{
						return true;
					}
					
					EntityGuard g = (EntityGuard)target;
					if ( !g.getPlayerGuard().equals("") && g.spawnedNearBandits )
					{
						return false;
					}
				}
				
				if ( target instanceof EntityToroNpc || target instanceof EntityVillager || ( target instanceof EntityPlayer && shouldAttackPlayer((EntityPlayer)target) ) ) // || target instanceof EntityAdventurer )
				{
					return true;
				}
				
				if ( target instanceof EntitySentry )
				{
					if ( target instanceof EntityOrc )
					{
						return !(taskOwner instanceof EntityOrc);
					}
					else
					{
						return taskOwner instanceof EntityOrc;
					}
				}
				
				return false;
			}
		};
	}
	
	protected Province province;
	
	public boolean shouldAttackPlayer( EntityPlayer player )
    {
		//EntityPlayer player = (EntityPlayer)target;
		
		if ( taskOwner.getRevengeTarget() != null && taskOwner.getRevengeTarget() == player )
		{
			return true;
		}
		
		if ( taskOwner.enemy == player )
		{
			return true;
		}
		
		if ( this.taskOwner instanceof EntityOrc )
		{
			if ( !ToroQuestConfiguration.orcsDropMasks )
			{
				// PEACEFUL
				if ( taskOwner.world.getDifficulty() == EnumDifficulty.PEACEFUL )
				{
					return false;
				}
				
				return true;
			}
		}
		else if ( this.taskOwner instanceof EntitySentry )
		{
			EntitySentry sentry = (EntitySentry)taskOwner;
			if ( sentry.enemy == player ) return true;
			
			if ( player.getHeldItemMainhand().getItem() == Items.EMERALD || player.getHeldItemOffhand().getItem() == Items.EMERALD )
			{
				if ( !sentry.getBribed() && sentry.passiveTimer == -1 )
				{
					sentry.passiveTimer = 4;
					sentry.getNavigator().tryMoveToEntityLiving(player, 0.45D+rand.nextDouble()/10.0D);

					boolean flag = true;
					
					if ( sentry.emeraldGreeting && sentry.getDistance(player) < 12 )
					{
						sentry.chat(player, "emeralds", null);
						sentry.emeraldGreeting = false;
					}
					
					List<EntitySentry> bandits = sentry.world.<EntitySentry>getEntitiesWithinAABB(EntitySentry.class, new AxisAlignedBB(sentry.getPosition()).grow(25, 12, 25));
		    		{
		    			for ( EntitySentry bandit : bandits )
		    			{
		    				if ( !bandit.getBribed() && bandit.passiveTimer == -1 )
		    				{
		    					bandit.passiveTimer = 4;
		        				bandit.getNavigator().tryMoveToEntityLiving(player, 0.45D+rand.nextDouble()/10.0D);
		        				bandit.emeraldGreeting = flag;
		    				}
		    			}
		    		}
					return false;
				}
			}
			
			if ( sentry.getBribed() || sentry.passiveTimer > 0 )
			{
				return false;
			}
			else if ( sentry.passiveTimer == 0 )
			{
				// PEACEFUL
				if ( sentry.world.getDifficulty() == EnumDifficulty.PEACEFUL )
				{
					return false;
				}
				
				sentry.chat(player, "betray", null);
				return true;
			}
		
			int totalRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.EARTH);
			totalRep += PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.FIRE);
			totalRep += PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.MOON);
			totalRep += PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.WATER);
			totalRep += PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.SUN);
			totalRep += PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.WIND);
			
			if ( totalRep <= -50 )
			{
//				sentry.passiveTimer = 4;
//				
//				List<EntitySentry> bandits = sentry.world.<EntitySentry>getEntitiesWithinAABB(EntitySentry.class, new AxisAlignedBB(sentry.getPosition()).grow(25, 12, 25));
//	    		{
//	    			for ( EntitySentry bandit : bandits )
//	    			{
//	    				bandit.passiveTimer = 4;
//	    			}
//	    		}

				String bandit = "";
				
				totalRep = 0;
	
				if ( PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.EARTH) < totalRep )
				{
					totalRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.EARTH);
					bandit = TextComponentHelper.createComponentTranslation(player, "civilization.earth.name", new Object[0]).toString();
				}
				if ( PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.FIRE) < totalRep )
				{
					totalRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.FIRE);
					bandit = TextComponentHelper.createComponentTranslation(player, "civilization.fire.name", new Object[0]).toString();
				}
				if ( PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.MOON) < totalRep )
				{
					totalRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.MOON);
					bandit = TextComponentHelper.createComponentTranslation(player, "civilization.moon.name", new Object[0]).toString();
				}
				if ( PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.WATER) < totalRep )
				{
					totalRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.WATER);
					bandit = TextComponentHelper.createComponentTranslation(player, "civilization.water.name", new Object[0]).toString();
				}
				if ( PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.SUN) < totalRep )
				{
					totalRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.SUN);
					bandit = TextComponentHelper.createComponentTranslation(player, "civilization.sun.name", new Object[0]).toString();
				}
				if ( PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.WIND) < totalRep )
				{
					totalRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(CivilizationType.WIND);
					bandit = TextComponentHelper.createComponentTranslation(player, "civilization.wind.name", new Object[0]).toString();
				}
				
				sentry.chat(player, "outlaw", TextComponentHelper.createComponentTranslation(player, "civilization.house.name", new Object[0]).toString() + " " + bandit);
				return false;
			}
			
			for ( ItemStack i: player.inventory.armorInventory )
			{
				if ( i.getItem() instanceof ItemBanditArmor || i.getItem() instanceof ItemLegendaryBanditArmor )
				{
					boolean flag = true;
					
					if ( sentry.helloGreeting && sentry.getDistance(player) < 12 )
					{
						sentry.chat(player, "hello", null);
						sentry.helloGreeting = false;
					}
					
					List<EntitySentry> bandits = sentry.world.<EntitySentry>getEntitiesWithinAABB(EntitySentry.class, new AxisAlignedBB(sentry.getPosition()).grow(25, 12, 25));
		    		{
		    			for ( EntitySentry bandit : bandits )
		    			{
		    				bandit.helloGreeting = flag;
		    			}
		    		}
		    		
					return false;
				}
			}
		}
		
		// PEACEFUL
		if ( taskOwner.world.getDifficulty() == EnumDifficulty.PEACEFUL )
		{
			return false;
		}
		
		return true;
    }
	
	Random rand = new Random();
	
	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute()
	{
		if ( this.taskOwner.getAttackTarget() != null || this.rand.nextInt(12) != 0 )
		{
			return false;
	    }
		
		List<EntityLivingBase> list = this.taskOwner.world.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.taskOwner.getPosition()).grow(30, 12, 30), this.targetEntitySelector);
		
		if (list.isEmpty())
		{
			return false;
		}

		Collections.sort(list, this.theNearestAttackableTargetSorter);

		for ( EntityLivingBase npc : list )
		{
			if ( this.taskOwner.canEntityBeSeen(npc) && !npc.isInvisible() )
			{
				if ( npc instanceof EntityPlayer && ( !npc.isSprinting() && this.rand.nextInt((int)this.taskOwner.getDistance(npc)+8) > (npc.isSneaking()?16:8) ) )
				{
					continue;
				}
				targetEntity = npc;
				return true;
			}
		}
		for ( EntityLivingBase npc : list )
		{
			if ( npc instanceof EntityVillager || this.taskOwner.getDistance(npc) <= 5.0D )
			{
				targetEntity = npc;
				return true;
			}
		}
		targetEntity = null;
		return false;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting()
	{
		this.taskOwner.setAttackTarget(this.targetEntity);
		super.startExecuting();
	}

	public static class Sorter implements Comparator<Entity>
	{
		private final Entity theEntity;

		public Sorter(Entity theEntityIn)
		{
			this.theEntity = theEntityIn;
		}

		public int compare(Entity p_compare_1_, Entity p_compare_2_) {
			double d0 = this.theEntity.getDistanceSq(p_compare_1_);
			double d1 = this.theEntity.getDistanceSq(p_compare_2_);
			return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
		}
	}
	
 	@Nullable
    private BlockPos getRandPos(World worldIn, Entity entityIn, int horizontalRange, int verticalRange)
    {
        BlockPos blockpos = new BlockPos(entityIn);
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();
        float f = (float)(horizontalRange * horizontalRange * verticalRange * 2);
        BlockPos blockpos1 = null;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int l = i - horizontalRange; l <= i + horizontalRange; ++l)
        {
            for (int i1 = j - verticalRange; i1 <= j + verticalRange; ++i1)
            {
                for (int j1 = k - horizontalRange; j1 <= k + horizontalRange; ++j1)
                {
                    blockpos$mutableblockpos.setPos(l, i1, j1);
                    IBlockState iblockstate = worldIn.getBlockState(blockpos$mutableblockpos);

                    if (iblockstate.getMaterial() == Material.WATER)
                    {
                        float f1 = (float)((l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k));

                        if (f1 < f)
                        {
                            f = f1;
                            blockpos1 = new BlockPos(blockpos$mutableblockpos);
                        }
                    }
                }
            }
        }

        return blockpos1;
    }
}