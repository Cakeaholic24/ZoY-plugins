package net.runelite.client.plugins.zflaxspinner;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.InventoryItemQuery;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

@Slf4j
public class MiscUtils {

	@Inject
	private Client client;

	@Inject
	ExecutorService executorService;

	public MenuEntry targetMenu;
	protected static final java.util.Random random = new java.util.Random();

	public boolean isInRegion(Client client, Integer region) {
		if (client.getLocalPlayer() == null)
			return false;
		return (client.getLocalPlayer().getWorldLocation().getPlane() < 1 && client.getLocalPlayer().getWorldLocation().getRegionID() == region);
	}

	public boolean isInArea(Client client, WorldArea area) {
		if (client.getLocalPlayer() == null)
			return false;
		return (client.getLocalPlayer().getWorldLocation().getPlane() < 2 && client.getLocalPlayer().getWorldArea().intersectsWith(area));
	}

	public boolean inventoryContains(Client client, int itemID)
	{
		if (client.getItemContainer(InventoryID.INVENTORY) == null)
		{
			return false;
		}

		return new InventoryItemQuery(InventoryID.INVENTORY)
				.idEquals(itemID)
				.result(client)
				.size() >= 1;
	}

	public boolean inventoryContains(Client client, String itemName)
	{
		if (client.getItemContainer(InventoryID.INVENTORY) == null)
		{
			return false;
		}

		WidgetItem inventoryItem = new InventoryWidgetItemQuery()
				.filter(i -> client.getItemDefinition(i.getId())
						.getName()
						.toLowerCase()
						.contains(itemName))
				.result(client)
				.first();

		return inventoryItem != null;
	}

	public boolean inventoryContains(Client client, Collection<Integer> itemIds)
	{
		if (client.getItemContainer(InventoryID.INVENTORY) == null)
		{
			return false;
		}
		return getInventoryItems(itemIds).size() > 0;
	}

	//doesn't NPE
	public boolean bankContainsAnyOf(int... ids)
	{
		if (isBankOpen(client))
		{
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

			return new BankItemQuery().idEquals(ids).result(client).size() > 0;
		}
		return false;
	}

	public List<WidgetItem> getInventoryItems(Collection<Integer> ids)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		List<WidgetItem> matchedItems = new ArrayList<>();

		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (ids.contains(item.getId()))
				{
					matchedItems.add(item);
				}
			}
			return matchedItems;
		}
		return null;
	}

	public boolean inventoryFull()
	{
		return getInventorySpace() <= 0;
	}

	public int getInventorySpace()
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			return 28 - inventoryWidget.getWidgetItems().size();
		}
		else
		{
			return -1;
		}
	}

	public boolean isBankOpen(Client client) {
		return (client.getItemContainer(InventoryID.BANK) != null);
	}

	public boolean isMoving(LocalPoint previous)
	{
		if(previous == null)
		{
			return false;
		}
		return !client.getLocalPlayer().getLocalLocation().equals(previous);
	}

	public Widget getBankItemWidget(int id)
	{
		if (!isBankOpen(client))
		{
			return null;
		}

		WidgetItem bankItem = new BankItemQuery().idEquals(id).result(client).first();
		if (bankItem != null)
		{
			return bankItem.getWidget();
		}
		else
		{
			return null;
		}
	}

	public void withdrawAllItem(Widget bankItemWidget)
	{
		executorService.submit(() ->
		{
			targetMenu = new MenuEntry("Withdraw-All", "", 7, MenuAction.CC_OP.getId(), bankItemWidget.getIndex(), 786444, false);
			click();
		});
	}

	public void withdrawAllItem(int bankItemID)
	{
		Widget item = getBankItemWidget(bankItemID);
		if (item != null)
		{
			withdrawAllItem(item);
		}
		else
		{
			log.debug("Withdraw all item not found.");
		}
	}

	//Checks if Stamina enhancement is active and if stamina potion is in inventory
	public WidgetItem shouldStamPot(int energy)
	{
		if (client.getEnergy() <= energy && !isBankOpen(client))
		{
			return getInventoryWidgetItem(List.of(ItemID.ENERGY_POTION1, ItemID.ENERGY_POTION2, ItemID.ENERGY_POTION3, ItemID.ENERGY_POTION4));
		}
		else
		{
			return null;
		}
	}

	public boolean drinkStamPot(int energy, long delay)
	{
		WidgetItem staminaPotion = shouldStamPot(energy);
		if (staminaPotion != null)
		{
			log.info("using stamina potion");
			targetMenu = new MenuEntry("", "", staminaPotion.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), staminaPotion.getIndex(), 9764864, false);
			click(staminaPotion.getCanvasBounds(), delay);
			return true;
		}
		return false;
	}
	public WidgetItem getInventoryWidgetItem(int id)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (item.getId() == id)
				{
					return item;
				}
			}
		}
		return null;
	}
	public WidgetItem getInventoryWidgetItem(Collection<Integer> ids)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (ids.contains(item.getId()))
				{
					return item;
				}
			}
		}
		return null;
	}

	public boolean inventoryItemContainsAmount(int id, int amount, boolean stackable, boolean exactAmount)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
		int total = 0;
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
			for (WidgetItem item : items)
			{
				if (item.getId() == id)
				{
					if (stackable)
					{
						total = item.getQuantity();
						break;
					}
					total++;
				}
			}
		}
		if ((exactAmount && total != amount) || (total < amount))
		{
			return false;
		}
		return true;
	}

	public long get_random_delay(int min, int max)
	{
		return getRandomIntBetweenRange(min, max);
	}
	public Integer get_random_tick_delay(int min, int max)
	{
		return getRandomIntBetweenRange(min, max);
	}

	public void openbank(GameObject gameObject)
	{
		executorService.submit(() ->
		{
		targetMenu = new MenuEntry("open", "", gameObject.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId(), gameObject.getLocalLocation().getSceneX(), gameObject.getLocalLocation().getSceneY(), false);
		click();
		});
	}

	public void clickwheel(GameObject gameObject)
	{
		executorService.submit(() ->
		{
			targetMenu = new MenuEntry("Spin", "", gameObject.getId(), MenuAction.GAME_OBJECT_SECOND_OPTION.getId(), gameObject.getLocalLocation().getSceneX(), gameObject.getLocalLocation().getSceneY(), false);
			click();
		});
	}

	public void spinflax()
	{
		executorService.submit(() ->
		{
			targetMenu = new MenuEntry("Spin", "", 1, 57, -1, 17694736, false);
			click();
		});
	}

	public void climbdown(GameObject gameObject)
	{
		executorService.submit(() ->
		{
			targetMenu = new MenuEntry("Climb-down", "", gameObject.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), gameObject.getLocalLocation().getSceneX(), gameObject.getLocalLocation().getSceneY(), false);
			click();
		});
	}

	public void deposit(Widget widget)
	{
		executorService.submit(() ->
		{
			targetMenu = new MenuEntry("Deposit Inventory", "", 1, 57, -1, widget.getId(), false);
			click();
		});
	}

	public void clickwallobject(WallObject wallObject)
	{
		executorService.submit(() ->
		{
			targetMenu = new MenuEntry("", "", wallObject.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), wallObject.getLocalLocation().getSceneX(), wallObject.getLocalLocation().getSceneY(), false);
			click();
		});
	}

	public void clickgameobject(GameObject gameObject)
	{
		executorService.submit(() ->
		{
			targetMenu = new MenuEntry("", "", gameObject.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), gameObject.getSceneMinLocation().getX(), gameObject.getSceneMinLocation().getY(), false);
			click();
		});
	}

	public void click()
	{
		Point pos = client.getMouseCanvasPosition();

		if (client.isStretchedEnabled())
		{
			final Dimension stretched = client.getStretchedDimensions();
			final Dimension real = client.getRealDimensions();
			final double width = (stretched.width / real.getWidth());
			final double height = (stretched.height / real.getHeight());
			final Point point = new Point((int) (pos.getX() * width), (int) (pos.getY() * height));
			client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
			client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
			client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, System.currentTimeMillis(), 0, point.getX(), point.getY(), 1, false, 1));
			return;
		}

		client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 501, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
		client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 502, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
		client.getCanvas().dispatchEvent(new MouseEvent(client.getCanvas(), 500, System.currentTimeMillis(), 0, pos.getX(), pos.getY(), 1, false, 1));
	}

	public void click(Rectangle rectangle, long delay)
	{
		executorService.submit(() ->
		{
			try {
				sleep(delay);
				click(rectangle);
			} catch (RuntimeException  e) {
				e.printStackTrace();
			}
		});
	}

	public void sleep(long toSleep)
	{
		try
		{
			long start = System.currentTimeMillis();
			Thread.sleep(toSleep);

			// Guarantee minimum sleep
			long now;
			while (start + toSleep > (now = System.currentTimeMillis()))
			{
				Thread.sleep(start + toSleep - now);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void click(Rectangle rectangle)
	{
		assert !client.isClientThread();

		Point point = getClickPoint(rectangle);
		click(point);
	}

	public Point getClickPoint(@NotNull Rectangle rect)
	{
		final int x = (int) (rect.getX() + getRandomIntBetweenRange((int) rect.getWidth() / 6 * -1, (int) rect.getWidth() / 6) + rect.getWidth() / 2);
		final int y = (int) (rect.getY() + getRandomIntBetweenRange((int) rect.getHeight() / 6 * -1, (int) rect.getHeight() / 6) + rect.getHeight() / 2);

		return new Point(x, y);
	}
	public void click(Point point) {
		if (client.isStretchedEnabled()) {
			Dimension stretched = client.getStretchedDimensions();
			Dimension real = client.getRealDimensions();
			double width = stretched.width / real.getWidth();
			double height = stretched.height / real.getHeight();
			point = new Point((int)(point.getX() * width), (int)(point.getY() * height));
		}
		mouseEvent(MouseEvent.MOUSE_PRESSED, point);
		mouseEvent(MouseEvent.MOUSE_RELEASED, point);
		mouseEvent(MouseEvent.MOUSE_CLICKED, point);
	}
	private void mouseEvent(int id, @NotNull Point point)
	{
		MouseEvent e = new MouseEvent(
				client.getCanvas(), id,
				System.currentTimeMillis(),
				0, point.getX(), point.getY(),
				1, false, 1
		);

		client.getCanvas().dispatchEvent(e);
	}

	public int getRandomIntBetweenRange(int min, int max)
	{
		//return (int) ((Math.random() * ((max - min) + 1)) + min); //This does not allow return of negative values
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public long randomDelay(boolean weightedDistribution, int min, int max, int deviation, int target)
	{
		if (weightedDistribution)
		{
			/* generate a gaussian random (average at 0.0, std dev of 1.0)
			 * take the absolute value of it (if we don't, every negative value will be clamped at the minimum value)
			 * get the log base e of it to make it shifted towards the right side
			 * invert it to shift the distribution to the other end
			 * clamp it to min max, any values outside of range are set to min or max */
			return (long) clamp((-Math.log(Math.abs(random.nextGaussian()))) * deviation + target, min, max);
		}
		else
		{
			/* generate a normal even distribution random */
			return (long) clamp(Math.round(random.nextGaussian() * deviation + target), min, max);
		}
	}

	private double clamp(double val, int min, int max)
	{
		return Math.max(min, Math.min(max, val));
	}


	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getOpcode() == MenuAction.CC_OP.getId() && (event.getParam1() == WidgetInfo.WORLD_SWITCHER_LIST.getId() ||
				event.getParam1() == 11927560 || event.getParam1() == 4522007 || event.getParam1() == 24772686))
		{
			return;
		}
		if (targetMenu != null)
		{
			client.setLeftClickMenuEntry(targetMenu);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (targetMenu != null)
		{
			event.setMenuEntry(targetMenu);
		}

		targetMenu = null;
	}
}
