package com.newerstylehitsplats;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.runelite.api.Actor;

final class HitsplatTracker
{
	private static final int MAX_HITSPLATS_PER_ACTOR = 4;
	private final Map<Actor, List<TrackedHitsplat>> hitsplats = new IdentityHashMap<>();

	synchronized void add(Actor actor, int amount, int type, int disappearsOnGameCycle,
		int currentGameCycle, List<CombatIcon> icons)
	{
		if (disappearsOnGameCycle <= currentGameCycle)
		{
			return;
		}

		List<TrackedHitsplat> actorHitsplats = hitsplats.computeIfAbsent(actor, ignored -> new ArrayList<>());
		removeExpired(actorHitsplats, currentGameCycle);

		boolean[] occupiedSlots = new boolean[MAX_HITSPLATS_PER_ACTOR];
		for (TrackedHitsplat hitsplat : actorHitsplats)
		{
			occupiedSlots[hitsplat.getSlot()] = true;
		}

		int slot = firstFreeSlot(occupiedSlots);
		if (slot == -1)
		{
			TrackedHitsplat earliest = actorHitsplats.get(0);
			for (TrackedHitsplat hitsplat : actorHitsplats)
			{
				if (hitsplat.getDisappearsOnGameCycle() < earliest.getDisappearsOnGameCycle())
				{
					earliest = hitsplat;
				}
			}
			slot = earliest.getSlot();
			actorHitsplats.remove(earliest);
		}

		actorHitsplats.add(new TrackedHitsplat(
			actor, amount, type, currentGameCycle, disappearsOnGameCycle, slot, icons));
	}

	synchronized List<TrackedHitsplat> snapshot(int currentGameCycle)
	{
		List<TrackedHitsplat> result = new ArrayList<>();
		Iterator<Map.Entry<Actor, List<TrackedHitsplat>>> iterator = hitsplats.entrySet().iterator();
		while (iterator.hasNext())
		{
			List<TrackedHitsplat> actorHitsplats = iterator.next().getValue();
			removeExpired(actorHitsplats, currentGameCycle);
			if (actorHitsplats.isEmpty())
			{
				iterator.remove();
			}
			else
			{
				result.addAll(actorHitsplats);
			}
		}
		return result;
	}

	synchronized void clear()
	{
		hitsplats.clear();
	}

	private static void removeExpired(List<TrackedHitsplat> hitsplats, int currentGameCycle)
	{
		hitsplats.removeIf(hitsplat -> hitsplat.getDisappearsOnGameCycle() <= currentGameCycle);
	}

	private static int firstFreeSlot(boolean[] occupiedSlots)
	{
		for (int slot = 0; slot < occupiedSlots.length; slot++)
		{
			if (!occupiedSlots[slot])
			{
				return slot;
			}
		}
		return -1;
	}
}
