package io.leikvolle.interactionhighlighter;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hdtileindicators")
public interface InteractionHighlightConfig extends Config
{

	@Alpha
	@ConfigItem(
			keyName = "highlightAttackColor",
			name = "Hovered tile",
			description = "Configures the highlight color of hovered tile",
			position = 1
	)
	default Color highlightAttackColor()
	{
		return new Color(0x80FF0000, true);
	}

	@Alpha
	@ConfigItem(
			keyName = "highlightNpcColor",
			name = "Hovered tile",
			description = "Configures the highlight color of hovered tile",
			position = 2
	)
	default Color highlightNpcColor()
	{
		return new Color(0x80FFFF00, true);
	}

	@Alpha
	@ConfigItem(
			keyName = "highlightObjectColor",
			name = "Hovered tile",
			description = "Configures the highlight color of hovered tile",
			position = 3
	)
	default Color highlightObjectColor()
	{
		return new Color(0x8000FFFF, true);
	}

	@ConfigItem(
			keyName = "highlightInteractionWidth",
			name = "Interaction highlight width",
			description = "Limits how often the interaction highlight updates",
			position = 4
	)
	default int highlightInteractionWidth() {return 3;}

	@ConfigItem(
			keyName = "highlightInteractionFeather",
			name = "Interaction highlight feather",
			description = "Limits how often the interaction highlight updates",
			position = 5
	)
	default int highlightInteractionFeather() {return 50;}

	@ConfigItem(
			keyName = "highlightNPCs",
			name = "Highlight NPCs",
			description = "Limits how often the interaction highlight updates",
			position = 6
	)
	default boolean highlightNPCs() {return true;}

	@ConfigItem(
			keyName = "highlightGameObjects",
			name = "Highlight Game Objects",
			description = "Limits how often the interaction highlight updates",
			position = 7
	)
	default boolean highlightGameObjects() {return true;}

	@ConfigItem(
			keyName = "highlightWallObjects",
			name = "Highlight Wall Objects",
			description = "Limits how often the interaction highlight updates",
			position = 8
	)
	default boolean highlightWallObjects() {return true;}
}