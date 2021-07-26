package io.leikvolle.interactionhighlighter;

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

import java.util.HashSet;
import java.util.Set;
@Slf4j
@PluginDescriptor(
		name = "Interaction Highlighter",
		description = "Highlight the NPC or object you are hovering, like in rs3",
		tags = {"highlight", "overlay"}
)
public class InteractionHighlightPlugin extends Plugin
{

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InteractionHighlightOverlay overlay;

	@Getter(AccessLevel.PACKAGE)
	private final Set<TileObject> tileObjects = new HashSet<>();

	@Provides
	InteractionHighlightConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InteractionHighlightConfig.class);
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
		tileObjects.clear();
	}
	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		addTileObject(event.getGameObject());
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event) {
		addTileObject(event.getWallObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event) {
		removeTileObject(event.getWallObject());
	}

	@Subscribe
	public void onWallObjectChanged(WallObjectChanged event)
	{
		removeTileObject(event.getPrevious());
		addTileObject(event.getWallObject());
	}

	@Subscribe
	public void onGameObjectChanged(GameObjectChanged event)
	{
		removeTileObject(event.getPrevious());
		addTileObject(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		removeTileObject(event.getGameObject());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			// on region changes the tiles get set to null
			tileObjects.clear();
		}
	}

	private void addTileObject(TileObject tileObject)
	{
		tileObjects.add(tileObject);
	}

	private void removeTileObject(TileObject tileObject)
	{
		tileObjects.remove(tileObject);
	}
}