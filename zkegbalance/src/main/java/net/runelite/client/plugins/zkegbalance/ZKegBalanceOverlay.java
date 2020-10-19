package net.runelite.client.plugins.zkegbalance;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import static net.runelite.api.MenuOpcode.RUNELITE_OVERLAY_CONFIG;
import net.runelite.client.ui.overlay.Overlay;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

public class ZKegBalanceOverlay extends Overlay
{
	private final Client client;
	private final ZKegBalancePlugin plugin;
	private final ZKegBalanceConfig config;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	private ZKegBalanceOverlay(Client client, ZKegBalancePlugin plugin, ZKegBalanceConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		this.setPriority(OverlayPriority.HIGHEST);
		this.setPosition(OverlayPosition.BOTTOM_LEFT);
		this.getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Z Keg Balance Overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin == null)
			return null;

		if (!plugin.pluginStarted)
			return null;

		panelComponent.getChildren().clear();

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT);

		tableComponent.addRow("Z Keg Balance");
		tableComponent.addRow("Status: "+plugin.status);

		if (!tableComponent.isEmpty())
		{
			panelComponent.getChildren().add(tableComponent);
		}

		panelComponent.setPreferredSize(new Dimension(175, 100));
		//panelComponent.setBackgroundColor(Color.BLACK);

		return panelComponent.render(graphics);
	}
}
