package com.newerstylehitsplats;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(NewerStyleHitsplatsConfig.GROUP)
public interface NewerStyleHitsplatsConfig extends Config
{
	String GROUP = "newerstylehitsplats";
	String LEGACY_GROUP = "rs3stylehitsplats";

	@ConfigItem(
		keyName = "showMyHits",
		name = "Show your hits",
		description = "Show hits caused by your character",
		position = 0
	)
	default boolean showMyHits()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showIncomingHits",
		name = "Show incoming hits",
		description = "Show hits applied to your character",
		position = 1
	)
	default boolean showIncomingHits()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showOtherHits",
		name = "Show other hits",
		description = "Show hits caused by other players and NPCs",
		position = 2
	)
	default boolean showOtherHits()
	{
		return true;
	}

	@Range(min = 75, max = 150)
	@ConfigItem(
		keyName = "scale",
		name = "Scale",
		description = "Size of the replacement hitsplats",
		position = 3
	)
	default int scale()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "fadeOut",
		name = "Smooth fade out",
		description = "Smoothly fade hitsplats during the final part of their lifetime",
		position = 4
	)
	default boolean fadeOut()
	{
		return true;
	}

	@Range(min = 60, max = 100)
	@ConfigItem(
		keyName = "opacity",
		name = "Opacity",
		description = "Initial opacity of hitsplats before they begin fading",
		position = 5
	)
	default int opacity()
	{
		return 95;
	}

	@ConfigItem(
		keyName = "multiplyByTen",
		name = "2010 x10 numbers",
		description = "Display damage numbers multiplied by ten, like the 2010 hitpoints system",
		position = 6
	)
	default boolean multiplyByTen()
	{
		return false;
	}
}
