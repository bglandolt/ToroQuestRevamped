package net.torocraft.toroquest.entities;

import com.google.common.base.Predicate;

import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.torocraft.toroquest.ToroQuest;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.ai.EntityAIRaid;
import net.torocraft.toroquest.entities.ai.EntityAIZombieLeap;

public class EntityZombieRaider extends EntityZombie implements IMob
{
	// ========================== DATA MANAGER ==========================

	public static DataParameter<Integer> DESPAWN_TIMER = EntityDataManager.<Integer>createKey(EntityZombieRaider.class, DataSerializers.VARINT);
	public static DataParameter<Integer> RAID_X = EntityDataManager.<Integer>createKey(EntityZombieRaider.class, DataSerializers.VARINT);
	public static DataParameter<Integer> RAID_Z = EntityDataManager.<Integer>createKey(EntityZombieRaider.class, DataSerializers.VARINT);

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.getDataManager().register(DESPAWN_TIMER, Integer.valueOf(100));
		this.getDataManager().register(RAID_X, Integer.valueOf(0));
		this.getDataManager().register(RAID_Z, Integer.valueOf(0));
	}
	
    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        
        compound.setInteger("customDespawnTimer", this.despawnTimer());
        compound.setInteger("raidX", this.getRaidLocationX());
        compound.setInteger("raidZ", this.getRaidLocationZ());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        
        this.setDespawnTimer(compound.getInteger("customDespawnTimer"));
        this.setRaidLocation(compound.getInteger("raidX"), compound.getInteger("raidZ"));
    }
	
	public Integer despawnTimer()
	{
		return this.getDataManager().get(DESPAWN_TIMER);
	}
	
	public Integer despawnTick()
	{
		int d = this.despawnTimer()-1;
		this.getDataManager().set(DESPAWN_TIMER, d);
		return d;
	}
	
	public void setDespawnTimer( int i )
	{
		this.getDataManager().set(DESPAWN_TIMER, i);
	}
    
	protected void setRaidLocation(Integer x, Integer z)
	{
		if ( x != null && z != null && ( x != 0 && z != 0 ) )
		{
			this.getDataManager().set(RAID_X, x);
			this.getDataManager().set(RAID_Z, z);
			this.tasks.addTask(3, new EntityAIRaid(this, x, z, 0.8D));
		}
	}
	
	public Integer getRaidLocationX()
	{
		return this.getDataManager().get(RAID_X).intValue();
	}
	
	public Integer getRaidLocationZ()
	{
		return this.getDataManager().get(RAID_Z).intValue();
	}
	
	// ==================================================================
    
	@Override
	protected boolean canDespawn()
	{
		return false;
	}
		
	@Override
	public boolean hasHome()
	{
		return false;
	}
	
	@Override
	public void onLivingUpdate()
    {
		super.onLivingUpdate();
		
		if ( world.isRemote )
		{
			return;
		}
		
		if ( this.ticksExisted % 100 == 0 )
    	{
			if ( this.despawnTick() < 0 )
    		{
    			if ( this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.getPosition()).grow(25, 15, 25)).isEmpty() || ( this.world.getWorldTime() == 22000 && this.despawnTimer() < -50 ) || ( this.despawnTimer() < -100 ) )
				{
	    			this.setHealth(0);
	    			this.setDead();
	    			return;
				}
    		}
    		    		
            if ( this.getAttackTarget() == null )
            {
            	return;
            }
                            		
    		if ( this.getNavigator().getPathToEntityLiving(this.getAttackTarget()) == null )
    		{
    			Vec3d vector3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this, 8, 8, this.getAttackTarget().getPositionVector());
    				
			    if ( vector3d != null )
			    {
					this.setAttackTarget( null );
					this.setRevengeTarget( null );
			        this.getNavigator().tryMoveToXYZ(vector3d.x, vector3d.y, vector3d.z, 0.8D);
			    }
    		}
    	}
    }
	
	@Override
	protected void initEntityAI()
    {
		this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIZombieLeap(this, 0.38F, false));
        this.tasks.addTask(3, new EntityAIZombieAttack(this, 1.0D, false));
        this.tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
	    this.tasks.addTask(7, new EntityAIMoveThroughVillage(this, 0.8D, false));
        this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 0.8D));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, 20, false, false, new Predicate<EntityVillager>()
		{
			@Override
			public boolean apply(EntityVillager target)
			{
				return true;
			}
		}));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityToroNpc>(this, EntityToroNpc.class, 20, true, false, new Predicate<EntityToroNpc>()
		{
			@Override
			public boolean apply(EntityToroNpc target)
			{
				return true;
			}
		}));
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 20, true, false, new Predicate<EntityPlayer>()
		{
			@Override
			public boolean apply(EntityPlayer target)
			{
				return true;
			}
		}));
    }
	
	@Override
	protected void applyEntityAI()
	{
		// remove target tasks, because mine are more better.
	}
	
	// =========================== ZOMBIE RAIDER ===========================
	
	public EntityZombieRaider(World worldIn)
	{
		super(worldIn);
    	((PathNavigateGround)this.getNavigator()).setBreakDoors(true);

    	int x = this.getRaidLocationX();
	    int z = this.getRaidLocationZ();
	    
	    if ( !( x == 0 && z == 0 ) )
		{
			this.tasks.addTask(7, new EntityAIRaid(this, this.getRaidLocationX(), this.getRaidLocationZ(), 0.8D));
		}
	}

	public EntityZombieRaider(World worldIn, int x, int z)
	{
		super(worldIn);
		this.setRaidLocation(x+rand.nextInt(33)-16, z+rand.nextInt(33)-16);
		this.tasks.addTask(7, new EntityAIRaid(this, x, z, 0.8D));
	}

	public static String NAME = "zombie_raider";
	
	static
	{
		if (ToroQuestConfiguration.specificEntityNames)
		{
			NAME = ToroQuestEntities.ENTITY_PREFIX + NAME;
		}
	}
	public static void init(int entityId)
	{
		EntityRegistry.registerModEntity(new ResourceLocation(ToroQuest.MODID, NAME), EntityZombieRaider.class, NAME, entityId, ToroQuest.INSTANCE, 80, 3,
				true, 0x000000, 0xe000000);
	}
	
}