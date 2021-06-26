package io.leikvolle.hdtileindicator;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hdtileindicators")
public interface HDTileIndicatorConfig extends Config
{
	@ConfigItem(
			keyName = "highlightDestinationColor",
			name = "Destination tile",
			description = "Configures the highlight color of current destination",
			position = 1
	)
	default Color highlightDestinationColor()
	{
		return new Color(0xFFDDC64C);
	}

	@ConfigItem(
			keyName = "highlightDestinationTile",
			name = "Highlight destination tile",
			description = "Highlights tile player is walking to",
			position = 2
	)
	default boolean highlightDestinationTile()
	{
		return true;
	}

	@ConfigItem(
			keyName = "highlightInteractionColor",
			name = "Hovered tile",
			description = "Configures the highlight color of hovered tile",
			position = 3
	)
	default Color highlightInteractionColor()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
			keyName = "highlightAttackColor",
			name = "Hovered tile",
			description = "Configures the highlight color of hovered tile",
			position = 4
	)
	default Color highlightAttackColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "highlightHoveredInteraction",
			name = "Highlight hovered tile",
			description = "Highlights tile player is hovering with mouse",
			position = 5
	)
	default boolean highlightHoveredTile()
	{
		return true;
	}

	@ConfigItem(
			keyName = "highlightInteractionFramerate",
			name = "Interaction highlight framerate",
			description = "Limits how often the interaction highlight updates",
			position = 6
	)
	default int highlightInteractionFramerate() {return 15;}

	@ConfigItem(
			keyName = "highlightInteractionWidth",
			name = "Interaction highlight width",
			description = "Limits how often the interaction highlight updates",
			position = 7
	)
	default int highlightInteractionWidth() {return 4;}

	@ConfigItem(
			keyName = "scalingFactor",
			name = "Scaling Factor",
			description = "Limits how often the interaction highlight updates",
			position = 8
	)
	default int scalingFactor() {return 2;}
}