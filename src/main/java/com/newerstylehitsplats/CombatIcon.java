package com.newerstylehitsplats;

import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;

enum CombatIcon
{
	ATTACK(Source.SPRITE, SpriteID.TinyCombatStaticons.ATTACK),
	STRENGTH(Source.SPRITE, SpriteID.TinyCombatStaticons.STRENGTH),
	DEFENCE(Source.SPRITE, SpriteID.TinyCombatStaticons.DEFENCE),
	RANGED(Source.SPRITE, SpriteID.TinyCombatStaticons.RANGED),
	MAGIC(Source.SPRITE, SpriteID.TinyCombatStaticons.MAGIC),
	HITPOINTS(Source.SPRITE, SpriteID.TinyCombatStaticons.HITPOINTS),
	PRAYER(Source.SPRITE, SpriteID.TinyCombatStaticons.PRAYER),
	ANTIPOISON(Source.ITEM, ItemID._4DOSEANTIPOISON),
	ANTIVENOM(Source.ITEM, ItemID.ANTIVENOM_4);

	enum Source
	{
		SPRITE,
		ITEM
	}

	private final Source source;
	private final int imageId;

	CombatIcon(Source source, int imageId)
	{
		this.source = source;
		this.imageId = imageId;
	}

	Source getSource()
	{
		return source;
	}

	int getImageId()
	{
		return imageId;
	}
}
