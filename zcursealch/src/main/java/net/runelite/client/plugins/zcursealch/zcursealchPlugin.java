package net.runelite.client.plugins.zcursealch;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.iutils.*;
import org.pf4j.Extension;
import static net.runelite.client.plugins.zcursealch.zcursealchState.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.OverlayManager;

import java.time.Instant;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
		name = "UG Curse Alch",
		description = "Curse/Alch plugin",
		tags = { "zoy", "alch", "mage", "stun","UG" },
		enabledByDefault = false,
		type = PluginType.PVM
)
@Slf4j
public class zcursealchPlugin extends Plugin {


	zcursealchState state;

	@Inject
	private Client client;

	@Inject
	private iUtils utils;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private ContainerUtils container;

	@Inject
	private NPCUtils npcutils;

	@Inject
	private InterfaceUtils interfaceutils;

	@Inject
	private CalculationUtils calculationUtils;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	zcursealchOverlay overlay;

	@Inject
	zcursealchnpcOverlay npcoverlay;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private zcursealchConfig config;

	public NPC splashNPC1;
	protected static final java.util.Random random = new java.util.Random();
	private static final String OUT_OF_RUNES_MSG = "You do not have enough";
	private static final String UNREACHABLE_MSG = "I can't reach that";
	private boolean dostun;
	private boolean doalch;
	private MenuEntry entry;

	int itemID = -1;
	int timeout = 0;
	long sleepLength = 0;
	boolean startcursealch;
	Instant botTimer;
	NPC splashNPC;
	WidgetItem targetItem;
	Spells selectedSpell;

	@Override
	protected void startUp() throws Exception {
		doalch = false;
		dostun = false;
		botTimer = Instant.now();
		itemID = config.itemID();
		overlayManager.add(overlay);
		overlayManager.add(npcoverlay);
		utils.sendGameMessage("Plugin Started");
	}
	
	@Override
	protected void shutDown() throws Exception {
		doalch = false;
		dostun = false;
		botTimer = null;
		startcursealch = false;
		itemID = -1;
		timeout = 0;
		overlayManager.remove(overlay);
		overlayManager.remove(npcoverlay);
		utils.sendGameMessage("Plugin Ended");
	}

	@Provides
	zcursealchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(zcursealchConfig.class);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup() != "CurseAlcher")
		{
			return;
		}
		switch (event.getKey())
		{
			case "npcID":
				splashNPC = npcutils.findNearestNpc(config.npcID());
				log.debug("NPC ID set to {}", config.npcID());
				break;
			case "itemID":
				itemID = config.itemID();
				log.debug("Item ID set to {}", config.itemID());
				break;
			case "getSpell":
				selectedSpell = config.getSpell();
				log.debug("Spell set to {}", selectedSpell.getName());
				break;
		}
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("CurseAlcher"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		switch (configButtonClicked.getKey())
		{
			case "startButton":
				if (!startcursealch)
				{
					startcursealch = true;
					botTimer = Instant.now();
					state = null;
					timeout = 0;
					selectedSpell = config.getSpell();
					overlayManager.add(overlay);
					overlayManager.add(npcoverlay);
					utils.sendGameMessage("Start Button Clicked, Plugin Starting");
				}
				else
				{
					startcursealch = false;
					doalch = false;
					dostun = false;
					utils.sendGameMessage("Stop Button Clicked, Plugin Stopping");
				}
				break;
		}
	}

	public zcursealchState getState()
	{
		if (timeout > 0)
		{
			return IDLING;
		}
		if(dostun){
			return Curse_NPC;
		}
		if(doalch){
			targetItem = getItem();
			return Alch_ITEM;
		}
		if(splashNPC != null && !doalch && !dostun){
			dostun = true;
		}
		splashNPC = npcutils.findNearestNpc(config.npcID());
		splashNPC1 = npcutils.findNearestNpc(config.npcID());
		return (splashNPC != null) ? FIND_NPC : NPC_NOT_FOUND;
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if(!startcursealch){
			return;
		}
		state = getState();
		switch (state)
		{
			case IDLING:
				timeout--;
				return;
			case Curse_NPC:
				if (splashNPC == null)
				{
					return;
				}
				entry = new MenuEntry("Cast", "", splashNPC.getIndex(), MenuOpcode.SPELL_CAST_ON_NPC.getId(),
					0, 0, false);
				utils.oneClickCastSpell(selectedSpell.getSpell(), entry, splashNPC.getConvexHull().getBounds(), sleepDelay());
				dostun = false;
				doalch = true;
				break;
			case FIND_NPC:
				splashNPC = npcutils.findNearestNpc(config.npcID());
				if (splashNPC == null)
				{
					return;
				}
				timeout = 4;
				break;
			case NPC_NOT_FOUND:
				log.debug("NPC not found");
				if (config.logout())
				{
					interfaceutils.logout();
				}
				else
				{
					timeout = 4;
				}
				break;
			case Alch_ITEM:
				entry = new MenuEntry("Cast", "", targetItem.getId(), MenuOpcode.ITEM_USE_ON_WIDGET.getId(), targetItem.getIndex(), 9764864, true);
				utils.oneClickCastSpell(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY, entry, targetItem.getCanvasBounds().getBounds(), sleepDelay());
				doalch = false;
				dostun = true;
				timeout = 3;
				break;
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event){
		if (event.getMessage().contains(OUT_OF_RUNES_MSG)){
			startcursealch = false;
			utils.sendGameMessage("Out of runes! Stopping plugin");
			if (config.logout())
			{
				interfaceutils.logout();
			}
			return;
		}
		if (event.getMessage().contains(UNREACHABLE_MSG)){
			startcursealch = false;
			utils.sendGameMessage("Failed to Reach NPC, Stopping plugin");
			if (config.logout())
			{
				interfaceutils.logout();
			}
			return;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (entry != null) {
			event.setMenuEntry(entry);
			log.info("Entry: "+entry);
		}

		entry = null;
	}

	private WidgetItem getItem()
	{
		log.debug("finding item");
		return inventory.getWidgetItem(itemID);
	}

	private long sleepDelay()
	{
		sleepLength = calculationUtils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

}