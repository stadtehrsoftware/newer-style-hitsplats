package com.newerstylehitsplats;

import java.awt.Color;
import net.runelite.api.HitsplatID;

enum HitsplatPalette
{
	DAMAGE(new Color(195, 35, 28), new Color(88, 5, 5), new Color(245, 91, 68)),
	MISS(new Color(48, 78, 135), new Color(15, 24, 54), new Color(111, 151, 221)),
	POISON(new Color(83, 142, 32), new Color(24, 65, 15), new Color(151, 211, 75)),
	VENOM(new Color(38, 91, 45), new Color(10, 43, 24), new Color(90, 168, 95)),
	HEAL(new Color(144, 64, 151), new Color(60, 20, 70), new Color(212, 121, 218)),
	SPECIAL(new Color(46, 123, 132), new Color(11, 45, 55), new Color(100, 190, 198));

	private final Color top;
	private final Color bottom;
	private final Color border;

	HitsplatPalette(Color top, Color bottom, Color border)
	{
		this.top = top;
		this.bottom = bottom;
		this.border = border;
	}

	Color getTop()
	{
		return top;
	}

	Color getBottom()
	{
		return bottom;
	}

	Color getBorder()
	{
		return border;
	}

	static HitsplatPalette forType(int hitsplatType)
	{
		switch (hitsplatType)
		{
			case HitsplatID.BLOCK_ME:
			case HitsplatID.BLOCK_OTHER:
				return MISS;
			case HitsplatID.POISON:
			case HitsplatID.DISEASE:
			case HitsplatID.DISEASE_BLOCKED:
				return POISON;
			case HitsplatID.VENOM:
				return VENOM;
			case HitsplatID.HEAL:
			case HitsplatID.SANITY_RESTORE:
				return HEAL;
			case HitsplatID.CYAN_UP:
			case HitsplatID.CYAN_DOWN:
			case HitsplatID.PRAYER_DRAIN:
			case HitsplatID.CORRUPTION:
			case HitsplatID.SANITY_DRAIN:
				return SPECIAL;
			default:
				return DAMAGE;
		}
	}
}
