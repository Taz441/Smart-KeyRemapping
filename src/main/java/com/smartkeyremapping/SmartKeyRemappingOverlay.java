package com.smartkeyremapping;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class SmartKeyRemappingOverlay extends Overlay
{
    private final Client client;
    private final SmartKeyRemappingPlugin plugin;
    private final SmartKeyRemappingConfig config;

    @Inject
    public SmartKeyRemappingOverlay(Client client, SmartKeyRemappingPlugin plugin, SmartKeyRemappingConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.MANUAL);
        drawAfterInterface(InterfaceID.CHATBOX);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isChatInputAvailable())
        {
            return null;
        }

        Rectangle inputBounds = plugin.getInputAreaBounds();
        if (inputBounds == null)
        {
            return null;
        }

        net.runelite.api.Point mouseCanvas = client.getMouseCanvasPosition();
        boolean isHovered = false;
        if (mouseCanvas != null)
        {
            Point mousePt = new Point(mouseCanvas.getX(), mouseCanvas.getY());
            isHovered = inputBounds.contains(mousePt);
        }

        if (!plugin.isTyping() && config.clickToFocus() && isHovered && config.showHoverHighlight())
        {
            graphics.setColor(config.hoverFillColor());
            graphics.fillRect(inputBounds.x, inputBounds.y, inputBounds.width, inputBounds.height);
            graphics.setColor(config.hoverOutlineColor());
            graphics.drawRect(inputBounds.x, inputBounds.y, inputBounds.width - 1, inputBounds.height - 1);
        }

        return null;
    }
}