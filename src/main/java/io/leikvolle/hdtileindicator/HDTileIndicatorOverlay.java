package io.leikvolle.hdtileindicator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;

import static net.runelite.api.Constants.TILE_FLAG_BRIDGE;
@Slf4j
public class HDTileIndicatorOverlay extends Overlay
{
    private final Client client;
    private final HDTileIndicatorConfig config;
    private final HDTileIndicatorPlugin plugin;

    private final BufferedImage ARROW_ICON;
    private BufferedImage highlightImage;
    private long lastHighlightRender = 0;

    @Inject
    private HDTileIndicatorOverlay(Client client, HDTileIndicatorConfig config, HDTileIndicatorPlugin plugin, SkillIconManager iconManager)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);

        ARROW_ICON = ImageUtil.loadImageResource(HDTileIndicatorPlugin.class, "arrow.png");
        
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

        if (config.highlightDestinationTile())
        {
            renderTile(graphics, client.getLocalDestinationLocation(), config.highlightDestinationColor());
        }

        renderHighlight(graphics);


        return null;
    }

    private Dimension renderHighlight(Graphics2D graphics) {

        if (client.isMenuOpen()) {
            return null;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();
        int last = menuEntries.length - 1;

        if (last < 0)
        {
            return null;
        }

        MenuEntry menuEntry = menuEntries[last];
        String target = menuEntry.getTarget();
        String option = menuEntry.getOption();
        MenuAction type = MenuAction.of(menuEntry.getType());

        if (type == MenuAction.RUNELITE_OVERLAY || type == MenuAction.CC_OP_LOW_PRIORITY)
        {
            // These are always right click only
            return null;
        }

        if (Strings.isNullOrEmpty(option) || Strings.isNullOrEmpty(target))
        {
            return null;
        }

        Color highlightColor;
        log.info(option);
        switch (option)
        {
            case "Attack":
                highlightColor = new Color(0xFFFF0000);
                break;
            default:
                highlightColor = new Color(0xffFFEF00);
                break;
        }

        Point mousePosition = client.getMouseCanvasPosition();

        Actor nearest = null;
        int nearesOffset = Integer.MAX_VALUE;
        for (Actor actor:client.getNpcs()) {
            String name = actor.getName();
            if (name != null && name.equals(target.replaceAll("(<.*?>)?(\\(.*?\\))?","").trim())) {
                Shape shape = Perspective.getClickbox(client, actor.getModel(), actor.getOrientation(), actor.getLocalLocation());
                if (shape != null) {
                    Model model = actor.getModel();
                    if (shape.contains(mousePosition.getX(), mousePosition.getY())) {
                        log.info(actor.getName() + "." + target);
                        if (nearesOffset > model.getBufferOffset()) {
                            nearest = actor;
                            nearesOffset = model.getBufferOffset();
                        }
                    }
                }
            }
        }

        if (nearest != null) {
            drawHighlight(graphics, nearest.getModel(), nearest.getLocalLocation().getX(), nearest.getLocalLocation().getY(), nearest.getOrientation(), highlightColor);
        }

        return null;
    }

    private void drawHighlight(final Graphics2D graphics, Model model, int localX, int localY, int rotation, Color color) {
        long start = System.currentTimeMillis();
        if (model == null) {
            log.info("Model is null");
            return;
        }
        int scalingFactor = config.scalingFactor();

        if (System.currentTimeMillis() - lastHighlightRender < (1000 / config.highlightInteractionFramerate())) {
            graphics.drawImage(highlightImage, 0, 0, null);
            return;
        }

        highlightImage = new BufferedImage(client.getCanvasWidth()/scalingFactor, client.getCanvasHeight()/scalingFactor, BufferedImage.TYPE_4BYTE_ABGR);

        int vCount = model.getVerticesCount();
        int[] x3d = model.getVerticesX();
        int[] y3d = model.getVerticesY();
        int[] z3d = model.getVerticesZ();

        int[] x2d = new int[vCount];
        int[] y2d = new int[vCount];

        int localZ = getHeight(client, localX, localY, client.getPlane());

        long before = System.currentTimeMillis();
        Perspective.modelToCanvas(client, vCount, localX, localY, localZ, rotation, x3d, z3d, y3d, x2d, y2d);
        log.info(System.currentTimeMillis()-before + " " + 1000.0/(System.currentTimeMillis()-before) + " Model");


        int tCount = model.getTrianglesCount();
        int[] tx = model.getTrianglesX();
        int[] ty = model.getTrianglesY();
        int[] tz = model.getTrianglesZ();

        before = System.currentTimeMillis();
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        org.locationtech.jts.geom.Polygon[] polygons = new org.locationtech.jts.geom.Polygon[tCount];

        int tAfterCulling = 0;
        for (int i = 0; i < tCount; i++) {
            if (getTriDirection(x2d[tx[i]], y2d[tx[i]], x2d[ty[i]], y2d[ty[i]], x2d[tz[i]], y2d[tz[i]]) >= 0) {
                continue;
            }
            Polygon p = new Polygon(
                    new int[]{x2d[tx[i]] / scalingFactor,x2d[ty[i]]/scalingFactor,x2d[tz[i]]/scalingFactor},
                    new int[]{y2d[tx[i]]/scalingFactor,y2d[ty[i]]/scalingFactor,y2d[tz[i]]/scalingFactor},
                    3);
            Rectangle b = p.getBounds();
            minX = Math.min(minX, b.x);
            minY = Math.min(minY, b.y);
            maxX = Math.max(maxX, b.x+b.width);
            maxY = Math.max(maxY, b.y+b.height);
            //polygons.add(p);
            fillPolygon(highlightImage, p, config.highlightInteractionColor());

            /*
            Coordinate[] coords = new Coordinate[] {
                    new Coordinate(x2d[tx[i]], y2d[tx[i]]),
                    new Coordinate(x2d[ty[i]], y2d[ty[i]]),
                    new Coordinate(x2d[tz[i]], y2d[tz[i]]),
                    new Coordinate(x2d[tx[i]], y2d[tx[i]])
            };
            org.locationtech.jts.geom.Polygon p = new GeometryFactory().createPolygon(coords);
            polygons[tAfterCulling] = p;
            tAfterCulling++;*/

        }
        //GeometryCollection gc = new GeometryFactory().createGeometryCollection(Arrays.copyOfRange(polygons, 0, tAfterCulling));
        //log.info(tCount + " " + tAfterCulling);
        //ShapeWriter sw = new ShapeWriter();
        Graphics2D g = highlightImage.createGraphics();
        //g.draw(sw.toShape(gc.union()));
        //polygons.parallelStream().forEach(polygon -> fillPolygon(highlightImage, polygon, config.highlightInteractionColor()));
        log.info(System.currentTimeMillis()-before + " " + 1000.0/(System.currentTimeMillis()-before) + " Plygon");
        //highlightImage = ImageUtil.resizeImage(highlightImage, client.getCanvasWidth(), client.getCanvasHeight());
        before = System.currentTimeMillis();
        highlightImage = outlineImage(highlightImage, new Rectangle(minX, minY, maxX-minX, maxY-minY), color);
        log.info(System.currentTimeMillis()-before + " " + 1000.0/(System.currentTimeMillis()-before) + " Outline");

        graphics.drawImage(highlightImage, 0, 0, null);
        lastHighlightRender = System.currentTimeMillis();
        log.info(System.currentTimeMillis()-start + " " + 1000.0/(System.currentTimeMillis()-before) + " Total");

    }

    private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color)
    {
        if (dest == null)
        {
            return;
        }

        final Polygon poly;

        Player localPlayer = client.getLocalPlayer();
        Actor ta = localPlayer.getInteracting();

        if (ta == null) {
            poly = getCanvasTargetTileAreaPoly(client, dest, 0.5, client.getPlane(), 0);
            Point canvasLoc = Perspective.getCanvasImageLocation(client, dest, ARROW_ICON, 150 + (int)(20 * Math.sin(client.getGameCycle()/10.0)));

            if (canvasLoc != null) {
                graphics.drawImage(ARROW_ICON, canvasLoc.getX(), canvasLoc.getY(), null);
            }
        } else {
            poly = getCanvasTargetTileAreaPoly(client, ta.getLocalLocation(), 0.5, client.getPlane(), 0);
        }

        if (poly != null)
        {
            OverlayUtil.renderPolygon(graphics, poly, color);
        }

    }

    public static Polygon getCanvasTargetTileAreaPoly(
            @Nonnull Client client,
            @Nonnull LocalPoint localLocation,
            double size,
            int plane,
            int zOffset)
    {

        final byte[][][] tileSettings = client.getTileSettings();

        final int sceneX = localLocation.getSceneX();
        final int sceneY = localLocation.getSceneY();

        if (sceneX < 0 || sceneY < 0 || sceneX >= Perspective.SCENE_SIZE || sceneY >= Perspective.SCENE_SIZE)
        {
            return null;
        }

        int tilePlane = plane;
        if (plane < Constants.MAX_Z - 1 && (tileSettings[1][sceneX][sceneY] & TILE_FLAG_BRIDGE) == TILE_FLAG_BRIDGE)
        {
            tilePlane = plane + 1;
        }

        Polygon poly = new Polygon();
        int resolution = 128;
        final int height = getHeight(client, localLocation.getX(), localLocation.getY(), tilePlane) - zOffset;

        for (int i = 0; i < resolution; i++) {
            double angle = ((float)i/resolution)*2*Math.PI;
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

    private static int getHeight(@Nonnull Client client, int localX, int localY, int plane)
    {
        int sceneX = localX >> Perspective.LOCAL_COORD_BITS;
        int sceneY = localY >> Perspective.LOCAL_COORD_BITS;
        if (sceneX >= 0 && sceneY >= 0 && sceneX < Perspective.SCENE_SIZE && sceneY < Perspective.SCENE_SIZE)
        {
            int[][][] tileHeights = client.getTileHeights();

            int x = localX & (Perspective.LOCAL_TILE_SIZE - 1);
            int y = localY & (Perspective.LOCAL_TILE_SIZE - 1);
            int var8 = x * tileHeights[plane][sceneX + 1][sceneY] + (Perspective.LOCAL_TILE_SIZE - x) * tileHeights[plane][sceneX][sceneY] >> Perspective.LOCAL_COORD_BITS;
            int var9 = tileHeights[plane][sceneX][sceneY + 1] * (Perspective.LOCAL_TILE_SIZE - x) + x * tileHeights[plane][sceneX + 1][sceneY + 1] >> Perspective.LOCAL_COORD_BITS;
            return (Perspective.LOCAL_TILE_SIZE - y) * var8 + y * var9 >> Perspective.LOCAL_COORD_BITS;
        }

        return 0;
    }

    public static BufferedImage erodeImage(final BufferedImage image, final Color color)
    {
        final BufferedImage erodedImage = image;
        for (int x = 0; x < erodedImage.getWidth(); x++)
        {
            for (int y = 0; y < erodedImage.getHeight(); y++)
            {
                int pixel = image.getRGB(x, y);
                if (pixel == color.getRGB()) {
                    erodedImage.setRGB(x, y, Color.TRANSLUCENT);
                }
            }
        }
        return erodedImage;
    }

    private BufferedImage outlineImage(BufferedImage image, Rectangle bounds, Color c) {
        BufferedImage outline = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        int[] dc = {-1,0,1};
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};
        int minX = Math.max(bounds.x, 0);
        int maxX = Math.min(bounds.x+bounds.width, image.getWidth());
        int minY = Math.max(bounds.y, 0);
        int maxY = Math.min(bounds.y+bounds.height, image.getHeight());
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                if (image.getRGB(x, y) != 0x00000000) {
                    for (int i = 0; i < dx.length; i++) {
                        int xx = x + dx[i];
                        int yy = y + dy[i];
                        if (xx >= 0 && xx < image.getWidth() && yy >= 0 && yy < image.getHeight() && image.getRGB(xx, yy) == 0x00000000) {
                            outline.setRGB(x, y, c.getRGB());
                        }
                    }
                    /*for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            int xx = x + dc[i];
                            int yy = y + dc[j];
                            if (xx >= 0 && xx < image.getWidth() && yy >= 0 && yy < image.getHeight() && image.getRGB(xx, yy) == 0x00000000) {
                                outline.setRGB(x, y, c.getRGB());
                            }
                        }
                    }*/
                }
            }
        }
        return outline;
    }

    private void fillPolygon(BufferedImage image, Polygon p, Color c) {
        Rectangle bounds = p.getBounds();
        int minX = Math.max(bounds.x, 0);
        int maxX = Math.min(bounds.x+bounds.width, image.getWidth());
        int minY = Math.max(bounds.y, 0);
        int maxY = Math.min(bounds.y+bounds.height, image.getHeight());
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                if (p.contains(x, y)) {
                    image.setRGB(x, y, c.getRGB());
                }
            }
        }
    }

    private void fillPolygons(BufferedImage image, List<Polygon> polygons, Rectangle bounds, Color c) {
        int minX = Math.max(bounds.x, 0);
        int maxX = Math.min(bounds.x+bounds.width, image.getWidth());
        int minY = Math.max(bounds.y, 0);
        int maxY = Math.min(bounds.y+bounds.height, image.getHeight());
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                int finalX = x;
                int finalY = y;
                if (polygons.parallelStream().anyMatch(p -> p.contains(finalX, finalY))) {
                    image.setRGB(x, y, c.getRGB());
                }
            }
        }
    }

    private int getTriDirection(int x1, int y1, int x2, int y2, int x3, int y3) {
        int x4 = x2 - x1;
        int y4 = y2 - y1;
        int x5 = x3 - x1;
        int y5 = y3 - y1;
        return x4 * y5 - y4 * x5;
    }

}