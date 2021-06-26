package io.leikvolle.hdtileindicator;


import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import static net.runelite.api.ObjectID.ORE_VEIN_26661;
import static net.runelite.api.ObjectID.ORE_VEIN_26662;
import static net.runelite.api.ObjectID.ORE_VEIN_26663;
import static net.runelite.api.ObjectID.ORE_VEIN_26664;

import java.util.HashSet;
import java.util.Set;
@Slf4j
@PluginDescriptor(
		name = "HD Tile Indicator",
		description = "Highlight the tile you are currently moving to in high definition",
		tags = {"highlight", "overlay"},
		enabledByDefault = false
)
public class HDTileIndicatorPlugin extends Plugin
{

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HDTileIndicatorOverlay overlay;

	@Getter(AccessLevel.PACKAGE)
	private final Set<GameObject> rocks = new HashSet<>();

	@Provides
	HDTileIndicatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HDTileIndicatorConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		rocks.clear();
	}
/*
	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		addGameObject(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectChanged(GameObjectChanged event)
	{
		removeGameObject(event.getPrevious());
		addGameObject(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		removeGameObject(event.getGameObject());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			// on region changes the tiles get set to null
			rocks.clear();
		}
	}

	private void addGameObject(GameObject gameObject)
	{
		if (gameObject.getRenderable() != null || gameObject.getRenderable() instanceof Model) {
			rocks.add(gameObject);
		}
	}

	private void removeGameObject(GameObject gameObject)
	{
		rocks.remove(gameObject);
	}*/
}