
package net.runelite.client.plugins.zfoodeater;

import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.iutils.*;

import com.google.inject.Provides;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.Subscribe;
import org.pf4j.Extension;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
		name = "<html>UG Food Eater <font size=\"\" color=\"green\"<b>:)</font></b></html>",
		description = "beta.",
		tags = { "zoy", "food", "custom","Z","eater","auto","eat","UG" },
		type = PluginType.UTILITY
)
@Slf4j
public class zfoodeaterPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private iUtils utils;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private MenuUtils menu;

	@Inject
	private MouseUtils mouse;

	@Inject
	private CalculationUtils calculationUtils;

	@Inject
	private zfoodeaterConfiguration config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ExecutorService executorService;

	private int nextEatHP;
	Player player;

	private List<WidgetItem> foodids = new ArrayList<>();
	private List<Integer> ints = new ArrayList<>();


	@Provides
	zfoodeaterConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(zfoodeaterConfiguration.class);
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			nextEatHP = calculationUtils.getRandomIntBetweenRange(config.minEatHP(), config.maxEatHP());
		}
	}

	@Override
	protected void shutDown()
	{

	}

	private void eatItem()
	{
		ints = utils.stringToIntList(config.food());
		for (int i = 0; i < ints.size(); i++) {
			foodids.add(inventory.getWidgetItem(ints.get(i)));
		}
		long sleep = 0;
		for (WidgetItem item : foodids) {
			if (item != null)
			{
				sleep += calculationUtils.getRandomIntBetweenRange(config.randLow(), config.randHigh());
				utils.doItemActionMsTime(item, MenuOpcode.ITEM_FIRST_OPTION.getId(), WidgetInfo.INVENTORY.getId(), sleep);
			}
		}
		foodids.clear();
	}

	@Subscribe
	private void onGameTick(GameTick event) {

		player = client.getLocalPlayer();

		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {

			if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= nextEatHP)
			{
				eatItem();
				nextEatHP = calculationUtils.getRandomIntBetweenRange(config.minEatHP(), config.maxEatHP());
			}
		}
	}


	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{

		if (event.getCommand().equalsIgnoreCase("eat1"))
		{
			ints = utils.stringToIntList(config.food());
			for (int i = 0; i < ints.size(); i++) {
				foodids.add(inventory.getWidgetItem(ints.get(i)));
			}
			long sleep = 0;
			for (WidgetItem item : foodids) {
				if (item != null)
				{
					sleep += calculationUtils.getRandomIntBetweenRange(config.randLow(), config.randHigh());
					utils.doItemActionMsTime(item, MenuOpcode.ITEM_FIRST_OPTION.getId(), WidgetInfo.INVENTORY.getId(), sleep);
				}
			}
			foodids.clear();
		}

	}

}
