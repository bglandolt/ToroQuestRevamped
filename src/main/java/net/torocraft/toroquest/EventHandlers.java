package net.torocraft.toroquest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockClay;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockDoubleStoneSlab;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStem;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.command.TextComponentHelper;
import net.torocraft.toroquest.civilization.CivilizationDataAccessor;
import net.torocraft.toroquest.civilization.CivilizationType;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.CivilizationsWorldSaveData;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.civilization.ReputationLevel;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapability;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.civilization.quests.QuestBase;
import net.torocraft.toroquest.civilization.quests.QuestBreed;
import net.torocraft.toroquest.civilization.quests.QuestBuild;
import net.torocraft.toroquest.civilization.quests.QuestBuild.DataWrapper;
import net.torocraft.toroquest.civilization.quests.QuestFarm;
import net.torocraft.toroquest.civilization.quests.QuestMine;
import net.torocraft.toroquest.civilization.quests.QuestRecruit;
import net.torocraft.toroquest.civilization.quests.util.QuestData;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.config.ToroQuestConfiguration.Raider;
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
import net.torocraft.toroquest.entities.EntityVillageLord;
import net.torocraft.toroquest.entities.EntityWolfRaider;
import net.torocraft.toroquest.entities.EntityZombieRaider;
import net.torocraft.toroquest.entities.EntityZombieVillagerRaider;
import net.torocraft.toroquest.entities.ai.EntityAIRaidDespawn;
import net.torocraft.toroquest.network.ToroQuestPacketHandler;
import net.torocraft.toroquest.network.message.MessageRequestPlayerCivilizationSync;
import net.torocraft.toroquest.util.TaskRunner;

public class EventHandlers
{

	public static Random rand = new Random();

	private int RAIDER_DISTANCE = ToroQuestConfiguration.disableMobSpawningNearVillage + 2 < 76 ? 76 : ToroQuestConfiguration.disableMobSpawningNearVillage + 2;
	private final String INITIAL_SPAWN_TAG = "initialSpawn";

	public static int MAX_SPAWN_HEIGHT = ToroQuestConfiguration.maxSpawnHeight;
	public static int MIN_SPAWN_HEIGHT = ToroQuestConfiguration.minSpawnHeight;
	public static int SPAWN_RANGE = MAX_SPAWN_HEIGHT - MIN_SPAWN_HEIGHT;

	public static final int timeFadeIn = 20;
	public static final int displayTime = 50;
	public static final int timeFadeOut = 20;

	protected int spawningTicks = 0;

	public static void repLevelMessage( EntityPlayer player, CivilizationType civ, int startRep, int afterRep ) // TODO
	{
		if ( startRep > afterRep )
		{
			return;
		}

		ReputationLevel before = ReputationLevel.fromReputation(startRep);
		ReputationLevel after = ReputationLevel.fromReputation(afterRep);

		if ( before == after )
		{
			return;
		}

		if ( after == ReputationLevel.FRIENDLY ) 		// 50
		{
			if ( player instanceof EntityPlayerMP )
			{
				ToroQuestTriggers.FRIENDLY_ACHIEVEMNT.trigger((EntityPlayerMP) player);
			}
		}
		else if ( after == ReputationLevel.HONORED )	// 100
		{
			if ( player instanceof EntityPlayerMP )
			{
				ToroQuestTriggers.FRIENDLY_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.HONORED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
			}
		}
		else if ( after == ReputationLevel.RENOWNED )	// 250
		{
			if ( player instanceof EntityPlayerMP )
			{
				ToroQuestTriggers.FRIENDLY_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.HONORED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.RENOWNED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
			}
		}
		else if ( after == ReputationLevel.EXALTED )	// 500
		{
			if ( player instanceof EntityPlayerMP )
			{
				ToroQuestTriggers.FRIENDLY_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.HONORED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.RENOWNED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.EXALTED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
			}
		}
		else if ( after == ReputationLevel.CHAMPION )	// 1000
		{
			if ( player instanceof EntityPlayerMP )
			{
				ToroQuestTriggers.FRIENDLY_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.HONORED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.RENOWNED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.EXALTED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.CHAMPION_ACHIEVEMNT.trigger((EntityPlayerMP) player);
			}
		}
		else if ( after == ReputationLevel.HERO )		// 2000
		{
			if ( player instanceof EntityPlayerMP )
			{
				ToroQuestTriggers.FRIENDLY_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.HONORED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.RENOWNED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.EXALTED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.CHAMPION_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.HERO_ACHIEVEMNT.trigger((EntityPlayerMP) player);
			}
		}
		else if ( after == ReputationLevel.LEGEND )		// 3000
		{
			if ( player instanceof EntityPlayerMP )
			{
				ToroQuestTriggers.FRIENDLY_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.HONORED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.RENOWNED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.EXALTED_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.CHAMPION_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.HERO_ACHIEVEMNT.trigger((EntityPlayerMP) player);
				ToroQuestTriggers.LEGEND_ACHIEVEMNT.trigger((EntityPlayerMP) player);
			}
		}
	}

	@SubscribeEvent
	public void onDeath( PlayerEvent.Clone event )
	{
		if ( event.getEntityPlayer().getEntityWorld().isRemote )
		{
			return;
		}

		PlayerCivilizationCapability newCap = PlayerCivilizationCapabilityImpl.get(event.getEntityPlayer());
		PlayerCivilizationCapability originalCap = PlayerCivilizationCapabilityImpl.get(event.getOriginal());

		if ( originalCap == null )
		{
			return;
		}

		if ( newCap == null )
		{
			throw new NullPointerException("missing player capability during clone");
		}

		newCap.readNBT(originalCap.writeNBT());
	}

	@SubscribeEvent
	public void onSave( PlayerEvent.SaveToFile event )
	{
		if ( event.getEntityPlayer().getEntityWorld().isRemote )
		{
			return;
		}
		PlayerCivilizationCapability cap = PlayerCivilizationCapabilityImpl.get(event.getEntityPlayer());
		if ( cap == null )
		{
			return;
		}

		NBTTagCompound civData = cap.writeNBT();

		if ( civData == null || civData.getTag("reputations") == null || ((NBTTagList) civData.getTag("reputations")).tagCount() < 1 )
		{
			return;
		}

		event.getEntityPlayer().getEntityData().setTag(ToroQuest.MODID + ".playerCivilization", civData);
	}

	@SubscribeEvent
	public void onLoad( PlayerEvent.LoadFromFile event )
	{
		if ( event.getEntityPlayer() == null || event.getEntityPlayer().getEntityWorld().isRemote )
		{
			return;
		}

		PlayerCivilizationCapability cap = PlayerCivilizationCapabilityImpl.get(event.getEntityPlayer());

		if ( cap == null )
		{
			return;
		}

		NBTTagCompound c = event.getEntityPlayer().getEntityData().getCompoundTag(ToroQuest.MODID + ".playerCivilization");

		cap.readNBT(c);
	}

	@SubscribeEvent
	public void onEntityLoad( AttachCapabilitiesEvent<Entity> event )
	{
		if ( !(event.getObject() instanceof EntityPlayer) )
		{
			return;
		}
		EntityPlayer player = (EntityPlayer) event.getObject();
		event.addCapability(new ResourceLocation(ToroQuest.MODID, "playerCivilization"), new PlayerCivilizationCapabilityProvider(player));
		syncClientCapability(player);
	}

	private void syncClientCapability( EntityPlayer player )
	{
		// rrmote
		if ( player.getEntityWorld().isRemote )
		{
			TaskRunner.queueTask(new SyncTask(), 30);
		}
	}

	public static class PlayerCivilizationCapabilityProvider implements ICapabilityProvider
	{

		@CapabilityInject( PlayerCivilizationCapability.class )
		public static final Capability<PlayerCivilizationCapability> CAP = null;

		private PlayerCivilizationCapability instance;

		public PlayerCivilizationCapabilityProvider( EntityPlayer player )
		{
			instance = new PlayerCivilizationCapabilityImpl(player);
		}

		@Override
		public boolean hasCapability( Capability<?> capability, EnumFacing facing )
		{
			return capability == CAP;
		}

		@Override
		public <T> T getCapability( Capability<T> capability, EnumFacing facing )
		{
			if ( CAP != null && capability == CAP )
			{
				return PlayerCivilizationCapabilityImpl.INSTANCE.cast(instance);
			}
			return null;
		}
	}

	public static void adjustPlayerRep( EntityPlayer player, int chunkX, int chunkZ, int value )
	{
		if ( player == null || player.world.isRemote || player.dimension != 0 )
		{
			return;
		}

		Province province = CivilizationUtil.getProvinceAt(player.getEntityWorld(), chunkX, chunkZ);

		if ( province == null )
		{
			return;
		}

		adjustPlayerRep(player, province.civilization, value);
	}

	public static void adjustPlayerRep( EntityPlayer player, CivilizationType civ, int value )
	{
		if ( player == null || player.world.isRemote || civ == null || player.dimension != 0 )
		{
			return;
		}

		if ( value < 0 )
		{
			player.sendStatusMessage(TextComponentHelper.createComponentTranslation(player, "text.toroquest.crime_reported", new Object[0]), true);
		}

		int startRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(civ);
		int afterRep = startRep + value;

		if ( afterRep < -3000 )
		{
			PlayerCivilizationCapabilityImpl.get(player).setReputation(civ, -3000);
		}
		else
		{
			PlayerCivilizationCapabilityImpl.get(player).adjustReputation(civ, value);
		}

		repLevelMessage(player, civ, startRep, afterRep);

	}

	public static void reportCrimeRep( EntityPlayer player, Province province, int value )
	{
		if ( player == null || player.world.isRemote || province == null || province.civilization == null || player.dimension != 0 )
		{
			return;
		}

		player.sendStatusMessage(TextComponentHelper.createComponentTranslation(player, "text.toroquest.crime_reported", new Object[0]), true);

		CivilizationType civ = province.getCiv();

		int startRep = PlayerCivilizationCapabilityImpl.get(player).getReputation(civ);
		int afterRep = startRep + value;

		if ( afterRep < -3000 )
		{
			PlayerCivilizationCapabilityImpl.get(player).setReputation(civ, -3000);
		}
		else
		{
			PlayerCivilizationCapabilityImpl.get(player).adjustReputation(civ, value);
		}
	}

	@SubscribeEvent
	public void checkKillInCivilization( LivingDeathEvent event )
	{
		DamageSource source = event.getSource();

		if ( source == null )
		{
			return;
		}

		Entity s = source.getTrueSource();

		if ( !(s instanceof EntityPlayer) || s.world.isRemote )
		{
			return;
		}

		EntityPlayer player = (EntityPlayer) s;

		Entity e = event.getEntity();

		if ( e == null || !(e instanceof EntityLivingBase) )
		{
			return;
		}

		EntityLivingBase victim = (EntityLivingBase) e;

		if ( victim instanceof EntityMule && e instanceof EntityPlayer )
		{
			List<EntityCaravan> caravans = victim.getEntityWorld().getEntitiesWithinAABB(EntityCaravan.class, victim.getEntityBoundingBox().grow(16, 16, 16));
			for ( EntityCaravan caravan : caravans )
			{
				if ( ((EntityMule) victim).getLeashHolder() == caravan )
				{
					((EntityToroVillager) caravan).setMurder((EntityPlayer) e);
				}
			}
		}

		Province province = PlayerCivilizationCapabilityImpl.get(player).getInCivilization();

		if ( province == null || province.civilization == null )
		{
			return;
		}

		if ( victim instanceof IMob || victim instanceof EntityMob )
		{
			float hpr = victim.getMaxHealth() / ToroQuestConfiguration.healthOfMobNeededToGainOneRep;

			if ( hpr < 1.0F )
			{
				if ( rand.nextFloat() < hpr )
				{
					adjustPlayerRep(player, province.civilization, 1);
				}
			}
			else
			{
				if ( rand.nextFloat() < hpr % 1 )
				{
					hpr++;
				}
				else
				{

				}
				adjustPlayerRep(player, province.civilization, (int) hpr);
			}
		}
		else if ( victim instanceof EntityVillager || victim instanceof EntityGuard )
		{
			World world = victim.world;

			reportCrimeRep(player, province, -ToroQuestConfiguration.murderRepLoss);

			List<EntityToroNpc> guards = world.getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(20, 16, 20), new Predicate<EntityToroNpc>()
			{
				public boolean apply( @Nullable EntityToroNpc entity )
				{
					return true;
				}
			});

			for ( EntityToroNpc guard : guards )
			{
				guard.setMurder(player);
				guard.setAttackTarget(player);
			}

			List<EntityToroVillager> villagers = world.getEntitiesWithinAABB(EntityToroVillager.class, new AxisAlignedBB(player.getPosition()).grow(20, 16, 20), new Predicate<EntityToroVillager>()
			{
				public boolean apply( @Nullable EntityToroVillager entity )
				{
					return true;
				}
			});

			for ( EntityToroVillager villager : villagers )
			{
				villager.setMurder(player);
			}
		}

		int rep = PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization);

		if ( rep >= 100 || (!ToroQuestConfiguration.loseReputationForAnimalGrief) )
		{
			return;
		}

		if ( victim instanceof EntityChicken || (victim instanceof EntityHorse && (!((EntityHorse) victim).isTame() && !((EntityHorse) victim).isHorseSaddled())) || (victim instanceof EntityDonkey && (!((EntityDonkey) victim).isTame() || !((EntityDonkey) victim).isHorseSaddled())) || victim instanceof EntityPig || victim instanceof EntitySheep || victim instanceof EntityCow || victim instanceof EntityMule )
		{

			boolean witnessed = villagersReportCrime(player.getEntityWorld(), player);

			List<EntityToroNpc> help = player.world.getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
			{
				public boolean apply( @Nullable EntityToroNpc entity )
				{
					return true;
				}
			});
			Collections.shuffle(help);
			boolean flag = false;
			for ( EntityToroNpc entity : help )
			{
				if ( !entity.canEntityBeSeen(player) )
				{
					continue;
				}

				witnessed = true;
				entity.getNavigator().tryMoveToEntityLiving(player, 0.6D);

				entity.setAnnoyed(player);
				entity.setAttackTarget(player);

				if ( !flag )
				{
					flag = true;
					entity.chat(entity, player, "butcher", null);
				}
			}
			if ( witnessed )
				reportCrimeRep(player, province, -ToroQuestConfiguration.murderLivestockRepLoss);
		}
		else if ( victim instanceof EntityIronGolem )
		{
			// if ( rep < 100 && ToroQuestConfiguration.loseReputationForAnimalGrief )
			{
				boolean witnessed = villagersReportCrime(player.getEntityWorld(), player);

				List<EntityToroNpc> help = player.world.getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
				{
					public boolean apply( @Nullable EntityToroNpc entity )
					{
						return true;
					}
				});
				Collections.shuffle(help);
				boolean flag = false;
				for ( EntityToroNpc entity : help )
				{
					if ( !entity.canEntityBeSeen(player) )
					{
						continue;
					}

					witnessed = true;
					entity.getNavigator().tryMoveToEntityLiving(player, 0.6D);

					entity.setAnnoyed(player);
					entity.setAttackTarget(player);

					if ( !flag )
					{
						flag = true;
						entity.chat(entity, player, "golemKill", null);
					}
				}
				if ( witnessed )
					reportCrimeRep(player, province, -ToroQuestConfiguration.murderLivestockRepLoss);
			}
		}
	}

	@SubscribeEvent
	public void handleEnteringProvince( EntityEvent.EnteringChunk event )
	{
		if ( !(event.getEntity() instanceof EntityPlayerMP) )
		{
			return;
		}
		EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
		PlayerCivilizationCapabilityImpl.get(player).updatePlayerLocation(event.getNewChunkX(), event.getNewChunkZ());
	}

	@SideOnly( Side.CLIENT )
	public static TextComponentString leavingMessage( EntityPlayer player, Province province )
	{
		int rep = PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization);
		String s;
		if ( rep >= 50 )
		{
			s = province.civilization.getFriendlyLeavingMessage(province);
		}
		else if ( rep <= -10 )
		{
			s = province.civilization.getHostileLeavingMessage(province);
		}
		else
		{
			s = province.civilization.getNeutralLeavingMessage(province);
		}
		return new TextComponentString(s);
	}

	@SideOnly( Side.CLIENT )
	public static TextComponentString enteringMessage( EntityPlayer player, Province province )
	{
		int rep = PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization);
		String s;
		if ( rep >= 10 )
		{
			s = province.civilization.getFriendlyEnteringMessage(province);
		}
		else if ( rep <= -10 )
		{
			s = province.civilization.getHostileEnteringMessage(province);
		}
		else
		{
			s = province.civilization.getNeutralEnteringMessage(province);
		}
		return new TextComponentString(s);
	}

	@SubscribeEvent
	public void breed( BabyEntitySpawnEvent event )
	{
		EntityPlayer e = event.getCausedByPlayer();

		if ( e == null || e.world.isRemote )
		{
			return;
		}

		Province province = PlayerCivilizationCapabilityImpl.get(e).getInCivilization();

		if ( province == null || province.civilization == null )
		{
			return;
		}

		if ( !(event.getParentA() instanceof EntityAnimal) )
		{
			return;
		}

		EntityAnimal animal = (EntityAnimal) event.getParentA();

		if ( !(event.getParentB() instanceof EntityAnimal) )
		{
			return;
		}

		EntityPlayer playerA = ((EntityAnimal) event.getParentA()).getLoveCause();
		EntityPlayer playerB = ((EntityAnimal) event.getParentB()).getLoveCause();

		if ( playerA != null )
		{
			if ( rand.nextInt(3) == 0 )
				adjustPlayerRep(playerA, event.getParentA().chunkCoordX, event.getParentA().chunkCoordZ, 1);

			try
			{
				QuestBreed.INSTANCE.onBreed(e, animal);
			}
			catch (Exception ex)
			{

			}

			return;
		}

		animal = (EntityAnimal) event.getParentB();

		if ( playerB != null )
		{
			if ( rand.nextInt(3) == 0 )
				adjustPlayerRep(playerB, event.getParentB().chunkCoordX, event.getParentB().chunkCoordZ, 1);

			try
			{
				QuestBreed.INSTANCE.onBreed(e, animal);
			}
			catch (Exception ex)
			{

			}

			return;
		}
	}

	@SubscribeEvent
	public void onBucketUse( FillBucketEvent event )
	{
		if ( event.getWorld().isRemote )
		{
			return;
		}

		EntityPlayer player = event.getEntityPlayer();

		if ( player == null )
		{
			return;
		}

		final RayTraceResult target = event.getTarget();
		if ( target == null || target.typeOfHit != RayTraceResult.Type.BLOCK )
			return;
		BlockPos blockPos = new BlockPos(target.hitVec.x, target.hitVec.y, target.hitVec.z);

		if ( event.getEmptyBucket() == null )
			return;

		// reputation
		Province province = PlayerCivilizationCapabilityImpl.get(player).getInCivilization();

		if ( province == null || province.civilization == null )
		{
			return;
		}

		int rep = PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization);

		if ( event.getEmptyBucket().getUnlocalizedName().contains("Water") )
		{
			// bandits
			List<EntityToroMob> mob = event.getWorld().getEntitiesWithinAABB(EntityToroMob.class, new AxisAlignedBB(blockPos).grow(6, 6, 6), new Predicate<EntityToroMob>()
			{
				public boolean apply( @Nullable EntityToroMob entity )
				{
					return true;
				}
			});
			for ( EntityToroMob m : mob )
			{
				if ( rand.nextBoolean() )
					m.setAttackTarget(player);
			}

			// guards
			List<EntityToroNpc> guards = event.getWorld().getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(blockPos).grow(4, 4, 4), new Predicate<EntityToroNpc>()
			{
				public boolean apply( @Nullable EntityToroNpc entity )
				{
					return true;
				}
			});
			Collections.shuffle(guards);

			boolean flag = false;

			if ( rep >= 250 || (!ToroQuestConfiguration.loseReputationForBlockGrief) )
			{
				for ( EntityToroNpc entity : guards )
				{
					if ( !entity.canEntityBeSeen(player) )
					{
						continue;
					}

					// if ( entity instanceof EntityVillageLord )
					// {
					// entity.setAnnoyed(player);
					// }
					// else if ( rand.nextBoolean() )
					// {
					// entity.setAnnoyed(player);
					// }

					if ( !flag )
					{
						flag = true;
						entity.chat(entity, player, "water", null);
					}
				}
			}
			else
			{
				for ( EntityToroNpc entity : guards )
				{
					if ( !entity.canEntityBeSeen(player) )
					{
						continue;
					}

					if ( entity instanceof EntityVillageLord )
					{
						entity.setAnnoyed(player);
						((EntityVillageLord) entity).callForHelp(player);
					}
					else
					{
						if ( entity.isAnnoyed() )
						{
							if ( !entity.inCombat() )
							{
								entity.setAnnoyed(player);
								entity.setAttackTarget(player);
							}
						}
						else
						{
							entity.setAnnoyed(player);
						}
					}

					if ( !flag )
					{
						flag = true;
						entity.chat(entity, player, "water", null);
					}
				}
			}
			if ( flag )
				reportCrimeRep(player, province, -ToroQuestConfiguration.unexpensiveRepLoss);
		}
		else if ( event.getEmptyBucket().getUnlocalizedName().contains("Lava") )
		{
			// bandits
			List<EntityToroMob> mob = event.getWorld().getEntitiesWithinAABB(EntityToroMob.class, new AxisAlignedBB(player.getPosition()).grow(6, 6, 6), new Predicate<EntityToroMob>()
			{
				public boolean apply( @Nullable EntityToroMob entity )
				{
					return true;
				}
			});
			for ( EntityToroMob m : mob )
			{
				m.setAttackTarget(player);
			}

			// guards
			List<EntityToroNpc> help = event.getWorld().getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
			{
				public boolean apply( @Nullable EntityToroNpc entity )
				{
					return true;
				}
			});
			Collections.shuffle(help);

			boolean flag = false;
			boolean witnessed = false;

			boolean onGuard = !(event.getWorld().getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(blockPos).grow(4, 4, 4), new Predicate<EntityToroNpc>()
			{
				public boolean apply( @Nullable EntityToroNpc entity )
				{
					return true;
				}
			}).isEmpty());

			boolean onVillager = !(event.getWorld().getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(blockPos).grow(4, 4, 4), new Predicate<EntityVillager>()
			{
				public boolean apply( @Nullable EntityVillager entity )
				{
					return true;
				}
			}).isEmpty());

			if ( onGuard || onVillager )
			{
				witnessed = villagersReportCrime(event.getWorld(), player);
				for ( EntityToroNpc entity : help )
				{
					if ( !entity.canEntityBeSeen(player) )
					{
						continue;
					}
					entity.setAnnoyed(player);
					entity.setAttackTarget(player);
					if ( !flag )
					{
						flag = true;
						entity.chat(entity, player, "lavaonperson", null);
					}
				}
				reportCrimeRep(player, province, -ToroQuestConfiguration.lavaGriefRepLoss);
			}
			else if ( rep >= 250 || (!ToroQuestConfiguration.loseReputationForBlockGrief) )
			{
				for ( EntityToroNpc entity : help )
				{
					if ( !entity.canEntityBeSeen(player) )
					{
						continue;
					}
					if ( !flag )
					{
						flag = true;
						if ( entity.actionReady() )
						{
							entity.chat(entity, player, "lavaallowed", null);
						}
					}
				}
				return;
			}
			else
			{
				witnessed = villagersReportCrime(event.getWorld(), player);

				for ( EntityToroNpc entity : help )
				{
					if ( !entity.canEntityBeSeen(player) )
					{
						continue;
					}

					witnessed = true;
					entity.getNavigator().tryMoveToEntityLiving(player, 0.6D);

					entity.setAnnoyed(player);
					entity.setAttackTarget(player);

					if ( !flag )
					{
						flag = true;
						entity.chat(entity, player, "lavacrime", null);
					}
				}
				if ( witnessed )
					reportCrimeRep(player, province, -ToroQuestConfiguration.lavaGriefRepLoss);
			}
		}
	}

	@SubscribeEvent
	public void placeEvent( EntityPlaceEvent event )
	{
		if ( event.getWorld().isRemote )
		{
			return;
		}

		Block e = event.getState().getBlock();

		BlockPos blockPos = event.getPos();

		if ( blockPos == null )
		{
			return;
		}

		Entity eventEntity = event.getEntity();

		if ( eventEntity == null || !(eventEntity instanceof EntityPlayer) )
		{
			return;
		}

		EntityPlayer player = (EntityPlayer) eventEntity;
		
		if ( e instanceof net.minecraftforge.common.IPlantable && !(e instanceof net.minecraftforge.common.IShearable) )
		{
			try
			{
				QuestFarm.INSTANCE.onFarm(player);
			}
			catch (Exception ee)
			{

			}
			return;
		}
		else
		// {
		// if ( e instanceof BlockDoor )
		// {
		// Village v =
		// eventEntity.world.getVillageCollection().getNearestVillage(event.getPos(),
		// 64);
		// System.out.println(v != null ? v.getNumVillageDoors(): "novillage");
		// Set<QuestData> quests =
		// PlayerCivilizationCapabilityImpl.get(player).getCurrentQuests();
		//
		// DataWrapper quest = new DataWrapper();
		//
		// for ( QuestData data : quests )
		// {
		// quest.setData(data);
		//
		// if ( quest.isBuildQuest() )
		// {
		// Province province =
		// PlayerCivilizationCapabilityImpl.get(player).getInCivilization();
		//
		// if ( province != null && province.civilization != null &&
		// quest.isInCorrectProvince(province) )
		// {
		// QuestBuild.INSTANCE.perform(quest);
		// }
		// }
		// }
		// }
		// else
		if ( event.getPlacedBlock().getMaterial() == Material.ROCK ) // || event.getPlacedBlock() instanceof BlockStone
																	 // || event.getPlacedBlock() instanceof
																	 // BlockStoneBrick || event.getPlacedBlock()
																	 // instanceof BlockSandStone ) // ||
																	 // event.getPlacedBlock() instanceof BlockStone )
		{
			Set<QuestData> quests = PlayerCivilizationCapabilityImpl.get(player).getCurrentQuests();

			DataWrapper quest = new DataWrapper();

			for ( QuestData data : quests )
			{
				quest.setData(data);

				if ( quest.isBuildQuest() )
				{
					Province province = PlayerCivilizationCapabilityImpl.get(player).getInCivilization();

					if ( province != null && province.civilization != null && quest.isInCorrectProvince(province) )
					{
						QuestBuild.INSTANCE.perform(quest);
					}
				}
			}
		}
		// }

		if ( !(e instanceof BlockFire) && !(e instanceof BlockTNT) )
		{
			return;
		}

		// Bandits
		List<EntityToroMob> mob = event.getWorld().getEntitiesWithinAABB(EntityToroMob.class, new AxisAlignedBB(blockPos).grow(6, 6, 6), new Predicate<EntityToroMob>()
		{
			public boolean apply( @Nullable EntityToroMob entity )
			{
				return true;
			}
		});
		for ( EntityToroMob m : mob )
		{
			m.setAttackTarget(player);
		}

		boolean onGuard = !(event.getWorld().getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(blockPos).grow(3, 3, 3), new Predicate<EntityToroNpc>()
		{
			public boolean apply( @Nullable EntityToroNpc entity )
			{
				return true;
			}
		}).isEmpty());

		boolean onVillager = !(event.getWorld().getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(blockPos).grow(3, 3, 3), new Predicate<EntityVillager>()
		{
			public boolean apply( @Nullable EntityVillager entity )
			{
				return true;
			}
		}).isEmpty());

		Province province = PlayerCivilizationCapabilityImpl.get(player).getInCivilization();

		if ( province == null || province.civilization == null )
		{
			return;
		}

		List<EntityToroNpc> help = event.getWorld().getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
		{
			public boolean apply( @Nullable EntityToroNpc entity )
			{
				return true;
			}
		});
		Collections.shuffle(help);

		int rep = PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization);

		boolean witnessed = false;

		boolean flag = false;

		if ( e instanceof BlockTNT )
		{
			for ( EntityToroNpc entity : help )
			{
				if ( !entity.canEntityBeSeen(player) )
				{
					continue;
				}

				if ( entity.isAnnoyed() )
				{
					if ( !entity.inCombat() )
					{
						entity.setAnnoyed(player);
						entity.setAttackTarget(player);
					}
				}
				else
				{
					entity.setAnnoyed(player);
				}

				if ( !flag )
				{
					flag = true;
					entity.chat(entity, player, "explosives", null);
				}
			}
			witnessed = villagersReportCrime(event.getWorld(), player);
		}
		else if ( onGuard || onVillager )
		{
			for ( EntityToroNpc entity : help )
			{
				entity.setAnnoyed(player);
				if ( !entity.inCombat() )
					entity.setAttackTarget(player);

				if ( !flag )
				{
					flag = true;
					entity.chat(entity, player, "fireonperson", null);
				}
			}
			witnessed = villagersReportCrime(event.getWorld(), player);
		}
		else if ( rep < 250 && ToroQuestConfiguration.loseReputationForBlockGrief )
		{
			for ( EntityToroNpc entity : help )
			{
				if ( !entity.canEntityBeSeen(player) )
				{
					continue;
				}

				witnessed = true;
				entity.getNavigator().tryMoveToEntityLiving(player, 0.6D);

				if ( entity.isAnnoyed() )
				{
					if ( !entity.inCombat() )
					{
						entity.setAnnoyed(player);
						entity.setAttackTarget(player);
					}
				}
				else
				{
					entity.setAnnoyed(player);
				}

				if ( !flag )
				{
					flag = true;
					entity.chat(entity, player, "firespread", null);
				}
			}
			witnessed = villagersReportCrime(event.getWorld(), player);
		}
		if ( witnessed )
			reportCrimeRep(player, province, -ToroQuestConfiguration.fireGriefRepLoss);
	}

	@SubscribeEvent
	public void harvestDrops( HarvestDropsEvent event )
	{
		if ( event.getWorld().isRemote )
		{
			return;
		}

		/* Quest Mine */
		if ( event.getHarvester() != null )
		{
			Set<QuestData> quests = PlayerCivilizationCapabilityImpl.get(event.getHarvester()).getCurrentQuests();

			for ( QuestData data : quests )
			{
				try
				{
					if ( data.getiData().containsKey("block_type") )
					{
						int bt = data.getiData().get("block_type");

						for ( ItemStack drop : event.getDrops() )
						{
							if ( QuestMine.INSTANCE.isCorrectBlock(data.getPlayer(), drop.getItem(), bt) )
							{
								QuestMine.INSTANCE.perform(data, drop.getCount());
							}
						}
					}
				}
				catch (Exception e)
				{

				}
			}
		}
	}

	@SubscribeEvent
	public void grief( BreakEvent event )
	{
		if ( event.getWorld().isRemote )
		{
			return;
		}

		if ( event.getPlayer() == null || event.getPos() == null || event.getState() == null )
		{
			return;
		}

		Block block = event.getState().getBlock();

		Province province = PlayerCivilizationCapabilityImpl.get(event.getPlayer()).getInCivilization();

		if ( rand.nextInt(8000) <= ToroQuestConfiguration.artifactDropRate && (block instanceof BlockStone || block instanceof BlockGrass || block instanceof BlockDirt || block instanceof BlockOre || block instanceof BlockGravel || block instanceof BlockClay || block instanceof BlockSand) )
		{
			World world = event.getWorld();
			block.dropXpOnBlockBreak(world, event.getPos(), 10);
			ItemStack stack = randomStolenItem(world, province);
			if ( stack == null )
				return;
			EntityItem entityitem = new EntityItem(world, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), stack);
			entityitem.setDefaultPickupDelay();
			world.spawnEntity(entityitem);
		}

		if ( province == null || province.civilization == null )
		{
			return;
		}

		int rep = PlayerCivilizationCapabilityImpl.get(event.getPlayer()).getReputation(province.civilization);

		if ( rep >= 250 || (!ToroQuestConfiguration.loseReputationForBlockGrief) || event.getPlayer().isPotionActive(MobEffects.INVISIBILITY) )
		{
			return;
		}

		boolean valuable = false;
		boolean witnessed = false;
		boolean flag = false;

		if ( isBuilding(block) )
		{
			valuable = false;
			witnessed = villagersReportCrime(event.getWorld(), event.getPlayer());
		}
		else if ( isValuableBuilding(block) )
		{
			valuable = true;
			witnessed = villagersReportCrime(event.getWorld(), event.getPlayer());

			List<EntityVillageLord> villageLord = event.getWorld().getEntitiesWithinAABB(EntityVillageLord.class, new AxisAlignedBB(event.getPlayer().getPosition()).grow(16, 12, 16), new Predicate<EntityVillageLord>()
			{
				public boolean apply( @Nullable EntityVillageLord entity )
				{
					return true;
				}
			});

			if ( block instanceof BlockQuartz || block == Blocks.GOLD_BLOCK )
			{
				for ( EntityVillageLord entity : villageLord )
				{
					entity.setAnnoyed(event.getPlayer());
					entity.chat(entity, event.getPlayer(), "throne", null);
					flag = true;
					break;
				}
			}
			else
			{
				for ( EntityVillageLord entity : villageLord )
				{
					entity.setAnnoyed(event.getPlayer());
					entity.chat(entity, event.getPlayer(), "crime", null);
					flag = true;
					break;
				}
			}
		}
		else if ( event.getState().getBlock() instanceof net.minecraftforge.common.IPlantable && !(event.getState().getBlock() instanceof net.minecraftforge.common.IShearable) )
		{
			List<EntityPlayer> players = event.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(event.getPos()).grow(6, 3, 6));

			for ( EntityPlayer player : players )
			{
				/* If a villager farms a crop, cancel the player losing rep */
				List<EntityVillager> villagers = event.getWorld().getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(event.getPos()).grow(1.5, 1.5, 1.5), new Predicate<EntityVillager>()
				{
					public boolean apply( @Nullable EntityVillager entity )
					{
						return true;
					}
				});

				if ( !villagers.isEmpty() )
				{
					continue;
				}

				try
				{
					QuestFarm.INSTANCE.destroyedCrop(player);
				}
				catch (Exception e)
				{

				}

				if ( rep >= 50 || (!ToroQuestConfiguration.loseReputationForCropGrief) || player.isPotionActive(MobEffects.INVISIBILITY) )
				{
					continue;
				}

				witnessed = villagersReportCrime(event.getWorld(), player);

				List<EntityToroNpc> help = event.getWorld().getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
				{
					public boolean apply( @Nullable EntityToroNpc entity )
					{
						return true;
					}
				});

				Collections.shuffle(help);

				for ( EntityToroNpc entity : help )
				{
					if ( !entity.canEntityBeSeen(player) )
					{
						continue;
					}

					witnessed = true;
					entity.getNavigator().tryMoveToEntityLiving(player, 0.6D);

					if ( entity.isAnnoyed() )
					{
						if ( !entity.inCombat() )
						{
							entity.setAnnoyed(player);
							if ( rand.nextBoolean() )
							{
								entity.setAttackTarget(player);
							}
						}
					}
					else
					{
						entity.setAnnoyed(player);
					}

					if ( !flag && entity.actionReady() )
					{
						flag = true;
						entity.chat(entity, player, "crops", null);
					}
				}

				if ( witnessed ) reportCrimeRep(player, province, -ToroQuestConfiguration.unexpensiveRepLoss);
				
				return;
			}
		}
		else
		{
			return;
		}

		List<EntityToroNpc> help = event.getPlayer().world.getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(event.getPlayer().getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
		{
			public boolean apply( @Nullable EntityToroNpc entity )
			{
				return true;
			}
		});

		Collections.shuffle(help);

		for ( EntityToroNpc entity : help )
		{
			if ( !entity.canEntityBeSeen(event.getPlayer()) )
			{
				continue;
			}

			witnessed = true;
			entity.getNavigator().tryMoveToEntityLiving(event.getPlayer(), 0.6D);

			if ( !valuable )
			{
				if ( entity.isAnnoyed() )
				{
					if ( !entity.inCombat() )
					{
						entity.setAnnoyed(event.getPlayer());
						entity.setAttackTarget(event.getPlayer());
					}
				}
				else
				{
					entity.setAnnoyed(event.getPlayer());
				}

				if ( !flag )
				{
					flag = true;
					entity.chat(entity, event.getPlayer(), "grief", null);
				}
			}
			else
			{
				entity.setAnnoyed(event.getPlayer());
				entity.setAttackTarget(event.getPlayer());
				if ( !flag )
				{
					flag = true;
					entity.chat(entity, event.getPlayer(), "grief", null);
				}
			}
		}
		
		if ( witnessed )
		{
			if ( !valuable )
			{
				reportCrimeRep(event.getPlayer(), province, -ToroQuestConfiguration.unexpensiveRepLoss);
			}
			else
			{
				reportCrimeRep(event.getPlayer(), province, -ToroQuestConfiguration.expensiveRepLoss);
			}
		}
	}

	private boolean villagersReportCrime( World world, EntityPlayer player )
	{
		List<EntityToroVillager> villagerList = world.getEntitiesWithinAABB(EntityToroVillager.class, new AxisAlignedBB(player.getPosition()).grow(12, 12, 12), new Predicate<EntityToroVillager>()
		{
			public boolean apply( @Nullable EntityToroVillager entity )
			{
				return true;
			}
		});
		Collections.shuffle(villagerList);
		boolean flag = false;
		for ( EntityToroVillager villager : villagerList )
		{
			if ( !villager.canEntityBeSeen(player) )
			{
				continue;
			}
			if ( !flag )
			{
				villager.reportToGuards(player);
				villager.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
				flag = true;
			}
			villager.blockTrade();
		}
		return flag;
	}

	public static boolean isBuilding( Block block )
	{
		return (block instanceof BlockPlanks) || (block.getDefaultState() == Blocks.COBBLESTONE.getDefaultState()) || (block.getDefaultState() == Blocks.WOOL.getDefaultState()) || (block.getDefaultState() == Blocks.STAINED_HARDENED_CLAY.getDefaultState()) || (block.getDefaultState() == Blocks.STONEBRICK.getDefaultState()) || (block.getDefaultState() == Blocks.GLASS_PANE.getDefaultState()) || (block.getDefaultState() == Blocks.GRASS_PATH.getDefaultState()) || (block.getDefaultState() == Blocks.SANDSTONE.getDefaultState())
		// || (block.getDefaultState() == Blocks.STONE_SLAB.getDefaultState())
			|| (block.getDefaultState() == Blocks.LOG.getDefaultState()) || (block.getDefaultState() == Blocks.WOODEN_PRESSURE_PLATE.getDefaultState())
			// || (block.getDefaultState() == Blocks.CRAFTING_TABLE.getDefaultState())
			|| (block.getDefaultState() == Blocks.TORCH.getDefaultState()) || (block instanceof BlockCarpet) || (block instanceof BlockFence) || (block instanceof BlockColored) || (block instanceof BlockLog) || (block instanceof BlockFlowerPot) || (block instanceof BlockSlab) || (block instanceof BlockStairs) || (block instanceof BlockLadder) || (block instanceof BlockTrapDoor) || (block instanceof BlockDoubleStoneSlab);
		// || (block.getDefaultState() == Block.getBlockFromName(""));
	}

	public static boolean isValuableBuilding( Block block )
	{
		return (block.getDefaultState() == Blocks.BOOKSHELF.getDefaultState()) || (block.getDefaultState() == Blocks.GOLD_BLOCK.getDefaultState())
		// || (block instanceof Blocks.)
			|| (block.getDefaultState() == Blocks.TRAPPED_CHEST.getDefaultState()) || (block.getDefaultState() == Blocks.EMERALD_BLOCK.getDefaultState()) || (block instanceof BlockQuartz) || (block instanceof BlockCauldron) || (block instanceof BlockBanner) || (block.getDefaultState() == Blocks.JUKEBOX) || (block.getDefaultState() == Blocks.QUARTZ_STAIRS.getDefaultState())
			// || (block.getDefaultState() == Blocks.CHEST.getDefaultState())
			|| (block instanceof BlockDoor) || (block instanceof BlockBed) || (block.getDefaultState() == Blocks.ANVIL.getDefaultState());
	}

	public static boolean isCrop( Block block )
	{
		return block instanceof BlockCrops || block instanceof BlockStem || block.getDefaultState() == Blocks.FARMLAND.getDefaultState();
	}

	@SubscribeEvent
	public void civTimer( WorldTickEvent event )
	{
		if ( TickEvent.Phase.START.equals(event.phase) || event.world == null )
		{
			return;
		}

		if ( event.world.isRemote || ++this.spawningTicks % 200 != 0 )
		{
			return;
		}

		for ( EntityPlayer p : event.world.playerEntities )
		{
			Province province = PlayerCivilizationCapabilityImpl.get(p).getInCivilization();
			if ( province == null )
			{
				continue;
			}
			CivilizationDataAccessor worldData = CivilizationsWorldSaveData.get(p.world);
			if ( worldData == null )
			{
				continue;
			}
			int duration = PlayerCivilizationCapabilityImpl.get(p).getReputation(province.civilization) + 1;
			if ( duration <= 50 )
			{
				continue;
			}
			duration = MathHelper.clamp(PlayerCivilizationCapabilityImpl.get(p).getReputation(province.civilization), 0, 3000) * 4;
			int power = 0;
			if ( worldData.hasTrophyBeholder(province.id) )
			{
				power = 1;
			}
			if ( worldData.hasTrophyMage(province.id) )
			{
				p.addPotionEffect(new PotionEffect(MobEffects.HASTE, duration, power, true, false));
			}
			if ( worldData.hasTrophyLord(province.id) )
			{
				p.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, duration, power, true, false));
			}
			if ( worldData.hasTrophyPig(province.id) )
			{
				p.addPotionEffect(new PotionEffect(MobEffects.SATURATION, duration, power, true, false));
			}
			if ( worldData.hasTrophyBandit(province.id) )
			{
				p.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, duration, power, true, false));
			}
			if ( worldData.hasTrophySkeleton(province.id) )
			{
				p.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, duration, power, true, false));
			}
			if ( worldData.hasTrophySpider(province.id) )
			{
				p.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, duration, power, true, false));
				p.addPotionEffect(new PotionEffect(MobEffects.SPEED, duration, power, true, false));
			}
		}

		if ( this.spawningTicks < 1200 )
		{
			return;
		}

		this.spawningTicks = 0;

		int players = event.world.playerEntities.size();

		if ( players > 0 )
		{
			if ( ToroQuestConfiguration.banditSpawnRate > 0 && (rand.nextInt(100)) < ToroQuestConfiguration.banditSpawnRate + MathHelper.clamp((players - 1) * 2, 0, ToroQuestConfiguration.banditSpawnRate) )
			{
				spawnBanditsNearPlayer(event.world);
			}

			if ( ToroQuestConfiguration.caravanSpawnRate > 0 && (event.world.getWorldTime() <= 11000 || event.world.getWorldTime() >= 23000) && (rand.nextInt(100)) < ToroQuestConfiguration.caravanSpawnRate + MathHelper.clamp((players - 1) * 2, 0, ToroQuestConfiguration.caravanSpawnRate) )
			{
				spawnCaravanNearProvince(event.world);
			}

			if ( ToroQuestConfiguration.provinceSiegeRate > 0 && rand.nextInt(100) < ToroQuestConfiguration.provinceSiegeRate + MathHelper.clamp((players - 1) * 2, 0, ToroQuestConfiguration.provinceSiegeRate) )
			{
				spawnRaiders(event.world);
				if ( rand.nextInt(4) == 0 )
				{
					spawnRaiders(event.world);
				}
			}

			if ( ToroQuestConfiguration.fugitiveSpawnRate > 0 && rand.nextInt(100) < ToroQuestConfiguration.fugitiveSpawnRate + MathHelper.clamp((players - 1), 0, ToroQuestConfiguration.fugitiveSpawnRate) )
			{
				spawnFugitives(event.world);
			}
		}

		try
		{
			for ( EntityPlayer p : event.world.playerEntities )
			{
				if ( PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.EARTH) < 0 )
				{
					PlayerCivilizationCapabilityImpl.get(p).adjustReputation(CivilizationType.EARTH, (int) MathHelper.clamp(-PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.EARTH) / 30.0, 1.0, 100.0));
				}
				if ( PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.FIRE) < 0 )
				{
					PlayerCivilizationCapabilityImpl.get(p).adjustReputation(CivilizationType.FIRE, (int) MathHelper.clamp(-PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.FIRE) / 30.0, 1.0, 100.0));
				}
				if ( PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.MOON) < 0 )
				{
					PlayerCivilizationCapabilityImpl.get(p).adjustReputation(CivilizationType.MOON, (int) MathHelper.clamp(-PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.MOON) / 30.0, 1.0, 100.0));
				}
				if ( PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.SUN) < 0 )
				{
					PlayerCivilizationCapabilityImpl.get(p).adjustReputation(CivilizationType.SUN, (int) MathHelper.clamp(-PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.SUN) / 30.0, 1.0, 100.0));
				}
				if ( PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.WATER) < 0 )
				{
					PlayerCivilizationCapabilityImpl.get(p).adjustReputation(CivilizationType.WATER, (int) MathHelper.clamp(-PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.WATER) / 30.0, 1.0, 100.0));
				}
				if ( PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.WIND) < 0 )
				{
					PlayerCivilizationCapabilityImpl.get(p).adjustReputation(CivilizationType.WIND, (int) MathHelper.clamp(-PlayerCivilizationCapabilityImpl.get(p).getReputation(CivilizationType.WIND) / 30.0, 1.0, 100.0));
				}
			}
		}
		catch (Exception e)
		{

		}
	}

	public void spawnCaravanNearProvince( World world )
	{
		if ( world.isRemote )
		{
			return;
		}

		try
		{
			List<EntityPlayer> players = world.playerEntities;
			Collections.shuffle(players);
			int tries = 3;
			while (tries > 0)
			{
				tries--;

				for ( EntityPlayer player : players )
				{
					if ( player.world.provider.getDimension() != 0 )
					{
						continue;
					}

					Village village = world.getVillageCollection().getNearestVillage(player.getPosition(), 320);

					if ( village == null )
					{
						continue;
					}

					Province province = CivilizationUtil.getProvinceAt(world, village.getCenter().getX() / 16, village.getCenter().getZ() / 16);

					if ( province == null )
					{
						continue;
					}

					int x = 0;
					int z = 0;

					if ( CivilizationUtil.getProvinceAt(world, player.chunkCoordX, player.chunkCoordZ) == null )
					{
						int playerX = (int) player.posX;
						int playerZ = (int) player.posZ;
						double angle = rand.nextDouble() * Math.PI * 2.0D;
						int range = 25 + rand.nextInt(25);

						x = (int) (Math.cos(angle) * range);
						z = (int) (Math.sin(angle) * range);

						for ( int i = 0; 8 > i; i++ )
						{
							double distance = Math.abs(province.getCenterPosX() - playerX) + Math.abs(province.getCenterPosZ() - playerZ);
							if ( distance < 200 + i * 3 && distance > 140 )
							{
								break;
							}
							else
							{
								x = (int) (Math.cos(angle) * range);
								z = (int) (Math.sin(angle) * range);
							}
						}
					}
					else
					{
						double angle = rand.nextDouble() * Math.PI * 2.0D;
						int range = 150 + rand.nextInt(80);

						x = (int) (Math.cos(angle) * range);
						z = (int) (Math.sin(angle) * range);

						for ( int i = 0; 8 > i; i++ )
						{
							double distance = player.getDistance(x, player.posY, z);
							if ( distance < 50 + i * 3 && distance > 25 )
							{
								break;
							}
							else
							{
								x = (int) (Math.cos(angle) * range);
								z = (int) (Math.sin(angle) * range);
							}
						}
					}

					BlockPos loc = new BlockPos(x, MAX_SPAWN_HEIGHT, z);
					BlockPos banditSpawnPos = findSpawnLocationFrom(world, loc);

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

					int i = rand.nextInt(3) + 1;
					while (i > 0)
					{
						i--;
						EntityCaravan e = new EntityCaravan(world);
						e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
						world.spawnEntity(e);
						e.velocityChanged = true;

						if ( rand.nextBoolean() )
						{
							if ( rand.nextBoolean() )
							{
								EntityGuard g = new EntityGuard(world, province);
								g.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
								g.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD, 1));
								ItemStack istack = new ItemStack(Item.getByNameOrId("spartanshields:shield_tower_wood"));
								if ( istack != null && !istack.isEmpty() )
								{
									g.setHeldItem(EnumHand.OFF_HAND, istack);
								}
								else
								{
									g.setHeldItem(EnumHand.OFF_HAND, new ItemStack(Items.SHIELD, 1));
								}
								world.spawnEntity(g);
								g.velocityChanged = true;
								g.getNavigator().tryMoveToEntityLiving(player, 0.6);
								g.setAttackTarget(player);
								g.setAttackTarget(null);
							}
							else
							{
								e.addCaravan();
								if ( rand.nextBoolean() )
								{
									e.addCaravan();
								}
							}
						}
						e.getNavigator().tryMoveToEntityLiving(player, 0.6);
						e.setAttackTarget(player);
						e.setAttackTarget(null);
					}
					return;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("ERROR SPAWNING EntityCaravan: " + e);
			return;
		}
	}

	protected void spawnBanditsNearPlayer( World world )
	{
		if ( world.isRemote )
		{
			return;
		}

		try
		{

			List<EntityPlayer> players = world.playerEntities;
			Collections.shuffle(players);
			int tries = 3;
			while (tries > 0)
			{
				tries--;

				for ( EntityPlayer player : players )
				{
					if ( player.world.provider.getDimension() != 0 )
					{
						continue;
					}

					int playerPosX = (int) player.posX;
					int playerPosZ = (int) player.posZ;

					if ( CivilizationUtil.getProvinceAt(world, playerPosX / 16, playerPosZ / 16) != null )
					{
						continue;
					}

					int range = 40 + rand.nextInt(20);

					// boolean raiders = rand.nextBoolean();
					//
					// if ( !raiders )
					// {
					// range = 40+rand.nextInt(8);
					// }

					double angle = rand.nextDouble() * Math.PI * 2.0D;

					int x = (int) (Math.cos(angle) * range);
					int z = (int) (Math.sin(angle) * range);

					x += playerPosX;
					z += playerPosZ;

					BlockPos banditSpawnPos = findSpawnLocationFrom(world, new BlockPos(x, MAX_SPAWN_HEIGHT, z));

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

					// float difficulty =
					// world.getDifficultyForLocation(banditSpawnPos).getAdditionalDifficulty();

					int amountToSpawn = rand.nextInt((int) MathHelper.clamp(MathHelper.sqrt(player.experienceLevel), 1, 7)) + 1;

					if ( ToroQuestConfiguration.orcsAreNeutral || rand.nextBoolean() )
					{
						boolean cavalry = rand.nextInt(100) < ToroQuestConfiguration.banditMountChance;

						for ( int i = amountToSpawn; i > 0; i-- )
						{
							if ( cavalry && world.canSeeSky(banditSpawnPos) )
							{
								EntitySentry e = new EntitySentry(world);
								e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
								// ForgeChunkManager.forceChunk(//ForgeChunkManager.requestTicket(ToroQuest.INSTANCE,
								// world, Type.ENTITY), new ChunkPos(e.chunkCoordX, e.chunkCoordZ));
								world.spawnEntity(e);

								e.despawnTick();
								e.velocityChanged = true;
								e.setAttackTarget(player);
								e.setMount();
								e.writeEntityToNBT(new NBTTagCompound());
							}
							else
							{
								EntitySentry e = new EntitySentry(world, playerPosX * 2 - banditSpawnPos.getX(), playerPosZ * 2 - banditSpawnPos.getZ());
								e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
								// ForgeChunkManager.forceChunk(//ForgeChunkManager.requestTicket(ToroQuest.INSTANCE,
								// world, Type.ENTITY), new ChunkPos(e.chunkCoordX, e.chunkCoordZ));
								world.spawnEntity(e);

								e.despawnTick();
								e.velocityChanged = true;
								e.setAttackTarget(player);
								e.writeEntityToNBT(new NBTTagCompound());
							}
						}
					}
					else
					{
						boolean cavalry = rand.nextInt(100) < ToroQuestConfiguration.orcMountChance;

						for ( int i = amountToSpawn; i > 0; i-- )
						{
							if ( cavalry && world.canSeeSky(banditSpawnPos) )
							{
								EntityOrc e = new EntityOrc(world);
								e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
								// ForgeChunkManager.forceChunk(//ForgeChunkManager.requestTicket(ToroQuest.INSTANCE,
								// world, Type.ENTITY), new ChunkPos(e.chunkCoordX, e.chunkCoordZ));
								world.spawnEntity(e);

								e.despawnTick();
								e.velocityChanged = true;
								e.setAttackTarget(player);
								e.setMount();
							}
							else
							{
								EntityOrc e = new EntityOrc(world, playerPosX * 2 - banditSpawnPos.getX(), playerPosZ * 2 - banditSpawnPos.getZ());
								e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
								// ForgeChunkManager.forceChunk(//ForgeChunkManager.requestTicket(ToroQuest.INSTANCE,
								// world, Type.ENTITY), new ChunkPos(e.chunkCoordX, e.chunkCoordZ));
								world.spawnEntity(e);

								e.despawnTick();
								e.velocityChanged = true;
								e.setAttackTarget(player);
							}
						}
					}
					return;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("ERROR SPAWNING EntityBandit: " + e);
			return;
		}
	}

	private int getRandomRaiderDistance()
	{
		// 208 is province distance
		return rand.nextInt(105) - 52;
	}

	protected void spawnRaiders( World world )
	{
		if ( world.isRemote )
		{
			return;
		}

		try
		{
			List<EntityPlayer> players = world.playerEntities;
			Collections.shuffle(players);
			int tries = 3;
			while (tries > 0)
			{
				tries--;

				for ( EntityPlayer player : players )
				{
					if ( player.world.provider.getDimension() != 0 )
					{
						continue;
					}

					Province province = CivilizationUtil.getProvinceAt(world, player.chunkCoordX, player.chunkCoordZ);

					if ( province == null )
					{
						continue;
					}

					int xdif = player.getPosition().getX() - province.getCenterPosX();
					int zdif = player.getPosition().getZ() - province.getCenterPosZ();

					// System.out.println("x --- " + province.getCenterPosX());
					// System.out.println("z --- " + province.getCenterPosZ());

					int x = province.getCenterPosX();
					int z = province.getCenterPosZ();

					// _______
					// | | |
					// |_2_|_1_|
					// | | |
					// |_3_|_4_|

					if ( xdif > 0 )
					{
						if ( zdif > 0 )
						{
							// QUAD 1
							if ( rand.nextBoolean() )
							{
								x -= getRandomRaiderDistance();
								z -= RAIDER_DISTANCE;
							}
							else
							{
								x -= RAIDER_DISTANCE;
								z -= getRandomRaiderDistance();
							}
						}
						else
						{
							// QUAD 4
							if ( rand.nextBoolean() )
							{
								x -= RAIDER_DISTANCE;
								z += getRandomRaiderDistance();
							}
							else
							{
								x -= getRandomRaiderDistance();
								z += RAIDER_DISTANCE;
							}
						}
					}
					else
					{
						if ( zdif > 0 )
						{
							// QUAD 2
							if ( rand.nextBoolean() )
							{
								x += getRandomRaiderDistance();
								z -= RAIDER_DISTANCE;
							}
							else
							{
								x += RAIDER_DISTANCE;
								z -= getRandomRaiderDistance();
							}
						}
						else
						{
							// QUAD 3
							if ( rand.nextBoolean() )
							{
								x += getRandomRaiderDistance();
								z += RAIDER_DISTANCE;
							}
							else
							{
								x += RAIDER_DISTANCE;
								z += getRandomRaiderDistance();
							}
						}
					}

					// System.out.println("x" + x);
					// System.out.println("z" + z);

					BlockPos loc = new BlockPos(x, MAX_SPAWN_HEIGHT, z);

					// System.out.println("loc" + loc);

					BlockPos banditSpawnPos = findSpawnLocationFrom(world, loc);

					// System.out.println("bsp" + banditSpawnPos);

					if ( banditSpawnPos == null )
					{
						continue;
					}

					if ( !(world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(banditSpawnPos).grow(25, 10, 25))).isEmpty() )
					{
						continue;
					}

					int rep = Math.abs(PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization));

					boolean isNightTime = world.getWorldTime() >= 13000 && world.getWorldTime() <= 21000;

					switch( province.getCiv() )
					{
					case FIRE:
					{
						if ( rand.nextInt(100) < ToroQuestConfiguration.configRaiderSiegeChance )
						{
							if ( !spawnRaider(ToroQuestConfiguration.raiderList_RED_BRIAR, rep, banditSpawnPos, province, player, world, isNightTime) )
							{
								// WOLVES
								spawnWolves(world, banditSpawnPos, rep, player, province);
							}
						}
						else if ( isNightTime && rand.nextBoolean() )
						{
							// ZOMBIE RAIDERS
							spawnZombies(world, banditSpawnPos, rep, player, province);
						}
						else
						{
							// BANDITS
							spawnBandits(world, banditSpawnPos, province, player);
						}
						return;
					}
					case EARTH:
					{
						if ( rand.nextInt(100) < ToroQuestConfiguration.configRaiderSiegeChance )
						{
							if ( !spawnRaider(ToroQuestConfiguration.raiderList_GREEN_WILD, rep, banditSpawnPos, province, player, world, isNightTime) )
							{
								// WOLVES
								spawnWolves(world, banditSpawnPos, rep, player, province);
							}
						}
						else if ( isNightTime && rand.nextBoolean() )
						{
							// ZOMBIE RAIDERS
							spawnZombies(world, banditSpawnPos, rep, player, province);
						}
						else
						{
							// BANDITS
							spawnBandits(world, banditSpawnPos, province, player);
						}
						return;
					}
					case MOON:
					{
						if ( rand.nextInt(100) < ToroQuestConfiguration.configRaiderSiegeChance )
						{
							if ( !spawnRaider(ToroQuestConfiguration.raiderList_BLACK_MOOR, rep, banditSpawnPos, province, player, world, isNightTime) )
							{
								// WITCHES
								spawnWitches(world, banditSpawnPos, rep, player, province);
							}
						}
						else if ( isNightTime && rand.nextBoolean() )
						{
							// ZOMBIE RAIDERS
							spawnZombies(world, banditSpawnPos, rep, player, province);
						}
						else
						{
							// BANDITS
							spawnBandits(world, banditSpawnPos, province, player);
						}
						return;
					}
					case SUN:
					{
						if ( rand.nextInt(100) < ToroQuestConfiguration.configRaiderSiegeChance )
						{
							if ( !spawnRaider(ToroQuestConfiguration.raiderList_YELLOW_DAWN, rep, banditSpawnPos, province, player, world, isNightTime) )
							{
								// HUSKS
								spawnHusks(world, banditSpawnPos, rep, player, province);
							}
						}
						else if ( rand.nextBoolean() )
						{
							// HUSKS
							spawnHusks(world, banditSpawnPos, rep, player, province);
						}
						else
						{
							// BANDITS
							spawnBandits(world, banditSpawnPos, province, player);
						}
						return;
					}
					case WIND:
					{
						if ( rand.nextInt(100) < ToroQuestConfiguration.configRaiderSiegeChance )
						{
							if ( !spawnRaider(ToroQuestConfiguration.raiderList_BROWN_MITHRIL, rep, banditSpawnPos, province, player, world, isNightTime) )
							{
								// WOLVES
								spawnWolves(world, banditSpawnPos, rep, player, province);
							}
						}
						else if ( isNightTime && rand.nextBoolean() )
						{
							// ZOMBIE RAIDERS
							spawnZombies(world, banditSpawnPos, rep, player, province);
						}
						else
						{
							// BANDITS
							spawnBandits(world, banditSpawnPos, province, player);
						}
						return;
					}
					case WATER:
					{
						if ( rand.nextInt(100) < ToroQuestConfiguration.configRaiderSiegeChance )
						{
							if ( !spawnRaider(ToroQuestConfiguration.raiderList_BLUE_GLACIER, rep, banditSpawnPos, province, player, world, isNightTime) )
							{
								// WOLVES
								spawnWolves(world, banditSpawnPos, rep, player, province);
							}
						}
						else if ( isNightTime && rand.nextBoolean() )
						{
							// ZOMBIE RAIDERS
							spawnZombies(world, banditSpawnPos, rep, player, province);
						}
						else
						{
							// BANDITS
							spawnBandits(world, banditSpawnPos, province, player);
						}
						return;
					}
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("TOROQUEST ERROR SPAWNING: " + e);
			return;
		}
	}

	public void spawnBandits( World world, BlockPos banditSpawnPos, Province province, EntityPlayer player )
	{
		if ( world.getEntitiesWithinAABB(EntityToroMob.class, new AxisAlignedBB(banditSpawnPos).grow(32, 16, 32)).size() > 3 )
		{
			return;
		}

		int rep = Math.abs(PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization));
		int count = (rand.nextInt(MathHelper.clamp(((rep + 700) / 200), 3, 9)) + 1);

		if ( ToroQuestConfiguration.orcsAreNeutral || rand.nextBoolean() )
		{
			for ( int i = count; i > 0; i-- )
			{
				EntitySentry e = new EntitySentry(world, (province.getCenterPosX() + player.getPosition().getX()) / 2, (province.getCenterPosZ() + player.getPosition().getZ()) / 2);

				e.despawnTick();
				e.velocityChanged = true;
				e.setAttackTarget(player);

				e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
				world.spawnEntity(e);
				e.addedToChunk = true;

				e.writeEntityToNBT(new NBTTagCompound());
			}
		}
		else
		{
			for ( int i = count; i > 0; i-- )
			{
				EntityOrc e = new EntityOrc(world, (province.getCenterPosX() + player.getPosition().getX()) / 2, (province.getCenterPosZ() + player.getPosition().getZ()) / 2);

				e.despawnTick();
				e.velocityChanged = true;
				e.setAttackTarget(player);

				e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
				world.spawnEntity(e);
				e.addedToChunk = true;

				e.writeEntityToNBT(new NBTTagCompound());
			}
		}
	}

	public void spawnZombies( World world, BlockPos banditSpawnPos, int rep, EntityPlayer player, Province province )
	{
		for ( int i = (rand.nextInt(MathHelper.clamp(((rep + 350) / 100), 4, 8)) + 5); i > 0; i-- )
		{
			if ( ToroQuestConfiguration.zombieRaiderVillagerChance > rand.nextInt(100) )
			{
				EntityZombieVillagerRaider e = new EntityZombieVillagerRaider(world, (province.getCenterPosX() + player.getPosition().getX()) / 2, (province.getCenterPosZ() + player.getPosition().getZ()) / 2);

				e.velocityChanged = true;
				e.setAttackTarget(player);

				e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
				// e.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(e)),
				// (IEntityLivingData)null);
				e.addedToChunk = true;
				world.spawnEntity(e);
			}
			else
			{
				EntityZombieRaider e = new EntityZombieRaider(world, (province.getCenterPosX() + player.getPosition().getX()) / 2, (province.getCenterPosZ() + player.getPosition().getZ()) / 2);

				e.velocityChanged = true;
				e.setAttackTarget(player);

				e.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
				// e.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(e)),
				// (IEntityLivingData)null);
				e.addedToChunk = true;
				world.spawnEntity(e);
			}
		}
	}

	public void spawnHusks( World w, BlockPos bsp, int rep, EntityPlayer p, Province province )
	{
		for ( int i = (rand.nextInt(MathHelper.clamp(((rep + 350) / 100), 4, 8)) + 5); i > 0; i-- )
		{
			EntityHusk creature = new EntityHusk(w);

			creature.tasks.addTask(3, new EntityAIRaidDespawn(creature, (province.getCenterPosX() + p.getPosition().getX()) / 2, (province.getCenterPosZ() + p.getPosition().getZ()) / 2, 1.0F));
			creature.enablePersistence();

			creature.getEntityData().setInteger("despawnTimer", 100);
			creature.getEntityData().setFloat("raidSpeed", 1.0F);
			creature.getEntityData().setInteger("raidX", province.getCenterPosX() + rand.nextInt(33) - 16);
			creature.getEntityData().setInteger("raidZ", province.getCenterPosZ() + rand.nextInt(33) - 16);
			creature.velocityChanged = true;
			creature.setAttackTarget(p);

			creature.setPosition(bsp.getX() + 0.5, bsp.getY() + 0.1, bsp.getZ() + 0.5);
			w.spawnEntity(creature);
			creature.addedToChunk = true;

			creature.writeEntityToNBT(new NBTTagCompound());
		}
	}

	public void spawnWitches( World w, BlockPos bsp, int rep, EntityPlayer p, Province province )
	{
		for ( int i = (rand.nextInt(MathHelper.clamp(((Math.abs(rep) + 500) / 200), 2, 6)) + 3); i > 0; i-- )
		{
			EntityWitch creature = new EntityWitch(w);

			creature.tasks.addTask(3, new EntityAIRaidDespawn(creature, (province.getCenterPosX() + p.getPosition().getX()) / 2, (province.getCenterPosZ() + p.getPosition().getZ()) / 2, 1.0F));
			creature.enablePersistence();

			creature.getEntityData().setInteger("despawnTimer", 100);
			creature.getEntityData().setFloat("raidSpeed", 1.0F);
			creature.getEntityData().setInteger("raidX", province.getCenterPosX() + rand.nextInt(33) - 16);
			creature.getEntityData().setInteger("raidZ", province.getCenterPosZ() + rand.nextInt(33) - 16);
			creature.velocityChanged = true;
			creature.setAttackTarget(p);

			creature.setPosition(bsp.getX() + 0.5, bsp.getY() + 0.1, bsp.getZ() + 0.5);
			w.spawnEntity(creature);
			creature.addedToChunk = true;

			creature.writeEntityToNBT(new NBTTagCompound());
		}
	}

	public void spawnWolves( World w, BlockPos banditSpawnPos, int rep, EntityPlayer player, Province province )
	{
		for ( int i = (rand.nextInt(MathHelper.clamp(((Math.abs(rep) + 500) / 200), 2, 9)) + 3); i > 0; i-- )
		{
			EntityWolfRaider creature = new EntityWolfRaider(w, (province.getCenterPosX() + player.getPosition().getX()) / 2, (province.getCenterPosZ() + player.getPosition().getZ()) / 2);

			creature.getEntityData().setInteger("despawnTimer", 100);
			creature.getEntityData().setFloat("raidSpeed", 1.0F);
			creature.getEntityData().setInteger("raidX", province.getCenterPosX() + rand.nextInt(33) - 16);
			creature.getEntityData().setInteger("raidZ", province.getCenterPosZ() + rand.nextInt(33) - 16);
			creature.velocityChanged = true;
			creature.setAttackTarget(player);

			creature.setPosition(banditSpawnPos.getX() + 0.5, banditSpawnPos.getY() + 0.1, banditSpawnPos.getZ() + 0.5);
			w.spawnEntity(creature);
			creature.addedToChunk = true;
		}
	}

	public boolean spawnRaider( ArrayList<Raider> r, int rep, BlockPos bsp, Province province, EntityPlayer p, World w, boolean isNightTime )
	{
		ArrayList<Raider> raiderList = r;
		Collections.shuffle(raiderList);

		for ( Raider raider : raiderList )
		{
			if ( raider.minReputationRequired <= rep && ((isNightTime && !raider.timeType.equals("day")) || (!isNightTime && !raider.timeType.equals("night"))) )
			{
				int randomAmount = Math.abs(rep - raider.minReputationRequired) / 100 + raider.maxSpawn - raider.minSpawn;
				int count = MathHelper.clamp((randomAmount > 0 ? rand.nextInt(randomAmount) : 0) + raider.minSpawn, raider.minSpawn, raider.maxSpawn);

				for ( int i = count; i > 0; i-- )
				{
					String className = raider.entityResourceName;
					try
					{
						EntityCreature creature = (EntityCreature) Class.forName(className).getConstructor(new Class[]
						{
							World.class
						}).newInstance(new Object[]
						{
							w
						});

						creature.tasks.addTask(3, new EntityAIRaidDespawn(creature, (province.getCenterPosX() + p.getPosition().getX()) / 2, (province.getCenterPosZ() + p.getPosition().getZ()) / 2, 1.0F));
						creature.enablePersistence();

						creature.getEntityData().setInteger("despawnTimer", 100);
						creature.getEntityData().setFloat("raidSpeed", 1.0F);
						creature.getEntityData().setInteger("raidX", province.getCenterPosX() + rand.nextInt(33) - 16);
						creature.getEntityData().setInteger("raidZ", province.getCenterPosZ() + rand.nextInt(33) - 16);
						creature.velocityChanged = true;
						creature.setAttackTarget(p);

						creature.setPosition(bsp.getX() + 0.5, bsp.getY() + 0.1, bsp.getZ() + 0.5);
						w.spawnEntity(creature);
						creature.addedToChunk = true;

						creature.writeEntityToNBT(new NBTTagCompound());
					}
					catch (Exception error)
					{
						System.err.println("Incorrect raider resource name: " + className);
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onGuiOpen( RightClickBlock event )
	{
		Vec3d vec = event.getHitVec();

		if ( vec == null )
		{
			return;
		}

		BlockPos pos = new BlockPos(vec.x, vec.y, vec.z);

		Block block = event.getWorld().getBlockState(pos).getBlock();

		if ( block == null )
		{
			return;
		}

		if ( !(block == Blocks.TRAPPED_CHEST) )
		{
			return;
		}

		EntityPlayer player = event.getEntityPlayer();

		if ( player == null )
		{
			return;
		}

		Province province = CivilizationUtil.getProvinceAt(event.getWorld(), player.chunkCoordX, player.chunkCoordZ);

		if ( province == null )
		{
			return;
		}

		int rep = PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization);

		if ( rep >= 250 || (!ToroQuestConfiguration.loseReputationForBlockGrief) || player.isPotionActive(MobEffects.INVISIBILITY) )
		{
			return;
		}

		boolean witnessed = villagersReportCrime(event.getWorld(), player);

		List<EntityToroNpc> help = player.world.getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
		{
			public boolean apply( @Nullable EntityToroNpc entity )
			{
				return true;
			}
		});
		Collections.shuffle(help);
		boolean flag = false;
		for ( EntityToroNpc entity : help )
		{
			if ( !entity.canEntityBeSeen(player) )
			{
				continue;
			}

			witnessed = true;
			entity.getNavigator().tryMoveToEntityLiving(player, 0.6D);

			entity.setAnnoyed(player);
			entity.setAttackTarget(player);

			if ( !flag )
			{
				flag = true;
				entity.chat(entity, player, "steal", null);
			}
		}
		if ( witnessed )
			reportCrimeRep(player, province, -ToroQuestConfiguration.expensiveRepLoss);
	}

	public static int randomSpawnDistance( int num )
	{
		Random rand = new Random();
		int result = rand.nextInt(num / 2) + num;
		if ( rand.nextBoolean() )
		{
			result = -result;
		}
		return result;
	}

	private void spawnFugitives( World world )
	{
		if ( world.isRemote )
		{
			return;
		}

		try
		{
			List<EntityPlayer> players = world.playerEntities;
			Collections.shuffle(players);
			int tries = 3;
			while (tries > 0)
			{
				tries--;

				for ( EntityPlayer player : players )
				{
					if ( world.provider.getDimension() != 0 )
					{
						continue;
					}

					Province province = CivilizationUtil.getProvinceAt(world, player.chunkCoordX, player.chunkCoordZ);

					if ( province == null )
					{
						continue;
					}

					int villageCenterX = province.getCenterPosX();
					int villageCenterZ = province.getCenterPosZ();

					double angle = rand.nextDouble() * Math.PI * 2.0D;

					int range = rand.nextInt(64);

					int x = (int) (Math.cos(angle) * range);
					int z = (int) (Math.sin(angle) * range);

					x += villageCenterX;
					z += villageCenterZ;

					BlockPos loc = new BlockPos(x, MAX_SPAWN_HEIGHT, z);
					BlockPos spawnPos = findSpawnLocationFrom(world, loc);

					if ( spawnPos == null )
					{
						continue;
					}

					if ( !(world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(spawnPos).grow(20, 10, 20))).isEmpty() )
					{
						continue;
					}

					int localFugitiveCount = world.getEntitiesWithinAABB(EntityFugitive.class, new AxisAlignedBB(spawnPos).grow(90, 45, 90)).size();

					if ( localFugitiveCount > 3 )
					{
						continue;
					}

					if ( localFugitiveCount == 3 )
					{
						if ( rand.nextBoolean() )
						{
							this.spawnFugitive(world, spawnPos, player);
						}
						continue;
					}
					else if ( localFugitiveCount < 1 )
					{
						this.spawnFugitive(world, spawnPos, player);
					}

					this.spawnFugitive(world, spawnPos, player);

					return;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("ERROR SPAWNING EntityFugitive: " + e);
			return;
		}
	}

	public void spawnFugitive( World world, BlockPos spawnPos, EntityPlayer player )
	{
		EntityFugitive e = new EntityFugitive(world);
		e.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
		world.spawnEntity(e);
		e.velocityChanged = true;
		e.setAttackTarget(player);
	}

	public static BlockPos findSpawnLocationFrom( World world, BlockPos spawnPos )
	{
		boolean[] airSpace =
		{
			false, false
		};
		IBlockState blockState;
		for ( int j = 0; j <= 6; j++ )
		{
			for ( int i = 0; i <= SPAWN_RANGE; i++ )
			{
				blockState = world.getBlockState(spawnPos);

				if ( isAir(blockState) )
				{
					if ( airSpace[0] )
					{
						airSpace[1] = true;
					}
					else
					{
						airSpace[0] = true;
					}
				}
				else if ( isStructureBlock(blockState) || isLiquid(blockState) )
				{
					break;
				}
				else if ( isGroundBlock(blockState) )
				{
					if ( airSpace[0] && airSpace[1] )
					{
						return spawnPos.up();
					}
					else
					{
						airSpace[0] = false;
						airSpace[1] = false;
					}
				}
				else
				{
					airSpace[0] = false;
					airSpace[1] = false;
				}
				spawnPos = spawnPos.down();
			}
			spawnPos = spawnPos.add(j * 2 * (rand.nextBoolean() ? 1 : -1), 0, j * 2 * (rand.nextBoolean() ? 1 : -1));
		}
		return null;
	}

	public static BlockPos findTeleportLocationFrom( World world, BlockPos pos )
	{
		boolean[] airSpace =
		{
			false, false
		};
		IBlockState blockState;
		BlockPos spawnPos;
		for ( int j = 0; j < 16; j++ )
		{
			spawnPos = new BlockPos(pos.getX() + rand.nextInt(32) + j * 2 * (rand.nextBoolean() ? 1 : -1), MAX_SPAWN_HEIGHT, pos.getZ() + rand.nextInt(32) + j * 2 * (rand.nextBoolean() ? 1 : -1));

			for ( int i = 0; i < SPAWN_RANGE; i++ )
			{
				blockState = world.getBlockState(spawnPos);

				if ( isAir(blockState) )
				{
					if ( airSpace[0] )
					{
						airSpace[1] = true;
					}
					else
					{
						airSpace[0] = true;
					}
				}
				else if ( isStructureBlock(blockState) || isLiquid(blockState) )
				{
					break;
				}
				else if ( isGroundBlock(blockState) )
				{
					if ( airSpace[0] && airSpace[1] )
					{
						return spawnPos.up();
					}
					else
					{
						airSpace[0] = false;
						airSpace[1] = false;
					}
				}
				else
				{
					airSpace[0] = false;
					airSpace[1] = false;
				}
				spawnPos = spawnPos.down();
			}
		}
		return null;
	}

	public static BlockPos findSpawnSurface( World world, BlockPos pos )
	{
		pos = pos.up(64);
		IBlockState blockState;
		int yOffset = 64;
		boolean[] airSpace =
		{
			false, false
		};

		while (yOffset > 0)
		{
			blockState = world.getBlockState(pos);

			if ( isAir(blockState) )
			{
				if ( airSpace[0] )
				{
					airSpace[1] = true;
				}
				else
				{
					airSpace[0] = true;
				}
			}
			else if ( isLiquid(blockState) )
			{
				break;
			}
			else if ( !(blockState.getBlock() instanceof BlockAir) )
			{
				if ( airSpace[0] && airSpace[1] )
				{
					return pos.up();
				}
				else
				{
					airSpace[0] = false;
					airSpace[1] = false;
				}
			}
			else
			{
				airSpace[0] = false;
				airSpace[1] = false;
			}
			pos = pos.down();
			yOffset--;
		}
		return null;
	}

	protected static boolean isLiquid( IBlockState blockState )
	{
		return blockState.getBlock() instanceof BlockLiquid;
	}

	public static boolean isGroundBlock( IBlockState blockState )
	{
		if ( blockState.getBlock() instanceof BlockLeaves || blockState.getBlock() instanceof BlockLog || blockState.getBlock() instanceof BlockBush )
		{
			return false;
		}
		return blockState.isOpaqueCube();
	}

	public static boolean isAir( IBlockState blockState )
	{
		return blockState.getBlock() == Blocks.AIR;
	}

	public static boolean isStructureBlock( IBlockState blockState )
	{
		if ( !blockState.getBlock().getDefaultState().isFullCube() || blockState.getBlock() instanceof BlockLeaves || blockState.getBlock().getDefaultState() == Blocks.WOOL.getDefaultState() || blockState.getBlock().getDefaultState() == Blocks.CONCRETE.getDefaultState() || blockState.getBlock().getDefaultState() == Blocks.STONEBRICK.getDefaultState() || blockState.getBlock() instanceof BlockPlanks || blockState.getBlock() instanceof BlockFire )
		{
			return true;
		}
		return false;
	}

	public static ItemStack randomStolenItem( World world, Province province )
	{
		if ( world == null )
		{
			return null;
		}

		// ItemStack stolenItem = new
		// ItemStack(STOLEN_ITEMS[rand.nextInt(STOLEN_ITEMS.length)]);

		CivilizationType civ = null;

		if ( province == null )
		{
			province = QuestBase.chooseRandomProvince(null, world, false);

			if ( province != null )
			{
				civ = province.civilization;
			}
			else
			{
				return null;
			}
		}
		else
		{
			civ = province.civilization;
		}

		if ( civ == null )
		{
			return null;
		}

		ItemStack stolenItem = null;

		switch( civ )
		{
		case FIRE:
		{
			stolenItem = new ItemStack(Item.getByNameOrId("toroquest:artifact_red"));
			break;
		}
		case MOON:
		{
			stolenItem = new ItemStack(Item.getByNameOrId("toroquest:artifact_black"));
			break;
		}
		case EARTH:
		{
			stolenItem = new ItemStack(Item.getByNameOrId("toroquest:artifact_green"));
			break;
		}
		case WATER:
		{
			stolenItem = new ItemStack(Item.getByNameOrId("toroquest:artifact_blue"));
			break;
		}
		case WIND:
		{
			stolenItem = new ItemStack(Item.getByNameOrId("toroquest:artifact_brown"));
			break;
		}
		case SUN:
		{
			stolenItem = new ItemStack(Item.getByNameOrId("toroquest:artifact_yellow"));
			break;
		}
		default:
		{
			return null;
		}
		}

		if ( stolenItem.getItem() == null )
		{
			return null;
		}

		if ( !stolenItem.hasTagCompound() )
		{
			stolenItem.setTagCompound(new NBTTagCompound());
		}

		stolenItem.getTagCompound().setString("civilizationName", civ.name());
		stolenItem.getTagCompound().setBoolean("isStolen", true);
		return stolenItem;
	}

	@SubscribeEvent( priority = EventPriority.NORMAL, receiveCanceled = true ) // XXX
	public void onEntitySpawn( EntityJoinWorldEvent event )
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
				catch (Exception e)
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

				Province province = CivilizationUtil.getProvinceAt(creature.getEntityWorld(), creaturePosX / 16, creaturePosZ / 16);

				// =-=-=-=-=-=-=-=-= province =-=-=-=-=-=-=-=-=-=
				if ( province != null )
				{
					if ( creature instanceof EntityCreeper || creature instanceof EntityEnderman )
					{
						creature.setDead();
						event.setCanceled(true);
						return;
					}

					int villageCenterX = province.getCenterPosX();
					int villageCenterZ = province.getCenterPosZ();
					/*
					 * Village length is equal to 208.
					 */
					if ( !creature.isNoDespawnRequired() )
					{
						int allowedDistance = (creature instanceof EntityZombie) ? ToroQuestConfiguration.disableZombieSpawningNearVillage : ToroQuestConfiguration.disableMobSpawningNearVillage;

						if ( (creature.getPosition().getY() >= 40 && Math.abs(villageCenterX - creaturePosX) < allowedDistance && Math.abs(villageCenterZ - creaturePosZ) < allowedDistance) )
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
						if ( ToroQuestConfiguration.zombieAttackVillageChance > 0 && ToroQuestConfiguration.zombieAttackVillageChance > rand.nextInt(100) )
						{
							if ( province == null )
							{
								province = CivilizationUtil.getProvinceAt(world, creaturePosX / 16 + 2, creaturePosZ / 16 + 2);

								if ( province == null )
								{
									province = CivilizationUtil.getProvinceAt(world, creaturePosX / 16 + 2, creaturePosZ / 16 - 2);

									if ( province == null )
									{
										province = CivilizationUtil.getProvinceAt(world, creaturePosX / 16 - 2, creaturePosZ / 16 + 2);

										if ( province == null )
										{
											province = CivilizationUtil.getProvinceAt(world, creaturePosX / 16 - 2, creaturePosZ / 16 - 2);
										}
									}
								}
							}
							if ( province != null )
							{
								if ( creature instanceof EntityZombieVillager || (ToroQuestConfiguration.zombieRaiderVillagerChance > 0 && ToroQuestConfiguration.zombieRaiderVillagerChance > rand.nextInt(100)) )
								{
									if ( !world.isRemote )
									{
										EntityZombieVillagerRaider zombie = new EntityZombieVillagerRaider(world, province.getCenterPosX(), province.getCenterPosZ());
										BlockPos pos = creature.getPosition();
										zombie.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
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
										EntityZombieRaider zombie = new EntityZombieRaider(world, province.getCenterPosX(), province.getCenterPosZ());
										BlockPos pos = creature.getPosition();
										zombie.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
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
						Province province = CivilizationUtil.getProvinceAt(creature.getEntityWorld(), creature.getPosition().getX() / 16, creature.getPosition().getZ() / 16);

						if ( province == null )
						{
							return;
						}
					}

					EntityVillager villager = (EntityVillager) creature;

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
							if ( world.rand.nextInt(100) < ToroQuestConfiguration.shopKeeperSpawnChance )
							{
								EntityShopkeeper newEntity = new EntityShopkeeper(world);
								BlockPos pos = creature.getPosition();
								newEntity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
								newEntity.setGrowingAge(villager.getGrowingAge());
								world.spawnEntity(newEntity);
							}
							else
							{
								@SuppressWarnings( "deprecation" )
								EntityToroVillager newEntity = new EntityToroVillager(world, villager.getProfession());
								BlockPos pos = creature.getPosition();
								newEntity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
								if ( world.rand.nextInt(3) == 0 )
								{
									newEntity.setGrowingAge(-10000 * (world.rand.nextInt(6) + 1));
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
			else if ( event.getEntity() instanceof EntityIronGolem && !(event.getEntity() instanceof IMob) ) // creature.getClass().equals(EntityIronGolem.class)
																											 // )
			{
				for ( String tag : event.getEntity().getTags() )
				{
					if ( tag.equals(INITIAL_SPAWN_TAG) )
					{
						return;
					}
				}

				event.getEntity().addTag(INITIAL_SPAWN_TAG);

				Province province = CivilizationUtil.getProvinceAt(creature.getEntityWorld(), event.getEntity().getPosition().getX() / 16, event.getEntity().getPosition().getZ() / 16);

				if ( province != null )
				{
					List<EntityPlayer> players = event.getEntity().world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(event.getEntity().getPosition()).grow(5, 5, 5), new Predicate<EntityPlayer>()
					{
						public boolean apply( @Nullable EntityPlayer entity )
						{
							return true;
						}
					});

					for ( EntityPlayer player : players )
					{
						EventHandlers.adjustPlayerRep(player, province.getCiv(), ToroQuestConfiguration.recruitGuardRepGain);

						player.sendStatusMessage(new TextComponentString("Golem Constructed!"), true);
						event.getEntity().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

						try
						{
							QuestRecruit.INSTANCE.onRecruit(player);
						}
						catch (Exception e)
						{

						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void handleWorldTick( ClientTickEvent event )
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
	public void potionAdded( PotionAddedEvent event ) // TODO
	{
		if ( event.getEntityLiving() instanceof EntityWitch )
		{
			EntityWitch w0 = (EntityWitch) event.getEntityLiving();
			event.getEntityLiving().clearActivePotions();
			if ( w0.getAttackTarget() instanceof EntityWitch )
			{
				EntityWitch w1 = (EntityWitch) w0.getAttackTarget();
				w0.setAttackTarget(null);
				w1.setAttackTarget(null);
			}
		}
	}
	//
	// event.
	// System.out.println(event.getPotionEffect().getPotion());
	//
	// if ( event.getEntityLiving() instanceof EntitySentry )
	// {
	// event.
	// System.out.println(potion);
	//
	// if ( potion.getThrower() instanceof EntitySentry )
	// {
	// System.out.println(potion.getThrower());
	// event.setCanceled(true);
	// return;
	// }
	// }
	// }

	@SubscribeEvent
	public void crystalProjectileImpact( ProjectileImpactEvent event )
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

			// Entity entity = event.getRayTraceResult().entityHit;
			//
			// if ( entity == null )
			// {
			//
			// }

			EntityPotion p = (EntityPotion) event.getEntity();

			BlockPos pos = event.getRayTraceResult().getBlockPos();

			if ( pos == null )
			{
				pos = event.getRayTraceResult().entityHit.getPosition();
			}

			if ( pos != null )
			{
				if ( event.getEntity() instanceof EntitySmartArrow )
				{
					if ( event.getEntity().getEntityWorld().getBlockState(pos).getBlock() instanceof BlockFence || event.getEntity().getEntityWorld().getBlockState(pos).getBlock() instanceof BlockTrapDoor )
					{
						event.setCanceled(true);
					}
				}
				else if ( p.getThrower() instanceof EntitySentry )
				{
					AxisAlignedBB axisalignedbb = new AxisAlignedBB(pos).grow(4.0D, 2.0D, 4.0D);
					List<EntitySentry> list = event.getEntity().world.<EntitySentry>getEntitiesWithinAABB(EntitySentry.class, axisalignedbb);

					for ( EntitySentry entitylivingbase : list )
					{
						if ( entitylivingbase instanceof EntitySentry && !(entitylivingbase instanceof EntityOrc) )
							entitylivingbase.potionImmunity = 2;
					}
					return;
				}
				else if ( p.getThrower() instanceof EntityPlayer || p.getThrower() == null )
				{
					Province province = CivilizationUtil.getProvinceAt(event.getEntity().world, pos.getX() / 16, pos.getZ() / 16);

					if ( province == null )
					{
						return;
					}

					EntityPlayer player = (EntityPlayer) p.getThrower();

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
						for ( EntityLivingBase guard : guards )
						{
							if ( guard instanceof EntityToroVillager )
							{
								((EntityToroVillager) guard).setUnderAttack(p.getThrower());
							}
						}

						adjustPlayerRep((EntityPlayer) p.getThrower(), civ, -guards.size() * 10);
					}

					List<EntityVillager> villagers = event.getEntity().world.<EntityVillager>getEntitiesWithinAABB(EntityVillager.class, axisalignedbb);

					if ( !villagers.isEmpty() )
					{
						for ( EntityLivingBase villager : villagers )
						{
							if ( villager instanceof EntityToroVillager )
							{
								((EntityToroVillager) villager).setUnderAttack(p.getThrower());
							}
						}

						adjustPlayerRep((EntityPlayer) (p).getThrower(), civ, -villagers.size() * 10);
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

		// System.out.println("hit: " + entity);

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
				EntityArrow arrow = (EntityArrow) event.getEntity();
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
				EntityArrow arrow = (EntityArrow) event.getEntity();
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
				EntityArrow arrow = (EntityArrow) event.getEntity();
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
	public void livingHurtEvent( LivingHurtEvent event )
	{
		EntityLivingBase victim = event.getEntityLiving();
		EntityLivingBase attacker = getAttacker(event);

		if ( victim == null || attacker == null )
		{
			return;
		}

		// if ( victim.world.isRemote )
		// {
		// return;
		// }

		// === VICTIM ===
		if ( victim instanceof EntityVillager && !(victim instanceof EntityFugitive) )
		{
			EntityVillager villager = (EntityVillager) victim;

			/* if there are any enemies near this entity, do not take damage */
			if ( villager.getAttackTarget() != attacker && villager.getRevengeTarget() != attacker )
			{
				if ( !villager.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(villager.getPosition()).grow(3, 3, 3), new Predicate<EntityLivingBase>()
				{
					public boolean apply( @Nullable EntityLivingBase entity )
					{
						if ( entity instanceof IMob || entity instanceof EntityMob )
						{
							return true;
						}
						else
						{
							return false;
						}
					}
				}).isEmpty() )
				{
					event.setAmount(0);
					event.setCanceled(true);
					return;
				}
			}

			if ( attacker instanceof EntityToroNpc || attacker instanceof EntityVillager )
			{
				event.setAmount(0);
				event.setCanceled(true);
				return;
			}

			if ( attacker instanceof EntityPlayer ) // PLAYER
			{
				EntityPlayer player = (EntityPlayer) attacker;
				Province province = CivilizationUtil.getProvinceAt(player.world, player.chunkCoordX, player.chunkCoordZ);

				if ( province == null )
				{
					province = CivilizationUtil.getProvinceAt(villager.world, villager.chunkCoordX, villager.chunkCoordZ);
				}

				if ( province != null )
				{
					adjustPlayerRep(player, province.civilization, -(int) MathHelper.clamp(event.getAmount() * 4, 5, villager.getHealth() * 4));
				}
			}

			if ( villager instanceof EntityToroVillager )
			{
				((EntityToroVillager) villager).callForHelp(attacker, true);
			}
			else // VANILLA VILLAGER
			{
				boolean flag = false;

				List<EntityGuard> guards = villager.world.getEntitiesWithinAABB(EntityGuard.class, new AxisAlignedBB(villager.getPosition()).grow(16, 12, 16), new Predicate<EntityGuard>()
				{
					public boolean apply( @Nullable EntityGuard entity )
					{
						return true;
					}
				});

				for ( EntityGuard guard : guards )
				{
					if ( guard.getAttackTarget() == null )
					{
						villager.getNavigator().tryMoveToEntityLiving(guard, 0.7F);

						if ( attacker instanceof EntityPlayer )
						{
							guard.setAnnoyed((EntityPlayer) attacker);
							if ( !flag && guard.actionReady() && guard.getDistance(attacker) <= 8.0D )
							{
								guard.chat((EntityPlayer) attacker, "attackvillager", null);
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
			if ( victim instanceof EntityMule && attacker instanceof EntityPlayer )
			{
				List<EntityCaravan> caravans = victim.getEntityWorld().getEntitiesWithinAABB(EntityCaravan.class, victim.getEntityBoundingBox().grow(20.0D, 10.0D, 20.0D));
				for ( EntityCaravan caravan : caravans )
				{
					((EntityToroVillager) caravan).setUnderAttack(attacker);
				}
			}
		}
		else if ( victim instanceof EntityIronGolem )
		{
			if ( attacker instanceof EntityPlayer ) // PLAYER
			{
				EntityPlayer player = (EntityPlayer) attacker;
				Province province = CivilizationUtil.getProvinceAt(player.world, player.chunkCoordX, player.chunkCoordZ);

				if ( province == null )
				{
					province = CivilizationUtil.getProvinceAt(victim.world, victim.chunkCoordX, victim.chunkCoordZ);
				}

				if ( province != null )
				{
					int rep = PlayerCivilizationCapabilityImpl.get(player).getReputation(province.civilization);

					if ( rep < 100 && ToroQuestConfiguration.loseReputationForAnimalGrief )
					{
						boolean witnessed = villagersReportCrime(player.getEntityWorld(), player);

						List<EntityToroNpc> help = player.world.getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
						{
							public boolean apply( @Nullable EntityToroNpc entity )
							{
								return true;
							}
						});
						Collections.shuffle(help);
						boolean flag = false;
						for ( EntityToroNpc entity : help )
						{
							if ( !entity.canEntityBeSeen(player) )
							{
								continue;
							}

							witnessed = true;
							entity.getNavigator().tryMoveToEntityLiving(player, 0.6D);

							if ( entity.isAnnoyed() )
							{
								entity.setAttackTarget(player);
							}

							entity.setAnnoyed(player);

							if ( !flag )
							{
								if ( entity.actionReady() )
								{
									flag = true;
									entity.chat(entity, player, "golem", null);
								}
							}
						}
						if ( witnessed )
							reportCrimeRep(player, province, -1);
					}
					else
					{
						List<EntityToroNpc> help = player.world.getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(player.getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
						{
							public boolean apply( @Nullable EntityToroNpc entity )
							{
								return true;
							}
						});
						Collections.shuffle(help);
						boolean flag = false;

						for ( EntityToroNpc entity : help )
						{
							if ( !entity.canEntityBeSeen(player) )
							{
								continue;
							}

							entity.getNavigator().tryMoveToEntityLiving(player, 0.6D);

							if ( !flag )
							{
								if ( entity.actionReady() )
								{
									flag = true;
									entity.chat(entity, player, "golem", null);
								}
							}
						}
					}
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
				event.setAmount(event.getAmount() * ToroQuestConfiguration.guardDamageBaseMultiplierToPlayers * this.rngDamageMultiplier());
				return;
			}
			else
			{
				Province province = CivilizationUtil.getProvinceAt(attacker.getEntityWorld(), attacker.chunkCoordX, attacker.chunkCoordZ);

				if ( province == null || !victim.isNonBoss() || victim.getMaxHealth() >= ToroQuestConfiguration.minBaseHealthToBeConsideredBossMob ) // NO
																																					 // PROVINCE
				{
					event.setAmount(event.getAmount() * ToroQuestConfiguration.guardDamageBaseMultiplierToMobsOutsideProvinceOrToBosses);
					return;
				}
				else
				{
					event.setAmount(event.getAmount() * ToroQuestConfiguration.guardDamageBaseMultiplierToMobs * this.rngDamageMultiplier());
				}

				CivilizationDataAccessor worldData = CivilizationsWorldSaveData.get(attacker.world);

				if ( worldData == null )
				{
					return;
				}

				if ( worldData.hasTrophyTitan(province.id) )
				{
					event.setAmount(event.getAmount() * ToroQuestConfiguration.trophyTitanAdditionalGuardDamageMulitiplier);
				}
			}
		}
		else if ( attacker instanceof EntityToroMob )
		{
			if ( attacker instanceof EntityOrc )
			{
				event.setAmount(event.getAmount() * this.rngDamageMultiplier() * ((event.getSource() != null && (event.getSource().isMagicDamage())) ? (this.damageMultiplier(ToroQuestConfiguration.banditAttackDamage, attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue())) : 1.0F));
			}
			else // if ( !(attacker instanceof EntityBanditLord) )
			{
				event.setAmount(event.getAmount() * this.rngDamageMultiplier() * ((event.getSource() != null && (event.getSource().isMagicDamage())) ? (this.damageMultiplier(ToroQuestConfiguration.banditAttackDamage, attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue())) : 1.0F));
			}
		}
		else if ( attacker instanceof EntityIronGolem )
		{
			if ( victim instanceof EntityToroNpc || victim instanceof EntityVillager )
			{
				((EntityIronGolem) attacker).setAttackTarget(null);
				event.setAmount(0);
				event.setCanceled(true);
			}
		}
	}

	protected float rngDamageMultiplier()
	{
		return 1.0F + ((rand.nextFloat() - 0.5F) / 20.0F);
	}

	protected float damageMultiplier( float base, double current )
	{
		if ( current <= 0.0D || base <= 0.0F )
		{
			return 1.0F;
		}
		float amount = (float) (1.0D + (current - base) / base);
		return amount > 0.0F ? amount : 0.0F;
	}

	private EntityLivingBase getAttacker( LivingHurtEvent event )
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
	 * private BlockPos findSurface(World world, BlockPos start) {
	 * 
	 * int minY = world.getActualHeight();
	 * 
	 * BlockPos pos;
	 * 
	 * IBlockState blockState;
	 * 
	 * for (int y = world.getActualHeight(); y > 0; y--) {
	 * 
	 * pos = new BlockPos(start.getX(), y, start.getZ());
	 * blockState = world.getBlockState(pos);
	 * 
	 * if (isLiquid(blockState)) {
	 * return null;
	 * }
	 * 
	 * if (isGroundBlock(blockState)) {
	 * if (y < minY) {
	 * minY = y;
	 * }
	 * 
	 * break;
	 * }
	 * }
	 * 
	 * return new BlockPos(start.getX(), minY, start.getZ());
	 * }
	 * 
	 * private boolean isLiquid(IBlockState blockState) {
	 * return blockState.getBlock() == Blocks.WATER || blockState.getBlock() ==
	 * Blocks.LAVA;
	 * }
	 * 
	 * private boolean isGroundBlock(IBlockState blockState) {
	 * 
	 * if (blockState.getBlock() == Blocks.LEAVES || blockState.getBlock() ==
	 * Blocks.LEAVES2 || blockState.getBlock() == Blocks.LOG
	 * || blockState.getBlock() instanceof BlockBush) {
	 * return false;
	 * }
	 * 
	 * return blockState.isOpaqueCube();
	 * 
	 * }
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
	public void death( LivingDeathEvent event )
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
				if ( !hasEnderIdol && player.inventory.offHandInventory.get(0).getItem().equals(Item.getByNameOrId("toroquest:ender_idol")) )
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
					stack.put(player.getName(), savedInventory);
					player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
					player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
					event.setCanceled(true);
				}
				else
				{
					savedInventory = null;
					// if ( stack.containsKey( player.getName()) )
					try
					{
						stack.remove(player.getName());
					}
					catch (Exception e)
					{

					}
				}

				// if ( event.getSource() != null && event.getSource().getTrueSource()
				// instanceof EntityGuard )
				// {
				// event.getSource().getTrueSource().setCustomNameTag("Guard");
				// }
				// else if ( event.getSource() != null && event.getSource().getTrueSource()
				// instanceof EntitySentry )
				// {
				// if ( event.getSource().getTrueSource() instanceof EntityOrc )
				// {
				// event.getSource().getTrueSource().setCustomNameTag("Orc");
				// }
				// else
				// {
				// event.getSource().getTrueSource().setCustomNameTag("Bandit");
				// }
				// }
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
				if ( !hasEnderIdol && player.inventory.offHandInventory.get(0).getItem().equals(Item.getByNameOrId("toroquest:ender_idol")) )
				{
					player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
					hasEnderIdol = true;
				}

				if ( hasEnderIdol )
				{
					player.closeScreen();
					player.inventory.closeInventory(player);
					player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
					player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
					player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
					player.playSound(SoundEvents.ENTITY_ENDEREYE_DEATH, 1.0F, 1.0F);
					player.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
					player.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
					this.teleportRandomly(player);
					player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
					player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
					player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
					player.playSound(SoundEvents.ENTITY_ENDEREYE_DEATH, 1.0F, 1.0F);
					player.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
					player.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
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
			EntityLivingBase mob = (EntityLivingBase) (event.getEntity());
			DamageSource source = event.getSource();
			if ( source == null )
				return;
			if ( source.getTrueSource() == null )
				return;

			if ( !(source.getTrueSource() instanceof EntityPlayer) )
			{
				return;
			}

			EntityPlayer player = (EntityPlayer) (source.getTrueSource());

			Province province = PlayerCivilizationCapabilityImpl.get(player).getInCivilization();

			if ( rand.nextInt(2000) <= ToroQuestConfiguration.artifactDropRate )
			{
				ItemStack item = randomStolenItem(mob.world, province);
				if ( item == null )
					return;
				mob.entityDropItem(item, 1);
			}
		}
	}

	protected void teleportRandomly( EntityPlayer p )
	{
		BlockPos pos = findTeleportLocationFrom(p.world, p.getPosition());
		if ( pos != null )
			p.attemptTeleport(pos.getX() + (rand.nextBoolean() ? 8 : -8), pos.getY(), pos.getZ() + (rand.nextBoolean() ? 8 : -8));
	}

	@SubscribeEvent
	public void respawn( PlayerEvent.Clone event )
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
				SavedInventory savedIventory = stack.get(player.getName());
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
				player.addExperience(savedIventory.experienceTotal);
				player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.8F);
				player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_SCREAM, SoundCategory.PLAYERS, 0.8F, 0.8F);
				stack.remove(player.getName());
			}
			catch (Exception e)
			{

			}
		}
	}

}
