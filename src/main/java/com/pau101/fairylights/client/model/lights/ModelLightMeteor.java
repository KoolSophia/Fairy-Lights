package com.pau101.fairylights.client.model.lights;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.pau101.fairylights.client.model.AdvancedModelRenderer;
import com.pau101.fairylights.server.fastener.connection.type.hanginglights.Light;
import com.pau101.fairylights.util.Mth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class ModelLightMeteor extends ModelLight {
	private AdvancedModelRenderer[] lights;

	private AdvancedModelRenderer connector;

	private AdvancedModelRenderer cap;

	private AdvancedModelRenderer rodDepthMask;

	public ModelLightMeteor() {
		connector = new AdvancedModelRenderer(this, 77, 0);
		connector.addBox(-1, -0.5F, -1, 2, 2, 2, 0);
		amutachromicParts.addChild(connector);
		cap = new AdvancedModelRenderer(this, 77, 0);
		cap.addBox(-1, -25.45F - 0.05F, -1, 2, 1, 2, 0);
		amutachromicParts.addChild(cap);
		int lightCount = 12;
		lights = new AdvancedModelRenderer[lightCount];
		float rodScale = 0.8F;
		for (int i = 0; i < lightCount; i++) {
			AdvancedModelRenderer light = new AdvancedModelRenderer(this, 37, 72);
			light.addMeteorLightBox(-1, -i * 2 - 2.5F - 0.05F, -1, 2, 2, 2, i == 0 ? 0 : i == lightCount - 1 ? 1 : 2);
			light.isMeteorLightGlow = true;
			lights[i] = light;
			light.scaleX = light.scaleZ = rodScale;
			colorableParts.addChild(light);
		}
		rodDepthMask = new AdvancedModelRenderer(this);
		rodDepthMask.addBox(-1, 0, -1, 2, 24, 2, 0.45F);
		rodDepthMask.rotateAngleX = Mth.PI;
		rodDepthMask.scaleX = rodDepthMask.scaleZ = rodScale;
		amutachromicParts.addChild(rodDepthMask);
	}

	@Override
	public boolean hasRandomRotatation() {
		return true;
	}

	@Override
	public void render(World world, Light light, float scale, Vec3d color, int moonlight, int sunlight, float normalBrightness, int index, float delta) {
		if (hasRandomRotatation()) {
			float randomOffset = Mth.mod(Mth.hash(index) * Mth.DEG_TO_RAD, Mth.TAU) + Mth.PI / 4;
			colorableParts.secondaryRotateAngleY = randomOffset;
			amutachromicParts.secondaryRotateAngleY = randomOffset;
			amutachromicLitParts.secondaryRotateAngleY = randomOffset;
		}
		float stage = light.getTwinkleTimePercent(delta) * 3 - 1;
		rodDepthMask.isHidden = true;
		for (int i = 0; i < lights.length; i++) {
			float t = i / (float) lights.length;
			float brightness = t - stage > 0 ? 1 - Math.abs(t - stage) * 4 : 1 - Math.abs(t - stage);
			if (brightness < 0) {
				brightness = 0;
			}
			if (brightness > 1) {
				brightness = 1;
			}
			float b = Math.max(Math.max(brightness, world.getSunBrightness(1) * 0.95F + 0.05F) * 240, sunlight);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, b, moonlight);
			GlStateManager.enableLighting();
			for (int n = 0; n < lights.length; n++) {
				lights[n].isHidden = i != n;
			}
			amutachromicLitParts.render(scale);
			float[] hsb = new float[3];
			Color.RGBtoHSB((int) (color.xCoord * 255 + 0.5F), (int) (color.yCoord * 255 + 0.5F), (int) (color.zCoord * 255 + 0.5F), hsb);
			hsb[2] = brightness * 0.75F + 0.25F;
			int colorRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
			float cr = (colorRGB >> 16 & 0xFF) / 255F, cg = (colorRGB >> 8 & 0xFF) / 255F, cb = (colorRGB & 0xFF) / 255F;
			GlStateManager.color(cr, cg, cb);
			colorableParts.render(scale);
			if (i == 0 || i == lights.length - 1) {
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, sunlight, moonlight);
				float c = b / 255;
				if (c < 0.5F) {
					c = 0.5F;
				}
				GlStateManager.color(c, c, c);
				connector.isHidden = i != 0;
				cap.isHidden = i == 0;
				amutachromicParts.render(scale);
				GlStateManager.disableLighting();
			}
		}
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, sunlight, moonlight);
		GlStateManager.disableLighting();
		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		colorableParts.isGlowing = true;
		GlStateManager.depthMask(false);
		for (int i = 0; i < lights.length; i++) {
			float t = i / (float) lights.length;
			float brightness = t - stage > 0 ? 1 - Math.abs(t - stage) * 4 : 1 - Math.abs(t - stage) * 2;
			if (brightness < 0) {
				brightness = 0;
			}
			if (brightness > 1) {
				brightness = 1;
			}
			float[] hsb = new float[3];
			Color.RGBtoHSB((int) (color.xCoord * 255 + 0.5F), (int) (color.yCoord * 255 + 0.5F), (int) (color.zCoord * 255 + 0.5F), hsb);
			if (hsb[1] > 0) {
				hsb[1] = brightness;
				hsb[2] = 1;
			}
			int colorRGB = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
			float cr = (colorRGB >> 16 & 0xFF) / 255F, cg = (colorRGB >> 8 & 0xFF) / 255F, cb = (colorRGB & 0xFF) / 255F;
			GlStateManager.color(cr, cg, cb, brightness * 0.15F + 0.1F);
			for (int n = 0; n < lights.length; n++) {
				lights[n].isHidden = i != n;
			}
			colorableParts.render(scale);
		}
		GlStateManager.depthMask(true);
		colorableParts.isGlowing = false;
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		Minecraft.getMinecraft().entityRenderer.enableLightmap();
		GlStateManager.disableAlpha();
		GlStateManager.colorMask(false, false, false, false);
		rodDepthMask.isHidden = false;
		connector.isHidden = true;
		cap.isHidden = true;
		amutachromicParts.render(scale);
		connector.isHidden = false;
		cap.isHidden = false;
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.enableAlpha();
	}
}
