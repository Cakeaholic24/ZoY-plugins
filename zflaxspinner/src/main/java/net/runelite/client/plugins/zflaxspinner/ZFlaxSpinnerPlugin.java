package net.runelite.client.plugins.zflaxspinner;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.zflaxspinner.Tasks.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import java.util.concurrent.ExecutorService;

@Slf4j
@Extension
@PluginDescriptor(
	name = "UG Flax Spinner",
	description = "An automation utility for Flax Spinning",
	tags = {"combat", "strength", "hitpoints", "training", "keg", "balance", "warrior", "guild","UG"},
	enabledByDefault = false
)
public class ZFlaxSpinnerPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private MiscUtils utils;

	@Inject
	ExecutorService executorService;

	@Inject
	private ZFlaxSpinnerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ZFlaxSpinnerOverlay overlay;

	@Inject
	private ChatMessageManager chatMessageManager;

	boolean pluginStarted;
	public LocalPoint lastlocalpoint = new LocalPoint(0, 0);

	@Provides
	ZFlaxSpinnerConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(ZFlaxSpinnerConfig.class);
	}

	public String status = "initializing...";

	private TaskSet tasks = new TaskSet();

	public static int OpenBankDelay = 0;
	public static int WithdrawDelay = 0;
	public static int delay = 0;
	public static int potdelay = 0;

	@Override
	protected void startUp() throws Exception
	{
		pluginStarted = false;
		overlayManager.add(overlay);
		status = "initializing...";
		tasks.clear();
		tasks.addAll(this, client, config, utils, executorService,

			//--------------------------

			new OpenBankTask(),
			new WithdrawingFlaxTask(),
			new SpinWheelTask(),
			new FlaxRoomDoorTask(),
			new ExitFlaxRoomTask(),
			new ExitFlaxDoorTask(),
			new ClimbDownTask()
		);
	}

	@Override
	protected void shutDown() throws Exception
	{
		pluginStarted = false;
		overlayManager.remove(overlay);
		tasks.clear();
	}

	@Subscribe
	public void onConfigButtonClicked(ConfigButtonClicked event) {
		if (!event.getGroup().equals("zflaxspinner"))
			return;
		if (event.getKey().equals("startButton")) {
			pluginStarted = !pluginStarted;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!pluginStarted)
		{
			return;
		}

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

		if (inventoryWidget == null)
			return;

		Task task = tasks.getValidTask();

		if (task != null)
		{
			status = task.getTaskDescription();
			task.onGameTick(event);
		}
		lastlocalpoint = client.getLocalPlayer().getLocalLocation();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!pluginStarted)
			return;

		if (client.getGameState() != GameState.LOGIN_SCREEN)
			return;

	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		utils.onMenuEntryAdded(event);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		utils.onMenuOptionClicked(event);
	}

	private void sendGameMessage(String message)
	{
		chatMessageManager
			.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(
					new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append(message)
					.build())
				.build());
	}

	public int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

}
