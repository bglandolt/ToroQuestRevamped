package net.torocraft.toroquest.item.armor;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.ToroQuest;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.CivilizationsWorldSaveData;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.EntityGuard;
import net.torocraft.toroquest.entities.EntityVillageLord;
import net.torocraft.toroquest.material.ArmorMaterials;

@Mod.EventBusSubscriber
public class ItemRoyalArmor extends ItemArmor
{

	public static final String NAME = "royal";

	public static ItemRoyalArmor helmetItem;
	// public static ItemRoyalArmor chestplateItem;
	// public static ItemRoyalArmor leggingsItem;
	// public static ItemRoyalArmor bootsItem;

	@SubscribeEvent
	public static void init( final RegistryEvent.Register<Item> event )
	{
		helmetItem = new ItemRoyalArmor(NAME + "_helmet", 1, EntityEquipmentSlot.HEAD);
		helmetItem.setRegistryName(new ResourceLocation(ToroQuest.MODID, NAME + "_helmet"));
		event.getRegistry().register(helmetItem);
	}

	@SideOnly( Side.CLIENT )
	public void addInformation( ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn )
	{
		if ( ToroQuestConfiguration.useCrownToCreateNewProvinces )
		{
			tooltip.add("Crown a Guard within a civilization to create a new Village Lord, or crown a Guard to create a new province under a new ruler!\n\n§oWho did you kill to get this crown, Kingslayer?");
		}
		else
		{
			tooltip.add("Crown a Guard within a civilization to create a new Village Lord!\n\n§oWho did you kill to get this crown, Kingslayer?");
		}
	}

	public static void registerRenders()
	{
		registerRendersHelmet();
	}

	private static void registerRendersHelmet()
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(helmetItem, 0, model("helmet"));
	}

	private static ModelResourceLocation model( String model )
	{
		return new ModelResourceLocation(ToroQuest.MODID + ":" + NAME + "_" + model, "inventory");
	}

	public ItemRoyalArmor( String unlocalizedName, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn )
	{
		super(ArmorMaterials.ROYAL, renderIndexIn, equipmentSlotIn);
		this.setUnlocalizedName(unlocalizedName);
		setMaxDamage(-1);
	}

	@Override
	public boolean getIsRepairable( ItemStack toRepair, ItemStack repair )
	{
		return false;
		// ItemStack mat = new ItemStack( Items.GOLD_INGOT, 1 );
		// if (!mat.isEmpty() &&
		// net.minecraftforge.oredict.OreDictionary.itemMatches(mat,repair,false))
		// return true;
		// return super.getIsRepairable(toRepair, repair);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( World worldIn, EntityPlayer playerIn, EnumHand handIn )
	{
		if ( playerIn == null || playerIn.world == null || worldIn.isRemote )
		{
			return super.onItemRightClick(worldIn, playerIn, handIn);
		}

		ItemStack i = playerIn.getHeldItem(handIn);

		if ( i != null && !i.isEmpty() )
		{
			if ( playerIn.dimension == 0 )
			{
				List<EntityGuard> guards = playerIn.world.getEntitiesWithinAABB(EntityGuard.class, new AxisAlignedBB(playerIn.getPosition()).grow(3, 3, 3));

				if ( !guards.isEmpty() )
				{
					Province provinceOn = CivilizationUtil.getProvinceAt(playerIn.world, playerIn.chunkCoordX, playerIn.chunkCoordZ);

					if ( provinceOn != null )
					{
						// if ( !worldIn.isRemote )
						// {
						// return super.onItemRightClick(worldIn, playerIn, handIn);
						// }

						if ( provinceOn.hasLord )
						{
							playerIn.sendStatusMessage(new TextComponentString("§oThis province already has a ruler!§r"), true);
							return new ActionResult<ItemStack>(EnumActionResult.FAIL, i);
						}
						else if ( PlayerCivilizationCapabilityImpl.get(playerIn).getReputation(provinceOn.getCiv()) >= 0 )
						{
							for ( EntityGuard guard : guards )
							{

								EntityVillageLord vl = new EntityVillageLord(guard.getEntityWorld(), guard.getPosition().getX(), guard.getPosition().getY(), guard.getPosition().getZ());

								guard.setHealth(0);
								guard.setDead();

								// if ( !worldIn.isRemote )
								{
									vl.setPosition(guard.posX, guard.posY, guard.posZ);
									vl.addArmor();
									worldIn.spawnEntity(vl);
									vl.playTameEffect((byte) 6);
									worldIn.setEntityState(vl, (byte) 6);
									vl.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 1.0F, 0.8F);
									vl.playSound(SoundEvents.BLOCK_ANVIL_USE, 0.8F, 0.8F);
									vl.playSound(SoundEvents.ENTITY_EVOCATION_ILLAGER_AMBIENT, 1.0F, 1.0F);

									for ( EntityPlayer player : worldIn.playerEntities )
									{
										player.sendMessage(new TextComponentString("§l" + provinceOn.getName() + " has been founded!§r"));
									}
								}

								vl.pledgeAllegianceTo(provinceOn);

								return new ActionResult<ItemStack>(EnumActionResult.FAIL, ItemStack.EMPTY);
							}
							return new ActionResult<ItemStack>(EnumActionResult.FAIL, i);
						}
					}
					else if ( ToroQuestConfiguration.useCrownToCreateNewProvinces )
					{
						Province provinceNear = CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX + 6, playerIn.chunkCoordZ + 6);
						if ( provinceNear == null )
						{
							provinceNear = CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX + 6, playerIn.chunkCoordZ - 6);
						}
						if ( provinceNear == null )
						{
							provinceNear = CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX - 6, playerIn.chunkCoordZ + 6);
						}
						if ( provinceNear == null )
						{
							provinceNear = CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX - 6, playerIn.chunkCoordZ - 6);
						}

						if ( provinceNear == null )
						{
							provinceNear = CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX + 7, playerIn.chunkCoordZ);
						}
						if ( provinceNear == null )
						{
							provinceNear = CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX, playerIn.chunkCoordZ + 7);
						}
						if ( provinceNear == null )
						{
							provinceNear = CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX - 7, playerIn.chunkCoordZ);
						}
						if ( provinceNear == null )
						{
							provinceNear = CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX, playerIn.chunkCoordZ - 7);
						}

						if ( provinceNear != null )
						{
							playerIn.sendStatusMessage(new TextComponentString("§oToo close to another province!§r"), true);
							return new ActionResult<ItemStack>(EnumActionResult.FAIL, i);
						}

						for ( EntityGuard guard : guards )
						{
							if ( CivilizationUtil.getProvinceAt(worldIn, playerIn.chunkCoordX, playerIn.chunkCoordZ) == null )
							{
								EntityVillageLord vl = new EntityVillageLord(guard.getEntityWorld(), guard.getPosition().getX(), guard.getPosition().getY(), guard.getPosition().getZ());
								CivilizationsWorldSaveData.get(worldIn).register(playerIn.chunkCoordX, playerIn.chunkCoordZ, false);
								provinceOn = CivilizationUtil.getProvinceAt(worldIn, guard.chunkCoordX, guard.chunkCoordZ);

								// if ( provinceOn != null )
								{
									guard.setDead();
									guard.setHealth(0);

									if ( !worldIn.isRemote )
									{
										vl.setPosition(guard.posX, guard.posY, guard.posZ);
										vl.addArmor();
										worldIn.spawnEntity(vl);
										vl.playTameEffect((byte) 6);
										worldIn.setEntityState(vl, (byte) 6);
										vl.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 1.0F, 0.8F);
										vl.playSound(SoundEvents.BLOCK_ANVIL_USE, 0.8F, 0.8F);
										vl.playSound(SoundEvents.ENTITY_EVOCATION_ILLAGER_AMBIENT, 1.0F, 0.9F);
										// playerIn.sendStatusMessage(new TextComponentString( "§oProvince founded!§r"
										// ), true);

										for ( EntityPlayer player : worldIn.playerEntities )
										{
											player.sendMessage(new TextComponentString("§l" + provinceOn.getName() + " has been founded!§r"));
										}

										vl.pledgeAllegianceTo(provinceOn);
									}

									return new ActionResult<ItemStack>(EnumActionResult.FAIL, ItemStack.EMPTY);
								}
							}
							return new ActionResult<ItemStack>(EnumActionResult.FAIL, i);
						}
						return new ActionResult<ItemStack>(EnumActionResult.FAIL, i);
					}
				}
			}
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, i);
	}

}
