package com.newerstylehitsplats;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NewerStyleHitsplatsPluginLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NewerStyleHitsplatsPlugin.class);
		RuneLite.main(args);
	}
}
