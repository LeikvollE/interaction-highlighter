package io.leikvolle.interactionhighlighter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.ImageUtil;

import static net.runelite.api.Constants.TILE_FLAG_BRIDGE;

@Slf4j
public class HDTileIndicatorOverlay extends Overlay {
    private final Client client;
    private final HDTileIndicatorConfig config;
    private final HDTileIndicatorPlugin plugin;
    private final ModelOutlineRenderer modelOutlineRenderer;

    private final Pattern p;

    private final BufferedImage ARROW_ICON;

    @Inject
    private HDTileIndicatorOverlay(Client client, HDTileIndicatorConfig config, HDTileIndicatorPlugin plugin,
                                   ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);

        p = Pattern.compile("(?:<col=[^>]*>)?([^<]*)(?:<.*)?");

        ARROW_ICON = ImageUtil.loadImageResource(HDTileIndicatorPlugin.class, "arrow.png");
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        renderHighlight();

        return null;
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

        Point mousePosition = client.getMouseCanvasPosition();

        NPC nearest = null;
        for (NPC actor : client.getNpcs()) {
            if (Objects.equals(actor.getName(), targetName)) {
                Shape shape = Perspective.getClickbox(client, actor.getModel(), actor.getOrientation(), actor.getLocalLocation());
                if (shape != null && shape.contains(mousePosition.getX(), mousePosition.getY())) {
                    nearest = actor;
                }
            }
        }
        if (nearest == null) {
            TileObject nearestGO = null;
            for (TileObject tileObject : plugin.getTileObjects()) {
                if (tileObject.getId() == menuEntry.getIdentifier()) {
                    Shape clickBox = tileObject.getClickbox();
                    if (clickBox != null && clickBox.contains(mousePosition.getX(), mousePosition.getY())) {
                        nearestGO = tileObject;
                    }
                }
            }
            if (nearestGO != null) {
                modelOutlineRenderer.drawOutline(nearestGO, config.highlightInteractionWidth(), config.highlightObjectColor(), config.highlightInteractionFeather());
            }
        } else {
            Color npcHighlightColor;
            switch (option) {
                case "Attack":
                    npcHighlightColor = config.highlightAttackColor();
                    break;
                default:
                    npcHighlightColor = config.highlightNpcColor();
                    break;
            }
            modelOutlineRenderer.drawOutline(nearest, config.highlightInteractionWidth(), npcHighlightColor, config.highlightInteractionFeather());
        }
    }

    private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color) {
        if (dest == null) {
            return;
        }

        final Polygon poly;

        Player localPlayer = client.getLocalPlayer();
        Actor ta = localPlayer.getInteracting();

        if (ta == null) {
            poly = getCanvasTargetTileAreaPoly(client, dest, 0.5, client.getPlane(), 0);
            Point canvasLoc = Perspective.getCanvasImageLocation(client, dest, ARROW_ICON, 150 + (int) (20 * Math.sin(client.getGameCycle() / 10.0)));

            if (canvasLoc != null) {
                graphics.drawImage(ARROW_ICON, canvasLoc.getX(), canvasLoc.getY(), null);
            }
        } else {
            poly = getCanvasTargetTileAreaPoly(client, ta.getLocalLocation(), 0.5, client.getPlane(), 0);
        }

        if (poly != null) {
            OverlayUtil.renderPolygon(graphics, poly, color);
        }

    }

    public static Polygon getCanvasTargetTileAreaPoly(
            @Nonnull Client client,
            @Nonnull LocalPoint localLocation,
            double size,
            int plane,
            int zOffset) {

        final byte[][][] tileSettings = client.getTileSettings();

        final int sceneX = localLocation.getSceneX();
        final int sceneY = localLocation.getSceneY();

        if (sceneX < 0 || sceneY < 0 || sceneX >= Perspective.SCENE_SIZE || sceneY >= Perspective.SCENE_SIZE) {
            return null;
        }

        int tilePlane = plane;
        if (plane < Constants.MAX_Z - 1 && (tileSettings[1][sceneX][sceneY] & TILE_FLAG_BRIDGE) == TILE_FLAG_BRIDGE) {
            tilePlane = plane + 1;
        }

        Polygon poly = new Polygon();
        int resolution = 128;
        final int height = getHeight(client, localLocation.getX(), localLocation.getY(), tilePlane) - zOffset;

        for (int i = 0; i < resolution; i++) {
            double angle = ((float) i / resolution) * 2 * Math.PI;
            double offsetX = Math.cos(angle);
            double offsetY = Math.sin(angle);
            int x = (int) (localLocation.getX() + (offsetX * Perspective.LOCAL_TILE_SIZE * size));
            int y = (int) (localLocation.getY() + (offsetY * Perspective.LOCAL_TILE_SIZE * size));
            Point p = Perspective.localToCanvas(client, x, y, height);
            if (p == null) {
                continue;
            }
            poly.addPoint(p.getX(), p.getY());

        }

        return poly;
    }

    private static int getHeight(@Nonnull Client client, int localX, int localY, int plane) {
        int sceneX = localX >> Perspective.LOCAL_COORD_BITS;
        int sceneY = localY >> Perspective.LOCAL_COORD_BITS;
        if (sceneX >= 0 && sceneY >= 0 && sceneX < Perspective.SCENE_SIZE && sceneY < Perspective.SCENE_SIZE) {
            int[][][] tileHeights = client.getTileHeights();

            int x = localX & (Perspective.LOCAL_TILE_SIZE - 1);
            int y = localY & (Perspective.LOCAL_TILE_SIZE - 1);
            int var8 = x * tileHeights[plane][sceneX + 1][sceneY] + (Perspective.LOCAL_TILE_SIZE - x) * tileHeights[plane][sceneX][sceneY] >> Perspective.LOCAL_COORD_BITS;
            int var9 = tileHeights[plane][sceneX][sceneY + 1] * (Perspective.LOCAL_TILE_SIZE - x) + x * tileHeights[plane][sceneX + 1][sceneY + 1] >> Perspective.LOCAL_COORD_BITS;
            return (Perspective.LOCAL_TILE_SIZE - y) * var8 + y * var9 >> Perspective.LOCAL_COORD_BITS;
        }

        return 0;
    }

}