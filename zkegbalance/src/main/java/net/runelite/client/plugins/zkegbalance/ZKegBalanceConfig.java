
package net.runelite.client.plugins.zkegbalance;

import net.runelite.client.config.*;

@ConfigGroup("zkegbalance")

public interface ZKegBalanceConfig extends Config {

	@ConfigSection(
		keyName = "Config",
		name = "Configuration",
		description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
		position = 1
	)
	String Config = "Config";

	@Range(
		min = 0,
		max = 99
	)
	@ConfigItem(
		keyName = "HPThresholdConfig",
		name = "HP Threshold",
		description = "",
		position = 3,
		section = "Config"
	)
	default int threshold()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "FoodIDConfig",
		name = "Food ID",
		description = "",
		position = 3,
		section = "Config"
	)
	default int foodid()
	{
		return 50;
	}

	@ConfigSection(
			keyName = "delayTickConfig",
			name = "Game Tick Configuration",
			description = "Configure how the bot handles game tick delays, 1 game tick equates to roughly 600ms",
			position = 1
	)
	String delayTickConfig = "delayTickConfig";


	@Range(
			min = 0,
			max = 25
	)
	@ConfigItem(
			keyName = "tickDelayMin",
			name = "Game Tick Min",
			description = "",
			position = 2,
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
			position = 3,
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
			position = 4,
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
			position = 5,
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
			position = 6,
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
			position = 7
	)
	default Button startButton()
	{
		return new Button();
	}

}