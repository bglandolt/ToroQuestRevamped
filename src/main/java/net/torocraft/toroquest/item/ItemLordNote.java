package net.torocraft.toroquest.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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
import net.torocraft.toroquest.civilization.quests.QuestRecruit;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.EntityGuard;
import net.torocraft.toroquest.entities.EntityToroVillager;

@Mod.EventBusSubscriber
public class ItemLordNote extends Item
{
	public static ItemLordNote INSTANCE;
	public static final String NAME = "lord_note";
	private static ResourceLocation REGISTRY_NAME = new ResourceLocation(ToroQuest.MODID, NAME);

	@SubscribeEvent
	public static void init( final RegistryEvent.Register<Item> event )
	{
		INSTANCE = new ItemLordNote();
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

	public ItemLordNote()
	{
		setUnlocalizedName(NAME);
		this.maxStackSize = 1;
		this.setCreativeTab(CreativeTabs.MISC);
	}

	// A detailed trade proposal requesting the exchange of several rare goods

	@Override
	@SideOnly( Side.CLIENT )
	public void addInformation( ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn )
	{
		tooltip.add("An important-looking envelope sealed with a royal wax stamp. §7[give this note to the recipient using the top-right box]§r");
	}
}
