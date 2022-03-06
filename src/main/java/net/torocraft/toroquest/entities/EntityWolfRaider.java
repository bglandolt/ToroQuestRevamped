package net.torocraft.toroquest.entities;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.torocraft.toroquest.ToroQuest;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.ai.EntityAIRaid;

public class EntityWolfRaider extends EntityWolf implements IMob
{

	// ========================== DATA MANAGER ==========================

	public static DataParameter<Integer> DESPAWN_TIMER = EntityDataManager.<Integer>createKey(EntityWolfRaider.class, DataSerializers.VARINT);
	public static DataParameter<Integer> RAID_X = EntityDataManager.<Integer>createKey(EntityWolfRaider.class, DataSerializers.VARINT);
	public static DataParameter<Integer> RAID_Z = EntityDataManager.<Integer>createKey(EntityWolfRaider.class, DataSerializers.VARINT);

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
			this.tasks.addTask(3, new EntityAIRaid(this, x, z, 1.0D));
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
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
	    this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
	}

	public EntityWolfRaider(World worldIn)
	{
		super(worldIn);
		this.setAngry(true);
		this.setTamed(false);
		
		if ( this.getRaidLocationX() != 0 && this.getRaidLocationZ() != 0 ) 
		{
			this.tasks.addTask(7, new EntityAIRaid(this, this.getRaidLocationX(), this.getRaidLocationZ(), 1.0D));
		}
	}
	
	public EntityWolfRaider(World worldIn, int x, int z)
	{
		this(worldIn);
		this.setRaidLocation(x+rand.nextInt(33)-16, z+rand.nextInt(33)-16);
		this.tasks.addTask(7, new EntityAIRaid(this, x, z, 1.0D));
	}
	
	@Override
	public boolean hasHome()
	{
		return false;
	}
	
	@Override
	protected boolean canDespawn()
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

	public static String NAME = "wolf_raider";
	static
	{
		if (ToroQuestConfiguration.specificEntityNames)
		{
			NAME = ToroQuestEntities.ENTITY_PREFIX + NAME;
		}
	}
	public static void init(int entityId)
	{
		EntityRegistry.registerModEntity(new ResourceLocation(ToroQuest.MODID, NAME), EntityWolfRaider.class, NAME, entityId, ToroQuest.INSTANCE, 80, 3,
				true, 0x203090, 0xe09939);
	}
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
		return false;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void initEntityAI()
    {
		this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAILeapAtTarget(this, 0.45F));
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.2D, true));
	    this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 0.8D, false));
        this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 0.8D));
        this.tasks.addTask(10, new EntityAILookIdle(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
        this.targetTasks.addTask(4, new EntityAITargetNonTamed(this, EntityAnimal.class, true, new Predicate<EntityAnimal>()
        {
            public boolean apply(@Nullable EntityAnimal p_apply_1_)
            {
                return ( despawnTimer() < 48 && !( p_apply_1_ instanceof EntityWolf ) );
            }
        }));
		this.targetTasks.addTask(5, new EntityAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, 20, false, false, new Predicate<EntityVillager>()
		{
			@Override
			public boolean apply(EntityVillager target)
			{
				return true;
			}
		}));
        this.targetTasks.addTask(6, new EntityAINearestAttackableTarget<EntityToroNpc>(this, EntityToroNpc.class, 20, true, false, new Predicate<EntityToroNpc>()
		{
			@Override
			public boolean apply(EntityToroNpc target)
			{
				return true;
			}
		}));
        this.targetTasks.addTask(7, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 20, false, false, new Predicate<EntityPlayer>()
		{
			@Override
			public boolean apply(EntityPlayer target)
			{
				return true;
			}
		}));
    }
}