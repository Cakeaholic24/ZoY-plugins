package net.runelite.client.plugins.zcursealch;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("CurseAlcher")
    public interface zcursealchConfig extends Config{
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
	String delayConfig = "delayConfig";

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
		return 80;
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
		return 70;
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
		return 8;
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

	@ConfigItem(
		keyName = "startButton",
		name = "Start/Stop",
		description = "Start or stop the bot",
		position = 11
	)
	default Button startButton()
	{
		return new Button();
	}

	@ConfigItem(
		keyName = "logout",
		name = "Logout when out of runes",
		description = "Enable to logout when out of runes. Won't work if you are attacking an npc that attacks you",
		position = 31
	)
	default boolean logout() { return true; }
}
