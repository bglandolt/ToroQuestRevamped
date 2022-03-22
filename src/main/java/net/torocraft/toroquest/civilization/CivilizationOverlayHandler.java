package net.torocraft.toroquest.civilization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.configuration.ConfigurationHandler;
import net.torocraft.toroquest.util.Hud;
import net.torocraft.toroquest.util.ToroGuiUtils;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

public class CivilizationOverlayHandler extends Hud
{

	// ToroGuiUtils
	
	String displayPosition;
	public final int PADDING_FROM_EDGE_X = -8;
	public final int PADDING_FROM_EDGE_Y = -8;

	int screenWidth;
	int screenHeight;

	public CivilizationOverlayHandler(Minecraft mc)
	{
		super(mc, -ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, -ToroGuiUtils.DEFAULT_ICON_TEXTURE_HEIGTH); // + PADDING_FROM_EDGE??
	}

	@Override
	public void render(int screenWidth, int screenHeight)
	{
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		EntityPlayerSP player = mc.player;

		if ( player.dimension != 0 )
		{
			titleTimer = PI;
			return;
		}

		Province civ = PlayerCivilizationCapabilityImpl.get(player).getInCivilization();

		if ( civ == null || civ.civilization == null )
		{
			titleTimer = PI;
			return;
		}

		displayPosition = ConfigurationHandler.repDisplayPosition;

		if ( "OFF".equals(displayPosition) )
		{
			titleTimer = PI;
			return;
		}

		drawCurrentCivilizationIcon(civ, player);
	}

	private void drawCurrentCivilizationIcon(Province civ, EntityPlayerSP player)
	{
		drawReputationText(civ, player);
		drawCivilizationBadge(civ.civilization);
		drawTitleText(civ, player);
		// XXX
		// could display how much rep you lost
	}
	
	//public static void drawRect(int left, int top, int right, int bottom, int color) {

	
//	@SideOnly(Side.CLIENT)
//	@SubscribeEvent
//	public void handleEnterProvince(CivilizationEvent.Enter event)
//	{
//		if ( event.getEntityPlayer() != null && event.getEntityPlayer().dimension == 0 && ToroQuestConfiguration.showProvinceEnterTitle )
//		{
//			if ( event.getEntityPlayer().ticksExisted - displayCapture >= displayWait )
//			{
//				displayCapture = event.getEntityPlayer().ticksExisted;
//				String subTitle = "House " + event.province.getCiv().getLocalizedName();
//				Minecraft.getMinecraft().ingameGUI.displayTitle(null, TextFormatting.ITALIC + subTitle, 0, 0, 0);
//				Minecraft.getMinecraft().ingameGUI.displayTitle(TextFormatting.BOLD + event.province.getName(), subTitle, timeFadeIn, displayTime, timeFadeOut); // with TextFormatting
//				// Minecraft.getMinecraft().ingameGUI.displayTitle(TextFormatting.BOLD + event.province.getName() + TextFormatting.UNDERLINE, subTitle, timeFadeIn, displayTime, timeFadeOut); // with TextFormatting
//			}
//		}
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	private void drawReputationText(Province civ, EntityPlayerSP player)
	{
		int textX = determineTextX();
		int textY = determineIconY();
		
		ReputationLevel rep = PlayerCivilizationCapabilityImpl.get(player).getReputationLevel(civ.civilization);
		
		// LEGEND(), HERO(), CHAMPION(), EXALTED(), REVERED(), HONORED(), FRIENDLY(), NEUTRAL(), UNFRIENDLY(), HOSTILE(), HATED(), EXILED();
		
		int color = 0xffffff;
		
		switch ( rep )
		{
		
			case EXILED:
			{
				color = 0x550000;
				break;
			}
			case HATED:
			{
				color = 0xee1122;
				break;
			}
			case HOSTILE:
			{
				color = 0xff5555;
				break;
			}
			case UNFRIENDLY:
			{
				color = 0xffaaaa;
				break;
			}
			case NEUTRAL:
			{
				color = 0xffeeaa;
				break;
			}
			case FRIENDLY:
			{
				color = 0xbbeebb;
				break;
			}
			case HONORED:
			{
				color = 0x99eea0;
				break;
			}
			case REVERED:
			{
				color = 0x50ee60;
				break;
			}
			case EXALTED:
			{
				color = 0x00cc25;
				break;
			}
			case CHAMPION:
			{
				color = 0x99eeff;
				break;
			}
			case HERO:
			{
				color = 0xff88dd;
				break;
			}
			case LEGEND:
			{
				color = 0xffaa00;
				break;
			}
		}
			
		if (displayPosition.contains("RIGHT"))
		{
			//textY -= 20;
			textX -= 10;
			drawRightString("§lHouse " + civ.civilization.getLocalizedName(), textX, textY, 0xffffff);
			textY += 10;
			
			// 1
			drawRightString(civ.name, textX, textY, 0xffffff);

			textY += 10;
			
			// 2
			drawRightString(rep.getLocalname(), textX, textY, color);
			textY += 10;
			
			// 3
			drawRightString("Reputation: " + Integer.toString(PlayerCivilizationCapabilityImpl.get(player).getReputation(civ.civilization), 10), textX, textY, 0xffffff);
		}
		else
		{
			if (displayPosition.contains("TOP"))
			{
				textX += 10;
			}
			//textY -= 20;
			textX += 12;
			drawString("§lHouse " + civ.civilization.getLocalizedName(), textX, textY, 0xffffff);
			textY += 10;
			
			// 1
			drawString(civ.name, textX, textY, 0xffffff);

			textY += 10;

			// 2
			drawString(PlayerCivilizationCapabilityImpl.get(player).getReputationLevel(civ.civilization).getLocalname(), textX, textY, color);
			textY += 10;
			
			// 3
			drawString("Reputation: " + Integer.toString(PlayerCivilizationCapabilityImpl.get(player).getReputation(civ.civilization), 10), textX, textY, 0xffffff);
		}
	}
	
	// XXX
	
	public static float PI = (float)Math.PI;

	public float titleTimer = PI;
	private void drawTitleText(Province civ, EntityPlayerSP entityPlayer)
	{
		int titleX = (screenWidth - ToroGuiUtils.DEFAULT_TITLE_TEXTURE_WIDTH)/2;
		
		if ( titleTimer > 0.1F )
		{
			float alpha = MathHelper.clamp(MathHelper.sin(titleTimer)*1.25F,0.02F,1.0F);
			
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
			GlStateManager.scale(2.0F, 2.0F, 2.0F);
			// this.drawCenteredString(civ.getName(), titleX, 4, toHex(1.0F, 1.0F, 1.0F, alpha));
			// int textwidth = (int)(font.getStringBounds(civ.getName(), frc).getWidth());
			// this.drawString(civ.getName(), titleX, 4, toHex(1.0F, 1.0F, 1.0F, alpha));
		    this.fontRenderer.drawString(civ.getName(), (screenWidth/4.0F-this.fontRenderer.getStringWidth(civ.getName())/2.0F), 4, toHex(1.0F, 1.0F, 1.0F, alpha), true);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			ToroGuiUtils.drawOverlayTitle(mc, titleX, 8, ToroGuiUtils.DEFAULT_TITLE_TEXTURE_WIDTH, 0, ToroGuiUtils.DEFAULT_TITLE_TEXTURE_WIDTH, ToroGuiUtils.DEFAULT_TITLE_TEXTURE_HEIGTH);
			// ToroGuiUtils.drawOverlayIcon(mc, 32, 32, ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, 0, ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, ToroGuiUtils.DEFAULT_ICON_TEXTURE_HEIGTH);
			GlStateManager.popMatrix();

			titleTimer = titleTimer - 0.0075F;
		}
	}
	
	public static int toHex(float r, float g, float b, float a)
	{
		return (((int)(a * 255.0F) & 255) << 24) | (((int)(r * 255.0F) & 255) << 16) | (((int)(g * 255.0F) & 255) << 8) | ((int)(b * 255.0F) & 255);
	}

	public static float[] getRGBA(int color)
	{
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		return new float[] {r, g, b, a};
	}

	private void drawCivilizationBadge(CivilizationType civType)
	{
		int badgeX = determineBadgeX();
		int badgeY = determineIconY();
				
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		//GlStateManager.disableTexture2D();
		// GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		//GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// GlStateManager.color(1F, 1F, 1F, alpha);
		ToroGuiUtils.drawOverlayIcon(mc, badgeX, badgeY, ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, 0, ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, ToroGuiUtils.DEFAULT_ICON_TEXTURE_HEIGTH);
		GlStateManager.popMatrix();
		
//		GlStateManager.pushAttrib();
//		GlStateManager.disableDepth();
//		GlStateManager.enableBlend();
//		
//		ToroGuiUtils.drawOverlayIcon(mc, badgeX, badgeY, iconIndex(civType)*ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, 0, ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, ToroGuiUtils.DEFAULT_ICON_TEXTURE_HEIGTH);
//		
//		GlStateManager.popAttrib();
//		GlStateManager.enableDepth();
//		GlStateManager.disableBlend();
	}

	private int determineTextX()
	{
		int x = PADDING_FROM_EDGE_X + ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH;

		if (displayPosition.contains("RIGHT"))
		{
			x = screenWidth - PADDING_FROM_EDGE_X - ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH;
		}
		else
		{
			if (displayPosition.contains("TOP"))
			{
				x -= 10;
			}
		}

		if (displayPosition.contains("CENTER"))
		{
			x = (screenWidth + ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH + PADDING_FROM_EDGE_X) / 2;
		}

		return x + ConfigurationHandler.repDisplayX;
	}

	private int determineBadgeX()
	{
		int x = PADDING_FROM_EDGE_X;

		if (displayPosition.contains("RIGHT"))
		{
			x = screenWidth - ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH;
//			if (displayPosition.contains("TOP"))
//			{
//				x -= 10;
//			}
		}
		else
		{
			x += 10;
		}

		if (displayPosition.contains("CENTER"))
		{
			x = (screenWidth - ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH) / 2;
		}

		return x + ConfigurationHandler.repDisplayX;
	}

	private int determineIconY()
	{
		int y = PADDING_FROM_EDGE_Y;

		if (displayPosition.contains("BOTTOM"))
		{
			y = screenHeight - ToroGuiUtils.DEFAULT_ICON_TEXTURE_HEIGTH;
		}
		else
		{
			y += 10;
		}
		
		return y + ConfigurationHandler.repDisplayY;
	}

//	private int iconIndex(CivilizationType civ)
//	{
//		switch (civ)
//		{
//			case FIRE:
//			{
//				return 1;
//			}
//			case EARTH:
//			{
//				return 3;
//			}
//			case MOON:
//			{
//				return 2;
//			}
//			case SUN:
//			{
//				return 4;
//			}
//			case WIND:
//			{
//				return 5;
//			}
//			case WATER:
//			{
//				return 0;
//			}
//			default:
//			{
//				return 6;
//			}
//		}
//	}

}

//int i = PlayerCivilizationCapabilityImpl.get(player).getReputation(civ.civilization);
//if ( i < 0 )
//{
//	drawString( "Bounty: " + Integer.toString(PlayerCivilizationCapabilityImpl.get(player).getReputation(civ.civilization), 10), textX, textY,
//	0xffffff);
//}
//else
//{drawString("Reputation: " + Integer.toString(PlayerCivilizationCapabilityImpl.get(player).getReputation(civ.civilization), 10), textX, textY,
//		0xffffff);}

//drawRightString(Integer.toString(PlayerCivilizationCapabilityImpl.get(player).getReputation(civ.civilization), 10) + " Rep", textX, textY,
//		0xffffff);
//textY += 10;
//
//drawRightString(PlayerCivilizationCapabilityImpl.get(player).getReputationLevel(civ.civilization).getLocalname(), textX, textY, 0xffffff);
//textY += 10;
