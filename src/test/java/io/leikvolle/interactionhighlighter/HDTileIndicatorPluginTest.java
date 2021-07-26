package io.leikvolle.interactionhighlighter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HDTileIndicatorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(InteractionHighlightPlugin.class);
		RuneLite.main(args);
	}
}