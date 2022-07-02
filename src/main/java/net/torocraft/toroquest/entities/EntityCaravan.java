package net.torocraft.toroquest.entities;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.torocraft.toroquest.EventHandlers;
import net.torocraft.toroquest.ToroQuest;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.config.ToroQuestConfiguration;

public class EntityCaravan extends EntityToroVillager implements IMerchant
{

	public static String NAME = "caravan";

	static
	{
		if ( ToroQuestConfiguration.specificEntityNames )
		{
			NAME = ToroQuestEntities.ENTITY_PREFIX + NAME;
		}
	}

	public boolean escorted = false;
	protected Random rand = new Random();
	protected int despawnTimer = 300;
	protected boolean canSpawnBandits = true;
	protected boolean backupLeash = true;

	@Override
	public void readEntityFromNBT( NBTTagCompound compound )
	{
		this.despawnTimer = compound.getInteger("customDespawnTimer");
		this.backupLeash = compound.getBoolean("backupLeash");
		this.canSpawnBandits = compound.getBoolean("canSpawnBandits");
		super.readEntityFromNBT(compound);
	}

	@Override
	public void writeEntityToNBT( NBTTagCompound compound )
	{
		compound.setInteger("customDespawnTimer", this.despawnTimer);
		compound.setBoolean("backupLeash", this.backupLeash);
		compound.setBoolean("canSpawnBandits", this.canSpawnBandits);
		super.writeEntityToNBT(compound);
	}

	public static void init( int entityId )
	{
		EntityRegistry.registerModEntity(new ResourceLocation(ToroQuest.MODID, NAME), EntityCaravan.class, NAME, entityId, ToroQuest.INSTANCE, 80, 3, true, 0x000000, 0xe0d6b9);
	}

	public EntityCaravan( World worldIn )
	{
		super(worldIn, 0);
		// this.enablePersistence();
		// this.tasks.addTask(1, caravanTask);
	}

	@Override
	protected boolean canDespawn()
	{
		return this.despawnTimer <= 0;
	}

	public void addCaravan()
	{
		if ( !this.world.isRemote )
		{
			EntityMule mule = new EntityMule(this.world);
			mule.addTag(this.getUniqueID().toString());
			mule.addTag("toroquest_caravan");
			// mule.setHorseTamed(true);
			mule.replaceItemInInventory(499, new ItemStack(Item.getItemFromBlock(Blocks.CHEST)));
			mule.replaceItemInInventory(400, new ItemStack((Items.SADDLE)));

			if ( rand.nextInt(4) == 0 )
			{
				for ( int i = rand.nextInt(4) + 4; i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.BREAD), 1));
				for ( int i = rand.nextInt(4) + 4; i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.WHEAT), 5));
			}
			else if ( rand.nextInt(4) == 0 )
			{
				for ( int i = rand.nextInt(4) + 4; i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.EMERALD), 5));
			}
			else if ( rand.nextInt(4) == 0 )
			{
				// for ( int i = rand.nextInt(3)+3; i > 0; i-- )
				// mule.replaceItemInInventory(500+rand.nextInt(18), new
				// ItemStack((Blocks.),rand.nextInt(5)+1));
				for ( int i = rand.nextInt(4) + 4; i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.RABBIT_HIDE), 5));
				for ( int i = rand.nextInt(4) + 2; i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.LEATHER), 5));
			}
			else
			{
				for ( int i = rand.nextInt(3) + 2; i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.BEETROOT), 5));
				for ( int i = rand.nextInt(4); i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.APPLE), 5));
				for ( int i = rand.nextInt(4); i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.CARROT), 5));
				for ( int i = rand.nextInt(4); i > 0; i-- )
					mule.replaceItemInInventory(500 + rand.nextInt(18), new ItemStack((Items.POTATO), 5));
			}

			mule.setPosition(this.posX, this.posY, this.posZ);
			// mule.tasks.addTask(1, new EntityAISmartTempt( mule, 1.5D, Items.AIR ) );
			mule.getEntityData().setInteger("despawnTimer", 300);
			mule.getEntityData().setFloat("raidSpeed", 1.0F);
			mule.getEntityData().setInteger("raidX", 0);
			mule.getEntityData().setInteger("raidZ", 0);
			world.spawnEntity(mule);
			mule.removePassengers();
			mule.writeEntityToNBT(new NBTTagCompound());
		}
		// this.tasks.addTask( 1, this.caravanTask );
	}

	// protected EntityCaravan e = this;

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();

		if ( this.world.isRemote || this.escorted )
		{
			return;
		}

		if ( !this.escorted && this.ticksExisted % 20 == 0 )
		{
			if ( this.underAttack != null )
			{
				if ( --this.blockedTrade <= 0 )
				{
					this.underAttack = null;
				}
				else
				{
					return;
				}
			}

			List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.getPosition()).grow(25, 15, 25));

			if ( this.ticksExisted % 100 == 0 )
			{
				if ( this.despawnTimer-- < 0 )
				{
					if ( players.isEmpty() || (this.world.getWorldTime() == 22000 && this.despawnTimer < -150) || (this.despawnTimer < -300) )
					{
						this.setHealth(0);
						this.setDead();
						return;
					}
				}
			}

			List<EntityMule> mules = this.world.getEntitiesWithinAABB(EntityMule.class, new AxisAlignedBB(getPosition()).grow(25, 15, 25), new Predicate<EntityMule>()
			{
				public boolean apply( @Nullable EntityMule entity )
				{
					return true;
				}
			});

			for ( EntityMule mule : mules )
			{
				if ( mule.isEntityAlive() && mule.hasChest() && mule.isHorseSaddled() && mule.getTags().contains(this.entityUniqueID.toString()) )
				{
					if ( mule.getLeashHolder() == null || !mule.getLeashed() )
					{
						if ( this.despawnTimer > 290 )
						{
							mule.setLeashHolder(this, true);
						}
						else if ( this.backupLeash )
						{
							mule.setLeashHolder(this, true);
							this.backupLeash = false;
							this.writeEntityToNBT(new NBTTagCompound());
						}
					}

					if ( this.rand.nextBoolean() && !mule.getPassengers().isEmpty() )
					{
						mule.removePassengers();
						this.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
					}

					if ( mule.getDistance(this) > 9 )
					{
						mule.getNavigator().tryMoveToEntityLiving(this, 1.5);
					}
				}
			}

			if ( this.canSpawnBandits && this.rand.nextInt(200) == 0 )
			{
				if ( this.spawnBanditsNearPlayer() )
				{
					this.canSpawnBandits = false;
					this.writeEntityToNBT(new NBTTagCompound());
				}
			}

			Province province = CivilizationUtil.getProvinceAt(this.world, this.chunkCoordX, this.chunkCoordZ);

			if ( province != null && Math.abs(this.posX - province.getCenterPosX()) + Math.abs(this.posZ - province.getCenterPosZ()) <= 86 )
			{
				float multiplier = 1.0f;

				for ( EntityMule mule : mules )
				{
					if ( mule != null && mule.isEntityAlive() && mule.getTags().contains(this.entityUniqueID.toString()) ) // && mule.getLeashHolder() !=
																															 // null &&
																															 // mule.getLeashHolder() ==
																															 // creature )
					{
						mule.removePassengers();
						mule.clearLeashed(true, false);
						this.clearLeashed(true, false);

						if ( ToroQuestConfiguration.removeMuleOnCaravanEscort )
						{
							try
							{
								mule.removePassengers();
							}
							catch (Exception e)
							{

							}
							mule.setHealth(0);
							mule.setDead();
						}
						else
						{
							try
							{
								mule.getEntityData().removeTag("despawnTimer");
								mule.getEntityData().removeTag("raidSpeed");
								mule.getEntityData().removeTag("raidX");
								mule.getEntityData().removeTag("raidZ");
							}
							catch (Exception e)
							{

							}
						}
						multiplier += 0.25f;
					}
				}

				for ( EntityPlayer player : players )
				{
					ITextComponent message = new TextComponentString("§lCaravan Escorted!§r");
					this.escorted = true;
					player.sendStatusMessage(message, true);
					playSound(SoundEvents.ENTITY_MULE_AMBIENT, 1.0F, 1.0F);
					playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
					EventHandlers.adjustPlayerRep(player, province.civilization, (int) (ToroQuestConfiguration.escortCaravanRepGain * multiplier));
				}

				this.setHealth(0.0F);
				this.setDead();

				@SuppressWarnings( "deprecation" )
				EntityToroVillager newEntity = new EntityToroVillager(this.world, this.getProfession());
				BlockPos pos = this.getPosition();
				newEntity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				newEntity.copyLocationAndAnglesFrom(this);
				this.world.spawnEntity(newEntity);
				return;
			}
			else
				for ( EntityPlayer player : players )
				{
					this.getNavigator().tryMoveToEntityLiving(player, 0.6);
					ITextComponent message = new TextComponentString("§oEscorting Caravan§r");
					player.sendStatusMessage(message, true);
					if ( this.despawnTimer < 0 )
					{
						this.despawnTimer = 0;
					}
				}
		}

	}

	// protected EntityAISmartTempt caravanTask = new EntityAISmartTempt(this, 0.5D,
	// Items.AIR)
	// {
	// @Override
	// public boolean shouldExecute()
	// {
	// if ( escorted ) // || ( underAttack != null && underAttack instanceof
	// EntityPlayer ) || blockedTrade > 0 )
	// {
	// return false;
	// }
	// return super.shouldExecute();
	// }
	//
	// @Override
	// protected boolean isTempting(ItemStack stack)
	// {
	// return !escorted;
	// }
	//
	// @Override
	// public void updateTask()
	// {
	// if ( !escorted && world.getWorldTime() % 20 == 0 )
	// {
	// if ( creature.underAttack != null )
	// {
	// creature.underAttack = null;
	// return;
	// }
	// List<EntityMule> mules = world.getEntitiesWithinAABB(EntityMule.class, new
	// AxisAlignedBB(getPosition()).grow(32, 16, 32), new Predicate<EntityMule>()
	// {
	// public boolean apply(@Nullable EntityMule entity)
	// {
	// return true;
	// }
	// });
	// for (EntityMule mule: mules)
	// {
	// if ( mule.isEntityAlive() && mule.hasChest() && mule.isHorseSaddled() &&
	// mule.getTags().contains( e.entityUniqueID.toString() ) )
	// {
	// mule.removePassengers();
	// if ( mule.getLeashHolder() == null )
	// {
	// if ( despawnTimer > 600 )
	// {
	// mule.setLeashHolder(creature, true);
	// }
	// else if ( backupLeash )
	// {
	// mule.setLeashHolder(creature, true);
	// backupLeash = false;
	// }
	// }
	// if ( mule.getDistance(creature) > 9 )
	// {
	// mule.getNavigator().tryMoveToEntityLiving(creature, 2.0 );
	// }
	// }
	// }
	//
	// List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
	// new AxisAlignedBB(e.getPosition()).grow(32, 16, 32), new
	// Predicate<EntityPlayer>()
	// {
	// public boolean apply(@Nullable EntityPlayer entity)
	// {
	// return true;
	// }
	// });
	//
	// if ( players.size() < 1 )
	// {
	// if ( --despawnTimer < 0 )
	// {
	// setDead();
	// }
	// return;
	// }
	//
	// if ( rand.nextInt(40) == 0 )
	// {
	// spawnSentryNearPlayer();
	// }
	//
	// Province province = CivilizationUtil.getProvinceAt(e.world, e.chunkCoordX,
	// e.chunkCoordZ);
	//
	// if ( province != null && Math.abs(e.posX - province.getCenterX()) +
	// Math.abs(e.posZ - province.getCenterZ()) <= 86 )
	// {
	// float multiplier = 1.0f;
	// escorted = true;
	// writeToNBT(new NBTTagCompound());
	// for ( EntityMule mule: mules )
	// {
	// if ( mule != null && mule.isEntityAlive() && mule.getTags().contains(
	// e.entityUniqueID.toString() ) ) // && mule.getLeashHolder() != null &&
	// mule.getLeashHolder() == creature )
	// {
	// mule.removePassengers();
	// mule.clearLeashed(true, false);
	// e.clearLeashed(true, false);
	// if ( ToroQuestConfiguration.removeMuleOnCaravanEscort )
	// {
	// try
	// {
	// mule.getRidingEntity().dismountRidingEntity();
	// }
	// catch ( Exception e )
	// {
	//
	// }
	// mule.setDead();
	// }
	// multiplier += 0.5f;
	// }
	// }
	// for ( EntityPlayer player: players )
	// {
	// ITextComponent message = new TextComponentString( "§lCaravan Escorted!§r" );
	// player.sendStatusMessage(message, true);
	// playSound(SoundEvents.ENTITY_MULE_AMBIENT, 1.0F, 1.0F);
	// playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
	// EventHandlers.adjustPlayerRep(player, province.civilization,
	// (int)(ToroQuestConfiguration.escortCaravanRepGain*multiplier));
	// }
	// }
	// for ( EntityPlayer player: players )
	// {
	// ITextComponent message = new TextComponentString( "§oEscorting Caravan§r" );
	// player.sendStatusMessage(message, true);
	// despawnTimer = 600;
	// }
	// super.updateTask();
	// }
	// }
	// };

	protected boolean spawnBanditsNearPlayer()
	{
		if ( this.world.isRemote )
		{
			return false;
		}

		try
		{
			int tries = 3;

			while (tries > 0)
			{
				tries--;
				{
					if ( this.world.provider.getDimension() != 0 )
					{
						continue;
					}

					int playerPosX = (int) this.posX;
					int playerPosZ = (int) this.posZ;

					if ( CivilizationUtil.getProvinceAt(world, playerPosX / 16, playerPosZ / 16) != null )
					{
						continue;
					}

					int range = 40 + rand.nextInt(20);

					double angle = rand.nextDouble() * Math.PI * 2.0D;

					int x = (int) (Math.cos(angle) * range);
					int z = (int) (Math.sin(angle) * range);

					x += playerPosX;
					z += playerPosZ;

					BlockPos banditSpawnPos = EventHandlers.findSpawnLocationFrom(world, new BlockPos(x, EventHandlers.MAX_SPAWN_HEIGHT, z));

					if ( banditSpawnPos == null )
					{
						continue;
					}

					if ( CivilizationUtil.getProvinceAt(world, banditSpawnPos.getX() / 16, banditSpawnPos.getZ() / 16) != null )
					{
						continue;
					}

					if ( !(world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(banditSpawnPos).grow(25, 10, 25))).isEmpty() )
					{
						continue;
					}

					// =-=-=-=-=-=-=-=-=-=-=-=-=
					// BANDIT
					// =-=-=-=-=-=-=-=-=-=-=-=-=

					int amountToSpawn = 3;

					if ( ToroQuestConfiguration.orcsAreNeutral || rand.nextBoolean() )
					{
						for ( int i = amountToSpawn; i > 0; i-- )
						{
							EntitySentry e = new EntitySentry(world, playerPosX * 2 - banditSpawnPos.getX(), playerPosZ * 2 - banditSpawnPos.getZ());
							e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
							// ForgeChunkManager.forceChunk(//ForgeChunkManager.requestTicket(ToroQuest.INSTANCE,
							// world, Type.ENTITY), new ChunkPos(e.chunkCoordX, e.chunkCoordZ));

							world.spawnEntity(e);

							e.despawnTick();
							e.velocityChanged = true;
							e.setAttackTarget(this);
							e.writeEntityToNBT(new NBTTagCompound());
						}
					}
					else
					{
						for ( int i = amountToSpawn; i > 0; i-- )
						{
							EntityOrc e = new EntityOrc(world, playerPosX * 2 - banditSpawnPos.getX(), playerPosZ * 2 - banditSpawnPos.getZ());
							e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
							// ForgeChunkManager.forceChunk(//ForgeChunkManager.requestTicket(ToroQuest.INSTANCE,
							// world, Type.ENTITY), new ChunkPos(e.chunkCoordX, e.chunkCoordZ));

							world.spawnEntity(e);

							e.despawnTick();
							e.velocityChanged = true;
							e.setAttackTarget(this);
							e.writeEntityToNBT(new NBTTagCompound());
						}
					}
					return true;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("ERROR SPAWNING Caravan Bandits: " + e);
		}
		return false;
	}

	// protected boolean spawnSentryNearPlayer()
	// {
	// World world = this.world;
	//
	// if ( world.isRemote )
	// {
	// return false;
	// }
	//
	// try
	// {
	// int range = 50;
	// {
	// Province province = CivilizationUtil.getProvinceAt(world, this.chunkCoordX,
	// this.chunkCoordZ);
	//
	// if ( province != null )
	// {
	// return false;
	// }
	//
	// int villageCenterX = (int)this.posX;
	// int villageCenterZ = (int)this.posZ;
	//
	// int x = (rand.nextInt(range));
	// int z = (rand.nextInt(range));
	//
	// while ( x < range/2 && z < range/2 )
	// {
	// x = (rand.nextInt(range));
	// z = (rand.nextInt(range));
	// }
	//
	// x *= (rand.nextInt(2)*2-1);
	// z *= (rand.nextInt(2)*2-1);
	//
	// x += villageCenterX;
	// z += villageCenterZ;
	//
	// BlockPos loc = new BlockPos(x,world.getHeight()/2,z);
	//
	// BlockPos banditSpawnPos = EventHandlers.findSpawnLocationFrom(world, loc);
	//
	// if (banditSpawnPos == null)
	// {
	// return false;
	// }
	//
	// province = CivilizationUtil.getProvinceAt(world, banditSpawnPos.getX()/16,
	// banditSpawnPos.getZ()/16);
	//
	// if ( province != null )
	// {
	// return false;
	// }
	//
	// if ( !world.getEntitiesWithinAABB(EntityPlayer.class, new
	// AxisAlignedBB(banditSpawnPos).grow(20, 10, 20)).isEmpty() )
	// {
	// return false;
	// }
	//
	// if ( ToroQuestConfiguration.orcsAreNeutral || rand.nextBoolean() )
	// {
	// for ( int i = rand.nextInt(3) + 2; i > 0; i-- )
	// {
	// EntitySentry entity = new EntitySentry(world);
	// entity.despawnTick();
	// entity.setPosition(banditSpawnPos.getX() + 0.5,banditSpawnPos.getY() + 0.1,
	// banditSpawnPos.getZ() + 0.5 );
	// entity.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)),
	// (IEntityLivingData) null);
	// world.spawnEntity(entity);
	// entity.setAttackTarget(this);
	// // entity.setRaidLocation(villageCenterX, villageCenterZ);
	// }
	// }
	// else
	// {
	// for ( int i = rand.nextInt(3) + 2; i > 0; i-- )
	// {
	// EntitySentry entity = new EntityOrc(world);
	// entity.despawnTick();
	// entity.setPosition(banditSpawnPos.getX() + 0.5,banditSpawnPos.getY() + 0.1,
	// banditSpawnPos.getZ() + 0.5 );
	// entity.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)),
	// (IEntityLivingData) null);
	// world.spawnEntity(entity);
	// entity.setAttackTarget(this);
	// // entity.setRaidLocation(villageCenterX, villageCenterZ);
	// }
	// }
	// return true;
	// }
	// }
	// catch (Exception e)
	// {
	// System.out.println("ERROR SPAWNING EntityBandit: " + e);
	// return false;
	// }
	// }
}