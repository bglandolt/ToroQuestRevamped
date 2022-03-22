package net.torocraft.toroquest.entities;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookAtTradePlayer;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIPlay;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITradePlayer;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.SoundHandler;
import net.torocraft.toroquest.ToroQuest;
import net.torocraft.toroquest.civilization.CivilizationHandlers;
import net.torocraft.toroquest.civilization.CivilizationType;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.civilization.quests.QuestTradeWithVillagers;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.ai.EntityAIAvoidEnemies;
import net.torocraft.toroquest.entities.ai.EntityAISmartTempt;
import net.torocraft.toroquest.entities.ai.EntityAIToroHarvestFarmland;
import net.torocraft.toroquest.entities.ai.EntityAIToroVillagerMate;
import net.torocraft.toroquest.entities.trades.ToroVillagerTrades;

@SuppressWarnings("deprecation")
public class EntityToroVillager extends EntityVillager implements INpc, IMerchant
{
	// -------------------------------------------------------------------
	public short canTalk = 0;
	public boolean uiClick = true;
	public short chattingWithGuard = 0;
	public short blockedTrade = 0;
	public static String NAME = "toro_villager";
	public EntityLivingBase underAttack = null;
    public BlockPos bedLocation = null;
	public ItemStack treasureMap = null;
	// public Integer maxTrades = null;
	public Integer varient = null;
	// public int job = 0;
	boolean hitSafety = false;
	// -------------------------------------------------------------------
    private static final DataParameter<Integer> PROFESSION = EntityDataManager.<Integer>createKey(EntityToroVillager.class, DataSerializers.VARINT);
    private boolean isMating;
    private boolean isPlaying;
    public Village village;
    /** This villager's current customer. */
    @Nullable
    private EntityPlayer buyingPlayer;
    /** Initialises the MerchantRecipeList.java */
    @Nullable
    private MerchantRecipeList buyingList;
    private int timeUntilReset;
    /** addDefaultEquipmentAndRecipies is called if this is true */
    private boolean isWillingToMate;
    private int wealth;
    /** Last player to trade with this villager, used for aggressivity. */
    private int careerId;
    /** This is the EntityVillager's career level value */
    private int careerLevel;
    private boolean isLookingForHome = true;
    private boolean areAdditionalTasksSet = false;
    private final InventoryBasic villagerInventory;
	  private boolean needsInitilization;
	// -------------------------------------------------------------------
    
	@Override
	protected void initEntityAI()
    {              
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAvoidEnemies(this, 0.5D, 0.65D));
		this.tasks.addTask(1, new EntityAIPanic(this, 0.65D)
		{
			@Override
			public boolean shouldExecute()
		    {
		        if ( (isUnderAttack() && this.creature.canEntityBeSeen(underAttack)) || isBurning() )
		        {
		            return this.findRandomPosition();
		        }
		        return false;
		    }
			
			@Override
			protected boolean findRandomPosition()
		    {
		        Vec3d vec3d = null;

				if ( isUnderAttack() && underAttack.getPositionVector() != null )
				{
			        vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 8, underAttack.getPositionVector());
				}
				else
				{
			        vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 8, this.creature.getPositionVector());
				}
				
		        if ( vec3d == null )
		        {
		            return false;
		        }
		        else
		        {
		            this.randPosX = vec3d.x;
		            this.randPosY = vec3d.y;
		            this.randPosZ = vec3d.z;
		            return true;
		        }
		    }
		});
        this.tasks.addTask(3, new EntityAIToroVillagerMate(this));
        this.tasks.addTask(4, new EntityAITradePlayer(this));
        this.tasks.addTask(5, new EntityAILookAtTradePlayer(this));
        this.tasks.addTask(6, new EntityAISmartTempt(this, 0.4D, Items.EMERALD)
        {
        	@Override
			public boolean shouldExecute()
		    {
        		super.shouldExecute();
		        if ( isUnderAttack() || !canTrade() || isTrading() || isBurning() || isMating() )
		        {
			        return false;
		        }
		        return super.shouldExecute();
		    }
		});
        this.tasks.addTask(7, new EntityAIMoveIndoors(this));
        this.tasks.addTask(8, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(9, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(10, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(11, new EntityAIWanderAvoidWater(this, 0.5D)
        {
        	@Override
        	public boolean shouldExecute()
            {
        		if ( isMating() || isTrading() || isUnderAttack() || chattingWithGuard > 0 )
        		{
        			return false;
        		}
        		
                if ( !this.mustUpdate )
                {
                    if ( this.entity.getIdleTime() >= 100 )
                    {
                        return false;
                    }

                    if ( !this.entity.isInWater() && !this.entity.isInLava() && this.entity.getRNG().nextInt(this.executionChance) != 0 )
                    {
                        return false;
                    }
                }

                Vec3d vec3d = this.getPosition();

                if ( vec3d == null )
                {
                    return false;
                }
                else
                {
                    this.x = vec3d.x;
                    this.y = vec3d.y;
                    this.z = vec3d.z;
                    this.mustUpdate = false;
                    return true;
                }
            }
        	
        	@Override
            protected Vec3d getPosition()
            {
        		if ( !this.entity.hasPath() && ( this.entity.isInWater() || this.entity.isInLava() ) )
                {
                    Vec3d vec3d = RandomPositionGenerator.getLandPos(this.entity, 16, 8);
                    return vec3d == null ? super.getPosition() : vec3d;
                }
                else
                {
                    return this.entity.getRNG().nextFloat() >= this.probability ? RandomPositionGenerator.getLandPos(this.entity, 12, 6) : super.getPosition();
                }
            }
        });
        this.tasks.addTask(12, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F)
        {
        	@Override
	        public boolean shouldExecute()
	        {
		        if ( isUnderAttack() || !canTrade() || isBurning() || isMating() || EntityToroVillager.this.hasPath() )
        		{
	        		this.closestEntity = null;
        			return false;
        		}
        		if ( getCustomer() != null )
        		{
            		this.closestEntity = getCustomer();
            		return true;
        		}
        		this.closestEntity = null;
        		return false;
	        }
        });
    }
	
	@Override
	public String getName()
    {
        String s = EntityList.getEntityString(this);

        if (s == null)
        {
            s = "generic";
        }

        return I18n.translateToLocal("entity." + s + ".name");
    }

	@Override
	public boolean processInteract( EntityPlayer player, EnumHand hand )
	{
		if ( this.world.isRemote || !this.isEntityAlive() || this.isTrading() || this.isChild() || this.isMating() || this.isBurning() )
		{
			return true;
		}
        
		ItemStack itemstack = player.getHeldItem(hand);
		
        boolean flag = itemstack.getItem() == Items.NAME_TAG;

        if (flag)
        {
            itemstack.interactWithEntity(player, this, hand);
            return true;
        }
        
		for ( ItemStack itemStack : player.getArmorInventoryList() )
		{
			if ( itemStack.getItem().equals(Item.getByNameOrId("toroquest:bandit_helmet") ) || itemStack.getItem().equals(Item.getByNameOrId("toroquest:legendary_bandit_helmet") ) )
			{
	    		this.callForHelp(player, true);
				if ( this.canTalk < 1 )
				{
					this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume()*1.2F, this.getSoundPitch());
					this.canTalk = 2;
				}
				this.setUnderAttack(player);
				return true;
			}
		}
		
		RepData repData = this.getReputation(player);
		
		if ( repData == null || repData.civ == null || repData.rep == null )
		{
			if ( this.canTalk < 1 )
			{
				this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
				this.canTalk = 2;
			}
			return true;
		}
		
		if ( this.underAttack == player || ( repData.rep != null && repData.rep < -50 ) )
		{
    		this.callForHelp(player, true);
			if ( this.canTalk < 1 )
			{
				this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume()*1.2F, this.getSoundPitch());
				this.canTalk = 2;
			}
			this.setUnderAttack(player);
			return true;
		}
		
//		if ( this.job == 0 )
//		{
//			this.getRecipes(player);
//	        this.setCustomer(null);
//	        this.writeEntityToNBT(new NBTTagCompound());
//		}
		
		Item item = itemstack.getItem();

        if ( ToroQuestConfiguration.recruitVillagers && player.isSneaking() && item.equals(Item.getByNameOrId("toroquest:recruitment_papers") ) )
        {
        	if ( repData.rep != null && repData.rep >= 0 && this.canTrade() && !this.isUnderAttack() )
    		{
	        	playSound(SoundEvents.ENTITY_ILLAGER_CAST_SPELL, 1.2F, 1.2F);
	        	playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 1.0F);
	        	playSound(SoundEvents.BLOCK_ANVIL_USE, 0.5F, 0.8F);
	        	
	        	player.setHeldItem(hand, new ItemStack(item, itemstack.getCount()-1 ));
	        	
				EntityGuard newEntity = new EntityGuard(world);
				newEntity.setPosition(this.posX, this.posY, this.posZ);
				newEntity.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(this.getPosition())), (IEntityLivingData) null);
				newEntity.copyLocationAndAnglesFrom(this);
				newEntity.actionTimer = 1;
				
				this.setDead();
				world.spawnEntity(newEntity);
				
				newEntity.recruitGuard(player, repData.prov, "civvillagerrecruit");
    		}
        	else if ( this.canTalk <= 0 )
			{
    			this.playTameEffect(false);
    			this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
    			this.canTalk = 2;
			}
        	return true;
        }
        
    	
    	if ( this.blockedTrade > 0 )
		{
    		if ( this.canTalk <= 0 )
			{
				this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume()*1.2F, this.getSoundPitch()*0.9F);
				this.canTalk = 1;
			}
    		return true;
		}
		else
		{
			this.getRecipes(player, repData);
			
			if ( this.buyingList == null || this.buyingList.isEmpty() )
			{
				if ( this.canTalk <= 0 )
    			{
    				this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
    				this.canTalk = 1;
    			}
                return true;
			}
			else
            {
                this.setCustomer(player);
                player.displayVillagerTradeGui(this);

    			if ( this.canTalk <= 0 )
    			{
    				this.playSound(SoundEvents.ENTITY_VILLAGER_TRADING, this.getSoundVolume(), this.getSoundPitch());
    				this.canTalk = 1;
    			}
    			return true;
            }
		}
	}
	
	protected void playTameEffect(boolean play)
    {
        EnumParticleTypes enumparticletypes = EnumParticleTypes.HEART;
        
        if (!play)
        {
            enumparticletypes = EnumParticleTypes.SMOKE_NORMAL;
        }

        for (int i = 0; i < 7; ++i)
        {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
        }
    }
	
	public void callForHelp( EntityLivingBase attacker, boolean attackThem )
	{
//		if ( this.world.isRemote )
//		{
//			return;
//		}
		
		this.setUnderAttack(attacker);
		
		List<EntityToroVillager> villagers = world.getEntitiesWithinAABB(EntityToroVillager.class, new AxisAlignedBB(getPosition()).grow(16, 8, 16), new Predicate<EntityToroVillager>()
		{
			public boolean apply(@Nullable EntityToroVillager entity)
			{
				return true;
			}
		});

		for (EntityToroVillager villager : villagers)
		{
			if ( villager.canEntityBeSeen(attacker) )
			{
				villager.setUnderAttack(attacker);
			}
		}
		
		List<EntityGuard> guards = world.getEntitiesWithinAABB(EntityGuard.class, new AxisAlignedBB(getPosition()).grow(20, 12, 20), new Predicate<EntityGuard>()
		{
			public boolean apply(@Nullable EntityGuard entity)
			{
				return true;
			}
		});
		
		boolean flag = false;
		
		for ( EntityGuard guard : guards )
		{
			if ( guard.getAttackTarget() == null )
			{
				this.getNavigator().tryMoveToEntityLiving(guard, 0.7F);
				
				if ( attacker instanceof EntityPlayer ) 
				{
					guard.setAnnoyed( (EntityPlayer)attacker );
					if ( !flag && guard.actionReady() && guard.getDistance(attacker) <= 8.0D )
					{
						guard.chat((EntityPlayer)attacker, "attackvillager", null);
						flag = true;
					}
				}
				
				if ( guard.isAnnoyed() || attackThem )
				{
					guard.setAttackTarget(attacker);
				}
			}
		}
	}
	
	// guards move to the player
	public void reportToGuards( EntityPlayer player )
	{
		List<EntityToroNpc> guards = world.getEntitiesWithinAABB(EntityToroNpc.class, new AxisAlignedBB(getPosition()).grow(16, 12, 16), new Predicate<EntityToroNpc>()
		{
			public boolean apply(@Nullable EntityToroNpc entity)
			{
				return true;
			}
		});
		Collections.shuffle(guards);
		for (EntityToroNpc guard : guards)
		{
			if ( !guard.inCombat )
			{
				this.getNavigator().tryMoveToEntityLiving(guard, 0.5D);
				guard.getNavigator().tryMoveToEntityLiving(player, 0.5D);
			}
		}
	}
	
	
	
	
	// ========================================================================
	// ========================================================================
	//                               T R A D E S
	// ========================================================================
	// ========================================================================
		
	@Nullable
	@Override
    public MerchantRecipeList getRecipes(EntityPlayer player)
    {
		RepData repData = this.getReputation(player);
		
		if ( repData == null || repData.civ == null || repData.rep == null || repData.rep < -50 )
		{
			return new MerchantRecipeList();
		}
		
		return this.getRecipes(player, repData);
    }
	
	@Nullable
    public MerchantRecipeList getRecipes(EntityPlayer player, RepData repData)
    {
		if ( this.buyingList == null || this.buyingList.isEmpty() || this.needsInitilization )
        {
            this.populateBuyingList(player, repData);
        }

        return net.minecraftforge.event.ForgeEventFactory.listTradeOffers(this, player, this.buyingList);
    }
	
//	  @Override
//    private void populateBuyingList()
//    {
//		EntityPlayer player = this.buyingPlayer;
//		if ( player != null )
//		{
//			RepData repData = this.getReputation(player);
//			this.populateBuyingList(player, repData);
//		}
//    }

	private void populateBuyingList(EntityPlayer player, RepData repData)
    {    	
        if ( this.careerId != 0 && this.careerLevel != 0 )
        {
        	this.careerLevel = MathHelper.clamp(1+repData.rep/600, 1, 8);
        }
        else
        {
            this.careerId = this.getProfessionForge().getRandomCareer(this.rand) + 1;
            this.careerLevel = 1;
        }

        this.buyingList = this.createTradesBaseOnRep(player, repData);

        // XXX this adds extra trades based of the villagers career level!!!
//        int i = this.careerId - 1;
//        int j = this.careerLevel - 1;
//        java.util.List<EntityVillager.ITradeList> trades = this.getProfessionForge().getCareer(i).getTrades(j);
//
//        if (trades != null)
//        {
//            for (EntityVillager.ITradeList tradeList : trades)
//            {
//            	tradeList.addMerchantRecipe(this, this.buyingList, this.rand);
//            }
//        }
    }
	
	protected MerchantRecipeList createTradesBaseOnRep(EntityPlayer player, RepData repData)
	{		
//		if ( repData == null || repData.civ == null || repData.rep == null || repData.rep < -50 )
//		{
//			return new MerchantRecipeList();
//		}
//		return ToroVillagerTrades.trades(this, player, repData.rep, repData.civ, this.getProfessionForge().getCareer(this.job-1).getName(), ""+this.varient );
		return ToroVillagerTrades.trades(this, player, repData.rep, repData.civ, this.getProfessionForge().getCareer(this.careerId-1).getName(), String.valueOf(this.varient) );
	}
    
	// ========================================================================
	// ========================================================================

	// ========================================================================
	// ========================================================================
    
    
    
    
    
    
    
	
	@Override
	public void onLivingUpdate()
	{

		super.onLivingUpdate();
				
		if ( this.world.isRemote )
		{
			return;
		}
		
//		livingUpdateTimer++;
//		livingUpdateTimer %= 100;
//		if ( livingUpdateTimer == 0 )
		
//		if ( this.world.isRemote )
//		{
//			return;
//		}
		
		this.uiClick = true;

		if ( this.ticksExisted % 100 == 0 )
		{
//			if ( world % 1200 == 0 )
//			{
//				if ( this.maxTrades > 0 )
//				{
//					this.maxTrades--;
//				}
//			}
			if ( this.getHealth() >= this.getMaxHealth() )
			{
				if ( this.blockedTrade <= 0 )
				{
					this.hitSafety = true;
				}
			}
			else this.heal(1.0f);
			
			if ( this.canTalk > 0 )
			{	
				this.canTalk--;
			}
			
			if ( this.blockedTrade > 0 )
    		{
    			this.blockedTrade--;
    		}
			else
			{
				this.underAttack = null;
			}
			
			if ( this.chattingWithGuard > 0 )
			{
				this.chattingWithGuard--;
			}
			
            if ( this.village == null )
            {
	            this.world.getVillageCollection().addToVillagerPositionList(this.getPos());
	            this.village = this.world.getVillageCollection().getNearestVillage(this.getPos(), 32);
            }
            
            if ( this.village == null )
            {
                this.detachHome();
            }
            else if ( this.isLookingForHome )
            {
                this.isLookingForHome = false;
                this.setHomePosAndDistance(this.village.getCenter(), this.village.getVillageRadius());
            }
            
	        if ( !this.isTrading() && this.timeUntilReset > 0 )
	        {
	            --this.timeUntilReset;

	            if ( this.timeUntilReset <= 0 )
	            {
//                    for ( MerchantRecipe merchantrecipe : this.buyingList )
//                    {
//                        if ( merchantrecipe.isRecipeDisabled() )
//                        {
//                            merchantrecipe.increaseMaxTradeUses(ToroVillagerTrades.MAX_TRADE_AMOUNT);
//                        }
//                    }
                    this.timeUntilReset = 60;
                    this.needsInitilization = true;
	            }
	        }
		}
	}
	
	@Override
	public boolean canBeLeashedTo(EntityPlayer player)
	{
		this.callForHelp( player, false );
		if ( !this.getLeashed() )
		{
			RepData repData = getReputation(player);
			if ( repData != null )
			{
				CivilizationHandlers.adjustPlayerRep(player, repData.civ, -ToroQuestConfiguration.leashVillagerRepLoss);
			}
		}
		return true;
	}
	
	public EntityToroVillager(World worldIn)
	{
		this(worldIn, 0);
	}
	
	public EntityToroVillager(World worldIn, int professionId )
	{
		super(worldIn, professionId);
        this.villagerInventory = new InventoryBasic("Items", false, 8);
        this.setProfession(professionId);
        this.setAdditionalAItasks();
        this.setSize(0.6F, 1.95F);
        ((PathNavigateGround)this.getNavigator()).setBreakDoors(true);
        this.setCanPickUpLoot(true);
		this.stepHeight = 1.05F;
    	this.varient = this.rand.nextInt(ToroQuestConfiguration.villagerUniqueShopInventoryVarients+1);
    	this.writeEntityToNBT(new NBTTagCompound());
	}
    
    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
    	super.readEntityFromNBT(compound);

    	if ( compound.hasKey("Varient") )
    	{
    		this.varient = compound.getInteger("Varient");
    	}
    	else
    	{
        	this.varient = this.rand.nextInt(ToroQuestConfiguration.villagerUniqueShopInventoryVarients+1);
    	}
    	
//    	if ( compound.hasKey("Career") )
//    	{
//    		this.job = compound.getInteger("Career");
//    	}

        this.setProfession(compound.getInteger("Profession"));
        
        if (compound.hasKey("ProfessionName"))
        {
            net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession p =
                net.minecraftforge.fml.common.registry.ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new net.minecraft.util.ResourceLocation(compound.getString("ProfessionName")));
            if (p == null)
                p = net.minecraftforge.fml.common.registry.ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new net.minecraft.util.ResourceLocation("minecraft:farmer"));
            this.setProfession(p);
        }
        
        this.wealth = compound.getInteger("Riches");
        this.careerId = compound.getInteger("Career");
        this.careerLevel = compound.getInteger("CareerLevel");
        this.isWillingToMate = compound.getBoolean("Willing");

        if (compound.hasKey("Offers", 10))
        {
            NBTTagCompound nbttagcompound = compound.getCompoundTag("Offers");
            this.buyingList = new MerchantRecipeList(nbttagcompound);
        }

        NBTTagList nbttaglist = compound.getTagList("Inventory", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            ItemStack itemstack = new ItemStack(nbttaglist.getCompoundTagAt(i));

            if (!itemstack.isEmpty())
            {
                this.villagerInventory.addItem(itemstack);
            }
        }

        this.setCanPickUpLoot(true);
        this.setAdditionalAItasks();
    }
    
    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setInteger("Profession", this.getProfession());
        compound.setString("ProfessionName", this.getProfessionForge().getRegistryName().toString());
        compound.setInteger("Riches", this.wealth);
        compound.setInteger("Career", this.careerId);
        compound.setInteger("CareerLevel", this.careerLevel);
        compound.setBoolean("Willing", this.isWillingToMate);

        if ( this.buyingList != null )
        {
            compound.setTag("Offers", this.buyingList.getRecipiesAsTags());
        }

        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.villagerInventory.getSizeInventory(); ++i)
        {
            ItemStack itemstack = this.villagerInventory.getStackInSlot(i);

            if (!itemstack.isEmpty())
            {
                nbttaglist.appendTag(itemstack.writeToNBT(new NBTTagCompound()));
            }
        }

        compound.setTag("Inventory", nbttaglist);
        
        if ( this.varient == null )
    	{
    		this.varient = this.rand.nextInt(ToroQuestConfiguration.villagerUniqueShopInventoryVarients+1);
    	}
        compound.setInteger("Varient", this.varient);
        
//      if ( compound.hasKey("Career") )
//    	{
//    		this.job = compound.getInteger("Career");
//    	}
    }
    
	// =========================== REPUTATION ============================
	static class RepData
	{
		public CivilizationType civ;
		public Province prov;
		public Integer rep;
	}

	protected RepData getReputation(EntityPlayer player)
	{
		if ( player == null )
		{
			return null;
		}
		
		RepData repData = new RepData();
		
		Province province = CivilizationUtil.getProvinceAt( player.world, player.chunkCoordX, player.chunkCoordZ );

		if ( province == null || province.getCiv() == null )
		{
			return null;
		}
		
		repData.civ = province.getCiv();
		repData.prov = province;
		repData.rep = PlayerCivilizationCapabilityImpl.get(player).getReputation( repData.civ );
				
		return repData;
	}
	// ===================================================================

	// ============================ ATTACKED =============================
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if ( this.world.isRemote )
        {
            return false;
        }
		
		if ( source.getTrueSource() == null )
		{
	        Vec3d vec3d = RandomPositionGenerator.getLandPos(this, 8, 4);
            if ( vec3d != null )
            {
		        this.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, 0.5D);
            }
		}
		else if ( source.getTrueSource() instanceof EntityLivingBase )
		{
			if ( source.getTrueSource() instanceof EntityToroNpc )
			{
				amount = 0.0F;
				return false;
			}
			
			EntityLivingBase e = (EntityLivingBase)source.getTrueSource();
			
			if ( e instanceof EntityPlayer )
			{
				if ( this.hitSafety )
				{
					this.hitSafety = false;
					this.playSound(SoundEvents.BLOCK_CLOTH_BREAK, this.getSoundVolume(), this.getSoundPitch());
					amount = 0.0F;
					return false;
				}
				
				List<EntityLivingBase> enemies = e.getEntityWorld().getEntitiesWithinAABB(EntityGuard.class, new AxisAlignedBB(this.getPosition()).grow(16, 12, 16), new Predicate<EntityLivingBase>()
				{
					public boolean apply(@Nullable EntityLivingBase entity)
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
				});
				
				if ( !enemies.isEmpty() )
				{
					amount = 0.0F;
					return false;
				}
			}
			this.callForHelp(e, true);
		}
		
		return super.attackEntityFrom(source, amount);
	}
	// ====================================================================

	// ============================= TRADING ==============================
	@Override
	public void useRecipe(MerchantRecipe recipe) // XXX
    {
		recipe.incrementToolUses();
		
        if ( this.timeUntilReset < 30 )
        {
        	this.timeUntilReset = 30;
        }

        this.livingSoundTime = -this.getTalkInterval();

        if ( this.uiClick ) 
    	{
    		this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, this.getSoundVolume(), this.getSoundPitch());
    		if ( ToroQuestConfiguration.coinTradeSounds )
    		{
	    		switch ( rand.nextInt(3) )
	        	{
	        		case 0:
	        		{
	            		this.playSound(SoundHandler.TRADEC_0, this.getSoundVolume()*0.9F, this.getSoundPitch()*1.1F-0.2F); break;
	        		}
	        		case 1:
	        		{
	            		this.playSound(SoundHandler.TRADEC_1, this.getSoundVolume()*0.9F, this.getSoundPitch()*1.1F-0.2F); break;
	        		}
	        		case 2:
	        		{
	            		this.playSound(SoundHandler.TRADEC_2, this.getSoundVolume()*0.9F, this.getSoundPitch()*1.1F-0.2F); break;
	        		}
	        	}
    		}
    		this.uiClick = false;
    	}
    	
        try
        {
        	QuestTradeWithVillagers.INSTANCE.onTrade(this.getCustomer());
        }
        catch(Exception e)
        {
        	
        }
        		
//        if ( recipe.getToolUses() <= 1 )
//        {
//            if ( this.getCustomer() != null )
//            {
//                this.lastBuyingPlayer = this.getCustomer().getUniqueID();
//            }
//            else
//            {
//                this.lastBuyingPlayer = null;
//            }
//        }
		
//        if (recipe.getItemToBuy().getItem() == Items.EMERALD)
//        {
//            this.wealth += recipe.getItemToBuy().getCount();
//        }

//        int xp = 0;
//        if (recipe.getItemToBuy() == Items.EMERALD)
//        {
//        	xp = recipe.getItemToBuy().getCount()
//        {
//            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, xp));
//        }

        if ( this.getCustomer() instanceof EntityPlayerMP )
        {
            CriteriaTriggers.VILLAGER_TRADE.trigger((EntityPlayerMP)this.getCustomer(), this, recipe.getItemToSell());
        }

    }
	
	
//    public void useRecipe(MerchantRecipe recipe)
//    {
//        recipe.incrementToolUses();
//        this.livingSoundTime = -this.getTalkInterval();
//        this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
//        int i = 3 + this.rand.nextInt(4);
//
//        if (recipe.getToolUses() == 1 || this.rand.nextInt(5) == 0)
//        {
//            this.timeUntilReset = 40;
//            this.needsInitilization = true;
//            this.isWillingToMate = true;
//
//            if (this.buyingPlayer != null)
//            {
//                this.lastBuyingPlayer = this.buyingPlayer.getUniqueID();
//            }
//            else
//            {
//                this.lastBuyingPlayer = null;
//            }
//
//            i += 5;
//        }
//
//        if (recipe.getItemToBuy().getItem() == Items.EMERALD)
//        {
//            this.wealth += recipe.getItemToBuy().getCount();
//        }
//
//        if (recipe.getRewardsExp())
//        {
//            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, i));
//        }
//
//        if (this.buyingPlayer instanceof EntityPlayerMP)
//        {
//            CriteriaTriggers.VILLAGER_TRADE.trigger((EntityPlayerMP)this.buyingPlayer, this, recipe.getItemToSell());
//        }
//    }
	
	
	// ===================================================================
	
	// ========================== UNDER ATTACK ===========================
	public boolean isUnderAttack()
	{
		return this.underAttack != null && this.underAttack.isEntityAlive();
	}
			
	public boolean canTrade()
	{
		return this.blockedTrade < 1;
	}
	
	public void blockTrade()
	{
		if ( this.blockedTrade < 8 )
		{
			this.blockedTrade += 4;
		}
	}
	
	public void setUnderAttack( EntityLivingBase entity )
	{
		if ( entity instanceof EntityPlayer )
		{
			this.underAttack = (EntityPlayer)entity;
			if ( this.blockedTrade < 16 )
			{
				this.blockedTrade += 4;
			}
		}
		else
		{
			this.underAttack = null;
			if ( this.blockedTrade < 8 )
			{
				this.blockedTrade += 2;
			}
		}
	}
	
	public void setMurder( EntityPlayer player )
	{
		this.underAttack = player;
		this.blockedTrade = 64;
	}
	// ===================================================================
	
	// ============================= MATING ==============================
	@Override
	public boolean getIsWillingToMate(boolean updateFirst)
    {
        if ( updateFirst )
        {
        	int s = 8;
        	int x = (int)(this.posX+0.5D);
        	int y = (int)(this.posY+0.5D);
        	int z = (int)(this.posZ+0.5D);
			
			for ( int xx = x-s; x+s >= xx; xx++ )
			{
				for ( int yy = y-s; y+s >= yy; yy++ )
				{
					for ( int zz = z-s; z+s >= zz; zz++ )
					{
						Block bed = this.world.getBlockState((new BlockPos(xx, yy, zz))).getBlock();
    					if ( bed instanceof BlockBed )
    					{
    						//System.out.println(bed);
    						this.bedLocation = new BlockPos(xx, yy, zz);
    				        return this.bedLocation != null ? true: false;
    					}
					}
				}
			}
        }
        return this.bedLocation != null ? true: false;
    }
	
	@Override
	public void setIsWillingToMate(boolean isWillingToMate)
    {
        if ( isWillingToMate )
        {
        	this.getIsWillingToMate(true);
        }
        else
        {
        	this.bedLocation = null;
        }
    }
	
	@Override
	public EntityVillager createChild(EntityAgeable ageable)
    {
        EntityToroVillager entityvillager = new EntityToroVillager( this.world, this.getProfession() );
        entityvillager.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(entityvillager)), (IEntityLivingData)null);
        return entityvillager;
    }
	// ===================================================================
	
	// =============================== MISC ==============================
	@Override
	public int getHorizontalFaceSpeed()
	{
		return 10;
	}
	
	@Override
	protected boolean canDespawn()
	{
		return false;
	}
	
	static
	{
		if (ToroQuestConfiguration.specificEntityNames)
		{
			NAME = ToroQuestEntities.ENTITY_PREFIX + NAME;
		}
	}
	
	public static void init(int entityId)
	{
		EntityRegistry.registerModEntity(new ResourceLocation(ToroQuest.MODID, NAME), EntityToroVillager.class, NAME, entityId, ToroQuest.INSTANCE, 80, 3,
				true, 0x000000, 0xe0d6b9);
	}
	
	@Nullable
	@Override
	protected SoundEvent getAmbientSound()
    {
        if ( this.isTrading() ) 
        {
        	return SoundEvents.ENTITY_VILLAGER_TRADING;
        }
        else if ( rand.nextBoolean() )
        {
        	return SoundEvents.ENTITY_VILLAGER_AMBIENT;
        }
        else
        {
        	return null;
        }
    }
	
    @Override
    protected void collideWithNearbyEntities()
    {
    	
    }
	
	@Override
	public boolean attackEntityAsMob(Entity victim)
	{
		this.setAttackTarget(null);
		return false;
	}
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
	// ===================================================================
    private void setAdditionalAItasks()
    {
        if (!this.areAdditionalTasksSet)
        {
            this.areAdditionalTasksSet = true;

            if (this.isChild())
            {
                this.tasks.addTask(8, new EntityAIPlay(this, 0.32D));
            }
            else if (this.getProfession() == 0)
            {
                this.tasks.addTask(6, new EntityAIToroHarvestFarmland(this, 0.6D));
            }
        }
    }

    /**
     * This is called when Entity's growing age timer reaches 0 (negative values are considered as a child, positive as
     * an adult)
     */
    protected void onGrowingAdult()
    {
        if (this.getProfession() == 0)
        {
            this.tasks.addTask(8, new EntityAIToroHarvestFarmland(this, 0.6D));
        }

        super.onGrowingAdult();
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
    }

    @Override
    protected void updateAITasks()
    {
    	
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(PROFESSION, Integer.valueOf(0));
    }

    public static void registerFixesVillager(DataFixer fixer)
    {
        EntityLiving.registerFixesMob(fixer, EntityVillager.class);
        fixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists(EntityVillager.class, new String[] {"Inventory"}));
        fixer.registerWalker(FixTypes.ENTITY, new IDataWalker()
        {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn)
            {
                if (EntityList.getKey(EntityVillager.class).equals(new ResourceLocation(compound.getString("id"))) && compound.hasKey("Offers", 10))
                {
                    NBTTagCompound nbttagcompound = compound.getCompoundTag("Offers");

                    if (nbttagcompound.hasKey("Recipes", 9))
                    {
                        NBTTagList nbttaglist = nbttagcompound.getTagList("Recipes", 10);

                        for (int i = 0; i < nbttaglist.tagCount(); ++i)
                        {
                            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                            DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "buy");
                            DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "buyB");
                            DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "sell");
                            nbttaglist.set(i, nbttagcompound1);
                        }
                    }
                }

                return compound;
            }
        });
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LootTableList.ENTITIES_VILLAGER;
    }

    public void setProfession(int professionId)
    {
        this.dataManager.set(PROFESSION, Integer.valueOf(professionId));
        net.minecraftforge.fml.common.registry.VillagerRegistry.onSetProfession(this, professionId);
    }

    @Deprecated //Use Forge Variant below
    public int getProfession()
    {
        return Math.max(((Integer)this.dataManager.get(PROFESSION)).intValue(), 0);
    }

    private net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession prof;
    public void setProfession(net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession prof)
    {
        this.prof = prof;
        this.setProfession(net.minecraftforge.fml.common.registry.VillagerRegistry.getId(prof));
    }

    public net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession getProfessionForge()
    {
        if (this.prof == null)
        {
            this.prof = net.minecraftforge.fml.common.registry.VillagerRegistry.getById(this.getProfession());
            if (this.prof == null)
                return net.minecraftforge.fml.common.registry.VillagerRegistry.getById(0); //Farmer
        }
        return this.prof;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        super.notifyDataManagerChange(key);
        if (key.equals(PROFESSION))
        {
            net.minecraftforge.fml.common.registry.VillagerRegistry.onSetProfession(this, this.dataManager.get(PROFESSION));
        }
    }

    public boolean isMating()
    {
        return this.isMating;
    }

    public void setMating(boolean mating)
    {
        this.isMating = mating;
    }

    public void setPlaying(boolean playing)
    {
        this.isPlaying = playing;
    }

    public boolean isPlaying()
    {
        return this.isPlaying;
    }

    /**
     * Hint to AI tasks that we were attacked by the passed EntityLivingBase and should retaliate. Is not guaranteed to
     * change our actual active target (for example if we are currently busy attacking someone else)
     */
    public void setRevengeTarget(@Nullable EntityLivingBase livingBase)
    {
        super.setRevengeTarget(livingBase);

        if (this.village != null && livingBase != null)
        {
            this.village.addOrRenewAgressor(livingBase);

            if (livingBase instanceof EntityPlayer)
            {
                int i = -1;

                if (this.isChild())
                {
                    i = -3;
                }

                this.village.modifyPlayerReputation(livingBase.getUniqueID(), i);

                if (this.isEntityAlive())
                {
                    this.world.setEntityState(this, (byte)13);
                }
            }
        }
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause)
    {
        if (this.village != null)
        {
            Entity entity = cause.getTrueSource();

            if (entity != null)
            {
                if (entity instanceof EntityPlayer)
                {
                    this.village.modifyPlayerReputation(entity.getUniqueID(), -2);
                }
                else if (entity instanceof IMob)
                {
                    this.village.endMatingSeason();
                }
            }
            else
            {
                EntityPlayer entityplayer = this.world.getClosestPlayerToEntity(this, 16.0D);

                if (entityplayer != null)
                {
                    this.village.endMatingSeason();
                }
            }
        }

        super.onDeath(cause);
    }

    public void setCustomer(@Nullable EntityPlayer player)
    {
        this.buyingPlayer = player;
    }

    @Nullable
    public EntityPlayer getCustomer()
    {
        return this.buyingPlayer;
    }

    public boolean isTrading()
    {
        return this.getCustomer() != null;
    }

    /**
     * Notifies the merchant of a possible merchantrecipe being fulfilled or not. Usually, this is just a sound byte
     * being played depending if the suggested itemstack is not null.
     */
    public void verifySellingItem(ItemStack stack)
    {
        if ( !this.world.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20 )
        {
            this.livingSoundTime = -this.getTalkInterval();
            this.playSound(stack.isEmpty() ? SoundEvents.ENTITY_VILLAGER_NO : SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
            this.canTalk = 1;
        }
    }

    @SideOnly(Side.CLIENT)
    public void setRecipes(@Nullable MerchantRecipeList recipeList)
    {
    }

    public World getWorld()
    {
        return this.world;
    }

    public BlockPos getPos()
    {
        return new BlockPos(this);
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    public ITextComponent getDisplayName()
    {
        Team team = this.getTeam();
        String s = this.getCustomNameTag();

        if (s != null && !s.isEmpty())
        {
            TextComponentString textcomponentstring = new TextComponentString(ScorePlayerTeam.formatPlayerName(team, s));
            textcomponentstring.getStyle().setHoverEvent(this.getHoverEvent());
            textcomponentstring.getStyle().setInsertion(this.getCachedUniqueIdString());
            return textcomponentstring;
        }
        else
        {
            String s1 = null;

            switch (this.getProfession())
            {
                case 0:

                    if (this.careerId == 1)
                    {
                        s1 = "farmer";
                    }
                    else if (this.careerId == 2)
                    {
                        s1 = "fisherman";
                    }
                    else if (this.careerId == 3)
                    {
                        s1 = "shepherd";
                    }
                    else if (this.careerId == 4)
                    {
                        s1 = "fletcher";
                    }

                    break;
                case 1:

                    if (this.careerId == 1)
                    {
                        s1 = "librarian";
                    }
                    else if (this.careerId == 2)
                    {
                        s1 = "cartographer";
                    }

                    break;
                case 2:
                    s1 = "cleric";
                    break;
                case 3:

                    if (this.careerId == 1)
                    {
                        s1 = "armor";
                    }
                    else if (this.careerId == 2)
                    {
                        s1 = "weapon";
                    }
                    else if (this.careerId == 3)
                    {
                        s1 = "tool";
                    }

                    break;
                case 4:

                    if (this.careerId == 1)
                    {
                        s1 = "butcher";
                    }
                    else if (this.careerId == 2)
                    {
                        s1 = "leather";
                    }

                    break;
                case 5:
                    s1 = "nitwit";
            }

            s1 = this.getProfessionForge().getCareer(this.careerId-1).getName();
            {
                ITextComponent itextcomponent = new TextComponentTranslation("entity.Villager." + s1, new Object[0]);
                itextcomponent.getStyle().setHoverEvent(this.getHoverEvent());
                itextcomponent.getStyle().setInsertion(this.getCachedUniqueIdString());

                if (team != null)
                {
                    itextcomponent.getStyle().setColor(team.getColor());
                }

                return itextcomponent;
            }
        }
    }

    public float getEyeHeight()
    {
        return this.isChild() ? 0.81F : 1.62F;
    }

    /**
     * Handler for {@link World#setEntityState}
     */
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id)
    {
        if (id == 12)
        {
            this.spawnParticles(EnumParticleTypes.HEART);
        }
        else if (id == 13)
        {
            this.spawnParticles(EnumParticleTypes.VILLAGER_ANGRY);
        }
        else if (id == 14)
        {
            this.spawnParticles(EnumParticleTypes.VILLAGER_HAPPY);
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles(EnumParticleTypes particleType)
    {
        for (int i = 0; i < 5; ++i)
        {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(particleType, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 1.0D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
        }
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
        return this.finalizeMobSpawn(difficulty, livingdata, true);
    }

    @Override
    public IEntityLivingData finalizeMobSpawn(DifficultyInstance p_190672_1_, @Nullable IEntityLivingData p_190672_2_, boolean p_190672_3_)
    {
        p_190672_2_ = super.onInitialSpawn(p_190672_1_, p_190672_2_);

        if (p_190672_3_)
        {
            net.minecraftforge.fml.common.registry.VillagerRegistry.setRandomProfession(this, this.world.rand);
        }

        this.setAdditionalAItasks();
        return p_190672_2_;
    }

    public void setLookingForHome()
    {
        this.isLookingForHome = true;
    }

    /**
     * Called when a lightning bolt hits the entity.
     */
    public void onStruckByLightning(EntityLightningBolt lightningBolt)
    {
        if (!this.world.isRemote && !this.isDead)
        {
            EntityWitch entitywitch = new EntityWitch(this.world);
            entitywitch.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            entitywitch.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(entitywitch)), (IEntityLivingData)null);
            entitywitch.setNoAI(this.isAIDisabled());

            if (this.hasCustomName())
            {
                entitywitch.setCustomNameTag(this.getCustomNameTag());
                entitywitch.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
            }

            this.world.spawnEntity(entitywitch);
            this.setDead();
        }
    }

    public InventoryBasic getVillagerInventory()
    {
        return this.villagerInventory;
    }

    /**
     * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
     * better.
     */
    protected void updateEquipmentIfNeeded(EntityItem itemEntity)
    {
        ItemStack itemstack = itemEntity.getItem();
        Item item = itemstack.getItem();

        if (this.canVillagerPickupItem(item))
        {
            ItemStack itemstack1 = this.villagerInventory.addItem(itemstack);

            if (itemstack1.isEmpty())
            {
                itemEntity.setDead();
            }
            else
            {
                itemstack.setCount(itemstack1.getCount());
            }
        }
    }

    private boolean canVillagerPickupItem(Item itemIn)
    {
    	return itemIn instanceof net.minecraftforge.common.IPlantable;
        // return itemIn == Items.BREAD || itemIn == Items.POTATO || itemIn == Items.CARROT || itemIn == Items.WHEAT || itemIn == Items.WHEAT_SEEDS || itemIn == Items.BEETROOT || itemIn == Items.BEETROOT_SEEDS;
    }

    public boolean hasEnoughFoodToBreed()
    {
    	return true; // return this.hasEnoughItems(1);
    }

    /**
     * Used by {@link net.minecraft.entity.ai.EntityAIVillagerInteract EntityAIVillagerInteract} to check if the
     * villager can give some items from an inventory to another villager.
     */
    public boolean canAbondonItems()
    {
    	return true;
        // return this.hasEnoughItems(2);
    }

    public boolean wantsMoreFood()
    {
    	return true;
//        boolean flag = this.getProfession() == 0;
//
//        if (flag)
//        {
//            return !this.hasEnoughItems(5);
//        }
//        else
//        {
//            return !this.hasEnoughItems(1);
//        }
    }

    /**
     * Returns true if villager has enough items in inventory
     */
    private boolean hasEnoughItems(int multiplier)
    {
    	return false;
//        boolean flag = this.getProfession() == 0;
//
//        for (int i = 0; i < this.villagerInventory.getSizeInventory(); ++i)
//        {
//            ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
//
//            if (!itemstack.isEmpty())
//            {
//                if ( )
//                {
//                    return true;
//                }
//            }
//        }
//
//        return false;
    }

    /**
     * Returns true if villager has seeds, potatoes or carrots in inventory
     */
    public boolean isFarmItemInInventory()
    {
        for (int i = 0; i < this.villagerInventory.getSizeInventory(); ++i)
        {
            ItemStack itemstack = this.villagerInventory.getStackInSlot(i);

            if ( !itemstack.isEmpty() && itemstack.getItem() instanceof net.minecraftforge.common.IPlantable ) // itemstack.getItem() instanceof ItemSeeds || itemstack.getItem() == Items.POTATO || itemstack.getItem() == Items.CARROT ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean replaceItemInInventory(int inventorySlot, ItemStack itemStackIn)
    {
        if (super.replaceItemInInventory(inventorySlot, itemStackIn))
        {
            return true;
        }
        else
        {
            int i = inventorySlot - 300;

            if (i >= 0 && i < this.villagerInventory.getSizeInventory())
            {
                this.villagerInventory.setInventorySlotContents(i, itemStackIn);
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
}