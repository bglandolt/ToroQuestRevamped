package net.torocraft.toroquest.entities;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.ToroQuest;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.ai.AIHelper;
import net.torocraft.toroquest.entities.ai.EntityAIRaid;
import net.torocraft.toroquest.entities.ai.EntityAIThrow;
import net.torocraft.toroquest.entities.render.RenderPigLord;
import net.torocraft.toroquest.generation.WorldGenPlacer;

public class EntityPigLord extends EntityPigZombie implements IMob
{

	// ============================================================================================================================

	protected static final DataParameter<Integer> RAID_X = EntityDataManager.<Integer>createKey(EntityPigLord.class, DataSerializers.VARINT);
	protected static final DataParameter<Integer> RAID_Y = EntityDataManager.<Integer>createKey(EntityPigLord.class, DataSerializers.VARINT);
	protected static final DataParameter<Integer> RAID_Z = EntityDataManager.<Integer>createKey(EntityPigLord.class, DataSerializers.VARINT);

	@Override
	protected void entityInit()
	{
		super.entityInit();

		this.getDataManager().register(RAID_X, Integer.valueOf(0));
		this.getDataManager().register(RAID_Y, Integer.valueOf(0));
		this.getDataManager().register(RAID_Z, Integer.valueOf(0));
	}

	@Override
	public void writeEntityToNBT( NBTTagCompound compound )
	{
		super.writeEntityToNBT(compound);

		compound.setInteger("raidX", this.getRaidLocationX());
		compound.setInteger("raidY", this.getRaidLocationY());
		compound.setInteger("raidZ", this.getRaidLocationZ());
	}

	@Override
	public void readEntityFromNBT( NBTTagCompound compound )
	{
		super.readEntityFromNBT(compound);

		this.setRaidLocation(compound.getInteger("raidX"), compound.getInteger("raidY"), compound.getInteger("raidZ"));
	}

	protected void setRaidLocation( int x, int y, int z )
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

	public static String NAME = "pig_lord";

	static
	{
		if ( ToroQuestConfiguration.specificEntityNames )
		{
			NAME = ToroQuestEntities.ENTITY_PREFIX + NAME;
		}
	}

	@Override
	public void setChild( boolean childZombie )
	{
		return;
	}

	public static void init( int entityId )
	{
		EntityRegistry.registerModEntity(new ResourceLocation(ToroQuest.MODID, NAME), EntityPigLord.class, NAME, entityId, ToroQuest.INSTANCE, 80, 3, true, 0xffffff, 0x909090);
	}

	public boolean isNonBoss()
	{
		return false;
	}

	@Override
	public int getHorizontalFaceSpeed()
	{
		return 5;
	}

	@Override
	protected float getWaterSlowDown()
	{
		return 0.9F;
	}

	public static void registerRenders()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityPigLord.class, new IRenderFactory<EntityPigLord>()
		{
			@Override
			public Render<EntityPigLord> createRenderFor( RenderManager manager )
			{
				return new RenderPigLord(manager);
			}
		});
	}

	public EntityPigLord( World worldIn, int x, int y, int z )
	{
		super(worldIn);

		this.setUpEntity();

		this.setRaidLocation(x, y, z);
		this.tasks.addTask(4, new EntityAIRaid(this, x, z, 0.75D));
	}

	private void setUpEntity()
	{
		this.isImmuneToFire = true;
		this.enablePersistence();
		this.setChild(false);
		this.setSize(1.9F, 5.9F);
		this.setRealSize(1.9F, 5.9F);
		this.experienceValue = 280;
		this.stepHeight = 4.05F;
	}

	public EntityPigLord( World worldIn )
	{
		super(worldIn);

		this.setUpEntity();

		int x = this.getRaidLocationX();
		int y = this.getRaidLocationY();
		int z = this.getRaidLocationZ();

		if ( y != 0 )
		{
			this.tasks.addTask(4, new EntityAIRaid(this, x, z, 0.75D));
		}
	}

	// INCREASE RENDER DISTNACE
	@SideOnly( Side.CLIENT )
	public AxisAlignedBB getRenderBoundingBox()
	{
		return this.getEntityBoundingBox().grow(64.0);
	}

	protected void setEquipmentBasedOnDifficulty( DifficultyInstance difficulty )
	{
		setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
		setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.GOLDEN_SWORD));
	}

	@Override
	public float getEyeHeight()
	{
		return super.getEyeHeight() * 3f;
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		// this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.75D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200D * ToroQuestConfiguration.bossHealthMultiplier);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5D * ToroQuestConfiguration.bossAttackDamageMultiplier);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);
	}

	/**
	 * Called only once on an entity when first time spawned, via egg, mob
	 * spawner, natural spawning etc, but not called when entity is reloaded
	 * from nbt. Mainly used for initializing attributes and inventory
	 */
	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn( DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata )
	{
		WorldGenPlacer.clearTrees(this.world, new BlockPos((int) this.posX, (int) this.posY, (int) this.posZ), 32);
		livingdata = super.onInitialSpawn(difficulty, livingdata);

		if ( (int) this.posY != 0 && this.getRaidLocationY() == 0 )
		{
			this.setRaidLocation((int) this.posX, (int) this.posY, (int) this.posZ);
		}

		return livingdata;
	}

	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	protected void initEntityAI()
	{
		ai();
	}

	@Override
	protected void applyEntityAI()
	{
		super.applyEntityAI();
	}

	protected void ai()
	{
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIThrow(this, 1.2D, true, 0.75, 40));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(4, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
		// targetTasks.addTask(2, new EntityAINearestAttackableTarget(this,
		// EntityPlayer.class, true));
	}

	private void triggerBossAbility()
	{
		if ( this.world.isRemote )
		{
			return;
		}

		if ( rand.nextBoolean() )
		{
			this.spawnLightning(16);

			List<EntityPigZombie> pigZombieCount = world.getEntitiesWithinAABB(EntityPigZombie.class, new AxisAlignedBB(this.getPosition()).grow(16, 8, 16));

			for ( EntityPigZombie p : pigZombieCount )
			{
				p.setAttackTarget(this.getAttackTarget());
			}
		}
	}

	private void spawnLightning( int range )
	{
		if ( !this.isEntityAlive() || this.getHealth() <= 0 )
		{
			return;
		}

		this.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 16, 2));
		if ( this.world.isRemote )
		{
			return;
		}
		int amount = rand.nextInt(5);

		for ( int i = 0; i < amount; i++ )
		{
			lightning(range);
		}

		// for ( int xx = -range; xx < range; xx++ )
		// {
		// for ( int yy = -range; yy < range; yy++ )
		// {
		// for ( int zz = -range; zz < range; zz++ )
		// {
		// BlockPos pos = new BlockPos(this.posX+xx,this.posY+yy,this.posZ+zz);
		// IBlockState block = world.getBlockState(pos);
		// if ( block != null )
		// {
		// if ( block == Blocks.FIRE.getDefaultState() )
		// {
		// world.setBlockToAir(pos);
		// world.setBlockState(pos.down(), Blocks.MAGMA.getDefaultState() );
		// }
		// }
		// }
		// }
		// }
	}

	protected void lightning( int range )
	{
		if ( this.world.isRemote )
		{
			return;
		}
		// EntityPigZombie mob = new EntityPigZombie(world);
		BlockPos pos = getSurfacePosition(this.world, this.posX + rand.nextInt(range) - range / 2, this.posY + rand.nextInt(range / 2), this.posZ + rand.nextInt(range) - range / 2);
		if ( pos == null )
		{
			return;
		}
		this.world.addWeatherEffect(new EntityLightningBolt(this.world, pos.getX(), pos.getY(), pos.getZ(), false));
		// mob.setPosition(pos.getX(),pos.getY(),pos.getZ());
		// world.spawnEntity(mob);
		// mob.spawnExplosionParticle();
		// mob.setAttackTarget(this.getAttackTarget());
	}

	private BlockPos getSurfacePosition( World world, double x, double y, double z )
	{
		IBlockState blockState;
		BlockPos search = new BlockPos(x, y, z);
		while (search.getY() > 0)
		{
			search = search.down();
			blockState = world.getBlockState(search);
			if ( isLiquid(blockState) )
			{
				break;
			}
			if ( (blockState).isOpaqueCube() )
			{
				break;
			}
		}
		return search;
	}

	private boolean isLiquid( IBlockState blockState )
	{
		return blockState.getBlock() == Blocks.WATER || blockState.getBlock() == Blocks.LAVA;
	}

	private Block getBlockDoor( BlockPos pos )
	{
		IBlockState iblockstate = this.world.getBlockState(pos);
		Block block = iblockstate.getBlock();
		if ( block == Blocks.BEDROCK )
		{
			return null;
		}
		return block; // instanceof BlockDoor && iblockstate.getMaterial() == Material.WOOD ?
						 // (BlockDoor)block : null;
	}

	public void onLivingUpdate()
	{

		super.onLivingUpdate();

		if ( this.world.isRemote )
		{
			return;
		}

		if ( this.getAttackTarget() != null )
		{
			AIHelper.faceEntitySmart(this, this.getAttackTarget());
			this.getLookHelper().setLookPositionWithEntity(this.getAttackTarget(), 30.0F, 30.0F);
		}

		float health = this.getHealth() / this.getMaxHealth();

		if ( this.ticksExisted % 25 == 0 )
		{
			this.triggerBossAbility();
			this.heal(ToroQuestConfiguration.bossHealthMultiplier);
			this.bossInfo.setPercent(health);

			if ( this.collidedHorizontally )
			{
				PathNavigateGround pathnavigateground = (PathNavigateGround) this.getNavigator();
				Path path = pathnavigateground.getPath();
				if ( path != null && !path.isFinished() )
				{
					for ( int i = 0; i < Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength()); ++i )
					{
						PathPoint pathpoint = path.getPathPointFromIndex(i);
						for ( int j = 8; j >= 0; j-- )
						{
							BlockPos doorPosition = new BlockPos(pathpoint.x + rand.nextInt(4) - 3, pathpoint.y + j, pathpoint.z + rand.nextInt(4) - 3);
							Block doorBlock = this.getBlockDoor(doorPosition);

							if ( doorBlock != null && doorBlock != Blocks.AIR && doorBlock.getBlockHardness(doorBlock.getDefaultState(), this.world, doorPosition) <= 3.0F )
							{
								this.world.setBlockState(doorPosition, Blocks.AIR.getDefaultState());
								if ( rand.nextBoolean() )
								{
									if ( rand.nextBoolean() )
										this.world.playEvent(1021, doorPosition, 0);
									else
										this.playSound(SoundEvents.BLOCK_STONE_BREAK, 1.0F, 1.0F);

								}
								this.world.playEvent(2001, doorPosition, Block.getIdFromBlock(doorBlock));
							}
						}
					}
				}
			}
		}

		if ( health <= 0.2 && this.ticksExisted % 11 == 0 )
		{
			this.spawnLightning(16);
			this.spawnLightning(48);
			this.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 25, 2, true, false));
			if ( rand.nextInt(6) == 0 )
				this.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_HURT, this.getSoundVolume() * 2.0F, ((this.rand.nextInt(5) / 10) + 0.4F));
		}
	}

	// private boolean bossTriggered;

	public boolean attackEntityFrom( DamageSource source, float amount )
	{

		if ( this.world.isRemote )
		{
			return false;
		}

		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

		if ( this.isEntityInvulnerable(source) || source == DamageSource.LIGHTNING_BOLT || source == DamageSource.FALL || source == null || source.getTrueSource() == null || !(this.isEntityAlive()) )
		{
			return false;
		}

		if ( source.getTrueSource() instanceof EntityLivingBase )
		{
			double dist = source.getTrueSource().getDistanceSq(this.getPosition());

			if ( dist > 256 )
			{
				amount = (float) (amount * (256.0f / dist));
			}

			if ( !(source.getTrueSource() instanceof EntityPlayer) )
			{
				amount = amount / 16.0f;
			}

			List<EntityPigZombie> help = world.getEntitiesWithinAABB(EntityPigZombie.class, new AxisAlignedBB(getPosition()).grow(32, 16, 32), new Predicate<EntityPigZombie>()
			{
				public boolean apply( @Nullable EntityPigZombie entity )
				{
					return true;
				}
			});

			for ( EntityPigZombie pigman : help )
			{
				if ( pigman.getAttackTarget() == null )
				{
					pigman.setAttackTarget((EntityLivingBase) source.getTrueSource());
				}
				else
				{
					if ( rand.nextInt(6) == 0 )
						this.setAttackTarget((EntityLivingBase) source.getTrueSource());
				}
			}

		}

		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void onDeath( DamageSource cause )
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
				for ( int yy = -32; yy < 32; yy++ )
				{
					for ( int zz = -range; zz < range; zz++ )
					{
						BlockPos pos = new BlockPos(x + xx, y + yy, z + zz);
						world.extinguishFire(null, pos, EnumFacing.UP);
						IBlockState block = world.getBlockState(pos);
						if ( block == Blocks.OBSIDIAN.getDefaultState() || block == Blocks.NETHER_WART.getDefaultState() )
						{
							world.setBlockState(pos, Blocks.GRAVEL.getDefaultState());
						}
						if ( block == Blocks.SOUL_SAND.getDefaultState() || block == Blocks.NETHERRACK.getDefaultState() || block == Blocks.MAGMA.getDefaultState() )
						{
							world.setBlockState(pos, Blocks.DIRT.getDefaultState());
						}
					}
				}
			}
		}
	}

	private void dropBossLoot()
	{
		if ( !this.world.isRemote )
		{
			int x = this.getPosition().getX();
			int y = this.getPosition().getY();
			int z = this.getPosition().getZ();
			int range = 128;
			for ( int xx = -range / 2; xx < range; xx++ )
			{
				for ( int yy = -range / 4; yy < range / 2; yy++ )
				{
					for ( int zz = -range / 2; zz < range; zz++ )
					{
						BlockPos pos = new BlockPos(x + xx - 16, y + yy - 4, z + zz - 16);
						world.extinguishFire(null, pos, EnumFacing.UP);
						IBlockState block = world.getBlockState(pos);
						if ( block != null && block == Blocks.OBSIDIAN )
						{
							world.setBlockToAir(pos);
						}
					}
				}
			}

			dropTrophy();
			dropSword();

			this.dropLootItem(Items.BONE, rand.nextInt(3) + 3);
			this.dropLootItem(Items.BONE, rand.nextInt(3) + 3);
			this.dropLootItem(Items.BONE, rand.nextInt(3) + 3);

			this.dropLootItem(Items.ROTTEN_FLESH, rand.nextInt(3) + 3);
			this.dropLootItem(Items.ROTTEN_FLESH, rand.nextInt(3) + 3);
			this.dropLootItem(Items.ROTTEN_FLESH, rand.nextInt(3) + 3);

			this.dropLootItem(Items.PORKCHOP, rand.nextInt(3) + 3);
			this.dropLootItem(Items.PORKCHOP, rand.nextInt(3) + 3);
			this.dropLootItem(Items.PORKCHOP, rand.nextInt(3) + 3);

			this.dropLootItem(Items.COOKED_PORKCHOP, rand.nextInt(3) + 3);
			this.dropLootItem(Items.COOKED_PORKCHOP, rand.nextInt(3) + 3);
			this.dropLootItem(Items.COOKED_PORKCHOP, rand.nextInt(3) + 3);

			this.dropLootItem(Items.GOLD_NUGGET, rand.nextInt(3) + 3);
			this.dropLootItem(Items.GOLD_INGOT, rand.nextInt(3) + 3);
			this.dropLootItem(Items.GOLDEN_CARROT, 1);

		}
	}

	private void dropTrophy()
	{
		ItemStack stack = new ItemStack(Item.getByNameOrId("toroquest:trophy_pig"));
		EntityItem dropItem = new EntityItem(world, posX, posY + 1, posZ, stack.copy());
		dropItem.setNoPickupDelay();
		dropItem.motionY = 0.5;
		dropItem.motionZ = 0.0;
		dropItem.motionX = 0.0;
		this.world.spawnEntity(dropItem);
		dropItem.setGlowing(true);
	}

	private void dropSword()
	{
		ItemStack stack = new ItemStack(Items.GOLDEN_SWORD, 1);
		stack.addEnchantment(Enchantment.getEnchantmentByLocation("minecraft:knockback"), 13);
		stack.setStackDisplayName("Sword of Nago");
		EntityItem dropItem = new EntityItem(world, posX, posY + 1, posZ, stack.copy());
		dropItem.setNoPickupDelay();
		dropItem.motionY = 0.5;
		dropItem.motionZ = 0.0;
		dropItem.motionX = 0.0;
		this.world.spawnEntity(dropItem);
	}

	private void dropLootItem( Item item, int amount )
	{
		if ( amount == 0 )
		{
			return;
		}

		for ( int i = 0; i < amount; i++ )
		{
			ItemStack stack = new ItemStack(item);
			EntityItem dropItem = new EntityItem(world, posX, posY, posZ, stack.copy());
			dropItem.setNoPickupDelay();
			dropItem.motionY = rand.nextDouble() + 0.1;
			dropItem.motionZ = 2 * rand.nextGaussian();
			dropItem.motionX = 2 * rand.nextGaussian();
			this.world.spawnEntity(dropItem);
		}

	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		this.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT, this.getSoundVolume() * 2.0F, ((this.rand.nextInt(5) / 10) + 0.4F));
		return null;
	}

	@Override
	protected SoundEvent getHurtSound( DamageSource damageSourceIn )
	{
		this.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_HURT, this.getSoundVolume() * 2.0F, ((this.rand.nextInt(5) / 10) + 0.4F));
		return null;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		this.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_DEATH, this.getSoundVolume() * 2.0F, ((this.rand.nextInt(5) / 10) + 0.4F));
		return null;
	}

	@Override
	protected SoundEvent getStepSound()
	{
		this.playSound(SoundEvents.ENTITY_IRONGOLEM_STEP, this.getSoundVolume() * 2.0F, ((this.rand.nextInt(5) / 10) + 0.4F));
		return null;
	}

	private final BossInfoServer bossInfo = (BossInfoServer) (new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);

	/**
	 * Add the given player to the list of players tracking this entity. For
	 * instance, a player may track a boss in
	 * order to view its associated boss bar.
	 */
	public void addTrackingPlayer( EntityPlayerMP player )
	{
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	/**
	 * Removes the given player from the list of players tracking this entity. See
	 * {@link Entity#addTrackingPlayer} for
	 * more information on tracking.
	 */
	public void removeTrackingPlayer( EntityPlayerMP player )
	{
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	protected void setRealSize( float width, float height )
	{
		if ( width != this.width || height != this.height )
		{
			float f = this.width;
			this.width = width;
			this.height = height;

			if ( this.width < f )
			{
				double d0 = (double) width / 2.0D;
				this.setEntityBoundingBox(new AxisAlignedBB(this.posX - d0, this.posY, this.posZ - d0, this.posX + d0, this.posY + (double) this.height, this.posZ + d0));
				return;
			}

			AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
			this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double) this.width, axisalignedbb.minY + (double) this.height, axisalignedbb.minZ + (double) this.width));

			if ( this.width > f && !this.firstUpdate && !this.world.isRemote )
			{
				this.move(MoverType.SELF, (double) (f - this.width), 0.0D, (double) (f - this.width));
			}
		}
	}
}
