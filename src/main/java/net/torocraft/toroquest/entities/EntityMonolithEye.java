package net.torocraft.toroquest.entities;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.ToroQuest;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.render.RenderMonolithEye;
import net.torocraft.toroquest.generation.WorldGenPlacer;

public class EntityMonolithEye extends EntityMob implements IRangedAttackMob, IMob
{
	
	// replace with wither?
	
	public static String NAME = "monolitheye";
	static
	{
		if (ToroQuestConfiguration.specificEntityNames)
		{
			NAME = ToroQuestEntities.ENTITY_PREFIX + NAME;
		}
	}
	
	@Override
	public boolean getAlwaysRenderNameTag()
    {
        return false;
    }
	
    public boolean startRiding(Entity entityIn, boolean force)
    {
    	return false;
    }
    
	@Override
	protected void updateLeashedState()
    {
	   this.clearLeashed(true, true);
       return;
    }
	
	@Override
	public boolean canBeLeashedTo(EntityPlayer player)
    {
		return false;
    }
	
	public EntityMonolithEye(World worldIn)
	{
		super(worldIn);
		this.enablePersistence();
		this.setSize(20.0F, 38.0F);
		this.setRealSize(20.0F, 38.0F);
		this.isImmuneToFire = true;
        this.bossInfo.setColor(BossInfo.Color.WHITE);
		this.setEntityInvulnerable(true);
		this.setNoGravity(true);
		this.experienceValue = 440;
	}
	
	public EntityMonolithEye(World worldIn, int x, int y, int z)
	{
		this(worldIn);
		this.setRaidLocation(x, y, z);
	}
	
	protected void setRealSize(float width, float height)
    {
        if (width != this.width || height != this.height)
        {
            float f = this.width;
            this.width = width;
            this.height = height;

            if (this.width < f)
            {
                double d0 = (double)width / 2.0D;
                this.setEntityBoundingBox(new AxisAlignedBB(this.posX - d0, this.posY, this.posZ - d0, this.posX + d0, this.posY + (double)this.height, this.posZ + d0));
                return;
            }

            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)this.width, axisalignedbb.minY + (double)this.height, axisalignedbb.minZ + (double)this.width));

            if (this.width > f && !this.firstUpdate && !this.world.isRemote)
            {
                this.move(MoverType.SELF, (double)(f - this.width), 0.0D, (double)(f - this.width));
            }
        }
    }
	
	@Override
	@Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
		WorldGenPlacer.clearTrees(this.world, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 56);
        
		if ( (int)this.posY != 0 && this.getRaidLocationY() == 0 )
		{
			this.setRaidLocation((int)this.posX, (int)this.posY, (int)this.posZ);
		}
		
		return super.onInitialSpawn(difficulty, livingdata);
    }
	
	// ============================================================================================================================
	
	protected static final DataParameter<Integer> RAID_X = EntityDataManager.<Integer>createKey(EntityMonolithEye.class, DataSerializers.VARINT);
	protected static final DataParameter<Integer> RAID_Y = EntityDataManager.<Integer>createKey(EntityMonolithEye.class, DataSerializers.VARINT);
	protected static final DataParameter<Integer> RAID_Z = EntityDataManager.<Integer>createKey(EntityMonolithEye.class, DataSerializers.VARINT);

	@Override
	protected void entityInit()
	{
		super.entityInit();
		
		this.getDataManager().register(RAID_X, Integer.valueOf(0));
		this.getDataManager().register(RAID_Y, Integer.valueOf(0));
		this.getDataManager().register(RAID_Z, Integer.valueOf(0));
	}
	
    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        
        compound.setInteger("raidX", this.getRaidLocationX());
        compound.setInteger("raidY", this.getRaidLocationY());
        compound.setInteger("raidZ", this.getRaidLocationZ());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        
        this.setRaidLocation(compound.getInteger("raidX"), compound.getInteger("raidY"), compound.getInteger("raidZ"));
    }
	
	protected void setRaidLocation(int x, int y, int z)
	{
		this.getDataManager().set(RAID_X, x);
		this.getDataManager().set(RAID_Y, y);
		this.getDataManager().set(RAID_Z, z);
	}
	
	public Integer getRaidLocationX()
	{
		return this.getDataManager().get(RAID_X).intValue();
	}
	
	public Integer getRaidLocationY()
	{
		return this.getDataManager().get(RAID_Y).intValue();
	}
	
	public Integer getRaidLocationZ()
	{
		return this.getDataManager().get(RAID_Z).intValue();
	}
		
	// ============================================================================================================================
		
	@Override
	public void onDeath(DamageSource cause)
	{
		super.onDeath(cause);
		
		if ( !this.world.isRemote )
		{
			this.dropBossLoot();
			
			int x = this.getRaidLocationX();
			int y = this.getRaidLocationY();
			int z = this.getRaidLocationZ();
			
			if ( y == 0 )
			{
				x = this.getPosition().getX();
				y = this.getPosition().getY();
				z = this.getPosition().getZ();
			}
			
			int range = 64;
			for ( int xx = -range; xx < range; xx++ )
			{
				for ( int yy = -32; yy < 8; yy++ )
				{
					for ( int zz = -range; zz < range; zz++ )
					{
						BlockPos pos = new BlockPos(x+xx,y+yy,z+zz);
						world.extinguishFire(null, pos, EnumFacing.UP);
						IBlockState block = world.getBlockState(pos);
						if ( block == Blocks.OBSIDIAN.getDefaultState() )
						{
							world.setBlockState(pos, Blocks.GRAVEL.getDefaultState() );
						}
					}
				}
			}
		}
	}
	
	@Override
    public boolean canBePushed()
    {
        return false;
    }
	
//	static class AIFireballAttack extends EntityAIBase
//    {
//        private final EntityMonolithEye parentEntity;
//        public int attackTimer;
//    	public int attacks = 3;
//
//        public AIFireballAttack(EntityMonolithEye ghast)
//        {
//            this.parentEntity = ghast;
//        }
//
//        /**
//         * Returns whether the EntityAIBase should begin execution.
//         */
//        public boolean shouldExecute()
//        {
//            return this.parentEntity.getAttackTarget() != null;
//        }
//
//        /**
//         * Execute a one shot task or start executing a continuous task
//         */
//        public void startExecuting()
//        {
//            this.attackTimer = 0;
//        }
//
//        /**
//         * Keep ticking a continuous task that has already been started
//         */
//        public void updateTask()
//        {
//            EntityLivingBase entitylivingbase = this.parentEntity.getAttackTarget();
//            
//            if ( entitylivingbase == null )
//            {
//            	return;
//            }
//            
//            this.parentEntity.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
//
//            if (entitylivingbase.getDistanceSq(this.parentEntity) < 9216.0D)
//            {
//                World world = this.parentEntity.world;
//                ++this.attackTimer;
//
//                if (this.attackTimer == 5)
//                {
//                    world.playEvent((EntityPlayer)null, 1015, new BlockPos(this.parentEntity), 0);
//                	this.attacks = 3;
//                }
//
//                
//                if (this.attackTimer == 20)
//                {
//                    Vec3d vec3d = this.parentEntity.getLook(1.0F);
//                    double d2 = entitylivingbase.posX - (this.parentEntity.posX + vec3d.x * 4.0D);
//                    double d3 = entitylivingbase.getEntityBoundingBox().minY + (double)(entitylivingbase.height / 4.0F) - (this.parentEntity.posY);
//                    double d4 = entitylivingbase.posZ - (this.parentEntity.posZ + vec3d.z * 4.0D);
//                    world.playEvent((EntityPlayer)null, 1016, new BlockPos(this.parentEntity), 0);
//                    EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, this.parentEntity, d2, d3, d4);
//                    entitylargefireball.explosionPower = 2;
//                    entitylargefireball.posX = this.parentEntity.posX + vec3d.x * 4.0D;
//                    entitylargefireball.posY = this.parentEntity.posY + (double)(this.parentEntity.height / 2.0F) + 0.5D;
//                    entitylargefireball.posZ = this.parentEntity.posZ + vec3d.z * 4.0D;
//                    world.spawnEntity(entitylargefireball);
//                    if ( this.attacks > 0 )
//                    {
//                    	this.attackTimer = 10;
//                    	this.attacks--;
//                    }
//                    else
//                    {
//                    	this.attackTimer = -40;
//                    	this.attacks = 3;
//                    }
//                }
//            }
//            else if (this.attackTimer > 0)
//            {
//                this.attackTimer = 0;
//            	this.attacks = 3;
//            }
//        }
//
//    }
	
	public static void registerRenders()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityMonolithEye.class, new IRenderFactory<EntityMonolithEye>()
		{
			@Override
			public Render<EntityMonolithEye> createRenderFor(RenderManager manager)
			{
				return new RenderMonolithEye(manager);
			}
		});
	}

	@Override
	protected void collideWithEntity(Entity entityIn)
	{
		super.collideWithEntity(entityIn);
		
		if ( entityIn instanceof EntityLivingBase )
		{
			float damage = 6.0f;
	
			DamageSource ds = DamageSource.causeIndirectMagicDamage(this, this);
	
			this.spawnExplosionParticle();
			entityIn.attackEntityFrom(ds, damage);
			entityIn.attackEntityFrom(DamageSource.causeMobDamage(this),
			(float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
			this.world.setEntityState(this, (byte) 15);
			this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
			if ( !this.world.isRemote )
			{
				Vec3d velocityVector = new Vec3d(entityIn.posX - this.posX,entityIn.posY -  this.posY,entityIn.posZ -  this.posZ);
				entityIn.addVelocity((velocityVector.x),(velocityVector.y),(velocityVector.z));
				entityIn.velocityChanged = true;
			}
		}
	}

	public static void init(int entityId)
	{
		EntityRegistry.registerModEntity(new ResourceLocation(ToroQuest.MODID, NAME), EntityMonolithEye.class, NAME, entityId, ToroQuest.INSTANCE, 80,
				3, true, 0xff3024, 0xe0d6b9);
	}
	

	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	protected void initEntityAI()
	{
		//this.tasks.addTask(3, new EntityMonolithEye.AIFireballAttack(this));
		//this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
		//this.tasks.addTask(1, new EntityAIStayCentered(this));
		//tasks.addTask(4, new AIAttack(this));
		// this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 64.0F));
		//this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, true, false, new MonolithEyeTargetSelector(this)));
	}

	static class MonolithEyeTargetSelector implements Predicate<EntityLivingBase> {
		private final EntityMonolithEye parentEntity;

		public MonolithEyeTargetSelector(EntityMonolithEye guardian) {
			this.parentEntity = guardian;
		}

		public boolean apply(@Nullable EntityLivingBase e)
		{
			return (e instanceof EntityLivingBase  && e.getDistanceSq(this.parentEntity) > 512.0D );
		}
	}

	protected SoundEvent getAmbientSound()
	{
		EntityLivingBase entitylivingbase = this.getAttackTarget();
		if ( entitylivingbase != null )
		{
			this.world.playSound((EntityPlayer)null, entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, SoundEvents.ENTITY_ENDERMEN_STARE, this.getSoundCategory(), 1.0F, 0.7F + rand.nextFloat()/100.0F);
		}
		return SoundEvents.ENTITY_ENDERMEN_AMBIENT;
	}

	protected SoundEvent getHurtSound()
	{
		EntityLivingBase entitylivingbase = this.getRevengeTarget();
		if ( entitylivingbase != null )
		{
			this.world.playSound((EntityPlayer)null, entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, SoundEvents.ENTITY_ENDERDRAGON_HURT, this.getSoundCategory(), 1.0F, 0.8F + rand.nextFloat()/100.0F);
		}
		return SoundEvents.ENTITY_ENDERDRAGON_HURT;
	}

	protected SoundEvent getDeathSound()
	{
		EntityLivingBase entitylivingbase = this.getRevengeTarget();
		if ( entitylivingbase != null )
		{
			this.world.playSound((EntityPlayer)null, entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, SoundEvents.ENTITY_ENDERDRAGON_DEATH, this.getSoundCategory(), 1.0F, 0.8F + rand.nextFloat()/100.0F);
		}
		return SoundEvents.ENTITY_ENDERDRAGON_DEATH;
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200D * ToroQuestConfiguration.bossHealthMultiplier);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5D * ToroQuestConfiguration.bossAttackDamageMultiplier);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);
	}

	protected int crystalsDestroyed = 0;
	
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
		
		if (this.world.isRemote)
		{
			return;
		}
				
		if ( this.getAttackTarget() != null )
		{
	        this.getLookHelper().setLookPositionWithEntity(this.getAttackTarget(), 30.0F, 30.0F);
	        this.faceEntity(this.getAttackTarget(), 30.0F, 30.0F);
		}
		
		if ( this.ticksExisted % 25 == 0 )
		{
			
			this.heal(ToroQuestConfiguration.bossHealthMultiplier);
	        this.bossInfo.setPercent(this.getHealth()/this.getMaxHealth());
			
			if ( this.getAttackTarget() != null )
			{
				this.spawnAuraParticle();
				this.world.setEntityState(this, (byte)16);
				
				if ( !this.getAttackTarget().isEntityAlive() )
				{
					this.setAttackTarget(null);
					return;
				}
			}
			
			List<EntityPlayer> e = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(getPosition()).grow(88,64,88), new Predicate<EntityPlayer>()
			{
				public boolean apply(@Nullable EntityPlayer entity)
				{
					return true;
				}
			});
			
			for ( EntityPlayer p : e )
			{
				if ( this.getDistance(p) < 88 )
				{
					this.setAttackTarget(p);
				}
				break;
			}
			
			List<EntityEnderCrystal> crystals = world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(this.getPosition()).grow(96, 64, 96));
			
			if ( crystals.size() < 1 )
			{
				if ( this.getIsInvulnerable() )
				{
					this.spawnExplosionParticle();
					this.world.setEntityState(this, (byte) 15);
					this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_DEATH, this.getSoundCategory(), 1.0F, 1.0F);
					if ( this.getAttackTarget() != null )
					{
						this.world.playSound((EntityPlayer)null, this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, SoundEvents.ENTITY_ENDERDRAGON_DEATH, this.getSoundCategory(), 1.0F, 1.0F);
			            this.world.playSound((EntityPlayer)null, this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, this.getSoundCategory(), 1.0F, 1.0F);
			            this.world.playSound((EntityPlayer)null, this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, this.getSoundCategory(), 1.0F, 1.0F);
					}
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 32, this.posY - rand.nextFloat() * 32, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 16, this.posY - rand.nextFloat() * 32, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 32, this.posY - rand.nextFloat() * 32, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 16, this.posY - rand.nextFloat() * 32, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 32, this.posY - rand.nextFloat() * 32, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 16, this.posY - rand.nextFloat() * 32, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 32, this.posY - rand.nextFloat() * 16, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 16, this.posY - rand.nextFloat() * 16, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 32, this.posY - rand.nextFloat() * 16, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 16, this.posY - rand.nextFloat() * 16, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 32, this.posY - rand.nextFloat() * 16, this.posZ + rand.nextGaussian() * 16, false));
			    	this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX + rand.nextGaussian() * 16, this.posY - rand.nextFloat() * 16, this.posZ + rand.nextGaussian() * 16, false));
				}
				this.bossInfo.setColor(BossInfo.Color.PURPLE);
				this.setEntityInvulnerable(false);
			}
			else
			{
		        this.bossInfo.setColor(BossInfo.Color.WHITE);
				this.setEntityInvulnerable(true);
			}
			
			int current = this.crystalsDestroyed;
			
			this.crystalsDestroyed = MathHelper.clamp(12 - crystals.size(), 0, 12);
			
			if ( current != this.crystalsDestroyed && this.getAttackTarget() != null )
			{
				this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_DEATH, this.getSoundCategory(), 1.0F, 1.0F);
				this.world.playSound((EntityPlayer)null, this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, SoundEvents.ENTITY_ENDERDRAGON_HURT, this.getSoundCategory(), 1.0F, 1.0F + rand.nextFloat()/50.0F);
	            this.world.playSound((EntityPlayer)null, this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, this.getSoundCategory(), 1.0F, 1.0F);
			}
		}
		
		// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
		
		if ( this.getRaidLocationY() != 0 )
		{
			this.setPosition(this.getRaidLocationX(), this.getRaidLocationY(), this.getRaidLocationZ());
		}
		
		if ( this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive() )
		{
			return;
		}

		double d0 = this.getDistance(this.getAttackTarget());
	    
        if ( d0 <= 22 )
        {
        	this.collideWithEntity(this.getAttackTarget());
        }
        
        if ( d0 <= 88)
	    {
	    	 int attackTimer = this.ticksExisted % 132;
	    	 
	    	 if ( attackTimer == 28 )
	         {
		         this.world.playSound((EntityPlayer)null, this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, this.getSoundCategory(), 1.0F, 1.2F);
	        	 // this.world.playEvent((EntityPlayer)null, 1015, new BlockPos(this), 0);
	         }
	    	 else if ( ( attackTimer == 30 && this.crystalsDestroyed >= 8 ) || attackTimer == 40 || ( attackTimer == 50 && this.crystalsDestroyed >= 4 ) || ( attackTimer == 60 && this.crystalsDestroyed >= 1 ) || ( attackTimer == 70 && this.crystalsDestroyed >= 12 ) )
	         {
	             Vec3d vec3d = this.getLook(1.0F);
	             double d2 = this.getAttackTarget().posX - (this.posX + vec3d.x) + rand.nextGaussian() * (this.crystalsDestroyed/8.0D);
	             double d3 = this.getAttackTarget().getEntityBoundingBox().minY + (double)(this.getAttackTarget().height) - (this.posY + (double)(this.height / 9.0D)) + rand.nextGaussian() * (this.crystalsDestroyed/12.0D) - 3.0D;
	             double d4 = this.getAttackTarget().posZ - (this.posZ + vec3d.z) + rand.nextGaussian() * (this.crystalsDestroyed/8.0D);
	             //this.world.playEvent((EntityPlayer)null, 1016, new BlockPos(this), 0);
		         this.world.playSound((EntityPlayer)null, this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, SoundEvents.ENTITY_ENDERDRAGON_SHOOT, this.getSoundCategory(), 1.0F, 0.9F + rand.nextFloat()/25.0F);
	             EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, this, d2, d3, d4);
	             entitylargefireball.explosionPower = 2;
	             entitylargefireball.posX = this.posX - vec3d.x * 2.0D;
	             entitylargefireball.posY = this.posY - vec3d.y * 2.0D;
	             entitylargefireball.posZ = this.posZ - vec3d.z * 2.0D;
	             this.world.spawnEntity(entitylargefireball);
	         }
	    }
	    else
	    {
	    	this.setAttackTarget(null);
	    	return;
	    }
	}

//	@SideOnly(Side.CLIENT)
	public void spawnAuraParticle( )
	{
		if ( this.getAttackTarget() != null )
		{
			if (this.world instanceof WorldServer)
			{
				for ( int i = 16; i > 0; i-- )
				{
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.PORTAL, this.getAttackTarget().posX + this.rand.nextGaussian() * 0.06999999523162842D, this.getAttackTarget().posY + this.rand.nextGaussian() * 0.20999999523162842D, this.getAttackTarget().posZ + this.rand.nextGaussian() * 0.06999999523162842D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
				}
			}
		}
		// else
//		{
//			this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + this.rand.nextGaussian() * 16, this.posY + this.rand.nextGaussian() * 16, this.posZ + this.rand.nextGaussian() * 16, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
//			this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + this.rand.nextGaussian() * 16, this.posY + this.rand.nextGaussian() * 16, this.posZ + this.rand.nextGaussian() * 16, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
//			this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + this.rand.nextGaussian() * 16, this.posY + this.rand.nextGaussian() * 16, this.posZ + this.rand.nextGaussian() * 16, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
//			this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + this.rand.nextGaussian() * 16, this.posY + this.rand.nextGaussian() * 16, this.posZ + this.rand.nextGaussian() * 16, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
//			this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + this.rand.nextGaussian() * 16, this.posY + this.rand.nextGaussian() * 16, this.posZ + this.rand.nextGaussian() * 16, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
//			this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + this.rand.nextGaussian() * 16, this.posY + this.rand.nextGaussian() * 16, this.posZ + this.rand.nextGaussian() * 16, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
//			this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + this.rand.nextGaussian() * 16, this.posY + this.rand.nextGaussian() * 16, this.posZ + this.rand.nextGaussian() * 16, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
//			this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + this.rand.nextGaussian() * 16, this.posY + this.rand.nextGaussian() * 16, this.posZ + this.rand.nextGaussian() * 16, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, this.rand.nextGaussian() * 0.05D, new int[0]);
//		}
	}

	public void spawnAttackParticles()
	{
		if (this.world instanceof WorldServer)
		{
			for ( int i = 16; i > 0; i-- )
			{
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + this.rand.nextGaussian() * 2.05D, this.posY + this.rand.nextGaussian() * 2.05D, this.posZ + this.rand.nextGaussian() * 2.05D, this.rand.nextGaussian() * 2.05D, this.rand.nextGaussian() * 2.05D, this.rand.nextGaussian() * 2.05D, new int[0]);
			}
		}
	}

	@Nullable
	protected ResourceLocation getLootTable()
	{
		return null; // LootTableList.ENTITIES_GUARDIAN;
	}


	@Override
	public void setSwingingArms(boolean swingingArms)
	{

	}

//	private void spawnParticles(double xSpeed, double ySpeed, double zSpeed)
//	{
//		if (this.world.isRemote)
//		{
//			for (int i = 0; i < 32; ++i) 
//			{
//				world.spawnParticle(EnumParticleTypes.PORTAL, posX, posY, posZ, xSpeed, ySpeed, zSpeed, new int[0]);
//			}
//		}
//		else
//		{
//			this.world.setEntityState(this, (byte) 42);
//		}
//
//	}

	public float getEyeHeight()
	{
		return 16.0F;
	}
	
	// INCREASE RENDER DISTNACE
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return this.getEntityBoundingBox().grow(88.0);
    }

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if (this.world.isRemote)
		{
			return false;
		}
		
		if (this.getIsInvulnerable())
        {
			this.playSound(SoundEvents.ENTITY_ENDERMEN_AMBIENT, 0.8F, 0.7F + rand.nextFloat()/25.0F);
			this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 0.8F, 0.7F + rand.nextFloat()/25.0F);
            return false;
        }
		
        this.bossInfo.setPercent(this.getHealth()/this.getMaxHealth());
		
		if ( this.isEntityInvulnerable(source) || source == DamageSource.FALL || source == null || source.getTrueSource() == null || !(this.isEntityAlive()) )
		{
			return false;
		}
		
		if ( source.getTrueSource() instanceof EntityLivingBase )
		{
			if ( super.attackEntityFrom(source, amount) )
			{
		        this.world.playSound((EntityPlayer)null, source.getTrueSource().posX, source.getTrueSource().posY, source.getTrueSource().posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, this.getSoundCategory(), 1.0F, 1.4F);
		        this.world.playSound((EntityPlayer)null, source.getTrueSource().posX, source.getTrueSource().posY, source.getTrueSource().posZ, SoundEvents.ENTITY_ENDERDRAGON_HURT, this.getSoundCategory(), 1.0F, 1.2F);
		        return true;
			}
		}
		return false;
	}
	
	protected void damageEntity(DamageSource damageSrc, float damageAmount)
    {
        if (!this.isEntityInvulnerable(damageSrc))
        {
            damageAmount = net.minecraftforge.common.ForgeHooks.onLivingHurt(this, damageSrc, damageAmount);
            if (damageAmount <= 0) return;
            damageAmount = this.applyArmorCalculations(damageSrc, damageAmount);
            damageAmount = this.applyPotionDamageCalculations(damageSrc, damageAmount);
            float f = damageAmount;
            damageAmount = Math.max(damageAmount - this.getAbsorptionAmount(), 0.0F);
            this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - damageAmount));
            damageAmount = net.minecraftforge.common.ForgeHooks.onLivingDamage(this, damageSrc, damageAmount);

            if (damageAmount != 0.0F)
            {
                float f1 = this.getHealth();
                this.getCombatTracker().trackDamage(damageSrc, f1, damageAmount);
                this.setHealth(f1 - damageAmount); // Forge: moved to fix MC-121048
                this.setAbsorptionAmount(this.getAbsorptionAmount() - damageAmount);
            }
        }
//    	if ( this.getHealth() <= 0 )
//        {
//        	this.dropBossLoot();
//        }
    }

	protected void redirectAttack(DamageSource source, float amount)
	{
		Entity attacker = source.getTrueSource();
		if (attacker != null) {
			attacker.attackEntityFrom(source, amount);
		}
	}

//	protected void redirectArrowAtAttacker(DamageSource source) {
//		if ("arrow".equals(source.getDamageType())) {
//
//			if (source.getTrueSource() != null && source.getTrueSource() instanceof EntityLivingBase) {
//				attackWithArrow((EntityLivingBase) source.getTrueSource());
//			}
//
//			if (source.getImmediateSource() != null) {
//				source.getImmediateSource().setDead();
//			}
//
//		}
//	}

//	protected void attackWithArrow(EntityLivingBase target) {
//
//		int charge = 2 + rand.nextInt(10);
//
//		EntityArrow entityarrow = new EntityTippedArrow(this.world, this);
//		double d0 = target.posX - this.posX;
//		double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityarrow.posY;
//		double d2 = target.posZ - this.posZ;
//		double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
//		entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F,
//				(float) (14 - this.world.getDifficulty().getDifficultyId() * 4));
//		int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, this);
//		int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
//		entityarrow.setDamage((double) (charge * 2.0F) + this.rand.nextGaussian() * 0.25D
//				+ (double) ((float) this.world.getDifficulty().getDifficultyId() * 0.11F));
//
//		if (i > 0) {
//			entityarrow.setDamage(entityarrow.getDamage() + (double) i * 0.5D + 0.5D);
//		}
//
//		if (j > 0) {
//			entityarrow.setKnockbackStrength(j);
//		}
//
//		if (rand.nextBoolean()) {
//			entityarrow.setFire(100);
//		}
//
//		this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
//		this.world.spawnEntity(entityarrow);
//	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
	{
		// TODO Auto-generated method stub
		
	}
	
	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);

	/**
     * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in
     * order to view its associated boss bar.
     */
    public void addTrackingPlayer(EntityPlayerMP player)
    {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    /**
     * Removes the given player from the list of players tracking this entity. See {@link Entity#addTrackingPlayer} for
     * more information on tracking.
     */
    public void removeTrackingPlayer(EntityPlayerMP player)
    {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }
    
    private void dropBossLoot()
	{
    	this.dropTrophy();
    	
		this.dropLootItem(Item.getByNameOrId("minecraft:obsidian"), rand.nextInt(3)+1);
		this.dropLootItem(Item.getByNameOrId("minecraft:obsidian"), rand.nextInt(3)+1);
		this.dropLootItem(Item.getByNameOrId("minecraft:obsidian"), rand.nextInt(3)+1);
		this.dropLootItem(Item.getByNameOrId("minecraft:obsidian"), rand.nextInt(3)+1);
		this.dropLootItem(Item.getByNameOrId("minecraft:obsidian"), rand.nextInt(3)+1);
		this.dropLootItem(Item.getByNameOrId("minecraft:obsidian"), rand.nextInt(3)+1);
	}
    
    private void dropTrophy()
	{
		ItemStack stack = new ItemStack(Item.getByNameOrId("toroquest:trophy_beholder"));
		EntityItem dropItem = new EntityItem(world, posX, posY+1, posZ, stack.copy());
		dropItem.setNoPickupDelay();
		dropItem.motionY = 0.5;
		dropItem.motionZ = 0.0;
		dropItem.motionX = 0.0;
		this.world.spawnEntity(dropItem);
	}

	private void dropLootItem(Item item, int amount)
	{
		if (amount == 0)
		{
			return;
		}

		for (int i = 0; i < amount; i++)
		{
			ItemStack stack = new ItemStack(item);
			EntityItem dropItem = new EntityItem(world, posX, posY, posZ, stack.copy());
			dropItem.setNoPickupDelay();
			dropItem.motionY = rand.nextDouble();
			dropItem.motionZ = rand.nextDouble() - 0.5d;
			dropItem.motionX = rand.nextDouble() - 0.5d;
			this.world.spawnEntity(dropItem);
			dropItem.setGlowing(true);
		}

	}
	
}
