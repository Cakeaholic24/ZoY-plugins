package net.runelite.client.plugins.cursealch;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("CurseAlcher")
    public interface cursealchConfig extends Config{
    @ConfigItem(
            keyName = "npcID",
            name = "NPC ID",
            description = "Provide ID of the NPC to splash",
            position = 1
    )
    default int npcID()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "itemID",
            name = "Item ID",
            description = "Provide ID of the item to High Alc",
            position = 2
    )
    default int itemID()
    {
        return 0;
    }
	@ConfigItem(
		keyName = "getSpell",
		name = "Spell",
		description = "Choose a spell, only required for single casting",
		position = 3
	)
	default Spells getSpell()
	{
		return Spells.CURSE;
	}

	@ConfigSection(
		keyName = "delayConfig",
		name = "Sleep Delay Configuration",
		description = "Configure how the bot handles sleep delays",
		position = 5
	)
	default boolean delayConfig()
	{
		return false;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepMin",
		name = "Sleep Min",
		description = "",
		position = 6,
		section = "delayConfig"
	)
	default int sleepMin()
	{
		return 60;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepMax",
		name = "Sleep Max",
		description = "",
		position = 7,
		section = "delayConfig"
	)
	default int sleepMax()
	{
		return 350;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepTarget",
		name = "Sleep Target",
		description = "",
		position = 8,
		section = "delayConfig"
	)
	default int sleepTarget()
	{
		return 100;
	}

	@Range(
		min = 0,
		max = 550
	)
	@ConfigItem(
		keyName = "sleepDeviation",
		name = "Sleep Deviation",
		description = "",
		position = 9,
		section = "delayConfig"
	)
	default int sleepDeviation()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "sleepWeightedDistribution",
		name = "Sleep Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 10,
		section = "delayConfig"
	)
	default boolean sleepWeightedDistribution()
	{
		return false;
	}

	@ConfigSection(
		keyName = "delayTickConfig",
		name = "Game Tick Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
		position = 11
	)
	default boolean delayTickConfig()
	{
		return false;
	}

	@Range(
		min = 0,
		max = 25
	)
	@ConfigItem(
		keyName = "tickDelayMin",
		name = "Game Tick Min",
		description = "",
		position = 12,
		section = "delayTickConfig"
	)
	default int tickDelayMin()
	{
		return 1;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayMax",
		name = "Game Tick Max",
		description = "",
		position = 13,
		section = "delayTickConfig"
	)
	default int tickDelayMax()
	{
		return 10;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayTarget",
		name = "Game Tick Target",
		description = "",
		position = 114,
		section = "delayTickConfig"
	)
	default int tickDelayTarget()
	{
		return 5;
	}

	@Range(
		min = 0,
		max = 30
	)
	@ConfigItem(
		keyName = "tickDelayDeviation",
		name = "Game Tick Deviation",
		description = "",
		position = 15,
		section = "delayTickConfig"
	)
	default int tickDelayDeviation()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "tickDelayWeightedDistribution",
		name = "Game Tick Weighted Distribution",
		description = "Shifts the random distribution towards the lower end at the target, otherwise it will be an even distribution",
		position = 16,
		section = "delayTickConfig"
	)
	default boolean tickDelayWeightedDistribution()
	{
		return false;
	}

	@ConfigItem(
		keyName = "startButton",
		name = "Start/Stop",
		description = "Start or stop the bot",
		position = 30
	)
	default Button startButton()
	{
		return new Button();
	}
}
