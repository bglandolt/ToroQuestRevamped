package net.torocraft.toroquest.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ToroGuiUtils
{

	/* CivilizationOverlayHandler */

	public static final ResourceLocation ICONS = new ResourceLocation("toroquest:textures/gui/icons.png");
	public static final ResourceLocation TITLE = new ResourceLocation("toroquest:textures/gui/title.png");

	public static final float TEXTURE_HEIGHT_SCALER = 1.0F / 256.0F;
	public static final float TEXTURE_WIDTH_SCALER = 1.0F / 256.0F;

	public static final int DEFAULT_ICON_TEXTURE_WIDTH = 32;
	public static final int DEFAULT_ICON_TEXTURE_HEIGTH = 41;

	public static final int DEFAULT_TITLE_TEXTURE_WIDTH = 256;
	public static final int DEFAULT_TITLE_TEXTURE_HEIGTH = 256;

	public static void drawOverlayIcon( Minecraft mc, int left, int top, int horizontalIconIndex, int verrticalIconIndex )
	{
		mc.getTextureManager().bindTexture(ICONS);
		ToroGuiUtils.drawTexturedModalRect(left, top, horizontalIconIndex * DEFAULT_ICON_TEXTURE_WIDTH, verrticalIconIndex * DEFAULT_ICON_TEXTURE_HEIGTH, DEFAULT_ICON_TEXTURE_WIDTH, DEFAULT_ICON_TEXTURE_HEIGTH);
	}

	public static void drawOverlayIcon( Minecraft mc, int x, int y, int textureX, int textureY, int width, int height )
	{
		mc.getTextureManager().bindTexture(ICONS);
		ToroGuiUtils.drawTexturedModalRect(x, y, textureX, textureY, width, height);
	}

	public static void drawOverlayTitle( Minecraft mc, int x, int y, int textureX, int textureY, int width, int height )
	{
		mc.getTextureManager().bindTexture(TITLE);
		ToroGuiUtils.drawTexturedModalRect(x, y, textureX, textureY, width, height);
	}

	public static void drawTexturedModalRect( int x, int y, int textureX, int textureY, int width, int height )
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x + 0, y + height, 0.0D).tex((textureX + 0) * TEXTURE_HEIGHT_SCALER, (textureY + height) * TEXTURE_WIDTH_SCALER).endVertex();
		;
		worldrenderer.pos(x + width, y + height, 0.0D).tex((textureX + width) * TEXTURE_HEIGHT_SCALER, (textureY + height) * TEXTURE_WIDTH_SCALER).endVertex();
		worldrenderer.pos(x + width, y + 0, 0.0D).tex((textureX + width) * TEXTURE_HEIGHT_SCALER, (textureY + 0) * TEXTURE_WIDTH_SCALER).endVertex();
		worldrenderer.pos(x + 0, y + 0, 0.0D).tex((textureX + 0) * TEXTURE_HEIGHT_SCALER, (textureY + 0) * TEXTURE_WIDTH_SCALER).endVertex();
		tessellator.draw();
	}
}
