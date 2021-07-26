package io.leikvolle.interactionhighlight;

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
			name = "Attack highlight color",
			description = "The color of the outline if attacking an npc",
			position = 1
	)
	default Color highlightAttackColor()
	{
		return new Color(0x90FF0000, true);
	}

	@Alpha
	@ConfigItem(
			keyName = "highlightNpcColor",
			name = "NPC highlight color",
			description = "The color of the outline when hovering an npc",
			position = 2
	)
	default Color highlightNpcColor()
	{
		return new Color(0x90FFFF00, true);
	}

	@Alpha
	@ConfigItem(
			keyName = "highlightObjectColor",
			name = "Object highlight color",
			description = "The color of the outline when hovering an object",
			position = 3
	)
	default Color highlightObjectColor()
	{
		return new Color(0x9000FFFF, true);
	}

	@ConfigItem(
			keyName = "highlightInteractionWidth",
			name = "Interaction highlight width",
			description = "The width of the outline",
			position = 4
	)
	default int highlightInteractionWidth() {return 4;}

	@ConfigItem(
			keyName = "highlightInteractionFeather",
			name = "Interaction highlight feather",
			description = "Feather of the outline",
			position = 5
	)
	default int highlightInteractionFeather() {return 50;}

	@ConfigItem(
			keyName = "highlightNPCs",
			name = "Highlight NPCs",
			description = "Highlight NPCs",
			position = 6
	)
	default boolean highlightNPCs() {return true;}

	@ConfigItem(
			keyName = "highlightGameObjects",
			name = "Highlight Game Objects",
			description = "Highlight Game Objects",
			position = 7
	)
	default boolean highlightGameObjects() {return true;}
}