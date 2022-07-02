package net.torocraft.toroquest.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.ToroQuest;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.EntityGuard;
import net.torocraft.toroquest.entities.EntityToroVillager;

@Mod.EventBusSubscriber
public class ItemRecruitmentPapers extends Item
{

	public static ItemRecruitmentPapers INSTANCE;
	public static final String NAME = "recruitment_papers";
	private static ResourceLocation REGISTRY_NAME = new ResourceLocation(ToroQuest.MODID, NAME);

	@SubscribeEvent
	public static void init( final RegistryEvent.Register<Item> event )
	{
		INSTANCE = new ItemRecruitmentPapers();
		INSTANCE.setRegistryName(REGISTRY_NAME);
		event.getRegistry().register(INSTANCE);
	}

	public static void registerRenders()
	{
		ModelResourceLocation model = new ModelResourceLocation(ToroQuest.MODID + ":" + NAME, "inventory");
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(INSTANCE, 0, model);
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(INSTANCE, 0, new ModelResourceLocation(ToroQuest.MODID + ":" + NAME, "inventory"));
	}

	public ItemRecruitmentPapers()
	{
		setUnlocalizedName(NAME);
		this.maxStackSize = 16;
		this.setCreativeTab(CreativeTabs.MISC);
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void addInformation( ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn )
	{
		if ( ToroQuestConfiguration.recruitBandits )
		{
			if ( ToroQuestConfiguration.recruitVillagers )
			{
				tooltip.add(I18n.format("item.recruitment_papers.description0"));
				tooltip.add(I18n.format("item.recruitment_papers.description1"));
				
				tooltip.add(I18n.format("item.recruitment_papers.description2"));
			}
			else
			{
				tooltip.add(I18n.format("item.recruitment_papers.description0"));
				tooltip.add(I18n.format("item.recruitment_papers.description1"));
				
				tooltip.add(I18n.format("item.recruitment_papers.description4"));
			}
		}
		else
		{
			if ( ToroQuestConfiguration.recruitVillagers )
			{
				tooltip.add(I18n.format("item.recruitment_papers.description0"));
				tooltip.add(I18n.format("item.recruitment_papers.description1"));
				
				tooltip.add(I18n.format("item.recruitment_papers.description3"));
			}
			else
			{
				tooltip.add(I18n.format("item.recruitment_papers.description0"));
				tooltip.add(I18n.format("item.recruitment_papers.description1"));
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( World worldIn, EntityPlayer player, EnumHand hand )
	{
		if ( !player.isSneaking() || !ToroQuestConfiguration.recruitVillagers )
		{
			return super.onItemRightClick(worldIn, player, hand);
		}

		player.setSneaking(false);

		List<EntityVillager> villagers = player.world.getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(player.getPosition()).grow(1.25, 1.25, 1.25));

		for ( EntityVillager v : villagers )
		{
			Province province = CivilizationUtil.getProvinceAt(v.getEntityWorld(), v.getPosition().getX() / 16, v.getPosition().getZ() / 16);

			if ( province == null )
			{
				v.playSound(SoundEvents.ENTITY_VILLAGER_NO, 0.8F, 1.0F);
				return super.onItemRightClick(worldIn, player, hand);
			}

			if ( !(v instanceof EntityToroVillager) )
			{
				if ( !v.isEntityAlive() || v.isChild() )
				{
					break;
				}

				ItemStack itemstack = player.getHeldItem(hand);
				Item item = itemstack.getItem();

				if ( item.equals(Item.getByNameOrId("toroquest:recruitment_papers")) && PlayerCivilizationCapabilityImpl.get(player).getReputation(province.getCiv()) >= 0 )
				{
					if ( !v.world.isRemote )
					{
						v.playSound(SoundEvents.ENTITY_ILLAGER_CAST_SPELL, 1.2F, 1.2F);
						v.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 1.0F);
						v.playSound(SoundEvents.BLOCK_ANVIL_USE, 0.5F, 0.8F);

						itemstack.shrink(1);

						EntityGuard newEntity = new EntityGuard(v.world);
						newEntity.setPosition(v.posX, v.posY, v.posZ);
						newEntity.onInitialSpawn(v.world.getDifficultyForLocation(new BlockPos(v.getPosition())), (IEntityLivingData) null);
						newEntity.copyLocationAndAnglesFrom(v);
						newEntity.actionTimer = 1;

						v.setDead();
						v.world.spawnEntity(newEntity);

						newEntity.getLookHelper().setLookPositionWithEntity(player, 30.0F, 30.0F);
						newEntity.faceEntity(player, 30.0F, 30.0F);

						newEntity.recruitGuard(player, province, "civvillagerrecruit");
						newEntity.spawnedNearBandits = false;
					}
				}
				else
				{
					v.playSound(SoundEvents.ENTITY_VILLAGER_NO, 0.8F, 1.0F);
				}
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
			}
		}
		return super.onItemRightClick(worldIn, player, hand);
	}
}
