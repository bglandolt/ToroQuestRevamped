package net.torocraft.toroquest.entities;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.civilization.CivilizationType;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.ai.AIHelper;
import net.torocraft.toroquest.entities.ai.EntityAIRaid;

public class EntityToroNpc extends EntityCreature
{	
	// -------------------------------------------------------------------
	public EntityPlayer annoyedAt = null;
	public int isAnnoyedTimer = 0;
	public EntityPlayer underAttack = null;
	public int underAttackTimer = 0;
	public EntityPlayer murderWitness = null;
	public int murderTimer = 0;
	public int actionTimer = 5;
	public boolean inCombat = false;
	public float capeAni = 0;
	public boolean capeAniUp = true;
	public boolean interactTalkReady = true;
	public boolean returningToPost = false;
	EntityPlayer talkingWith = null;
	protected boolean hitSafety = true;
	public Random rand = new Random();
	// -------------------------------------------------------------------
	
	public void playChatSound()
	{
		this.playSound( SoundEvents.VINDICATION_ILLAGER_AMBIENT, 1.0F, 0.9F + rand.nextFloat()/5.0F );
		// SoundHandler
	}
	
	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.getDataManager().register(CIV, String.valueOf(""));
		this.getDataManager().register(PROV_ID, String.valueOf(""));
		this.getDataManager().register(RAID_X, Integer.valueOf(0));
		this.getDataManager().register(RAID_Y, Integer.valueOf(0));
		this.getDataManager().register(RAID_Z, Integer.valueOf(0));
	}
	
	public static DataParameter<Integer> RAID_X = EntityDataManager.<Integer>createKey(EntityToroNpc.class, DataSerializers.VARINT);
	public static DataParameter<Integer> RAID_Y = EntityDataManager.<Integer>createKey(EntityToroNpc.class, DataSerializers.VARINT);
	public static DataParameter<Integer> RAID_Z = EntityDataManager.<Integer>createKey(EntityToroNpc.class, DataSerializers.VARINT);

	/* set raid location to -1 if there is no valid y.
	 * If y is 0, then it is an invalid raid location.
	 */
	protected void setRaidLocation(int x, int y, int z)
	{
		this.getDataManager().set(RAID_X, x);
		this.getDataManager().set(RAID_Y, y);
		this.getDataManager().set(RAID_Z, z);
	}
	
	public Integer getRaidLocationX()
	{
		return this.getDataManager().get(RAID_X);
	}
	
	public Integer getRaidLocationY()
	{
		return this.getDataManager().get(RAID_Y);
	}
	
	public Integer getRaidLocationZ()
	{
		return this.getDataManager().get(RAID_Z);
	}
	
//	public boolean raidLocationNotSet()
//	{
//		return ( this.getDataManager().get(RAID_X) == 0 && this.getDataManager().get(RAID_Y) == 0 && this.getDataManager().get(RAID_Z) == 0 );
//	}
	
	// ========================= EntityToroNpc ===========================
	public EntityToroNpc(World worldIn, Province prov)
	{
		super(worldIn);
		this.enablePersistence();
		this.setSize(0.6F, 1.9F);
		this.experienceValue = 30;
		Arrays.fill(inventoryHandsDropChances, ToroQuestConfiguration.guardHandsDropChance);
		Arrays.fill(inventoryArmorDropChances, ToroQuestConfiguration.guardArmorDropChance);
		this.detachHome();
		this.setCanPickUpLoot(false);
		this.setLeftHanded(false);
		((PathNavigateGround) this.getNavigator()).setBreakDoors(true);
		this.pledgeAllegianceIfUnaffiliated(false);
	}
		
	public Province getHomeProvince()
	{
		return CivilizationUtil.getProvinceFromUUID(this.world, this.getUUID());
	}
	
	public Province getStandingInProvince()
	{
		return CivilizationUtil.getProvinceAt(this.world, this.chunkCoordX, this.chunkCoordZ);
	}
	
	// ====================================================================
	
	// ======================== PLEDGE ALLEGIANCE =========================
	/* returns TRUE if allegiance is set by this method */
	protected boolean pledgeAllegianceIfUnaffiliated( boolean force )
	{
		if ( force || this.getCivilization() == null || this.getUUID() == null )
		{			
			if ( this.pledgeAllegiance(this.getStandingInProvince()) )
			{
				if ( force )
				{
					this.playTameEffect((byte)6);
			        this.world.setEntityState(this, (byte)6);
				}
		        return true;
			}
		}
		return false;
	}
	
	public boolean pledgeAllegianceTo( Province p )
	{
		if ( this.pledgeAllegiance(p) )
		{
			this.playTameEffect((byte)6);
		    this.world.setEntityState(this, (byte)6);
	        return true;
		}
		return false;
	}
	// ===
	
	/* returns true if the province is not null */
	protected boolean pledgeAllegiance( Province prov )
	{
		if ( prov != null )
		{
			this.setCivilization(prov.getCiv());
			this.setUUID(prov.getUUID());
			this.onPledge(prov);
			return true;
		}
		return false;
	}
	
	protected boolean pledgeAllegiance()
	{
		return this.pledgeAllegiance(this.getStandingInProvince());
	}
	
	protected void onPledge( Province prov )
	{

	}
	
    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        
        compound.setInteger("raidX", this.getRaidLocationX());
        compound.setInteger("raidY", this.getRaidLocationY());
        compound.setInteger("raidZ", this.getRaidLocationZ());
        
        compound.setString("provID", this.getDataManager().get(PROV_ID));
        compound.setString("civ", this.getDataManager().get(CIV));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        
        this.setRaidLocation(compound.getInteger("raidX"), compound.getInteger("raidY"), compound.getInteger("raidZ"));
        
        this.setCivilization(this.enumCiv(compound.getString("civ")));
        this.setUUID(this.enumUUID(compound.getString("provID")));

    }
	
	// ===================================================================
	
	// ========================== CIVILIZATION ===========================
	public static DataParameter<String> CIV = EntityDataManager.<String>createKey(EntityToroNpc.class, DataSerializers.STRING);
	
	public void setCivilization( @Nullable CivilizationType civ)
	{
		if ( civ == null )
		{
			this.getDataManager().set(CIV, "");
		}
		else
		{
			this.getDataManager().set(CIV, civ.toString());
		}
		//this.getDataManager().setDirty(CIV);
	}
	
	private CivilizationType civ = null;
	
	public CivilizationType getCivilization()
	{
		if ( this.civ != null )
		{
			return this.civ;
		}
		else
		{
			return this.civ = enumCiv(this.getDataManager().get(CIV));
		}
	}
	
    @Override
    protected void collideWithNearbyEntities()
    {
    	
    }
	
	protected CivilizationType enumCiv(String s)
	{
		try
		{
			CivilizationType civ = CivilizationType.valueOf(s);
			return civ;
		}
		catch ( Exception e )
		{
			return null;
		}
	}
	
//	public ResourceLocation getCivSkin()
//	{
//		return this.CIV_SKIN = new ResourceLocation(ToroQuest.MODID + ":textures/entity/guard/guard_null.png");
//	}
	
	// ===================================================================
	
	// ============================== UUID ===============================
	
	public static DataParameter<String> PROV_ID = EntityDataManager.<String>createKey(EntityToroNpc.class, DataSerializers.STRING);
	
	public void setUUID( @Nullable UUID uuid)
	{
		if ( uuid == null )
		{
			this.getDataManager().set(PROV_ID, "");
		}
		else
		{
			this.getDataManager().set(PROV_ID, uuid.toString());
		}
		//this.getDataManager().setDirty(PROV_ID);
	}
	
	private UUID prov_uuid = null;
	
	public UUID getUUID()
	{
		if ( this.prov_uuid != null )
		{
			return this.prov_uuid;
		}
		else
		{
			return this.prov_uuid = enumUUID(this.getDataManager().get(PROV_ID));
		}
	}
	
	protected UUID enumUUID(String s)
	{
		try
		{
			UUID uuid = UUID.fromString(s);
			return uuid;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	//==================================================== Return To Post ===========================================================

	public boolean returnToPost()
	{
		if ( this.hasPath() || this.getAttackTarget() != null )
		{
			return false;
		}
		
		int raid_y = this.getRaidLocationY();
		
		if ( raid_y == 0 )
		{
			return false;
		}
		
		int raid_x = this.getRaidLocationX();
		int raid_z = this.getRaidLocationZ();
		
		if ( raid_y > 0 && this.getNavigator().tryMoveToXYZ(raid_x, raid_y, raid_z, 0.6D) ) // try moving directly
		{
            AIHelper.faceEntitySmart(this, raid_x, raid_z);
			return true;
		}
		
		double x = raid_x - this.posX;
		double z = raid_z - this.posZ;
		
		double xz = Math.abs(x) + Math.abs(z);
		
		if ( xz <= 3 ) // if near post, do nothing
		{
			if ( raid_y > 0 && Math.abs(this.posY-raid_y) >= 3 ) // unless too far from y
			{
				BlockPos moveTo = EntityAIRaid.findValidSurface(this.world, new BlockPos(raid_x, this.posY, raid_z), 8);
				
				if ( moveTo != null )
				{
					if ( this.getNavigator().tryMoveToXYZ(moveTo.getX(), moveTo.getY(), moveTo.getZ(), 0.6D) )
					{
			            AIHelper.faceEntitySmart(this, moveTo.getX(), moveTo.getZ());
						return true;
					}
				}
			}
			return this.returningToPost = false;
		}
		
		x = x/xz * 16 + this.posX;
		z = z/xz * 16 + this.posZ;
		
		BlockPos moveTo = EntityAIRaid.findValidSurface(this.world, new BlockPos(x, this.posY, z), 8);
		
		if ( moveTo != null )
		{
			if ( this.getNavigator().tryMoveToXYZ(moveTo.getX(), moveTo.getY(), moveTo.getZ(), 0.6D) )
			{
	            AIHelper.faceEntitySmart(this, moveTo.getX(), moveTo.getZ());
				return true;
			}
		}
				
		Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this, 16, 8, new Vec3d(x,this.posY,z));
		
		if ( vec3d == null || !this.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, 0.6D) )
        {
			vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this, 8, 8, new Vec3d(x,this.posY,z));
			
			if ( vec3d == null || !this.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, 0.6D ) )
			{
				vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this, 20, 8, new Vec3d(x,this.posY,z));
				
				if ( vec3d == null || !this.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, 0.6D ) )
				{
					return false;
				}
			}
        }
		
        AIHelper.faceEntitySmart(this, (int)vec3d.x, (int)vec3d.z);
		return true;
	}
	
	// ===================================================================
	
	// ============================ PROVINCE =============================
//	public static DataParameter<String> PROV = EntityDataManager.<String>createKey(EntityToroNpc.class, DataSerializers.STRING);
//
//	public void setProvince( @Nullable String prov )
//	{
//		if ( prov == null )
//		{
//			this.getDataManager().set(PROV, "");
//		}
//		else
//		{
//			this.getDataManager().set(PROV, prov.toString());
//		}
//		this.getDataManager().setDirty(PROV);
//	}
//	
//	public String getProvince()
//	{
//		return (this.getDataManager().get(PROV));
//	}
	// ===================================================================

	@Override
	public void onLivingUpdate()
	{
		this.updateArmSwingProgress();
		super.onLivingUpdate();
	}

	public boolean attackEntityAsMob(Entity entityIn)
	{
		float f = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		int knockback = 0;

		if (entityIn instanceof EntityLivingBase)
		{
			f += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase) entityIn).getCreatureAttribute());
			knockback += EnchantmentHelper.getKnockbackModifier(this);
		}

		boolean successfulAttack = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

		if (successfulAttack) {
			handleSuccessfulAttack(entityIn, knockback);
		}

		return successfulAttack;
	}

	protected void handleSuccessfulAttack(Entity entityIn, int knockback)
	{
		if (knockback > 0 && entityIn instanceof EntityLivingBase)
		{
			((EntityLivingBase) entityIn).knockBack(this, (float) knockback * 0.5F, (double) MathHelper.sin(this.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
			this.motionX *= 0.6D;
			this.motionZ *= 0.6D;
		}

		int j = EnchantmentHelper.getFireAspectModifier(this);

		if (j > 0)
		{
			entityIn.setFire(j * 4);
		}

		if (entityIn instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer) entityIn;
			ItemStack itemstack = this.getHeldItemMainhand();
			ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : null;

			if ( itemstack != null && itemstack1 != null && itemstack.getItem() instanceof ItemAxe && itemstack1.getItem() instanceof ItemShield )
			{
				float f1 = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

				if (this.rand.nextFloat() < f1) {
					entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
					this.world.setEntityState(entityplayer, (byte) 30);
				}
			}
		}

		this.applyEnchantments(this, entityIn);
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
	}
	
	//============================= Friendly =============================
	public boolean isFriendly( EntityPlayer player)
	{
		return ( this.murderWitness == null || this.murderWitness != player ) && ( this.underAttack == null || this.underAttack != player ) && ( this.annoyedAt == null || this.annoyedAt != player );
	}
	//====================================================================

	// ============================= MISC ================================
	@Override
	public SoundCategory getSoundCategory()
	{
		return SoundCategory.NEUTRAL;
	}
	
	public boolean actionReady()
	{
		return this.actionTimer < 1;
	}
	
	public boolean isAnnoyed()
	{
		return this.isAnnoyedTimer > 0;
	}
	
	public int actionTimer()
	{
		return this.actionTimer;
	}
	
	public void setActionTimer(int n)
	{
		this.actionTimer = n;
	}
	
	public void setAnnoyed( EntityPlayer player )
	{
		if ( this.isAnnoyed() )
		{
			this.isAnnoyedTimer = 8;
		}
		else
		{
			this.isAnnoyedTimer = 4;
		}
		this.annoyedAt = player;
	}
	
	public boolean isAnnoyedAt( EntityPlayer player )
	{
		return ( this.isAnnoyed() && this.annoyedAt != null && this.annoyedAt == player );
	}
	
	public void setUnderAttack( EntityPlayer player )
	{
		this.setAnnoyed( player );
		this.underAttack = player;
		this.underAttackTimer = 16;
	}
	
	public void setMurder( EntityPlayer player )
	{
		this.setUnderAttack( player );
		this.murderWitness = player;
		this.murderTimer = 64;
	}
	
	public EntityPlayer murderWitness()
	{
		return this.murderWitness;
	}
	
	public EntityPlayer underAttack()
	{
		return this.underAttack;
	}
	
	public boolean inCombat()
	{
		return this.getAttackTarget() != null || this.getRevengeTarget() != null || this.inCombat;
	}
	
	@Override
	public boolean hasHome()
	{
		return false;
	}
	
	@Override
	public BlockPos getHomePosition()
    {
		return null;
    }
	
	@Override
    protected float getWaterSlowDown()
    {
        return 0.9F;
    }
	
    public boolean startRiding(Entity entityIn, boolean force)
    {
    	return false;
    }
	
	public void chat( EntityToroNpc guard, EntityPlayer player, String message, @Nullable String extra )
	{
		return;
	}
	
	public void callForHelp( EntityLivingBase attacker )
	{
		return;
	}
    
	@Override
	public int getHorizontalFaceSpeed()
	{
		return 10;
	}
	
	@Override
	protected SoundEvent getSwimSound()
	{
		return SoundEvents.ENTITY_HOSTILE_SWIM;
	}

	@Override
	protected SoundEvent getSplashSound()
	{
		return SoundEvents.ENTITY_HOSTILE_SPLASH;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_HOSTILE_DEATH;
	}
	
	@Override
	protected SoundEvent getFallSound(int heightIn)
	{
		return heightIn > 4 ? SoundEvents.ENTITY_HOSTILE_BIG_FALL : SoundEvents.ENTITY_HOSTILE_SMALL_FALL;
	}

	@Override
	protected boolean canDropLoot()
	{
		return true;
	}
	
	@Override
	protected boolean canDespawn()
	{
		return false;
	}
	
	@Override
	protected void updateLeashedState()
	{
	   this.clearLeashed(true, false);
	   return;
	}
		
	@Override
	public boolean canBeLeashedTo(EntityPlayer player)
	{
		return false;
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id)
    {
		if (id == 8)
        {
            this.playTameEffect(id);
        }
        if (id == 7)
        {
            this.playTameEffect(id);
        }
        else if (id == 6)
        {
            this.playTameEffect(id);
        }
        super.handleStatusUpdate(id);
    }
	
	public void playTameEffect(byte id)
    {
        EnumParticleTypes enumparticletypes = EnumParticleTypes.HEART;

        if (id == 6 )
        {
            enumparticletypes = EnumParticleTypes.SMOKE_NORMAL;
        }
        else if (id == 7)
        {
        	enumparticletypes = EnumParticleTypes.VILLAGER_ANGRY;
        }

        for (int i = 0; i < 7; ++i)
        {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
        }
    }
	
//	@Override
//	public boolean attackEntityFrom(DamageSource source, float amount)
//	{
//		if ( super.attackEntityFrom(source, amount) )
//		{
//			if ( ToroQuestConfiguration.enableBloodParticles )
//			{
//				int a = (int)MathHelper.clamp(Math.sqrt(amount-1), 0, 8);
//				if ( a > 0 ) this.spawnHitParticles(a);
//			}
//			return true;
//		}
//		return false;
//	}
	
//	public void spawnHitParticles( int amount )
//	{
//		double xx = this.posX + -MathHelper.sin(this.rotationYaw * 0.017453292F)/16.0D;
//		double yy = this.posY + 0.5D + this.height * 0.5D;
//		double zz = this.posZ + MathHelper.cos(this.rotationYaw * 0.017453292F)/16.0D;
//
//		if (this.world instanceof WorldServer)
//		{
//			for ( int i = (int)amount; i > 0; i-- )
//			{
//				((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE, xx+this.rand.nextGaussian()/10.0D, yy-this.rand.nextDouble()/4.0D, zz+this.rand.nextGaussian()/10.0D, 0, 0, 0, 0, 0.4D, new int[0]);
//			}
//		}
//	}
	
	// ===================================================================
	
	@Override
	public EnumCreatureAttribute getCreatureAttribute()
	{
	    return EnumCreatureAttribute.ILLAGER;
	}
	
}
