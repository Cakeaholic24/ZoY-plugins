package net.runelite.client.plugins.zcursealch;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;

@Slf4j
@Singleton
class zcursealchnpcOverlay extends OverlayPanel
{

    private final Client client;
    private final zcursealchPlugin plugin;
    private final zcursealchConfig config;


    @Inject
    private zcursealchnpcOverlay(final Client client, final zcursealchPlugin plugin, final zcursealchConfig config)
    {
        super(plugin);
        //setPosition(OverlayPosition.BOTTOM_LEFT);
		this.setPosition(OverlayPosition.DYNAMIC);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Stun Alcher overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
    	Shape clickbox;
        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		if (this.plugin.splashNPC1 != null) {
			clickbox = Perspective.getClickbox(this.client, this.plugin.splashNPC1.getModel(), this.plugin.splashNPC1.getOrientation(), this.plugin.splashNPC1.getLocalLocation());
			if (clickbox != null) {
				OverlayUtil.renderHoverableArea(graphics, clickbox, this.mouse(), Color.CYAN, Color.BLACK, Color.blue);
			} else {
				OverlayUtil.renderActorOverlay(graphics, this.plugin.splashNPC1.getInteracting(), "", Color.CYAN);
			}
		}
        return super.render(graphics);
    }
	public net.runelite.api.Point mouse()
	{
		return client.getMouseCanvasPosition();
	}
}