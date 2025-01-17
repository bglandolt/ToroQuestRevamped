package net.torocraft.toroquest.entities.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.entities.EntityOrc;
import net.torocraft.toroquest.entities.model.Model64Layer;

@SideOnly( Side.CLIENT )
public class RenderOrc extends RenderBiped<EntityOrc>
{
	// private static ResourceLocation TEXTURES = new
	// ResourceLocation(ToroQuest.MODID + ":textures/entity/bandit/bandit.png");

	private final Model64Layer defaultModel = (Model64Layer) mainModel;

	public RenderOrc( RenderManager renderManagerIn )
	{
		super(renderManagerIn, new Model64Layer(), 0.6F);
		// LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this)
		// {
		// protected void initArmor()
		// {
		// this.modelLeggings = new Model64Layer(0.5F, false);
		// this.modelArmor = new Model64Layer(0.75F, false);
		// }
		// };
		// this.addLayer(layerbipedarmor);
		if ( ToroQuestConfiguration.renderArrowLayer )
		{
			this.addLayer(new LayerArrow(this));
		}
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	@Override
	public void doRender( EntityOrc entity, double x, double y, double z, float entityYaw, float partialTicks )
	{
		this.setModelVisibilities(entity);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	private void setModelVisibilities( EntityOrc entity )
	{
		ItemStack itemstack = entity.getHeldItemMainhand();
		ItemStack itemstack1 = entity.getHeldItemOffhand();

		defaultModel.setVisible(true);

		Model64Layer.ArmPose modelbiped$armpose = Model64Layer.ArmPose.EMPTY;
		Model64Layer.ArmPose modelbiped$armpose1 = Model64Layer.ArmPose.EMPTY;

		if ( itemstack != null )
		{
			modelbiped$armpose = Model64Layer.ArmPose.ITEM;

			if ( entity.getItemInUseCount() > 0 )
			{
				EnumAction enumaction = itemstack.getItemUseAction();

				if ( enumaction == EnumAction.BLOCK )
				{
					modelbiped$armpose = Model64Layer.ArmPose.BLOCK;
				}
				else if ( enumaction == EnumAction.BOW )
				{
					modelbiped$armpose = Model64Layer.ArmPose.BOW_AND_ARROW;
				}
			}
		}

		if ( itemstack1 != null )
		{
			modelbiped$armpose1 = Model64Layer.ArmPose.ITEM;

			if ( entity.getItemInUseCount() > 0 )
			{
				EnumAction enumaction1 = itemstack1.getItemUseAction();

				if ( enumaction1 == EnumAction.BLOCK )
				{
					modelbiped$armpose1 = Model64Layer.ArmPose.BLOCK;
				}
			}
		}

		// if (entity.getPrimaryHand() == EnumHandSide.RIGHT)
		{
			defaultModel.rightArmPose = modelbiped$armpose;
			defaultModel.leftArmPose = modelbiped$armpose1;
		}
		// else
		// {
		// defaultModel.rightArmPose = modelbiped$armpose1;
		// defaultModel.leftArmPose = modelbiped$armpose;
		// }

	}

	@Override
	protected void preRenderCallback( EntityOrc entitylivingbaseIn, float partialTickTime )
	{
		GlStateManager.scale(entitylivingbaseIn.getRenderSizeXZ(), entitylivingbaseIn.getRenderSizeY(), entitylivingbaseIn.getRenderSizeXZ());
	}

	@Override
	protected ResourceLocation getEntityTexture( EntityOrc entity )
	{
		return entity.getSkin();
	}
}