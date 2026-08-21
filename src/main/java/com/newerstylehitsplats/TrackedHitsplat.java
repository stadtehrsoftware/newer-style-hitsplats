package com.newerstylehitsplats;

import java.util.List;
import net.runelite.api.Actor;

final class TrackedHitsplat
{
	private final Actor actor;
	private final int amount;
	private final int type;
	private final int appliedOnGameCycle;
	private final int disappearsOnGameCycle;
	private final int slot;
	private final List<CombatIcon> icons;

	TrackedHitsplat(Actor actor, int amount, int type, int appliedOnGameCycle,
		int disappearsOnGameCycle, int slot, List<CombatIcon> icons)
	{
		this.actor = actor;
		this.amount = amount;
		this.type = type;
		this.appliedOnGameCycle = appliedOnGameCycle;
		this.disappearsOnGameCycle = disappearsOnGameCycle;
		this.slot = slot;
		this.icons = List.copyOf(icons);
	}

	Actor getActor()
	{
		return actor;
	}

	int getAmount()
	{
		return amount;
	}

	int getType()
	{
		return type;
	}

	int getDisappearsOnGameCycle()
	{
		return disappearsOnGameCycle;
	}

	int getAppliedOnGameCycle()
	{
		return appliedOnGameCycle;
	}

	int getSlot()
	{
		return slot;
	}

	List<CombatIcon> getIcons()
	{
		return icons;
	}
}
