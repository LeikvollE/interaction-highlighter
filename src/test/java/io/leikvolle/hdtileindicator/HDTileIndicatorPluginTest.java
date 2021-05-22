package io.leikvolle.hdtileindicator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HDTileIndicatorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HDTileIndicatorPlugin.class);
		RuneLite.main(args);
	}
}