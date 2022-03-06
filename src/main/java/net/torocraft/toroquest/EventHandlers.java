package net.torocraft.toroquest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.torocraft.toroquest.civilization.CivilizationDataAccessor;
import net.torocraft.toroquest.civilization.CivilizationHandlers;
import net.torocraft.toroquest.civilization.CivilizationType;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.CivilizationsWorldSaveData;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.config.ToroQuestConfiguration.Trade;
import net.torocraft.toroquest.entities.EntityCaravan;
import net.torocraft.toroquest.entities.EntityFugitive;
import net.torocraft.toroquest.entities.EntityGuard;
import net.torocraft.toroquest.entities.EntityMonolithEye;
import net.torocraft.toroquest.entities.EntityOrc;
import net.torocraft.toroquest.entities.EntitySentry;
import net.torocraft.toroquest.entities.EntityShopkeeper;
import net.torocraft.toroquest.entities.EntitySmartArrow;
import net.torocraft.toroquest.entities.EntityToroMob;
import net.torocraft.toroquest.entities.EntityToroNpc;
import net.torocraft.toroquest.entities.EntityToroVillager;
import net.torocraft.toroquest.entities.EntityWolfRaider;
import net.torocraft.toroquest.entities.EntityZombieRaider;
import net.torocraft.toroquest.entities.EntityZombieVillagerRaider;
import net.torocraft.toroquest.entities.ai.EntityAIRaidDespawn;
import net.torocraft.toroquest.network.ToroQuestPacketHandler;
import net.torocraft.toroquest.network.message.MessageRequestPlayerCivilizationSync;
import net.torocraft.toroquest.util.TaskRunner;

public class EventHandlers
{

	private Random rand = new Random();
	
//	public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio)
//    {
//        net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(this, entityIn, strength, xRatio, zRatio);
//        if(event.isCanceled()) return;
//        strength = event.getStrength(); xRatio = event.getRatioX(); zRatio = event.getRatioZ();
//        if (this.rand.nextDouble() >= this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue())
//        {
//            this.isAirBorne = true;
//            float f = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio);
//            this.motionX /= 2.0D;
//            this.motionZ /= 2.0D;
//            this.motionX -= xRatio / (double)f * (double)strength;
//            this.motionZ -= zRatio / (double)f * (double)strength;
//
//            if (this.onGround)
//            {
//                this.motionY /= 2.0D;
//                this.motionY += (double)strength;
//
//                if (this.motionY > 0.4000000059604645D)
//                {
//                    this.motionY = 0.4000000059604645D;
//                }
//            }
//        }
//    }
	
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true) // XXX
	public void onEntitySpawn(EntityJoinWorldEvent event)
	{
		Entity entity = event.getEntity();
		World world = event.getWorld();
		
		if ( world == null || entity == null )
		{
			return;
		}
		
		if ( entity instanceof EntityCreature )
		{
			EntityCreature creature = (EntityCreature) entity;

			// =-=-=-=-=-=-=-=-= custom raider =-=-=-=-=-=-=-=-=-=
			if ( entity.getEntityData().hasKey("despawnTimer") )
			{
				try
				{
					creature.tasks.addTask(3, new EntityAIRaidDespawn(creature, creature.getEntityData().getInteger("raidX"), creature.getEntityData().getInteger("raidZ"), creature.getEntityData().getFloat("raidSpeed")));
					creature.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityVillager>(creature, EntityVillager.class, false));
					creature.enablePersistence();
				}
				catch ( Exception e )
				{
					
				}
				return;
			}
			// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
			
			// =-=-=-=-=-=-=-=-=-=-=-= mob =-=-=-=-=-=-=-=-=-=-=-=
			if ( creature instanceof IMob )
			{
				int creaturePosX = creature.getPosition().getX();
				int creaturePosZ = creature.getPosition().getZ();
				
				Province province = CivilizationUtil.getProvinceAt(creature.getEntityWorld(), creaturePosX/16, creaturePosZ/16);
				
				// =-=-=-=-=-=-=-=-= province =-=-=-=-=-=-=-=-=-=
				if ( province != null )
				{
					if ( creature instanceof EntityCreeper || creature instanceof EntityEnderman )
					{
						creature.setDead();
						event.setCanceled(true);
						return;
					}
					
					int villageCenterX = province.getCenterX();
					int villageCenterZ = province.getCenterZ();
					/*
					 * Village length is equal to 208.
				 	 */
					if ( !creature.isNoDespawnRequired() )
					{
						int allowedDistance = (creature instanceof EntityZombie)?ToroQuestConfiguration.disableZombieSpawningNearVillage:ToroQuestConfiguration.disableMobSpawningNearVillage;				
						
						if ( ( creature.getPosition().getY() >= 40 && Math.abs(villageCenterX-creaturePosX) < allowedDistance && Math.abs(villageCenterZ-creaturePosZ) < allowedDistance ) )
						{
							creature.setDead();
							event.setCanceled(true);
							return;
						}
					}
				}
				
				// =-=-=-=-=-=-=-=-= zombie =-=-=-=-=-=-=-=-=-=
				if ( creature.getClass() == EntityZombie.class )
				{
					if ( !(creature instanceof EntityZombieRaider || creature instanceof EntityZombieVillagerRaider) )
					{
						if ( ToroQuestConfiguration.zombieAttackVillageChance > 0 && ToroQuestConfiguration.zombieAttackVillageChance > rand.nextInt( 100 ) )
						{
							if ( province == null )
							{
								province = CivilizationUtil.getProvinceAt(world, creaturePosX/16+2, creaturePosZ/16+2);
								
								if ( province == null )
								{
									province = CivilizationUtil.getProvinceAt(world, creaturePosX/16+2, creaturePosZ/16-2);
									
									
									if ( province == null )
									{
										province = CivilizationUtil.getProvinceAt(world, creaturePosX/16-2, creaturePosZ/16+2);
										
										if ( province == null )
										{
											province = CivilizationUtil.getProvinceAt(world, creaturePosX/16-2, creaturePosZ/16-2);
										}
									}
								}
							}
							if ( province != null )
							{
								if ( creature instanceof EntityZombieVillager || ( ToroQuestConfiguration.zombieRaiderVillagerChance > 0 && ToroQuestConfiguration.zombieRaiderVillagerChance > rand.nextInt( 100 ) ) )
								{
									if ( !world.isRemote )
									{
										EntityZombieVillagerRaider zombie = new EntityZombieVillagerRaider(world, province.getCenterX(), province.getCenterZ());
										BlockPos pos = creature.getPosition();
										zombie.setPosition(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
										world.spawnEntity(zombie);
									}
									creature.setHealth(0);
									creature.setDead();
									event.setCanceled(true);
								}
								else
								{
									if ( !world.isRemote )
									{
										EntityZombieRaider zombie = new EntityZombieRaider(world, province.getCenterX(), province.getCenterZ());
										BlockPos pos = creature.getPosition();
										zombie.setPosition(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
										world.spawnEntity(zombie);
									}
									creature.setHealth(0);
									creature.setDead();
									event.setCanceled(true);
								}
							}
						}
					}
				}
				return;
			}
			else if ( creature instanceof EntityVillager )
			{
				if ( !(creature instanceof EntityToroVillager || creature instanceof EntityFugitive) )
				{
					if ( ToroQuestConfiguration.useDefaultVillagers )
					{
						return;
					}
	
					if ( ToroQuestConfiguration.useDefaultVillagersOutsideOfProvince )
					{
						Province province = CivilizationUtil.getProvinceAt(creature.getEntityWorld(), creature.getPosition().getX()/16, creature.getPosition().getZ()/16);
						
						if ( province == null )
						{
							return;
						}
					}
					
					EntityVillager villager = (EntityVillager)creature;
	
					String jobName = villager.getProfessionForge().getCareer(0).getName();
					
					boolean flag = false;
					
					for ( Trade trade : ToroQuestConfiguration.trades )
					{
						if ( jobName.equals(trade.job) )
						{
							flag = true;
							break;
						}
					}
					
					if ( flag )
					{
						if ( !world.isRemote )
						{
							if ( world.rand.nextInt(100) == ToroQuestConfiguration.shopKeeperSpawnChance )
							{
								EntityShopkeeper newEntity = new EntityShopkeeper( world );
								BlockPos pos = creature.getPosition();
								newEntity.setPosition(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
								newEntity.setGrowingAge(villager.getGrowingAge());
								world.spawnEntity(newEntity);
							}
							else
							{
								@SuppressWarnings("deprecation")
								EntityToroVillager newEntity = new EntityToroVillager( world, villager.getProfession() );
								BlockPos pos = creature.getPosition();
								newEntity.setPosition(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
								if ( world.rand.nextInt(3) == 0 )
								{
									newEntity.setGrowingAge(-10000*(world.rand.nextInt(6)+1));
								}
								else
								{
									newEntity.setGrowingAge(villager.getGrowingAge());
								}
								world.spawnEntity(newEntity);
							}
						}
						villager.setHealth(0);
						villager.setDead();
						event.setCanceled(true);
					}
				}
				return;
			}
			else if ( !ToroQuestConfiguration.villagesSpawnGolems && creature.getClass().equals(EntityIronGolem.class) )
			{
				EntityIronGolem golem = (EntityIronGolem)creature;
				
				if ( !golem.isPlayerCreated() )
				{
					golem.setHealth(0);
					golem.setDead();
					event.setCanceled(true);
					return;
			 	}

				golem.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityToroMob>(golem, EntityToroMob.class, 0, true, true, new Predicate<EntityToroMob>()
				{
					@Override
					public boolean apply(EntityToroMob target)
					{
						return true;
					}
				}));
				golem.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityWolfRaider>(golem, EntityWolfRaider.class, 0, true, true, new Predicate<EntityWolfRaider>()
				{
					@Override
					public boolean apply(EntityWolfRaider target)
					{
						return true;
					}
				}));
				return;
			}
		}
//		else if ( entity instanceof EntityPlayer )
//		{
//			EntityPlayer ep = (EntityPlayer)entity;
//			Province province = CivilizationUtil.getProvinceAt(ep.getEntityWorld(), ep.chunkCoordX, ep.chunkCoordZ);
//			
//			if ( province != null && entity.dimension == 0 && ToroQuestConfiguration.showProvinceEnterLeaveMessage )
//			{
//				String subTitle = "House " + province.getCiv().getLocalizedName();
//				Minecraft.getMinecraft().ingameGUI.displayTitle(null, TextFormatting.ITALIC + subTitle, 0, 0, 0);
//				Minecraft.getMinecraft().ingameGUI.displayTitle(TextFormatting.BOLD + ep.getName(), subTitle, CivilizationHandlers.timeFadeIn, CivilizationHandlers.displayTime*2, CivilizationHandlers.timeFadeOut); // with TextFormatting
//			}
//		}
	}
	 
	@SubscribeEvent
	public void handleWorldTick(ClientTickEvent event)
	{
		TaskRunner.run();
	}

	public static class SyncTask implements Runnable
	{
		public void run()
		{
			ToroQuestPacketHandler.INSTANCE.sendToServer(new MessageRequestPlayerCivilizationSync());
		}
	}
	
	@SubscribeEvent
	public void crystalAttackFrom( AttackEntityEvent event )
	{
		Entity entityCrystal = event.getTarget();
		if ( entityCrystal != null && entityCrystal instanceof EntityEnderCrystal )
		{
			List<EntityMonolithEye> eyes = entityCrystal.world.getEntitiesWithinAABB(EntityMonolithEye.class, new AxisAlignedBB(entityCrystal.getPosition()).grow(96, 64, 96));
			if ( eyes.size() > 0 )
			{
				if ( event != null && !(event.getEntity() instanceof EntityMonolithEye) )
				{
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
    public void potionAdded(PotionAddedEvent event) // TODO
    {
    	if ( event.getEntityLiving() instanceof EntityWitch )
    	{
    		EntityWitch w0 = (EntityWitch)event.getEntityLiving();
    		event.getEntityLiving().clearActivePotions();
    		if ( w0.getAttackTarget() instanceof EntityWitch )
    		{
        		EntityWitch w1 = (EntityWitch)w0.getAttackTarget();
    			w0.setAttackTarget(null);
    			w1.setAttackTarget(null);
    		}
    	}
    }
//		
//		event.
//		System.out.println(event.getPotionEffect().getPotion());
//		
//		if ( event.getEntityLiving() instanceof EntitySentry )
//		{
//			event.
//			System.out.println(potion);
//
//			if ( potion.getThrower() instanceof EntitySentry )
//			{
//				System.out.println(potion.getThrower());
//				event.setCanceled(true);
//				return;
//			}
//		}
//    }
	
	@SubscribeEvent
	public void crystalProjectileImpact(ProjectileImpactEvent event)
	{
		// already in game : damage blazes & endermen
		
		// lava to obsidian / extinguish entity / extinguish flames
		
		if ( event == null || event.getEntity() == null )
		{
			return;
		}
		
		// === POTION ===
		if ( event.getEntity() instanceof EntityPotion && !event.getEntity().world.isRemote )
		{
			
//			Entity entity = event.getRayTraceResult().entityHit;
//			
//			if ( entity == null )
//			{
//				
//			}
			
        	EntityPotion p = (EntityPotion)event.getEntity();
			
			BlockPos pos = event.getRayTraceResult().getBlockPos();
			
			if ( pos == null )
			{
				pos = event.getRayTraceResult().entityHit.getPosition();
			}
			
			if ( pos != null )
			{
				if ( event.getEntity() instanceof EntitySmartArrow )
				{
					if ( event.getEntity().getEntityWorld().getBlockState(pos).getBlock() instanceof BlockFence || event.getEntity().getEntityWorld().getBlockState(pos).getBlock() instanceof BlockTrapDoor  )
					{
						event.setCanceled(true);
					}
				}
				else if ( p.getThrower() instanceof EntitySentry )
				{
					AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos).grow(4.0D, 2.0D, 4.0D);
			        List<EntitySentry> list = event.getEntity().world.<EntitySentry>getEntitiesWithinAABB(EntitySentry.class, axisalignedbb);
			 
		            for (EntitySentry entitylivingbase : list)
		            {
		            	if ( entitylivingbase instanceof EntitySentry && !(entitylivingbase instanceof EntityOrc) ) entitylivingbase.potionImmunity = 2;
		            }
					return;
				}
				else if ( p.getThrower() instanceof EntityPlayer || p.getThrower() == null )
				{
					Province province = CivilizationUtil.getProvinceAt(event.getEntity().world, pos.getX()/16, pos.getZ()/16);

			        if ( province == null )
					{
						return;
					}
			        
					EntityPlayer player = (EntityPlayer)p.getThrower();
					
					if ( player == null )
					{
						player = p.world.getClosestPlayerToEntity(p, 12);
						if ( player == null )
						{
							return;
						}
					}
					
					String potion = p.getPotion().getTextComponent().toString();

					for ( String s : ToroQuestConfiguration.safePotionList )
					{
						if ( potion.contains(s) )
			            {
			            	return;
			            }
					}
					
					AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos).grow(4.0D, 2.0D, 4.0D);
					
			        List<EntityGuard> guards = event.getEntity().world.<EntityGuard>getEntitiesWithinAABB(EntityGuard.class, axisalignedbb);
		            					
		            CivilizationType civ = province.getCiv();
		            
		    		if ( civ == null )
		    		{
		    			return;
		    		}
		    		
			        if ( !guards.isEmpty() )
			        {			        	
			            for (EntityLivingBase guard : guards)
			            {
			            	if ( guard instanceof EntityToroVillager )
			    			{
			    				((EntityToroVillager)guard).setUnderAttack(p.getThrower());
			    			}
			            }
			    		
			    		CivilizationHandlers.adjustPlayerRep((EntityPlayer)p.getThrower(), civ, -guards.size()*10);
			        }
			        
			        List<EntityVillager> villagers = event.getEntity().world.<EntityVillager>getEntitiesWithinAABB(EntityVillager.class, axisalignedbb);

			        if ( !villagers.isEmpty() )
			        {
			            for (EntityLivingBase villager : villagers)
			            {
			            	if ( villager instanceof EntityToroVillager )
			    			{
			    				((EntityToroVillager)villager).setUnderAttack(p.getThrower());
			    			}
			            }
			            
			    		CivilizationHandlers.adjustPlayerRep((EntityPlayer)(p).getThrower(), civ, -villagers.size()*10);
			        }
					return;
				}
				
					
			}
		}
		// ==============
		
		Entity entity = event.getRayTraceResult().entityHit;
		
		if ( entity == null )
		{
			return;
		}
		
//		System.out.println("hit: " + entity);

		if ( entity instanceof EntityEnderCrystal )
		{
			if ( !(event.getEntity() instanceof EntityFireball) )
			{
				List<EntityMonolithEye> eyes = entity.world.getEntitiesWithinAABB(EntityMonolithEye.class, new AxisAlignedBB(entity.getPosition()).grow(96, 64, 96));
				if ( eyes.size() > 0 )
				{
					if ( event.getEntity() != null )
					{
						event.setCanceled(true);
						return;
					}
				}
			}
		}
		else if ( entity instanceof EntityGuard )
		{
			if ( event.getEntity() instanceof EntityArrow )
			{
				EntityArrow arrow = (EntityArrow)event.getEntity();
				if ( arrow.shootingEntity instanceof EntityGuard )
				{
					event.setCanceled(true);
					return;
				}
			}
		}
		else if ( entity instanceof EntityToroMob )
		{
			if ( event.getEntity() instanceof EntityArrow )
			{
				EntityArrow arrow = (EntityArrow)event.getEntity();
				if ( arrow.shootingEntity instanceof EntityToroMob && arrow.shootingEntity.getClass() == entity.getClass() )
				{
					event.setCanceled(true);
					return;
				}
			}
		}
		else if ( entity instanceof EntityIronGolem )
		{
			if ( event.getEntity() instanceof EntityArrow )
			{
				EntityArrow arrow = (EntityArrow)event.getEntity();
				if ( arrow.shootingEntity instanceof EntityGuard )
				{
					event.setCanceled(true);
					return;
				}
			}
		}
	}

	// attackentityfrom damage
	@SubscribeEvent
	public void livingHurtEvent(LivingHurtEvent event)
	{
		EntityLivingBase victim = event.getEntityLiving();
		EntityLivingBase attacker = getAttacker(event);
		
		if ( victim == null || attacker == null )
		{
			return;
		}

//		if ( victim.world.isRemote )
//		{
//			return;
//		}
		
		// === VICTIM ===
		if ( victim instanceof EntityVillager && !(victim instanceof EntityFugitive) )
		{
			EntityVillager villager = (EntityVillager)victim;
			
			if ( attacker instanceof EntityToroNpc || attacker instanceof EntityVillager )
			{
				event.setAmount(0);
				event.setCanceled(true);
				return;
			}

			if ( attacker instanceof EntityPlayer ) // PLAYER
			{
            	EntityPlayer player = (EntityPlayer)attacker;
				Province province = CivilizationUtil.getProvinceAt(player.world, player.chunkCoordX, player.chunkCoordZ);
				
				if ( province == null )
				{
					province = CivilizationUtil.getProvinceAt(villager.world, villager.chunkCoordX, villager.chunkCoordZ);
				}

				if ( province != null )
				{
					CivilizationHandlers.adjustPlayerRep(player, province.civilization, -(int)MathHelper.clamp(event.getAmount()*4,5,villager.getHealth()*4));
				}
			}
			
			if ( villager instanceof EntityToroVillager )
			{
				((EntityToroVillager)villager).callForHelp(attacker, true);
			}
			else // VANILLA VILLAGER
			{
				boolean flag = false;
				
				List<EntityGuard> guards = villager.world.getEntitiesWithinAABB(EntityGuard.class, new AxisAlignedBB(villager.getPosition()).grow(16, 12, 16), new Predicate<EntityGuard>()
				{
					public boolean apply(@Nullable EntityGuard entity)
					{
						return true;
					}
				});
				
				for (EntityGuard guard : guards)
				{
					if ( guard.getAttackTarget() == null )
					{
						villager.getNavigator().tryMoveToEntityLiving(guard, 0.7F);
						
						if ( attacker instanceof EntityPlayer ) 
						{
							guard.setAnnoyed( (EntityPlayer)attacker );
							if ( !flag && guard.actionReady() && guard.getDistance(attacker) <= 8.0D )
							{
								guard.chat((EntityPlayer)attacker, "attackvillager", null);
								flag = true;
							}
						}
						guard.setAttackTarget(attacker);
					}
				}
			}
		}
		else if ( victim instanceof EntityAnimal )
		{
			if (victim instanceof EntityMule && attacker instanceof EntityPlayer)
			{
				List<EntityCaravan> caravans = victim.getEntityWorld().getEntitiesWithinAABB(EntityCaravan.class, victim.getEntityBoundingBox().grow(20.0D, 10.0D, 20.0D));
				for (EntityCaravan caravan : caravans)
				{
					((EntityToroVillager)caravan).setUnderAttack( attacker );
				}
			}
		}
		
		// === ATTACKER ===
		if ( attacker instanceof EntityGuard )
		{
			if ( victim instanceof EntityCreeper )
			{
				victim.setHealth(0);
			}
			else if ( victim instanceof EntityIronGolem || victim instanceof EntityVillager || victim instanceof EntityToroNpc )
			{
				((EntityGuard) attacker).setAttackTarget(null);
				event.setAmount(0);
				event.setCanceled(true);
				return;
			}
			else if ( victim instanceof EntityPlayer ) // PLAYER
			{
				event.setAmount( event.getAmount() * ToroQuestConfiguration.guardDamageBaseMultiplierToPlayers * this.rngDamageMultiplier() );
				return;
			}
			else
			{
				Province province = CivilizationUtil.getProvinceAt( attacker.getEntityWorld(), attacker.chunkCoordX, attacker.chunkCoordZ );
				
				if ( province == null || !victim.isNonBoss() || victim.getMaxHealth() >= ToroQuestConfiguration.minBaseHealthToBeConsideredBossMob ) // NO PROVINCE
				{
					event.setAmount( event.getAmount() * ToroQuestConfiguration.guardDamageBaseMultiplierToMobsOutsideProvinceOrToBosses );
					return;
				}
				else
				{
					event.setAmount( event.getAmount() * ToroQuestConfiguration.guardDamageBaseMultiplierToMobs * this.rngDamageMultiplier() );
				}
				
				CivilizationDataAccessor worldData = CivilizationsWorldSaveData.get(attacker.world);
				
				if ( worldData == null )
				{
					return;
				}
				
				if ( worldData.hasTrophyTitan(province.id) )
				{
					event.setAmount( event.getAmount() * ToroQuestConfiguration.trophyTitanAdditionalGuardDamageMulitiplier );
				}
			}
		}
		else if ( attacker instanceof EntityToroMob )
		{
			if ( attacker instanceof EntityOrc )
			{
				event.setAmount( event.getAmount() * this.rngDamageMultiplier() * ( (event.getSource()!=null&&(event.getSource().isMagicDamage())) ? (this.damageMultiplier(ToroQuestConfiguration.banditAttackDamage,attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue())) : 1.0F) );
			}
			else // if ( !(attacker instanceof EntityBanditLord) )
			{
				event.setAmount( event.getAmount() * this.rngDamageMultiplier() * ( (event.getSource()!=null&&(event.getSource().isMagicDamage())) ? (this.damageMultiplier(ToroQuestConfiguration.banditAttackDamage,attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue())) : 1.0F) );
			}
		}
		else if ( attacker instanceof EntityIronGolem )
		{
			if ( victim instanceof EntityToroNpc || victim instanceof EntityVillager )
			{
				((EntityIronGolem)attacker).setAttackTarget(null);
				event.setAmount(0);
				event.setCanceled(true);
			}
		}
	}
	
	protected float rngDamageMultiplier()
	{
		return 1.0F+((rand.nextFloat()-0.5F)/20.0F);
	}
	
	protected float damageMultiplier(float base, double current)
	{
		if ( current <= 0.0D || base <= 0.0F )
		{
			return 1.0F;
		}
		float amount = (float)( 1.0D+(current-base)/base );
		return amount>0.0F?amount:0.0F;
	}

	private EntityLivingBase getAttacker(LivingHurtEvent event)
	{
		try
		{
			return (EntityLivingBase) event.getSource().getTrueSource();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/*
	private BlockPos findSurface(World world, BlockPos start) {

		int minY = world.getActualHeight();

		BlockPos pos;

		IBlockState blockState;

		for (int y = world.getActualHeight(); y > 0; y--) {

			pos = new BlockPos(start.getX(), y, start.getZ());
			blockState = world.getBlockState(pos);

			if (isLiquid(blockState)) {
				return null;
			}

			if (isGroundBlock(blockState)) {
				if (y < minY) {
					minY = y;
				}

				break;
			}
		}

		return new BlockPos(start.getX(), minY, start.getZ());
	}

	private boolean isLiquid(IBlockState blockState) {
		return blockState.getBlock() == Blocks.WATER || blockState.getBlock() == Blocks.LAVA;
	}

	private boolean isGroundBlock(IBlockState blockState) {

		if (blockState.getBlock() == Blocks.LEAVES || blockState.getBlock() == Blocks.LEAVES2 || blockState.getBlock() == Blocks.LOG
				|| blockState.getBlock() instanceof BlockBush) {
			return false;
		}

		return blockState.isOpaqueCube();

	}
	*/
	
	static class SavedInventory
	{
		NonNullList<ItemStack> mainInventory = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
	    NonNullList<ItemStack> armorInventory = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
	    NonNullList<ItemStack> offHandInventory = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
		int experienceTotal;
	}
	
	private Map<String, SavedInventory> stack = new HashMap<String, SavedInventory>();
  
	@SubscribeEvent
	public void death(LivingDeathEvent event)
	{
		if ( (event.getEntity() instanceof EntityPlayer) )
		{
			EntityPlayer player = (EntityPlayer) event.getEntity();
			if ( player == null )
			{
				return;
			}
			
			if ( !ToroQuestConfiguration.enderIdolTeleport )
			{
				SavedInventory savedInventory = new SavedInventory();
				boolean hasEnderIdol = false;
				int m = 0;
				// =-=-=-=-=-=-= MAIN =-=-=-=-=-=-=
				for ( ItemStack itemStack : player.inventory.mainInventory )
				{
					if ( !hasEnderIdol && itemStack.getItem().equals(Item.getByNameOrId("toroquest:ender_idol")) )
					{
						player.inventory.mainInventory.set(m, ItemStack.EMPTY);
						savedInventory.experienceTotal = (0 + player.experienceTotal);
						hasEnderIdol = true;
					}
					else
					{
						savedInventory.mainInventory.set(m, itemStack);
					}
					m++;
				}
				// =-=-=-=-=-=-= ARMOR =-=-=-=-=-=-=
				int a = 0;
				for ( ItemStack itemStack : player.inventory.armorInventory )
				{
					savedInventory.armorInventory.set(a, itemStack);
					a++;
				}
				// =-=-=-=-=-=-= OFF =-=-=-=-=-=-=
				if ( !hasEnderIdol && player.inventory.offHandInventory.get(0).getItem().equals(Item.getByNameOrId("toroquest:ender_idol") ) )
				{
					player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
					savedInventory.experienceTotal = (0 + player.experienceTotal);
					hasEnderIdol = true;
				}
				else
				{
					savedInventory.offHandInventory.set(0, player.inventory.offHandInventory.get(0));
				}
				if ( hasEnderIdol )
				{
					player.inventory.clear();
					player.closeScreen();
					player.inventory.closeInventory(player);
					player.experienceTotal = 0;
					stack.put( player.getName(), savedInventory );
					player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F );
					player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F );
					event.setCanceled(true);
				}
				else
				{
					savedInventory = null;
					//if ( stack.containsKey( player.getName()) )
					try
					{
						stack.remove( player.getName() );
					}
					catch ( Exception e )
					{
						
					}
				}
			}
			else
			{
				boolean hasEnderIdol = false;
				int m = 0;
				// =-=-=-=-=-=-= MAIN =-=-=-=-=-=-=
				for ( ItemStack itemStack : player.inventory.mainInventory )
				{
					if ( !hasEnderIdol && itemStack.getItem().equals(Item.getByNameOrId("toroquest:ender_idol")) )
					{
						player.inventory.mainInventory.set(m, ItemStack.EMPTY);
						hasEnderIdol = true;
					}
					m++;
				}
				// =-=-=-=-=-=-= OFF =-=-=-=-=-=-=
				if ( !hasEnderIdol && player.inventory.offHandInventory.get(0).getItem().equals(Item.getByNameOrId("toroquest:ender_idol") ) )
				{
					player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
					hasEnderIdol = true;
				}
				if ( hasEnderIdol )
				{
					player.closeScreen();
					player.inventory.closeInventory(player);
					player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F );
					player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F );
					player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F );
					player.playSound(SoundEvents.ENTITY_ENDEREYE_DEATH, 1.0F, 1.0F );
					player.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F );
					player.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F );
					this.teleportRandomly(player);
					player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F );
					player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F );
					player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F );
					player.playSound(SoundEvents.ENTITY_ENDEREYE_DEATH, 1.0F, 1.0F );
					player.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F );
					player.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F );
					player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 30, 1, true, false));
					player.setHealth(player.getMaxHealth());
					event.setCanceled(true);
				}
			}
		}
		else if ( event.getEntity() instanceof IMob || event.getEntity() instanceof EntityMob )
		{
			if ( !(event.getEntity() instanceof EntityLivingBase) )
			{
				return;
			}
			EntityLivingBase mob = (EntityLivingBase)(event.getEntity());
			DamageSource source = event.getSource();
			if ( source == null ) return;
			if ( source.getTrueSource() == null ) return;
			
			if ( !(source.getTrueSource() instanceof EntityPlayer) )
			{
				return;
			}
			
			EntityPlayer player = (EntityPlayer)(source.getTrueSource());
	
			Province province = PlayerCivilizationCapabilityImpl.get(player).getInCivilization();
			
			if ( rand.nextInt(2000) <= ToroQuestConfiguration.artifactDropRate )
			{
				ItemStack item = CivilizationHandlers.randomStolenItem( mob.world, province );
				if ( item == null ) return;
				mob.entityDropItem( item, 1 );
			}
//			if (province == null || province.civilization == null)
//			{
//				return;
//			}
			//CivilizationHandlers.adjustPlayerRep(player, province.civilization, 1);
		}
	}

	protected void teleportRandomly(EntityPlayer p)
    {
		BlockPos pos = CivilizationHandlers.findTeleportLocationFrom(p.world, p.getPosition());
		if ( pos != null ) p.attemptTeleport(pos.getX()+(rand.nextBoolean()?8:-8), pos.getY(), pos.getZ()+(rand.nextBoolean()?8:-8));
    }
	
	@SubscribeEvent
	public void respawn(PlayerEvent.Clone event)
	{
		if ( !ToroQuestConfiguration.enderIdolTeleport )
		{
			EntityPlayer player = event.getEntityPlayer();
			if ( player == null )
			{
				return;
			}
			try
			{
				SavedInventory savedIventory = stack.get( player.getName() );
				int m = 0;
				for ( ItemStack itemStack : savedIventory.mainInventory )
				{
					player.inventory.mainInventory.set(m, itemStack);
					m++;
				}
				int a = 0;
				for ( ItemStack itemStack : savedIventory.armorInventory )
				{
					player.inventory.armorInventory.set(a, itemStack);
					a++;
				}
				player.inventory.offHandInventory.set(0, savedIventory.offHandInventory.get(0));
				player.addExperience( savedIventory.experienceTotal );
				player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.8F );
				player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_SCREAM, SoundCategory.PLAYERS, 0.8F, 0.8F );
				stack.remove( player.getName() );
			}
			catch ( Exception e )
			{
				
			}
		}
	}
}
