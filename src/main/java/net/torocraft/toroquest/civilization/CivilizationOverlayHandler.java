package net.torocraft.toroquest.civilization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.torocraft.toroquest.civilization.player.PlayerCivilizationCapabilityImpl;
import net.torocraft.toroquest.configuration.ConfigurationHandler;
import net.torocraft.toroquest.util.Hud;
import net.torocraft.toroquest.util.ToroGuiUtils;

public class CivilizationOverlayHandler extends Hud
{
	String displayPosition = ConfigurationHandler.repDisplayPosition;
	public final int PADDING_FROM_EDGE_X = -8;
	public final int PADDING_FROM_EDGE_Y = -8;

	int screenWidth;
	int screenHeight;

	public static float PI = (float) Math.PI;
	public float titleTimer = PI;

	public CivilizationOverlayHandler( Minecraft mc )
	{
		super(mc, -ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, -ToroGuiUtils.DEFAULT_ICON_TEXTURE_HEIGTH);
	}

	@Override
	public void render( int screenWidth, int screenHeight )
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

	private void drawCurrentCivilizationIcon( Province civ, EntityPlayerSP player )
	{
		drawReputationText(civ, player);
		drawCivilizationBadge(civ.civilization);
		drawTitleText(civ, player);
	}

	private void drawReputationText( Province civ, EntityPlayerSP player )
	{
		int textX = determineTextX();
		int textY = determineIconY();

		ReputationLevel rep = PlayerCivilizationCapabilityImpl.get(player).getReputationLevel(civ.civilization);

		// LEGEND(), HERO(), CHAMPION(), EXALTED(), REVERED(), HONORED(), FRIENDLY(),
		// NEUTRAL(), UNFRIENDLY(), HOSTILE(), HATED(), EXILED();

		int color = 0xffffff;

		switch( rep )
		{

		case EXILED:
		{
			color = 0x550000;
			break;
		}
		case HATED:
		{
			color = 0xAA0000;
			break;
		}
		case HOSTILE:
		{
			color = 0xFF5555;
			break;
		}
		case UNFRIENDLY:
		{
			color = 0xFF5555;
			break;
		}
		case NEUTRAL:
		{
			color = 0xFFFF55;
			break;
		}
		case FRIENDLY:
		{
			color = 0x55FF55;
			break;
		}
		case HONORED:
		{
			color = 0x55FF55;
			break;
		}
		case RENOWNED:
		{
			color = 0x50ee60;
			break;
		}
		case EXALTED:
		{
			color = 0x00AA00;
			break;
		}
		case CHAMPION:
		{
			color = 0xFF55FF;
			break;
		}
		case HERO:
		{
			color = 0x55FFFF;
			break;
		}
		case LEGEND:
		{
			color = 0xFFAA00;
			break;
		}
		}

		if ( displayPosition.contains("RIGHT") )
		{
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
			if ( displayPosition.contains("TOP") )
			{
				textX += 10;
			}

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

	private void drawTitleText( Province civ, EntityPlayerSP entityPlayer )
	{
		int titleX = (screenWidth - ToroGuiUtils.DEFAULT_TITLE_TEXTURE_WIDTH) / 2;

		if ( titleTimer > 0.1F )
		{
			float alpha = MathHelper.clamp(MathHelper.sin(titleTimer) * 1.25F, 0.02F, 1.0F);
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
			GlStateManager.scale(2.0F, 2.0F, 2.0F);
			this.fontRenderer.drawString(civ.getName(), (screenWidth / 4.0F - this.fontRenderer.getStringWidth(civ.getName()) / 2.0F), 4, toHex(1.0F, 1.0F, 1.0F, alpha), true);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			ToroGuiUtils.drawOverlayTitle(mc, titleX, 8, ToroGuiUtils.DEFAULT_TITLE_TEXTURE_WIDTH, 0, ToroGuiUtils.DEFAULT_TITLE_TEXTURE_WIDTH, ToroGuiUtils.DEFAULT_TITLE_TEXTURE_HEIGTH);
			GlStateManager.popMatrix();
			titleTimer = titleTimer - 0.0075F;
		}
	}

	public static int toHex( float r, float g, float b, float a )
	{
		return (((int) (a * 255.0F) & 255) << 24) | (((int) (r * 255.0F) & 255) << 16) | (((int) (g * 255.0F) & 255) << 8) | ((int) (b * 255.0F) & 255);
	}

	public static float[] getRGBA( int color )
	{
		float a = (float) (color >> 24 & 255) / 255.0F;
		float r = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float b = (float) (color & 255) / 255.0F;
		return new float[]
		{
			r, g, b, a
		};
	}

	private void drawCivilizationBadge( CivilizationType civType )
	{
		int badgeX = determineBadgeX();
		int badgeY = determineIconY();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		ToroGuiUtils.drawOverlayIcon(mc, badgeX, badgeY, iconIndex(civType) * ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, 0, ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH, ToroGuiUtils.DEFAULT_ICON_TEXTURE_HEIGTH);
		GlStateManager.popMatrix();
	}

	private int determineTextX()
	{
		int x = PADDING_FROM_EDGE_X + ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH;

		if ( displayPosition.contains("RIGHT") )
		{
			x = screenWidth - PADDING_FROM_EDGE_X - ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH;
		}
		else
		{
			if ( displayPosition.contains("TOP") )
			{
				x -= 10;
			}
		}

		if ( displayPosition.contains("CENTER") )
		{
			x = (screenWidth + ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH + PADDING_FROM_EDGE_X) / 2;
		}

		return x + ConfigurationHandler.repDisplayX;
	}

	private int determineBadgeX()
	{
		int x = PADDING_FROM_EDGE_X;

		if ( displayPosition.contains("RIGHT") )
		{
			x = screenWidth - ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH;
		}
		else
		{
			x += 10;
		}

		if ( displayPosition.contains("CENTER") )
		{
			x = (screenWidth - ToroGuiUtils.DEFAULT_ICON_TEXTURE_WIDTH) / 2;
		}

		return x + ConfigurationHandler.repDisplayX;
	}

	private int determineIconY()
	{
		int y = PADDING_FROM_EDGE_Y;

		if ( displayPosition.contains("BOTTOM") )
		{
			y = screenHeight - ToroGuiUtils.DEFAULT_ICON_TEXTURE_HEIGTH;
		}
		else
		{
			y += 10;
		}

		return y + ConfigurationHandler.repDisplayY;
	}

	private int iconIndex( CivilizationType civ )
	{
		switch( civ )
		{
		case FIRE:
		{
			return 1;
		}
		case EARTH:
		{
			return 3;
		}
		case MOON:
		{
			return 2;
		}
		case SUN:
		{
			return 4;
		}
		case WIND:
		{
			return 5;
		}
		case WATER:
		{
			return 0;
		}
		default:
		{
			return 6;
		}
		}
	}
}
