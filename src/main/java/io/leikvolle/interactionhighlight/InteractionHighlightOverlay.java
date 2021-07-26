package io.leikvolle.interactionhighlight;

import java.awt.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Slf4j
public class InteractionHighlightOverlay extends Overlay {

    private final Client client;
    private final InteractionHighlightConfig config;
    private final InteractionHighlightPlugin plugin;
    private final ModelOutlineRenderer modelOutlineRenderer;

    private final Pattern p;

    @Inject
    private InteractionHighlightOverlay(Client client, InteractionHighlightConfig config, InteractionHighlightPlugin plugin,
                                        ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);

        p = Pattern.compile("(?:<col=[^>]*>)?([^<]*)(?:<.*)?");
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        renderHighlight();

        return null;
    }

    private boolean renderNpcHighlight(String targetName, String option) {
        Point mousePosition = this.client.getMouseCanvasPosition();
        NPC nearest = null;
        int shortestDistance = Integer.MAX_VALUE;
        for (NPC actor : client.getNpcs()) {
            if (Objects.equals(actor.getName(), targetName)) {
                Shape shape = Perspective.getClickbox(client, actor.getModel(), actor.getOrientation(), actor.getLocalLocation());
                if (shape != null && shape.contains(mousePosition.getX(), mousePosition.getY())) {
                    LocalPoint local = actor.getLocalLocation();
                    LocalPoint camera = new LocalPoint(client.getCameraX(), client.getCameraY());
                    int actorDistance = local.distanceTo(camera);
                    if (actorDistance < shortestDistance && actor.getWorldLocation().getPlane() == client.getPlane()) {
                        nearest = actor;
                        shortestDistance = actorDistance;
                    }
                }
            }
        }
        if (nearest != null) {
            Color highlightColor;
            switch (option) {
                case "Attack":
                    highlightColor = config.highlightAttackColor();
                    break;
                default:
                    highlightColor = config.highlightNpcColor();
                    break;
            }
            this.modelOutlineRenderer.drawOutline(nearest, this.config.highlightInteractionWidth(), highlightColor, this.config.highlightInteractionFeather());
            return true;
        }
        return false;
    }

    private boolean renderObjectHighlight(int objectId) {
        Point mousePosition = this.client.getMouseCanvasPosition();
        TileObject nearestGO = null;
        int shortestDistance = Integer.MAX_VALUE;
        for (TileObject tileObject : plugin.getTileObjects()) {
            if (tileObject.getId() == objectId) {
                Shape clickBox = tileObject.getClickbox();
                if (clickBox != null && clickBox.contains(mousePosition.getX(), mousePosition.getY())) {
                    LocalPoint local = tileObject.getLocalLocation();
                    LocalPoint camera = new LocalPoint(client.getCameraX(), client.getCameraY());
                    int objectDistance = local.distanceTo(camera);
                    if (objectDistance < shortestDistance && tileObject.getWorldLocation().getPlane() == client.getPlane()) {
                        nearestGO = tileObject;
                        shortestDistance = objectDistance;
                    }
                }
            }
        }
        if (nearestGO != null) {
            this.modelOutlineRenderer.drawOutline(nearestGO, config.highlightInteractionWidth(), config.highlightObjectColor(), config.highlightInteractionFeather());
            return true;
        }
        return false;
    }

    private void renderHighlight() {

        if (client.isMenuOpen()) {
            return;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();
        int last = menuEntries.length - 1;

        if (last < 0) {
            return;
        }

        MenuEntry menuEntry = menuEntries[last];

        Matcher m = p.matcher(menuEntry.getTarget());
        final String targetName = m.matches() ? m.group(1) : "";

        String option = menuEntry.getOption();

        if (this.config.highlightNPCs() && renderNpcHighlight(targetName, option)) {

        } else if (this.config.highlightGameObjects() && renderObjectHighlight(menuEntry.getIdentifier())) {

        }
    }

}