/*
 * Copyright (c) 2019-2020, ganom <https://github.com/Ganom>
 * All rights reserved.
 * Licensed under GPL3, see LICENSE for the full scope.
 */

package net.runelite.client.plugins.zgearswapper;

import com.google.common.base.Splitter;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.zgearswapper.utils.PrayerMap;
import net.runelite.client.plugins.zgearswapper.utils.Spells;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.util.Clipboard;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.plugins.iutils.*;
import org.pf4j.Extension;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "<html>UG Swapper <font size=\"\" color=\"green\"<b>:)</font></b></html>",
	description = "Multiple Swapper that support multiple commands pipelined together.",
	tags = { "zoy", "swapper", "custom","UG" },
	type = PluginType.UTILITY
)
@Slf4j
public class zgearswapperPlugin extends Plugin
{
	private static final Splitter NEWLINE_SPLITTER = Splitter
		.on("\n")
		.omitEmptyStrings()
		.trimResults();

	@Inject
	private Client client;

	@Inject
	private iUtils utils;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private ContainerUtils container;

	@Inject
	private MenuUtils menu;

	@Inject
	private MouseUtils mouse;

	@Inject
	private NPCUtils npcutils;

	@Inject
	private InterfaceUtils interfaceutils;

	@Inject
	private CalculationUtils calculationUtils;

	@Inject
	private KeyManager keyManager;

	@Inject
	private zgearswapperConfiguration config;

	@Inject
	private ItemManager itemManager;

	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
		new ThreadPoolExecutor.DiscardPolicy());

	MenuEntry targetMenu;
	private Actor lastTarget;

	@Provides
	zgearswapperConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(zgearswapperConfiguration.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			keyManager.unregisterKeyListener(one);
			keyManager.unregisterKeyListener(two);
			keyManager.unregisterKeyListener(three);
			return;
		}
		keyManager.registerKeyListener(one);
		keyManager.registerKeyListener(two);
		keyManager.registerKeyListener(three);
	}
	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			targetMenu = null;
			keyManager.registerKeyListener(one);
			keyManager.registerKeyListener(two);
			keyManager.registerKeyListener(three);
		}
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(one);
		keyManager.unregisterKeyListener(two);
		keyManager.unregisterKeyListener(three);
	}

	private final HotkeyListener one = new HotkeyListener(() -> config.customOne())
	{
		@Override
		public void hotkeyPressed()
		{
			decode(config.gearone());
			utils.sendGameMessage("Gear Swap One");
		}
	};

	private final HotkeyListener two = new HotkeyListener(() -> config.customTwo())
	{
		@Override
		public void hotkeyPressed()
		{
			decode(config.geartwo());
			utils.sendGameMessage("Gear Swap Two");
		}
	};

	private final HotkeyListener three = new HotkeyListener(() -> config.customThree())
	{
		@Override
		public void hotkeyPressed()
		{
			decode(config.gearthree());
			utils.sendGameMessage("Gear Swap Three");
		}
	};

	@Subscribe
	private void onInteractingChanged(InteractingChanged event) {
		try {
			if (event.getSource() instanceof Player) {
				Player localPlayer = this.client.getLocalPlayer();
				Player sourcePlayer = (Player)event.getSource();
				Actor targetActor = event.getTarget();
				if (localPlayer == sourcePlayer && targetActor != null) {
					if (this.lastTarget != targetActor) {
						this.lastTarget = targetActor;
					}
				}
			}
		} catch (Exception var5) {
			var5.printStackTrace();
		}

	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		if (event.getCommand().equalsIgnoreCase("copycs"))
		{
			final ItemContainer e = client.getItemContainer(InventoryID.EQUIPMENT);

			if (e == null)
			{
				log.error("CopyCS: Can't find equipment container.");
				return;
			}

			final StringBuilder sb = new StringBuilder();

			for (Item item : e.getItems())
			{
				if (item.getId() == -1 || item.getId() == 0)
				{
					continue;
				}

				sb.append(item.getId());
				sb.append(":");
				sb.append("Equip");
				sb.append("\n");
			}

			final String string = sb.toString();
			Clipboard.store(string);
		}
	}

	private void decode(String string)
	{
		final Map<String, String> map = new LinkedHashMap<>();
		final List<WidgetItem> equipitemids = new ArrayList<>();
		final List<WidgetItem> foodids = new ArrayList<>();
		final List<WidgetItem> inventoryItems = new ArrayList<>();
		final List<WidgetInfo> widgetlist = new ArrayList<>();
		final List<WidgetInfo> widgetlist1 = new ArrayList<>();
		final List<Widget> widg = new ArrayList<>();
		final List<Widget> widg1 = new ArrayList<>();
		final List<Integer> playerEquipment = new ArrayList<>();
		final List<Actor> lasttarg = new ArrayList<>();
		final List<Actor> movetarg = new ArrayList<>();
		WidgetItem dropitem = null;
		WidgetInfo widgetinfo = null;
		WidgetInfo widgetinfo1 = null;
		final Iterable<String> tmp = NEWLINE_SPLITTER.split(string);
		for (String s : tmp)
		{
			if (s.startsWith("//"))
			{
				continue;
			}
			String[] split = s.split(":");
			try
			{
				map.put(split[0], split[1]);
			}
			catch (IndexOutOfBoundsException e)
			{
				log.error("Decode: Invalid Syntax in decoder.");
				dispatchError("Invalid Syntax in decoder.");
				return;
			}
		}

		for (Map.Entry<String, String> entry : map.entrySet())
		{
			String param = entry.getKey();
			String command = entry.getValue().toLowerCase();

			switch (command)
			{
				case "equip":
				{
					equipitemids.add(inventory.getWidgetItem(Integer.parseInt(param)));
				}
				break;
				case "remove":
				{
					final Player p = client.getLocalPlayer();

					for (KitType kitType : KitType.values())
					{
						if (kitType == KitType.RING || kitType == KitType.AMMUNITION ||
							p.getPlayerAppearance() == null)
						{
							continue;
						}

						final int itemId = p.getPlayerAppearance().getEquipmentId(kitType);

						if (itemId != -1 && itemId == Integer.parseInt(param))
						{
							playerEquipment.add(kitType.getWidgetInfo().getId());
						}
					}
				}
				break;
				case "drop":
				{
					dropitem = inventory.getWidgetItem(Integer.parseInt(param));
					inventoryItems.addAll(inventory.getAllItems());
				}
				break;
				case "eat":
				{
					foodids.add(inventory.getWidgetItem(Integer.parseInt(param)));
				}
				break;
				case "pray":
				{
					widgetlist.add(getPrayerWidgetInfo(param));
					}
				break;
				case "castspell":
				{
					widgetlist.add(getSpellWidgetInfo(param));
				}
				break;
				case "leftclickcast":
				{
					widgetlist1.add(getSpellWidgetInfo(param));
				}
				break;
				case "enable":
				{
					widg.add(client.getWidget(593, 36));
				}
				break;
				case "enableon":
				{
					widg1.add(client.getWidget(593, 36));
				}
				break;
				case "hitlasttarget":
				{
					lasttarg.add(getLastTarget());
				}
				break;
				case "movetotarget":
				{
					movetarg.add(getLastTarget());
				}
				break;
			}
		}

		WidgetItem finalDropitem = dropitem;

		executorService.submit(() ->
		{
			for (WidgetItem item : foodids)
			{
				try
				{
					menu.setEntry(new MenuEntry("", "", item.getId(), MenuOpcode.ITEM_FIRST_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(),false));
					mouse.click(item.getCanvasBounds());
					Thread.sleep(getMillis());
					continue;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (WidgetItem item : equipitemids)
			{
				try
				{
					menu.setEntry(new MenuEntry("", "", item.getId(), MenuOpcode.ITEM_SECOND_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(),false));
					mouse.click(item.getCanvasBounds());
					Thread.sleep(getMillis());
					continue;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (Integer GearID : playerEquipment)
			{
				try
				{
					menu.setEntry(new MenuEntry("", "", 1, 57, -1, GearID,false));
					mouse.click(client.getMouseCanvasPosition());
					Thread.sleep(getMillis());
					continue;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (WidgetItem item : inventoryItems)
			{
				if (finalDropitem.getId() == item.getId()) //6512 is empty widget slot
				{
						menu.setEntry(new MenuEntry("", "", item.getId(), MenuOpcode.ITEM_FIFTH_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(),false));
						mouse.click(item.getCanvasBounds());
					try
					{
						Thread.sleep((int) getMillis());
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			for (WidgetInfo widginfo : widgetlist)
			{
				if (widginfo == null)
				{
					log.debug("Prayer: Can't find valid widget info for param {}.", widginfo);
					continue;
				}
				final Widget widget = client.getWidget(widginfo);
				if (widget == null)
				{
					log.debug("Prayer: Can't find valid widget for param {}.", widget);
					continue;
				}
					menu.setEntry(new MenuEntry("", "", 1, 57, widget.getItemId(), widget.getId(), false));
					mouse.click(client.getMouseCanvasPosition());
					try
					{
						Thread.sleep((int) getMillis());
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			for (WidgetInfo widginfo1 : widgetlist1)
			{
				if (widginfo1 == null)
				{
					log.debug("Spell: Can't find valid widget info for param {}.", widginfo1);
					continue;
				}
				final Widget widget = client.getWidget(widginfo1);
				if (widget == null)
				{
					log.debug("Spell: Can't find valid widget for param {}.", widget);
					continue;
				}
				menu.setEntry(new MenuEntry("", "", 1, 25, widget.getItemId(), widget.getId(), false));
				mouse.click(client.getMouseCanvasPosition());
				try
				{
					Thread.sleep((int) getMillis());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (Actor targ : lasttarg)
			{
				if (targ == null)
				{
					continue;
				}
				if (targ instanceof NPC)
				{
					final NPC npcTarget = new NPCQuery().idEquals(new int[]{((NPC) targ).getId()}).result(client).first();
					if (npcTarget == null)
					{
						continue;
					}
					menu.setEntry(new MenuEntry("", "", ((NPC) targ).getIndex(), client.isSpellSelected() ? MenuOpcode.SPELL_CAST_ON_NPC.getId() : MenuOpcode.NPC_SECOND_OPTION.getId(), 0, 0,false));
					mouse.click(client.getMouseCanvasPosition());
				}
				else
				{
					if (!(targ instanceof Player))
					{
						continue;
					}
					final Player playerTarget = new PlayerQuery().nameEquals(new String[]{targ.getName()}).result(client).first();
					if (playerTarget == null)
					{
						continue;
					}
					menu.setEntry(new MenuEntry("Attack", "<col=ffffff>" + playerTarget.getName() + "<col=ff3000>  (level-" + playerTarget.getCombatLevel() + ")", playerTarget.getPlayerId(), client.isSpellSelected() ? MenuOpcode.SPELL_CAST_ON_PLAYER.getId() : MenuOpcode.PLAYER_SECOND_OPTION.getId(), 0, 0,false));
					mouse.click(client.getMouseCanvasPosition());
					continue;
				}
				try
				{
					Thread.sleep((int) getMillis());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (Actor targ : movetarg)
			{
				if (targ == null)
				{
					continue;
				}
				if (!(targ instanceof Player))
				{
					continue;
				}
				final Player playerTarget = new PlayerQuery().nameEquals(new String[]{targ.getName()}).result(client).first();
				if (playerTarget == null)
				{
					continue;
				}
				menu.setEntry(new MenuEntry("Follow", "<col=ff0000>" + playerTarget.getName() + "<col=ff00>  (level-" + playerTarget.getCombatLevel() + ")", playerTarget.getPlayerId(), MenuOpcode.PLAYER_THIRD_OPTION.getId(), 0, 0, false));
				mouse.click(client.getMouseCanvasPosition());
				try
				{
					Thread.sleep((int) getMillis());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (Widget widget : widg)
			{
				if (widget == null)
				{
					log.debug("Spec: Can't find valid widget");
					continue;
				}
				utils.sendGameMessage("Enable: "+widget.getId());
				menu.setEntry(new MenuEntry("", "", 1, 57, -1, widget.getId(),false));
				mouse.click(client.getMouseCanvasPosition());
				try
				{
					Thread.sleep((int) getMillis());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				try
				{
					Thread.sleep((int) getMillis());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (Widget widget : widg1)
			{
				if (widget == null)
				{
					log.debug("Spec: Can't find valid widget");
					continue;
				}
				if (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) != 1)
				{
					utils.sendGameMessage("EnableOn: "+widget.getId());
					menu.setEntry(new MenuEntry("", "", 1, 57, -1, widget.getId(),false));
					mouse.click(client.getMouseCanvasPosition());
					continue;
				}
				try
				{
					Thread.sleep((int) getMillis());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				try
				{
					Thread.sleep((int) getMillis());
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	private long getMillis()
	{
		return (long) (Math.random() * config.randLow() + config.randHigh());
	}

	public Actor getLastTarget() {
		return this.lastTarget;
	}

	public WidgetInfo getPrayerWidgetInfo(String spell)
	{
		return PrayerMap.getWidget(spell);
	}

	public WidgetInfo getSpellWidgetInfo(String spell)
	{
		return Spells.getWidget(spell);
	}

	private void dispatchError(String error)
	{
		String str = ColorUtil.wrapWithColorTag("Gear Swapper", Color.MAGENTA)
			+ " has encountered an "
			+ ColorUtil.wrapWithColorTag("error", Color.RED)
			+ ": "
			+ error;

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", str, null);
	}

}
