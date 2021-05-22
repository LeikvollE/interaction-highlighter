package io.leikvolle.hdtileindicator;


import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.GameState;
import net.runelite.api.WallObject;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WallObjectChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
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

@PluginDescriptor(
		name = "HD Tile Indicator",
		description = "Highlight the tile you are currently moving to in high definition",
		tags = {"highlight", "overlay"},
		enabledByDefault = false
)
public class HDTileIndicatorPlugin extends Plugin
{

	private static final Set<Integer> MINE_SPOTS = ImmutableSet.of(ORE_VEIN_26661, ORE_VEIN_26662, ORE_VEIN_26663, ORE_VEIN_26664);

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HDTileIndicatorOverlay overlay;

	@Getter(AccessLevel.PACKAGE)
	private final Set<WallObject> veins = new HashSet<>();

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
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{

		WallObject wallObject = event.getWallObject();
		if (MINE_SPOTS.contains(wallObject.getId()))
		{
			veins.add(wallObject);
		}
	}

	@Subscribe
	public void onWallObjectChanged(WallObjectChanged event)
	{

		WallObject previous = event.getPrevious();
		WallObject wallObject = event.getWallObject();

		veins.remove(previous);
		if (MINE_SPOTS.contains(wallObject.getId()))
		{
			veins.add(wallObject);
		}
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		WallObject wallObject = event.getWallObject();
		veins.remove(wallObject);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			// on region changes the tiles get set to null
			veins.clear();
		}
	}
}