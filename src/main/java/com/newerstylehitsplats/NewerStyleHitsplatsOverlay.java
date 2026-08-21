package com.newerstylehitsplats;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class NewerStyleHitsplatsOverlay extends Overlay
{
	private static final int[] SLOT_X = {0, 0, -18, 18};
	private static final int[] SLOT_Y = {0, -20, -10, -10};
	private static final int MAX_CACHE_SIZE = 256;
	private static final Font DAMAGE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

	private final Client client;
	private final NewerStyleHitsplatsPlugin plugin;
	private final NewerStyleHitsplatsConfig config;
	private final Map<ImageKey, BufferedImage> imageCache =
		new LinkedHashMap<ImageKey, BufferedImage>(MAX_CACHE_SIZE, 0.75f, true)
		{
			@Override
			protected boolean removeEldestEntry(Map.Entry<ImageKey, BufferedImage> eldest)
			{
				return size() > MAX_CACHE_SIZE;
			}
		};

	@Inject
	private NewerStyleHitsplatsOverlay(Client client, NewerStyleHitsplatsPlugin plugin,
		NewerStyleHitsplatsConfig config)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPriority(Overlay.PRIORITY_HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		int gameCycle = client.getGameCycle();
		List<TrackedHitsplat> hitsplats = plugin.getTracker().snapshot(gameCycle);
		for (TrackedHitsplat hitsplat : hitsplats)
		{
			if (hitsplat.getActor().getWorldView() != client.getTopLevelWorldView())
			{
				continue;
			}

			BufferedImage image = getImage(hitsplat);
			Point location = hitsplat.getActor().getCanvasImageLocation(
				image, Math.max(0, hitsplat.getActor().getLogicalHeight() / 2));
			if (location == null)
			{
				continue;
			}

			int slot = hitsplat.getSlot();
			int scale = config.scale();
			int xOffset = SLOT_X[slot] * scale / 100;
			int yOffset = SLOT_Y[slot] * scale / 100;
			float opacity = calculateOpacity(
				hitsplat.getAppliedOnGameCycle(),
				hitsplat.getDisappearsOnGameCycle(),
				gameCycle,
				config.opacity(),
				config.fadeOut());
			Composite previousComposite = graphics.getComposite();
			int drawX = location.getX() + xOffset;
			int drawY = location.getY() + yOffset;
			try
			{
				// Keep an opaque cover underneath the fading replacement. Otherwise the
				// original circular hit splat becomes visible as this image fades.
				graphics.setComposite(AlphaComposite.SrcOver);
				graphics.setColor(new Color(14, 11, 9));
				int arc = Math.max(3, 4 * scale / 100);
				graphics.fillRoundRect(drawX, drawY, image.getWidth(), image.getHeight(), arc, arc);

				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				graphics.drawImage(image, drawX, drawY, null);
			}
			finally
			{
				graphics.setComposite(previousComposite);
			}
		}
		return null;
	}

	static float calculateOpacity(int appliedOnGameCycle, int disappearsOnGameCycle,
		int currentGameCycle, int configuredOpacity, boolean fadeOut)
	{
		float baseOpacity = Math.max(0, Math.min(100, configuredOpacity)) / 100f;
		if (!fadeOut)
		{
			return baseOpacity;
		}

		int lifetime = Math.max(1, disappearsOnGameCycle - appliedOnGameCycle);
		int fadeDuration = Math.max(1, Math.round(lifetime * 0.4f));
		int remaining = disappearsOnGameCycle - currentGameCycle;
		if (remaining >= fadeDuration)
		{
			return baseOpacity;
		}
		if (remaining <= 0)
		{
			return 0f;
		}

		float fade = remaining / (float) fadeDuration;
		float easedFade = fade * fade * (3f - 2f * fade);
		return baseOpacity * easedFade;
	}

	synchronized void clearImageCache()
	{
		imageCache.clear();
	}

	private synchronized BufferedImage getImage(TrackedHitsplat hitsplat)
	{
		int displayAmount = config.multiplyByTen() ? hitsplat.getAmount() * 10 : hitsplat.getAmount();
		HitsplatPalette palette = HitsplatPalette.forType(hitsplat.getType());
		List<CombatIcon> iconTypes = hitsplat.getIcons();
		List<BufferedImage> iconImages = new ArrayList<>(iconTypes.size());
		int loadedIconMask = 0;
		for (int index = 0; index < iconTypes.size(); index++)
		{
			BufferedImage iconImage = plugin.getIcons().get(iconTypes.get(index));
			iconImages.add(iconImage);
			if (iconImage != null)
			{
				loadedIconMask |= 1 << index;
			}
		}

		ImageKey key = new ImageKey(
			displayAmount, iconTypes, palette, config.scale(), loadedIconMask);
		return imageCache.computeIfAbsent(key,
			ignored -> createHitsplatImage(displayAmount, palette, iconImages, config.scale()));
	}

	private static BufferedImage createHitsplatImage(int amount, HitsplatPalette palette,
		List<BufferedImage> icons, int scale)
	{
		BufferedImage measuringImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D measuringGraphics = measuringImage.createGraphics();
		measuringGraphics.setFont(DAMAGE_FONT);
		FontMetrics metrics = measuringGraphics.getFontMetrics();
		String text = Integer.toString(amount);
		int panelWidth = Math.max(21, metrics.stringWidth(text) + 7);
		measuringGraphics.dispose();

		int iconCount = Math.max(1, icons.size());
		int iconStripWidth = 3 + iconCount * 13 + (iconCount - 1);
		int baseWidth = iconStripWidth + panelWidth;
		int baseHeight = 25;
		BufferedImage base = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = base.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		// Opaque backing masks the native circular splat without suppressing other actor UI.
		graphics.setColor(new Color(14, 11, 9, 252));
		graphics.fillRoundRect(0, 0, baseWidth, baseHeight, 4, 4);
		graphics.setColor(new Color(76, 61, 45));
		graphics.drawRoundRect(0, 0, baseWidth - 1, baseHeight - 1, 4, 4);

		graphics.setPaint(new GradientPaint(0, 4, new Color(65, 55, 42), 0, 21, new Color(20, 16, 12)));
		graphics.fillRect(2, 5, iconStripWidth - 3, 15);

		graphics.setPaint(new GradientPaint(0, 5, palette.getTop(), 0, 20, palette.getBottom()));
		graphics.fillRect(iconStripWidth, 5, panelWidth - 2, 15);
		graphics.setColor(palette.getBorder());
		graphics.drawRect(iconStripWidth, 5, panelWidth - 3, 14);
		graphics.setColor(new Color(35, 5, 4));
		graphics.drawLine(iconStripWidth + 1, 19, baseWidth - 3, 19);

		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		for (int index = 0; index < iconCount; index++)
		{
			int iconX = 2 + index * 14;
			BufferedImage icon = index < icons.size() ? icons.get(index) : null;
			if (icon != null)
			{
				graphics.drawImage(icon, iconX, 5, 13, 13, null);
			}
			else
			{
				drawFallbackSword(graphics, iconX);
			}
		}

		graphics.setFont(DAMAGE_FONT);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		FontMetrics textMetrics = graphics.getFontMetrics();
		int textX = iconStripWidth + (panelWidth - 2 - textMetrics.stringWidth(text)) / 2;
		int textY = 17;
		graphics.setColor(new Color(35, 0, 0));
		graphics.drawString(text, textX + 1, textY + 1);
		graphics.setColor(Color.WHITE);
		graphics.drawString(text, textX, textY);
		graphics.dispose();

		if (scale == 100)
		{
			return base;
		}

		int scaledWidth = Math.max(1, baseWidth * scale / 100);
		int scaledHeight = Math.max(1, baseHeight * scale / 100);
		BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D scaledGraphics = scaled.createGraphics();
		scaledGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		scaledGraphics.drawImage(base, 0, 0, scaledWidth, scaledHeight, null);
		scaledGraphics.dispose();
		return scaled;
	}

	private static void drawFallbackSword(Graphics2D graphics, int iconX)
	{
		graphics.setColor(new Color(220, 220, 215));
		graphics.drawLine(iconX + 2, 16, iconX + 10, 7);
		graphics.drawLine(iconX + 3, 17, iconX + 11, 8);
		graphics.setColor(new Color(139, 93, 38));
		graphics.drawLine(iconX + 1, 13, iconX + 5, 17);
		graphics.drawLine(iconX + 1, 17, iconX + 5, 13);
	}

	private static final class ImageKey
	{
		private final int amount;
		private final List<CombatIcon> icons;
		private final HitsplatPalette palette;
		private final int scale;
		private final int loadedIconMask;

		private ImageKey(int amount, List<CombatIcon> icons, HitsplatPalette palette,
			int scale, int loadedIconMask)
		{
			this.amount = amount;
			this.icons = icons;
			this.palette = palette;
			this.scale = scale;
			this.loadedIconMask = loadedIconMask;
		}

		@Override
		public boolean equals(Object other)
		{
			if (this == other)
			{
				return true;
			}
			if (!(other instanceof ImageKey))
			{
				return false;
			}
			ImageKey that = (ImageKey) other;
			return amount == that.amount && scale == that.scale && loadedIconMask == that.loadedIconMask
				&& icons.equals(that.icons) && palette == that.palette;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(amount, icons, palette, scale, loadedIconMask);
		}
	}
}
